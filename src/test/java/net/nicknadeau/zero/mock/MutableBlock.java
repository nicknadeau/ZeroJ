package net.nicknadeau.zero.mock;

import net.nicknadeau.zero.block.Block;

import java.math.BigInteger;

public final class MutableBlock implements Block {
    private int majorVersion = -1;
    private BigInteger number = null;
    private byte[] producerKey = null;
    private byte[] hash = null;
    private byte[] parentHash = null;
    private byte[] data = null;
    private byte[] signature = null;

    @Override
    public int getLayerZeroMajorVersion() {
        return this.majorVersion;
    }

    public MutableBlock setLayerZeroMajorVersion(int version) {
        this.majorVersion = version;
        return this;
    }

    @Override
    public BigInteger getBlockNumber() {
        return this.number;
    }

    public MutableBlock setBlockNumber(BigInteger number) {
        this.number = number;
        return this;
    }

    @Override
    public byte[] getBlockProducerPublicKey() {
        return this.producerKey;
    }

    public MutableBlock setBlockProducerPublicKey(byte[] key) {
        this.producerKey = key;
        return this;
    }

    @Override
    public byte[] getBlockHash() {
        return this.hash;
    }

    public MutableBlock setBlockHash(byte[] hash) {
        this.hash = hash;
        return this;
    }

    @Override
    public byte[] getParentBlockHash() {
        return this.parentHash;
    }

    public MutableBlock setParentBlockHash(byte[] hash) {
        this.parentHash = hash;
        return this;
    }

    @Override
    public byte[] getBlockData() {
        return this.data;
    }

    public MutableBlock setBlockData(byte[] data) {
        this.data = data;
        return this;
    }

    @Override
    public byte[] getBlockSignature() {
        return this.signature;
    }

    public MutableBlock setBlockSignature(byte[] signature) {
        this.signature = signature;
        return this;
    }
}
