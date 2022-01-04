package net.nicknadeau.zero.blockchain.callback;

import net.nicknadeau.zero.block.Block;

/**
 * A callback which must be implemented by the layer one protocol.
 *
 * This callback will be called by layer zero in order to notify layer one that the block is to be removed from the
 * blockchain.
 *
 * This callback may be called multiple times per block deletion attempt. Thus, it may be that layer one has already
 * removed the block and this callback is called again. In such a case, the operation should be treated as a success.
 * Note that the first time this callback is called, the block will be the head of a chain. This callback will never be
 * called in a situation where the block is not a head block.
 *
 * This callback will be invoked by whichever thread is removing the block from the database. Note that this callback
 * can also be triggered during the blockchain recovery phase, and thus will be invoked by whichever thread has
 * initiated the recovery.
 */
@FunctionalInterface
public interface LayerOneDeleteBlockCallback {

    /**
     * Removes the specified block from the blockchain from the perspective of layer one and returns an integer code
     * which indicates the result of the deletion operation. The deletion operation must be considered successful if,
     * after this callback returns, the block does not exist in the blockchain from the perspective of layer one. It
     * must be considered failed if that is not true.
     *
     * The returned error code is semi-opaque in the sense that a return value of zero indicates success (ie. the block
     * was removed). However, aside from zero, all other integer values are entirely opaque to layer zero aside from the
     * fact that all non-zero values must represent error/failure results. Thus, layer zero will treat any non-zero
     * value as indicating that the block was not removed. All non-zero integer values are defined by layer one
     * exclusively. This is so that layer one can have a flexible means of specifying error reasons without being
     * limited to the ways in which layer zero happens to specify them.
     *
     * @param block The block to remove.
     * @return the add result code, which is 0 for success or any other integer for a failure.
     */
    public int delete(Block block);
}
