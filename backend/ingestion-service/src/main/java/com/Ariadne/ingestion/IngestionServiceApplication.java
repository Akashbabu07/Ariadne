package com.Ariadne.ingestion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.Ariadne.ingestion", "com.Ariadne.shared"})
public class IngestionServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(IngestionServiceApplication.class, args);
	}
}