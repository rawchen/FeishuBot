package com.rawchen.feishubot.service;

import com.lark.oapi.Client;
import com.lark.oapi.service.contact.v3.model.GetUserReq;
import com.lark.oapi.service.contact.v3.model.GetUserResp;
import com.lark.oapi.service.contact.v3.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
	protected final Client client;

	public User getUserByOpenId(String openId) throws Exception {
		// 创建请求对象
		GetUserReq req = GetUserReq.newBuilder()
				.userId(openId)
				.userIdType("open_id")
				.departmentIdType("open_department_id")
				.build();
		// 发起请求
		GetUserResp resp = client.contact().user().get(req);

		// 处理服务端错误
		if (!resp.success()) {
			log.error("code:{},msg:{},reqId:{}"
					, resp.getCode(), resp.getMsg(), resp.getRequestId());
			throw new Exception("获取用户信息失败");
		}
		return resp.getData().getUser();
	}
}
