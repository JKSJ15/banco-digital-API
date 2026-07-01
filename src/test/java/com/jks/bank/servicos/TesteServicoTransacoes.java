package com.jks.bank.servicos;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import org.assertj.core.api.Assertions;
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

import com.jks.bank.dto.DepositoRequestDto;
import com.jks.bank.dto.PixRequestDto;
import com.jks.bank.dto.SaqueRequestDto;
import com.jks.bank.dto.TransacaoResponseDto;
import com.jks.bank.dto.TransferenciaRequestDto;
import com.jks.bank.entidades.Conta;
import com.jks.bank.entidades.StatusDaConta;
import com.jks.bank.entidades.TipoTransacao;
import com.jks.bank.entidades.Transacao;
import com.jks.bank.entidades.Usuario;
import com.jks.bank.exceptions.ContaBloqueadaException;
import com.jks.bank.exceptions.ContaEncerradaException;
import com.jks.bank.exceptions.ContaNaoEncontradaException;
import com.jks.bank.exceptions.SaldoInsuficienteException;
import com.jks.bank.exceptions.SenhaInvalidaException;
import com.jks.bank.exceptions.TransferenciaInvalidaException;
import com.jks.bank.exceptions.ValorInvalidoException;
import com.jks.bank.repositorios.RepositorioConta;
import com.jks.bank.repositorios.RepositorioTransacao;
import com.jks.bank.repositorios.RepositorioUsuario;
import com.jks.bank.util.RetornaEntidades;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class TesteServicoTransacoes {
	@InjectMocks
	ServicoTransacoes servicoTransacoes;
	@Mock
	RepositorioUsuario repositorioUsuario;
	@Mock
	RepositorioConta repositorioConta;
	@Mock
	RepositorioTransacao repositorioTransacao;
	@Mock
	PasswordEncoder passwordEncoder;
	Usuario usuario;
	Conta conta;
	Conta conta2;

	DepositoRequestDto depositoRequest;
	SaqueRequestDto saqueRequest;
	PixRequestDto pixRequest;
	TransacaoResponseDto transacaoResponse;
	TransferenciaRequestDto transferenciaRequest;

	@BeforeEach
	void metodo() {
		usuario = RetornaEntidades.gerarUsuario();
		conta = RetornaEntidades.gerarConta(usuario);
		usuario.setConta(conta);

		conta2 = new Conta();
		conta2.setId(10L);
		conta2.setChavePix("ejhvbyvcyvyy26747bbdiferente");
		conta2.setSaldo(BigDecimal.ZERO);
		conta2.setCep("12345-000");

		depositoRequest = new DepositoRequestDto(BigDecimal.TEN, "123", "Teste");
		saqueRequest = new SaqueRequestDto(BigDecimal.TEN, "123", "Teste");
		pixRequest = new PixRequestDto(conta2.getChavePix(), BigDecimal.valueOf(10), "123", "Teste");
		transferenciaRequest = new TransferenciaRequestDto(conta2.getId(), BigDecimal.valueOf(10), "123", "Teste");

		transacaoResponse = new TransacaoResponseDto(1l, TipoTransacao.TRANSFERENCIA, BigDecimal.TEN,
				LocalDateTime.now(), "Teste", 2l, 1l, BigDecimal.ZERO, BigDecimal.TEN);
		Authentication auth = UsernamePasswordAuthenticationToken.authenticated(usuario.getUsername(), null,
				new ArrayList<>());
		SecurityContextHolder.getContext().setAuthentication(auth);

		when(repositorioUsuario.findByLogin(anyString())).thenReturn(Optional.of(usuario));
		when(repositorioConta.findByUsuarioLogin(anyString())).thenReturn(Optional.of(conta));
		when(repositorioConta.findById(anyLong())).thenReturn(Optional.of(conta2));
		when(repositorioConta.findByChavePix(anyString())).thenReturn(Optional.of(conta2));
		when(passwordEncoder.matches(eq("123"), anyString())).thenReturn(true);
		when(repositorioTransacao.save(any(Transacao.class))).thenAnswer(invocation -> {
			Transacao t = invocation.getArgument(0);
			t.setId(1L);
			return t;
		});
	}

	// DEPOSITO
	@Test
	@DisplayName("deposito_retornaTransacao_quandoSucesso")
	void deposito_retornaTransacao_quandoSucesso() {
		transacaoResponse = servicoTransacoes.deposito(depositoRequest);
		Assertions.assertThat(transacaoResponse).isNotNull();
	}

	@Test
	@DisplayName("deposito_retornaSenhaInvalidaException_quandoSenhaErrada")
	void deposito_retornaSenhaInvalidaException_quandoSenhaErrada() {
		when(passwordEncoder.matches(eq("123"), anyString())).thenReturn(false);
		Assertions.assertThatThrownBy(() -> servicoTransacoes.deposito(depositoRequest))
				.isInstanceOf(SenhaInvalidaException.class);
	}

	@Test
	@DisplayName("deposito_retornaValorInvalidoException_quandoValorExcedeLimite")
	void deposito_retornaValorInvalidoException_quandoValorExcedeLimite() {
		depositoRequest = new DepositoRequestDto(BigDecimal.valueOf(50001), "123", "Teste");
		Assertions.assertThatThrownBy(() -> servicoTransacoes.deposito(depositoRequest))
				.isInstanceOf(ValorInvalidoException.class);
	}

	@Test
	@DisplayName("deposito_retornaContaEncerradaException_quandoContaEncerrada")
	void deposito_retornaContaEncerradaException_quandoContaEncerrada() {
		depositoRequest = new DepositoRequestDto(BigDecimal.TEN, "123", "Teste");
		conta.setStatus(StatusDaConta.ENCERRADA);
		Assertions.assertThatThrownBy(() -> servicoTransacoes.deposito(depositoRequest))
				.isInstanceOf(ContaEncerradaException.class);
	}

	// SAQUE
	@Test
	@DisplayName("saque_retornaTransacao_quandoSucesso")
	void saque_retornaTransacao_quandoSucesso() {
		conta.setSaldo(BigDecimal.valueOf(10));
		transacaoResponse = servicoTransacoes.saque(saqueRequest);
		Assertions.assertThat(transacaoResponse).isNotNull();
	}

	@Test
	@DisplayName("saque_retornaValorInvalidoException_quandoValorExcedeLimite")
	void saque_retornaValorInvalidoException_quandoValorExcedeLimite() {
		saqueRequest = new SaqueRequestDto(BigDecimal.valueOf(50001), "123", "Teste");
		conta.setSaldo(BigDecimal.valueOf(500000000));
		Assertions.assertThatThrownBy(() -> servicoTransacoes.saque(saqueRequest))
				.isInstanceOf(ValorInvalidoException.class);
	}

	@Test
	@DisplayName("saque_retornaSenhaInvalidaException_quandoSenhaInvalida")
	void saque_retornaSenhaInvalidaException_quandoSenhaInvalida() {
		when(passwordEncoder.matches(eq("123"), anyString())).thenReturn(false);
		saqueRequest = new SaqueRequestDto(BigDecimal.valueOf(500), "123", "Teste");
		conta.setSaldo(BigDecimal.valueOf(500));
		Assertions.assertThatThrownBy(() -> servicoTransacoes.saque(saqueRequest))
				.isInstanceOf(SenhaInvalidaException.class);
	}

	@Test
	@DisplayName("saque_retornaSaldoInsuficienteException_quandoValorExcedeSaldo")
	void saque_retornaSaldoInsuficienteException_quandoValorExcedeSaldo() {
		saqueRequest = new SaqueRequestDto(BigDecimal.valueOf(5000), "123", "Teste");
		Assertions.assertThatThrownBy(() -> servicoTransacoes.saque(saqueRequest))
				.isInstanceOf(SaldoInsuficienteException.class);
	}

	@Test
	@DisplayName("saque_retornaContaBloqueadaException_quandoContaEncerrada")
	void saque_retornaContaBloqueadaException_quandoContaEncerrada() {
		saqueRequest = new SaqueRequestDto(BigDecimal.valueOf(5000), "123", "Teste");
		conta.setStatus(StatusDaConta.ENCERRADA);
		Assertions.assertThatThrownBy(() -> servicoTransacoes.saque(saqueRequest))
				.isInstanceOf(ContaBloqueadaException.class);
	}

	@Test
	@DisplayName("saque_retornaContaBloqueadaException_quandoContaBloqueada")
	void saque_retornaContaBloqueadaException_quandoContaBloqueada() {
		saqueRequest = new SaqueRequestDto(BigDecimal.valueOf(5000), "123", "Teste");
		conta.setStatus(StatusDaConta.BLOQUEADA);
		Assertions.assertThatThrownBy(() -> servicoTransacoes.saque(saqueRequest))
				.isInstanceOf(ContaBloqueadaException.class);
	}

	// PIX
	@Test
	@DisplayName("pix_retornaTransacao_quandoSucesso")
	void pix_retornaTransacao_quandoSucesso() {
		conta.setSaldo(BigDecimal.valueOf(10));
		transacaoResponse = servicoTransacoes.pix(pixRequest);
		Assertions.assertThat(transacaoResponse).isNotNull();
	}

	@Test
	@DisplayName("pix_retornaTransferenciaInvalidaException_quandoChaveDestinoIgualaOrigem")
	void pix_retornaTransferenciaInvalidaException_quandoChaveDestinoIgualaOrigem() {
		when(repositorioConta.findByChavePix(anyString())).thenReturn(Optional.of(conta));
		conta.setSaldo(BigDecimal.valueOf(500));
		Assertions.assertThatThrownBy(() -> servicoTransacoes.pix(pixRequest))
				.isInstanceOf(TransferenciaInvalidaException.class);
	}

	@Test
	@DisplayName("pix_retornaContaNaoEncontradaException_quandoContaDestinoNaoEncontrada")
	void pix_retornaContaNaoEncontradaException_quandoContaDestinoNaoEncontrada() {
		when(repositorioConta.findByChavePix(anyString())).thenReturn(Optional.empty());
		conta.setSaldo(BigDecimal.valueOf(500));
		Assertions.assertThatThrownBy(() -> servicoTransacoes.pix(pixRequest))
				.isInstanceOf(ContaNaoEncontradaException.class);
	}

	@Test
	@DisplayName("pix_retornaSenhaInvalidaException_quandoSenhaInvalida")
	void pix_retornaSenhaInvalidaException_quandoSenhaInvalida() {
		when(passwordEncoder.matches(eq("123"), anyString())).thenReturn(false);
		pixRequest = new PixRequestDto(conta2.getChavePix(), BigDecimal.valueOf(10), "123", "Teste");
		conta.setSaldo(BigDecimal.valueOf(500));
		Assertions.assertThatThrownBy(() -> servicoTransacoes.pix(pixRequest))
				.isInstanceOf(SenhaInvalidaException.class);
	}

	@Test
	@DisplayName("pix_retornaValorInvalidoException_quandoExcedeLimiteDiario")
	void pix_retornaValorInvalidoException_quandoExcedeLimiteDiario() {
		conta.setSaldo(BigDecimal.valueOf(20001));
		pixRequest = new PixRequestDto(conta2.getChavePix(), BigDecimal.valueOf(20001), "123", "Teste");
		Assertions.assertThatThrownBy(() -> servicoTransacoes.pix(pixRequest))
				.isInstanceOf(ValorInvalidoException.class);
	}

	@Test
	@DisplayName("pix_retornaValorInvalidoException_quandoExcedeLimitePorTransacao")
	void pix_retornaValorInvalidoException_quandoExcedeLimitePorTransacao() {
		conta.setSaldo(BigDecimal.valueOf(10001));
		pixRequest = new PixRequestDto(conta2.getChavePix(), BigDecimal.valueOf(10001), "123", "Teste");
		Assertions.assertThatThrownBy(() -> servicoTransacoes.pix(pixRequest))
				.isInstanceOf(ValorInvalidoException.class);
	}

	@Test
	@DisplayName("pix_retornaContaBloqueadaException_quandoContaEncerrada")
	void pix_retornaContaBloqueadaException_quandoContaEncerrada() {
		conta.setStatus(StatusDaConta.ENCERRADA);
		Assertions.assertThatThrownBy(() -> servicoTransacoes.pix(pixRequest))
				.isInstanceOf(ContaBloqueadaException.class);
	}

	@Test
	@DisplayName("pix_retornaContaEncerradaException_quandoContaBloqueada")
	void pix_retornaContaEncerradaException_quandoContaBloqueada() {
		conta.setStatus(StatusDaConta.BLOQUEADA);
		Assertions.assertThatThrownBy(() -> servicoTransacoes.pix(pixRequest))
				.isInstanceOf(ContaBloqueadaException.class);
	}

	// TRANSFERÊNCIA
	@Test
	@DisplayName("transferencia_retornaTransacao_quandoSucesso")
	void transferencia_retornaTransacao_quandoSucesso() {
		conta.setId(1L);
		conta2.setId(10L);
		conta.setSaldo(BigDecimal.valueOf(500));

		TransacaoResponseDto response = servicoTransacoes.transferencia(transferenciaRequest);
		Assertions.assertThat(response).isNotNull();
	}

	@Test
	@DisplayName("transferencia_retornaTransferenciaInvalidaException_quandoContaDestinoIgualOrigem")
	void transferencia_retornaTransferenciaInvalidaException_quandoContaDestinoIgualOrigem() {
		when(repositorioConta.findById(anyLong())).thenReturn(Optional.of(conta));
		conta.setSaldo(BigDecimal.valueOf(500));
		conta.setId(1l);

		transferenciaRequest = new TransferenciaRequestDto(conta.getId(), BigDecimal.valueOf(10), "123", "Teste");
		Assertions.assertThatThrownBy(() -> servicoTransacoes.transferencia(transferenciaRequest))
				.isInstanceOf(TransferenciaInvalidaException.class);
	}

	@Test
	@DisplayName("transferencia_retornaContaNaoEncontradaException_quandoContaDestinoNaoEncontrada")
	void transferencia_retornaContaNaoEncontradaException_quandoContaDestinoNaoEncontrada() {
		conta.setSaldo(BigDecimal.valueOf(500));
		when(repositorioConta.findById(anyLong())).thenReturn(Optional.empty());

		TransferenciaRequestDto transferenciaRequest = new TransferenciaRequestDto(99L, BigDecimal.valueOf(100), "123",
				"Teste");

		Assertions.assertThatThrownBy(() -> servicoTransacoes.transferencia(transferenciaRequest))
				.isInstanceOf(ContaNaoEncontradaException.class);
	}

	@Test
	@DisplayName("transferencia_retornaSenhaInvalidaException_quandoSenhaInvalida")
	void transferencia_retornaSenhaInvalidaException_quandoSenhaInvalida() {
		when(passwordEncoder.matches(eq("123"), anyString())).thenReturn(false);

		TransferenciaRequestDto transferenciaRequest = new TransferenciaRequestDto(2L, BigDecimal.valueOf(100), "123",
				"Teste");

		Assertions.assertThatThrownBy(() -> servicoTransacoes.transferencia(transferenciaRequest))
				.isInstanceOf(SenhaInvalidaException.class);
	}

	@Test
	@DisplayName("transferencia_retornaValorInvalidoException_quandoExcedeLimite")
	void transferencia_retornaValorInvalidoException_quandoExcedeLimite() {
		conta.setSaldo(BigDecimal.valueOf(20000));
		TransferenciaRequestDto transferenciaRequest = new TransferenciaRequestDto(2L, BigDecimal.valueOf(20000), "123",
				"Teste");

		Assertions.assertThatThrownBy(() -> servicoTransacoes.transferencia(transferenciaRequest))
				.isInstanceOf(ValorInvalidoException.class);
	}

	@Test
	@DisplayName("transferencia_retornaContaBloqueadaException_quandoContaBloqueada")
	void transferencia_retornaContaBloqueadaException_quandoContaBloqueada() {
		conta.setStatus(StatusDaConta.BLOQUEADA);

		TransferenciaRequestDto transferenciaRequest = new TransferenciaRequestDto(2L, BigDecimal.valueOf(100), "123",
				"Teste");

		Assertions.assertThatThrownBy(() -> servicoTransacoes.transferencia(transferenciaRequest))
				.isInstanceOf(ContaBloqueadaException.class);
	}

	@Test
	@DisplayName("transferencia_retornaContaBloqueadaException_quandoContaEncerrada")
	void transferencia_retornaContaBloqueadaException_quandoContaEncerrada() {
		conta.setStatus(StatusDaConta.ENCERRADA);

		TransferenciaRequestDto transferenciaRequest = new TransferenciaRequestDto(2L, BigDecimal.valueOf(100), "123",
				"Teste");

		Assertions.assertThatThrownBy(() -> servicoTransacoes.transferencia(transferenciaRequest))
				.isInstanceOf(ContaBloqueadaException.class);
	}

}
