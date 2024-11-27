package com.rawchen.feishubot;

import cn.hutool.extra.spring.SpringUtil;
import com.lark.oapi.ws.Client;
import com.rawchen.feishubot.handler.EventHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FeishuBotApplication {

	public static void main(String[] args) {
		SpringApplication.run(FeishuBotApplication.class, args);

		Client cli = new Client.Builder("cli_a44axxxxxxxxx", "9GLUNqVTusxxxxxxxxxxxxxxxxxxx")
				.eventHandler(EventHandler.EVENT_HANDLER)
				.build();
		cli.start();
	}

}
