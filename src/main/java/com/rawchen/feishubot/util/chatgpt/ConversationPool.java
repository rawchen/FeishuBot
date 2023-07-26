package com.rawchen.feishubot.util.chatgpt;

import com.rawchen.feishubot.entity.Conversation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 会话池
 * 记录用户和对应gpt会话上下文的对应关系
 * 通过chatId来区分用户，一个用户对应一个会话
 * 从而实现gpt具有上下文能力
 */
@Component
@Slf4j
public class ConversationPool {
	public volatile Map<String, Conversation> conversationMap = new HashMap<>();

	public void addConversation(String chatId, Conversation conversation) {
//        log.info("chatId:{} 会话变更前:{}", chatId, conversationMap.get(chatId));
//        log.info("chatId:{} 会话变更后:{}", chatId, conversation);
		conversationMap.put(chatId, conversation);
	}

	public Conversation getConversation(String chatId) {
		return conversationMap.get(chatId);
	}
}
