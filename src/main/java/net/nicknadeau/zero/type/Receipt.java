package net.nicknadeau.zero.type;

import net.nicknadeau.zero.util.internal.ArgChecker;

/**
 * A receipt that is used to describe the result of performing some action.
 *
 * A successful receipt will have its {@link Receipt#getCode()} value equal to {@link ReceiptCode#SUCCESS}. In this
 * case, {@link Receipt#getErrorMessage()} will be invalid, {@link Receipt#getUnexpectedErrorCause()} will return a
 * null cause, and {@link Receipt#getLayerOneErrorCode()} will be nonsense.
 *
 * An unexpected error receipt will have its {@link Receipt#getCode()} value equal to {@link ReceiptCode#UNEXPECTED}.
 * In this case, {@link Receipt#getErrorMessage()} will be invalid, {@link Receipt#getLayerOneErrorCode()} will be
 * nonsense, but {@link Receipt#getUnexpectedErrorCause()} will return a non-null exception, which was the cause of the
 * error.
 *
 * A layer one failure receipt will have its {@link Receipt#getLayerOneErrorCode()} value equal to some error code
 * defined by the layer one protocol, and which is opaque to us. In this case, something went wrong in layer one and
 * we have no visibility into it. The value returned by {@link Receipt#getErrorMessage()} will be null and the value
 * returned by {@link Receipt#getUnexpectedErrorCause()} will also be null.
 *
 * Otherwise, the receipt is simply a failed receipt and {@link Receipt#getErrorMessage()} will be set to a valid
 * message but {@link Receipt#getUnexpectedErrorCause()} will return a null cause.
 *
 * Two receipts are considered equal if and only if they are the same instance.
 */
public final class Receipt {
    private final ReceiptCode code;
    private final String errorMessage;
    private final Exception unexpectedErrorCause;
    private final int layerOneErrorCode;

    private Receipt(ReceiptCode code, String errorMessage, Exception unexpectedErrorCause, int layerOneErrorCode) {
        this.code = code;
        this.errorMessage = errorMessage;
        this.unexpectedErrorCause = unexpectedErrorCause;
        this.layerOneErrorCode = layerOneErrorCode;
    }

    /**
     * Returns a new receipt whose code is {@link ReceiptCode#SUCCESS} and which has a null error message and null
     * unexpected error cause.
     *
     * @return the new receipt.
     */
    public static Receipt successfulReceipt() {
        return new Receipt(ReceiptCode.SUCCESS, null, null, -1);
    }

    /**
     * Returns a new receipt whose code is {@link ReceiptCode#UNEXPECTED} and which has a null error message and whose
     * unexpected error cause is the specified cause.
     *
     * @param cause The unexpected error cause.
     * @return the new receipt.
     * @throws NullPointerException if cause is null.
     */
    public static Receipt unexpectedErrorReceipt(Exception cause) {
        ArgChecker.assertNonNull(cause);
        return new Receipt(ReceiptCode.UNEXPECTED, null, cause, -1);
    }

    /**
     * Returns a new receipt whose code is the specified code and which has its error message set to the specified
     * message and which has a null unexpected error cause.
     *
     * @param code The receipt code.
     * @param errorMessage The error message.
     * @return the new receipt.
     * @throws NullPointerException if code is null or errorMessage is null.
     * @throws IllegalArgumentException if code is success or unexpected.
     */
    public static Receipt failedReceipt(ReceiptCode code, String errorMessage) {
        ArgChecker.assertNonNull(code);
        ArgChecker.assertNotEquals(code, ReceiptCode.SUCCESS);
        ArgChecker.assertNotEquals(code, ReceiptCode.UNEXPECTED);
        ArgChecker.assertNonNull(errorMessage);
        return new Receipt(code, errorMessage, null, -1);
    }

    /**
     * Returns a new receipt whose code is {@link ReceiptCode#LAYER_ONE_FAILURE} and which has its layer one error code
     * set to the specified code.
     *
     * @param layerOneErrorCode The error code that is defined by layer one and which encapsulates the failure reason.
     * @return the the receipt.
     */
    public static Receipt layerOneFailedReceipt(int layerOneErrorCode) {
        return new Receipt(ReceiptCode.LAYER_ONE_FAILURE, null, null, layerOneErrorCode);
    }

    /**
     * Returns the status code of this receipt.
     *
     * @return this receipt's code.
     */
    public ReceiptCode getCode() {
        return this.code;
    }

    /**
     * Returns the error message, which is only valid if the receipt code is not {@link ReceiptCode#SUCCESS} nor is
     * {@link ReceiptCode#UNEXPECTED}, otherwise is null.
     *
     * @return the error message.
     */
    public String getErrorMessage() {
        return this.errorMessage;
    }

    /**
     * Returns the cause of the unexpected error, which is only valid if the receipt code is
     * {@link ReceiptCode#UNEXPECTED}, otherwise is null.
     *
     * @return the unexpected error cause.
     */
    public Exception getUnexpectedErrorCause() {
        return this.unexpectedErrorCause;
    }

    /**
     * Returns the opaque layer one error code, which is only valid if the receipt code is
     * {@link ReceiptCode#LAYER_ONE_FAILURE}, otherwise the returned value is nonsense.
     *
     * @return the layer one error code.
     */
    public int getLayerOneErrorCode() {
        return this.layerOneErrorCode;
    }

    @Override
    public String toString() {
        if (this.code == ReceiptCode.SUCCESS) {
            return Receipt.class.getSimpleName() + "{ success }";
        } else if (this.code == ReceiptCode.UNEXPECTED) {
            return Receipt.class.getSimpleName() + "{ unexpected error }";
        } else if (this.code == ReceiptCode.LAYER_ONE_FAILURE) {
            return Receipt.class.getSimpleName() + "{ layer one failure }";
        } else {
            return Receipt.class.getSimpleName() + "{ failed | code=" + this.code + " }";
        }
    }
}
