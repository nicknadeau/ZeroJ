package net.nicknadeau.zero.util;

/**
 * A verification function that is used to determine whether or not a cryptographic signature was correctly obtained
 * for some public-private key pair and a payload.
 */
@FunctionalInterface
public interface SignatureVerifier {

    /**
     * Returns {@code true} if and only if the private key which corresponds to the specified public key was used to
     * sign the specified payload and the resultant signature is equivalent to the specified signature. Otherwise, if
     * any one of these conditions is not true, then returns {@code false}.
     *
     * @param publicKey The cryptographic public key.
     * @param payload The payload that was signed.
     * @param signature The expected signature produced by signing the payload, which is to be verified.
     * @return whether or not the signature is legitimate.
     * @throws NullPointerException if publicKey, payload, or signature are null.
     * @throws IllegalArgumentException if publicKey, payload, or signature are invalid in any way.
     */
    public boolean isValidSignature(byte[] publicKey, byte[] payload, byte[] signature);
}
