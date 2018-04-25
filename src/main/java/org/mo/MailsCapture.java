package org.mo;

import org.apache.commons.collections4.CollectionUtils;
import org.mo.access.exception.MailsCaptureException;
import org.mo.configuration.Configuration;
import org.mo.configuration.constant.ConfigurationConstants;
import org.mo.model.Mail;
import org.mo.protocol.implementation.EwsStoreManager;
import org.mo.protocol.implementation.Pop3ImapStoreManager;
import org.mo.protocol.interfaces.IStoreManager;
import org.mo.service.interfaces.implementation.MailService;
import org.mo.service.interfaces.implementation.RepositoryService;
import org.mo.service.interfaces.interfaces.IMailService;

import java.util.List;

/**
 * @author MAHBOUB Othmane
 */
public class MailsCapture {

    public static void main(String[] args) throws MailsCaptureException {

        // Fisrt we need to check if everything is ok with the configuration
        Configuration.getInstance().checkConfiguration();

        // Checking the protocol to be used
        String protocol = Configuration.getInstance().getProtocol();

        // Then We should choose the store manager to be used according to the used protocol
        IStoreManager storeManager;

        switch (protocol) {

            case ConfigurationConstants.MAIL_STORE_PROTOCOL_IMAP:

            case ConfigurationConstants.MAIL_STORE_PROTOCOL_POP3:
                storeManager = new Pop3ImapStoreManager();
                break;

            case ConfigurationConstants.MAIL_STORE_PROTOCOL_MICROSOFT_EWS:
                storeManager = new EwsStoreManager();
                break;

            default:
                storeManager = null;
                break;
        }

        if (storeManager != null) {

            // Initialize the store
            storeManager.init();

            // If the used protocol is Pop3 or Imap, then a connexion to the store must be established
            if (storeManager instanceof Pop3ImapStoreManager) {
                ((Pop3ImapStoreManager) storeManager).openConnection();
            }

            // Fetching the mails using the store
            List<Mail> mails = storeManager.fetchMails();

            // Initialise the repository service to write output files and folders
            RepositoryService.getInstance().initializeOutputDir();

            if (CollectionUtils.isNotEmpty(mails)) {

                Integer totalMessages = mails.size();

                Integer processingCounter = 1;

                // Initialise the mails processing service
                IMailService mailService = new MailService();

                // Parsing and processing the mails
                for (Mail mail : mails) {
                    mailService.processMail(mail);
                }

                // Writing the JSON report for each sender
                mailService.writeJsonReport();
            }

            // If the protocol used is Pop3 or Imap, then the connexion must be closed
            if (storeManager instanceof Pop3ImapStoreManager) {
                ((Pop3ImapStoreManager) storeManager).closeConnection();
            }
        }
        else {
            throw new MailsCaptureException(String.format("Unknown protocol '%s'. Closing the application", protocol));
        }

    }
}
