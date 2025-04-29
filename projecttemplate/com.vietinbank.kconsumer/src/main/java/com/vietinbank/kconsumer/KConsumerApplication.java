package com.vietinbank.kconsumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class KConsumerApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(KConsumerApplication.class, args);
	}

}
