package org.mo.service.interfaces.exception;

import org.mo.access.exception.MailsCaptureException;

/**
 * The repositoryService related exceptions
 */
public class RepositoryServiceException extends MailsCaptureException {

    public RepositoryServiceException(String message) {
        super(message);
    }

    public RepositoryServiceException(Throwable t) {
        super(t);
    }

    public RepositoryServiceException(String message, Throwable t) {
        super(message, t);
    }
}