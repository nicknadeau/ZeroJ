package net.nicknadeau.zero.mock;

import net.nicknadeau.zero.block.Block;
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

        // Add all blocks that are supposed to be in the database to it.
        boolean containsGenesis = false;
        for (Block block : blocksInDb) {
            Mockito.when(database.findBlockByHash(block.getBlockHash())).thenReturn(block);
            Mockito.when(database.blockExists(block.getBlockHash())).thenReturn(true);
            Mockito.when(database.saveBlock(block)).thenReturn(false);

            if (block.getBlockNumber().equals(BigInteger.ZERO)) {
                containsGenesis = true;
            }
        }
        Mockito.when(database.genesisBlockExists()).thenReturn(containsGenesis);

        // The blocks that are not yet in the database but which we must allow in, set their criteria.
        for (Block block : blocksToAdd) {
            Mockito.when(database.findBlockByHash(block.getBlockHash())).thenReturn(null);
            Mockito.when(database.blockExists(block.getBlockHash())).thenReturn(false);
            Mockito.when(database.saveBlock(block)).thenReturn(true);
        }

        return database;
    }
}
