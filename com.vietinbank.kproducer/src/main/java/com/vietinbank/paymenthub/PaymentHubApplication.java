package com.vietinbank.paymenthub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class PaymentHubApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(PaymentHubApplication.class, args);
	}

}
