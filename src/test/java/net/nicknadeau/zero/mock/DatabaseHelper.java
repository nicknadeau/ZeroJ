package net.nicknadeau.zero.mock;

import net.nicknadeau.zero.block.Block;
import net.nicknadeau.zero.block.BlockStatus;
import net.nicknadeau.zero.storage.ZeroDatabase;
import org.mockito.Mockito;

import java.math.BigInteger;
import java.util.Collection;

/**
 * Helper class that constructs instances of {@link ZeroDatabase} for tests.
 */
public final class DatabaseHelper {

    /**
     * Returns a new consistent mock database that contains all of the blocks in {@code blocksInDb} and which will not
     * contain (but will allow to be added) all of the blocks in {@code blocksToAdd}.
     */
    public static ZeroDatabase newConsistentDatabase(Collection<Block> blocksInDb, Collection<Block> blocksToAdd) {
        ZeroDatabase database = Mockito.mock(ZeroDatabase.class);
        // The database is always consistent.
        Mockito.when(database.containsPendingBlocks()).thenReturn(false);

        // Add all blocks that are supposed to be in the database to it.
        boolean containsGenesis = false;
        for (Block block : blocksInDb) {
            Mockito.when(database.findBlockByHash(block.getBlockHash())).thenReturn(block);
            Mockito.when(database.blockExists(block.getBlockHash())).thenReturn(true);
            Mockito.when(database.saveBlockAndStatus(block, BlockStatus.PENDING_ADDITION)).thenReturn(false);
            Mockito.when(database.updateBlockStatus(block.getBlockHash(), BlockStatus.ADDED)).thenReturn(false);
            Mockito.when(database.updateBlockStatus(block.getBlockHash(), BlockStatus.PENDING_DELETION)).thenReturn(true);
            Mockito.when(database.removeBlockByHash(block.getBlockHash())).thenReturn(true);

            if (block.getBlockNumber().equals(BigInteger.ZERO)) {
                containsGenesis = true;
            }
        }
        Mockito.when(database.genesisBlockExists()).thenReturn(containsGenesis);

        // The blocks that are not yet in the database but which we must allow in, set their criteria.
        for (Block block : blocksToAdd) {
            Mockito.when(database.findBlockByHash(block.getBlockHash())).thenReturn(null);
            Mockito.when(database.blockExists(block.getBlockHash())).thenReturn(false);
            Mockito.when(database.saveBlockAndStatus(block, BlockStatus.PENDING_ADDITION)).thenReturn(true);
            Mockito.when(database.updateBlockStatus(block.getBlockHash(), BlockStatus.ADDED)).thenReturn(true);
            Mockito.when(database.updateBlockStatus(block.getBlockHash(), BlockStatus.PENDING_DELETION)).thenReturn(true);
            Mockito.when(database.removeBlockByHash(block.getBlockHash())).thenReturn(true);
        }

        return database;
    }
}
