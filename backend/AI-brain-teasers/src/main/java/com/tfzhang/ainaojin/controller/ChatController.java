package com.tfzhang.ainaojin.controller;

import com.tfzhang.ainaojin.model.ChatRoom;
import com.tfzhang.ainaojin.service.ChatService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
public class ChatController {

    @Resource
    private ChatService chatService;

    @PostMapping("/{roomId}/chat")
    public String doChat(@PathVariable long roomId, @RequestParam String userPrompt) {
        return chatService.doChat(roomId, userPrompt);
    }

    @GetMapping("/rooms")
    public List<ChatRoom> getChatRoomList() {
        return chatService.getChatRoomList();
    }
}
