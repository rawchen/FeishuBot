package com.rawchen.feishubot.config;

import com.lark.oapi.Client;
import com.lark.oapi.sdk.servlet.ext.ServletAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeiShuConfig {

	@Value("${feishu.appId}")
	private String appId;

	@Value("${feishu.appSecret}")
	private String appSecret;

	@Bean
	public ServletAdapter getServletAdapter() {
		return new ServletAdapter();
	}

	@Bean
	public Client getClient() {
		return Client.newBuilder(appId, appSecret).build();
	}
}
