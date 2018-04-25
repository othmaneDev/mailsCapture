package org.mo.service.interfaces.implementation;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import microsoft.exchange.webservices.data.core.exception.service.local.ServiceLocalException;
import microsoft.exchange.webservices.data.core.service.item.Item;
import microsoft.exchange.webservices.data.property.complex.FileAttachment;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.mo.configuration.Configuration;
import org.mo.configuration.exception.ConfigurationException;
import org.mo.model.Attachment;
import org.mo.model.Mail;
import org.mo.service.interfaces.constant.RepositoryConstants;
import org.mo.service.interfaces.exception.RepositoryServiceException;
import org.mo.service.interfaces.interfaces.IRepositoryService;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * The Repository service implementation
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RepositoryService implements IRepositoryService {

    public static final String OUTPUT_DIRECTORY_CLEANING_FAILED_ERROR = "The output directory couldn't be cleaned";

    private File outputDirectory;

    /**
     * Unique instance du repository.
     */
    private static IRepositoryService REPOSITORY_SERVICE_INSTANCE;

    /**
     * Get the unique instance of the {@link RepositoryService}
     *
     * @return The unique instance
     */
    public static IRepositoryService getInstance() {
        if (REPOSITORY_SERVICE_INSTANCE == null) {
            REPOSITORY_SERVICE_INSTANCE = new RepositoryService();
        }
        return REPOSITORY_SERVICE_INSTANCE;
    }

    @Override
    public Boolean initializeOutputDir() throws RepositoryServiceException, ConfigurationException {

        outputDirectory = new File(Configuration.getInstance().getOutputDirPath());

        // If the directory already exist (Not a new one), clean it
        if (outputDirectory.exists()) {

            try {
                FileUtils.cleanDirectory(outputDirectory);
            }
            catch (IOException e) {
                throw new RepositoryServiceException(OUTPUT_DIRECTORY_CLEANING_FAILED_ERROR);
            }
        }

        return Boolean.TRUE;
    }

    @Override
    public void writeEml(String senderMail, Long index, Mail mail) throws RepositoryServiceException {

        if (StringUtils.isNotEmpty(senderMail) && index != null && mail != null) {

            File emlFile = getFile(senderMail, index, index + RepositoryConstants.EML_FILE_EXTENSION);

            // Check for IMAP and POP3 messages
            if (mail.getMessage() instanceof Message) {

                try {
                    ((Message) mail.getMessage()).writeTo(FileUtils.openOutputStream(emlFile));
                }
                catch (IOException | MessagingException e) {
                    throw new RepositoryServiceException(String.format(
                        "An error occured when writing the EML file (senderMail = '%s', index = %d",
                        senderMail,
                        index));

                }
            }

            // Check for the exchange messages
            else if (mail.getMessage() instanceof Item) {

                try {

                    String mimeContent = ((Item) mail.getMessage()).getMimeContent().toString();

                    if (StringUtils.isNotEmpty(mimeContent)) {
                        FileUtils.copyToFile(new ByteArrayInputStream(mimeContent.getBytes()), emlFile);
                    }
                }
                catch (ServiceLocalException | IOException e) {
                    throw new RepositoryServiceException(String.format(
                        "An error occured when writing the EML file  (senderMail = '%s', index = %d",
                        senderMail,
                        index));
                }

            }
        }

    }

    /**
     * @see {@link IRepositoryService#writeAttachment(String, Long, Attachment)}
     */
    @Override
    public void writeAttachment(String senderMail, Long index, Attachment attachment) throws RepositoryServiceException {

        if (StringUtils.isNotEmpty(senderMail) && index != null && attachment != null) {

            // Check for the POP3 and IMAP attachments
            if (attachment.getAttachment() instanceof BodyPart) {

                BodyPart bodyPart = (BodyPart) attachment.getAttachment();

                try {

                    File attachmentFile = getFile(senderMail,
                        index,
                        index + "_" + bodyPart.getFileName());

                    FileUtils.copyToFile(bodyPart.getInputStream(), attachmentFile);
                }
                catch (MessagingException | IOException e) {
                    throw new RepositoryServiceException(String.format("An error occurs" +
                            " when writing one of the sender attachments " + " '%s', index = %d", senderMail, index));
                }
            }

            // Check for the exchange attachments
            else if (attachment.getAttachment() instanceof FileAttachment) {

                FileAttachment fileAttachment = (FileAttachment) attachment.getAttachment();
                OutputStream fileStream = null;

                try {

                    File attachmentFile = getFile(senderMail,
                        index,
                        index + "_" + fileAttachment.getName());

                    fileStream = new FileOutputStream(attachmentFile);

                    fileAttachment.load(fileStream);
                }
                catch (Exception e) {
                    throw new RepositoryServiceException(e);
                }
                finally {

                    if (fileStream != null) {

                        try {

                            fileStream.flush();
                            fileStream.close();
                        }
                        catch (IOException e) {
                            throw new RepositoryServiceException("An error occurs when closing the fileStream");
                        }
                    }
                }
            }
        }

    }

    @Override
    public void writeTxtFile(String senderMail, Long index, String subject, String content) throws RepositoryServiceException {

        if (StringUtils.isNotEmpty(senderMail) && index != null) {

            File textFile = getFile(senderMail, index, index + RepositoryConstants.TEXT_FILE_EXTENSION);

            try {
                FileUtils.writeLines(textFile,
                    Arrays.asList(RepositoryConstants.SUBJECT_WORDNG + subject,
                        RepositoryConstants.CONTENT_WORDING + content));
            }
            catch (IOException e) {
                throw new RepositoryServiceException(e);
            }
        }

    }

    @Override
    public void writeJsonFile(String senderMail, JSONArray summaryArray) throws RepositoryServiceException {

        if (StringUtils.isNotEmpty(senderMail) && summaryArray != null) {

            File summaryFile = getFile(senderMail, null, RepositoryConstants.JSON_FILE_NAME);
            FileWriter fileWriter = null;

            try {

                fileWriter = new FileWriter(summaryFile);

                fileWriter.write(summaryArray.toJSONString());
            }
            catch (IOException e) {
                throw new RepositoryServiceException(e);
            }
            finally {

                if (fileWriter != null) {

                    try {

                        fileWriter.flush();
                        fileWriter.close();

                    }
                    catch (IOException e) {
                        throw new RepositoryServiceException(e);
                    }
                }
            }
        }

    }

    /**
     * Getting a {@link File} object for one sender at an according index
     *
     * @param senderMail Sender mail
     * @param index Theincremental index which is optionnal
     * @param filename The file name
     * @return a {@link File} Object
     */
    private File getFile(String senderMail, Long index, String filename) {
        return new File(outputDirectory.getAbsolutePath() + File.separator + senderMail.toLowerCase() +
                (index != null ? File.separator + index : StringUtils.EMPTY) + File.separator + filename);

    }
}
