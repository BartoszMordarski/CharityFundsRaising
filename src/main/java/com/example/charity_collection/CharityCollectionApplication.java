package com.example.charity_collection;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CharityCollectionApplication {

	public static void main(String[] args) {
		SpringApplication.run(CharityCollectionApplication.class, args);
	}

}
