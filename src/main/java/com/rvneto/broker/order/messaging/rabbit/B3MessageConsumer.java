package com.rvneto.broker.order.messaging.rabbit;

import com.rvneto.broker.order.dto.B3OrderResponseDTO;
import com.rvneto.broker.order.service.OrderStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class B3MessageConsumer {

    private final OrderStatusService orderStatusService;

    @RabbitListener(queues = "${app.rabbitmq.queue-out}")
    public void receiveFeedback(B3OrderResponseDTO response) {
        log.info("Feedback received from B3 for order: {} | Status: {}",
                response.getOrderId(), response.getStatus());
        try {
            orderStatusService.updateOrderStatus(response);
        } catch (Exception e) {
            log.error("Failed to update status for order {}: {}",
                    response.getOrderId(), e.getMessage(), e);
            // Rethrow so RabbitMQ can apply retry/DLQ policy
            throw e;
        }
    }
}