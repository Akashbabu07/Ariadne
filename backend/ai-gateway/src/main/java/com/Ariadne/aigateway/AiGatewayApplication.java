package com.Ariadne.aigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.Ariadne.aigateway", "com.Ariadne.shared"})
public class AiGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(AiGatewayApplication.class, args);
    }
}
