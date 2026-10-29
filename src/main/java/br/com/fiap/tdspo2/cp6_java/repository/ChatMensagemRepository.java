package br.com.fiap.tdspo2.cp6_java.repository;

import br.com.fiap.tdspo2.cp6_java.model.ChatMensagem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMensagemRepository extends JpaRepository<ChatMensagem, Long> {
}
