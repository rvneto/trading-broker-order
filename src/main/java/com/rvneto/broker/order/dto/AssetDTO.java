package com.rvneto.broker.order.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AssetDTO(
        String ticker,
        String name,
        BigDecimal currentPrice,
        LocalDateTime lastUpdate,
        String status
) {}
