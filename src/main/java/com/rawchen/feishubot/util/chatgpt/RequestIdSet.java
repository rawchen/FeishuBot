package com.rawchen.feishubot.util.chatgpt;

import java.util.HashSet;
import java.util.Set;

/**
 * 用来保存飞书推送过来的请求id
 * 事件可能会重复推送，所以需要去重
 */
public class RequestIdSet {
	public static final Set<String> requestIdSet = new HashSet<>();
}
