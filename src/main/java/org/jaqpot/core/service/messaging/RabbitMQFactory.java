package org.jaqpot.core.service.messaging;

import com.rabbitmq.client.ConnectionFactory;
import org.jaqpot.core.properties.PropertyManager;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

@Named
@ApplicationScoped
public class RabbitMQFactory {

    private static final Logger LOG = Logger.getLogger(org.jaqpot.core.service.messaging.RabbitMQFactory.class.getName());

    @Inject
    PropertyManager propertyManager;

    private RabbitMQ rabbitMQClient;

    RabbitMQFactory(){}

    @PostConstruct
    public void init() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(propertyManager.getPropertyOrDefault(PropertyManager.PropertyType.JAQPOT_RABBITMQ_HOST));
        factory.setUsername(propertyManager.getPropertyOrDefault(PropertyManager.PropertyType.JAQPOT_RABBITMQ_USERNAME));
        factory.setPassword(propertyManager.getPropertyOrDefault(PropertyManager.PropertyType.JAQPOT_RABBITMQ_PASSWORD));
        this.rabbitMQClient = new RabbitMQImpl(factory);
    }


    @Produces
    public RabbitMQ getClient() {
        return rabbitMQClient;
    }

}
