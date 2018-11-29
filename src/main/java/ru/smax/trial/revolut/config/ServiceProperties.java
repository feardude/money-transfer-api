package ru.smax.trial.revolut.config;

import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toMap;

class ServiceProperties {
    private static final String PROPERTIES_FILE_PATH = "service.properties";
    private static final Map<String, String> PROPERTIES = readPropertiesFile();

    private ServiceProperties() {
        throw new InstantiationError("Not for instantiation!");
    }

    private static Map<String, String> readPropertiesFile() {
        final InputStream propertiesInputStream = ServiceProperties.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE_PATH);

        if (isNull(propertiesInputStream)) {
            throw new InstantiationError(format("File [%s] does not exist", PROPERTIES_FILE_PATH));
        }

        return asMap(propertiesInputStream);
    }

    private static Map<String, String> asMap(InputStream propsFileStream) {
        try {
            final Properties properties = new Properties();
            properties.load(propsFileStream);
            return properties.stringPropertyNames().stream()
                    .collect(toMap(
                            Function.identity(),
                            properties::getProperty
                    ));
        } catch (IOException e) {
            throw new IOError(e);
        }
    }

    static String getDbUrl() {
        return PROPERTIES.get("db.url");
    }

    static String getDbUsername() {
        return PROPERTIES.get("db.username");
    }

    static String getDbPassword() {
        return PROPERTIES.get("db.password");
    }
}
