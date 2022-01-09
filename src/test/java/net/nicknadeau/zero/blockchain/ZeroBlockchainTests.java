package net.nicknadeau.zero.blockchain;

import net.nicknadeau.zero.block.BlockStatus;
import net.nicknadeau.zero.blockchain.callback.LayerOneAddBlockCallback;
import net.nicknadeau.zero.blockchain.callback.LayerOneDeleteBlockCallback;
import net.nicknadeau.zero.blockchain.callback.LayerOneValidateBlockCallback;
import net.nicknadeau.zero.blockchain.callback.ZeroCallbacks;
import net.nicknadeau.zero.exception.LayersOutOfSyncException;
import net.nicknadeau.zero.mock.BlockHelper;
import net.nicknadeau.zero.mock.CallbackHelper;
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

import java.util.Collections;

public class ZeroBlockchainTests {
    private static final HashFunction MIRROR_HASH = (payload) -> payload;
    private static final SignatureVerifier ALWAYS_OK_VERIFIER = (key, payload, signature) -> true;

    @Test
    public void testAddLayerZeroInvalidBlock() throws Exception {
        MutableBlock genesisBlock = BlockHelper.newGenesisBlock(MIRROR_HASH);
        ZeroDatabase database = DatabaseHelper.newConsistentDatabase(Collections.emptySet(), Collections.singleton(genesisBlock));

        // Setting the block hash null will certainly invalidate it.
        genesisBlock.setBlockHash(null);

        ZeroCallbacks callbacks = CallbackHelper.newSuccessfulCallbacks();
        ZeroBlockchain blockchain = ZeroBlockchain.Builder.newBuilder()
                .withDatabase(database)
                .withHashFunction(MIRROR_HASH)
                .withSignatureVerifier(ALWAYS_OK_VERIFIER)
                .withCallbacks(callbacks)
                .build()
                ;
        Receipt receipt = blockchain.addBlock(genesisBlock);
        Assert.assertNotNull(receipt);
        Assert.assertEquals(ReceiptCode.INVALID_PARAMETER, receipt.getCode());
    }

    @Test
    public void testAddLayerOneInvalidBlock() throws Exception {
        MutableBlock genesisBlock = BlockHelper.newGenesisBlock(MIRROR_HASH);
        ZeroDatabase database = DatabaseHelper.newConsistentDatabase(Collections.emptySet(), Collections.singleton(genesisBlock));

        int errorCode = 1;
        LayerOneValidateBlockCallback validateCallback = (block) -> { return errorCode; };
        ZeroCallbacks callbacks = CallbackHelper.newCallbacks(validateCallback);

        ZeroBlockchain blockchain = ZeroBlockchain.Builder.newBuilder()
                .withDatabase(database)
                .withHashFunction(MIRROR_HASH)
                .withSignatureVerifier(ALWAYS_OK_VERIFIER)
                .withCallbacks(callbacks)
                .build()
                ;
        Receipt receipt = blockchain.addBlock(genesisBlock);
        Assert.assertNotNull(receipt);
        Assert.assertEquals(ReceiptCode.LAYER_ONE_FAILURE, receipt.getCode());
        Assert.assertEquals(errorCode, receipt.getLayerOneErrorCode());
    }

    @Test
    public void testAddWhenSaveBlockCallFails() throws Exception {
        MutableBlock genesisBlock = BlockHelper.newGenesisBlock(MIRROR_HASH);
        ZeroDatabase database = DatabaseHelper.newConsistentDatabase(Collections.emptySet(), Collections.singleton(genesisBlock));

        // Override the database to fail when we first try to save the block.
        Mockito.when(database.saveBlockAndStatus(genesisBlock, BlockStatus.PENDING_ADDITION)).thenReturn(false);

        ZeroCallbacks callbacks = CallbackHelper.newSuccessfulCallbacks();
        ZeroBlockchain blockchain = ZeroBlockchain.Builder.newBuilder()
                .withDatabase(database)
                .withHashFunction(MIRROR_HASH)
                .withSignatureVerifier(ALWAYS_OK_VERIFIER)
                .withCallbacks(callbacks)
                .build()
                ;
        Receipt receipt = blockchain.addBlock(genesisBlock);
        Assert.assertNotNull(receipt);
        Assert.assertEquals(ReceiptCode.FAILED, receipt.getCode());
    }

    @Test
    public void testAddWhenLayerOneAddBlockFails() throws Exception {
        MutableBlock genesisBlock = BlockHelper.newGenesisBlock(MIRROR_HASH);
        ZeroDatabase database = DatabaseHelper.newConsistentDatabase(Collections.emptySet(), Collections.singleton(genesisBlock));

        int errorCode = 1;
        LayerOneAddBlockCallback addCallback = (block) -> { return errorCode; };
        ZeroCallbacks callbacks = CallbackHelper.newCallbacks(addCallback);

        ZeroBlockchain blockchain = ZeroBlockchain.Builder.newBuilder()
                .withDatabase(database)
                .withHashFunction(MIRROR_HASH)
                .withSignatureVerifier(ALWAYS_OK_VERIFIER)
                .withCallbacks(callbacks)
                .build()
                ;
        Receipt receipt = blockchain.addBlock(genesisBlock);
        Assert.assertNotNull(receipt);
        Assert.assertEquals(ReceiptCode.LAYER_ONE_FAILURE, receipt.getCode());
        Assert.assertEquals(errorCode, receipt.getLayerOneErrorCode());
    }

    @Test
    public void testAddWhenExceptionThrown() throws Exception {
        MutableBlock genesisBlock = BlockHelper.newGenesisBlock(MIRROR_HASH);
        ZeroDatabase database = DatabaseHelper.newConsistentDatabase(Collections.emptySet(), Collections.singleton(genesisBlock));

        // An easy place to throw an exception is in a layer one callback.
        RuntimeException error = new RuntimeException();
        LayerOneAddBlockCallback addCallback = (block) -> { throw error; };
        ZeroCallbacks callbacks = CallbackHelper.newCallbacks(addCallback);

        ZeroBlockchain blockchain = ZeroBlockchain.Builder.newBuilder()
                .withDatabase(database)
                .withHashFunction(MIRROR_HASH)
                .withSignatureVerifier(ALWAYS_OK_VERIFIER)
                .withCallbacks(callbacks)
                .build()
                ;
        Receipt receipt = blockchain.addBlock(genesisBlock);
        Assert.assertNotNull(receipt);
        Assert.assertEquals(ReceiptCode.UNEXPECTED, receipt.getCode());
        Assert.assertEquals(error, receipt.getUnexpectedErrorCause());
    }

    @Test
    public void testAddBlockSucceeds() throws Exception {
        MutableBlock genesisBlock = BlockHelper.newGenesisBlock(MIRROR_HASH);
        ZeroDatabase database = DatabaseHelper.newConsistentDatabase(Collections.emptySet(), Collections.singleton(genesisBlock));

        ZeroCallbacks callbacks = CallbackHelper.newSuccessfulCallbacks();
        ZeroBlockchain blockchain = ZeroBlockchain.Builder.newBuilder()
                .withDatabase(database)
                .withHashFunction(MIRROR_HASH)
                .withSignatureVerifier(ALWAYS_OK_VERIFIER)
                .withCallbacks(callbacks)
                .build()
                ;
        Receipt receipt = blockchain.addBlock(genesisBlock);
        Assert.assertNotNull(receipt);
        Assert.assertEquals(ReceiptCode.SUCCESS, receipt.getCode());
    }

    @Test(expected = LayersOutOfSyncException.class)
    public void testAddBlockWhenUpdateStatusCallFails() throws Exception {
        MutableBlock genesisBlock = BlockHelper.newGenesisBlock(MIRROR_HASH);
        ZeroDatabase database = DatabaseHelper.newConsistentDatabase(Collections.emptySet(), Collections.singleton(genesisBlock));

        // Override the database to say it fails to update the status.
        Mockito.when(database.updateBlockStatus(genesisBlock.getBlockHash(), BlockStatus.ADDED)).thenReturn(false);

        ZeroCallbacks callbacks = CallbackHelper.newSuccessfulCallbacks();
        ZeroBlockchain blockchain = ZeroBlockchain.Builder.newBuilder()
                .withDatabase(database)
                .withHashFunction(MIRROR_HASH)
                .withSignatureVerifier(ALWAYS_OK_VERIFIER)
                .withCallbacks(callbacks)
                .build()
                ;
        blockchain.addBlock(genesisBlock);
    }

    @Test(expected = LayersOutOfSyncException.class)
    public void testBlockchainBackedByOutOfSyncDatabase() throws Exception {
        ZeroDatabase database = DatabaseHelper.newConsistentDatabase(Collections.emptySet(), Collections.emptySet());

        // Override the database to say it contains a pending block.
        Mockito.when(database.containsPendingBlocks()).thenReturn(true);

        ZeroCallbacks callbacks = CallbackHelper.newSuccessfulCallbacks();
        ZeroBlockchain blockchain = ZeroBlockchain.Builder.newBuilder()
                .withDatabase(database)
                .withHashFunction(MIRROR_HASH)
                .withSignatureVerifier(ALWAYS_OK_VERIFIER)
                .withCallbacks(callbacks)
                .build()
                ;

        // We add a null block just to prove the point -- when out of sync we expect the error thrown immediately.
        blockchain.addBlock(null);
    }

    @Test
    public void testRemoveBlockWhenUpdateStatusCallFails() throws Exception {
        MutableBlock genesisBlock = BlockHelper.newGenesisBlock(MIRROR_HASH);
        ZeroDatabase database = DatabaseHelper.newConsistentDatabase(Collections.emptySet(), Collections.singleton(genesisBlock));

        // Override the database to fail when we first try to update the block status.
        Mockito.when(database.updateBlockStatus(genesisBlock.getBlockHash(), BlockStatus.PENDING_DELETION)).thenReturn(false);

        ZeroCallbacks callbacks = CallbackHelper.newSuccessfulCallbacks();
        ZeroBlockchain blockchain = ZeroBlockchain.Builder.newBuilder()
                .withDatabase(database)
                .withHashFunction(MIRROR_HASH)
                .withSignatureVerifier(ALWAYS_OK_VERIFIER)
                .withCallbacks(callbacks)
                .build()
                ;
        Receipt receipt = blockchain.removeBlock(genesisBlock);
        Assert.assertNotNull(receipt);
        Assert.assertEquals(ReceiptCode.FAILED, receipt.getCode());
    }

    @Test
    public void testRemoveWhenLayerOneDeleteBlockFails() throws Exception {
        MutableBlock genesisBlock = BlockHelper.newGenesisBlock(MIRROR_HASH);
        ZeroDatabase database = DatabaseHelper.newConsistentDatabase(Collections.emptySet(), Collections.singleton(genesisBlock));

        int errorCode = 1;
        LayerOneDeleteBlockCallback deleteCallback = (block) -> { return errorCode; };
        ZeroCallbacks callbacks = CallbackHelper.newCallbacks(deleteCallback);
        ZeroBlockchain blockchain = ZeroBlockchain.Builder.newBuilder()
                .withDatabase(database)
                .withHashFunction(MIRROR_HASH)
                .withSignatureVerifier(ALWAYS_OK_VERIFIER)
                .withCallbacks(callbacks)
                .build()
                ;
        Receipt receipt = blockchain.removeBlock(genesisBlock);
        Assert.assertNotNull(receipt);
        Assert.assertEquals(ReceiptCode.LAYER_ONE_FAILURE, receipt.getCode());
        Assert.assertEquals(errorCode, receipt.getLayerOneErrorCode());
    }

    @Test(expected = LayersOutOfSyncException.class)
    public void testRemoveWhenRemoveDatabaseCallFails() throws Exception {
        MutableBlock genesisBlock = BlockHelper.newGenesisBlock(MIRROR_HASH);
        ZeroDatabase database = DatabaseHelper.newConsistentDatabase(Collections.emptySet(), Collections.singleton(genesisBlock));

        // Override the database to fail when we try to remove the block.
        Mockito.when(database.removeBlockByHash(genesisBlock.getBlockHash())).thenReturn(false);

        ZeroCallbacks callbacks = CallbackHelper.newSuccessfulCallbacks();
        ZeroBlockchain blockchain = ZeroBlockchain.Builder.newBuilder()
                .withDatabase(database)
                .withHashFunction(MIRROR_HASH)
                .withSignatureVerifier(ALWAYS_OK_VERIFIER)
                .withCallbacks(callbacks)
                .build()
                ;
        blockchain.removeBlock(genesisBlock);
    }

    @Test
    public void testRemoveWhenExceptionThrown() throws Exception {
        MutableBlock genesisBlock = BlockHelper.newGenesisBlock(MIRROR_HASH);
        ZeroDatabase database = DatabaseHelper.newConsistentDatabase(Collections.emptySet(), Collections.singleton(genesisBlock));

        IllegalArgumentException exception = new IllegalArgumentException();
        LayerOneDeleteBlockCallback deleteCallback = (block) -> { throw exception; };
        ZeroCallbacks callbacks = CallbackHelper.newCallbacks(deleteCallback);
        ZeroBlockchain blockchain = ZeroBlockchain.Builder.newBuilder()
                .withDatabase(database)
                .withHashFunction(MIRROR_HASH)
                .withSignatureVerifier(ALWAYS_OK_VERIFIER)
                .withCallbacks(callbacks)
                .build()
                ;
        Receipt receipt = blockchain.removeBlock(genesisBlock);
        Assert.assertNotNull(receipt);
        Assert.assertEquals(ReceiptCode.UNEXPECTED, receipt.getCode());
        Assert.assertEquals(exception, receipt.getUnexpectedErrorCause());
    }

    @Test(expected = LayersOutOfSyncException.class)
    public void testRemoveBlockWhenOutOfSync() throws Exception {
        MutableBlock genesisBlock = BlockHelper.newGenesisBlock(MIRROR_HASH);
        ZeroDatabase database = DatabaseHelper.newConsistentDatabase(Collections.emptySet(), Collections.singleton(genesisBlock));
        Mockito.when(database.containsPendingBlocks()).thenReturn(true);

        ZeroCallbacks callbacks = CallbackHelper.newSuccessfulCallbacks();
        ZeroBlockchain blockchain = ZeroBlockchain.Builder.newBuilder()
                .withDatabase(database)
                .withHashFunction(MIRROR_HASH)
                .withSignatureVerifier(ALWAYS_OK_VERIFIER)
                .withCallbacks(callbacks)
                .build()
                ;
        blockchain.removeBlock(genesisBlock);
    }

    @Test
    public void testRemoveNullBlock() throws Exception {
        ZeroDatabase database = DatabaseHelper.newConsistentDatabase(Collections.emptySet(), Collections.emptySet());

        ZeroCallbacks callbacks = CallbackHelper.newSuccessfulCallbacks();
        ZeroBlockchain blockchain = ZeroBlockchain.Builder.newBuilder()
                .withDatabase(database)
                .withHashFunction(MIRROR_HASH)
                .withSignatureVerifier(ALWAYS_OK_VERIFIER)
                .withCallbacks(callbacks)
                .build()
                ;
        Receipt receipt = blockchain.removeBlock(null);
        Assert.assertNotNull(receipt);
        Assert.assertEquals(ReceiptCode.DOES_NOT_EXIST, receipt.getCode());
    }

    @Test
    public void testRemoveBlockSucceeds() throws Exception {
        MutableBlock genesisBlock = BlockHelper.newGenesisBlock(MIRROR_HASH);
        ZeroDatabase database = DatabaseHelper.newConsistentDatabase(Collections.emptySet(), Collections.singleton(genesisBlock));

        ZeroCallbacks callbacks = CallbackHelper.newSuccessfulCallbacks();
        ZeroBlockchain blockchain = ZeroBlockchain.Builder.newBuilder()
                .withDatabase(database)
                .withHashFunction(MIRROR_HASH)
                .withSignatureVerifier(ALWAYS_OK_VERIFIER)
                .withCallbacks(callbacks)
                .build()
                ;
        Receipt receipt = blockchain.removeBlock(genesisBlock);
        Assert.assertNotNull(receipt);
        Assert.assertEquals(ReceiptCode.SUCCESS, receipt.getCode());
    }
}
