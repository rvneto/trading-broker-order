package com.rvneto.broker.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class BrokerOrderApplication {

	public static void main(String[] args) {
		SpringApplication.run(BrokerOrderApplication.class, args);
	}

}
