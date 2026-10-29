package br.com.fiap.tdspo2.cp6_java.service;

import br.com.fiap.tdspo2.cp6_java.model.ChatMensagem;
import br.com.fiap.tdspo2.cp6_java.repository.ChatMensagemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.server.ResponseStatusException;

import javax.naming.LimitExceededException;
import java.util.Map;

public class ChatService {

    @Autowired
    private ChatMensagemRepository chatRepository;

    @Autowired
    private OllamaService ollamaService;
    public ChatMensagem perguntar(Map<String, String> payload){
        String message = payload.get("message");
        if (message == null || message.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Digite uma mensagem.");
        }
        if(message.length()> 280){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Digite uma mensagem com menos de 280 caracteres.");
        }
        message = message.trim();

        String reply = ollamaService.askOllama(message);

        ChatMensagem userMsg = new ChatMensagem("user", message);
        chatRepository.save(userMsg);

        ChatMensagem botMsg = new ChatMensagem("assistant", reply);
        chatRepository.save(botMsg);

        return botMsg;
    }
}
