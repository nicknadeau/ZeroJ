package net.nicknadeau.zero.storage;

import net.nicknadeau.zero.block.Block;

/**
 * A database that layer zero calls into to determine its state.
 *
 * Any implementation must be thread-safe in the sense that multiple threads should be able to call into the same or
 * different methods in parallel and each thread must be guaranteed to always see a consistent view of the database.
 * It is permissible that certain threads may see stale state, but absolutely prohibited that inconsistent state ever
 * be witnessed.
 */
public interface ZeroDatabase {

    /**
     * Returns {@code true} if and only if this database contains a genesis block and {@code false} otherwise.
     *
     * @return true if a genesis block exists and otherwise false.
     */
    public boolean genesisBlockExists();

    /**
     * Returns {@code true} if and only if this database contains a block with the specified hash and {@code false}
     * otherwise.
     *
     * Returns {@code false} if {@code blockHash == null}.
     *
     * @return true if a block with the given hash exists and otherwise false.
     * @throws NullPointerException if blockHash is null.
     */
    public boolean blockExists(byte[] blockHash);

    /**
     * Returns the block in the database with the specified block hash or {@code null} if no such block exists.
     *
     * Returns {@code null} if {@code blockHash == null};
     *
     * @param blockHash The block hash of the block to find.
     * @return the block or null if not found.
     */
    public Block findBlockByHash(byte[] blockHash);
}
