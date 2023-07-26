package com.rawchen.feishubot.util.chatgpt;

import com.rawchen.feishubot.entity.gpt.Answer;

public interface AnswerProcess {

	void process(Answer answer) throws Exception;
}
