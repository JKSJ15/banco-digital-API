package com.jks.bank.repositorios;

import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.jks.bank.entidades.Conta;
import com.jks.bank.entidades.RefreshToken;
import com.jks.bank.entidades.Usuario;
import com.jks.bank.servicos.ServicoJwt;
import com.jks.bank.servicos.ServicoRefreshToken;
import com.jks.bank.util.RetornaEntidades;

@DataJpaTest
@Import({ServicoRefreshToken.class, ServicoJwt.class})
public class TesteRepositorioRefreshToken {
	@Autowired
	RepositorioUsuario repositorioUsuario;
	@Autowired
	RepositorioConta repositorioConta;
	@Autowired
	RepositorioRefreshToken repositorioRefreshToken;
	@Autowired
	ServicoRefreshToken servicoRefreshToken;
	@Autowired
	ServicoJwt servicoJwt;

	Usuario usuario;
	Conta conta;

	RefreshToken refreshTokenEntidade;
	String refreshToken;

	@BeforeEach
	void metodo() {
		usuario = RetornaEntidades.gerarUsuario();
		conta = RetornaEntidades.gerarConta(usuario);
		usuario.setConta(conta);

		repositorioUsuario.save(usuario);
		repositorioConta.save(conta);
		
		refreshToken = servicoJwt.criarRefreshToken(usuario);
		refreshTokenEntidade = servicoRefreshToken.gerarEntidadeRefreshToken(refreshToken, usuario);
		repositorioRefreshToken.save(refreshTokenEntidade);
	}

	@Test
	@DisplayName("findByToken_retornaEntidadeRefreshToken_quandoEncontrado")
	void findByToken_retornaEntidadeRefreshToken_quandoEncontrado() {
		Optional<RefreshToken> optional = repositorioRefreshToken.findByToken(refreshToken);
		Assertions.assertThat(optional).isNotEmpty();
		Assertions.assertThat(optional.get().getId()).isEqualTo(refreshTokenEntidade.getId());
	}
	
	@Test
	@DisplayName("findByToken_retornaEmpty_quandoNaoEncontrado")
	void findByToken_retornaEmpty_quandoNaoEncontrado() {
		Optional<RefreshToken> optional = repositorioRefreshToken.findByToken("tokennaoexistente");
		Assertions.assertThat(optional).isEmpty();
	}
	
	@Test
	@DisplayName("deleteByUsuario_deletaEntidadeRefreshToken_quandoUsuarioEncontrado")
	void deleteByUsuario_deletaEntidadeRefreshToken_quandoUsuarioEncontrado() {
		repositorioRefreshToken.deleteByUsuario(usuario);
		Optional<RefreshToken> optional = repositorioRefreshToken.findByToken(refreshToken);
		Assertions.assertThat(optional).isEmpty();
	}
}
