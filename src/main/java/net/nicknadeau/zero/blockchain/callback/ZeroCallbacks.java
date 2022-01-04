package net.nicknadeau.zero.blockchain.callback;

import net.nicknadeau.zero.util.internal.ArgChecker;

/**
 * A class which holds all of the callbacks any layer one implementation must implement in order to interface with
 * layer zero correctly.
 *
 * Use {@link ZeroCallbacks.Builder} to construct new instances of this class.
 *
 * This class is immutable insofar as each of the callback implementations are immutable.
 */
public final class ZeroCallbacks {
    private final LayerOneValidateBlockCallback validateCallback;
    private final LayerOneAddBlockCallback addCallback;
    private final LayerOneDeleteBlockCallback deleteCallback;

    private ZeroCallbacks(LayerOneValidateBlockCallback validateCallback
            , LayerOneAddBlockCallback addCallback
            , LayerOneDeleteBlockCallback deleteCallback
    ) {
        ArgChecker.assertNonNull(validateCallback);
        ArgChecker.assertNonNull(addCallback);
        ArgChecker.assertNonNull(deleteCallback);
        this.validateCallback = validateCallback;
        this.addCallback = addCallback;
        this.deleteCallback = deleteCallback;
    }

    /**
     * Returns the callback which is used by layer one to validate a block.
     *
     * @return the validation callback.
     */
    public LayerOneValidateBlockCallback getLayerOneValidateBlockCallback() {
        return this.validateCallback;
    }

    /**
     * Returns the callback which is used by layer one to add a block.
     *
     * @return the add block callback.
     */
    public LayerOneAddBlockCallback getLayerOneAddBlockCallback() {
        return this.addCallback;
    }

    /**
     * Returns the callback which is used by layer one to delete a block.
     *
     * @return the delete block callback.
     */
    public LayerOneDeleteBlockCallback getLayerOneDeleteBlockCallback() {
        return this.deleteCallback;
    }

    //------------------------------------------------------------------------------------------------------------------

    /**
     * A builder which constructs new instances of {@link ZeroCallbacks}.
     */
    public static final class Builder {
        private LayerOneValidateBlockCallback validateCallback;
        private LayerOneAddBlockCallback addCallback;
        private LayerOneDeleteBlockCallback deleteCallback;

        private Builder() {}

        /**
         * Returns a new builder instance.
         *
         * @return the new builder.
         */
        public static Builder newBuilder() {
            return new Builder();
        }

        /**
         * Uses the specified callback as the {@link LayerOneValidateBlockCallback} callback.
         *
         * @param callback The callback to use.
         * @return this builder.
         */
        public Builder withValidateBlockCallback(LayerOneValidateBlockCallback callback) {
            this.validateCallback = callback;
            return this;
        }

        /**
         * Uses the specified callback as the {@link LayerOneAddBlockCallback} callback.
         *
         * @param callback The callback to use.
         * @return this builder.
         */
        public Builder withAddBlockCallback(LayerOneAddBlockCallback callback) {
            this.addCallback = callback;
            return this;
        }

        /**
         * Uses the specified callback as the {@link LayerOneDeleteBlockCallback} callback.
         *
         * @param callback The callback to use.
         * @return this builder.
         */
        public Builder withDeleteBlockCallback(LayerOneDeleteBlockCallback callback) {
            this.deleteCallback = callback;
            return this;
        }

        /**
         * Returns a newly constructed instance of {@link ZeroCallbacks}, which uses each of the callbacks given to this
         * builder. If multiple callbacks of the same type were provided, only the last such callback will be used.
         *
         * @return the new instance.
         */
        public ZeroCallbacks build() {
            return new ZeroCallbacks(this.validateCallback, this.addCallback, this.deleteCallback);
        }
    }
}
