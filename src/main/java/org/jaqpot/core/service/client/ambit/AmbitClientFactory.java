package org.jaqpot.core.service.client.ambit;

/**
 * Created by Angelos Valsamis on 20/12/2016.
 */
import org.jaqpot.core.annotations.Jackson;
import org.jaqpot.core.properties.PropertyManager;
import org.jaqpot.core.service.annotations.UnSecure;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;


@Named
@ApplicationScoped
public class AmbitClientFactory {

    private static final Logger LOG = Logger.getLogger(AmbitClientFactory.class.getName());

    private Ambit ambit;

    @Inject
    @Jackson
    JacksonSerializer serializer;

    @Inject
    PropertyManager propertyManager;

    @Inject
    public AmbitClientFactory() {

    }

    @PostConstruct
    public void init(){
        this.ambit = new AmbitClientImpl(propertyManager.getPropertyOrDefault(PropertyManager.PropertyType.JAQPOT_AMBIT),serializer);
    }


    @Produces
    public Ambit getRestClient() {
        return ambit;
    }

    @PreDestroy
    public void destroy() {
        try {
            this.ambit.close();
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "Could not successfully close AmbitClient", ex);
        }
    }

}
