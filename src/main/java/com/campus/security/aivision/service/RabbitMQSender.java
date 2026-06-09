package com.campus.security.aivision.service;

import com.campus.security.aivision.dto.VisionAlertEvent;
import com.campus.security.aivision.dto.VisionNormalEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RabbitMQSender {

    private final RabbitTemplate rabbitTemplate;

    @Value("${ai.vision.rabbitmq.alerts-exchange:vision.alerts.exchange}")
    private String alertsExchange;

    @Value("${ai.vision.rabbitmq.alerts-routing-key:vision.alerts.key}")
    private String alertsRoutingKey;

    @Value("${ai.vision.rabbitmq.events-exchange:vision.events.exchange}")
    private String eventsExchange;

    @Value("${ai.vision.rabbitmq.events-routing-key:vision.events.key}")
    private String eventsRoutingKey;

    /**
     * Send emergency/alert event to RabbitMQ
     */
    public void sendAlertEvent(VisionAlertEvent event) {
        try {
            log.info("Publishing alert event to RabbitMQ for detection ID: {}", event.getDetectionId());
            rabbitTemplate.convertAndSend(alertsExchange, alertsRoutingKey, event);
            log.info("Alert event published successfully");
        } catch (Exception e) {
            log.error("Failed to publish alert event to RabbitMQ: {}", e.getMessage(), e);
        }
    }

    /**
     * Send normal detection event to RabbitMQ
     */
    public void sendNormalEvent(VisionNormalEvent event) {
        try {
            log.info("Publishing normal detection event to RabbitMQ for detection ID: {}", event.getDetectionId());
            rabbitTemplate.convertAndSend(eventsExchange, eventsRoutingKey, event);
            log.info("Normal detection event published successfully");
        } catch (Exception e) {
            log.error("Failed to publish normal detection event to RabbitMQ: {}", e.getMessage(), e);
        }
    }
}
