package org.mo.filter.exception;

import org.mo.access.exception.MailsCaptureException;

public class MailFilterException extends MailsCaptureException {

    public MailFilterException(Throwable e) {
        super(e);
    }
}
