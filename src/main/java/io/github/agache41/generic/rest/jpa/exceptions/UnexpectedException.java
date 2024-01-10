
/*
 *    Copyright 2022-2023  Alexandru Agache
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package io.github.agache41.generic.rest.jpa.exceptions;

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
