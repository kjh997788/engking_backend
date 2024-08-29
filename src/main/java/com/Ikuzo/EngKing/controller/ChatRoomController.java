package com.Ikuzo.EngKing.controller;

import com.Ikuzo.EngKing.dto.ChatRoomRequestDto;
import com.Ikuzo.EngKing.dto.ChatRoomResponseDto;
import com.Ikuzo.EngKing.entity.ChatMessages;
import com.Ikuzo.EngKing.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/chatroom")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

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

    // 추가적인 엔드포인트 정의...
}
