package com.rawchen.feishubot.entity;

public enum Mode {
	/**
	 * 快速回复模式，只要问就找可用的账号服务
	 */
	FAST,
	/**
	 * 保持会话模式，尽可能使用同一个账号服务
	 */
	KEEP
}
