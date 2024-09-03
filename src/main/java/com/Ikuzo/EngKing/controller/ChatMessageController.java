package com.Ikuzo.EngKing.controller;

import com.Ikuzo.EngKing.dto.ChatMessageRequestDto;
import com.Ikuzo.EngKing.dto.ChatMessageResponseDto;
import com.Ikuzo.EngKing.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/chatmessage")
public class ChatMessageController {

    private final ChatMessageService chatMessageService;

//    @PostMapping("/create")
//    public ChatMessages addMessageToChatRoom(@PathVariable String chatRoomId, @RequestBody ChatMessages messages) {
//        return chatMessageService.addMessageToChatRoom(chatRoomId, messages);
//    }

    @PostMapping("/allmessages")
    public ResponseEntity<List<ChatMessageResponseDto>> selectChatRoomsByChatRoomId(@RequestBody ChatMessageRequestDto chatMessageRequestDto) {
        String memberId = chatMessageRequestDto.getMemberId();
        String chatRoomId = chatMessageRequestDto.getChatRoomId();

        List<ChatMessageResponseDto> chatRoomResponseDtoLists = chatMessageService.selectChatMessagesByChatRoomId(chatRoomId);

        return ResponseEntity.status(HttpStatus.OK).body(chatRoomResponseDtoLists);
    }


}
