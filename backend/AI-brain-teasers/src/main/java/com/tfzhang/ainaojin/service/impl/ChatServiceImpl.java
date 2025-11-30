package com.tfzhang.ainaojin.service.impl;

import com.tfzhang.ainaojin.model.ChatRoom;
import com.tfzhang.ainaojin.service.AiManager;
import com.tfzhang.ainaojin.service.ChatService;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessage;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessageRole;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service
public class ChatServiceImpl implements ChatService {

    @Resource
    private AiManager aiManager;

    final Map<Long, List<ChatMessage>> chatHistories = new HashMap<>();

    @Override
    public String doChat(long roomId, String userPrompt) {
        String systemPrompt = "你是一位脑筋急转弯游戏主持人，我们将进行一个“是非问答”推理游戏。\n\n" +
                "游戏规则如下：\n\n" +
                "当我说“开始”时，你要随机出一道脑筋急转弯题目（题干简短、有趣、但需要逻辑推理或反向思考）。\n\n" +
                "出题后，你只负责回答我的提问，每次只能回答以下三种之一：\n\n" +
                "是\n\n" +
                "否\n\n" +
                "与此无关\n\n" +
                "在合适的时候，你可以适当引导我，比如说“你离真相更近了”或“你可能忽略了某个细节”。\n\n" +
                "游戏结束条件（满足任一即可）：\n\n" +
                "我说出“不想玩了”、“告诉我答案”、“揭晓答案”等类似表达；\n\n" +
                "我已经基本推理出真相、还原了故事，或所有关键问题都被询问到；\n\n" +
                "我输入“退出”；\n\n" +
                "已经问了 10 个问题，但我仍然没有接近真相或关键线索。\n\n" +
                "结束时你的任务：\n\n" +
                "输出“游戏结束”，并给出本题的正确答案或“汤底”（即故事的完整解释）。\n\n" +
                "如果我表现得不错，可以适当给一句点评或鼓励。\n\n" +
                "准备好后，当我输入“开始”，游戏正式开始。";

        List<ChatMessage> messages = new ArrayList<>();
        final ChatMessage systemMessage = ChatMessage.builder().role(ChatMessageRole.SYSTEM).content(systemPrompt).build();
        final ChatMessage userMessage = ChatMessage.builder().role(ChatMessageRole.USER).content(userPrompt).build();

        if (!chatHistories.containsKey(roomId) && "开始".equals(userPrompt)) {
            chatHistories.put(roomId, messages);
            messages.add(systemMessage);
        } else {
            messages = chatHistories.getOrDefault(roomId, new ArrayList<>());
        }
        messages.add(userMessage);

        String answer = aiManager.doChat(messages);
        final ChatMessage answerMessage = ChatMessage.builder().role(ChatMessageRole.ASSISTANT).content(answer).build();
        messages.add(answerMessage);

        if (answer.contains("游戏结束")) {
            chatHistories.remove(roomId);
        } else {
            chatHistories.put(roomId, messages);
        }

        return answer;
    }

    @Override
    public List<ChatRoom> getChatRoomList() {
        List<ChatRoom> chatRoomList = new ArrayList<>();
        for (Map.Entry<Long, List<ChatMessage>> entry : chatHistories.entrySet()) {
            ChatRoom chatRoom = new ChatRoom();
            chatRoom.setRoomId(entry.getKey());
            chatRoom.setChatMessageList(entry.getValue());
            chatRoomList.add(chatRoom);
        }
        return chatRoomList;
    }
}
