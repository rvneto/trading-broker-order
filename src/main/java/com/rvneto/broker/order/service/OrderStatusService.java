package com.rvneto.broker.order.service;

import com.rvneto.broker.order.domain.Order;
import com.rvneto.broker.order.domain.OrderStatus;
import com.rvneto.broker.order.dto.B3OrderResponseDTO;
import com.rvneto.broker.order.messaging.kafka.OrderEventProducer;
import com.rvneto.broker.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderStatusService {

    private final OrderRepository orderRepository;
    private final OrderEventProducer orderEventProducer;

    @Transactional
    public void updateOrderStatus(B3OrderResponseDTO response) {
        Order order = orderRepository.findById(Long.valueOf(response.getOrderId()))
                .orElseThrow(() -> new RuntimeException("Order not found: " + response.getOrderId()));

        log.info("Processing B3 feedback: Order {} | Status: {}", order.getId(), response.getStatus());

        // Update order with final status and executed price from B3
        order.setStatus(OrderStatus.valueOf(response.getStatus()));
        order.setExecutedPrice(response.getExecutedPrice());
        // fix: removed manual setUpdatedAt() — @UpdateTimestamp handles this automatically

        orderRepository.save(order);
        log.info("Order {} successfully updated to {}", order.getId(), response.getStatus());

        // Notify ecosystem with final status event
        orderEventProducer.sendOrderEvent(order.mapToEvent());
    }
}
