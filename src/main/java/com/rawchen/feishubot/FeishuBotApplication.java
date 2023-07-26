package com.rawchen.feishubot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FeishuBotApplication {

	public static void main(String[] args) {
		SpringApplication.run(FeishuBotApplication.class, args);
	}

}
