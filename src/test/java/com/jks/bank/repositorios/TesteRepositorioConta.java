package com.jks.bank.repositorios;

import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.jks.bank.entidades.Conta;
import com.jks.bank.entidades.Usuario;
import com.jks.bank.util.RetornaEntidades;

@DataJpaTest
public class TesteRepositorioConta {
	@Autowired
	RepositorioConta repositorioConta;
	@Autowired
	RepositorioUsuario repositorioUsuario;
	Usuario usuario;
	Conta conta;

	@BeforeEach
	void metodo() {
		usuario = RetornaEntidades.gerarUsuario();
		conta = RetornaEntidades.gerarConta(usuario);
		usuario.setConta(conta);
		repositorioUsuario.save(usuario);
		repositorioConta.save(conta);
	}

	@Test
	@DisplayName("findByUsuarioId_retornaConta_quandoEncontrada")
	void findByUsuarioId_retornaUsuario_quandoEncontrado() {
		Optional<Conta> contaOptional = repositorioConta.findByUsuarioId(usuario.getId());
		Assertions.assertThat(contaOptional).isNotEmpty();
		Assertions.assertThat(contaOptional.get().getId()).isEqualTo(conta.getId());
	}

	@Test
	@DisplayName("findByUsuarioId_retornaEmpty_quandoNaoEncontrado")
	void findByUsuarioId_retornaNull_quandoNaoEncontrado() {
		Optional<Conta> contaOptional = repositorioConta.findByUsuarioId(100000l);
		Assertions.assertThat(contaOptional).isEmpty();
	}

	@Test
	@DisplayName("findByUsuarioLogin_retornaConta_quandoEncontrada")
	void findByUsuarioLogin_retornaUsuario_quandoEncontrado() {
		Optional<Conta> contaOptional = repositorioConta.findByUsuarioLogin(usuario.getUsername());
		Assertions.assertThat(contaOptional).isNotEmpty();
		Assertions.assertThat(contaOptional.get().getId()).isEqualTo(conta.getId());
	}

	@Test
	@DisplayName("findByUsuarioLogin_retornaEmpty_quandoNaoEncontrado")
	void findByUsuarioLogin_retornaNull_quandoNaoEncontrado() {
		Optional<Conta> contaOptional = repositorioConta.findByUsuarioLogin("naoexiste");
		Assertions.assertThat(contaOptional).isEmpty();
	}

	@Test
	@DisplayName("findByChavePix_retornaConta_quandoEncontrada")
	void findByChavePix_retornaUsuario_quandoEncontrado() {
		Optional<Conta> contaOptional = repositorioConta.findByChavePix(conta.getChavePix());
		Assertions.assertThat(contaOptional).isNotEmpty();
		Assertions.assertThat(contaOptional.get().getId()).isEqualTo(conta.getId());
	}

	@Test
	@DisplayName("findByChavePix_retornaEmpty_quandoNaoEncontrado")
	void findByChavePix_retornaNull_quandoNaoEncontrado() {
		Optional<Conta> contaOptional = repositorioConta.findByChavePix("naoexiste");
		Assertions.assertThat(contaOptional).isEmpty();
	}
}
