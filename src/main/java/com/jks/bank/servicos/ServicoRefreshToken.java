package com.jks.bank.servicos;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.jks.bank.entidades.RefreshToken;
import com.jks.bank.entidades.Usuario;
import com.jks.bank.exceptions.RefreshTokenInvalidoException;
import com.jks.bank.repositorios.RepositorioRefreshToken;

@Service
public class ServicoRefreshToken {
	private static final Logger log = LoggerFactory.getLogger(ServicoRefreshToken.class);
	private final RepositorioRefreshToken repositorioRefreshToken;
	private final ServicoJwt jwtService;
	private static final String AMARELO = "\u001B[33m";
	private static final String RESETAR = "\u001B[0m";

	public ServicoRefreshToken(RepositorioRefreshToken repositorioRefreshToken, ServicoJwt jwtService) {
		super();
		this.repositorioRefreshToken = repositorioRefreshToken;
		this.jwtService = jwtService;
	}

	public RefreshToken gerarEntidadeRefreshToken(String refreshToken, Usuario usuario) {
		log.debug("criando entidade refresh token para usuário {}", usuario.getUsername());
		jwtService.validarRefreshToken(refreshToken, usuario);
		RefreshToken refresh = RefreshToken.builder().withToken(refreshToken).withUsuario(usuario)
				.withExpiraEm(Instant.now().plus(3, ChronoUnit.DAYS)).build();

		return repositorioRefreshToken.save(refresh);
	}

	public RefreshToken encontrarEntidadeRefreshToken(String refreshToken) {
		return repositorioRefreshToken.findByToken(refreshToken).orElseThrow(() -> {
			log.warn(AMARELO + "refresh token não encontrado" + RESETAR);
			return new RefreshTokenInvalidoException("refresh token inválido!");
		});
	}

	public void validarEntidadeRefreshToken(String refreshToken) {
		log.debug("validando entidade refresh token ");
		RefreshToken refresh = repositorioRefreshToken.findByToken(refreshToken).orElseThrow(() -> {
			log.warn(AMARELO + "refresh token não encontrado" + RESETAR);
			return new RefreshTokenInvalidoException("refresh token inválido!");
		});
		if (refresh.tokenEstaExpirado()) {
			log.warn(AMARELO + "refresh token expirado para usuário {}" + RESETAR, refresh.getUsuario().getUsername());
			throw new RefreshTokenInvalidoException("refresh token inválido!");
		}
	}

	public void deletarPeloUsuario(Usuario usuario) {
		log.debug("removendo refresh tokens do usuário {}", usuario.getUsername());
		repositorioRefreshToken.deleteByUsuario(usuario);
	}
}
