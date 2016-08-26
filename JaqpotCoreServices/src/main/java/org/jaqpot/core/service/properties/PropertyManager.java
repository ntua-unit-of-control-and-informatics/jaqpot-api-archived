package org.jaqpot.core.service.properties;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.ResourceBundle;

/**
 * Created by root on 26/8/2016.
 */
@Dependent
public class PropertyManager {

    public enum PropertyType {
        JAQPOT_ADMINISTRATORS("jaqpot.administrators"),
        JAQPOT_AA("jaqpot.aa"),
        SERVER_BASE_PATH("ServerBasePath"),
        ALGORITHMS_BASE_PATH("AlgorithmsBasePath"),
        IMAGE_BASE_PATH("ImageBasePath"),
        VALIDATION_BASE_PATH("ValidationBasePath"),
        INTERLAB_BASE_PATH("InterlabBasePath"),
        JAQPOT_MAIL_SEND("jaqpot.mail.dosend"),
        JAQPOT_MAIL_MANDRILL_API_KEY("jaqpot.mail.mandrillApiKey"),
        JAQPOT_MAIL_FROM_MAIL("jaqpot.mail.fromMail"),
        JAQPOT_MAIL_FROM_NAME("jaqpot.mail.fromName"),
        JAQPOT_MAIL_RECIPIENTS("jaqpot.mail.recipients"),
        JAQPOT_MAIL_ALLOWORIGIN("jaqpot.cors.alloworigin"),
        DEFAULT_DOA ("default.doa"),
        DEFAULT_SCALING("default.scaling"),
        DEFAULT_STANDARIZATION("default.standarization");

        private final String value;

        PropertyType(String value) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }
    }

    private ResourceBundle configResourceBundle;

    @Inject
    public PropertyManager() { configResourceBundle = ResourceBundle.getBundle("config");
    }

    public String getProperty(PropertyType propertyType)
    {
        String property = System.getenv(propertyType.getValue());
        if (property == null || property.isEmpty())
        {
            property = configResourceBundle.getString(propertyType.getValue());
        }
        return  property;
    }
}
