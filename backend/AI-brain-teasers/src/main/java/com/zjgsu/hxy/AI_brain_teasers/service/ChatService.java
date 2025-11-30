package com.zjgsu.hxy.AI_brain_teasers.service;

import com.zjgsu.hxy.AI_brain_teasers.model.ChatRoom;

import java.util.List;

public interface ChatService {
    String doChat(long roomId, String userPrompt);
    List<ChatRoom> getChatRoomList();
}
