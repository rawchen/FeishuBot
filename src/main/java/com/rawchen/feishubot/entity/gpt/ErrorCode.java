package com.rawchen.feishubot.entity.gpt;

import java.util.HashMap;
import java.util.Map;

public class ErrorCode {

	public static final int INVALID_JWT = 1;

	/**
	 * 账号繁忙
	 */
	public static final int BUSY = 2;
	public static final int INVALID_API_KEY = 3;

	/**
	 * 4.0  3小时25次的对话限制
	 */
	public static final int CHAT_LIMIT = 4;


	public static final int RESPONSE_ERROR = 5;

	public static final int ACCOUNT_DEACTIVATED = 6;

	public static final Map<Integer, String> map = new HashMap<>();

	static {
		map.put(INVALID_JWT, "无效的access token");
		map.put(BUSY, "账号繁忙中");
		map.put(INVALID_API_KEY, "无效的api key");
		map.put(CHAT_LIMIT, "4.0接口被限制了");
		map.put(RESPONSE_ERROR, "响应错误");
		map.put(ACCOUNT_DEACTIVATED, "账号被停用");
	}


}
