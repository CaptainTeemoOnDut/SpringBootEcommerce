package com.example.AppChatDemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class AppChatDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(AppChatDemoApplication.class, args);
	}

}
