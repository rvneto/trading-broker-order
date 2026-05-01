package com.rvneto.broker.order.service;

import com.rvneto.broker.order.client.AssetClient;
import com.rvneto.broker.order.client.WalletClient;
import com.rvneto.broker.order.domain.Order;
import com.rvneto.broker.order.domain.OrderSide;
import com.rvneto.broker.order.domain.OrderStatus;
import com.rvneto.broker.order.dto.B3OrderRequestDTO;
import com.rvneto.broker.order.dto.OrderRequestDTO;
import com.rvneto.broker.order.messaging.kafka.OrderEventProducer;
import com.rvneto.broker.order.messaging.rabbit.B3MessageProducer;
import com.rvneto.broker.order.repository.OrderRepository;
import feign.FeignException;
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
    private final AssetClient assetClient;
    private final OrderEventProducer orderEventProducer;
    private final B3MessageProducer b3MessageProducer;

    @Transactional
    public Order placeOrder(OrderRequestDTO request) {
        log.info("Processing new {} order for user: {}", request.getSide(), request.getUserId());

        // 1. Validate ticker exists and is ACTIVE in asset catalog
        validateTicker(request.getTicker());

        // 2. Validate balance for BUY orders via Wallet API (Feign)
        if (request.getSide() == OrderSide.BUY) {
            validateBalance(request);
        }

        // 3. Persist order with PENDING status
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

        // 4. Send to B3 via RabbitMQ
        b3MessageProducer.sendToB3(mapToB3Message(order));

        // 5. Publish PENDING event to Kafka for wallet and other consumers
        orderEventProducer.sendOrderEvent(order.mapToEvent());

        return order;
    }

    private void validateTicker(String ticker) {
        try {
            var asset = assetClient.getByTicker(ticker.toUpperCase());
            if (!"ACTIVE".equalsIgnoreCase(asset.status())) {
                throw new RuntimeException("Asset is not available for trading: " + ticker);
            }
            log.info("Ticker {} validated — status: {}", ticker, asset.status());
        } catch (FeignException.NotFound e) {
            throw new RuntimeException("Asset not found or inactive: " + ticker);
        } catch (FeignException e) {
            log.error("Asset service unavailable during ticker validation: {}", e.getMessage());
            throw new RuntimeException("Asset service unavailable. Please try again later.");
        }
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
