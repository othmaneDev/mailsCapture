package org.mo.protocol.exception;

import org.mo.access.exception.MailsCaptureException;

public class EwsStoreManagerException extends MailsCaptureException {

    public EwsStoreManagerException(Throwable e) {
        super(e);
    }

    public EwsStoreManagerException(String message) {
        super(message);
    }

    public EwsStoreManagerException(String message, Throwable e) {
        super(message, e);
    }
}
