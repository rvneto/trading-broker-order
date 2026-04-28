package com.rvneto.broker.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class B3OrderResponseDTO {

    private String orderId;
    private String status;
    private BigDecimal executedPrice;

}