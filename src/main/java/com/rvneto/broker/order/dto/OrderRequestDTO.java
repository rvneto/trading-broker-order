package com.rvneto.broker.order.dto;

import com.rvneto.broker.order.domain.OrderSide;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderRequestDTO {

    private String userId;

    @NotBlank(message = "Ticker is required")
    private String ticker;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be greater than zero")
    private BigDecimal quantity;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be greater than zero")
    private BigDecimal price;

    @NotNull(message = "Order side (BUY/SELL) is required")
    private OrderSide side;
}
