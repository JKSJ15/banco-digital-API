package com.jks.bank.servicos;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Service;

import com.jks.bank.entidades.RefreshToken;
import com.jks.bank.entidades.Usuario;
import com.jks.bank.exceptions.RefreshTokenInvalidoException;
import com.jks.bank.repositorios.RepositorioRefreshToken;

@Service
public class ServicoRefreshToken {
	private final RepositorioRefreshToken repositorioRefreshToken;
	private final ServicoJwt jwtService;

	public ServicoRefreshToken(RepositorioRefreshToken repositorioRefreshToken, ServicoJwt jwtService) {
		super();
		this.repositorioRefreshToken = repositorioRefreshToken;
		this.jwtService = jwtService;
	}

	public RefreshToken gerarEntidadeRefreshToken(String refreshToken, Usuario usuario) {
		jwtService.validarRefreshToken(refreshToken, usuario);
		RefreshToken refresh = RefreshToken.builder().withToken(refreshToken).withUsuario(usuario)
				.withExpiraEm(Instant.now().plus(3, ChronoUnit.DAYS)).build();

		return repositorioRefreshToken.save(refresh);
	}

	public RefreshToken encontrarEntidadeRefreshToken(String refreshToken) {
		return repositorioRefreshToken.findByRefreshToken(refreshToken)
				.orElseThrow(() -> new RefreshTokenInvalidoException("refresh token inválido!"));
	}
	
	public void validarEntidadeRefreshToken(String refreshToken) {
		RefreshToken refresh = repositorioRefreshToken.findByRefreshToken(refreshToken)
				.orElseThrow(() -> new RefreshTokenInvalidoException("refresh token inválido!"));
		if(refresh.tokenEstaExpirado()) {
			throw new RefreshTokenInvalidoException("refresh token inválido!");
			}
	}
}
