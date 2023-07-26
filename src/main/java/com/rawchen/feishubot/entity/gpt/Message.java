package com.rawchen.feishubot.entity.gpt;

import lombok.Data;

/**
 * gpt对话响应对象
 */
@Data
public class Message {
	private String id;
	private Author author;
	private Double createTime;
	private Object updateTime;
	private Content content;
	private String status;
	private Boolean endTurn;
	private Double weight;

}
