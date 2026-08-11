package com.devfat.mini_ecommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ConfigurationPropertiesScan
@EnableJpaAuditing(auditorAwareRef = "securityAuditorAware")
public class MiniEcommerceApplication {
	public static void main(String[] args) {
		SpringApplication.run(MiniEcommerceApplication.class, args);
	}
}
