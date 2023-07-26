package com.rawchen.feishubot.util.chatgpt;

import com.rawchen.feishubot.entity.Account;
import com.rawchen.feishubot.entity.AccountList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

/**
 * 操作account配置文件工具类
 */
@Slf4j
public class AccountUtil {
	private static final String YAML_PATH = "accounts.yaml";

	public static AccountList readAccounts() {
		Yaml yaml = new Yaml(new Constructor(AccountList.class));
		try (InputStream in = new FileSystemResource(YAML_PATH).getInputStream()) {
			return yaml.loadAs(in, AccountList.class);
		} catch (IOException e) {
			throw new RuntimeException("Failed to read YAML", e);
		}
	}

	public static void writeAccounts(AccountList accountList) {
		DumperOptions options = new DumperOptions();
		options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
		options.setPrettyFlow(true);
		Yaml yaml = new Yaml(new Representer(), options);
		try (FileWriter writer = new FileWriter(YAML_PATH)) {
			yaml.dump(accountList, writer);
		} catch (IOException e) {
			throw new RuntimeException("Failed to write YAML", e);
		}
	}

	public static void queryAccountLevel() {
		Set<String> accounts = AccountPool.accountPool.keySet();
		for (String account : accounts) {
			ChatService chatService = AccountPool.accountPool.get(account);
			if (chatService != null) {
				int level = chatService.getLevel();
				chatService.queryAccountLevel();
				if (level != chatService.getLevel()) {
					log.info("账户「{}」等级变更: {} -> {}", account, level, chatService.getLevel());
				}
				//如果是plus账号降级
				//从plus账号池移动到普通账号池
				if (level == 4 && chatService.getLevel() == 3) {
					AccountPool.plusPool.remove(account);
					AccountPool.normalPool.put(account, chatService);
				}

				//如果是普通账号升级
				//从普通账号池移动到plus账号池
				if (level == 3 && chatService.getLevel() == 4) {
					AccountPool.normalPool.remove(account);
					AccountPool.plusPool.put(account, chatService);
				}
			}
		}
	}

	public static void updateToken(String accountName, String newToken) {
		AccountList accountList = readAccounts();
		List<Account> accounts = accountList.getAccounts();
		for (Account account : accounts) {
			if (account.getAccount().equals(accountName)) {
				account.setToken(newToken);
				break;
			}
		}
		writeAccounts(accountList);
	}

	public static void refreshToken() {
		AccountList accountList = readAccounts();
		List<Account> accounts = accountList.getAccounts();
		for (Account account : accounts) {
			if (account.getAccount() != null && account.getPassword() != null) {
				ChatService chatService = AccountPool.accountPool.get(account.getAccount());
				if (chatService != null) {
					chatService.refreshToken();
				}
			}
		}
		writeAccounts(accountList);
	}
}
