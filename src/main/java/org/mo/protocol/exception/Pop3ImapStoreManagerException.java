package org.mo.protocol.exception;

import org.mo.access.exception.MailsCaptureException;

public class Pop3ImapStoreManagerException extends MailsCaptureException {

    public Pop3ImapStoreManagerException(Throwable e) {
        super(e);
    }

    public Pop3ImapStoreManagerException(String message) {
        super(message);
    }

    public Pop3ImapStoreManagerException(String message, Throwable e) {
        super(message, e);
    }
}
