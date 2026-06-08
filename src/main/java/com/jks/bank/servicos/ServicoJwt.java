package com.jks.bank.servicos;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.jks.bank.entidades.Usuario;

@Service
public class ServicoJwt {
	private static final Logger log = LoggerFactory.getLogger(ServicoJwt.class);
	private Algorithm algorithm;

	public ServicoJwt(@Value("${api.security.token.secret}") String CHAVE_SECRETA) {
		this.algorithm = Algorithm.HMAC256(CHAVE_SECRETA);
	}

	// TOKEN ACESSO
	public String criarTokenDeAcesso(Usuario usuario) {
		try {
			log.debug("criando token de acesso para usuário {}", usuario.getUsername());
			return JWT.create().withJWTId(UUID.randomUUID().toString()).withIssuedAt(new Date())
					.withExpiresAt(criarTempoDeExpiracaoTokenAcesso()).withIssuer("BancoDigitalAPI")
					.withSubject(usuario.getUsername()).sign(algorithm);
		} catch (JWTCreationException e) {
			log.error("erro ao gerar token de acesso", e);
			return null;
		}

	}

	public String validarTokenDeAcesso(String token) {
		try {
			String login = JWT.require(algorithm).withIssuer("BancoDigitalAPI").build().verify(token).getSubject();
			return login;
		} catch (JWTVerificationException e) {
			log.warn("token de acesso inválido: {}", e.getMessage());
			return null;
		}
	}

	// REFRESH TOKEN
	public String criarRefreshToken(Usuario usuario) {
		try {
			log.debug("criando refresh token para usuário {}", usuario.getUsername());
			return JWT.create().withJWTId(UUID.randomUUID().toString()).withIssuedAt(new Date())
					.withExpiresAt(Date.from(criarTempoDeExpiracaoRefreshToken())).withIssuer("BancoDigitalAPI")
					.withSubject(usuario.getUsername()).sign(algorithm);
		} catch (JWTCreationException e) {
			log.error("erro ao gerar refresh token", e);
			throw new RuntimeException("erro na geração do token");
		}
	}

	public boolean validarRefreshToken(String token, Usuario usuario) {
		try {
			log.debug("validando refresh token para usuário {}", usuario.getUsername());
			JWT.require(algorithm).withIssuer("BancoDigitalAPI").withSubject(usuario.getUsername()).build()
					.verify(token);
			return true;
		} catch (JWTVerificationException e) {
			log.warn("erro ao validar refresh token: {}", e.getMessage());
			return false;
		}
	}

	// METODOS INTERNOS
	private Instant criarTempoDeExpiracaoTokenAcesso() {
		return Instant.now().plus(15, ChronoUnit.MINUTES);
	}

	private Instant criarTempoDeExpiracaoRefreshToken() {
		return Instant.now().plus(3, ChronoUnit.DAYS);
	}
}
