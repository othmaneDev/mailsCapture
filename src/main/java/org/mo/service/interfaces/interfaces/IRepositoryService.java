package org.mo.service.interfaces.interfaces;

import org.json.simple.JSONArray;
import org.mo.configuration.exception.ConfigurationException;
import org.mo.model.Attachment;
import org.mo.model.Mail;
import org.mo.service.interfaces.exception.RepositoryServiceException;

/**
 * Repository service interface that exposes what it can do
 */
public interface IRepositoryService {

    /**
     * Initialise output directory to store mails
     *
     * @throws RepositoryServiceException
     */
    Boolean initializeOutputDir() throws RepositoryServiceException, ConfigurationException;

    /**
     * Write the eml file within the output directory
     *
     * @param senderMail The sender mail
     * @param index The index of the file on which the writing begins
     * @param mailMessage The {@link Mail} to write
     * @throws RepositoryServiceException
     */
    void writeEml(String senderMail, Long index, Mail mailMessage) throws RepositoryServiceException;

    /**
     * Write the mail attachment in the appropriate directory
     *
     * @param senderMail The sender mail
     * @param index The index of the file on which the writing begins
     * @param attachment The {@link Attachment}
     * @throws RepositoryServiceException
     */
    void writeAttachment(String senderMail, Long index, Attachment attachment) throws RepositoryServiceException;

    /**
     * Write the summary text in the appropriate folder
     * 
     * @param senderMail The sender mail
     * @param index The index of the file on which the writing begins
     * @param subject The mail subject
     * @param content The mail content
     * @throws RepositoryServiceException
     */
    void writeTxtFile(String senderMail, Long index, String subject, String content) throws RepositoryServiceException;

    /**
     * For one according sender, write the JSON repor.
     *
     * @param senderMail The sender mail
     * @param summaryArray A json Array of the content to write
     */
    void writeJsonFile(String senderMail, JSONArray summaryArray) throws RepositoryServiceException;
}