package net.nicknadeau.zero.mock;

import net.nicknadeau.zero.blockchain.callback.LayerOneAddBlockCallback;
import net.nicknadeau.zero.blockchain.callback.LayerOneDeleteBlockCallback;
import net.nicknadeau.zero.blockchain.callback.LayerOneValidateBlockCallback;
import net.nicknadeau.zero.blockchain.callback.ZeroCallbacks;

/**
 * Helper class that constructs instances of {@link ZeroCallbacks} for tests.
 */
public final class CallbackHelper {

    /**
     * Returns callbacks, each of which returns a successful code when called.
     */
    public static ZeroCallbacks newSuccessfulCallbacks() {
        LayerOneValidateBlockCallback validateCallback = (block) -> 0;
        LayerOneAddBlockCallback addCallback = (block) -> 0;
        LayerOneDeleteBlockCallback deleteCallback = (block) -> 0;
        return newCallbacksPrivate(validateCallback, addCallback, deleteCallback);
    }

    /**
     * Returns callbacks, each of which returns a successful code when called, except for the given callback - the given
     * callback will be used in place of a default successful callback of that type.
     */
    public static ZeroCallbacks newCallbacks(LayerOneValidateBlockCallback validateCallback) {
        LayerOneAddBlockCallback addCallback = (block) -> 0;
        LayerOneDeleteBlockCallback deleteCallback = (block) -> 0;
        return newCallbacksPrivate(validateCallback, addCallback, deleteCallback);
    }

    /**
     * Returns callbacks, each of which returns a successful code when called, except for the given callback - the given
     * callback will be used in place of a default successful callback of that type.
     */
    public static ZeroCallbacks newCallbacks(LayerOneAddBlockCallback addCallback) {
        LayerOneValidateBlockCallback validateCallback = (block) -> 0;
        LayerOneDeleteBlockCallback deleteCallback = (block) -> 0;
        return newCallbacksPrivate(validateCallback, addCallback, deleteCallback);
    }

    /**
     * Returns callbacks, each of which returns a successful code when called, except for the given callback - the given
     * callback will be used in place of a default successful callback of that type.
     */
    public static ZeroCallbacks newCallbacks(LayerOneDeleteBlockCallback deleteCallback) {
        LayerOneValidateBlockCallback validateCallback = (block) -> 0;
        LayerOneAddBlockCallback addCallback = (block) -> 0;
        return newCallbacksPrivate(validateCallback, addCallback, deleteCallback);
    }

    private static ZeroCallbacks newCallbacksPrivate(LayerOneValidateBlockCallback validate, LayerOneAddBlockCallback add, LayerOneDeleteBlockCallback delete) {
        return ZeroCallbacks.Builder.newBuilder()
                .withValidateBlockCallback(validate)
                .withAddBlockCallback(add)
                .withDeleteBlockCallback(delete)
                .build()
                ;
    }
}
