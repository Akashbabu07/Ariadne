package com.Ariadne.scheduler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.Ariadne.scheduler", "com.Ariadne.shared"})
@EnableFeignClients
@EnableScheduling
public class SchedulerServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(SchedulerServiceApplication.class, args);
	}
}