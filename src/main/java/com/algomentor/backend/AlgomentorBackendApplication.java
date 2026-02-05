package com.algomentor.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AlgomentorBackendApplication {

	public static void main(String[] args) {
		DotenvBootstrap.load();
		SpringApplication.run(AlgomentorBackendApplication.class, args);

	}


}
