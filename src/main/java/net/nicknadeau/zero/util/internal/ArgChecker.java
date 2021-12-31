package net.nicknadeau.zero.util.internal;

/**
 * A utility class that is used to verify method arguments.
 */
public final class ArgChecker {

    /**
     * Throws {@link NullPointerException} if {@code object} is null. Otherwise, does nothing.
     *
     * @param object the object to test.
     * @throws NullPointerException if object is null.
     */
    public static void assertNonNull(Object object) {
        if (object == null) {
            throw new NullPointerException("object must be non-null");
        }
    }

    /**
     * Throws {@link IllegalArgumentException} if {@code value} is equal to {@code target}.
     *
     * @param value The value to test.
     * @param target The target value, which value must not be equal to.
     * @throws IllegalArgumentException if value is equal to target.
     */
    public static void assertNotEquals(Object value, Object target) {
        if (value.equals(target)) {
            throw new IllegalArgumentException("value " + value + " must not equal " + target);
        }
    }

    /**
     * Throws {@link IllegalArgumentException} if {@code value} is not equal to {@code target}.
     *
     * @param value The value to test.
     * @param target The target value, which value must be equal to.
     * @throws IllegalArgumentException if value is not equal to target.
     */
    public static void assertEqualTo(int value, int target) {
        if (value != target) {
            throw new IllegalArgumentException("value " + value + " must be equal to " + target);
        }
    }

    /**
     * Throws {@link IllegalArgumentException} if {@code value} is less than {@code threshold}.
     *
     * @param value The value to test.
     * @param threshold The threshold, which value must be greater or equal to.
     * @throws IllegalArgumentException if value is less than threshold.
     */
    public static void assertGreaterOrEqualTo(int value, int threshold) {
        if (value < threshold) {
            throw new IllegalArgumentException("value " + value + " must be greater or equal to " + threshold);
        }
    }
}
