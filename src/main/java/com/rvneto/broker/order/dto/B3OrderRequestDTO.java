package com.rvneto.broker.order.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class B3OrderRequestDTO {

    private String orderId;
    private String ticker;
    private BigDecimal quantity;
    private BigDecimal price;
    private String side;

}