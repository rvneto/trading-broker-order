package com.rvneto.broker.order.messaging.kafka;

import com.rvneto.broker.order.dto.OrderEventDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderEventProducer {

    private final KafkaTemplate<String, OrderEventDTO> kafkaTemplate;

    @Value("${app.kafka.producer.topic-order-events}")
    private String topic;

    public void sendOrderEvent(OrderEventDTO event) {
        log.info("Publicando evento de ordem no tópico {} com ID: {}", topic, event.getOrderId());
        kafkaTemplate.send(topic, event.getUserId(), event);
    }

}