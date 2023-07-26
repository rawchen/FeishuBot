package com.rawchen.feishubot.service;

import com.rawchen.feishubot.entity.Account;
import com.rawchen.feishubot.util.chatgpt.AccountUtil;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountService {
	/**
	 * 从配置文件读取账号信息
	 *
	 * @return
	 */
	public List<Account> getAccounts() {
		return AccountUtil.readAccounts().getAccounts();
	}

	/**
	 * 更新账号的token到配置文件
	 *
	 * @param accountName
	 * @param newToken
	 */
	public void updateTokenForAccount(String accountName, String newToken) {
		AccountUtil.updateToken(accountName, newToken);
	}
}
