package org.mo.filter;

import org.mo.configuration.exception.ConfigurationException;
import org.mo.filter.exception.MailFilterException;
import org.mo.model.Mail;
import org.mo.util.MailsCaptureUtil;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Utility class for filtering POP3 and IMAP messages
 *
 */
public final class Pop3ImapMailFilter {

    private static final String QUOTE = "'";

    private static final String NON_WORDING = "none";

    /**
     * Filtering the messages present in the folder
     *
     * @param folder The folder containing the mails to be filtered
     * @return a list of {@link Mail}
     */
    public static List<Mail> filterMessages(Folder folder) throws MailFilterException, ConfigurationException {

        List<Mail> foundMessages = new ArrayList<>();

        if (folder != null) {

            Message[] messages;

            try {
                // Launch search on the folder using filters (sender, subject and hasAttachments filters)
                messages = folder.search(new Pop3ImapMailMatcher());
            }
            catch (MessagingException e) {
                throw new MailFilterException(e);
            }

            if (messages != null && messages.length > 0) {
                // Convert the result from Pop3Imap message to mail and return it
                Arrays.stream(messages)
                    .filter(message -> message != null)
                    .forEach(message -> foundMessages
                        .add(MailsCaptureUtil.convertPop3ImapMessageToMail(message)));
            }
        }

        return foundMessages;
    }
}