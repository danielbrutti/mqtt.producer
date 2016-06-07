package com.mateam.mqtt.producer;

import com.mateam.mqtt.connector.data.MqttPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Created by danielbrutti on 07/06/16.
 */
@Component
public class MqttMessageProcessor {

    Logger logger = LoggerFactory.getLogger(MqttMessageProcessor.class);

    public void process(MqttPacket packet){
        String topic = packet.getTopic();
        logger.info("Packet arrived from topic: {}", topic);

        if(topic.startsWith("lastwill/")){
            logger.info("LAST WILL received from {}", topic.split("/")[1]);
        }
    }

}
