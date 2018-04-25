package org.mo.configuration;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.mo.configuration.constant.ConfigurationConstants;
import org.mo.configuration.exception.ConfigurationException;
import org.mo.filter.constant.MailFilterConstants;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Configuration class for loading the properties in the memory
 */
public class Configuration {

    private static final String CONFIGURATION_FILENAME = "application.properties";
    public static final String THE_SENDER_FILTER_ERROR = "The sender filter is mandatory";
    public static final String LOADING_PROPERTIES_ERROR = "An error occurs when loading the configuration properties file";

    private static Configuration CONFIGURATION_INSTANCE;

    /**
     * Synchronized access point for the unique instance of the Configuration class
     */
    public static synchronized Configuration getInstance() throws ConfigurationException {
        if (CONFIGURATION_INSTANCE == null) {
            CONFIGURATION_INSTANCE = new Configuration();
        }
        return CONFIGURATION_INSTANCE;
    }

    /**
     * The configuration properties
     */
    private static Properties properties;

    private Configuration() throws ConfigurationException {
        loadProperties();
    }

    /**
     * Loading the application.properties in the memory
     */
    private void loadProperties() throws ConfigurationException {

        this.properties = new Properties();
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream configFileStream = classLoader.getResourceAsStream(CONFIGURATION_FILENAME);

        try {
            properties.load(configFileStream);
        }
        catch (IOException e) {
            throw new ConfigurationException(LOADING_PROPERTIES_ERROR);
        }
    }

    /**
     * Checking application configuration
     */
    public void checkConfiguration() throws ConfigurationException {

        // Check that the sender filter is here
        if (StringUtils.isEmpty(getPropertyValueAsString(MailFilterConstants.MAIL_FILTER_FROM))) {
            throw new ConfigurationException(THE_SENDER_FILTER_ERROR);
        }
    }

    /**
     * Get the protocol which is has been configured in the application.properties file
     * by default, the POP3 protocol is used in case the protocol is absent or not configured yet
     *
     * @return The protocol used as String
     */
    public String getProtocol() {

        String protocolToBeUsed = ConfigurationConstants.MAIL_STORE_PROTOCOL_DEFAULT_VALUE;

        String protocolValue = getPropertyValueAsString(ConfigurationConstants.MAIL_STORE_PROTOCOL);

        // Check that the protocol is within the list of handled protocols
        if (StringUtils.isNotEmpty(protocolValue) && ConfigurationConstants.HANDLED_PROTOTOLS.contains(protocolValue)) {
            protocolToBeUsed = protocolValue;
        }

        return protocolToBeUsed;
    }

    /**
     * Returns the directory path where the application will store the mails.
     *
     * @return The output directory path as String
     */
    public String getOutputDirPath() {

        String outputDirectoryValue = ConfigurationConstants.MAIL_OUTPUT_PATH_DEFAULT;
        String configurationValue = getPropertyValueAsString(ConfigurationConstants.MAIL_OUTPUT_PATH);

        if (StringUtils.isNotEmpty(configurationValue)) {
            outputDirectoryValue = configurationValue;
        }

        return outputDirectoryValue;
    }

    /**
     * Get the property value as String from a key if exist, null otherwise
     * 
     * @param key The property key
     * @return The property vale if exist for the given key, null otherwise
     */
    public String getPropertyValueAsString(String key) {

        return this.properties != null ? this.properties.getProperty(key) : null;
    }

    /**
     * Get the property value as Boolean from a key if exist, false otherwise
     *
     * @param key The property key
     * @return The property vale if exist for the given key, false otherwise
     */
    public Boolean getPropertyValueAsBoolean(String key) {

        return this.properties != null ? BooleanUtils.toBoolean(this.properties.getProperty(key)) : false;
    }

}
