package com.jks.bank.repositorios;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jks.bank.entidades.Usuario;

public interface RepositorioUsuario extends JpaRepository<Usuario, Long> {
	Optional<Usuario> findByLogin(String login);
	boolean existsByCpf(String cpf);
	boolean existsByTelefone(String telefone);
}
