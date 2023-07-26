package com.rawchen.feishubot.entity.gpt;

import lombok.Data;

import java.util.List;

@Data
public class Content {
	private String content_type;
	private List<String> parts;
	private String text;
}
