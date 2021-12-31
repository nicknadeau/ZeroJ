package net.nicknadeau.zero.util.internal;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class PreImageUtilTests {

    @Test
    public void testGenesisPreImages() {
        for (GenesisPreImageValues testcase : getGenesisTestCases()) {
            int zeroMajorVersion = testcase.zeroMajorVersion;
            byte[] producerPublicKey = testcase.producerPublicKey;
            byte[] blockData = testcase.blockData;
            byte[] image = BlockHashPreImageUtil.createGenesisPreImage(zeroMajorVersion, producerPublicKey, blockData);
            Assert.assertArrayEquals(testcase.expectedPreImage, image);
        }
    }

    @Test
    public void testNonGenesisPreImages() {
        for (NonGenesisPreImageValues testCase : getNonGenesisTestCases()) {
            BigInteger blockNumber = testCase.blockNumber;
            byte[] producerPublicKey = testCase.producerPublicKey;
            byte[] parentBlockHash = testCase.parentBlockHash;
            byte[] blockData = testCase.blockData;
            byte[] image = BlockHashPreImageUtil.createNonGenesisPreImage(blockNumber, producerPublicKey, parentBlockHash, blockData);
            Assert.assertArrayEquals(testCase.expectedPreImage, image);
        }
    }

    private static Collection<GenesisPreImageValues> getGenesisTestCases() {
        List<GenesisPreImageValues> cases = new ArrayList<>();
        cases.add(getGenesisTestCase1());
        cases.add(getGenesisTestCase2());
        cases.add(getGenesisTestCase3());
        return cases;
    }

    private static Collection<NonGenesisPreImageValues> getNonGenesisTestCases() {
        List<NonGenesisPreImageValues> cases = new ArrayList<>();
        cases.add(getNonGenesisTestCase1());
        cases.add(getNonGenesisTestCase2());
        cases.add(getNonGenesisTestCase3());
        cases.add(getNonGenesisTestCase4());
        cases.add(getNonGenesisTestCase5());
        cases.add(getNonGenesisTestCase6());
        return cases;
    }

    //------------------------------------------------------------------------------------------------------------------

    // ==== genesis pre-image test cases ====

    private static GenesisPreImageValues getGenesisTestCase1() {
        int zeroMajorVersion = 0;
        byte[] producerPublicKey = new byte[1];
        byte[] blockData = new byte[0];
        byte[] expectedPreImage = new byte[]{ 0x0, 0x0, 0x0, 0x0, 0x0 };
        return new GenesisPreImageValues(zeroMajorVersion, producerPublicKey, blockData, expectedPreImage);
    }

    private static GenesisPreImageValues getGenesisTestCase2() {
        int zeroMajorVersion = 1;
        byte[] producerPublicKey = new byte[3];
        byte[] blockData = new byte[1];
        byte[] expectedPreImage = new byte[]{ 0x0, 0x0, 0x0, 0x1, 0x0, 0x0, 0x0, 0x0 };
        return new GenesisPreImageValues(zeroMajorVersion, producerPublicKey, blockData, expectedPreImage);
    }

    private static GenesisPreImageValues getGenesisTestCase3() {
        int zeroMajorVersion = (int) Long.MAX_VALUE;
        byte[] producerPublicKey = new byte[]{ (byte) 0xAF, 0x07, 0x1C, (byte) 0x90, (byte) 0xBB, 0x23 };
        byte[] blockData = new byte[]{ 0x0, (byte) 0xF7, (byte) 0xD4, 0x0, 0x3C };
        byte[] expectedPreImage = new byte[]{ (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xAF, 0x07, 0x1C, (byte) 0x90, (byte) 0xBB, 0x23, 0x0, (byte) 0xF7, (byte) 0xD4, 0x0, 0x3C };
        return new GenesisPreImageValues(zeroMajorVersion, producerPublicKey, blockData, expectedPreImage);
    }

    // ==== non-genesis pre-image test cases ====

    private static NonGenesisPreImageValues getNonGenesisTestCase1() {
        BigInteger blockNumber = BigInteger.ONE;
        byte[] producerPublicKey = new byte[1];
        byte[] parentBlockHash = new byte[1];
        byte[] blockData = new byte[0];
        byte[] expectedPreImage = new byte[]{ 0x1, 0x0, 0x0 };
        return new NonGenesisPreImageValues(blockNumber, producerPublicKey, parentBlockHash, blockData, expectedPreImage);
    }

    private static NonGenesisPreImageValues getNonGenesisTestCase2() {
        BigInteger blockNumber = BigInteger.valueOf(300_000);
        byte[] producerPublicKey = new byte[2];
        byte[] parentBlockHash = new byte[3];
        byte[] blockData = new byte[1];
        byte[] expectedPreImage = new byte[]{ 0x4, (byte) 0x93, (byte) 0xE0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0 };
        return new NonGenesisPreImageValues(blockNumber, producerPublicKey, parentBlockHash, blockData, expectedPreImage);
    }

    private static NonGenesisPreImageValues getNonGenesisTestCase3() {
        BigInteger blockNumber = BigInteger.valueOf(Integer.MAX_VALUE).setBit(31);
        byte[] producerPublicKey = new byte[]{ (byte) 0xFF };
        byte[] parentBlockHash = new byte[]{ 0x1C, 0x07, 0x42 };
        byte[] blockData = new byte[]{ (byte) 0xCC, (byte) 0xE4, 0x3E, 0x0 };
        byte[] expectedPreImage = new byte[]{ (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, 0x1C, 0x07, 0x42, (byte) 0xCC, (byte) 0xE4, 0x3E, 0x0 };
        return new NonGenesisPreImageValues(blockNumber, producerPublicKey, parentBlockHash, blockData, expectedPreImage);
    }

    private static NonGenesisPreImageValues getNonGenesisTestCase4() {
        BigInteger blockNumber = BigInteger.valueOf(Long.MAX_VALUE);
        byte[] producerPublicKey = new byte[]{ (byte) 0xA1, 0x4, 0x72, 0x0, 0x0 };
        byte[] parentBlockHash = new byte[]{ 0x0, 0x0, 0x1, 0x0 };
        byte[] blockData = new byte[]{ (byte) 0xA0, 0x0, 0x0A, 0x0 };
        byte[] expectedPreImage = new byte[]{ 0x7F, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xA1, 0x4, 0x72, 0x0, 0x0, 0x0, 0x0, 0x1, 0x0, (byte) 0xA0, 0x0, 0x0A, 0x0 };
        return new NonGenesisPreImageValues(blockNumber, producerPublicKey, parentBlockHash, blockData, expectedPreImage);
    }

    private static NonGenesisPreImageValues getNonGenesisTestCase5() {
        BigInteger intermediateNumber = BigInteger.valueOf(300_000).shiftLeft(64);
        BigInteger blockNumber = BigInteger.valueOf(Long.MAX_VALUE).setBit(63).add(intermediateNumber);
        byte[] producerPublicKey = new byte[]{ (byte) 0xB2 };
        byte[] parentBlockHash = new byte[]{ (byte) 0xFF };
        byte[] blockData = new byte[]{ 0x2F, 0x3E, 0x4B, 0x5A };
        byte[] expectedPreImage = new byte[]{ 0x4, (byte) 0x93, (byte) 0xE0, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,(byte)  0xB2, (byte) 0xFF, 0x2F, 0x3E, 0x4B, 0x5A };
        return new NonGenesisPreImageValues(blockNumber, producerPublicKey, parentBlockHash, blockData, expectedPreImage);
    }

    private static NonGenesisPreImageValues getNonGenesisTestCase6() {
        // We want a number that is positive but which nonetheless would be all 1 bits. We compose this but sign-extending
        // the zero bit into a full byte at the head of the raw number. Internally, this should be chopped so that the leading
        // zero byte is absent (an unsigned representation).
        byte[] rawNumber = new byte[]{ 0x0, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF };
        BigInteger blockNumber = new BigInteger(rawNumber);
        Assert.assertTrue(blockNumber.signum() > 0);
        byte[] producerPublicKey = new byte[1];
        byte[] parentBlockHash = new byte[1];
        byte[] blockData = new byte[0];
        byte[] expectedPreImage = new byte[]{ (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, 0x0, 0x0 };
        return new NonGenesisPreImageValues(blockNumber, producerPublicKey, parentBlockHash, blockData, expectedPreImage);
    }

    private static final class GenesisPreImageValues {
        private final int zeroMajorVersion;
        private final byte[] producerPublicKey;
        private final byte[] blockData;
        private final byte[] expectedPreImage;

        private GenesisPreImageValues(int zeroMajorVersion, byte[] producerPublicKey, byte[] blockData, byte[] expectedPreImage) {
            Assert.assertNotNull(producerPublicKey);
            Assert.assertNotNull(blockData);
            Assert.assertNotNull(expectedPreImage);

            this.zeroMajorVersion = zeroMajorVersion;
            this.producerPublicKey = Arrays.copyOf(producerPublicKey, producerPublicKey.length);
            this.blockData = Arrays.copyOf(blockData, blockData.length);
            this.expectedPreImage = Arrays.copyOf(expectedPreImage, expectedPreImage.length);
        }
    }

    private static final class NonGenesisPreImageValues {
        private final BigInteger blockNumber;
        private final byte[] producerPublicKey;
        private final byte[] parentBlockHash;
        private final byte[] blockData;
        private final byte[] expectedPreImage;

        private NonGenesisPreImageValues(BigInteger blockNumber, byte[] producerPublicKey, byte[] parentBlockHash, byte[] blockData, byte[] expectedPreImage) {
            Assert.assertNotNull(blockNumber);
            Assert.assertNotNull(producerPublicKey);
            Assert.assertNotNull(parentBlockHash);
            Assert.assertNotNull(blockData);
            Assert.assertNotNull(expectedPreImage);

            this.blockNumber = blockNumber;
            this.producerPublicKey = Arrays.copyOf(producerPublicKey, producerPublicKey.length);
            this.parentBlockHash = Arrays.copyOf(parentBlockHash, parentBlockHash.length);
            this.blockData = Arrays.copyOf(blockData, blockData.length);
            this.expectedPreImage = Arrays.copyOf(expectedPreImage, expectedPreImage.length);
        }
    }
}
