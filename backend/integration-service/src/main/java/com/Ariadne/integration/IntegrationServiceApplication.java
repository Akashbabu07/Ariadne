package com.Ariadne.integration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.Ariadne.integration", "com.Ariadne.shared"})
public class IntegrationServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(IntegrationServiceApplication.class, args);
	}
}
