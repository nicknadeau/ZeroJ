package net.nicknadeau.zero.util;

/**
 * A hash function.
 */
@FunctionalInterface
public interface HashFunction {

    /**
     * Returns a hash of the specified payload.
     *
     * @param payload The payload to hash.
     * @return the hash of the payload.
     * @throws NullPointerException if payload is null.
     * @throws IllegalArgumentException if {@code payload.length == 0}.
     */
    public byte[] hash(byte[] payload);
}
