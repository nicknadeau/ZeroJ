package net.nicknadeau.zero.blockchain;

import net.nicknadeau.zero.block.Block;
import net.nicknadeau.zero.block.BlockStatus;
import net.nicknadeau.zero.blockchain.callback.ZeroCallbacks;
import net.nicknadeau.zero.exception.DatabaseError;
import net.nicknadeau.zero.exception.LayersOutOfSyncException;
import net.nicknadeau.zero.exception.RuntimeAssertionError;
import net.nicknadeau.zero.storage.ZeroDatabase;
import net.nicknadeau.zero.type.Receipt;
import net.nicknadeau.zero.type.ReceiptCode;
import net.nicknadeau.zero.util.HashFunction;
import net.nicknadeau.zero.util.SignatureVerifier;
import net.nicknadeau.zero.util.internal.ArgChecker;
import net.nicknadeau.zero.util.internal.BlockValidator;

import java.util.Collection;

/**
 * The layer zero blockchain.
 *
 * If the blockchain ever gets into an inconsistent state, which happens when layer zero and layer one get out of sync
 * with one another, then all public methods (unless otherwise noted) will immediately throw
 * {@link LayersOutOfSyncException}.
 *
 * Instances of this class should be constructed using {@link ZeroBlockchain.Builder}.
 */
public final class ZeroBlockchain {
    private final Object lock = new Object();
    private final ZeroDatabase database;
    private final HashFunction hashFunction;
    private final SignatureVerifier signatureVerifier;
    private final ZeroCallbacks callbacks;
    private boolean isOutOfSync;

    private ZeroBlockchain(ZeroDatabase database, HashFunction hashFunction, SignatureVerifier signatureVerifier, ZeroCallbacks callbacks) throws DatabaseError {
        ArgChecker.assertNonNull(database);
        ArgChecker.assertNonNull(hashFunction);
        ArgChecker.assertNonNull(signatureVerifier);
        ArgChecker.assertNonNull(callbacks);
        this.database = database;
        this.hashFunction = hashFunction;
        this.signatureVerifier = signatureVerifier;
        this.callbacks = callbacks;
        this.isOutOfSync = this.database.containsPendingBlocks();
    }

    /**
     * Returns {@code true} if and only if this blockchain is out of sync and {@code false} otherwise.
     *
     * If the blockchain is out of sync then all public methods will throw {@link LayersOutOfSyncException} when invoked
     * unless otherwise noted. This method, of course, is an exception to that rule. In the case that the blockchain is
     * out of sync, a recovery can be attempted via {@link ZeroBlockchain#recover()}.
     *
     * @return whether or not the blockchain is out of sync.
     */
    public boolean isOutOfSync() {
        synchronized (this.lock) {
            return this.isOutOfSync;
        }
    }

    /**
     * Attempts to run the recovery mechanisms on this blockchain in order to ensure the blockchain is consistent. If
     * the blockchain is already consistent then this method will do nothing. Otherwise, if it is in an inconsistent
     * state then this method will attempt to repair the state of the blockchain to put it back into a consistent state.
     *
     * If the blockchain is inconsistent and therefore all public methods are throwing {@link LayersOutOfSyncException},
     * this method will never throw such an exception and can always be invoked at any time, regardless of the state of
     * the blockchain.
     *
     * Returns a successful receipt if the recovery process succeeded, in which case the blockchain is guaranteed to be
     * consistent and any methods previously throwing {@link LayersOutOfSyncException} will no longer throw that error
     * (unless invoking those methods causes a state change that returns us into an inconsistent state, of course).
     *
     * Returns an unsuccessful receipt if the recovery could not be performed successfully, in which case the blockchain
     * is guaranteed to be in an inconsistent state and therefore will continue to throw {@link LayersOutOfSyncException}.
     *
     * This is a thread-safe blocking method. Only a single thread is able to enter ANY public method at a time, so that
     * internal consistency can be maintained.
     *
     * @return the receipt of the recovery operation.
     */
    public Receipt recover() {
        synchronized (this.lock) {
            if (!this.isOutOfSync) {
                return Receipt.successfulReceipt();
            }

            try {
                Collection<Block> blocks = this.database.findBlocksByStatus(BlockStatus.PENDING_ADDITION);
                if (blocks.size() > 1) {
                    throw RuntimeAssertionError.unexpected();
                } else if (blocks.size() == 1) {
                    Receipt receipt = addPendingBlock(blocks.iterator().next());
                    if (receipt.getCode() == ReceiptCode.SUCCESS) {
                        this.isOutOfSync = false;
                    }
                    return receipt;
                }

                // If we are here, then there was no pending addition block to recover, so try a pending deletion.
                blocks = this.database.findBlocksByStatus(BlockStatus.PENDING_DELETION);
                if (blocks.size() > 1) {
                    throw RuntimeAssertionError.unexpected();
                } else if (blocks.size() == 1) {
                    Receipt receipt = removePendingBlock(blocks.iterator().next());
                    if (receipt.getCode() == ReceiptCode.SUCCESS) {
                        this.isOutOfSync = false;
                    }
                    return receipt;
                } else {
                    // Then somehow we are both out of sync and yet have no pending blocks of any kind.
                    // We should never be in this state.
                    throw RuntimeAssertionError.unexpected();
                }
            } catch (Exception e) {
                return Receipt.unexpectedErrorReceipt(e);
            }
        }
    }

    /**
     * Attempts to remove the specified block from this blockchain and returns a receipt descriptive of the outcome of
     * this operation. As a result, calling into this method will cause the
     * {@link net.nicknadeau.zero.blockchain.callback.LayerOneDeleteBlockCallback} callback to be invoked.
     *
     * This operation is considered successful if and only if, both layer zero and layer one removed the block from the
     * blockchain.
     *
     * This is a thread-safe blocking method. Only a single thread is able to call ANY public method at a time, so that
     * internal consistency can be maintained.
     *
     * @param block The block to delete.
     * @return the receipt of the removal operation.
     * @throws LayersOutOfSyncException if removing this block caused the two layers to become out of sync, or if the
     * blockchain is already out of sync.
     */
    public Receipt removeBlock(Block block) throws LayersOutOfSyncException {
        synchronized (this.lock) {
            if (this.isOutOfSync) {
                throw new LayersOutOfSyncException();
            }
            if (block == null) {
                return Receipt.failedReceipt(ReceiptCode.DOES_NOT_EXIST, "block is null");
            }

            try {
                if (!this.database.updateBlockStatus(block.getBlockHash(), BlockStatus.PENDING_DELETION)) {
                    return Receipt.failedReceipt(ReceiptCode.FAILED, "failed to mark block for deletion");
                }

                return removePendingBlock(block);
            } catch (LayersOutOfSyncException e) {
                // In this case, we actually do want to allow the error to propagate.
                this.isOutOfSync = true;
                throw e;
            } catch (Exception e) {
                return Receipt.unexpectedErrorReceipt(e);
            }
        }
    }

    /**
     * Attempts to add the specified block to this blockchain and returns a receipt descriptive of the outcome of this
     * operation.
     *
     * This operation consists of two parts: validating the block and adding the block. No block shall be added that
     * fails any validation check. As a result, calling into this method will cause the
     * {@link net.nicknadeau.zero.blockchain.callback.LayerOneValidateBlockCallback} callback to be invoked during the
     * validation stage and the {@link net.nicknadeau.zero.blockchain.callback.LayerOneAddBlockCallback} callback to be
     * invoked once the block is actually added.
     *
     * This operation is considered successful if and only if, both layer zero and layer one added the block to the
     * blockchain. Note that if the block already exists in the blockchain, then re-adding it is a failure.
     *
     * This is a thread-safe blocking method. Only a single thread is able to call ANY public method at a time, so that
     * internal consistency can be maintained.
     *
     * @param block The block to add.
     * @return the receipt of the add operation.
     * @throws LayersOutOfSyncException if adding this block caused the two layers to become out of sync, or if the
     * blockchain is already out of sync.
     */
    public Receipt addBlock(Block block) throws LayersOutOfSyncException {
        synchronized (this.lock) {
            if (this.isOutOfSync) {
                throw new LayersOutOfSyncException();
            }

            try {
                // Perform the block verifications.
                Receipt receipt = validateBlock(block);
                if (receipt.getCode() != ReceiptCode.SUCCESS) {
                    return receipt;
                }

                // Add the block to layer zero and mark it as pending.
                if (!this.database.saveBlockAndStatus(block, BlockStatus.PENDING_ADDITION)) {
                    return Receipt.failedReceipt(ReceiptCode.FAILED, "failed to save block to database");
                }

                // Add the block to layer one and finish adding it to layer zero.
                return addPendingBlock(block);
            } catch (LayersOutOfSyncException e) {
                // In this case, we actually do want to allow the error to propagate.
                this.isOutOfSync = true;
                throw e;
            } catch (Exception e) {
                return Receipt.unexpectedErrorReceipt(e);
            }
        }
    }

    /**
     * Validates the block using both the layer zero and layer one validation logic.
     */
    private Receipt validateBlock(Block block) {
        // Perform the layer zero block verifications.
        Receipt receipt = BlockValidator.runLayerZeroValidation(block, this.database, this.hashFunction, this.signatureVerifier);
        if (receipt.getCode() != ReceiptCode.SUCCESS) {
            return receipt;
        }

        // Perform the layer one block verifications.
        int layerOneCode = this.callbacks.getLayerOneValidateBlockCallback().validate(block);
        if (layerOneCode != 0) {
            return Receipt.layerOneFailedReceipt(layerOneCode);
        }

        return Receipt.successfulReceipt();
    }

    /**
     * Adds the given block to layer one and then updates the status of the block to {@link BlockStatus#ADDED}.
     *
     * ASSUMPTION: The status of the block is already {@link BlockStatus#PENDING_ADDITION} on disk.
     */
    private Receipt addPendingBlock(Block block) throws LayersOutOfSyncException, DatabaseError {
        // Add the block to layer one.
        int layerOneCode = this.callbacks.getLayerOneAddBlockCallback().add(block);
        if (layerOneCode != 0) {
            return Receipt.layerOneFailedReceipt(layerOneCode);
        }

        // Finally, now we can update the status to being fully added to the blockchain.
        if (!this.database.updateBlockStatus(block.getBlockHash(), BlockStatus.ADDED)) {
            throw new LayersOutOfSyncException();
        }

        return Receipt.successfulReceipt();
    }

    /**
     * Removes the given block from layer one and then removes it from layer zero.
     *
     * ASSUMPTION: The status of the block is already {@link BlockStatus#PENDING_DELETION} on disk.
     */
    private Receipt removePendingBlock(Block block) throws LayersOutOfSyncException, DatabaseError {
        int layerOneCode = this.callbacks.getLayerOneDeleteBlockCallback().delete(block);
        if (layerOneCode != 0) {
            return Receipt.layerOneFailedReceipt(layerOneCode);
        }

        // Finally, now we can remove the block.
        if (!this.database.removeBlockByHash(block.getBlockHash())) {
            throw new LayersOutOfSyncException();
        }

        return Receipt.successfulReceipt();
    }

    //------------------------------------------------------------------------------------------------------------------

    /**
     * A builder which constructs new instances of {@link ZeroBlockchain}.
     */
    public static final class Builder {
        private ZeroDatabase database;
        private HashFunction hashFunction;
        private SignatureVerifier signatureVerifier;
        private ZeroCallbacks callbacks;

        private Builder() {}

        /**
         * Returns a new builder instance.
         *
         * @return the new builder.
         */
        public static Builder newBuilder() {
            return new Builder();
        }

        /**
         * Uses the specified database.
         *
         * @param database The database to use.
         * @return this builder.
         */
        public Builder withDatabase(ZeroDatabase database) {
            this.database = database;
            return this;
        }

        /**
         * Uses the specified hash function.
         *
         * @param function The hash function to use.
         * @return this builder.
         */
        public Builder withHashFunction(HashFunction function) {
            this.hashFunction = function;
            return this;
        }

        /**
         * Uses the specified signature verifier.
         *
         * @param verifier The signature verifier to use.
         * @return this builder.
         */
        public Builder withSignatureVerifier(SignatureVerifier verifier) {
            this.signatureVerifier = verifier;
            return this;
        }

        /**
         * Uses the specified callbacks.
         *
         * @param callbacks The callbacks to use.
         * @return this builder.
         */
        public Builder withCallbacks(ZeroCallbacks callbacks) {
            this.callbacks = callbacks;
            return this;
        }

        /**
         * Returns a newly constructed instance of {@link ZeroBlockchain}, which uses each of the objects given to this
         * builder. If multiple objects of the same type were provided, only the last such object will be used.
         *
         * @return the new instance.
         */
        public ZeroBlockchain build() throws DatabaseError {
            return new ZeroBlockchain(this.database
                    , this.hashFunction
                    , this.signatureVerifier
                    , this.callbacks
            );
        }
    }
}
