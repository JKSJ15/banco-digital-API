package com.jks.bank.controles;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import com.jks.bank.dto.DepositoRequestDto;
import com.jks.bank.dto.PixRequestDto;
import com.jks.bank.dto.SaqueRequestDto;
import com.jks.bank.dto.TransacaoResponseDto;
import com.jks.bank.dto.TransferenciaRequestDto;
import com.jks.bank.entidades.Conta;
import com.jks.bank.entidades.TipoTransacao;
import com.jks.bank.entidades.Usuario;
import com.jks.bank.servicos.ServicoTransacoes;
import com.jks.bank.util.RetornaEntidades;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class TesteControleTransacao {
	@InjectMocks
	ControleTransacoes controleTransacoes;
	@Mock
	ServicoTransacoes servicoTransacoes;

	Conta conta;
	Usuario usuario;

	DepositoRequestDto depositoRequest;
	SaqueRequestDto saqueRequest;
	PixRequestDto pixRequest;
	TransferenciaRequestDto transferenciaRequest;
	TransacaoResponseDto transacaoResponse;

	@BeforeEach
	void metodo() {
		usuario = RetornaEntidades.gerarUsuario();
		conta = RetornaEntidades.gerarConta(usuario);
		usuario.setConta(conta);

		depositoRequest = new DepositoRequestDto(BigDecimal.valueOf(200), "123", "Teste");
		saqueRequest = new SaqueRequestDto(BigDecimal.valueOf(200), "123", "Teste");
		pixRequest = new PixRequestDto("chavealeatoria", BigDecimal.valueOf(100), "123", "Teste");
		transferenciaRequest = new TransferenciaRequestDto(2l, BigDecimal.valueOf(100), "123", "Teste");
		transacaoResponse = new TransacaoResponseDto(1l, TipoTransacao.DEPOSITO, BigDecimal.valueOf(200),
				LocalDateTime.now(), "teste", 1l, 2l, BigDecimal.ZERO, BigDecimal.valueOf(200));
	}

	@Test
	@DisplayName("deposito_retornaOK_quandoSucesso")
	void deposito_retornaOK_quandoSucesso() {
		Mockito.when(servicoTransacoes.deposito(ArgumentMatchers.any(DepositoRequestDto.class)))
				.thenReturn(transacaoResponse);
		ResponseEntity<TransacaoResponseDto> resposta = controleTransacoes.deposito(depositoRequest);

		Assertions.assertThat(resposta.getBody()).isNotNull().isEqualTo(transacaoResponse);
		Assertions.assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
	}

	@Test
	@DisplayName("saque_retornaOK_quandoSucesso")
	void saque_retornaOK_quandoSucesso() {
		Mockito.when(servicoTransacoes.saque(ArgumentMatchers.any(SaqueRequestDto.class)))
				.thenReturn(transacaoResponse);
		ResponseEntity<TransacaoResponseDto> resposta = controleTransacoes.saque(saqueRequest);

		Assertions.assertThat(resposta.getBody()).isNotNull().isEqualTo(transacaoResponse);
		Assertions.assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
	}

	@Test
	@DisplayName("pix_retornaOK_quandoSucesso")
	void pix_retornaOK_quandoSucesso() {
		Mockito.when(servicoTransacoes.pix(ArgumentMatchers.any(PixRequestDto.class))).thenReturn(transacaoResponse);
		ResponseEntity<TransacaoResponseDto> resposta = controleTransacoes.pix(pixRequest);

		Assertions.assertThat(resposta.getBody()).isNotNull().isEqualTo(transacaoResponse);
		Assertions.assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
	}

	@Test
	@DisplayName("transferencia_retornaOK_quandoSucesso")
	void transferencia_retornaOK_quandoSucesso() {
		Mockito.when(servicoTransacoes.transferencia(ArgumentMatchers.any(TransferenciaRequestDto.class)))
				.thenReturn(transacaoResponse);
		ResponseEntity<TransacaoResponseDto> resposta = controleTransacoes.transferencia(transferenciaRequest);

		Assertions.assertThat(resposta.getBody()).isNotNull().isEqualTo(transacaoResponse);
		Assertions.assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
	}
}
