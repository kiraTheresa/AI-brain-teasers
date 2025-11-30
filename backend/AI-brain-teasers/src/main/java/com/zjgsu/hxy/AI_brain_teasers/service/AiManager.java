package com.zjgsu.hxy.AI_brain_teasers.service;

import com.zjgsu.hxy.AI_brain_teasers.model.ChatMessage;
import com.zjgsu.hxy.AI_brain_teasers.model.ChatMessageRole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiManager {

    @Resource
    private RestTemplate restTemplate;

    @Value("${deepseek.api-key}")
    private String apiKey;

    @Value("${deepseek.base-url:https://api.deepseek.com/v1}")
    private String baseUrl;

    @Value("${deepseek.model:deepseek-chat}")
    private String model;

    public String doChat(String systemPrompt, String userPrompt) {
        final List<ChatMessage> messages = new ArrayList<>();
        final ChatMessage systemMessage = ChatMessage.builder().role(ChatMessageRole.SYSTEM).content(systemPrompt).build();
        final ChatMessage userMessage = ChatMessage.builder().role(ChatMessageRole.USER).content(userPrompt).build();
        messages.add(systemMessage);
        messages.add(userMessage);
        return doChat(messages);
    }

    public String doChat(List<ChatMessage> messages) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        List<Map<String, String>> msgList = new ArrayList<>();
        for (ChatMessage m : messages) {
            Map<String, String> item = new HashMap<>();
            item.put("role", toApiRole(m.getRole()));
            item.put("content", m.getContent());
            msgList.add(item);
        }
        body.put("messages", msgList);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ChatCompletionResponse response = restTemplate.postForObject(
                    baseUrl + "/chat/completions",
                    entity,
                    ChatCompletionResponse.class
            );
            if (response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
                throw new RuntimeException("AI没有返回结果");
            }
            return response.getChoices().get(0).getMessage().getContent();
        } catch (HttpClientErrorException.Unauthorized e) {
            throw new RuntimeException("鉴权失败：请检查 deepseek.api-key 是否有效");
        }
    }

    private String toApiRole(ChatMessageRole role) {
        switch (role) {
            case SYSTEM:
                return "system";
            case USER:
                return "user";
            case ASSISTANT:
                return "assistant";
            default:
                return "user";
        }
    }

    public static class ChatCompletionResponse {
        private List<Choice> choices;

        public List<Choice> getChoices() {
            return choices;
        }

        public void setChoices(List<Choice> choices) {
            this.choices = choices;
        }
    }

    public static class Choice {
        private Message message;

        public Message getMessage() {
            return message;
        }

        public void setMessage(Message message) {
            this.message = message;
        }
    }

    public static class Message {
        private String content;

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
}
