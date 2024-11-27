package com.rawchen.feishubot.util.chatgpt;

import com.rawchen.feishubot.entity.Account;
import com.rawchen.feishubot.entity.Status;
import com.rawchen.feishubot.entity.gpt.Models;
import com.rawchen.feishubot.service.AccountService;
import com.rawchen.feishubot.util.TaskPool;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 账号池
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Data

public class AccountPool {
	protected final AccountService accountService;

	protected final Environment environment;

	private int size;

	@Value("${proxy.url}")
	private String proxyUrl;

	public static Map<String, ChatService> accountPool = new HashMap<>();

	public static Map<String, ChatService> normalPool = new HashMap<>();

	public static Map<String, ChatService> plusPool = new HashMap<>();

	/**
	 * 初始化账号池
	 */
	@PostConstruct
	public void init() {
		List<Account> accounts = accountService.getAccounts();
		List<String> usefulAccounts = new ArrayList<>();
		for (Account account : accounts) {

			ChatService chatService = new ChatService(account.getAccount(), account.getPassword(), account.getToken(), proxyUrl);
			if (account.getToken() == null) {
				log.debug("账号{}未登录", account.getAccount());
				boolean ok = chatService.build();
				if (ok) {
					log.debug("账号{}登录成功", account.getAccount());
					account.setToken(chatService.getAccessToken());
//					accountService.updateTokenForAccount(account.getAccount(), chatService.getAccessToken());

				} else {
					//ChatGpt登录失败
					log.error("账号{}登录失败", account.getAccount());
					continue;
				}
			}

			//查询账号是否plus用户
//			boolean b = chatService.queryAccountLevel();
			//如果token失效，重新登录，更新token，重新查询一次
//			if (!b) {
//				log.debug("账号{}登录失效", account.getAccount());
//				log.debug("账号{}重新登录", account.getAccount());
//				boolean ok = chatService.build();
//				if (ok) {
//					//重新登录成功
//					account.setToken(chatService.getAccessToken());
//					accountService.updateTokenForAccount(account.getAccount(), chatService.getAccessToken());
//					b = chatService.queryAccountLevel();
//					if (!b) {
//						continue;
//					}
//				} else {
//					//ChatGpt登录失败
//					log.error("账号{}登录失败", account.getAccount());
//					continue;
//				}
//			}


			usefulAccounts.add(account.getAccount());
			accountPool.put(account.getAccount(), chatService);
			size++;
			if (chatService.getLevel() == 3) {
//				log.info("账号{}为normal用户", account.getAccount());
				log.info("账号{}为normal用户", account.getToken());
				normalPool.put(account.getAccount(), chatService);
			}
			if (chatService.getLevel() == 4) {
//				log.info("账号{}为plus用户", account.getAccount());
				log.info("账号{}为plus用户", account.getToken());
				plusPool.put(account.getAccount(), chatService);
			}
		}

		log.info("normal账号池共{}个账号", normalPool.size());
		log.info("plus账号池共{}个账号", plusPool.size());
		TaskPool.init(usefulAccounts);
		TaskPool.runTask();
	}


	/**
	 * 需要plus模型，则从plus账号池中获取
	 * 需要normal模型，则从normal账号池中获取
	 *
	 * @param model 模型title
	 * @return
	 */
	public ChatService getFreeChatService(String model) {
		if (!StringUtils.hasText(model)) {
			model = Models.NORMAL_DEFAULT_MODEL;
		}

		List<ChatService> plusAccountList = new ArrayList<>();
		for (String s : plusPool.keySet()) {
			ChatService chatService = plusPool.get(s);
			if (chatService.getStatus() == Status.FINISHED) {
				plusAccountList.add(chatService);
			}
		}

		//如果plus账号池中有账号，且需要plus模型，则从plus账号池中获取
		//如果需要的模型是空模型(新建会话，使用对应账号默认模型就行)，也可以从plus账号池中获取
		if (plusAccountList.size() > 0 && (Models.plusModelTitle.contains(model) || model.equals(Models.EMPTY_MODEL))) {
			return plusAccountList.get((int) (Math.random() * plusAccountList.size()));
		}

		if (Models.plusModelTitle.contains(model)) {
			//如果需要plus模型，但是没有plus账号，返回null
			return null;
		}
		List<ChatService> normalAccountList = new ArrayList<>();
		for (String s : normalPool.keySet()) {
			ChatService chatService = normalPool.get(s);
			if (chatService.getStatus() == Status.FINISHED) {
				normalAccountList.add(chatService);
			}
		}
		if (normalAccountList.size() == 0) {
			return null;
		} else {
			return normalAccountList.get((int) (Math.random() * normalAccountList.size()));
		}
	}

	public void modifyChatService(ChatService chatService) {
		log.info("修改账号{}信息", chatService.getAccount());
		Account account = new Account();
		account.setAccount(chatService.getAccount());
		account.setToken(chatService.getAccessToken());
		account.setPassword(chatService.getPassword());
		accountService.updateTokenForAccount(chatService.getAccount(), chatService.getAccessToken());
	}

	public ChatService getChatService(String account) {
		if (account == null || account.equals("")) {
			if (plusPool.containsKey(account)) {
				return getFreeChatService(Models.PLUS_DEFAULT_MODEL);
			} else {
				return getFreeChatService(Models.NORMAL_DEFAULT_MODEL);
			}
		}
		return accountPool.get(account);
	}

	public static void removeAccount(String account) {
		log.info("移除账号{}", account);
		accountPool.remove(account);
		normalPool.remove(account);
		plusPool.remove(account);
	}

}
