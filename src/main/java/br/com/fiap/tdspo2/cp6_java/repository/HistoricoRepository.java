package br.com.fiap.tdspo2.cp6_java.repository;
import br.com.fiap.tdspo2.cp6_java.model.Historico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoricoRepository
        extends JpaRepository<Historico, Long> {

    List<Historico> findAllByOrderByDataHoraDesc();
}