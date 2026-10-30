package br.com.fiap.tdspo2.cp6_java.service;

import br.com.fiap.tdspo2.cp6_java.model.Historico;
import br.com.fiap.tdspo2.cp6_java.repository.HistoricoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HistoricoServiceTest {

    @Mock
    private HistoricoRepository repository;

    private HistoricoService historicoService;

    @BeforeEach
    void setUp() {
        historicoService = new HistoricoService();

        // Inject the mock repository into the service
        try {
            var field = HistoricoService.class.getDeclaredField("repository");
            field.setAccessible(true);
            field.set(historicoService, repository);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void shouldSaveHistorico() {
        String pergunta = "O que é Java?";
        String resposta = "Java é uma linguagem de programação.";

        when(repository.save(any(Historico.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Historico resultado = historicoService.salvar(pergunta, resposta);

        assertNotNull(resultado);
        assertEquals(pergunta, resultado.getPergunta());
        assertEquals(resposta, resultado.getResposta());

        verify(repository, times(1)).save(any(Historico.class));
    }

    @Test
    void shouldReturnHistoricoListOrderedByDateDescending() {
        Historico historico1 = new Historico();
        historico1.setPergunta("Pergunta 1");
        historico1.setResposta("Resposta 1");

        Historico historico2 = new Historico();
        historico2.setPergunta("Pergunta 2");
        historico2.setResposta("Resposta 2");

        List<Historico> historicos = List.of(historico1, historico2);

        when(repository.findAllByOrderByDataHoraDesc())
                .thenReturn(historicos);

        List<Historico> resultado = historicoService.listarTodos();

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals(historicos, resultado);

        verify(repository, times(1))
                .findAllByOrderByDataHoraDesc();
    }

    @Test
    void shouldReturnEmptyListWhenNoHistoricoExists() {
        when(repository.findAllByOrderByDataHoraDesc())
                .thenReturn(List.of());

        List<Historico> resultado = historicoService.listarTodos();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());

        verify(repository, times(1))
                .findAllByOrderByDataHoraDesc();
    }
}

