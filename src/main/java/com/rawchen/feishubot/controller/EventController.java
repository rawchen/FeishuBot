package com.rawchen.feishubot.controller;

import cn.hutool.json.JSONUtil;
import com.lark.oapi.event.EventDispatcher;
import com.lark.oapi.sdk.servlet.ext.ServletAdapter;
import com.rawchen.feishubot.entity.Conversation;
import com.rawchen.feishubot.handler.MessageHandler;
import com.rawchen.feishubot.util.chatgpt.ConversationPool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequiredArgsConstructor
@Slf4j
public class EventController {

//	protected final MessageHandler messageHandler;

	@Value("${feishu.verificationToken}")
	private String verificationToken;

	@Value("${feishu.encryptKey}")
	private String encryptionKey;

	private EventDispatcher EVENT_DISPATCHER;

	protected final ConversationPool conversationPool;

	protected final ServletAdapter servletAdapter;

	//处理事件回调
	@RequestMapping("/chatEvent")
	public void event(HttpServletRequest request, HttpServletResponse response)
			throws Throwable {
		if (EVENT_DISPATCHER == null) {
//			init();
		}

		servletAdapter.handleEvent(request, response, EVENT_DISPATCHER);
	}


	/**
	 * 处理消息卡片事件回调
	 *
	 * @param body
	 * @return
	 */
	@PostMapping("/cardEvent")
	@ResponseBody
	public String event(@RequestBody String body) {
//		log.debug("收到消息卡片事件: {}", body);
//		JSONObject payload = new JSONObject(body);
//		if (payload.opt("challenge") != null) {
//			JSONObject res = new JSONObject();
//			res.put("challenge", payload.get("challenge"));
//			return res.toString();
//		}
//
//		String chatId = String.valueOf(payload.get("open_chat_id"));
//
//		JSONObject action = (JSONObject) payload.get("action");
//		String option = (String) action.get("option");
//
//		Conversation bean = JSONUtil.toBean(option, Conversation.class);
//		log.debug("收到模型选择: {}", bean);
//		if (bean.getParentMessageId() == null) {
//			bean.setParentMessageId("");
//		}
//		if (bean.getConversationId() == null) {
//			bean.setConversationId("");
//		}
//		bean.setChatId(chatId);
//		conversationPool.addConversation(chatId, bean);
		return "";
	}

	@GetMapping("/ping")
	@ResponseBody
	public String ping() {
		return "pong";
	}
}
