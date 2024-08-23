package com.Ikuzo.EngKing.controller;

import com.Ikuzo.EngKing.dto.ConversationstartResponseDto;
import com.Ikuzo.EngKing.dto.ConversatoinStartRequestDto;
import com.Ikuzo.EngKing.service.ConversationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/conversation")
public class ConversationController {


//    @PostMapping("/start")
//    public ResponseEntity<ConversationstartResponseDto> conversationStart(@RequestBody ConversatoinStartRequestDto conversatoinStartRequestDto) throws Exception{
//        ConversationstartResponseDto conversatoinStartResponseDto = ConversationService;
//        return ResponseEntity.ok(conversatoinStartResponseDto);
//    }


}
