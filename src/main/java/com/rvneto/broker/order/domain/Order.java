package com.rvneto.broker.order.domain;

import com.rvneto.broker.order.dto.OrderEventDTO;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(nullable = false)
    private String ticker;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal quantity;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal price;

    // fix: nullable = true — executedPrice is null while order is PENDING
    // it is only set when B3 returns FILLED or REJECTED feedback
    @Column(precision = 19, scale = 4)
    private BigDecimal executedPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderSide side;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public OrderEventDTO mapToEvent() {
        return OrderEventDTO.builder()
                .orderId(this.getId())
                .userId(this.getUserId())
                .ticker(this.getTicker())
                .quantity(this.getQuantity())
                .price(this.getPrice())
                .executedPrice(this.getExecutedPrice())
                .side(this.getSide().name())
                .status(this.getStatus().name())
                .eventTimestamp(LocalDateTime.now())
                .build();
    }
}
