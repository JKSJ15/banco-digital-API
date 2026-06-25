package com.jks.bank.repositorios;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.jks.bank.entidades.Conta;
import com.jks.bank.entidades.TipoTransacao;
import com.jks.bank.entidades.Transacao;
import com.jks.bank.entidades.Usuario;
import com.jks.bank.util.RetornaEntidades;

@DataJpaTest
public class TesteRepositorioTransacao {
	@Autowired
	RepositorioTransacao repositorioTransacao;
	@Autowired
	RepositorioConta repositorioConta;
	@Autowired
	RepositorioUsuario repositorioUsuario;

	Usuario usuario;
	Conta conta;

	@BeforeEach
	void setUp() {
		usuario = RetornaEntidades.gerarUsuario();
		conta = RetornaEntidades.gerarConta(usuario);
		usuario.setConta(conta);

		repositorioUsuario.save(usuario);
		repositorioConta.save(conta);

		Transacao transacao = new Transacao();
		transacao.setContaOrigem(conta);
		transacao.setContaDestino(conta);
		transacao.setValor(BigDecimal.valueOf(100));
		transacao.setTipo(TipoTransacao.PIX);
		transacao.setData(LocalDateTime.now());
		transacao.setDescricao("testando repositorio");
		transacao.setSaldoAnterior(BigDecimal.ZERO);
		transacao.setSaldoPosterior(BigDecimal.valueOf(100));

		repositorioTransacao.save(transacao);
	}

	@Test
	@DisplayName("findByContaOrigemIdOrContaDestinoId retorna transação quando encontrada")
	void findByContaOrigemIdOrContaDestinoId_retornaTransacao_quandoEncontrada() {
		Pageable pageable = Pageable.unpaged();
		Page<Transacao> transacoes = repositorioTransacao.findByContaOrigemIdOrContaDestinoId(conta.getId(),
				conta.getId(), pageable);

		Assertions.assertThat(transacoes).isNotEmpty();
		Assertions.assertThat(transacoes.getContent().get(0).getValor()).isEqualTo(BigDecimal.valueOf(100));
	}

	@Test
	@DisplayName("buscarExtratoPorPeriodo retorna transação no intervalo")
	void buscarExtratoPorPeriodo_retornaTransacao_quandoDentroDoPeriodo() {
		Pageable pageable = Pageable.unpaged();
		LocalDateTime inicio = LocalDateTime.now().minusDays(1);
		LocalDateTime fim = LocalDateTime.now().plusDays(1);

		Page<Transacao> extrato = repositorioTransacao.buscarExtratoPorPeriodo(conta.getId(), inicio, fim, pageable);

		Assertions.assertThat(extrato).isNotEmpty();
	}

	@Test
	@DisplayName("findByContaOrigemIdOrContaDestinoId retorna transações da conta")
	void testFindByContaOrigemIdOrContaDestinoId() {
		Page<Transacao> page = repositorioTransacao.findByContaOrigemIdOrContaDestinoId(conta.getId(), conta.getId(),
				Pageable.unpaged());
		Assertions.assertThat(page).isNotEmpty();
		Assertions.assertThat(page.getTotalElements()).isEqualTo(1);
	}

	@Test
	@DisplayName("buscarExtratoPorPeriodo retorna transações no intervalo")
	void testBuscarExtratoPorPeriodo() {
		LocalDateTime inicio = LocalDateTime.now().minusDays(3);
		LocalDateTime fim = LocalDateTime.now();
		Page<Transacao> extrato = repositorioTransacao.buscarExtratoPorPeriodo(conta.getId(), inicio, fim,
				Pageable.unpaged());
		Assertions.assertThat(extrato).isNotEmpty();
		Assertions.assertThat(extrato.getTotalElements()).isEqualTo(1);
	}

	@Test
	@DisplayName("somarValoresPorPeriodo retorna soma correta")
	void testSomarValoresPorPeriodo() {
		LocalDateTime inicio = LocalDateTime.now().minusDays(3);
		LocalDateTime fim = LocalDateTime.now();
		BigDecimal soma = repositorioTransacao.somarValoresPorPeriodo(TipoTransacao.PIX, conta.getId(), inicio, fim);
		Assertions.assertThat(soma).isEqualTo(BigDecimal.valueOf(100.00));
	}

	@Test
	@DisplayName("quantidadePixMes retorna quantidade correta")
	void testQuantidadePixMes() {
		LocalDateTime inicio = LocalDateTime.now().minusDays(3);
		LocalDateTime fim = LocalDateTime.now();
		Long qtd = repositorioTransacao.quantidadePixMes(TipoTransacao.PIX, conta.getId(), inicio, fim);
		Assertions.assertThat(qtd).isEqualTo(1L);
	}

	@Test
	@DisplayName("totalEnviadoMes retorna soma correta")
	void testTotalEnviadoMes() {
		LocalDateTime inicio = LocalDateTime.now().minusDays(3);
		LocalDateTime fim = LocalDateTime.now();
		BigDecimal total = repositorioTransacao.totalEnviadoMes(conta.getId(), inicio, fim);
		Assertions.assertThat(total).isEqualTo(BigDecimal.valueOf(100.00));
	}

	@Test
	@DisplayName("totalRecebidoMes retorna soma correta")
	void testTotalRecebidoMes() {
		LocalDateTime inicio = LocalDateTime.now().minusDays(3);
		LocalDateTime fim = LocalDateTime.now();
		BigDecimal total = repositorioTransacao.totalRecebidoMes(conta.getId(), inicio, fim);
		Assertions.assertThat(total).isEqualTo(BigDecimal.valueOf(100.00));
	}
}
