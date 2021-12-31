package net.nicknadeau.zero.mock;

import net.nicknadeau.zero.ZeroVersion;
import net.nicknadeau.zero.block.Block;
import net.nicknadeau.zero.util.HashFunction;
import net.nicknadeau.zero.util.internal.BlockHashPreImageUtil;
import org.junit.Assert;

import java.math.BigInteger;
import java.util.Arrays;

/**
 * Helper class that constructs instances of {@link Block} for tests.
 */
public final class BlockHelper {

    /**
     * Returns a new genesis block whose hash is derived from the given function.
     */
    public static MutableBlock newGenesisBlock(HashFunction hashFunction) {
        Assert.assertNotNull(hashFunction);
        MutableBlock block = new MutableBlock()
                .setLayerZeroMajorVersion(ZeroVersion.ZERO_MAJOR_VERSION)
                .setBlockNumber(BigInteger.ZERO)
                .setBlockProducerPublicKey(new byte[1])
                .setBlockData(new byte[0])
                .setBlockSignature(new byte[1])
                ;
        byte[] hash = computeBlockHash(block, hashFunction);
        block.setBlockHash(hash);
        return block;
    }

    /**
     * Returns a new non-genesis block with the given number, parent, and whose hash is derived from the given function.
     */
    public static MutableBlock newNonGenesisBlock(BigInteger number, Block parent, HashFunction hashFunction) {
        Assert.assertNotNull(number);
        Assert.assertNotNull(parent);
        Assert.assertNotNull(hashFunction);
        byte[] parentHash = parent.getBlockHash();
        MutableBlock block = new MutableBlock()
                .setLayerZeroMajorVersion(ZeroVersion.ZERO_MAJOR_VERSION)
                .setBlockNumber(number)
                .setBlockProducerPublicKey(new byte[1])
                .setBlockData(new byte[0])
                .setBlockSignature(new byte[1])
                .setParentBlockHash(Arrays.copyOf(parentHash, parentHash.length))
                ;
        byte[] hash = computeBlockHash(block, hashFunction);
        block.setBlockHash(hash);
        return block;
    }

    private static byte[] computeBlockHash(Block block, HashFunction hashFunction) {
        byte[] preImage;
        if (block.getBlockNumber().equals(BigInteger.ZERO)) {
            int version = block.getLayerZeroMajorVersion();
            byte[] key = block.getBlockProducerPublicKey();
            byte[] data = block.getBlockData();
            preImage = BlockHashPreImageUtil.createGenesisPreImage(version, key, data);
        } else {
            BigInteger number = block.getBlockNumber();
            byte[] key = block.getBlockProducerPublicKey();
            byte[] parent = block.getParentBlockHash();
            byte[] data = block.getBlockData();
            preImage = BlockHashPreImageUtil.createNonGenesisPreImage(number, key, parent, data);
        }
        return hashFunction.hash(preImage);
    }
}
