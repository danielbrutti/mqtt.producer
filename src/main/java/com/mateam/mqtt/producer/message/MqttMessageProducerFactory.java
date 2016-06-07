package com.mateam.mqtt.producer.message;

public interface MqttMessageProducerFactory {

	MessageProducer build(String beanName);
}
