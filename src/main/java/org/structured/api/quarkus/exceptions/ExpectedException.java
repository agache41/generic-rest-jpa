package org.structured.api.quarkus.exceptions;

/**
 * The type Expected exception.
 */
public class ExpectedException extends RuntimeException {
    /**
     * Instantiates a new Expected exception.
     */
    public ExpectedException() {
    }

    /**
     * Instantiates a new Expected exception.
     *
     * @param message the message
     */
    public ExpectedException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Expected exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public ExpectedException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Instantiates a new Expected exception.
     *
     * @param cause the cause
     */
    public ExpectedException(Throwable cause) {
        super(cause);
    }

    /**
     * Instantiates a new Expected exception.
     *
     * @param message            the message
     * @param cause              the cause
     * @param enableSuppression  the enable suppression
     * @param writableStackTrace the writable stack trace
     */
    public ExpectedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
