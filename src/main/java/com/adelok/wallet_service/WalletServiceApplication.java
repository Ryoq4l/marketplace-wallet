package com.adelok.wallet_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@ComponentScan(basePackages = {"com.adelok.wallet_service", "org.adelok.common"})
@ConfigurationPropertiesScan
@EnableFeignClients
@SpringBootApplication
@EnableWebSecurity
public class WalletServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(WalletServiceApplication.class, args);
	}

}
