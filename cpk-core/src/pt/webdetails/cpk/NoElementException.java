/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */
package pt.webdetails.cpk;

/**
 *
 * @author joao
 */
public class NoElementException extends Exception {

    public NoElementException() {
        super();
    }

    public NoElementException(String message) {
        super(message);
    }

    public NoElementException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoElementException(Throwable cause) {
        super(cause);
    }
}
