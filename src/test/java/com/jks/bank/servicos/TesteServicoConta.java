package com.jks.bank.servicos;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.jks.bank.dto.CepResponseDto;
import com.jks.bank.dto.ContaResponseDto;
import com.jks.bank.dto.SenhaDto;
import com.jks.bank.entidades.Conta;
import com.jks.bank.entidades.StatusDaConta;
import com.jks.bank.entidades.Usuario;
import com.jks.bank.exceptions.ContaComDinheiroException;
import com.jks.bank.exceptions.ContaNaoEncontradaException;
import com.jks.bank.exceptions.SenhaInvalidaException;
import com.jks.bank.exceptions.UsuarioNaoEncontradoException;
import com.jks.bank.repositorios.RepositorioConta;
import com.jks.bank.repositorios.RepositorioUsuario;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TesteServicoConta {

	@Mock
	RepositorioConta repositorioConta;
	@Mock
	RepositorioUsuario repositorioUsuario;
	@Mock
	PasswordEncoder passwordEncoder;
	@Mock
	ServicoApiCep servicoCep;

	@InjectMocks
	ServicoConta servicoConta;

	Usuario usuario;
	Conta conta;
	SenhaDto senha;

	@BeforeEach
	void setup() {
		usuario = new Usuario();
		usuario.setId(1L);
		usuario.setLogin("testeLogin");
		usuario.setLogin("testeUser");
		usuario.setSenha("senhaCodificada");

		conta = new Conta();
		conta.setId(1L);
		conta.setUsuario(usuario);
		conta.setSaldo(BigDecimal.ZERO);
		conta.setCep("12345-000");

		senha = new SenhaDto("senhaPura");

		when(repositorioUsuario.findByLogin(anyString())).thenReturn(Optional.of(usuario));
		when(repositorioConta.findByUsuarioId(anyLong())).thenReturn(Optional.of(conta));
		when(passwordEncoder.matches(eq("senhaPura"), anyString())).thenReturn(true);
		when(servicoCep.buscarEndereco(anyString())).thenReturn(new CepResponseDto("73730000", "Quadra 12", "Lote 15",
				null, "Setor Central", "Santo Antônio do Descoberto", "GO", "Goiás", "Centro-Oeste", "5219753", null,
				"61", "9493", false));

		Authentication auth = new UsernamePasswordAuthenticationToken(usuario.getUsername(), null, new ArrayList<>());
		SecurityContextHolder.getContext().setAuthentication(auth);
	}

	@AfterEach
	void clearContext() {
		SecurityContextHolder.clearContext();
	}

	@Test
	@DisplayName("contaUsuario_retornaConta_quandoSucesso")
	void contaUsuario_retornaConta_quandoSucesso() {
		ContaResponseDto resposta = servicoConta.contaUsuario();
		Assertions.assertThat(resposta).isNotNull();
		Assertions.assertThat(resposta.id()).isEqualTo(conta.getId());
		Assertions.assertThat(resposta.bairro()).isEqualTo("Setor Central");
	}

	@Test
	@DisplayName("contaUsuario_retornaUsuarioNaoEncontradoException_quandoUsuarioNaoEncontrado")
	void contaUsuario_retornaUsuarioNaoEncontradoException_quandoUsuarioNaoEncontrado() {
		when(repositorioUsuario.findByLogin(anyString())).thenReturn(Optional.empty());
		Assertions.assertThatThrownBy(() -> servicoConta.contaUsuario())
				.isInstanceOf(UsuarioNaoEncontradoException.class);
	}

	@Test
	@DisplayName("contaUsuario_retornaContaNaoEncontradaException_quandoContaNaoEncontrada")
	void contaUsuario_retornaContaNaoEncontradaException_quandoContaNaoEncontrada() {
		when(repositorioConta.findByUsuarioId(anyLong())).thenReturn(Optional.empty());
		Assertions.assertThatThrownBy(() -> servicoConta.contaUsuario())
				.isInstanceOf(ContaNaoEncontradaException.class);
	}

	@Test
	@DisplayName("bloquearConta_alteraStatus_quandoSucesso")
	void bloquearConta_alteraStatus_quandoSucesso() {
		servicoConta.bloquearConta(senha);
		Assertions.assertThat(conta.getStatus()).isEqualTo(StatusDaConta.BLOQUEADA);
	}

	@Test
	@DisplayName("bloquearConta_retornaSenhaInvalidaException_quandoSenhaInvalida")
	void bloquearConta_retornaSenhaInvalidaException_quandoSenhaInvalida() {
		when(passwordEncoder.matches(eq("senhaPura"), anyString())).thenReturn(false);
		Assertions.assertThatThrownBy(() -> servicoConta.bloquearConta(senha))
				.isInstanceOf(SenhaInvalidaException.class);
	}

	@Test
	@DisplayName("desbloquearConta_alteraStatus_quandoSucesso")
	void desbloquearConta_alteraStatus_quandoSucesso() {
		conta.bloquearConta();
		servicoConta.desbloquearConta(senha);
		Assertions.assertThat(conta.getStatus()).isEqualTo(StatusDaConta.ATIVA);
	}

	@Test
	@DisplayName("desbloquearConta_retornaSenhaInvalidaException_quandoSenhaInvalida")
	void desbloquearConta_retornaSenhaInvalidaException_quandoSenhaInvalida() {
		when(passwordEncoder.matches(eq("senhaPura"), anyString())).thenReturn(false);
		Assertions.assertThatThrownBy(() -> servicoConta.desbloquearConta(senha))
				.isInstanceOf(SenhaInvalidaException.class);
	}

	@Test
	@DisplayName("encerrarConta_alteraStatus_quandoSaldoZero")
	void encerrarConta_alteraStatus_quandoSaldoZero() {
		servicoConta.encerrarConta(senha);
		Assertions.assertThat(conta.getStatus()).isEqualTo(StatusDaConta.ENCERRADA);
	}

	@Test
	@DisplayName("encerrarConta_retornaContaComDinheiroException_quandoSaldoPositivo")
	void encerrarConta_retornaContaComDinheiroException_quandoSaldoPositivo() {
		conta.setSaldo(BigDecimal.TEN);
		Assertions.assertThatThrownBy(() -> servicoConta.encerrarConta(senha))
				.isInstanceOf(ContaComDinheiroException.class);
	}

	@Test
	@DisplayName("encerrarConta_retornaSenhaInvalidaException_quandoSenhaInvalida")
	void encerrarConta_retornaSenhaInvalidaException_quandoSenhaInvalida() {
		when(passwordEncoder.matches(eq("senhaPura"), anyString())).thenReturn(false);
		Assertions.assertThatThrownBy(() -> servicoConta.encerrarConta(senha))
				.isInstanceOf(SenhaInvalidaException.class);
	}
}
