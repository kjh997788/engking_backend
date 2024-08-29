package com.Ikuzo.EngKing.controller;

import com.Ikuzo.EngKing.entity.ChatMessages;
import com.Ikuzo.EngKing.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/chatmessage")
public class ChatMessageController {

    private final ChatMessageService chatMessageService;

    @PostMapping("/create")
    public ChatMessages addMessageToChatRoom(@PathVariable String chatRoomId, @RequestBody ChatMessages messages) {
        return chatMessageService.addMessageToChatRoom(chatRoomId, messages);
    }
}
