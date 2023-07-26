package com.rawchen.feishubot;

import com.lark.oapi.service.im.v1.model.CreateMessageResp;
import com.lark.oapi.service.im.v1.model.PatchMessageResp;
import com.rawchen.feishubot.service.MessageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class FeishuBotApplicationTests {

    @Autowired
    MessageService messageService;

    @Test
    void contextLoads() {
    }

    @Test
    void test01() throws Exception {
        messageService.sendTextMessageByChatId("oc_e44b7351982677b1d768c3730cc37c13", "123");
    }

    @Test
    void test02() throws Exception {
        CreateMessageResp createMessageResp = messageService.sendGptAnswerMessage("oc_e44b7351982677b1d768c3730cc37c13", "title123", "12345");
        System.out.println(createMessageResp.getData().getMessageId());
    }

    @Test
    void test03() throws Exception {
        messageService.modifyMessageCard("om_ff84ab31f48201d498f24aca3aa5f23e", "34235235");
    }

    @Test
    void test04() throws Exception {
        String i = "A";
        while (true) {
            String temp = i + "A";
            PatchMessageResp patchMessageResp = messageService.modifyGptAnswerMessageCard("om_ff84ab31f48201d498f24aca3aa5f23e", "title123", temp);
            System.out.println(patchMessageResp.getMsg());
            i = temp;
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

}
