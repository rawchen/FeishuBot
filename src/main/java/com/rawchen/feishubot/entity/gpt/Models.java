package com.rawchen.feishubot.entity.gpt;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Models {

	/**
	 * plus用户默认模型
	 */
	public static final String PLUS_DEFAULT_MODEL = "Default (GPT-3.5)";

	/**
	 * 普通用户默认模型
	 */
	public static String NORMAL_DEFAULT_MODEL;

	/**
	 * 空模型
	 */
	public static final String EMPTY_MODEL = "Empty";

	/**
	 * 模型title和对应模型
	 */
	public static Map<String, Model> modelMap = new HashMap<>();

	/**
	 * plus模型title池 用于判断请求是否是plus模型
	 */
	public static Set<String> plusModelTitle = new HashSet<>();

	/**
	 * normal模型title池
	 */
	public static Set<String> normalModelTitle = new HashSet<>();


}
