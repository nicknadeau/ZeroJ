package net.nicknadeau.zero.util.internal;

import net.nicknadeau.zero.ZeroVersion;
import net.nicknadeau.zero.block.Block;
import net.nicknadeau.zero.exception.RuntimeAssertionError;
import net.nicknadeau.zero.storage.ZeroDatabase;
import net.nicknadeau.zero.type.Receipt;
import net.nicknadeau.zero.type.ReceiptCode;
import net.nicknadeau.zero.util.HashFunction;
import net.nicknadeau.zero.util.SignatureVerifier;

import java.math.BigInteger;
import java.util.Arrays;

/**
 * The validation class that performs block validation checks for layer zero.
 *
 * Note that this validator will NOT run the layer one block validation checks.
 */
public final class BlockValidator {

    /**
     * Validates the given {@code block} and returns a receipt which will have code
     * {@link net.nicknadeau.zero.type.ReceiptCode#SUCCESS} if and only if the block passes all layer zero validation
     * checks and otherwise will return one of the following descriptive failure codes:
     *
     * - {@link ReceiptCode#VERSION_MISMATCH}: if the block was produced with a conflicting major version.
     * - {@link ReceiptCode#INVALID_PARAMETER}: if the block has invalid attributes.
     * - {@link ReceiptCode#DOES_NOT_EXIST}: if the block is {@code null} or the block is a non-genesis block and its
     *   parent block does not exist.
     * - {@link ReceiptCode#EXISTS}: if the block already exists, or, in the case of the genesis block, any other
     *   genesis block already exists.
     * - {@link ReceiptCode#UNEXPECTED}: if an unexpected error occurred. In this case, the returned receipt will not
     *   have an error message, like each of the other cases, but instead will have the error object which caused the
     *   exceptional condition.
     *
     * @param block The block to validate.
     * @param database The database backing the blockchain.
     * @param hashFunction The hash function.
     * @param signatureVerifier The signature verification function.
     * @return the receipt of the validation action.
     */
    public static Receipt runLayerZeroValidation(Block block, ZeroDatabase database, HashFunction hashFunction, SignatureVerifier signatureVerifier) {
        ArgChecker.assertNonNull(database);
        ArgChecker.assertNonNull(hashFunction);
        ArgChecker.assertNonNull(signatureVerifier);

        try {
            if (block == null) {
                return Receipt.failedReceipt(ReceiptCode.DOES_NOT_EXIST, "block is null");
            }
            if (block.getBlockData() == null) {
                return Receipt.failedReceipt(ReceiptCode.INVALID_PARAMETER, "block data is null");
            }
            if (block.getBlockNumber() == null) {
                return Receipt.failedReceipt(ReceiptCode.INVALID_PARAMETER, "block number is null");
            }
            if (block.getBlockNumber().signum() == -1) {
                return Receipt.failedReceipt(ReceiptCode.INVALID_PARAMETER, "block number is negative");
            }
            byte[] producerPublicKey = block.getBlockProducerPublicKey();
            if (producerPublicKey == null) {
                return Receipt.failedReceipt(ReceiptCode.INVALID_PARAMETER, "producer public key is null");
            }
            if (producerPublicKey.length == 0) {
                return Receipt.failedReceipt(ReceiptCode.INVALID_PARAMETER, "producer public key is empty");
            }
            byte[] blockHash = block.getBlockHash();
            if (blockHash == null) {
                return Receipt.failedReceipt(ReceiptCode.INVALID_PARAMETER, "block hash is null");
            }
            if (blockHash.length == 0) {
                return Receipt.failedReceipt(ReceiptCode.INVALID_PARAMETER, "block hash is empty");
            }
            if (database.blockExists(blockHash)) {
                return Receipt.failedReceipt(ReceiptCode.EXISTS, "block already exists");
            }
            byte[] blockSignature = block.getBlockSignature();
            if (blockSignature == null) {
                return Receipt.failedReceipt(ReceiptCode.INVALID_PARAMETER, "block signature is null");
            } if (blockSignature.length == 0) {
                return Receipt.failedReceipt(ReceiptCode.INVALID_PARAMETER, "block signature is empty");
            }

            boolean isGenesisBlock = block.getBlockNumber().signum() == 0;
            if (isGenesisBlock) {
                if (block.getLayerZeroMajorVersion() != ZeroVersion.ZERO_MAJOR_VERSION) {
                    return Receipt.failedReceipt(ReceiptCode.VERSION_MISMATCH, "incompatible block major version");
                }
                if (block.getParentBlockHash() != null) {
                    return Receipt.failedReceipt(ReceiptCode.INVALID_PARAMETER, "genesis block has non-null parent block hash");
                }
            } else {
                byte[] parentHash = block.getParentBlockHash();
                if (parentHash == null) {
                    return Receipt.failedReceipt(ReceiptCode.INVALID_PARAMETER, "parent block hash is null");
                }
                if (parentHash.length == 0) {
                    return Receipt.failedReceipt(ReceiptCode.INVALID_PARAMETER, "parent block hash is empty");
                }
                Block parentBlock = database.findBlockByHash(parentHash);
                if (parentBlock == null) {
                    return Receipt.failedReceipt(ReceiptCode.DOES_NOT_EXIST, "parent block does not exist");
                }
                BigInteger parentBlockNumber = parentBlock.getBlockNumber();
                if (parentBlockNumber == null) {
                    throw RuntimeAssertionError.unexpected();
                }
                if (!parentBlockNumber.add(BigInteger.ONE).equals(block.getBlockNumber())) {
                    return Receipt.failedReceipt(ReceiptCode.INVALID_PARAMETER, "block number is not one larger than parent block number");
                }
            }

            // Validate the block hash by hashing the block's pre-image and comparing the result.
            byte[] blockHashPreImage = (isGenesisBlock)
                    ? BlockHashPreImageUtil.createGenesisPreImage(block.getLayerZeroMajorVersion(), producerPublicKey, block.getBlockData())
                    : BlockHashPreImageUtil.createNonGenesisPreImage(block.getBlockNumber(), producerPublicKey, block.getParentBlockHash(), block.getBlockData());
            byte[] expectedBlockHash = hashFunction.hash(blockHashPreImage);
            if (!Arrays.equals(expectedBlockHash, blockHash)) {
                return Receipt.failedReceipt(ReceiptCode.INVALID_PARAMETER, "block hash is illegitimate");
            }

            // Validate the block's cryptographic signature.
            if (!signatureVerifier.isValidSignature(producerPublicKey, blockHash, blockSignature)) {
                return Receipt.failedReceipt(ReceiptCode.INVALID_PARAMETER, "block signature is illegitimate");
            }

            // If we made it this far then all of the checks passed and we can return a successful receipt.
            return Receipt.successfulReceipt();
        } catch (Exception e) {
            return Receipt.unexpectedErrorReceipt(e);
        }
    }
}
