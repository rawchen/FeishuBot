package com.rawchen.feishubot.entity.gpt;

import lombok.Data;

@Data
public class Answer {

	private int seq;

	/**
	 * 是否正常返回GPT对话结果
	 */
	private boolean success;
	private Message message;
	private String conversationId;
	private Object error;
	/**
	 * 对话响应是否结束
	 */
	private boolean finished;
	/**
	 * 对话响应的文本
	 */
	private String answer;

	/**
	 * 错误响应码
	 */
	private int errorCode;
}
