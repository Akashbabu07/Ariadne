package com.Ariadne.graph;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.Ariadne.graph", "com.Ariadne.shared"})
public class GraphServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(GraphServiceApplication.class, args);
	}
}