package org.jaqpot.core.service.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jaqpot.ambitclient.AmbitClient;
import org.jaqpot.core.properties.PropertyManager;
import org.jaqpot.core.service.annotations.UnSecure;
import org.jaqpot.core.service.client.ambit.JacksonSerializer;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

/**
 * Created by Angelos Valsamis on 12/12/2016.
 */
@ApplicationScoped
public class AmbitClientFactory extends org.jaqpot.ambitclient.AmbitClientFactory {

    @Inject
    PropertyManager propertyManager;

    private AmbitClient ambitClient;

    @Inject
    public AmbitClientFactory() {

    }

    @PostConstruct
    public void init(){
        this.ambitClient = createNewClient(
                propertyManager.getPropertyOrDefault(PropertyManager.PropertyType.JAQPOT_AMBIT),
                new JacksonSerializer(new ObjectMapper()));
    }


    @Produces
    @UnSecure
    public AmbitClient getRestClient() {
        return ambitClient;
    }
}
