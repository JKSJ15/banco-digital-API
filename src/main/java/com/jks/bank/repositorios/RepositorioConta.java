package com.jks.bank.repositorios;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jks.bank.entidades.Conta;

public interface RepositorioConta extends JpaRepository<Conta, Long> {
	Optional<Conta> findByUsuarioId(Long usuarioId);
	
	Optional<Conta> findByUsuarioLogin(String login);

	Optional<Conta> findByChavePix(String chavePix);
}
