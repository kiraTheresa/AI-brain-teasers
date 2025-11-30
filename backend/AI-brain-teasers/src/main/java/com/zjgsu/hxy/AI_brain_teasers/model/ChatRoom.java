package com.zjgsu.hxy.AI_brain_teasers.model;

import java.util.List;

public class ChatRoom {
    private Long roomId;
    private List<ChatMessage> chatMessageList;

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public List<ChatMessage> getChatMessageList() {
        return chatMessageList;
    }

    public void setChatMessageList(List<ChatMessage> chatMessageList) {
        this.chatMessageList = chatMessageList;
    }
}
