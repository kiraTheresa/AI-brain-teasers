package com.zjgsu.hxy.AI_brain_teasers.service.impl;

import com.zjgsu.hxy.AI_brain_teasers.model.ChatRoom;
import com.zjgsu.hxy.AI_brain_teasers.service.AiManager;
import com.zjgsu.hxy.AI_brain_teasers.service.ChatService;
import com.zjgsu.hxy.AI_brain_teasers.model.ChatMessage;
import com.zjgsu.hxy.AI_brain_teasers.model.ChatMessageRole;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service
public class ChatServiceImpl implements ChatService {

    @Resource
    private AiManager aiManager;

    // 存储已生成的问题，避免重复
    private final Set<String> generatedQuestions = new HashSet<>();

    final Map<Long, List<ChatMessage>> chatHistories = new HashMap<>();

    @Override
    public String doChat(long roomId, String userPrompt) {
        // 将已生成的问题列表转换为字符串，用于提示AI避免重复
        String historyQuestions = String.join("\n", generatedQuestions);
        
        String systemPrompt = "你是一位脑筋急转弯游戏主持人，我们将进行一个“是非问答”推理游戏。\n\n" +
                "【题目生成要求】\n" +
                "1. 当我说“开始”时，你必须生成一个全新的、从未在任何对话中出现过的脑筋急转弯推理题。\n" +
                "2. 每道题目必须有独特背景、场景和细节，不得使用任何常见模板（如掉进洞里、深夜敲门、奇怪电话等网络高频题）。\n" +
                "3. 每次必须加入随机性：随机题材（悬疑、搞笑、生活、校园、科幻等）、随机角色、随机事件。\n" +
                "4. 不得与历史题目重复。（历史题目列表：" + historyQuestions + "）\n\n" +
                "【答题规则】\n" +
                "出题后，你只回答“是 / 否 / 与此无关”\n" +
                "必要时可以提示方向，但不得直接给出关键线索。\n\n" +
                "【游戏结束条件】\n" +
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
            // 更新系统消息的内容，确保包含最新的历史问题列表
            boolean hasSystemMessage = false;
            for (int i = 0; i < messages.size(); i++) {
                if (messages.get(i).getRole() == ChatMessageRole.SYSTEM) {
                    messages.set(i, systemMessage);
                    hasSystemMessage = true;
                    break;
                }
            }
            if (!hasSystemMessage) {
                messages.add(0, systemMessage);
            }
        }
        messages.add(userMessage);

        // 调用AI生成回答，确保answer变量被初始化
        String answer = aiManager.doChat(messages);
        
        // 如果是新生成的问题，添加到已生成问题集合
        if ("开始".equals(userPrompt) && messages.size() >= 2) {
            generatedQuestions.add(answer);
        }

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
