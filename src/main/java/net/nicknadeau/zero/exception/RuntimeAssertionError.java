package net.nicknadeau.zero.exception;

/**
 * Various assertion errors that are thrown at runtime as unchecked exceptions.
 */
public final class RuntimeAssertionError extends RuntimeException {

    private RuntimeAssertionError(String message) {
        super(message);
    }

    /**
     * Returns a new {@link RuntimeAssertionError} to indicate that an unexpected condition (ie. a condition which
     * violates an internal invariant) occurred.
     *
     * @return the error object.
     */
    public static RuntimeAssertionError unexpected() {
        return new RuntimeAssertionError("unexpected condition encountered");
    }
}
