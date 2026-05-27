package com.jks.bank.servicos;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.jks.bank.dto.DepositoRequestDto;
import com.jks.bank.dto.SaqueRequestDto;
import com.jks.bank.dto.TransacaoResponseDto;
import com.jks.bank.entidades.Conta;
import com.jks.bank.entidades.StatusDaConta;
import com.jks.bank.entidades.TipoTransacao;
import com.jks.bank.entidades.Transacao;
import com.jks.bank.entidades.Usuario;
import com.jks.bank.exceptions.ContaBloqueadaException;
import com.jks.bank.exceptions.ContaNaoEncontradaException;
import com.jks.bank.exceptions.UsuarioNaoEncontradoException;
import com.jks.bank.mapeamento.MapeamentoDeTransacao;
import com.jks.bank.repositorios.RepositorioConta;
import com.jks.bank.repositorios.RepositorioTransacao;
import com.jks.bank.repositorios.RepositorioUsuario;

@Service
public class ServicoTransacoes {
	private final RepositorioConta repConta;
	private final RepositorioTransacao repTransacao;
	private final RepositorioUsuario repUsuario;
	private final MapeamentoDeTransacao mapeadorTransacao;

	public ServicoTransacoes(RepositorioConta repConta, RepositorioTransacao repTransacao,
			RepositorioUsuario repUsuario, MapeamentoDeTransacao mapeadorTransacao) {
		super();
		this.repConta = repConta;
		this.repTransacao = repTransacao;
		this.repUsuario = repUsuario;
		this.mapeadorTransacao = mapeadorTransacao;
	}

	public Page<TransacaoResponseDto> extrato(Pageable pageable) {
		Conta conta = contaDoUsuarioAutenticado();
		Page<Transacao> transacoes = repTransacao.findByContaOrigemAndContaDestino(conta.getId());
		return transacoes.map(mapeadorTransacao::transacaoParaDto);
	}

	public TransacaoResponseDto transferencia() {
		Conta conta = contaDoUsuarioAutenticado();
		// validarConta(conta, depositoRequest.valor());
		return null;
	}

	public TransacaoResponseDto pix() {
		Conta conta = contaDoUsuarioAutenticado();
		// validarConta(conta, depositoRequest.valor());
		return null;
	}

	public TransacaoResponseDto saque(SaqueRequestDto saqueRequest) {
		Conta conta = contaDoUsuarioAutenticado();
		validarConta(conta);
		
		BigDecimal saldoatual = conta.getSaldo();
		BigDecimal saldoPosSaque = saldoatual.subtract(saqueRequest.valor());
		conta.setSaldo(saldoPosSaque);

		Transacao transacao = Transacao.builder().withContaDestino(null).withContaOrigem(conta)
				.withData(LocalDateTime.now()).withDescricao(saqueRequest.descricao()).withSaldoAnterior(saldoatual)
				.withSaldoPosterior(saldoPosSaque).withTipo(TipoTransacao.SAQUE)
				.withValor(saqueRequest.valor()).build();

		repConta.save(conta);
		Transacao transacaoSalva = repTransacao.save(transacao);

		return mapeadorTransacao.transacaoParaDto(transacaoSalva);
	}

	public TransacaoResponseDto deposito(DepositoRequestDto depositoRequest) {
		Conta contaDeposito = contaDoUsuarioAutenticado();
		
		BigDecimal saldoatual = contaDeposito.getSaldo();
		BigDecimal saldoPosDeposito = saldoatual.add(depositoRequest.valor());
		contaDeposito.setSaldo(saldoPosDeposito);

		Transacao transacao = Transacao.builder().withContaDestino(contaDeposito).withContaOrigem(null)
				.withData(LocalDateTime.now()).withDescricao(depositoRequest.descricao()).withSaldoAnterior(saldoatual)
				.withSaldoPosterior(saldoPosDeposito).withTipo(TipoTransacao.DEPOSITO)
				.withValor(depositoRequest.valor()).build();

		repConta.save(contaDeposito);
		Transacao transacaoSalva = repTransacao.save(transacao);

		return mapeadorTransacao.transacaoParaDto(transacaoSalva);
	}

	private Conta contaDoUsuarioAutenticado() {
		String login = SecurityContextHolder.getContext().getAuthentication().getName();
		Usuario usuario = repUsuario.findByLogin(login)
				.orElseThrow(() -> new UsuarioNaoEncontradoException("usuario não encontado!"));
		Conta conta = repConta.findByUsuario(usuario.getId())
				.orElseThrow(() -> new ContaNaoEncontradaException("Conta não encontrada!"));
		return conta;
	}

	private void validarConta(Conta conta) {
		if (conta.getStatus() == StatusDaConta.BLOQUEADA || conta.getStatus() == StatusDaConta.ENCERRADA) {
			throw new ContaBloqueadaException("indisponível, sua conta está " + conta.getStatus());
		}
	}
}
