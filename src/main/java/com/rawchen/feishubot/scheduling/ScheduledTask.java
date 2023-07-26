package com.rawchen.feishubot.scheduling;

import com.rawchen.feishubot.util.chatgpt.AccountUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ScheduledTask {
	@Scheduled(cron = "0 0 0 ? * MON")
	public void refreshAccountToken() {
		log.info("开始刷新账号token");
		try {
			AccountUtil.refreshToken();
		} catch (Exception e) {
			log.error("刷新账号token失败", e);
		}
	}

	/**
	 * 每隔两个小时执行一次
	 */
	@Scheduled(initialDelay = 1000 * 60, fixedRate = 1000 * 60 * 60 * 2)
	public void checkAccountLevel() {
		try {
			AccountUtil.queryAccountLevel();
		} catch (Exception e) {
			log.error("检查账号等级失败", e);
		}
	}
}
