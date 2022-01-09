package net.nicknadeau.zero.storage;

import net.nicknadeau.zero.block.Block;
import net.nicknadeau.zero.block.BlockStatus;

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

    /**
     * Saves the specified block in the database and returns {@code true} to indicate that the block was successfully
     * saved and {@code false} to indicate an error occurred and the block could not be saved.
     *
     * @param block The block to save.
     * @param status The block status to save.
     * @return whether or not the block and its status were saved.
     * @throws NullPointerException if block or status are null.
     */
    public boolean saveBlockAndStatus(Block block, BlockStatus status);

    /**
     * Updates the block with the specified block hash in the database so that it has the specified block status, and
     * returns {@code true} to indicate that the block status was successfully updated and {@code false} to indicate
     * an error occurred and the block status could not be updated.
     *
     * If there is no such block in the database with the specified block hash then this method must return
     * {@code false}.
     *
     * @param blockHash The block hash of the block to save the status for.
     * @param status The new block status to save.
     * @return whether or not the status was updated.
     * @throws NullPointerException if blockHash or status are null.
     */
    public boolean updateBlockStatus(byte[] blockHash, BlockStatus status);

    /**
     * Returns {@code true} if and only if this database contains at least one block whose associated block status value
     * is {@link BlockStatus#PENDING_ADDITION}. Returns {@code false} otherwise.
     *
     * @return whether or not this database contains any pending blocks.
     */
    public boolean containsPendingBlocks();

    /**
     * Returns {@code true} if and only if after returning from this method there is no such block in the database with
     * the specified block hash. Thus, a call into this method with a block hash that does not exist should return
     * {@code true}, including if {@code blockHash == null}. Returns {@code false} otherwise.
     *
     * @param blockHash The hash of the block to delete.
     * @return whether or not the deletion was successful.
     */
    public boolean removeBlockByHash(byte[] blockHash);
}
