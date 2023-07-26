package com.rawchen.feishubot.util;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;

/**
 * 机器人回复消息卡片消息格式
 */
public class MessageCard {
	private static JSONObject gptAnswerMessageCard;

	private static boolean hasSelection = false;

	private static void initChatGptAnswerMessageCard() {
		JSONObject markdownElement = new JSONObject();
		markdownElement.put("tag", "markdown");
		markdownElement.put("content", "");

		JSONObject title = new JSONObject();
		title.put("content", "");
		title.put("tag", "plain_text");

		JSONObject header = new JSONObject();
		header.put("template", "blue");
		header.put("title", title);

		JSONObject config = new JSONObject();
		config.put("wide_screen_mode", true);
		config.put("update_multi", true);

//    JSONObject line = new JSONObject();
//    line.put("tag", "hr");

		JSONObject jsonMessage = new JSONObject();
		jsonMessage.put("header", header);
		jsonMessage.put("config", config);
//    jsonMessage.put("elements", new JSONArray().put(markdownElement).put(line));
		jsonMessage.put("elements", new JSONArray().put(markdownElement));

		gptAnswerMessageCard = jsonMessage;
	}

	/**
	 * 普通消息卡片格式
	 *
	 * @param title
	 * @param content
	 * @return
	 */
	public static String ofGptAnswerMessageCard(String title, String content) {
		if (gptAnswerMessageCard == null) {
			initChatGptAnswerMessageCard();
		}
		JSONObject markdownElement = gptAnswerMessageCard.getJSONArray("elements").getJSONObject(0);
		markdownElement.put("content", content);
		JSONObject titleObject = gptAnswerMessageCard.getJSONObject("header").getJSONObject("title");
		titleObject.put("content", title);
		return gptAnswerMessageCard.toString();
	}

	/**
	 * 带模型选择的消息卡片
	 *
	 * @param title
	 * @param content
	 * @param selections
	 * @return
	 */
	public static String ofGptAnswerMessageCardWithSelection(String title, String content, Map<String, String> selections) {
		if (hasSelection) {
			gptAnswerMessageCard = null;
		}
		ofGptAnswerMessageCard(title, content);
		JSONObject selection = new JSONObject();
		JSONArray array = (JSONArray) gptAnswerMessageCard.get("elements");

		JSONObject tip = new JSONObject();
		array.put(tip);
		tip.put("tag", "div");
		JSONObject tipText = new JSONObject();
		tipText.put("content", "请选择接下来对话模型(不选则默认当前模型)");
		tipText.put("tag", "plain_text");
		tip.put("text", tipText);

		array.put(selection);
		selection.put("tag", "action");

		JSONArray actions = new JSONArray();
		selection.put("actions", actions);

		JSONObject action = new JSONObject();
		action.put("tag", "select_static");
		actions.put(action);

		JSONObject placeholder = new JSONObject();
		placeholder.put("content", "当前会话模型");
		placeholder.put("tag", "plain_text");

		action.put("placeholder", placeholder);

		JSONArray options = new JSONArray();
		action.put("options", options);

		for (String s : selections.keySet()) {
			JSONObject option = new JSONObject();
			options.put(option);
			option.put("value", selections.get(s));
			JSONObject text = new JSONObject();
			text.put("content", s);
			text.put("tag", "plain_text");
			option.put("text", text);
		}
		hasSelection = true;
		return gptAnswerMessageCard.toString();
	}


}
