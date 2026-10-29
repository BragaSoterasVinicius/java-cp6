package br.com.fiap.tdspo2.cp6_java.controller;

import br.com.fiap.tdspo2.cp6_java.model.ChatMensagem;
import br.com.fiap.tdspo2.cp6_java.service.ChatService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @PostMapping
    @Transactional
    public ChatMensagem perguntar(@RequestBody Map<String, String> payload){
        return chatService.perguntar(payload);
    }
}
