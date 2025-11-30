package com.tfzhang.ainaojin.model;

import com.volcengine.ark.runtime.model.completion.chat.ChatMessage;
import lombok.Data;

import java.util.List;

@Data
public class ChatRoom {
    private Long roomId;
    private List<ChatMessage> chatMessageList;
}
