package br.com.fiap.tdspo2.cp6_java.service;


import br.com.fiap.tdspo2.cp6_java.model.ChatMensagem;
import br.com.fiap.tdspo2.cp6_java.repository.ChatMensagemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private OllamaService ollamaService;

    @Mock
    private ChatMensagemRepository chatMensagemRepository;

    @InjectMocks
    private ChatService chatService;

    // 1. Valid message

    @Test
    void acceptsValidMessage() {

        when(ollamaService.askOllama("Olá"))
                .thenReturn("Olá! Como posso ajudar?");

        ChatMensagem result = chatService.perguntar(
                Map.of("message", "Olá")
        );

        assertThat(result).isNotNull();

        verify(ollamaService).askOllama("Olá");
    }

    // 2. Null message

    @Test
    void rejectsNullMessage() {

        Map<String, String> payload = new java.util.HashMap<>();
        payload.put("message", null);

        assertThatThrownBy(() -> chatService.perguntar(payload))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(exception -> {
                    ResponseStatusException ex =
                            (ResponseStatusException) exception;

                    assertThat(ex.getStatusCode())
                            .isEqualTo(HttpStatus.BAD_REQUEST);

                    assertThat(ex.getReason())
                            .isEqualTo("Digite uma mensagem.");
                });

        verifyNoInteractions(ollamaService);
    }

    // 3. Empty message

    @Test
    void rejectsEmptyMessage() {

        assertThatThrownBy(() ->
                chatService.perguntar(Map.of("message", ""))
        )
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(exception -> {
                    ResponseStatusException ex =
                            (ResponseStatusException) exception;

                    assertThat(ex.getStatusCode())
                            .isEqualTo(HttpStatus.BAD_REQUEST);
                });

        verifyNoInteractions(ollamaService);
    }

    // 4. Blank message

    @Test
    void rejectsBlankMessage() {

        assertThatThrownBy(() ->
                chatService.perguntar(Map.of("message", "   "))
        )
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(exception -> {
                    ResponseStatusException ex =
                            (ResponseStatusException) exception;

                    assertThat(ex.getStatusCode())
                            .isEqualTo(HttpStatus.BAD_REQUEST);

                    assertThat(ex.getReason())
                            .isEqualTo("Digite uma mensagem.");
                });

        verifyNoInteractions(ollamaService);
    }

    // 5. Message exceeding 280 characters

    @Test
    void rejectsMessageLongerThan280Characters() {

        String longMessage = "a".repeat(281);

        assertThatThrownBy(() ->
                chatService.perguntar(Map.of("message", longMessage))
        )
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(exception -> {
                    ResponseStatusException ex =
                            (ResponseStatusException) exception;

                    assertThat(ex.getStatusCode())
                            .isEqualTo(HttpStatus.BAD_REQUEST);

                    assertThat(ex.getReason())
                            .isEqualTo(
                                    "Digite uma mensagem com menos de 280 caracteres."
                            );
                });

        verifyNoInteractions(ollamaService);
    }

    // 6. Message with exactly 280 characters

    @Test
    void acceptsMessageWithExactly280Characters() {

        String message = "a".repeat(280);

        when(ollamaService.askOllama(message))
                .thenReturn("Resposta do Ollama");

        chatService.perguntar(Map.of("message", message));

        verify(ollamaService).askOllama(message);
    }

    // 7. Trims whitespace

    @Test
    void trimsMessageBeforeSendingToOllama() {

        when(ollamaService.askOllama("Olá"))
                .thenReturn("Olá!");

        chatService.perguntar(
                Map.of("message", "   Olá   ")
        );

        verify(ollamaService).askOllama("Olá");
    }
}