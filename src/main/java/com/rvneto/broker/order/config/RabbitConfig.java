package com.rvneto.broker.order.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Value("${app.rabbitmq.exchange}")
    private String exchangeName;

    @Value("${app.rabbitmq.routing-key}")
    private String routingKey;

    @Value("${app.rabbitmq.queue-in}")
    private String queueIn;

    @Value("${app.rabbitmq.queue-out}")
    private String queueOut;

    @Bean
    public DirectExchange brokerExchange() {
        return new DirectExchange(exchangeName);
    }

    @Bean
    public Queue orderOutQueue() {
        return QueueBuilder.durable(queueIn)
                .withArgument("x-dead-letter-exchange", "dlx.broker.orders")
                .withArgument("x-dead-letter-routing-key", "dlq-broker-to-b3")
                .build();
    }

    @Bean
    public Queue orderInQueue() {
        return QueueBuilder.durable(queueOut).build();
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable("dlq-broker-to-b3").build();
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange("dlx.broker.orders");
    }

    @Bean
    public Binding orderOutBinding() {
        return BindingBuilder.bind(orderOutQueue())
                .to(brokerExchange())
                .with(routingKey);
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with("dlq-broker-to-b3");
    }

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}