package br.com.fiap.tdspo2.cp6_java.service;


import br.com.fiap.tdspo2.cp6_java.config.OllamaConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;

@Service
public class OllamaService {

    private final RestTemplate restTemplate;
    private final OllamaConfig config;
    private final HistoricoService historicoService;

    public OllamaService(
            RestTemplate restTemplate,
            OllamaConfig config,
            HistoricoService historicoService) {

        this.restTemplate = restTemplate;
        this.config = config;
        this.historicoService = historicoService;
    }
    public String askOllama(String userMessage) {

        Map<String, Object> body = Map.of(
                "model", config.getModel(),
                "messages", List.of(
                        Map.of(
                                "role", "system",
                                "content", "Você é um assistente prestativo especialista em literatura clássica. Responda em português brasileiro. "
                        ),
                        Map.of(
                                "role", "user",
                                "content", userMessage
                        )
                ),
                //limites
                "stream", false,
                "options", Map.of(
                        "num_predict", 300,
                        "temperature", 0.7,
                        "top_p", 0.9,
                        "num_ctx", 4096
                )
        );
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            JsonNode response = restTemplate.postForObject(
                    config.getBaseUrl() + "/api/chat",
                    new HttpEntity<>(body, headers),
                    JsonNode.class
            );

            JsonNode content = response == null
                    ? null
                    : response.path("message").path("content");

            if (content == null || !content.isTextual()
                    || content.asText().isBlank()) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "O Ollama retornou uma resposta vazia."
                );
            }

            String resposta = content.asText();

            historicoService.salvar(userMessage, resposta);

            return resposta;

        } catch (RestClientResponseException e) {

            String reason = switch (e.getStatusCode().value()) {
                case 404 -> "Modelo não encontrado no Ollama. Verifique se ele foi baixado.";
                case 400 -> "Requisição inválida para o Ollama.";
                case 500 -> "O Ollama encontrou um erro ao processar a requisição.";
                default -> "O Ollama não conseguiu responder. Tente novamente.";
            };

            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY, reason
            );

        } catch (RestClientException e) {

            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Não foi possível conectar ao Ollama. Verifique se ele está em execução."
            );
        }
    }
}
