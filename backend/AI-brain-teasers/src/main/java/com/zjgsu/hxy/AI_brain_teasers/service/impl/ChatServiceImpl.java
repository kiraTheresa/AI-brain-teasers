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
                "【核心规则】\n" +
                "1. 当玩家第一次说"开始"时，你必须立即生成一个全新的、从未在任何对话中出现过的脑筋急转弯推理题。\n" +
                "2. 问题必须有独特背景、场景和细节，不得使用常见模板（如掉进洞里、深夜敲门、奇怪电话等网络高频题）。\n" +
                "3. 每次必须加入随机性：随机题材（悬疑、搞笑、生活、校园、科幻等）、随机角色、随机事件。\n" +
                "4. 不得与历史题目重复。（历史题目列表：" + historyQuestions + "）\n" +
                "5. 出题后，玩家开始提问，你只能回答"是 / 否 / 与此无关"。\n" +
                "6. 当玩家输入"退出"时，你必须输出"游戏结束"并给出完整答案。\n\n" +
                "【严格注意】：\n" +
                "- 玩家第一次输入"开始"时，你必须生成问题，绝对不能回答"是"或"否"！\n" +
                "- 只有在玩家提问后，你才能回答"是 / 否 / 与此无关"！\n" +
                "- 游戏必须按照：开始 → 出题 → 问答 → 结束的流程进行！\n" +
                "- 你必须严格遵守角色，不能偏离主持人身份！";

        List<ChatMessage> messages;
        final ChatMessage systemMessage = ChatMessage.builder().role(ChatMessageRole.SYSTEM).content(systemPrompt).build();
        final ChatMessage userMessage = ChatMessage.builder().role(ChatMessageRole.USER).content(userPrompt).build();

        // 初始化或获取聊天历史
        if (!chatHistories.containsKey(roomId)) {
            // 新房间，创建完整的聊天历史
            messages = new ArrayList<>();
            messages.add(systemMessage); // 添加系统提示
            chatHistories.put(roomId, messages);
        } else {
            // 已有房间，获取聊天历史
            messages = chatHistories.get(roomId);
            // 更新系统消息，确保包含最新的历史问题列表
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

        // 添加用户消息
        messages.add(userMessage);

        // 调用AI生成回答
        String answer = aiManager.doChat(messages);
        
        // 如果是新生成的问题，添加到已生成问题集合
        if ("开始".equals(userPrompt) && messages.size() == 2) { // 系统消息 + 开始消息 = 2条
            generatedQuestions.add(answer);
        }

        // 添加AI回复
        final ChatMessage answerMessage = ChatMessage.builder().role(ChatMessageRole.ASSISTANT).content(answer).build();
        messages.add(answerMessage);

        // 检查游戏结束条件
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
