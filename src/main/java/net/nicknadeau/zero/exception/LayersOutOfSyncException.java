package net.nicknadeau.zero.exception;

/**
 * An error that indicates that layer zero and layer one have become out of sync with one another. In other words, one
 * layer considers a certain block to exist and be part of the blockchain while the other layer disagrees. This is fatal
 * in the sense that any blockchain in this state is rendered unusable until this error is resolved.
 */
public final class LayersOutOfSyncException extends Exception {

    public LayersOutOfSyncException() {
        super();
    }
}
