package com.jks.bank.repositorios;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jks.bank.entidades.RefreshToken;

public interface RepositorioRefreshToken extends JpaRepository<RefreshToken, Long> {
	Optional<RefreshToken> findByRefreshToken(String token);
}
