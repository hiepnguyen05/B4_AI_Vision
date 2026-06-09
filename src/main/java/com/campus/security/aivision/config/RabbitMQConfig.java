package com.campus.security.aivision.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${ai.vision.rabbitmq.alerts-exchange:vision.alerts.exchange}")
    private String alertsExchange;

    @Value("${ai.vision.rabbitmq.alerts-queue:vision.alerts.queue}")
    private String alertsQueue;

    @Value("${ai.vision.rabbitmq.alerts-routing-key:vision.alerts.key}")
    private String alertsRoutingKey;

    @Value("${ai.vision.rabbitmq.events-exchange:vision.events.exchange}")
    private String eventsExchange;

    @Value("${ai.vision.rabbitmq.events-queue:vision.events.queue}")
    private String eventsQueue;

    @Value("${ai.vision.rabbitmq.events-routing-key:vision.events.key}")
    private String eventsRoutingKey;

    @Bean
    public TopicExchange alertsExchange() {
        return new TopicExchange(alertsExchange);
    }

    @Bean
    public Queue alertsQueue() {
        return new Queue(alertsQueue, true);
    }

    @Bean
    public Binding alertsBinding() {
        return BindingBuilder.bind(alertsQueue())
                .to(alertsExchange())
                .with("vision.alerts.#");
    }

    @Bean
    public TopicExchange eventsExchange() {
        return new TopicExchange(eventsExchange);
    }

    @Bean
    public Queue eventsQueue() {
        return new Queue(eventsQueue, true);
    }

    @Bean
    public Binding eventsBinding() {
        return BindingBuilder.bind(eventsQueue())
                .to(eventsExchange())
                .with("vision.events.#");
    }

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
