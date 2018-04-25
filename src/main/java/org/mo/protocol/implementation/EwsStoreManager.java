package org.mo.protocol.implementation;

import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.PropertySet;
import microsoft.exchange.webservices.data.core.enumeration.misc.ExchangeVersion;
import microsoft.exchange.webservices.data.core.enumeration.property.BasePropertySet;
import microsoft.exchange.webservices.data.core.enumeration.property.WellKnownFolderName;
import microsoft.exchange.webservices.data.core.enumeration.search.LogicalOperator;
import microsoft.exchange.webservices.data.core.enumeration.search.OffsetBasePoint;
import microsoft.exchange.webservices.data.core.service.item.EmailMessage;
import microsoft.exchange.webservices.data.core.service.item.Item;
import microsoft.exchange.webservices.data.core.service.schema.EmailMessageSchema;
import microsoft.exchange.webservices.data.core.service.schema.ItemSchema;
import microsoft.exchange.webservices.data.credential.WebCredentials;
import microsoft.exchange.webservices.data.search.FindItemsResults;
import microsoft.exchange.webservices.data.search.ItemView;
import microsoft.exchange.webservices.data.search.filter.SearchFilter;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.mo.access.exception.MailsCaptureException;
import org.mo.configuration.Configuration;
import org.mo.configuration.constant.ConfigurationConstants;
import org.mo.configuration.exception.ConfigurationException;
import org.mo.filter.constant.MailFilterConstants;
import org.mo.model.Mail;
import org.mo.protocol.exception.EwsStoreManagerException;
import org.mo.protocol.interfaces.IStoreManager;
import org.mo.util.MailsCaptureUtil;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * EWS store manager implementation
 */
public class EwsStoreManager implements IStoreManager {

    private static final Integer PAGE_SIZE = 150;
    private static final Integer VIEW_OFFSET = 0;
    private ExchangeService service;

    public void init() throws EwsStoreManagerException, ConfigurationException {

        if (service == null) {

            String host = Configuration.getInstance().getPropertyValueAsString(ConfigurationConstants.MAIL_HOST);
            String user = Configuration.getInstance().getPropertyValueAsString(ConfigurationConstants.MAIL_USER);
            String password = Configuration.getInstance().getPropertyValueAsString(ConfigurationConstants.MAIL_PASSWORD);
            String domain = Configuration.getInstance().getPropertyValueAsString(ConfigurationConstants.MAIL_EWS_DOMAIN);

            // Paramétrage du service EWS
            service = new ExchangeService(ExchangeVersion.Exchange2010_SP2);
            service.setCredentials(new WebCredentials(user, password, domain));
            service.setTraceEnabled(Configuration.getInstance().getPropertyValueAsBoolean(ConfigurationConstants.MAIL_DEBUG_ENABLE));

            try {
                service.setUrl(new URI(host));
            }
            catch (URISyntaxException e) {
                throw new EwsStoreManagerException(e);
            }
        }

    }

    @Override
    public List<Mail> fetchMails() throws EwsStoreManagerException {

        List<Mail> messageList = new ArrayList<>();

        if (service != null) {

            Boolean hasMoreMails = Boolean.TRUE;

            ItemView view = new ItemView(PAGE_SIZE, VIEW_OFFSET, OffsetBasePoint.Beginning);

            view.setPropertySet(new PropertySet(BasePropertySet.IdOnly, EmailMessageSchema.From,
                ItemSchema.Subject, ItemSchema.HasAttachments));

            FindItemsResults<Item> findResults;

            while (hasMoreMails) {

                try {

                    findResults = service.findItems(WellKnownFolderName.Inbox, getFilters(), view);

                    // Si on a un résultat alors on le converti pour traitement.
                    if (CollectionUtils.isNotEmpty(findResults.getItems())) {

                        service.loadPropertiesForItems(findResults,
                            new PropertySet(BasePropertySet.FirstClassProperties,
                                EmailMessageSchema.Attachments, EmailMessageSchema.MimeContent));

                        findResults.getItems()
                            .forEach(email -> {
                                try {
                                    messageList.add(MailsCaptureUtil.convertEwsEmailMessageToMail((EmailMessage) email));
                                }
                                catch (MailsCaptureException e) {
                                    // TODO log an error using self4j extention with lambok
                                }
                            });
                    }

                    hasMoreMails = findResults.isMoreAvailable();
                    if (hasMoreMails) {
                        view.setOffset(view.getOffset() + PAGE_SIZE);
                    }
                }
                catch (Exception e) {
                    throw new EwsStoreManagerException("An Error occurs when retrieving mails", e);

                }
            }
        }
        else {
            throw new EwsStoreManagerException("The store isn't initialized" +
                    "or the connexion isn't established yet");
        }

        return messageList;
    }

    private SearchFilter getFilters() throws ConfigurationException {

        String senderFilter = Configuration.getInstance().getPropertyValueAsString(MailFilterConstants.MAIL_FILTER_FROM);
        String subjectFilter = Configuration.getInstance().getPropertyValueAsString(MailFilterConstants.MAIL_FILTER_SUBJECT);
        String hasAttachmentsFilter = Configuration.getInstance().getPropertyValueAsString(MailFilterConstants.MAIL_FILTER_HAS_ATTACHMENT);
        Boolean hasAttachments = (StringUtils.isNotBlank(hasAttachmentsFilter) ? Configuration.getInstance().getPropertyValueAsBoolean(MailFilterConstants.MAIL_FILTER_HAS_ATTACHMENT) : false);

        SearchFilter.IsEqualTo filterFrom = new SearchFilter.IsEqualTo(EmailMessageSchema.From, senderFilter);
        SearchFilter.ContainsSubstring filterSubject = new SearchFilter.ContainsSubstring(ItemSchema.Subject, subjectFilter);
        SearchFilter.IsEqualTo filterAttachments = new SearchFilter.IsEqualTo(ItemSchema.HasAttachments, hasAttachments);

        SearchFilter.SearchFilterCollection searchFilter = new SearchFilter.SearchFilterCollection(LogicalOperator.And, filterFrom);

        if (StringUtils.isNotEmpty(subjectFilter)) {
            searchFilter.add(filterSubject);

        }

        if (hasAttachments != null) {
            searchFilter.add(filterAttachments);
        }

        return searchFilter;
    }
}
