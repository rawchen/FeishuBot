package com.rawchen.feishubot.entity.gpt;

import lombok.Data;

import java.util.List;

@Data
/**
 * openai 查询模型接口返回的模型信息
 */
public class Model {
	private String slug;
	private String max_tokens;
	private String title;
	private String description;
	private List<String> tags;
	private String capabilities;

}
