package com.meditreat.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = { "com.meditreat.model", "com.meditreat.app.entity" })
@EnableJpaRepositories(basePackages = { "com.meditreat.repository", "com.meditreat.app.repository" })
public class MeditreatAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(MeditreatAppApplication.class, args);
	}

}