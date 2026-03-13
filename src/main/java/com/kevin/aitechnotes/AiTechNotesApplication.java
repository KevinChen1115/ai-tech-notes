package com.kevin.aitechnotes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AiTechNotesApplication {
	public static void main(String[] args) {
		SpringApplication.run(AiTechNotesApplication.class, args);
	}
}
