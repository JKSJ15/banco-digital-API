package com.jks.bank.repositorios;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jks.bank.entidades.Conta;
import com.jks.bank.entidades.Usuario;

public interface RepositorioConta extends JpaRepository<Conta, Long> {
	Optional<Conta> findByUsuario(Usuario usuario);
}
