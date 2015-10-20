package org.coble.core.camel.test.utils;

import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertyLoader {

    private static final Logger logger = LoggerFactory.getLogger(PropertyLoader.class);

    /**
     * Load a property from the config.properties file
     *
     * @param propertyName
     * @return the value of the property
     */
    private static String getProperty(String environment, String propertyName) {
        Properties properties = new Properties();

        if (!properties.containsKey(propertyName)) {
            try {
                properties.load(PropertyLoader.class.getResourceAsStream("/properties/"+environment+".properties"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String result = properties.getProperty(propertyName);

        if (null == result) {
            logger.info("WARNING: property [" + propertyName + "] was not found, returning NULL");
        } else {
            logger.info("Found property: " + propertyName + " value is: " + result);
        }

        return result;
    }

    /**
     * This method will fetch an environment property from the properties file
     * this will automatically load the correct property using the environment
     * specified at runtime e.g. dev12
     *
     * @param propertyName
     * @return the value of the property
     */

    public static String getEnvironmentProperty(String propertyName) {
        return getProperty(getEnvironment(), propertyName);
    }

    /**
     * This method can be used to load a property from the property file
     * should be used for loading properties in the Misc Properties section
     *
     * @param propertyName
     * @return the value of the property
     */
    public static String getMiscProperty(String propertyName) {
        return getProperty("config", propertyName);
    }

    /**
     * determines the environment that is specified when running the tests
     * this is taken from the environment property in the pom.xml
     * you can specify the environment when running using maven with the following argument:
     * <p/>
     * -Denvironment=local
     * <p/>
     * for example to run tests against dev12 use:
     * -Denvironment=dev12
     *
     * @return the environment could be (local, dev15, dev12 or dev10)
     */
    public static String getEnvironment() {
        String environment = System.getProperty("environment");
        if (null == environment || environment.isEmpty()) {
            environment = getProperty("config", "defaultEnvironment");
            logger.info("Using default environment of [" + environment + "]");
        }
        logger.info("Using environment: " + environment);
        return environment != null ? environment.toLowerCase() : null;
    }
}
