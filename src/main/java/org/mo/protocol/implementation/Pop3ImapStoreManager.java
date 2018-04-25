package org.mo.protocol.implementation;

import org.apache.commons.lang3.StringUtils;
import org.mo.configuration.Configuration;
import org.mo.configuration.constant.ConfigurationConstants;
import org.mo.configuration.exception.ConfigurationException;
import org.mo.filter.Pop3ImapMailFilter;
import org.mo.filter.exception.MailFilterException;
import org.mo.model.Mail;
import org.mo.protocol.exception.Pop3ImapStoreManagerException;
import org.mo.protocol.interfaces.IStoreManager;

import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;

import java.util.List;
import java.util.Properties;

/**
 * POP3 and Imap store manager implementation
 */
public class Pop3ImapStoreManager implements IStoreManager {

    private Store store;

    public void init() throws Pop3ImapStoreManagerException, ConfigurationException {

        // Get the configuration
        String protocol = Configuration.getInstance().getProtocol();
        Properties props = new Properties();
        props.setProperty(ConfigurationConstants.MAIL_STORE_PROTOCOL, protocol);
        props.setProperty(ConfigurationConstants.MAIL_IMAP_STARTTLS_ENABLE, Boolean.TRUE.toString());
        props.setProperty(ConfigurationConstants.MAIL_IMAP_SSL_ENABLE, Boolean.TRUE.toString());

        // Get The session and set the related configuration
        Session session = Session.getInstance(props, null);
        session.setDebug(Configuration.getInstance().getPropertyValueAsBoolean(ConfigurationConstants.MAIL_DEBUG_ENABLE));

        // On récupère le store
        try {
            store = session.getStore(protocol);
        }
        catch (NoSuchProviderException e) {
            throw new Pop3ImapStoreManagerException(e);
        }

    }

    /**
     * Opening a connection to the store
     *
     * @throws Pop3ImapStoreManagerException
     */
    public void openConnection() throws Pop3ImapStoreManagerException, ConfigurationException {

        if (store != null && !store.isConnected()) {

            // Get connection specific parameters
            String host = Configuration.getInstance().getPropertyValueAsString(ConfigurationConstants.MAIL_HOST);
            String user = Configuration.getInstance().getPropertyValueAsString(ConfigurationConstants.MAIL_USER);
            String password = Configuration.getInstance().getPropertyValueAsString(ConfigurationConstants.MAIL_PASSWORD);

            if (StringUtils.isNotEmpty(host) && StringUtils.isNotEmpty(user) && StringUtils.isNotEmpty(password)) {

                try {
                    store.connect(host, user, password);
                }
                catch (MessagingException e) {
                    throw new Pop3ImapStoreManagerException(e);
                }
            }
            else {
                throw new Pop3ImapStoreManagerException("Connection failed. " +
                        "It's mandatory to provide the host, user et password of the mail server");
            }
        }
        else {
            throw new Pop3ImapStoreManagerException("The store hasn't been initialized.");
        }

    }

    /**
     * Close the store connection
     *
     * @throws Pop3ImapStoreManagerException
     */
    public void closeConnection() throws Pop3ImapStoreManagerException {

        if (store != null && store.isConnected()) {

            try {
                store.close();
            }
            catch (MessagingException e) {
                throw new Pop3ImapStoreManagerException("Closing the store has failed", e);
            }
        }

    }

    @Override
    public List<Mail> fetchMails() throws Pop3ImapStoreManagerException, ConfigurationException {

        List<Mail> messageList;

        if (store != null && store.isConnected()) {

            Folder emailFolder;

            try {
                emailFolder = store.getFolder(ConfigurationConstants.MAIL_ROOT_DIRECTORY_NAME);
            }
            catch (MessagingException e) {
                emailFolder = null;
            }

            if (emailFolder == null) {

                // Trying to retrieve the default directory
                try {
                    emailFolder = store.getDefaultFolder();
                }
                catch (MessagingException e) {
                    throw new Pop3ImapStoreManagerException(e);
                }
            }

            // Opening the mail folder in read only
            try {
                emailFolder.open(Folder.READ_ONLY);
            }
            catch (MessagingException e) {
                throw new Pop3ImapStoreManagerException(e);
            }

            try {
                messageList = Pop3ImapMailFilter.filterMessages(emailFolder);
            }
            catch (MailFilterException e) {
                throw new Pop3ImapStoreManagerException(e);
            }
        }
        else {
            throw new Pop3ImapStoreManagerException("The store initialization failed " +
                    "or the connexion hasn't been established");
        }

        return messageList;
    }
}
