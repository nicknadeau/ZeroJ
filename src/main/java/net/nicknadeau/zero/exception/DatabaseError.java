package net.nicknadeau.zero.exception;

/**
 * Thrown to indicate that a database error has occurred.
 */
public final class DatabaseError extends Exception {

    /**
     * Returns a newly created database error with the given error message.
     *
     * @param message The error message.
     */
    public DatabaseError(String message) {
        super(message);
    }

    /**
     * Returns a newly created database error with the given cause.
     *
     * @param cause The cause of the error.
     */
    public DatabaseError(Throwable cause) {
        super(cause);
    }

    /**
     * Returns a newly created database error with the given error message and cause.
     *
     * @param message The error message.
     * @param cause The cause of the error.
     */
    public DatabaseError(String message, Throwable cause) {
        super(message, cause);
    }
}
