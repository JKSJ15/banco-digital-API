package com.jks.bank.servicos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.jks.bank.dto.DepositoRequestDto;
import com.jks.bank.dto.PixRequestDto;
import com.jks.bank.dto.RelatorioDto;
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
	private final PasswordEncoder passwordEncoder;
	private static final BigDecimal LIMITE_TRANSFERENCIA = BigDecimal.valueOf(10000);
	private static final BigDecimal LIMITE_SAQUE = BigDecimal.valueOf(5000);
	private static final BigDecimal LIMITE_PIX = BigDecimal.valueOf(10000);
	private static final BigDecimal LIMITE_PIX_DIARIO = BigDecimal.valueOf(20000);
	private static final BigDecimal LIMITE_DEPOSITO = BigDecimal.valueOf(50000);

	public ServicoTransacoes(RepositorioConta repConta, RepositorioTransacao repTransacao,
			RepositorioUsuario repUsuario, PasswordEncoder passwordEncoder) {
		super();
		this.repConta = repConta;
		this.repTransacao = repTransacao;
		this.repUsuario = repUsuario;
		this.passwordEncoder = passwordEncoder;
	}

	public Page<TransacaoResponseDto> extrato(Pageable pageable, LocalDate inicio, LocalDate fim) {
		Conta conta = contaDoUsuarioAutenticado();
		if (inicio != null && fim != null) {
			if (fim.isBefore(inicio)) {
				throw new ValorInvalidoException("a data inicial não pode ser posterior à data final!");
			}
			return repTransacao
					.buscarExtratoPorPeriodo(conta.getId(), inicio.atStartOfDay(), fim.atTime(23, 59, 59), pageable)
					.map(MapeamentoDeTransacao::transacaoParaDto);
		}
		return repTransacao.findByContaOrigemIdOrContaDestinoId(conta.getId(), conta.getId(), pageable)
				.map(MapeamentoDeTransacao::transacaoParaDto);
	}

	public RelatorioDto relatorioMensal() {
		Conta conta = contaDoUsuarioAutenticado();

		LocalDate hoje = LocalDate.now();
		LocalDateTime inicioMes = hoje.withDayOfMonth(1).atStartOfDay();
		LocalDateTime fimMes = hoje.withDayOfMonth(hoje.lengthOfMonth()).atTime(23, 59, 59);

		BigDecimal recebido = repTransacao.totalRecebidoMes(conta.getId(), inicioMes, fimMes);
		BigDecimal enviado = repTransacao.totalEnviadoMes(conta.getId(), inicioMes, fimMes);
		Long pix = repTransacao.quantidadePixMes(TipoTransacao.PIX, conta.getId(), inicioMes, fimMes);

		BigDecimal movimentado = recebido.subtract(enviado);

		return new RelatorioDto(conta.getSaldo(), recebido, enviado, pix, movimentado);
	}

	@Transactional
	public TransacaoResponseDto transferencia(TransferenciaRequestDto transferenciaRequest) {
		validarSenha(transferenciaRequest.senha());
		validarLimite(transferenciaRequest.valor(), LIMITE_TRANSFERENCIA, "Tranferência");
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
		validarSenha(pixRequest.senha());
		validarLimite(pixRequest.valor(), LIMITE_PIX, "PIX");
		Conta conta = contaDoUsuarioAutenticado();
		validarLimitePixDiario(conta, pixRequest.valor());
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
		validarSenha(saqueRequest.senha());
		validarLimite(saqueRequest.valor(), LIMITE_SAQUE, "Saque");
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
		validarSenha(depositoRequest.senha());
		validarLimite(depositoRequest.valor(), LIMITE_DEPOSITO, "Depósito");
		Conta contaDeposito = contaDoUsuarioAutenticado();

		BigDecimal saldoAtual = contaDeposito.getSaldo();
		depositar(contaDeposito, depositoRequest.valor());

		Transacao transacaoSalva = realizarTransacao(TipoTransacao.DEPOSITO, contaDeposito, null,
				depositoRequest.valor(), depositoRequest.descricao(), saldoAtual, contaDeposito.getSaldo());

		repConta.save(contaDeposito);
		return MapeamentoDeTransacao.transacaoParaDto(transacaoSalva);
	}

	// MÉTODOS INTERNOS
	private void validarLimitePixDiario(Conta conta, BigDecimal valorRequest) {
		LocalDate hoje = LocalDate.now();
		BigDecimal soma = repTransacao.somarValoresPorPeriodo(TipoTransacao.PIX, conta.getId(), hoje.atStartOfDay(),
				hoje.atTime(23, 59, 59));

		if (soma == null) {
			soma = BigDecimal.ZERO;
		}
		if (soma.add(valorRequest).compareTo(LIMITE_PIX_DIARIO) > 0) {
			throw new ValorInvalidoException("limite diário de PIX excedido!");
		}
	}

	private void validarLimite(BigDecimal valor, BigDecimal limite, String operacao) {
		if (valor.compareTo(limite) > 0) {
			throw new ValorInvalidoException(operacao + " excede o limite permitido!");
		}
	}

	private void depositar(Conta conta, BigDecimal valor) {
		if (conta.getStatus() == StatusDaConta.ENCERRADA) {
			throw new ContaEncerradaException(
					"indisponível, a conta ID:" + conta.getId() + " está " + conta.getStatus());
		}
		BigDecimal saldoatual = conta.getSaldo();
		conta.setSaldo(saldoatual.add(valor));
	}

	private void sacar(Conta conta, BigDecimal valor) {
		BigDecimal saldoAtual = conta.getSaldo();
		if (saldoAtual.compareTo(valor) < 0) {
			throw new SaldoInsuficienteException("saldo insuficiente!");
		}
		conta.setSaldo(saldoAtual.subtract(valor));
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
		Conta conta = repConta.findByUsuarioLogin(login)
				.orElseThrow(() -> new ContaNaoEncontradaException("Conta não encontrada!"));
		return conta;
	}

	private void validarMovimentacaoDeSaida(Conta conta) {
		if (conta.getStatus() == StatusDaConta.BLOQUEADA || conta.getStatus() == StatusDaConta.ENCERRADA) {
			throw new ContaBloqueadaException("indisponível, sua conta está " + conta.getStatus());
		}
	}

	private void validarSenha(String senha) {
		String login = SecurityContextHolder.getContext().getAuthentication().getName();
		Usuario usuario = repUsuario.findByLogin(login)
				.orElseThrow(() -> new UsuarioNaoEncontradoException("usuario não encontado!"));
		if (!passwordEncoder.matches(senha, usuario.getPassword())) {
			throw new SenhaInvalidaException("senha inválida!");
		}
	}
}
