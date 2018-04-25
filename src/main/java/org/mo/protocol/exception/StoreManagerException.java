package org.mo.protocol.exception;

import org.mo.access.exception.MailsCaptureException;

public class StoreManagerException extends MailsCaptureException {

    public StoreManagerException(Throwable e) {
        super(e);
    }

    public StoreManagerException(String message) {
        super(message);
    }

    public StoreManagerException(String message, Throwable e) {
        super(message, e);
    }
}
