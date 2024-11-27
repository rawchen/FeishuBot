package com.rawchen.feishubot.handler;

import cn.hutool.core.date.DateUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONUtil;
import com.lark.oapi.core.request.EventReq;
import com.lark.oapi.event.CustomEventHandler;
import com.lark.oapi.event.EventDispatcher;
import com.lark.oapi.service.im.ImService;
import com.lark.oapi.service.im.v1.model.P2MessageReceiveV1;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * 事件处理器
 *
 * @author shuangquan.chen
 * @date 2024-04-29 16:15
 */
@Slf4j
@Component
@Data
public class EventHandler {

    public static final EventDispatcher EVENT_HANDLER = EventDispatcher.newBuilder("", "")
            .onP2MessageReceiveV1(new ImService.P2MessageReceiveV1Handler() {
                @Override
                public void handle(P2MessageReceiveV1 event) {
                    //处理消息事件
                    try {
//							log.info("收到消息: {}", Jsons.DEFAULT.toJson(event));
                        MessageHandler messageHandler = SpringUtil.getBean(MessageHandler.class);
//                        log.info("event: {}", JSONUtil.toJsonStr(event));
                        messageHandler.process(event);
                    } catch (Exception e) {
                        log.error("处理消息失败", e);
                        throw new RuntimeException(e);
                    }
                }
            })
            .build();
}
