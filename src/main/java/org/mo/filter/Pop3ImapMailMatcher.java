package org.mo.filter;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.mo.access.exception.MailsCaptureException;
import org.mo.configuration.Configuration;
import org.mo.configuration.exception.ConfigurationException;
import org.mo.filter.constant.MailFilterConstants;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.InternetAddress;
import javax.mail.search.SearchTerm;

import java.io.IOException;

/**
 * Matcher that apply filters to the mail message
 */
public final class Pop3ImapMailMatcher extends SearchTerm {

    @Override
    public boolean match(Message message) {

        Boolean isValidMessage = Boolean.FALSE;

        if (message != null) {

            String sender = null;
            String subjectFilter = null;
            String hasAttachmentsFilter = null;

            try {
                sender = Configuration.getInstance().getPropertyValueAsString(MailFilterConstants.MAIL_FILTER_FROM);
                subjectFilter = Configuration.getInstance().getPropertyValueAsString(MailFilterConstants.MAIL_FILTER_SUBJECT);
                hasAttachmentsFilter = Configuration.getInstance().getPropertyValueAsString(MailFilterConstants.MAIL_FILTER_SUBJECT);
            }
            catch (ConfigurationException e) {
                // TODO : log an error using self4j extention in lambok tool;
            }

            try {

                // Verifying the sender filter
                if (StringUtils.isNotEmpty(sender) && message.getFrom() != null && message.getFrom()[0] != null &&
                        sender.equals(((InternetAddress) message.getFrom()[0]).getAddress())) {
                    isValidMessage = Boolean.TRUE;
                }

                // If the sender filter is verified, and other filters exist, processed the verification from them
                if (isValidMessage && (StringUtils.isNotEmpty(subjectFilter) || StringUtils.isNotEmpty(hasAttachmentsFilter))) {

                    // Verifying the mail subject filter
                    if (StringUtils.isNotBlank(subjectFilter) && !message.getSubject().contains(subjectFilter)) {
                        isValidMessage = Boolean.FALSE;
                    }

                    // Verifying the attachments filter
                    if (isValidMessage && StringUtils.isNotEmpty(hasAttachmentsFilter)) {

                        try {

                            Boolean hasAttachments = BooleanUtils.toBoolean(hasAttachmentsFilter,
                                Boolean.TRUE.toString(),
                                Boolean.FALSE.toString());

                            Boolean attachmentsFound = Boolean.FALSE;

                            // Get the attachments
                            if (message.getContent() instanceof Multipart) {

                                Multipart multipart = (Multipart) message.getContent();

                                Integer i = 0;

                                while (Boolean.FALSE.equals(attachmentsFound) && i < multipart.getCount()) {

                                    BodyPart bodyPart = multipart.getBodyPart(i);

                                    if (Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition()) &&
                                            StringUtils.isNotEmpty(bodyPart.getFileName())) {
                                        attachmentsFound = Boolean.TRUE;
                                    }
                                    else {
                                        i++;
                                    }
                                }

                                isValidMessage = attachmentsFound.equals(hasAttachments);
                            }
                        }
                        catch (IllegalArgumentException e) {
                            new MailsCaptureException(String.format("Attachments filtering failed " +
                                    "current value : '%s' - expected value : '%s' or '%s'", hasAttachmentsFilter, Boolean.TRUE, Boolean.FALSE));
                        }
                    }
                }
            }
            catch (MessagingException | IOException e) {
                isValidMessage = Boolean.FALSE;
            }
        }

        return isValidMessage;
    }
}