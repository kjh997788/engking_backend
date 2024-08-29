package com.Ikuzo.EngKing.controller;

import com.Ikuzo.EngKing.dto.ChatRoomRequestDto;
import com.Ikuzo.EngKing.dto.ChatRoomResponseDto;
import com.Ikuzo.EngKing.entity.ChatRoom;
import com.Ikuzo.EngKing.entity.ChatMessages;
import com.Ikuzo.EngKing.service.ChatRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chatroom")
public class ChatRoomController {

    @Autowired
    private ChatRoomService chatRoomService;

    @PostMapping("/create")
    public ResponseEntity<ChatRoomResponseDto> createChatRoom(@RequestBody ChatRoomRequestDto chatRoomRequestDto) {
        String memberId = chatRoomRequestDto.getMemberId();
        String topic = chatRoomRequestDto.getTopic();
        String difficulty = chatRoomRequestDto.getDifficulty();

        ChatRoomResponseDto chatRoomResponseDto = chatRoomService.createChatRoom(memberId, topic, difficulty);

        if (chatRoomResponseDto.isSuccess())
            return ResponseEntity.status(HttpStatus.CREATED).body(chatRoomResponseDto);
        else
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(chatRoomResponseDto);
    }

    @PostMapping("/message/{chatRoomId}")
    public ChatMessages addMessageToChatRoom(@PathVariable String chatRoomId, @RequestBody ChatMessages messages) {
        return chatRoomService.addMessageToChatRoom(chatRoomId, messages);
    }


    // 추가적인 엔드포인트 정의...
}
