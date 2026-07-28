package com.Ariadne.knowledge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.Ariadne.knowledge", "com.Ariadne.shared"})
public class KnowledgeServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(KnowledgeServiceApplication.class, args);
	}
}