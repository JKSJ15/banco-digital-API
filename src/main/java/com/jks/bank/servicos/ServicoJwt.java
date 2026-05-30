package com.jks.bank.servicos;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.jks.bank.entidades.Usuario;

@Service
public class ServicoJwt {
	@Value("api.chavesecreta.bancodigital")
	private String CHAVE_SECRETA;
	private Algorithm algorithm = Algorithm.HMAC256(CHAVE_SECRETA);

	// TOKEN ACESSO
	public String criarTokenDeAcesso(Usuario usuario) {
		try {
			return JWT.create().withExpiresAt(criarTempoDeExpiracaoTokenAcesso()).withIssuer("BancoDigitalAPI")
					.withSubject(usuario.getUsername()).sign(algorithm);
		} catch (JWTCreationException e) {
			return null;
		}

	}

	public String validarTokenDeAcesso(String token) {
		try {
			String login = JWT.require(algorithm).withIssuer("BancoDigitalAPI").build().verify(token).getSubject();
			return login;
		} catch (JWTVerificationException e) {
			return null;
		}
	}

	// REFRESH TOKEN
	public String criarRefreshToken(Usuario usuario) {
		try {
			return JWT.create().withExpiresAt(Date.from(criarTempoDeExpiracaoRefreshToken()))
					.withIssuer("BancoDigitalAPI").withSubject(usuario.getUsername()).sign(algorithm);
		} catch (JWTCreationException e) {
			throw new RuntimeException("erro na geração do token");
		}
	}

	public boolean validarRefreshToken(String token, Usuario usuario) {
		try {
			JWT.require(algorithm).withIssuer("BancoDigitalAPI").withSubject(usuario.getUsername()).build().verify(token);
			return true;
		} catch (JWTVerificationException e) {
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
