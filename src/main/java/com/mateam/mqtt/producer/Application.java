package com.mateam.mqtt.producer;

import com.google.common.eventbus.Subscribe;
import com.google.gson.Gson;
import com.mateam.mqtt.connector.config.MqttConfiguration;
import com.mateam.mqtt.connector.connection.MqttConnector;
import com.mateam.mqtt.connector.events.MqttPacketAvailableEvent;
import com.mateam.mqtt.connector.exceptions.ConnectionException;
import com.mateam.mqtt.connector.util.LinuxUtils;
import com.mateam.mqtt.producer.message.MessageProducer;
import com.mateam.mqtt.producer.message.MqttMessageProducerFactory;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

@SpringBootApplication
@EnableScheduling
@EnableAsync
@ContextConfiguration(classes = {MqttConfiguration.class}, loader = AnnotationConfigContextLoader.class)
@ComponentScan(basePackages = {"com.mateam.mqtt"})
@EnableAutoConfiguration
public class Application {

    Logger logger = LoggerFactory.getLogger(Application.class);

    @Autowired
    private TaskScheduler taskScheduler;

    @Autowired
    private MqttConnector connector;

    @Autowired
    private MqttMessageProcessor mqttMessageProcessor;

    @Autowired
    private MqttMessageProducerFactory mqttMessageProducerFactory;

    @Value("${mqtt.producer.execution.delay:5}")
    private int executionDelay;

    @Value("${mqtt.broker.url:tcp://localhost:1883}")
    private String mqttBrokerURL;

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Application.class);
    }

    @Bean
    public CommandLineRunner run() {
        return new CommandLineRunner() {
            @Override
            public void run(String... args) throws Exception {

                if (connector.isConnected() == false) {
                    logger.info("Connecting to MQTT");
                    openConnection();
                }

                MessageProducer messageProducer = mqttMessageProducerFactory.build("messageProducer");

                taskScheduler.scheduleWithFixedDelay(messageProducer, executionDelay);

                logger.info("Producer has been scheduled");

            }
        };
    }

    private void openConnection() {
        logger.info("Open MQTT Connection");

        String macAddress;
        try {
            macAddress = LinuxUtils.getHostMacAddress() + "-PRODUCER";
        } catch (Exception e) {
            macAddress = UUID.randomUUID().toString();
        }

        connector.initiliaze(mqttBrokerURL, null, null, "lastwill/"+macAddress, macAddress, false, "/tmp");

        try {
            connector.connect();

            connector.subscribe("lastwill/#", 1);

            connector.registerListener(this);

            logger.info("Connection opened");
        } catch (ConnectionException e1) {
            logger.error("Error when opening MQTT connection", e1);
        }
    }

    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(1000);
        return taskScheduler;
    }


    @Subscribe
    public void onPacketArrive(MqttPacketAvailableEvent event){
        mqttMessageProcessor.process(event.getMqttPacket());
    }

}
