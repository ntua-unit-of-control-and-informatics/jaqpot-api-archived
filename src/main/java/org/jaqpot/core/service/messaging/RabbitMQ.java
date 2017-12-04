package org.jaqpot.core.service.messaging;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public interface RabbitMQ {

    void sendMessage(String topic, String message) throws IOException, TimeoutException;
}
