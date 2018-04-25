package org.mo.protocol.interfaces;

import org.mo.configuration.exception.ConfigurationException;
import org.mo.model.Mail;
import org.mo.protocol.exception.EwsStoreManagerException;
import org.mo.protocol.exception.Pop3ImapStoreManagerException;
import org.mo.protocol.exception.StoreManagerException;

import java.util.List;

/**
 * Store Manager interface that expose the store capabilities
 */
public interface IStoreManager {

    /**
     * Initialise the storeManager by loading the required configuration and the store
     * 
     * @throws StoreManagerException
     */
    void init() throws StoreManagerException, EwsStoreManagerException, Pop3ImapStoreManagerException, ConfigurationException;

    /**
     * Parse the mails in order to process them
     *
     * @throws StoreManagerException
     */
    List<Mail> fetchMails() throws StoreManagerException, EwsStoreManagerException, Pop3ImapStoreManagerException, ConfigurationException;
}
