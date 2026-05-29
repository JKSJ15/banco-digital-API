package com.jks.bank.servicos;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

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
import com.jks.bank.exceptions.TransferenciaInvalidaException;
import com.jks.bank.exceptions.UsuarioNaoEncontradoException;
import com.jks.bank.exceptions.ValorInvalidoException;
import com.jks.bank.mapeamento.MapeamentoDeTransacao;
import com.jks.bank.repositorios.RepositorioConta;
import com.jks.bank.repositorios.RepositorioTransacao;
import com.jks.bank.repositorios.RepositorioUsuario;

import jakarta.transaction.Transactional;

@Service
public class ServicoTransacoes {
	private final RepositorioConta repConta;
	private final RepositorioTransacao repTransacao;
	private final RepositorioUsuario repUsuario;

	public ServicoTransacoes(RepositorioConta repConta, RepositorioTransacao repTransacao,
			RepositorioUsuario repUsuario) {
		super();
		this.repConta = repConta;
		this.repTransacao = repTransacao;
		this.repUsuario = repUsuario;
	}

	public Page<TransacaoResponseDto> extrato(Pageable pageable) {
		Conta conta = contaDoUsuarioAutenticado();
		Page<Transacao> transacoes = repTransacao.findByContaOrigemIdOrContaDestinoId(conta.getId(), conta.getId(),
				pageable);
		return transacoes.map(MapeamentoDeTransacao::transacaoParaDto);
	}

	@Transactional
	public TransacaoResponseDto transferencia(TransferenciaRequestDto transferenciaRequest) {
		validarValor(transferenciaRequest.valor());
		Conta conta = contaDoUsuarioAutenticado();
		validarMovimentacaoDeSaida(conta);
		Conta contaDestino = repConta.findById(transferenciaRequest.idContaDestino())
				.orElseThrow(() -> new ContaNaoEncontradaException("conta destino não encontrada!"));

		if (conta.getId().equals(contaDestino.getId())) {
			throw new TransferenciaInvalidaException("transferência inválida!");
		}

		BigDecimal saldoatual = conta.getSaldo();
		sacar(conta, transferenciaRequest.valor());
		depositar(contaDestino, transferenciaRequest.valor());

		Transacao transacaoSalva = realizarTransacao(TipoTransacao.TRANSFERENCIA, contaDestino, conta,
				transferenciaRequest.valor(), transferenciaRequest.descricao(), saldoatual, conta.getSaldo());

		repConta.save(conta);
		repConta.save(contaDestino);
		return MapeamentoDeTransacao.transacaoParaDto(transacaoSalva);
	}

	@Transactional
	public TransacaoResponseDto pix(PixRequestDto pixRequest) {
		validarValor(pixRequest.valor());
		Conta conta = contaDoUsuarioAutenticado();
		validarMovimentacaoDeSaida(conta);
		Conta contaDestino = repConta.findByChavePix(pixRequest.chavePix())
				.orElseThrow(() -> new ContaNaoEncontradaException("conta destino não encontrada!"));

		if (conta.getChavePix().equals(contaDestino.getChavePix())) {
			throw new TransferenciaInvalidaException("transferência inválida!");
		}

		BigDecimal saldoatual = conta.getSaldo();
		sacar(conta, pixRequest.valor());
		depositar(contaDestino, pixRequest.valor());

		Transacao transacaoSalva = realizarTransacao(TipoTransacao.PIX, contaDestino, conta, pixRequest.valor(),
				pixRequest.descricao(), saldoatual, conta.getSaldo());

		repConta.save(conta);
		repConta.save(contaDestino);
		return MapeamentoDeTransacao.transacaoParaDto(transacaoSalva);
	}

	@Transactional
	public TransacaoResponseDto saque(SaqueRequestDto saqueRequest) {
		validarValor(saqueRequest.valor());
		Conta conta = contaDoUsuarioAutenticado();
		validarMovimentacaoDeSaida(conta);

		BigDecimal saldoatual = conta.getSaldo();
		sacar(conta, saqueRequest.valor());

		Transacao transacaoSalva = realizarTransacao(TipoTransacao.SAQUE, null, conta, saqueRequest.valor(),
				saqueRequest.descricao(), saldoatual, conta.getSaldo());

		repConta.save(conta);
		return MapeamentoDeTransacao.transacaoParaDto(transacaoSalva);
	}

	@Transactional
	public TransacaoResponseDto deposito(DepositoRequestDto depositoRequest) {
		validarValor(depositoRequest.valor());
		Conta contaDeposito = contaDoUsuarioAutenticado();

		BigDecimal saldoAtual = contaDeposito.getSaldo();
		depositar(contaDeposito, depositoRequest.valor());

		Transacao transacaoSalva = realizarTransacao(TipoTransacao.DEPOSITO, contaDeposito, null,
				depositoRequest.valor(), depositoRequest.descricao(), saldoAtual, contaDeposito.getSaldo());

		repConta.save(contaDeposito);
		return MapeamentoDeTransacao.transacaoParaDto(transacaoSalva);
	}

	// MÉTODOS INTERNOS
	private void validarValor(BigDecimal valor) {
		if (valor.compareTo(BigDecimal.ZERO) <= 0) {
			throw new ValorInvalidoException("o valor deve ser maior que zero!");
		}
	}

	private void depositar(Conta conta, BigDecimal valor) {
		if (conta.getStatus() == StatusDaConta.ENCERRADA) {
			throw new ContaEncerradaException("indisponível, sua conta está " + conta.getStatus());
		}
		BigDecimal saldoatual = conta.getSaldo();
		BigDecimal saldoPosDeposito = saldoatual.add(valor);
		conta.setSaldo(saldoPosDeposito);
	}

	private void sacar(Conta conta, BigDecimal valor) {
		BigDecimal saldoAtual = conta.getSaldo();
		if (saldoAtual.compareTo(valor) < 0) {
			throw new SaldoInsuficienteException("saldo insuficiente!");
		}
		BigDecimal saldoPosSaque = saldoAtual.subtract(valor);
		conta.setSaldo(saldoPosSaque);
	}

	private Transacao realizarTransacao(TipoTransacao tipo, Conta contaDestino, Conta contaOrigem, BigDecimal valor,
			String descricao, BigDecimal saldoAnterior, BigDecimal saldoPosterior) {

		Transacao transacao = Transacao.builder().withContaDestino(contaDestino).withContaOrigem(contaOrigem)
				.withData(LocalDateTime.now()).withDescricao(descricao).withSaldoAnterior(saldoAnterior)
				.withSaldoPosterior(saldoPosterior).withTipo(tipo).withValor(valor).build();

		return repTransacao.save(transacao);
	}

	private Conta contaDoUsuarioAutenticado() {
		String login = SecurityContextHolder.getContext().getAuthentication().getName();
		Usuario usuario = repUsuario.findByLogin(login)
				.orElseThrow(() -> new UsuarioNaoEncontradoException("usuario não encontado!"));
		Conta conta = repConta.findByUsuario(usuario.getId())
				.orElseThrow(() -> new ContaNaoEncontradaException("Conta não encontrada!"));
		return conta;
	}

	private void validarMovimentacaoDeSaida(Conta conta) {
		if (conta.getStatus() == StatusDaConta.BLOQUEADA || conta.getStatus() == StatusDaConta.ENCERRADA) {
			throw new ContaBloqueadaException("indisponível, sua conta está " + conta.getStatus());
		}
	}

	// private void validarSenha() {
	// if (!passwordEncoder.matches(
	// senha.senha(),
	// usuario.getSenha()
	// )) {
	// throw new SenhaInvalidaException("senha inválida!");}}
}
