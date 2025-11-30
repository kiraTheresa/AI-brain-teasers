package com.zjgsu.hxy.AI_brain_teasers.service;

import com.volcengine.ark.runtime.model.bot.completion.chat.BotChatCompletionRequest;
import com.volcengine.ark.runtime.model.bot.completion.chat.BotChatCompletionResult;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessage;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessageRole;
import com.volcengine.ark.runtime.service.ArkService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import retrofit2.adapter.rxjava2.HttpException;

@Service
public class AiManager {

    @Resource
    private ArkService service;

    @Value("${ai.botId}")
    private String botId;

    public String doChat(String systemPrompt, String userPrompt) {
        final List<ChatMessage> messages = new ArrayList<>();
        final ChatMessage systemMessage = ChatMessage.builder().role(ChatMessageRole.SYSTEM).content(systemPrompt).build();
        final ChatMessage userMessage = ChatMessage.builder().role(ChatMessageRole.USER).content(userPrompt).build();
        messages.add(systemMessage);
        messages.add(userMessage);
        return doChat(messages);
    }

    public String doChat(List<ChatMessage> messages) {
        BotChatCompletionRequest chatCompletionRequest = BotChatCompletionRequest.builder()
                .botId(botId)
                .messages(messages)
                .build();

        try {
            BotChatCompletionResult chatCompletionResult = service.createBotChatCompletion(chatCompletionRequest);
            if (chatCompletionResult.getChoices() == null || chatCompletionResult.getChoices().isEmpty()) {
                throw new RuntimeException("AI没有返回结果");
            }
            return (String) chatCompletionResult.getChoices().get(0).getMessage().getContent();
        } catch (HttpException e) {
            if (e.code() == 401) {
                throw new RuntimeException("鉴权失败：请检查 ai.apiKey 是否有效，以及 botId 是否具备访问权限");
            }
            throw e;
        }
    }
}
