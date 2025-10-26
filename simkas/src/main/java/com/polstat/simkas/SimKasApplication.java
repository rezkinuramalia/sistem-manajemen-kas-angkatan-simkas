package com.polstat.simkas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SimKasApplication {
	public static void main(String[] args) {
		SpringApplication.run(SimKasApplication.class, args);
		System.out.println("✅ SIMKAS Web Service is running...");
	}
}
