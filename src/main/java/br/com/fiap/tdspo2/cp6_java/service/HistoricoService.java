package br.com.fiap.tdspo2.cp6_java.service;


import br.com.fiap.tdspo2.cp6_java.model.Historico;
import br.com.fiap.tdspo2.cp6_java.repository.HistoricoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HistoricoService {

    @Autowired
    private HistoricoRepository repository;

    public Historico salvar(String pergunta, String resposta) {
        Historico historico = new Historico();

        historico.setPergunta(pergunta);
        historico.setResposta(resposta);

        return repository.save(historico);
    }

    public List<Historico> listarTodos() {
        return repository.findAllByOrderByDataHoraDesc();
    }
}
