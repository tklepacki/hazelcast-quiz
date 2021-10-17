package helpers;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigPropertiesReader {

    private static final String CONFIG_FILE_PATH = "src/test/resources/config.properties";

    private static Properties configProperties() {
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(CONFIG_FILE_PATH));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }

    public static String getCloudEndpoint() {
        String cloudEndpoint = configProperties().getProperty("cloudendpoint");
        if (cloudEndpoint == null || cloudEndpoint.isEmpty()) {
            try {
                throw new Exception("cloudendpoint property is not set in config.properties file");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return cloudEndpoint;
    }

    public static String getBearerToken() {
        String bearerToken = configProperties().getProperty("bearertoken");
        if (bearerToken == null || bearerToken.isEmpty()) {
            try {
                throw new Exception("bearertoken property is not set in config.properties file");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return bearerToken;
    }
}