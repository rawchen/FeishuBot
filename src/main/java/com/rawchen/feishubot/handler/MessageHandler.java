package com.rawchen.feishubot.handler;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.lark.oapi.Client;
import com.lark.oapi.service.contact.v3.model.User;
import com.lark.oapi.service.im.v1.model.*;
import com.rawchen.feishubot.entity.Conversation;
import com.rawchen.feishubot.entity.Mode;
import com.rawchen.feishubot.entity.Status;
import com.rawchen.feishubot.entity.gpt.Answer;
import com.rawchen.feishubot.entity.gpt.ErrorCode;
import com.rawchen.feishubot.entity.gpt.Models;
import com.rawchen.feishubot.service.MessageService;
import com.rawchen.feishubot.service.UserService;
import com.rawchen.feishubot.util.chatgpt.AccountPool;
import com.rawchen.feishubot.util.chatgpt.ChatService;
import com.rawchen.feishubot.util.chatgpt.ConversationPool;
import com.rawchen.feishubot.util.chatgpt.RequestIdSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@RequiredArgsConstructor
public class MessageHandler {


	protected final Client client;
	protected final AccountPool accountPool;
	protected final UserService userService;
	protected final MessageService messageService;
	protected final ConversationPool conversationPool;

	/**
	 * 飞书推送事件会重复，用于去重
	 *
	 * @param event
	 * @return
	 */
	private boolean checkInvalidEvent(P2MessageReceiveV1 event) {
//		String requestId = event.getRequestId();
		String requestId = event.getHeader().getEventId();
		// 根据内存记录的消息事件id去重
		if (RequestIdSet.requestIdSet.contains(requestId)) {
			//log.warn("重复请求，requestId:{}", requestId);
			return false;
		}
		RequestIdSet.requestIdSet.add(requestId);

		String createTime = event.getEvent().getMessage().getCreateTime();
		Long createTimeLong = Long.valueOf(createTime);
		Long now = System.currentTimeMillis();
		if (now - createTimeLong > 1000 * 10) {
			// 根据消息事件的创建时间去重（如果现在的时间比创建时间大10秒就说明是重复的，正常服务器时间同步正确应该只有1s的差别）
			//log.warn("消息过期，requestId:{}", requestId);
			return false;
		}

		P2MessageReceiveV1Data messageEvent = event.getEvent();
		EventMessage message = messageEvent.getMessage();

		String chatType = message.getChatType();
		String msgType = message.getMessageType();

		log.info("对话类型: {}，消息类型: {}", chatType, msgType);


		// 只处理私聊文本消息
//    if (!chatType.equals("p2p") || !msgType.equals("text")) {
//      log.warn("不支持的ChatType或MsgType,ChatGpt不处理");
//      return false;
//    }

		// 不是group群组@机器人的消息和机器人私聊消息，或者不是文本就不处理
		if ((!chatType.equals("group") && !chatType.equals("p2p")) || !msgType.equals("text")) {
			log.warn("不支持的ChatType或MsgType,ChatGpt不处理");
			return false;
		}
		return true;
	}


	public void process(P2MessageReceiveV1 event) throws Exception {
		P2MessageReceiveV1Data messageEvent = event.getEvent();
		EventMessage message = messageEvent.getMessage();
		// 如果是重复消息不处理
		if (!checkInvalidEvent(event)) {
			return;
		}
		log.info("事件时间: {}", DateUtil.formatDateTime(DateUtil.date(Long.parseLong(event.getEvent().getMessage().getCreateTime()))));
		JSONObject jsonObject = JSONObject.parseObject(message.getContent());
		String text = jsonObject.getString("text");

		// 去掉事件获取开头的"@_user_1"
		if (text.startsWith("@_user_1")) {
			text = text.substring(9);
		}

		try {
			User user = userService.getUserByOpenId(event.getEvent().getSender().getSenderId().getOpenId());
			String name = user.getName();
			log.info("{}: {}", name, text);
		} catch (Exception e) {
			log.error("获取用户信息失败", e);
		}

		if (text == null || text.equals("")) {
			return;
		}
		String chatId = message.getChatId();
		//尝试从会话池中获取会话，从而保持上下文
		Conversation conversation = conversationPool.getConversation(chatId);
		String model;

		String account = null;
		ChatService chatService;

		if (conversation == null) {
			//如果没有会话，新建会话，采用默认3.5的模型
			conversation = new Conversation();
			chatService = accountPool.getFreeChatService(Models.EMPTY_MODEL);
			if (chatService != null) {
//				if (chatService.getLevel() == 4) {
				if (chatService.getLevel() == 3) {
					model = Models.PLUS_DEFAULT_MODEL;
				} else {
					model = Models.NORMAL_DEFAULT_MODEL;
				}

				conversation.setChatId(chatId);
				conversation.setModel(model);
				//默认使用keep模式
				conversation.setMode(Mode.FAST);

				conversationPool.addConversation(chatId, conversation);
			} else {
				model = null;
			}


		} else {

			//新会话意图
			model = conversation.getModel();
			chatService = accountPool.getFreeChatService(model);
		}


		if (chatService == null) {
			if (accountPool.getSize() == 0) {
				messageService.sendTextMessageByChatId(chatId, "服务器未配置可用账户");
				return;
			}

			//如果是fast模式，则需要切换账号服务
//			if (conversation.getMode() == Mode.FAST) {
//				chatService = accountPool.getFreeChatService(Models.EMPTY_MODEL);
//				if (chatService == null) {
//					messageService.sendTextMessageByChatId(chatId, "目前无空闲该模型，请稍后再试，或者更换模型");
//					return;
//				} else {
//					conversation.setAccount(chatService.getAccount());
//
//					if (chatService.getLevel() == 4) {
//						conversation.setModel(Models.PLUS_DEFAULT_MODEL);
//					} else {
//						conversation.setModel(Models.NORMAL_DEFAULT_MODEL);
//					}
//				}
//			}
		}

		//账号池里所有的账号都在运行
		if (chatService == null) {
			messageService.sendTextMessageByChatId(chatId, "目前无空闲账号，请稍后再试");
			return;
		}

		String firstText = "正在生成中，请稍后...";

		if (chatService.getStatus() == Status.RUNNING) {
			//fast模式，切换账号，创建新会话
			chatService = accountPool.getFreeChatService(Models.EMPTY_MODEL);
			if (chatService.getStatus() == Status.RUNNING) {
				firstText = "目前该账号正在运行，请稍等...";
			} else {
				conversation.setAccount(chatService.getAccount());
				//fast模式下，切换账号和原账号模型保持一致
				//如果原账号是plus模型，则切换到的是normal账号，则用normal的模型
				if (chatService.getLevel() == 4) {
					if (Models.plusModelTitle.contains(conversation.getModel())) {
						conversation.setModel(conversation.getModel());
					} else {
						conversation.setModel(Models.PLUS_DEFAULT_MODEL);
					}
				} else {
					conversation.setModel(Models.NORMAL_DEFAULT_MODEL);
				}
			}
		}
		conversation.setAccount(chatService.getAccount());
		chatService.setStatus(Status.RUNNING);
		conversation.setStatus(Status.RUNNING);

		String title;
		if (!StringUtils.hasLength(chatService.getAccount())) {
			title = model;
		} else {
			title = model + " : " + chatService.getAccount();
		}

		if (chatService.getLevel() == 4) {
			title += "  [plus] ";
		}

		String modelParam = Models.PLUS_DEFAULT_MODEL;
		title = modelParam;
//		model = modelParam;

//    title += "「" + conversation.getMode() + "」";


		CreateMessageResp resp = messageService.sendGptAnswerMessage(chatId, title, firstText);
		String messageId = resp.getData().getMessageId();

//    Map<String, String> selections = createSelection(conversation);

//		String modelParam = Models.modelMap.get(conversation.getModel()).getSlug();

//		log.info("新建会话");
		conversation.setTitle(title);
		String finalTitle = title;
		ChatService finalChatService = chatService;
		chatService.newChat(text, modelParam, answer -> {
			processAnswer(answer, finalTitle, chatId, finalChatService, messageId, event, model);
		});


//		log.info("服务完成: {}|{}|{}|{}", chatService.getAccount(), model, chatId, messageId);
		log.info("服务完成: {}|{}|{}|{}\n", chatService.getAccessToken(), model, chatId, messageId);
	}

	private Map<String, String> createSelection(Conversation conversation) {
		//将curConversation转换成json格式
		String conversationStr = JSONUtil.toJsonStr(conversation);
		Map<String, String> selections = new HashMap<>();
		selections.put("使用当前上下文", conversationStr);

		for (String s : Models.normalModelTitle) {
			Conversation normalModelConversation = new Conversation();
			normalModelConversation.setModel(s);
			selections.put(s, JSONUtil.toJsonStr(normalModelConversation));
		}

		if (AccountPool.plusPool.isEmpty()) {
			return selections;
		}

		for (String modelTitle : Models.plusModelTitle) {
			Conversation plusModelConversation = new Conversation();
			plusModelConversation.setModel(modelTitle);
			selections.put(modelTitle, JSONUtil.toJsonStr(plusModelConversation));
		}
		return selections;
	}

	private void processAnswer(Answer answer, String title, String chatId, ChatService chatService, String messageId, P2MessageReceiveV1 event, String model) throws Exception {
		if (!answer.isSuccess()) {
			// gpt请求失败
			if (answer.getErrorCode() == ErrorCode.INVALID_JWT || answer.getErrorCode() == ErrorCode.INVALID_API_KEY) {

				messageService.modifyGptAnswerMessageCard(messageId, title, (String) answer.getError());
				boolean build = chatService.build();
				if (build) {
					accountPool.modifyChatService(chatService);
					RequestIdSet.requestIdSet.remove(event.getRequestId());
					process(event);
				} else {
					messageService.modifyGptAnswerMessageCard(messageId, title, "账号重新登录失败");
				}
				return;
			} else if (answer.getErrorCode() == ErrorCode.ACCOUNT_DEACTIVATED) {
				answer.setError(ErrorCode.map.get(ErrorCode.ACCOUNT_DEACTIVATED));
				log.error("账号{} {}", chatService.getAccount(), ErrorCode.map.get(ErrorCode.ACCOUNT_DEACTIVATED));
				AccountPool.removeAccount(chatService.getAccount());
			}
			messageService.modifyGptAnswerMessageCard(messageId, title, (String) answer.getError());
			chatService.setStatus(Status.FINISHED);
			conversationPool.getConversation(chatId).setStatus(Status.FINISHED);
			return;
		}
		Conversation conversation = conversationPool.getConversation(chatId);
//		conversation.setParentMessageId(answer.getMessage().getId());
//		conversation.setConversationId(answer.getConversationId());

		if (!answer.isFinished()) {
			chatService.setStatus(Status.RUNNING);
			conversation.setStatus(Status.RUNNING);
		} else {
			chatService.setStatus(Status.FINISHED);
			conversation.setStatus(Status.FINISHED);
			//    conversation.setChatId(chatId);
//      conversation.setParentMessageId(answer.getMessage().getId());
//      conversation.setConversationId(answer.getConversationId());
//    conversation.setAccount(chatService.getAccount());
//    conversation.setModel(model);
//    conversation.setTitle(title);
		}

//    selections.put("使用当前上下文", JSONUtil.toJsonStr(conversation));

		String content = answer.getAnswer();
		if (content == null || content.equals("")) {
			return;
		}
		PatchMessageResp resp1 = messageService.modifyGptAnswerMessageCard(messageId, title, content);
		if (resp1.getCode() != 0) {
			log.error("code: {}", resp1.getCode());
			log.error("msg: {}", resp1.getMsg());
			log.error("error msg: {}", resp1.getError().getMessage());
		}

		if (answer.isFinished() && resp1.getCode() == 230020) {
			//保证最后完整的gpt响应 不会被飞书消息频率限制
			while (answer.isFinished() && resp1.getCode() != 0) {
				log.warn("重试中 code: {} msg: {}", resp1.getCode(), resp1.getMsg());
				TimeUnit.MILLISECONDS.sleep(500);
				resp1 = messageService.modifyGptAnswerMessageCard(messageId, title, content);
			}
			log.info("重试成功");
		}
	}
}
