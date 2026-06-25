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
public class TesteRepositorioUsuario {
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
	@DisplayName("existsByCpf_retornaTrue_quandoEncontrada")
	void existsByCpf_retornaTrue_quandoEncontrada() {
		boolean existe = repositorioUsuario.existsByCpf(usuario.getCpf());
		Assertions.assertThat(existe).isTrue();
	}

	@Test
	@DisplayName("existsByCpf_retornaFalse_quandoNaoEncontrado")
	void existsByCpf_retornaFalse_quandoNaoEncontrado() {
		boolean existe = repositorioUsuario.existsByCpf("01010101010");
		Assertions.assertThat(existe).isFalse();
	}

	@Test
	@DisplayName("existsByTelefone_retornaTrue_quandoEncontrada")
	void existsByTelefone_retornaTrue_quandoEncontrada() {
		boolean existe = repositorioUsuario.existsByTelefone(usuario.getTelefone());
		Assertions.assertThat(existe).isTrue();
	}

	@Test
	@DisplayName("existsByTelefone_retornaFalse_quandoNaoEncontrado")
	void existsByTelefone_retornaFalse_quandoNaoEncontrado() {
		boolean existe = repositorioUsuario.existsByTelefone("8122222222");
		Assertions.assertThat(existe).isFalse();
	}

	@Test
	@DisplayName("findByUsuarioLogin_retornaConta_quandoEncontrada")
	void findByUsuarioLogin_retornaUsuario_quandoEncontrado() {
		Optional<Usuario> usuarioOptional = repositorioUsuario.findByLogin(usuario.getUsername());
		Assertions.assertThat(usuarioOptional).isNotEmpty();
		Assertions.assertThat(usuarioOptional.get().getId()).isEqualTo(usuario.getId());
	}

	@Test
	@DisplayName("findByUsuarioLogin_retornaEmpty_quandoNaoEncontrado")
	void findByUsuarioLogin_retornaNull_quandoNaoEncontrado() {
		Optional<Usuario> usuarioOptional = repositorioUsuario.findByLogin("naoexiste");
		Assertions.assertThat(usuarioOptional).isEmpty();
	}

}
