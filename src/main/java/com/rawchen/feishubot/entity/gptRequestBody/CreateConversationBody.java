package com.rawchen.feishubot.entity.gptRequestBody;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * 构造ChatGpt对话api请求体
 */
public class CreateConversationBody {
	public static String of(String messageId, String text, String parentMessageId, String conversationId, String model) {
		JSONObject body = new JSONObject();
		body.put("action", "next");
		JSONArray messages = new JSONArray();
		JSONObject message = new JSONObject();
		message.put("id", messageId);
		JSONObject author = new JSONObject();
		author.put("role", "user");
		message.put("author", author);
		JSONObject content = new JSONObject();
		content.put("content_type", "text");
		JSONArray parts = new JSONArray();
		parts.put(text);
		content.put("parts", parts);
		message.put("content", content);
		messages.put(message);
		body.put("messages", messages);
		body.put("parent_message_id", parentMessageId);
		body.put("conversation_id", conversationId);
		body.put("model", model);
		body.put("timezone_offset_min", -480);
		body.put("history_and_training_disabled", false);

		return body.toString();
	}

}

