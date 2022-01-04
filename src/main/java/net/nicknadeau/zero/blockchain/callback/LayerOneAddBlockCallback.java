package net.nicknadeau.zero.blockchain.callback;

import net.nicknadeau.zero.block.Block;

/**
 * A callback which must be implemented by the layer one protocol.
 *
 * This callback will be called by layer zero in order to notify layer one that the block is to be added to the
 * blockchain. When this callback is called, it is guaranteed that both layer zero and layer one already performed their
 * validation checks on the block and that the block passes all of the layer zero and one validation checks.
 *
 * This callback may be called multiple times per block addition attempt. Thus, it may be that layer one has already
 * added the block and this callback is called again. In such a case, the operation should be treated as a success.
 * Note that the first time this callback is called, the block will become the new head of a chain and therefore its
 * parent block is guaranteed to be the head of some chain. This callback will never be called in a situation where the
 * block's parent does not exist.
 *
 * This callback will be invoked by whichever thread is adding the block to the database. Note that this callback can
 * also be triggered during the blockchain recovery phase, and thus will be invoked by whichever thread has initiated
 * the recovery.
 */
@FunctionalInterface
public interface LayerOneAddBlockCallback {

    /**
     * Adds the specified block to the blockchain from the perspective of layer one and returns an integer code which
     * indicates the result of the add operation. The add operation must be considered successful if, after this
     * callback returns, the block exists in the blockchain from the perspective of layer one. It must be considered
     * failed if that is not true.
     *
     * The returned error code is semi-opaque in the sense that a return value of zero indicates success (ie. the block
     * was added). However, aside from zero, all other integer values are entirely opaque to layer zero aside from the
     * fact that all non-zero values must represent error/failure results. Thus, layer zero will treat any non-zero
     * value as indicating that the block was not added. All non-zero integer values are defined by layer one
     * exclusively. This is so that layer one can have a flexible means of specifying error reasons without being
     * limited to the ways in which layer zero happens to specify them.
     *
     * @param block The block to add.
     * @return the add result code, which is 0 for success or any other integer for a failure.
     */
    public int add(Block block);
}
