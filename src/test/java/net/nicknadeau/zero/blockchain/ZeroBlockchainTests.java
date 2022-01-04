package net.nicknadeau.zero.blockchain;

import net.nicknadeau.zero.blockchain.callback.LayerOneAddBlockCallback;
import net.nicknadeau.zero.blockchain.callback.LayerOneValidateBlockCallback;
import net.nicknadeau.zero.blockchain.callback.ZeroCallbacks;
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
    public void testAddLayerZeroInvalidBlock() {
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
    public void testAddLayerOneInvalidBlock() {
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
    public void testAddWhenSaveBlockCallFails() {
        MutableBlock genesisBlock = BlockHelper.newGenesisBlock(MIRROR_HASH);
        ZeroDatabase database = DatabaseHelper.newConsistentDatabase(Collections.emptySet(), Collections.singleton(genesisBlock));

        // Override the database to fail when we first try to save the block.
        Mockito.when(database.saveBlock(genesisBlock)).thenReturn(false);

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
    public void testAddWhenLayerOneAddBlockFails() {
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
    public void testAddWhenExceptionThrown() {
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
    public void testAddBlockSucceeds() {
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
}
