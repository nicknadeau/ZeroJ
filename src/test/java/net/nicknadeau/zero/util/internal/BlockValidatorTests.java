package net.nicknadeau.zero.util.internal;

import net.nicknadeau.zero.ZeroVersion;
import net.nicknadeau.zero.exception.RuntimeAssertionError;
import net.nicknadeau.zero.mock.BlockHelper;
import net.nicknadeau.zero.mock.DatabaseHelper;
import net.nicknadeau.zero.mock.MutableBlock;
import net.nicknadeau.zero.storage.ZeroDatabase;
import net.nicknadeau.zero.type.Receipt;
import net.nicknadeau.zero.type.ReceiptCode;
import net.nicknadeau.zero.util.HashFunction;
import net.nicknadeau.zero.util.SignatureVerifier;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.math.BigInteger;
import java.util.Collections;

public class BlockValidatorTests {
    private static final HashFunction MIRROR_HASH = (payload) -> payload;
    private static final SignatureVerifier ALWAYS_OK_VERIFIER = (key, payload, signature) -> true;

    @Test
    public void testNullArgs() throws Exception {
        MutableBlock genesisBlock = BlockHelper.newGenesisBlock(MIRROR_HASH);
        ZeroDatabase database = DatabaseHelper.newConsistentDatabase(Collections.emptySet(), Collections.singleton(genesisBlock));

        Receipt receipt = BlockValidator.runLayerZeroValidation(null, database, MIRROR_HASH, ALWAYS_OK_VERIFIER);
        Assert.assertEquals(ReceiptCode.DOES_NOT_EXIST, receipt.getCode());
        Assert.assertNotNull(receipt.getErrorMessage());
        Assert.assertNull(receipt.getUnexpectedErrorCause());

        try {
            BlockValidator.runLayerZeroValidation(new MutableBlock(), null, MIRROR_HASH, ALWAYS_OK_VERIFIER);
            Assert.fail();
        } catch (NullPointerException e) {
            // this is the expected behaviour
        }

        try {
            BlockValidator.runLayerZeroValidation(new MutableBlock(), database, null, ALWAYS_OK_VERIFIER);
            Assert.fail();
        } catch (NullPointerException e) {
            // this is the expected behaviour
        }

        try {
            BlockValidator.runLayerZeroValidation(new MutableBlock(), database, MIRROR_HASH, null);
            Assert.fail();
        } catch (NullPointerException e) {
            // this is the expected behaviour
        }
    }

    @Test
    public void testNullBlockAttributes() throws Exception {
        BigInteger number = BigInteger.ONE;
        byte[] producerKey = new byte[32];
        byte[] parentHash = new byte[32];
        byte[] hash = new byte[32];
        byte[] signature = new byte[32];
        byte[] data = new byte[32];

        MutableBlock block = new MutableBlock();
        block.setLayerZeroMajorVersion(ZeroVersion.ZERO_MAJOR_VERSION);
        block.setBlockNumber(number);
        block.setBlockHash(hash);
        block.setBlockData(data);
        block.setBlockSignature(signature);
        block.setBlockProducerPublicKey(producerKey);
        block.setParentBlockHash(parentHash);

        ZeroDatabase database = DatabaseHelper.newConsistentDatabase(Collections.emptySet(), Collections.singleton(block));

        for (int i = 0; i < 6; i++) {
            Receipt receipt = null;
            if (i == 0) {
                block.setBlockNumber(null);
                receipt = BlockValidator.runLayerZeroValidation(block, database, MIRROR_HASH, ALWAYS_OK_VERIFIER);
                block.setBlockNumber(number);
            } else if (i == 1) {
                block.setBlockProducerPublicKey(null);
                receipt = BlockValidator.runLayerZeroValidation(block, database, MIRROR_HASH, ALWAYS_OK_VERIFIER);
                block.setBlockProducerPublicKey(producerKey);
            } else if (i == 2) {
                block.setParentBlockHash(null);
                receipt = BlockValidator.runLayerZeroValidation(block, database, MIRROR_HASH, ALWAYS_OK_VERIFIER);
                block.setParentBlockHash(parentHash);
            } else if (i == 3) {
                block.setBlockHash(null);
                receipt = BlockValidator.runLayerZeroValidation(block, database, MIRROR_HASH, ALWAYS_OK_VERIFIER);
                block.setBlockHash(hash);
            } else if (i == 4) {
                block.setBlockSignature(null);
                receipt = BlockValidator.runLayerZeroValidation(block, database, MIRROR_HASH, ALWAYS_OK_VERIFIER);
                block.setBlockSignature(signature);
            } else if (i == 5) {
                block.setBlockData(null);
                receipt = BlockValidator.runLayerZeroValidation(block, database, MIRROR_HASH, ALWAYS_OK_VERIFIER);
                block.setBlockData(data);
            } else {
                Assert.fail();
            }

            Assert.assertNotNull(receipt);
            Assert.assertEquals(ReceiptCode.INVALID_PARAMETER, receipt.getCode());
            Assert.assertNotNull(receipt.getErrorMessage());
            Assert.assertNull(receipt.getUnexpectedErrorCause());
        }
    }

    @Test
    public void testIncompatibleMajorVersion() throws Exception {
        MutableBlock genesisBlock = BlockHelper.newGenesisBlock(MIRROR_HASH);
        ZeroDatabase database = DatabaseHelper.newConsistentDatabase(Collections.emptySet(), Collections.singleton(genesisBlock));

        genesisBlock.setLayerZeroMajorVersion(ZeroVersion.ZERO_MAJOR_VERSION + 1);
        Receipt receipt = BlockValidator.runLayerZeroValidation(genesisBlock, database, MIRROR_HASH, ALWAYS_OK_VERIFIER);
        Assert.assertEquals(ReceiptCode.VERSION_MISMATCH, receipt.getCode());
        Assert.assertNotNull(receipt.getErrorMessage());
        Assert.assertNull(receipt.getUnexpectedErrorCause());
    }

    @Test
    public void testParentDoesNotExist() throws Exception {
        MutableBlock genesisBlock = BlockHelper.newGenesisBlock(MIRROR_HASH);
        MutableBlock block = BlockHelper.newNonGenesisBlock(BigInteger.ONE, genesisBlock, MIRROR_HASH);
        ZeroDatabase database = DatabaseHelper.newConsistentDatabase(Collections.singleton(genesisBlock), Collections.singleton(block));

        // Override database to return a null parent block.
        Mockito.when(database.findBlockByHash(genesisBlock.getBlockHash())).thenReturn(null);

        Receipt receipt = BlockValidator.runLayerZeroValidation(block, database, MIRROR_HASH, ALWAYS_OK_VERIFIER);
        Assert.assertEquals(ReceiptCode.DOES_NOT_EXIST, receipt.getCode());
        Assert.assertNotNull(receipt.getErrorMessage());
        Assert.assertNull(receipt.getUnexpectedErrorCause());
    }

    @Test
    public void testParentNumberNotOneLess() throws Exception {
        MutableBlock genesisBlock = BlockHelper.newGenesisBlock(MIRROR_HASH);
        MutableBlock block = BlockHelper.newNonGenesisBlock(BigInteger.ONE, genesisBlock, MIRROR_HASH);
        ZeroDatabase database = DatabaseHelper.newConsistentDatabase(Collections.singleton(genesisBlock), Collections.singleton(block));

        block.setBlockNumber(block.getBlockNumber().add(BigInteger.ONE));
        Receipt receipt = BlockValidator.runLayerZeroValidation(block, database, MIRROR_HASH, ALWAYS_OK_VERIFIER);
        Assert.assertEquals(ReceiptCode.INVALID_PARAMETER, receipt.getCode());
        Assert.assertNotNull(receipt.getErrorMessage());
        Assert.assertNull(receipt.getUnexpectedErrorCause());
    }

    @Test
    public void testAddBlockThatAlreadyExists() throws Exception {
        MutableBlock genesisBlock = BlockHelper.newGenesisBlock(MIRROR_HASH);
        MutableBlock block = BlockHelper.newNonGenesisBlock(BigInteger.ONE, genesisBlock, MIRROR_HASH);
        ZeroDatabase database = DatabaseHelper.newConsistentDatabase(Collections.singleton(genesisBlock), Collections.singleton(block));

        // Override database to act as if the block already exists.
        Mockito.when(database.findBlockByHash(block.getBlockHash())).thenReturn(block);
        Mockito.when(database.blockExists(block.getBlockHash())).thenReturn(true);

        Receipt receipt = BlockValidator.runLayerZeroValidation(block, database, MIRROR_HASH, ALWAYS_OK_VERIFIER);
        Assert.assertEquals(ReceiptCode.EXISTS, receipt.getCode());
        Assert.assertNotNull(receipt.getErrorMessage());
        Assert.assertNull(receipt.getUnexpectedErrorCause());
    }

    @Test
    public void testAddGenesisWhenOtherGenesisExists() throws Exception {
        MutableBlock genesisBlock = BlockHelper.newGenesisBlock(MIRROR_HASH);
        MutableBlock otherGenesis = BlockHelper.newGenesisBlock(MIRROR_HASH);
        MutableBlock block = BlockHelper.newNonGenesisBlock(BigInteger.ONE, genesisBlock, MIRROR_HASH);
        ZeroDatabase database = DatabaseHelper.newConsistentDatabase(Collections.singleton(otherGenesis), Collections.singleton(block));

        Receipt receipt = BlockValidator.runLayerZeroValidation(genesisBlock, database, MIRROR_HASH, ALWAYS_OK_VERIFIER);
        Assert.assertEquals(ReceiptCode.EXISTS, receipt.getCode());
        Assert.assertNotNull(receipt.getErrorMessage());
        Assert.assertNull(receipt.getUnexpectedErrorCause());
    }

    @Test
    public void testIllegitimateHash() throws Exception {
        MutableBlock genesisBlock = BlockHelper.newGenesisBlock(MIRROR_HASH);
        ZeroDatabase database = DatabaseHelper.newConsistentDatabase(Collections.emptySet(), Collections.singleton(genesisBlock));

        HashFunction hashFunction = (payload) -> new byte[32];
        Receipt receipt = BlockValidator.runLayerZeroValidation(genesisBlock, database, hashFunction, ALWAYS_OK_VERIFIER);
        Assert.assertEquals(ReceiptCode.INVALID_PARAMETER, receipt.getCode());
        Assert.assertNotNull(receipt.getErrorMessage());
        Assert.assertNull(receipt.getUnexpectedErrorCause());
    }

    @Test
    public void testIllegitimateSignature() throws Exception {
        MutableBlock genesisBlock = BlockHelper.newGenesisBlock(MIRROR_HASH);
        ZeroDatabase database = DatabaseHelper.newConsistentDatabase(Collections.emptySet(), Collections.singleton(genesisBlock));

        SignatureVerifier signatureVerifier = (key, payload, signature) -> false;
        Receipt receipt = BlockValidator.runLayerZeroValidation(genesisBlock, database, MIRROR_HASH, signatureVerifier);
        Assert.assertEquals(ReceiptCode.INVALID_PARAMETER, receipt.getCode());
        Assert.assertNotNull(receipt.getErrorMessage());
        Assert.assertNull(receipt.getUnexpectedErrorCause());
    }

    @Test
    public void testUnexpectedErrorThrown() throws Exception {
        MutableBlock genesisBlock = BlockHelper.newGenesisBlock(MIRROR_HASH);
        ZeroDatabase database = DatabaseHelper.newConsistentDatabase(Collections.emptySet(), Collections.singleton(genesisBlock));

        RuntimeException cause = new RuntimeException();
        HashFunction unexpectedHash = (hash) -> { throw cause; };
        Receipt receipt = BlockValidator.runLayerZeroValidation(genesisBlock, database, unexpectedHash, ALWAYS_OK_VERIFIER);
        Assert.assertEquals(ReceiptCode.UNEXPECTED, receipt.getCode());
        Assert.assertNull(receipt.getErrorMessage());
        Assert.assertEquals(cause, receipt.getUnexpectedErrorCause());
    }

    @Test
    public void testAddValidBlock() throws Exception {
        MutableBlock genesisBlock = BlockHelper.newGenesisBlock(MIRROR_HASH);
        ZeroDatabase database = DatabaseHelper.newConsistentDatabase(Collections.emptySet(), Collections.singleton(genesisBlock));

        // Test a genesis block
        Receipt receipt = BlockValidator.runLayerZeroValidation(genesisBlock, database, MIRROR_HASH, ALWAYS_OK_VERIFIER);
        Assert.assertEquals(ReceiptCode.SUCCESS, receipt.getCode());
        Assert.assertNull(receipt.getErrorMessage());
        Assert.assertNull(receipt.getUnexpectedErrorCause());

        MutableBlock block = BlockHelper.newNonGenesisBlock(BigInteger.ONE, genesisBlock, MIRROR_HASH);
        database = DatabaseHelper.newConsistentDatabase(Collections.singleton(genesisBlock), Collections.singleton(block));

        // Test a non-genesis block
        receipt = BlockValidator.runLayerZeroValidation(block, database, MIRROR_HASH, ALWAYS_OK_VERIFIER);
        Assert.assertEquals(ReceiptCode.SUCCESS, receipt.getCode());
        Assert.assertNull(receipt.getErrorMessage());
        Assert.assertNull(receipt.getUnexpectedErrorCause());
    }

    @Test
    public void testAddBlockWithNegativeNumber() throws Exception {
        MutableBlock block = BlockHelper.newGenesisBlock(MIRROR_HASH);
        block.setBlockNumber(BigInteger.ONE.negate());

        ZeroDatabase database = DatabaseHelper.newConsistentDatabase(Collections.emptySet(), Collections.singleton(block));

        Receipt receipt = BlockValidator.runLayerZeroValidation(block, database, MIRROR_HASH, ALWAYS_OK_VERIFIER);
        Assert.assertEquals(ReceiptCode.INVALID_PARAMETER, receipt.getCode());
        Assert.assertNotNull(receipt.getErrorMessage());
        Assert.assertNull(receipt.getUnexpectedErrorCause());
    }

    @Test
    public void testAddBlockWithLengthZeroArrays() throws Exception {
        MutableBlock genesis = BlockHelper.newGenesisBlock(MIRROR_HASH);
        ZeroDatabase database = DatabaseHelper.newConsistentDatabase(Collections.emptySet(), Collections.singleton(genesis));

        // Check when the producer key is length zero.
        byte[] oldValue = genesis.getBlockProducerPublicKey();
        genesis.setBlockProducerPublicKey(new byte[0]);
        Receipt receipt = BlockValidator.runLayerZeroValidation(genesis, database, MIRROR_HASH, ALWAYS_OK_VERIFIER);
        Assert.assertEquals(ReceiptCode.INVALID_PARAMETER, receipt.getCode());
        Assert.assertNotNull(receipt.getErrorMessage());
        Assert.assertNull(receipt.getUnexpectedErrorCause());
        genesis.setBlockProducerPublicKey(oldValue);

        // Check when the hash is length zero.
        oldValue = genesis.getBlockHash();
        genesis.setBlockHash(new byte[0]);
        receipt = BlockValidator.runLayerZeroValidation(genesis, database, MIRROR_HASH, ALWAYS_OK_VERIFIER);
        Assert.assertEquals(ReceiptCode.INVALID_PARAMETER, receipt.getCode());
        Assert.assertNotNull(receipt.getErrorMessage());
        Assert.assertNull(receipt.getUnexpectedErrorCause());
        genesis.setBlockHash(oldValue);

        // Check when the signature is length zero.
        oldValue = genesis.getBlockSignature();
        genesis.setBlockSignature(new byte[0]);
        receipt = BlockValidator.runLayerZeroValidation(genesis, database, MIRROR_HASH, ALWAYS_OK_VERIFIER);
        Assert.assertEquals(ReceiptCode.INVALID_PARAMETER, receipt.getCode());
        Assert.assertNotNull(receipt.getErrorMessage());
        Assert.assertNull(receipt.getUnexpectedErrorCause());
        genesis.setBlockSignature(oldValue);

        // Check when the parent hash is length zero.
        MutableBlock block = BlockHelper.newNonGenesisBlock(BigInteger.ONE, genesis, MIRROR_HASH);
        block.setParentBlockHash(new byte[0]);
        receipt = BlockValidator.runLayerZeroValidation(block, database, MIRROR_HASH, ALWAYS_OK_VERIFIER);
        Assert.assertEquals(ReceiptCode.INVALID_PARAMETER, receipt.getCode());
        Assert.assertNotNull(receipt.getErrorMessage());
        Assert.assertNull(receipt.getUnexpectedErrorCause());
    }

    @Test
    public void testAddGenesisWithNonNullParentHash() throws Exception {
        MutableBlock block = BlockHelper.newGenesisBlock(MIRROR_HASH);
        block.setParentBlockHash(new byte[32]);

        ZeroDatabase database = DatabaseHelper.newConsistentDatabase(Collections.emptySet(), Collections.singleton(block));

        Receipt receipt = BlockValidator.runLayerZeroValidation(block, database, MIRROR_HASH, ALWAYS_OK_VERIFIER);
        Assert.assertEquals(ReceiptCode.INVALID_PARAMETER, receipt.getCode());
        Assert.assertNotNull(receipt.getErrorMessage());
        Assert.assertNull(receipt.getUnexpectedErrorCause());
    }

    @Test
    public void testAddGenesisBlockWhenGenesisExists() throws Exception {
        MutableBlock block = BlockHelper.newGenesisBlock(MIRROR_HASH);

        ZeroDatabase database = DatabaseHelper.newConsistentDatabase(Collections.singleton(block), Collections.emptySet());

        Receipt receipt = BlockValidator.runLayerZeroValidation(block, database, MIRROR_HASH, ALWAYS_OK_VERIFIER);
        Assert.assertEquals(ReceiptCode.EXISTS, receipt.getCode());
        Assert.assertNotNull(receipt.getErrorMessage());
        Assert.assertNull(receipt.getUnexpectedErrorCause());
    }

    @Test(expected = RuntimeAssertionError.class)
    public void testAddBlockWhenParentHasNullNumber() throws Exception {
        MutableBlock genesisBlock = BlockHelper.newGenesisBlock(MIRROR_HASH);
        MutableBlock block = BlockHelper.newNonGenesisBlock(BigInteger.ONE, genesisBlock, MIRROR_HASH);

        ZeroDatabase database = DatabaseHelper.newConsistentDatabase(Collections.singleton(genesisBlock), Collections.singleton(block));
        genesisBlock.setBlockNumber(null);

        Receipt receipt = BlockValidator.runLayerZeroValidation(block, database, MIRROR_HASH, ALWAYS_OK_VERIFIER);
        Assert.assertEquals(ReceiptCode.UNEXPECTED, receipt.getCode());
        Assert.assertNull(receipt.getErrorMessage());
        Exception error = receipt.getUnexpectedErrorCause();
        Assert.assertNotNull(error);
        throw error;
    }
}
