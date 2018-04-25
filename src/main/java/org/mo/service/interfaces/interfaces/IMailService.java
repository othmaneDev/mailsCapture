package org.mo.service.interfaces.interfaces;

import org.mo.model.Mail;
import org.mo.service.interfaces.exception.RepositoryServiceException;

/**
 * Mail service interface that expose what the service can do : processing a mail and writing report at json format
 */
public interface IMailService {

    /**
     * Processing a {@link org.mo.model.Mail}
     *
     * @param mail Mail to process
     */
    void processMail(Mail mail) throws RepositoryServiceException;

    /**
     * Wrting a JSON rapport in the output file for each sender found within the processing phase
     */
    void writeJsonReport();
}
