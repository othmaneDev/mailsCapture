package org.mo.configuration.constant;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Configuration constants
 *
 */
public interface ConfigurationConstants {

    /**
     * Application parameters
     */
    String MAIL_DEBUG_ENABLE = "mail.debug.enable";
    String MAIL_OUTPUT_PATH = "mail.output.path";
    String MAIL_OUTPUT_PATH_DEFAULT = "output";

    /**
     * Protocole parameters
     */
    String MAIL_STORE_PROTOCOL = "mail.store.protocol";
    String MAIL_STORE_PROTOCOL_IMAP = "imap";
    String MAIL_STORE_PROTOCOL_POP3 = "pop3";
    String MAIL_STORE_PROTOCOL_MICROSOFT_EWS = "ews";
    String MAIL_STORE_PROTOCOL_DEFAULT_VALUE = MAIL_STORE_PROTOCOL_IMAP;
    List<String> HANDLED_PROTOTOLS = Collections.unmodifiableList(Arrays.asList(MAIL_STORE_PROTOCOL_IMAP,
        MAIL_STORE_PROTOCOL_POP3,
        MAIL_STORE_PROTOCOL_MICROSOFT_EWS));

    /**
     * Mail server configuration properties
     */
    String MAIL_HOST = "mail.host";
    String MAIL_USER = "mail.user";
    String MAIL_PASSWORD = "mail.password";
    String MAIL_ROOT_DIRECTORY_NAME = "INBOX";
    String HTTPS_PREFIX = "https://";
    String MAIL_IMAP_STARTTLS_ENABLE = "mail.imap.starttls.enable";
    String MAIL_IMAP_SSL_ENABLE = "mail.imap.ssl.enable";

    /**
     * EWS protocol specific properties
     */
    String MAIL_EWS_DOMAIN = "mail.ews.domain";
    String MAIL_EWS_EMAIL = "mail.ews.email";

}