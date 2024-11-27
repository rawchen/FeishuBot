package com.rawchen.feishubot.entity.gptRequestBody;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 构造ChatGpt对话api请求体
 */
public class CreateConversationBody {
	public static String of(String messageId, String text, String parentMessageId, String conversationId, String model) {
		JSONObject param = new JSONObject();
//		body.put("action", "next");
//		JSONArray messages = new JSONArray();
//		JSONObject message = new JSONObject();
//		message.put("id", messageId);
//		JSONObject author = new JSONObject();
//		author.put("role", "user");
//		message.put("author", author);
//		JSONObject content = new JSONObject();
//		content.put("content_type", "text");
//		JSONArray parts = new JSONArray();
//		parts.put(text);
//		content.put("parts", parts);
//		message.put("content", content);
//		messages.put(message);
//		body.put("messages", messages);
//		body.put("parent_message_id", parentMessageId);
//		body.put("conversation_id", conversationId);
//		body.put("model", model);
//		body.put("timezone_offset_min", -480);
//		body.put("history_and_training_disabled", false);


		JSONObject message = new JSONObject();
		message.put("content", text);
		message.put("role", "user");
//		Message message = new Message().setRole("user").setContent(text);
		JSONArray messages = new JSONArray();
		messages.add(message);
		param.put("messages", messages);
        param.put("model", model);
//		param.put("model", "gpt-4-turbo");
		param.put("stream", false);

		return param.toJSONString();
	}

}

