package com.rawchen.feishubot.controller;

import cn.hutool.json.JSONUtil;
import com.lark.oapi.core.utils.Jsons;
import com.lark.oapi.event.EventDispatcher;
import com.lark.oapi.sdk.servlet.ext.ServletAdapter;
import com.lark.oapi.service.contact.v3.ContactService;
import com.lark.oapi.service.contact.v3.model.P2UserCreatedV3;
import com.lark.oapi.service.im.v1.ImService;
import com.lark.oapi.service.im.v1.model.P1MessageReadV1;
import com.lark.oapi.service.im.v1.model.P2MessageReadV1;
import com.lark.oapi.service.im.v1.model.P2MessageReceiveV1;
import com.rawchen.feishubot.entity.Conversation;
import com.rawchen.feishubot.handler.MessageHandler;
import com.rawchen.feishubot.util.chatgpt.ConversationPool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequiredArgsConstructor
@Slf4j
public class EventController {

	protected final MessageHandler messageHandler;

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
			init();
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
		log.debug("收到消息卡片事件: {}", body);
		JSONObject payload = new JSONObject(body);
		if (payload.opt("challenge") != null) {
			JSONObject res = new JSONObject();
			res.put("challenge", payload.get("challenge"));
			return res.toString();
		}

		String chatId = String.valueOf(payload.get("open_chat_id"));

		JSONObject action = (JSONObject) payload.get("action");
		String option = (String) action.get("option");

		Conversation bean = JSONUtil.toBean(option, Conversation.class);
		log.debug("收到模型选择: {}", bean);
		if (bean.getParentMessageId() == null) {
			bean.setParentMessageId("");
		}
		if (bean.getConversationId() == null) {
			bean.setConversationId("");
		}
		bean.setChatId(chatId);
		conversationPool.addConversation(chatId, bean);
		return "";
	}

	@GetMapping("/ping")
	@ResponseBody
	public String ping() {
		return "pong";
	}


	private void init() {
		EVENT_DISPATCHER = EventDispatcher.newBuilder(verificationToken,
						encryptionKey)
				.onP2MessageReceiveV1(new ImService.P2MessageReceiveV1Handler() {
					@Override
					public void handle(P2MessageReceiveV1 event) {
						//处理消息事件
						try {
							log.info("收到消息: {}", Jsons.DEFAULT.toJson(event));
							messageHandler.process(event);
						} catch (Exception e) {
							log.error("处理消息失败", e);
							throw new RuntimeException(e);
						}
					}
				}).onP2UserCreatedV3(new ContactService.P2UserCreatedV3Handler() {
					@Override
					public void handle(P2UserCreatedV3 event) {
						//员工入职事件
						//System.out.println(Jsons.DEFAULT.toJson(event));
						//System.out.println(event.getRequestId());
					}
				})
				.onP2MessageReadV1(new ImService.P2MessageReadV1Handler() {
					@Override
					public void handle(P2MessageReadV1 event) {
						//处理私聊已读事件
						//System.out.println(Jsons.DEFAULT.toJson(event));
						//System.out.println(event.getRequestId());
					}
				}).onP1MessageReadV1(new ImService.P1MessageReadV1Handler() {
					@Override
					public void handle(P1MessageReadV1 event) {
						//System.out.println(Jsons.DEFAULT.toJson(event));
						//System.out.println(event.getRequestId());
					}
				})
				.build();
	}
}
