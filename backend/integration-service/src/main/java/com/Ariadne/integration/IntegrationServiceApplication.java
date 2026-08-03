package com.Ariadne.integration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {"com.Ariadne.integration", "com.Ariadne.shared"})
@EnableFeignClients
public class IntegrationServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(IntegrationServiceApplication.class, args);
	}
}
