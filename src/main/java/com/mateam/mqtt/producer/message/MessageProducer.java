package com.mateam.mqtt.producer.message;

import com.mateam.mqtt.connector.connection.MqttConnector;
import com.mateam.mqtt.connector.data.MqttPacket;
import com.mateam.mqtt.connector.exceptions.CantSendException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.nio.charset.Charset;

/**
 * Created by danielbrutti on 07/06/16.
 */
public class MessageProducer implements Runnable {

    Logger logger = LoggerFactory.getLogger(MessageProducer.class);

    @Autowired
    private MqttConnector connector;

    @Value("${mqtt.producer.topic:messages/}")
    private String topic;

    private static int count = 0;

    @Override
    public void run() {

        if (connector.isConnected()) {

            String message = "Message #" + (++count);

            byte[] payload = message.getBytes(Charset.forName("UTF-8"));

            MqttMessage mqttMessage = new MqttMessage();
            mqttMessage.setQos(1);
            mqttMessage.setRetained(false);
            mqttMessage.setPayload(payload);

            MqttPacket packet = new MqttPacket(topic, mqttMessage);

            try {
                connector.send(packet);
            } catch (CantSendException e) {
                logger.error("Error sending {}", e.getMessage(), e);
            }
        }
    }
}
