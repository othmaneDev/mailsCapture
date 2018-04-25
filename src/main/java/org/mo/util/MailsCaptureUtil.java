package org.mo.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import microsoft.exchange.webservices.data.core.service.item.EmailMessage;
import microsoft.exchange.webservices.data.property.complex.AttachmentCollection;
import microsoft.exchange.webservices.data.property.complex.FileAttachment;

import com.sun.mail.util.BASE64DecoderStream;

import org.apache.commons.lang3.StringUtils;
import org.mo.access.constant.ContentTypes;
import org.mo.access.exception.MailsCaptureException;
import org.mo.model.Attachment;
import org.mo.model.Mail;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.InternetAddress;

import java.io.IOException;

/**
 * An utility class that provide tools to parse the mail message according to the specific protocol used
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MailsCaptureUtil {

    private static final Integer INTEGER_ZERO = 0;

    /**
     * Convert a message from the protocol EWS to a {@link Mail} object
     *
     * @param email The {@link EmailMessage}
     * @return The converted {@link Mail} object
     */
    public static Mail convertEwsEmailMessageToMail(EmailMessage email) throws MailsCaptureException {

        Mail<EmailMessage> result = null;

        if (email != null) {

            result = new Mail<>();
            result.setMessage(email);

            try {

                // Setting the mail sender
                result.setFrom(email.getFrom().getAddress());

                // Setting the mail subject
                result.setSubject(email.getSubject());

                // Setting the mail body
                result.setBody(email.getBody().toString());

                // Setting the mail attachments if any
                if (email.getHasAttachments()) {

                    AttachmentCollection attachmentCollection = email.getAttachments();

                    for (int i = INTEGER_ZERO; i < attachmentCollection.getCount(); i++) {

                        FileAttachment fileAttachment = (FileAttachment) attachmentCollection.getPropertyAtIndex(i);

                        result.getAttachments().add(new Attachment(fileAttachment));
                    }
                }
            }
            catch (Exception e) {
                throw new MailsCaptureException("An error has occured when converting the ews mail message to the Mail object", e);
            }
        }

        return result;
    }

    /**
     * Convert a message from one of the protocols : IMAP et POP3 to a {@link Mail} object
     *
     * @param message The message to be convered
     * @return The converted {@link Mail} object
     */
    public static Mail convertPop3ImapMessageToMail(Message message) {

        Mail<Message> result = null;

        if (message != null) {

            result = new Mail<>();
            result.setMessage(message);

            try {

                // Setting the mail sender
                if (message.getFrom() != null && message.getFrom().length > 0 && message.getFrom()[0] != null) {
                    result.setFrom(((InternetAddress) message.getFrom()[0]).getAddress());
                }

                // Setting the mail subject
                result.setSubject(message.getSubject());

                // Setting the mail body
                result.setBody(getBodyContent(message));

                // Setting attachments
                if (message.getContent() instanceof Multipart) {

                    Multipart multipart = (Multipart) message.getContent();

                    for (Integer i = INTEGER_ZERO; i < multipart.getCount(); i++) {

                        BodyPart bodyPart = multipart.getBodyPart(i);

                        // Check if there are some attachments with the mail
                        if (Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition()) &&
                                StringUtils.isNotEmpty(bodyPart.getFileName())) {
                            result.getAttachments().add(new Attachment(bodyPart));
                        }
                    }
                }
            }
            catch (MessagingException | IOException e) {
                new MailsCaptureException("An error has occured when parsing the mail contents", e);
            }
        }

        return result;
    }

    /**
     * Browse the portion of the mail provided to return the contents of the mail body recursively
     *
     * @param part The element part to browse and return its content
     * @return The message body, else return <code>null</code>.
     */
    private static String getBodyContent(Part part) throws MessagingException, IOException {

        String bodyContent = null;

        if (part.isMimeType(ContentTypes.TEXT_ALL) &&
                !(part.getContent() instanceof BASE64DecoderStream)) { // We do not handle images
            bodyContent = (String) part.getContent();
        }

        else if (part.isMimeType(ContentTypes.MULTIPART_ALL)) {

            Multipart multiPart = (Multipart) part.getContent();

            for (Integer i = 0; i < multiPart.getCount(); i++) {

                String bodyContentPart = getBodyContent(multiPart.getBodyPart(i));

                if (StringUtils.isNotEmpty(bodyContentPart)) {
                    bodyContent = bodyContentPart;
                }
            }
        }

        else if (part.isMimeType(ContentTypes.MULTIPART_ALTERNATIVE)) {

            Multipart multiPart = (Multipart) part.getContent();

            for (Integer i = 0; i < multiPart.getCount(); i++) {

                Part bodyPart = multiPart.getBodyPart(i);

                if (bodyPart.isMimeType(ContentTypes.TEXT_PLAIN)) {

                    if (StringUtils.isEmpty(bodyContent)) {
                        bodyContent = getBodyContent(bodyPart);
                    }
                }

                else if (bodyPart.isMimeType(ContentTypes.TEXT_HTML)) {

                    String bodyContentPart = getBodyContent(bodyPart);

                    if (StringUtils.isNotEmpty(bodyContentPart)) {
                        bodyContent = bodyContentPart;
                    }
                }

                else {
                    bodyContent = getBodyContent(bodyPart);
                }

            }
        }

        return bodyContent;
    }
}
