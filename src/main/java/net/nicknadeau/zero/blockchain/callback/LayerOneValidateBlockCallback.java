package net.nicknadeau.zero.blockchain.callback;

import net.nicknadeau.zero.block.Block;

/**
 * A callback which must be implemented by the layer one protocol.
 *
 * This callback will be called by layer zero in order to pass the delegation of the block verification task off to
 * layer one. When this callback is called, it is guaranteed that layer zero has already performed its validation checks
 * on the block and that the block passes all of the layer zero validation checks.
 *
 * This callback will only ever be called once per block addition attempt.
 *
 * This callback does NOT indicate that the block has been added to the blockchain, nor even that it ever will be added.
 * There is a separate callback used to notify layer one of the actual addition of the block to the blockchain.
 *
 * This callback will be invoked by whichever thread is adding the block to the database.
 */
@FunctionalInterface
public interface LayerOneValidateBlockCallback {

    /**
     * Validates the specified block from the perspective of layer one and returns an integer code which indicates the
     * result of the validation operation.
     *
     * The returned error code is semi-opaque in the sense that a return value of zero indicates success (ie. the block
     * is a valid block). However, aside from zero, all other integer values are entirely opaque to layer zero aside
     * from the fact that all non-zero values must represent error/failure results. Thus, layer zero will treat any non-
     * zero value as indicating that the block is invalid. All non-zero integer values are defined by layer one
     * exclusively. This is so that layer one can have a flexible means of specifying error reasons without being
     * limited to the ways in which layer zero happens to specify them.
     *
     * @param block The block to validate.
     * @return the validation result code, which is 0 for success or any other integer for a failure.
     */
    public int validate(Block block);
}
