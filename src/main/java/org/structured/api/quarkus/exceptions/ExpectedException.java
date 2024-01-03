
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
