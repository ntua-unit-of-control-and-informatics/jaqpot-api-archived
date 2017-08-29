package org.jaqpot.core.service.messaging;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class RabbitMQImpl implements RabbitMQ{

    private static final String EXCHANGE_NAME = "amq.topic";

    ConnectionFactory factory;
    public RabbitMQImpl(ConnectionFactory factory) {
        this.factory=factory;
    }

    @Override
    public void sendMessage(String topic, String message) throws IOException, TimeoutException {
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(EXCHANGE_NAME, "topic", true);

        String username = topic;
        String mes = message;

        channel.basicPublish(EXCHANGE_NAME, username, null, message.getBytes());
        System.out.println(" [x] Sent '" + username + "':'" + mes + "'");

        channel.close();
        connection.close();
    }
}
