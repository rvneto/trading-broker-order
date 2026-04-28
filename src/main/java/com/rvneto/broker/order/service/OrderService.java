package com.rvneto.broker.order.service;

import com.rvneto.broker.order.client.WalletClient;
import com.rvneto.broker.order.domain.Order;
import com.rvneto.broker.order.domain.OrderSide;
import com.rvneto.broker.order.domain.OrderStatus;
import com.rvneto.broker.order.dto.B3OrderRequestDTO;
import com.rvneto.broker.order.dto.OrderRequestDTO;
import com.rvneto.broker.order.messaging.kafka.OrderEventProducer;
import com.rvneto.broker.order.messaging.rabbit.B3MessageProducer;
import com.rvneto.broker.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final WalletClient walletClient;
    private final OrderEventProducer orderEventProducer;
    private final B3MessageProducer b3MessageProducer;

    @Transactional
    public Order placeOrder(OrderRequestDTO request) {
        log.info("Processing new {} order for user: {}", request.getSide(), request.getUserId());

        // 1. Validate balance for BUY orders via Wallet API (Feign)
        if (request.getSide() == OrderSide.BUY) {
            validateBalance(request);
        }

        // 2. Persist order with PENDING status
        Order order = Order.builder()
                .userId(request.getUserId())
                .ticker(request.getTicker().toUpperCase())
                .quantity(request.getQuantity())
                .price(request.getPrice())
                .side(request.getSide())
                .status(OrderStatus.PENDING)
                .build();

        order = orderRepository.save(order);
        log.info("Order {} persisted with status PENDING", order.getId());

        // 3. Send to B3 via RabbitMQ
        b3MessageProducer.sendToB3(mapToB3Message(order));

        // 4. Publish PENDING event to Kafka for wallet and other consumers
        orderEventProducer.sendOrderEvent(order.mapToEvent());

        return order;
    }

    private void validateBalance(OrderRequestDTO request) {
        BigDecimal totalOrderValue = request.getPrice().multiply(request.getQuantity());
        var walletSummary = walletClient.getWalletSummary(request.getUserId());

        if (walletSummary.getAvailableBalance().compareTo(totalOrderValue) < 0) {
            throw new RuntimeException("Insufficient balance. Available: "
                    + walletSummary.getAvailableBalance()
                    + " | Required: " + totalOrderValue);
        }
        log.info("Balance validated for user {}. Available: {} | Required: {}",
                request.getUserId(), walletSummary.getAvailableBalance(), totalOrderValue);
    }

    private B3OrderRequestDTO mapToB3Message(Order order) {
        return B3OrderRequestDTO.builder()
                .orderId(order.getId().toString())
                .ticker(order.getTicker())
                .quantity(order.getQuantity())
                .price(order.getPrice())
                .side(order.getSide().toString())
                .build();
    }

    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id);
    }

    public List<Order> findByUserId(String userId) {
        return orderRepository.findByUserId(userId);
    }
}
