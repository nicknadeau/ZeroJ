package net.nicknadeau.zero.blockchain;

import net.nicknadeau.zero.block.Block;
import net.nicknadeau.zero.block.BlockStatus;
import net.nicknadeau.zero.blockchain.callback.ZeroCallbacks;
import net.nicknadeau.zero.storage.ZeroDatabase;
import net.nicknadeau.zero.type.Receipt;
import net.nicknadeau.zero.type.ReceiptCode;
import net.nicknadeau.zero.util.HashFunction;
import net.nicknadeau.zero.util.SignatureVerifier;
import net.nicknadeau.zero.util.internal.ArgChecker;
import net.nicknadeau.zero.util.internal.BlockValidator;

/**
 * The layer zero blockchain.
 *
 * Instances of this class should be constructed using {@link ZeroBlockchain.Builder}.
 */
public final class ZeroBlockchain {
    private final Object lock = new Object();
    private final ZeroDatabase database;
    private final HashFunction hashFunction;
    private final SignatureVerifier signatureVerifier;
    private final ZeroCallbacks callbacks;

    private ZeroBlockchain(ZeroDatabase database, HashFunction hashFunction, SignatureVerifier signatureVerifier, ZeroCallbacks callbacks) {
        ArgChecker.assertNonNull(database);
        ArgChecker.assertNonNull(hashFunction);
        ArgChecker.assertNonNull(signatureVerifier);
        ArgChecker.assertNonNull(callbacks);
        this.database = database;
        this.hashFunction = hashFunction;
        this.signatureVerifier = signatureVerifier;
        this.callbacks = callbacks;
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
     * This is a thread-safe blocking method. Only a single thread is able to add a block at a time, so that internal
     * consistency can be maintained.
     *
     * @param block The block to add.
     * @return the receipt of the add operation.
     */
    public Receipt addBlock(Block block) {
        synchronized (this.lock) {
            try {
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

                //TODO: Handle layer state mismatch recovery.

                // Add the block to layer zero.
                if (!this.database.saveBlockAndStatus(block, BlockStatus.ADDED)) {
                    return Receipt.failedReceipt(ReceiptCode.FAILED, "failed to save block to database");
                }

                // Add the block to layer one.
                layerOneCode = this.callbacks.getLayerOneAddBlockCallback().add(block);
                return (layerOneCode == 0) ? Receipt.successfulReceipt() : Receipt.layerOneFailedReceipt(layerOneCode);
            } catch (Exception e) {
                return Receipt.unexpectedErrorReceipt(e);
            }
        }
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
        public ZeroBlockchain build() {
            return new ZeroBlockchain(this.database
                    , this.hashFunction
                    , this.signatureVerifier
                    , this.callbacks
            );
        }
    }
}
