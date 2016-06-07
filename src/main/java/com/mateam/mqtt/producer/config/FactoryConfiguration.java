package com.mateam.mqtt.producer.config;

import com.mateam.mqtt.producer.message.MessageProducer;
import com.mateam.mqtt.producer.message.MqttMessageProducerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.ServiceLocatorFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * Created by danielbrutti on 05/05/16.
 */
@Configuration
public class FactoryConfiguration {

    @Bean
    public FactoryBean<?> serviceLocatorFactoryBean() {
        ServiceLocatorFactoryBean factoryBean = new ServiceLocatorFactoryBean();
        factoryBean.setServiceLocatorInterface(MqttMessageProducerFactory.class);
        return factoryBean;
    }

    @Bean(name = "messageProducer")
    @Scope(scopeName = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public MessageProducer mqttDevice() {
        return new MessageProducer();
    }
}
