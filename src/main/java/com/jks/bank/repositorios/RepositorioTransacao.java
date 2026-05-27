package com.jks.bank.repositorios;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import com.jks.bank.entidades.TipoTransacao;
import com.jks.bank.entidades.Transacao;

public interface RepositorioTransacao extends JpaRepository<Transacao, Long> {
	Optional<Transacao> findByTipo(TipoTransacao tipoTransacao);

	Optional<Transacao> findByData(LocalDateTime dataTransacao);
	
	Page<Transacao> findByContaOrigemAndContaDestino(Long contaOrigemId);
}
