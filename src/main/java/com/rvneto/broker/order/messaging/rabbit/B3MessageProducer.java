package com.rvneto.broker.order.messaging.rabbit;

import com.rvneto.broker.order.dto.B3OrderRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class B3MessageProducer {

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.exchange}")
    private String exchange;

    @Value("${app.rabbitmq.routing-key}")
    private String routingKey;

    public void sendToB3(B3OrderRequestDTO message) {
        log.info("Sending order {} to B3 via exchange: {} | routing-key: {}",
                message.getOrderId(), exchange, routingKey);
        // fix: send through exchange with routing key instead of directly to queue
        rabbitTemplate.convertAndSend(exchange, routingKey, message);
    }
}