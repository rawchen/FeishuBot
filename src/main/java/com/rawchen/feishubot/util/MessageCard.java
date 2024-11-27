package com.rawchen.feishubot.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 机器人回复消息卡片消息格式
 */
@Slf4j
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
//    jsonMessage.put("elements", new JSONArray().put(markdownElement).put(line));
		JSONArray elements = new JSONArray();
		elements.add(markdownElement);
		jsonMessage.put("elements", elements);

		JSONObject jsonBody = new JSONObject();
		jsonBody.put("header", header);
		jsonBody.put("config", config);
		jsonBody.put("schema", "2.0");
		jsonBody.put("body", jsonMessage);

		gptAnswerMessageCard = jsonBody;
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
		JSONObject markdownElement = gptAnswerMessageCard.getJSONObject("body").getJSONArray("elements").getJSONObject(0);
		// 目前消息卡片不支持“```”代码块样式输出，因此可能会将代码块中一起出现“<”、“!”内容误识别xss注入后删掉
		// https://open.feishu.cn/document/common-capabilities/message-card/message-cards-content/using-markdown-tags
		// 卡片 JSON 2.0已支持基本的Markdown格式，除了![]()此格式飞书对链接做了校验，只能允许上传图片接口获取的key
		// https://open.feishu.cn/document/uAjLw4CM/ukzMukzMukzM/feishu-cards/card-components/content-components/rich-text
		content = StringUtil.replaceSpecialSymbol(content);
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
		JSONArray array = new JSONArray();
		array.add(gptAnswerMessageCard.get("elements"));

		JSONObject tip = new JSONObject();
		array.add(tip);
		tip.put("tag", "div");
		JSONObject tipText = new JSONObject();
		tipText.put("content", "请选择接下来对话模型(不选则默认当前模型)");
		tipText.put("tag", "plain_text");
		tip.put("text", tipText);

		array.add(selection);
		selection.put("tag", "action");

		JSONArray actions = new JSONArray();
		selection.put("actions", actions);

		JSONObject action = new JSONObject();
		action.put("tag", "select_static");
		actions.add(action);

		JSONObject placeholder = new JSONObject();
		placeholder.put("content", "当前会话模型");
		placeholder.put("tag", "plain_text");

		action.put("placeholder", placeholder);

		JSONArray options = new JSONArray();
		action.put("options", options);

		for (String s : selections.keySet()) {
			JSONObject option = new JSONObject();
			options.add(option);
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
