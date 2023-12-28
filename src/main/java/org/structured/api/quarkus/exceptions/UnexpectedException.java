package org.structured.api.quarkus.exceptions;

/**
 * The type Unexpected exception.
 */
public class UnexpectedException extends RuntimeException {
    /**
     * Instantiates a new Unexpected exception.
     */
    public UnexpectedException() {
    }

    /**
     * Instantiates a new Unexpected exception.
     *
     * @param message the message
     */
    public UnexpectedException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Unexpected exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public UnexpectedException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Instantiates a new Unexpected exception.
     *
     * @param cause the cause
     */
    public UnexpectedException(Throwable cause) {
        super(cause);
    }

    /**
     * Instantiates a new Unexpected exception.
     *
     * @param message            the message
     * @param cause              the cause
     * @param enableSuppression  the enable suppression
     * @param writableStackTrace the writable stack trace
     */
    public UnexpectedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
