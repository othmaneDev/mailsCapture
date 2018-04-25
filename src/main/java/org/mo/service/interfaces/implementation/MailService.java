package org.mo.service.interfaces.implementation;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.mo.model.Attachment;
import org.mo.model.Mail;
import org.mo.service.interfaces.exception.RepositoryServiceException;
import org.mo.service.interfaces.interfaces.IMailService;

import java.util.HashMap;
import java.util.Map;

public final class MailService implements IMailService {

    private static final String JSON_KEY_ID = "id";

    private static final String JSON_KEY_SUBJECT = "subject";

    /**
     * Map of index by sender found in the mail
     */
    private Map<String, Long> mapIndexBySender = new HashMap<>();

    /**
     * Map of JSOON by sender
     */
    private Map<String, JSONArray> mapJsonBySender = new HashMap<>();

    @Override
    public void processMail(Mail mail) throws RepositoryServiceException {

        if (mail != null) {

            // Get the current index, or create new one if it is not existing
            if (mapIndexBySender.containsKey(mail.getFrom())) {
                Long currentIndex = mapIndexBySender.get(mail.getFrom());
                mapIndexBySender.put(mail.getFrom(), ++currentIndex);
            }
            else {
                mapIndexBySender.put(mail.getFrom(), 1L);
            }

            // Adding the new element to the JSON array
            JSONArray currentJsonArray;

            if (mapJsonBySender.containsKey(mail.getFrom())) {
                currentJsonArray = mapJsonBySender.get(mail.getFrom());
            }
            else {
                currentJsonArray = new JSONArray();
                mapJsonBySender.put(mail.getFrom(), currentJsonArray);
            }

            Long indexToFiles = mapIndexBySender.get(mail.getFrom());
            JSONObject currentJsonMail = new JSONObject();

            currentJsonMail.put(JSON_KEY_SUBJECT, mail.getSubject());
            currentJsonMail.put(JSON_KEY_ID, indexToFiles);
            currentJsonArray.add(currentJsonMail);

            // Create eml file
            try {
                RepositoryService.getInstance().writeEml(mail.getFrom(), indexToFiles, mail);
            }
            catch (RepositoryServiceException e) {
                throw new RepositoryServiceException("An error occurs when creating eml file", e);
            }
            // Create text file
            try {
                RepositoryService.getInstance().writeTxtFile(mail.getFrom(), indexToFiles, mail.getSubject(), mail.getBody());
            }
            catch (RepositoryServiceException e) {
                throw new RepositoryServiceException("An error occurs when creating text file", e);
            }

            // Create attachments
            mail.getAttachments().forEach(message -> {

                try {
                    RepositoryService.getInstance().writeAttachment(mail.getFrom(),
                        indexToFiles,
                        (Attachment) message);
                }
                catch (RepositoryServiceException e) {
                    // TODO log an error using log4j extention in lambok tool
                }
            });
        }

    }

    @Override
    public void writeJsonReport() {

        mapJsonBySender.forEach((email, json) -> {

            try {
                RepositoryService.getInstance().writeJsonFile(email, json);
            }
            catch (RepositoryServiceException e) {
                // TODO log an error using log4j extention in lambok tool
            }
        });

    }
}
