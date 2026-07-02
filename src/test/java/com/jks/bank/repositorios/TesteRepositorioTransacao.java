package com.jks.bank.repositorios;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import com.jks.bank.entidades.Conta;
import com.jks.bank.entidades.StatusDaConta;
import com.jks.bank.entidades.TipoTransacao;
import com.jks.bank.entidades.Transacao;
import com.jks.bank.entidades.Usuario;
import com.jks.bank.util.RetornaEntidades;

@DataJpaTest
@ActiveProfiles("test")
public class TesteRepositorioTransacao {
	@Autowired
	RepositorioTransacao repositorioTransacao;
	@Autowired
	RepositorioConta repositorioConta;
	@Autowired
	RepositorioUsuario repositorioUsuario;

	Usuario usuario;
	Conta conta;

	Usuario usuario2;
	Conta conta2;

	@BeforeEach
	void setUp() {
		usuario = RetornaEntidades.gerarUsuario();
		conta = RetornaEntidades.gerarConta(usuario);
		usuario.setConta(conta);

		usuario2 = Usuario.builder().withCpf("00000000011").withDataNasc(LocalDate.of(2007, 10, 19))
				.withLogin("Jose@gmail").withNome("jose").withSenha("123").withTelefone("1234567811")
				.withContaBloqueada(false).build();
		conta2 = Conta.builder().withDataDaCriacao(LocalDate.now()).withSaldo(BigDecimal.ZERO).withCep("55730000")
				.withStatus(StatusDaConta.ATIVA).withAgencia("001").withChavePix(UUID.randomUUID().toString())
				.withNumero(String.valueOf(ThreadLocalRandom.current().nextInt(100000, 999999))).withUsuario(usuario2)
				.build();
		usuario2.setConta(conta2);

		repositorioUsuario.save(usuario);
		repositorioConta.save(conta);
		repositorioUsuario.save(usuario2);
		repositorioConta.save(conta2);

		salvarTransacao(BigDecimal.valueOf(100), TipoTransacao.PIX, "Transação PIX 1",
				LocalDateTime.now().minusDays(2));
		salvarTransacao(BigDecimal.valueOf(200), TipoTransacao.PIX, "Transação PIX 2",
				LocalDateTime.now().minusDays(1));
		salvarTransacao(BigDecimal.valueOf(300), TipoTransacao.TRANSFERENCIA, "Transação Transferência",
				LocalDateTime.now());
	}

	private void salvarTransacao(BigDecimal valor, TipoTransacao tipo, String descricao, LocalDateTime data) {
		Transacao t = new Transacao();
		t.setContaOrigem(conta);
		t.setContaDestino(conta2);
		t.setValor(valor);
		t.setTipo(tipo);
		t.setData(data);
		t.setDescricao(descricao);
		t.setSaldoAnterior(BigDecimal.ZERO);
		t.setSaldoPosterior(valor);
		repositorioTransacao.save(t);
	}

	@Test
	@DisplayName("buscarExtratoPorPeriodo_retornaTransacao_quandoDentroDoPeriodo")
	void buscarExtratoPorPeriodo_retornaTransacao_quandoDentroDoPeriodo() {
		Pageable pageable = Pageable.unpaged();
		LocalDateTime inicio = LocalDateTime.now().minusDays(1);
		LocalDateTime fim = LocalDateTime.now().plusDays(1);

		Page<Transacao> extrato = repositorioTransacao.buscarExtratoPorPeriodo(conta.getId(), inicio, fim, pageable);

		Assertions.assertThat(extrato).isNotEmpty();
		Assertions.assertThat(extrato.getTotalElements()).isEqualTo(1l);
	}

	@Test
	@DisplayName("buscarExtratoPorPeriodo_retornaVazio_quandoForaDoPeriodo")
	void buscarExtratoPorPeriodo_retornaVazio_quandoForaDoPeriodo() {
		Pageable pageable = Pageable.unpaged();
		LocalDateTime inicio = LocalDateTime.now();
		LocalDateTime fim = LocalDateTime.now().plusSeconds(1);

		Page<Transacao> extrato = repositorioTransacao.buscarExtratoPorPeriodo(conta.getId(), inicio, fim, pageable);

		Assertions.assertThat(extrato).isEmpty();
	}

	@Test
	@DisplayName("findByContaOrigemIdOrContaDestinoId_retornaTransacoes_quandoEncontradas")
	void findByContaOrigemIdOrContaDestinoId_retornaTransacoes_quandoEncontradas() {
		Page<Transacao> page = repositorioTransacao.findByContaOrigemIdOrContaDestinoId(conta.getId(), conta.getId(),
				Pageable.unpaged());
		Assertions.assertThat(page).isNotEmpty();
		Assertions.assertThat(page.getContent().get(0).getValor()).isEqualTo(BigDecimal.valueOf(100));
		Assertions.assertThat(page.getTotalElements()).isEqualTo(3);
	}

	@Test
	@DisplayName("findByContaOrigemIdOrContaDestinoId_retornaVazio_quandoNaoEncontrado")
	void findByContaOrigemIdOrContaDestinoId_retornaVazio_quandoNaoEncontrado() {
		Page<Transacao> page = repositorioTransacao.findByContaOrigemIdOrContaDestinoId(10000l, 10000l,
				Pageable.unpaged());
		Assertions.assertThat(page).isEmpty();
	}

	@Test
	@DisplayName("somarValoresPorPeriodo_retornaSoma_quandoSucesso")
	void somarValoresPorPeriodo_retornaSoma_quandoSucesso() {
		LocalDateTime inicio = LocalDateTime.now().minusDays(3);
		LocalDateTime fim = LocalDateTime.now();
		BigDecimal soma = repositorioTransacao.somarValoresPorPeriodo(TipoTransacao.PIX, conta.getId(), inicio, fim);
		Assertions.assertThat(soma.doubleValue()).isEqualTo(300.0);
	}

	@Test
	@DisplayName("somarValoresPorPeriodo_retornaZero_quandoNaoEncontrado")
	void somarValoresPorPeriodo_retornaZero_quandoNaoEncontrado() {
		LocalDateTime inicio = LocalDateTime.now().minusDays(3);
		LocalDateTime fim = LocalDateTime.now();
		BigDecimal soma = repositorioTransacao.somarValoresPorPeriodo(TipoTransacao.PIX, 100000l, inicio, fim);
		Assertions.assertThat(soma.doubleValue()).isEqualTo(0);
	}

	@Test
	@DisplayName("somarValoresPorPeriodo_retornaZero_quandoDataNaoEncontrada")
	void somarValoresPorPeriodo_retornaZero_quandoDataNaoEncontrada() {
		LocalDateTime inicio = LocalDateTime.now();
		LocalDateTime fim = LocalDateTime.now().plusSeconds(1l);
		BigDecimal soma = repositorioTransacao.somarValoresPorPeriodo(TipoTransacao.PIX, conta.getId(), inicio, fim);
		Assertions.assertThat(soma.doubleValue()).isEqualTo(0);
	}

	@Test
	@DisplayName("quantidadePixMes_retornaQuantidade_quandoSucesso")
	void quantidadePixMes_retornaQuantidade_quandoSucesso() {
		LocalDateTime inicio = LocalDateTime.now().minusDays(3);
		LocalDateTime fim = LocalDateTime.now();
		Long qtd = repositorioTransacao.quantidadePixMes(TipoTransacao.PIX, conta.getId(), inicio, fim);
		Assertions.assertThat(qtd).isEqualTo(2L);
	}

	@Test
	@DisplayName("quantidadePixMes_retornaZero_quandoNaoEncontrado")
	void quantidadePixMes_retornaZero_quandoNaoEncontrado() {
		LocalDateTime inicio = LocalDateTime.now().minusDays(3);
		LocalDateTime fim = LocalDateTime.now();
		Long qtd = repositorioTransacao.quantidadePixMes(TipoTransacao.PIX, 10000l, inicio, fim);
		Assertions.assertThat(qtd).isEqualTo(0);
	}

	@Test
	@DisplayName("quantidadePixMes_retornaZero_quandoDataNaoEncontrada")
	void quantidadePixMes_retornaZero_quandoDataNaoEncontrada() {
		LocalDateTime inicio = LocalDateTime.now();
		LocalDateTime fim = LocalDateTime.now().plusSeconds(1l);
		Long qtd = repositorioTransacao.quantidadePixMes(TipoTransacao.PIX, conta.getId(), inicio, fim);
		Assertions.assertThat(qtd).isEqualTo(0);
	}

	@Test
	@DisplayName("totalEnviadoMes_retornaSoma_quandoSucesso")
	void totalEnviadoMes_retornaSoma_quandoSucesso() {
		LocalDateTime inicio = LocalDateTime.now().minusDays(3);
		LocalDateTime fim = LocalDateTime.now();
		BigDecimal total = repositorioTransacao.totalEnviadoMes(conta.getId(), inicio, fim);
		Assertions.assertThat(total.doubleValue()).isEqualTo(600.0);
	}

	@Test
	@DisplayName("totalEnviadoMes_retornaZero_quandoNaoEncontrado")
	void totalEnviadoMes_retornaZero_quandoNaoEncontrado() {
		LocalDateTime inicio = LocalDateTime.now().minusDays(3);
		LocalDateTime fim = LocalDateTime.now();
		BigDecimal total = repositorioTransacao.totalEnviadoMes(10000l, inicio, fim);
		Assertions.assertThat(total.doubleValue()).isEqualTo(0);
	}

	@Test
	@DisplayName("totalEnviadoMes_retornaZero_quandoDataNaoEncontrada")
	void totalEnviadoMes_retornaZero_quandoDataNaoEncontrada() {
		LocalDateTime inicio = LocalDateTime.now();
		LocalDateTime fim = LocalDateTime.now().plusSeconds(1l);
		BigDecimal total = repositorioTransacao.totalEnviadoMes(conta.getId(), inicio, fim);
		Assertions.assertThat(total.doubleValue()).isEqualTo(0);
	}

	@Test
	@DisplayName("totalRecebidoMes_retornaSoma_quandoSucesso")
	void totalRecebidoMes_retornaSoma_quandoSucesso() {
		LocalDateTime inicio = LocalDateTime.now().minusDays(3);
		LocalDateTime fim = LocalDateTime.now();
		BigDecimal total = repositorioTransacao.totalRecebidoMes(conta2.getId(), inicio, fim);
		Assertions.assertThat(total.doubleValue()).isEqualTo(600.0);
	}

	@Test
	@DisplayName("totalRecebidoMes_retornaZero_quandoNaoEncontrado")
	void totalRecebidoMes_retornaZero_quandoNaoEncontrado() {
		LocalDateTime inicio = LocalDateTime.now().minusDays(3);
		LocalDateTime fim = LocalDateTime.now();
		BigDecimal total = repositorioTransacao.totalRecebidoMes(1000l, inicio, fim);
		Assertions.assertThat(total.doubleValue()).isEqualTo(0);
	}

	@Test
	@DisplayName("totalRecebidoMes_retornaZero_quandoDataNaoEncontrada")
	void totalRecebidoMes_retornaZero_quandoDataNaoEncontrada() {
		LocalDateTime inicio = LocalDateTime.now();
		LocalDateTime fim = LocalDateTime.now().plusSeconds(1l);
		BigDecimal total = repositorioTransacao.totalRecebidoMes(conta2.getId(), inicio, fim);
		Assertions.assertThat(total.doubleValue()).isEqualTo(0);
	}
}
