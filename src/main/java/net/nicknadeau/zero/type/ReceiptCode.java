package net.nicknadeau.zero.type;

/**
 * A status code captured by a {@link Receipt} in order to describe the result of some action.
 */
public enum ReceiptCode {

    /*
     * The action was successful.
     */
    SUCCESS

    /*
     * The action could not be performed because some required resource does not exist.
     */
    , DOES_NOT_EXIST

    /*
     * The action could not be performed because some resource which should not exist does exist.
     */
    , EXISTS

    /*
     * The action could not be performed because an invalid parameter was provided.
     */
    , INVALID_PARAMETER

    /*
     * The action could not be performed because there is a mismatch between major protocol versions and thus the
     * action is illegitimate.
     */
    , VERSION_MISMATCH

    /*
     * The action was performed by layer one, not layer zero, and it failed.
     */
    , LAYER_ONE_FAILURE

    /*
     * The action could not be performed because an unexpected error occurred.
     */
    , UNEXPECTED
}
