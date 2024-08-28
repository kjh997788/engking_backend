package com.Ikuzo.EngKing.controller;

import com.Ikuzo.EngKing.entity.ChatRoom;
import com.Ikuzo.EngKing.entity.ChatMessages;
import com.Ikuzo.EngKing.service.ChatRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chatroom")
public class ChatRoomController {

    @Autowired
    private ChatRoomService chatRoomService;

    @PostMapping("/create")
    public ChatRoom createChatRoom(@RequestParam String memberId, @RequestParam String topic, @RequestParam String difficulty) {
        return chatRoomService.createChatRoom(memberId, topic, difficulty);
    }

    @PostMapping("/message/{chatRoomId}")
    public ChatMessages addMessageToChatRoom(@PathVariable String chatRoomId, @RequestBody ChatMessages messages) {
        return chatRoomService.addMessageToChatRoom(chatRoomId, messages);
    }


    // 추가적인 엔드포인트 정의...
}
