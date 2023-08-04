package com.rawchen.feishubot.service;

import com.lark.oapi.Client;
import com.lark.oapi.service.im.v1.model.*;
import com.rawchen.feishubot.util.MessageCard;
import com.rawchen.feishubot.util.MessageContent;
import com.rawchen.feishubot.util.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
/**
 * 用于飞书机器人发送消息
 */
public class MessageService {
	protected final Client client;


	private CreateMessageReq createMessageReq(String receiveIdType, String receiveId, String msgType, String content) {

		return CreateMessageReq.newBuilder()
				.receiveIdType(receiveIdType)
				.createMessageReqBody(CreateMessageReqBody.newBuilder()
						.receiveId(receiveId)
						.msgType(msgType)
						.content(content)
						.uuid(UUID.randomUUID().toString())
						.build())
				.build();
	}

	private PatchMessageReq createPatchMessageReq(String messageId, String content) {

		// 目前消息卡片不支持“```”代码块样式输出，因此可能会将代码块中内容误识别xss注入后删掉
		// https://open.feishu.cn/document/common-capabilities/message-card/message-cards-content/using-markdown-tags
		content = StringUtil.replaceSpecialSymbol(content);

		return PatchMessageReq.newBuilder()
				.messageId(messageId)
				.patchMessageReqBody(PatchMessageReqBody.newBuilder()
						.content(content)
						.build())
				.build();
	}

	/**
	 * 发送文本消息
	 *
	 * @param chatId 用于表示聊天的id
	 * @param text   发送的文本
	 * @return
	 * @throws Exception
	 */
	public CreateMessageResp sendTextMessageByChatId(String chatId, String text) throws Exception {
		CreateMessageReq req = createMessageReq("chat_id", chatId, "text", MessageContent.ofText(text));
		return getCreateMessageResp(req);
	}

	private CreateMessageResp getCreateMessageResp(CreateMessageReq req) throws Exception {
		CreateMessageResp resp = null;

		resp = client.im().message().create(req);
		if (!resp.success()) {
			log.error(String.format("code:%s,msg:%s,reqId:%s"
					, resp.getCode(), resp.getMsg(), resp.getRequestId()));
			throw new Exception("发送text消息失败");
		}
		return resp;
	}

	/**
	 * 发送卡片消息
	 *
	 * @param chatId
	 * @param text
	 * @return
	 * @throws Exception
	 */
	public CreateMessageResp sendCardMessage(String chatId, String text) throws Exception {
		CreateMessageReq messageReq = createMessageReq("chat_id", chatId, "interactive", text);
		return getCreateMessageResp(messageReq);
	}

	/**
	 * 发送用户ChatGpt回复消息卡片的消息
	 *
	 * @param chatId
	 * @param title
	 * @param answer
	 * @return
	 * @throws Exception
	 */
	public CreateMessageResp sendGptAnswerMessage(String chatId, String title, String answer) throws Exception {
		return sendCardMessage(chatId, MessageCard.ofGptAnswerMessageCard(title, answer));
	}

	public CreateMessageResp sendGptAnswerMessageWithSelection(String chatId, String title, String answer, Map<String, String> selections) throws Exception {
		return sendCardMessage(chatId, MessageCard.ofGptAnswerMessageCardWithSelection(title, answer, selections));
	}

	/**
	 * 修改类型为消息卡片的消息
	 *
	 * @param messageId
	 * @param content
	 * @return
	 * @throws Exception
	 */
	public PatchMessageResp modifyMessageCard(String messageId, String content) throws Exception {
		PatchMessageReq req = createPatchMessageReq(messageId, content);
		return client.im().message().patch(req);
	}

	/**
	 * 修改用于ChatGpt回复的消息卡片的消息
	 *
	 * @param messageId
	 * @param title
	 * @param answer
	 * @return
	 * @throws Exception
	 */
	public PatchMessageResp modifyGptAnswerMessageCard(String messageId, String title, String answer) throws Exception {
		return modifyMessageCard(messageId, MessageCard.ofGptAnswerMessageCard(title, answer));
	}


	public PatchMessageResp modifyGptAnswerMessageCardWithSelection(String messageId, String title, String answer, Map<String, String> selections) throws Exception {
		return modifyMessageCard(messageId, MessageCard.ofGptAnswerMessageCardWithSelection(title, answer, selections));
	}


}
