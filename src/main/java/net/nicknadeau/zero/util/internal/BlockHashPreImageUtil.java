package net.nicknadeau.zero.util.internal;

import java.math.BigInteger;
import java.util.Arrays;

/**
 * A utility class that produces a block hash pre-image.
 */
public final class BlockHashPreImageUtil {

    /**
     * Returns the block hash pre-image for a genesis block.
     *
     * @param zeroMajorVersion The major version of the Zero implementation used to construct the block.
     * @param producerPublicKey The cryptographic public key of the block producer.
     * @param blockData The block's opaque data.
     * @return the pre-image.
     */
    public static byte[] createGenesisPreImage(int zeroMajorVersion, byte[] producerPublicKey, byte[] blockData) {
        ArgChecker.assertNonNull(producerPublicKey);
        ArgChecker.assertNonNull(blockData);

        int bytesLength = 4 + producerPublicKey.length + blockData.length;
        byte[] bytes = new byte[bytesLength];

        int offset = 0;
        writeBigEndianUnsignedInt(zeroMajorVersion, bytes, offset);
        offset += 4;
        writeBytes(producerPublicKey, bytes, offset);
        offset += producerPublicKey.length;
        writeBytes(blockData, bytes, offset);
        offset += blockData.length;

        ArgChecker.assertEqualTo(offset, bytesLength);
        return bytes;
    }

    /**
     * Returns the block hash pre-image for a non-genesis block.
     *
     * @param blockNumber The block number of the block.
     * @param producerPublicKey The cryptographic public key of the block producer.
     * @param parentBlockHash The block hash of this block's parent.
     * @param blockData The block's opaque data.
     * @return the pre-image.
     */
    public static byte[] createNonGenesisPreImage(BigInteger blockNumber, byte[] producerPublicKey, byte[] parentBlockHash, byte[] blockData) {
        ArgChecker.assertNonNull(blockNumber);
        ArgChecker.assertNonNull(producerPublicKey);
        ArgChecker.assertNonNull(parentBlockHash);
        ArgChecker.assertNonNull(blockData);

        // The block number must be positive. We get its big-endian representation and then chop the leading byte if it
        // is all zeroes, since that is a sign byte and we want an unsigned representation.
        ArgChecker.assertGreaterOrEqualTo(blockNumber.signum(), 0);
        byte[] bigEndianBlockNumber = blockNumber.toByteArray();
        bigEndianBlockNumber = ((bigEndianBlockNumber.length > 1) && (bigEndianBlockNumber[0] == 0x0))
                ? Arrays.copyOfRange(bigEndianBlockNumber, 1, bigEndianBlockNumber.length)
                : bigEndianBlockNumber
                ;

        int bytesLength = bigEndianBlockNumber.length + producerPublicKey.length + parentBlockHash.length + blockData.length;
        byte[] bytes = new byte[bytesLength];

        int offset = 0;
        writeBytes(bigEndianBlockNumber, bytes, offset);
        offset += bigEndianBlockNumber.length;
        writeBytes(producerPublicKey, bytes, offset);
        offset += producerPublicKey.length;
        writeBytes(parentBlockHash, bytes, offset);
        offset += parentBlockHash.length;
        writeBytes(blockData, bytes, offset);
        offset += blockData.length;

        ArgChecker.assertEqualTo(offset, bytesLength);
        return bytes;
    }

    /**
     * Writes the big-endian unsigned integer value of {@code value} into {@code bytes} at the given offset into the
     * bytes array.
     */
    private static void writeBigEndianUnsignedInt(int value, byte[] bytes, int offset) {
        ArgChecker.assertNonNull(bytes);
        ArgChecker.assertGreaterOrEqualTo(bytes.length, offset + 4);

        bytes[offset] = (byte) (value >> 24);
        bytes[offset + 1] = (byte) (value >> 16);
        bytes[offset + 2] = (byte) (value >> 8);
        bytes[offset + 3] = (byte) value;
    }

    /**
     * Writes the bytes in {@code value} directly into {@code bytes} at the given offset into the bytes array.
     */
    private static void writeBytes(byte[] value, byte[] bytes, int offset) {
        ArgChecker.assertNonNull(value);
        ArgChecker.assertNonNull(bytes);
        ArgChecker.assertGreaterOrEqualTo(bytes.length, offset + value.length);

        System.arraycopy(value, 0, bytes, offset, value.length);
    }
}
