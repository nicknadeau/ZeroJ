package net.nicknadeau.zero.block;

/**
 * The status of a {@link Block}.
 *
 * This is meta-data that is used internally by layer zero and is NOT part of consensus.
 */
public enum BlockStatus {

    /*
     * The block has been fully added to the blockchain. This has been confirmed by both layer zero and layer one.
     */
    ADDED(0)

    /*
     * The block has not been fully added to the blockchain. The block has not been confirmed as added by layer one yet.
     */
    , PENDING_ADDITION(1)

    /*
     * The block has not been fully removed from the blockchain. The block has not been confirmed as removed by layer
     * one yet.
     */
    , PENDING_DELETION(2)
    ;

    private final int value;
    private BlockStatus(int value) {
        this.value = value;
    }

    /**
     * Returns the integer representation of this block status.
     *
     * @return the integer representation of this status.
     */
    public int toInt() {
        return this.value;
    }

    /**
     * Returns the block status whose integer representation is the specified int, or {@code null} if there is no status
     * with the specified integer representation.
     *
     * @param value The integer representation of the status.
     * @return the status.
     */
    public static BlockStatus fromInt(int value) {
        for (BlockStatus status : BlockStatus.values()) {
            if (status.value == value) {
                return status;
            }
        }
        return null;
    }
}
