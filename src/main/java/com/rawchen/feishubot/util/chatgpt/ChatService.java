package com.rawchen.feishubot.util.chatgpt;

import cn.hutool.core.util.EscapeUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.rawchen.feishubot.entity.Status;
import com.rawchen.feishubot.entity.gpt.*;
import com.rawchen.feishubot.entity.gptRequestBody.CreateConversationBody;
import com.rawchen.feishubot.util.StringUtil;
import com.rawchen.feishubot.util.Task;
import com.rawchen.feishubot.util.TaskPool;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Semaphore;

@Data
@Slf4j
public class ChatService {

	private static final String ACCOUNT_LEVEL_URL = "/chatgpt/backend-api/models?history_and_training_disabled=false";
	private String account;
	private String password;
	private String accessToken;
	private int level;
	private volatile Status status;
	private Semaphore semaphore = new Semaphore(1);

	private StringBuilder errorString;

	private String proxyUrl;

	private static final String LOGIN_URL = "/chatgpt/login";
	private static final String CHAT_URL = "/v1/chat/completions";
	private static final String LIST_URL = "/chatgpt/backend-api/conversations?offset=0&limit=20";
	private static final String GEN_TITLE_URL = "/chatgpt/backend-api/conversation/gen_title/";


	public ChatService() {
		this.status = Status.FINISHED;
	}

	public ChatService(String account, String password, String accessToken, String proxyUrl) {
		this.accessToken = accessToken;
		this.account = account;
		this.password = password;
		this.proxyUrl = proxyUrl;
		this.status = Status.FINISHED;
		this.level = 3;
	}

	/**
	 * 账户构建：接口登录获取token
	 * 目前转为三方获取，因此无需登录，直接使用token
	 *
	 * @return
	 */
	public boolean build() {
//		if (password == null || password.equals("")) {
//			log.error("账号{}密码为空", account);
//			return false;
//		}
//		log.info("账号{}开始登录", account);
//		String loginUrl = proxyUrl + LOGIN_URL;
//		HashMap<String, Object> paramMap = new HashMap<>();
//		paramMap.put("username", account);
//		paramMap.put("password", password);
//
//		String params = JSONUtil.toJsonPrettyStr(paramMap);
//		String result = HttpUtil.post(loginUrl, params);
//
//		JSONObject jsonObject = new JSONObject(result);
//
//		if (jsonObject.opt("errorMessage") != null) {
//			log.error("账号{}登录失败：{}", account, jsonObject.opt("errorMessage"));
//			return false;
//		}
//		accessTokenaccessToken = jsonObject.optString("accessToken");
//		log.info("账号{}登录成功", account);
		return true;
	}

	public String getToken() {
		return "Bearer " + accessToken;
	}

	private void chat(String content, String model, AnswerProcess process, String parentMessageId, String conversationId) throws InterruptedException {
		semaphore.acquire();
		try {
			String createConversationUrl = proxyUrl + CHAT_URL;
			UUID uuid = UUID.randomUUID();
			String messageId = uuid.toString();

			String param = CreateConversationBody.of(messageId, content, parentMessageId, conversationId, model);
//			log.info("param: {}", param);
			post(param, createConversationUrl, process);
		} finally {
			semaphore.release();
		}
	}

	/**
	 * 新建会话
	 *
	 * @param content 对话内容
	 * @param model   模型
	 * @param process 回调
	 * @throws InterruptedException
	 */
	public void newChat(String content, String model, AnswerProcess process) throws InterruptedException {
		chat(content, model, process, "", "");
	}

	/**
	 * 继续会话
	 *
	 * @param content         对话内容
	 * @param model           模型
	 * @param parentMessageId 父消息id
	 * @param conversationId  会话id
	 * @param process         回调
	 * @throws InterruptedException
	 */
	public void keepChat(String content, String model, String parentMessageId, String conversationId, AnswerProcess process) throws InterruptedException {
		chat(content, model, process, parentMessageId, conversationId);
	}

	public void genTitle(String conversationId) {
		String listUrl = proxyUrl + GEN_TITLE_URL + conversationId;
		HttpResponse response = HttpRequest.get(listUrl).header("Authorization", getToken()).execute();
		log.info(response.body());
	}


	public void getConversationList() {
		String listUrl = proxyUrl + LIST_URL;
		HttpResponse response = HttpRequest.get(listUrl).header("Authorization", getToken()).execute();
	}

	/**
	 * 向gpt发起请求
	 *
	 * @param param   请求参数
	 * @param urlStr  请求的地址
	 * @param process 响应处理器
	 */
	private void post(String param, String urlStr, AnswerProcess process) {
		Answer answer = new Answer();
		try {
			String body = HttpRequest.post(urlStr)
					.header("Authorization", getToken())
					.body(param)
					.execute()
					.body();
//			log.info("Chat API Result: {}", body);
			JSONObject jsonObject = JSONObject.parseObject(body);
			JSONArray choices = jsonObject.getJSONArray("choices");
			if (choices == null || jsonObject.getJSONObject("error") != null) {
				JSONObject error = jsonObject.getJSONObject("error");
				if (error != null) {
					String msg = error.getString("message");
					log.error("Chat API Result Error: {}", body);
					//异步处理
					answer.setSuccess(true);
					answer.setFinished(true);
					answer.setAnswer(msg);
					TaskPool.addTask(new Task(process, answer, this.account));
				}
			} else {
				JSONObject choiceOne = choices.getJSONObject(0);
				JSONObject messageObject = choiceOne.getJSONObject("message");
				String messageContent = messageObject.getString("content");
				log.info("返回结果: {}", StringUtil.escape(messageContent));
				//异步处理
				answer.setSuccess(true);
				answer.setFinished(true);
				answer.setAnswer(messageContent);
				TaskPool.addTask(new Task(process, answer, this.account));
			}



			// 获取并处理响应
//			int status = connection.getResponseCode();
//			Reader streamReader = null;
//			boolean error = false;
//			errorString = new StringBuilder();
//			if (status > 299) {
//				streamReader = new InputStreamReader(connection.getErrorStream());
//				log.error("请求失败，状态码：{}", status);
//				error = true;
//			} else {
//				streamReader = new InputStreamReader(connection.getInputStream());
//			}

//			BufferedReader reader = new BufferedReader(streamReader);
//			String line;

//			int count = 0;
//			while ((line = reader.readLine()) != null) {
//				if (line.length() == 0) {
//					continue;
//				}
//				if (error) {
//					errorString.append(line);
//					log.error(line);
//
//					continue;
//				}
//
//				try {
//					count++;
//					answer = parse(line);
//
//					if (answer == null) {
//						continue;
//					}
//
//					answer.setSeq(count);
//					//每10行 才处理一次 为了防止飞书发消息太快被限制频率
//					if (answer.isSuccess() && !answer.isFinished() && count % 10 != 0) {
//						continue;
//					}
//
//					if (answer.isSuccess() && !answer.getMessage().getAuthor().getRole().equals("assistant")) {
//						continue;
//					}
//
//					//异步处理
//					TaskPool.addTask(new Task(process, answer, this.account));
//				} catch (Exception e) {
//					log.error("解析ChatGpt响应出错", e);
//					log.error(line);
//				}
//			}

//			if (error) {
//				answer = new Answer();
//				answer.setError(errorString.toString());
//				answer.setErrorCode(ErrorCode.RESPONSE_ERROR);
//				answer.setSuccess(false);
//
//				try {
//					JSONObject jsonObject = new JSONObject(errorString.toString());
//					String detail = jsonObject.optString("detail");
//					if (detail != null) {
//						JSONObject detailObject = new JSONObject(detail);
//						String code = detailObject.optString("code");
//						if (code.equals("account_deactivated")) {
//							answer.setErrorCode(ErrorCode.ACCOUNT_DEACTIVATED);
//						}
//					}
//
//
//				} catch (JSONException ignored) {
//				}
//				TaskPool.addTask(new Task(process, answer, this.account));
//			}

//			reader.close();
//			connection.disconnect();
		} catch (Exception e) {
			log.error("请求出错", e);
		}
	}


//	private Answer parse(String body) {
//		System.out.println(body);
//		Answer answer;
//
//		if (body.startsWith("data: [DONE]") || body.startsWith("data: {\"conversation_id\"")) {
//			return null;
//		}
//		if (body.startsWith("data:")) {
//
//			body = body.substring(body.indexOf("{"));
//			answer = JSONUtil.toBean(body, Answer.class);
//			answer.setSuccess(true);
//			if (answer.getMessage().getStatus().equals("finished_successfully")) {
//				answer.setFinished(true);
//			}
//			Message message = answer.getMessage();
//			Content content = message.getContent();
//			List<String> parts = content.getParts();
//			if (parts != null) {
//				String part = parts.get(0);
//				answer.setAnswer(part);
//			}
//			if (content.getText() != null) {
//				answer.setAnswer(content.getText());
//			}
//
//		} else {
//			answer = new Answer();
//			answer.setSuccess(false);
//			JSONObject jsonObject = new JSONObject(body);
//			String detail = jsonObject.optString("detail");
//			if (detail != null && detail.contains("Only one message")) {
//				log.warn("账号{}忙碌中", account);
//				answer.setErrorCode(ErrorCode.BUSY);
//				answer.setError(detail);
//				return answer;
//			}
//			if (detail != null && detail.contains("code")) {
//				JSONObject error = jsonObject.optJSONObject("detail");
//				String code = (String) error.opt("code");
//				if (code.equals("invalid_jwt")) {
//					answer.setErrorCode(ErrorCode.INVALID_JWT);
//				} else if (code.equals("invalid_api_key")) {
//					answer.setErrorCode(ErrorCode.INVALID_API_KEY);
//				} else if (code.equals("model_cap_exceeded")) {
//					answer.setErrorCode(ErrorCode.CHAT_LIMIT);
//				} else {
//					log.error(body);
//					log.warn("账号{} token失效", account);
//				}
//				answer.setError(error.get("message"));
//				return answer;
//			}
//			log.error("未知错误：{}", body);
//			log.error("账号{}未知错误：{}", account, body);
//			answer.setError(body);
//		}
//		return answer;
//	}

	public void refreshToken() {
		build();
	}

	/**
	 * 查询账号可用模型从而判断账号是否plus用户
	 *
	 * @return 查询是否成功 不成功的原因一般为token失效
	 */
	public boolean queryAccountLevel() {
		String url = proxyUrl + ACCOUNT_LEVEL_URL;
		HttpResponse response = HttpRequest.get(url).header("Authorization", getToken()).execute();
		String body = response.body();
		JSONObject jsonObject = JSONObject.parseObject(body);
		String models = jsonObject.getString("models");
		if (models == null || models.length() == 0) {
			log.warn("账号{}查询模型解析失败 : {}", account, body);
			return false;
		}
		cn.hutool.json.JSONArray objects = JSONUtil.parseArray(models);
		List<Model> list = JSONUtil.toList(objects, Model.class);
		boolean plus = false;
		for (Model model : list) {
			Models.modelMap.put(model.getTitle(), model);
			if (model.getSlug().startsWith("gpt-4")) {
				Models.plusModelTitle.add(model.getTitle());
				plus = true;
			} else {
				Models.normalModelTitle.add(model.getTitle());
				Models.NORMAL_DEFAULT_MODEL = model.getTitle();
			}
		}
		this.setLevel(plus ? 4 : 3);
		return true;
	}
}
