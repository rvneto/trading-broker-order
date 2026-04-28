package com.rvneto.broker.order.client;

import com.rvneto.broker.order.dto.WalletSummaryDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "broker-wallet-api", url = "${app.services.wallet-url}")
public interface WalletClient {

    @GetMapping("/api/v1/wallet/{userId}/summary")
    WalletSummaryDTO getWalletSummary(@PathVariable("userId") String userId);

}
