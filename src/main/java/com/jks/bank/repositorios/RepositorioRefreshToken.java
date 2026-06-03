package com.jks.bank.repositorios;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jks.bank.entidades.RefreshToken;
import com.jks.bank.entidades.Usuario;

import jakarta.transaction.Transactional;

public interface RepositorioRefreshToken extends JpaRepository<RefreshToken, Long> {
	Optional<RefreshToken> findByToken(String token);

	@Transactional
	void deleteByUsuario(Usuario usuario);
}
