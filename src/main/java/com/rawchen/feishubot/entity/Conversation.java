package com.rawchen.feishubot.entity;

import lombok.Data;

@Data
public class Conversation {
	/**
	 * gpt的会话id
	 */
	public String conversationId;
	/**
	 * 对话id，用于区分不同用户
	 */
	public String chatId;
	/**
	 * 服务该会话的gpt账号
	 */
	public String account;
	/**
	 * 服务该会话的gpt模型
	 */
	public String model;
	/**
	 * gpt中的上下文消息id
	 */
	public String parentMessageId;
	public volatile Status status;
	/**
	 * 消息中的标题
	 */
	public String title;

	/**
	 * 回复模式
	 */
	public Mode mode;
}
