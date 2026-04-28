package com.rvneto.broker.order.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class WalletSummaryDTO {

    private String userId;
    private BigDecimal availableBalance;
    private BigDecimal positionsValue;
    private BigDecimal totalEquity;
    private String currency;

}