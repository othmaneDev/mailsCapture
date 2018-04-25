package org.mo.access.exception;

/**
 * All the MailsCaptureExceptions must extend this class
 */
public class MailsCaptureException extends Exception {

    public MailsCaptureException(Throwable e) {
        super(e);
    }

    public MailsCaptureException(String message) {
        super(message);
    }

    public MailsCaptureException(String message, Throwable e) {
        super(message, e);
    }
}
