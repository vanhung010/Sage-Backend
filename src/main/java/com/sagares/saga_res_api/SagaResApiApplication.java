package com.sagares.saga_res_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class SagaResApiApplication {

	static void main(String[] args) {
		SpringApplication.run(SagaResApiApplication.class, args);
	}

}
