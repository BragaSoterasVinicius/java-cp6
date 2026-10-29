package br.com.fiap.tdspo2.cp6_java.controller;

import br.com.fiap.tdspo2.cp6_java.model.ChatMensagem;
import br.com.fiap.tdspo2.cp6_java.model.Historico;
import br.com.fiap.tdspo2.cp6_java.service.ChatService;
import br.com.fiap.tdspo2.cp6_java.service.HistoricoService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private HistoricoService historicoService;

    @GetMapping("/historico")
    public List<Historico> listar() {
        return historicoService.listarTodos();
    }

    @PostMapping("/chat")
    @Transactional
    public ChatMensagem perguntar(@RequestBody Map<String, String> payload){
        return chatService.perguntar(payload);
    }
}
