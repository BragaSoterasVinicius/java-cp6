package br.com.fiap.tdspo2.cp6_java;

import br.com.fiap.tdspo2.cp6_java.config.OllamaConfig;
import br.com.fiap.tdspo2.cp6_java.service.HistoricoService;
import br.com.fiap.tdspo2.cp6_java.service.OllamaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@ExtendWith(MockitoExtension.class)
class OllamaServiceTest {

	@Mock
	private OllamaConfig config;

	@Mock
	private HistoricoService historicoService;

	private RestTemplate restTemplate;
	private MockRestServiceServer server;
	private OllamaService service;

	private static final String BASE_URL = "http://localhost:11434";
	private static final String MODEL = "llama3.1:8b";

	@BeforeEach
	void setUp() {
		restTemplate = new RestTemplate();

		server = MockRestServiceServer
				.bindTo(restTemplate)
				.build();

		when(config.getBaseUrl()).thenReturn(BASE_URL);
		when(config.getModel()).thenReturn(MODEL);

		service = new OllamaService(
				restTemplate,
				config,
				historicoService
		);
	}

	// 1. Successful communication

	@Test
	void sendsConfiguredModelAndExtractsReply() {

		server.expect(requestTo(BASE_URL + "/api/chat"))
				.andExpect(method(HttpMethod.POST))
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.model").value(MODEL))
				.andExpect(jsonPath("$.messages[0].role").value("system"))
				.andExpect(jsonPath("$.messages[1].role").value("user"))
				.andExpect(jsonPath("$.messages[1].content").value("Olá"))
				.andExpect(jsonPath("$.stream").value(false))
				.andExpect(jsonPath("$.options.num_predict").value(300))
				.andExpect(jsonPath("$.options.temperature").value(0.7))
				.andExpect(jsonPath("$.options.top_p").value(0.9))
				.andExpect(jsonPath("$.options.num_ctx").value(4096))
				.andRespond(withSuccess(
						"""
                        {
                            "model": "llama3.1:8b",
                            "message": {
                                "role": "assistant",
                                "content": "Olá!"
                            },
                            "done": true
                        }
                        """,
						MediaType.APPLICATION_JSON
				));

		String result = service.askOllama("Olá");

		assertThat(result).isEqualTo("Olá!");

		verify(historicoService).salvar("Olá", "Olá!");
		server.verify();
	}

	// 2. Empty response

	@Test
	void throwsBadGatewayWhenOllamaReturnsEmptyContent() {

		server.expect(requestTo(BASE_URL + "/api/chat"))
				.andRespond(withSuccess(
						"""
                        {
                            "model": "llama3.1:8b",
                            "message": {
                                "role": "assistant",
                                "content": ""
                            },
                            "done": true
                        }
                        """,
						MediaType.APPLICATION_JSON
				));

		assertThatThrownBy(() -> service.askOllama("Olá"))
				.isInstanceOf(ResponseStatusException.class)
				.satisfies(exception -> {
					ResponseStatusException ex =
							(ResponseStatusException) exception;

					assertThat(ex.getStatusCode())
							.isEqualTo(HttpStatus.BAD_GATEWAY);

					assertThat(ex.getReason())
							.contains("resposta vazia");
				});

		verifyNoInteractions(historicoService);
		server.verify();
	}

	// 3. Missing response content

	@Test
	void throwsBadGatewayWhenOllamaReturnsIncompleteJson() {

		server.expect(requestTo(BASE_URL + "/api/chat"))
				.andRespond(withSuccess(
						"""
                        {
                            "model": "llama3.1:8b",
                            "done": true
                        }
                        """,
						MediaType.APPLICATION_JSON
				));

		assertThatThrownBy(() -> service.askOllama("Olá"))
				.isInstanceOf(ResponseStatusException.class)
				.satisfies(exception -> {
					ResponseStatusException ex =
							(ResponseStatusException) exception;

					assertThat(ex.getStatusCode())
							.isEqualTo(HttpStatus.BAD_GATEWAY);
				});

		verifyNoInteractions(historicoService);
		server.verify();
	}

	// 4. HTTP 404 - Model not found

	@Test
	void handlesModelNotFound() {

		server.expect(requestTo(BASE_URL + "/api/chat"))
				.andRespond(withStatus(HttpStatus.NOT_FOUND));

		assertThatThrownBy(() -> service.askOllama("Olá"))
				.isInstanceOf(ResponseStatusException.class)
				.satisfies(exception -> {
					ResponseStatusException ex =
							(ResponseStatusException) exception;

					assertThat(ex.getStatusCode())
							.isEqualTo(HttpStatus.BAD_GATEWAY);

					assertThat(ex.getReason())
							.contains("Modelo não encontrado");
				});

		verifyNoInteractions(historicoService);
		server.verify();
	}

	// 5. HTTP 400 - Invalid request

	@Test
	void handlesInvalidRequest() {

		server.expect(requestTo(BASE_URL + "/api/chat"))
				.andRespond(withStatus(HttpStatus.BAD_REQUEST));

		assertThatThrownBy(() -> service.askOllama("Olá"))
				.isInstanceOf(ResponseStatusException.class)
				.satisfies(exception -> {
					ResponseStatusException ex =
							(ResponseStatusException) exception;

					assertThat(ex.getStatusCode())
							.isEqualTo(HttpStatus.BAD_GATEWAY);

					assertThat(ex.getReason())
							.contains("Requisição inválida");
				});

		verifyNoInteractions(historicoService);
		server.verify();
	}

	// 6. HTTP 500 - Internal server error

	@Test
	void handlesOllamaInternalServerError() {

		server.expect(requestTo(BASE_URL + "/api/chat"))
				.andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

		assertThatThrownBy(() -> service.askOllama("Olá"))
				.isInstanceOf(ResponseStatusException.class)
				.satisfies(exception -> {
					ResponseStatusException ex =
							(ResponseStatusException) exception;

					assertThat(ex.getStatusCode())
							.isEqualTo(HttpStatus.BAD_GATEWAY);

					assertThat(ex.getReason())
							.contains("erro ao processar");
				});

		verifyNoInteractions(historicoService);
		server.verify();
	}

	// 7. Connection failure

	@Test
	void handlesOllamaConnectionFailure() {

		server.expect(requestTo(BASE_URL + "/api/chat"))
				.andRespond(request -> {
					throw new IOException("Connection refused");
				});

		assertThatThrownBy(() -> service.askOllama("Olá"))
				.isInstanceOf(ResponseStatusException.class)
				.satisfies(exception -> {
					ResponseStatusException ex =
							(ResponseStatusException) exception;

					assertThat(ex.getStatusCode())
							.isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);

					assertThat(ex.getReason())
							.contains("Não foi possível conectar");
				});

		verifyNoInteractions(historicoService);
		server.verify();
	}
}
