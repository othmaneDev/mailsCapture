package org.mo.configuration.exception;

import org.mo.access.exception.MailsCaptureException;

/**
 * ConfigurationException extends the {@link MailsCaptureException}
 */
public class ConfigurationException extends MailsCaptureException {

    public ConfigurationException(String message) {
        super(message);
    }
}
