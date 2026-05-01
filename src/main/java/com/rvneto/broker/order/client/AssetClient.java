package com.rvneto.broker.order.client;

import com.rvneto.broker.order.dto.AssetDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "broker-asset-api", url = "${app.services.asset-url}")
public interface AssetClient {

    @GetMapping("/api/v1/assets/{ticker}")
    AssetDTO getByTicker(@PathVariable("ticker") String ticker);
}
