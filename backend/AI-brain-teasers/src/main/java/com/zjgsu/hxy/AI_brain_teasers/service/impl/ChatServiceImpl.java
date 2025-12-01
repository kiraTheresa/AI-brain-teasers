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

    // 每个房间一套历史题目，避免不同房间互相影响
    private final Map<Long, Set<String>> roomQuestions = new HashMap<>();

    // 每个房间的聊天历史
    private final Map<Long, List<ChatMessage>> chatHistories = new HashMap<>();

    // 每个房间的状态：是否已经出题
    private final Map<Long, Boolean> roomHasQuestion = new HashMap<>();


    @Override
    public String doChat(long roomId, String userPrompt) {

        // 初始化房间数据结构
        roomQuestions.putIfAbsent(roomId, new HashSet<>());
        roomHasQuestion.putIfAbsent(roomId, false);

        // 将历史题目放入 systemPrompt
        String historyQuestions = String.join("\n", roomQuestions.get(roomId));

        String systemPrompt = getSystemPrompt(historyQuestions);

        // 构建 system message
        ChatMessage systemMessage =
                ChatMessage.builder().role(ChatMessageRole.SYSTEM).content(systemPrompt).build();

        // 获取聊天历史
        List<ChatMessage> messages = chatHistories.getOrDefault(roomId, new ArrayList<>());
        messages.removeIf(m -> m.getRole() == ChatMessageRole.SYSTEM);
        messages.add(0, systemMessage);

        // 添加用户消息
        ChatMessage userMessage =
                ChatMessage.builder().role(ChatMessageRole.USER).content(userPrompt).build();
        messages.add(userMessage);


        // 调用 AI
        String answer = aiManager.doChat(messages);

        // ⬅ 正确记录题目（只在“开始”且 AI 返回了一段题目时存储）
        if ("开始".equals(userPrompt)) {
            roomHasQuestion.put(roomId, true);
            roomQuestions.get(roomId).add(answer.trim());
        }

        // 记录 AI 回复
        ChatMessage aiMessage =
                ChatMessage.builder().role(ChatMessageRole.ASSISTANT).content(answer).build();
        messages.add(aiMessage);

        // 保存历史
        if (answer.contains("游戏结束")) {
            chatHistories.remove(roomId);
            roomHasQuestion.remove(roomId);
        } else {
            chatHistories.put(roomId, messages);
        }

        return answer;
    }



    // 分离 systemPrompt 构造
    private String getSystemPrompt(String history) {
        return """
        你是一位脑筋急转弯游戏主持人，我们将进行一个“是非问答”推理游戏。

        【题目生成规则】
        1. 当玩家第一次说“开始”时，你必须立即生成一个全新的、从未在任何对话中出现过的脑筋急转弯推理题。
        2. 题目必须具有独特的背景、场景和细节，不得使用网络高频模板。
        3. 每次出题必须随机选择题材（悬疑、搞笑、生活、校园、科幻等）、随机角色、随机事件。
        4. 不得与历史题目重复。（历史题目列表：
        """ + history + """
        ）
        5. 出题后，你只能回答：是 / 否 / 与此无关。

        【答题规则】
        玩家提问后，你只能回答上述三种之一；必要时可以适度引导。

        【游戏结束规则】
        玩家输入“退出”“揭晓答案”“告诉我答案”等；
        或玩家推理正确；
        或玩家问满 10 个问题。
        游戏结束时，你必须输出“游戏结束”并给出完整解释（汤底）。

        【注意】
        玩家第一次说“开始”时，你必须创建新题目，而不是回答是/否。
        题目必须新鲜、有创意，严禁重复。
        游戏流程：开始 → 出题 → 问答 → 结束。
        """;
    }



    @Override
    public List<ChatRoom> getChatRoomList() {
        List<ChatRoom> list = new ArrayList<>();
        chatHistories.forEach((roomId, history) -> {
            ChatRoom room = new ChatRoom();
            room.setRoomId(roomId);
            room.setChatMessageList(history);
            list.add(room);
        });
        return list;
    }
}
