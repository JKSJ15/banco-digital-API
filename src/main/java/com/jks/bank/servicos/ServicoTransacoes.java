package com.jks.bank.servicos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
	private static final Logger log = LoggerFactory.getLogger(ServicoTransacoes.class);
	private final RepositorioConta repConta;
	private final RepositorioTransacao repTransacao;
	private final RepositorioUsuario repUsuario;
	private final PasswordEncoder passwordEncoder;
	private static final BigDecimal LIMITE_TRANSFERENCIA = BigDecimal.valueOf(10000);
	private static final BigDecimal LIMITE_SAQUE = BigDecimal.valueOf(5000);
	private static final BigDecimal LIMITE_PIX = BigDecimal.valueOf(10000);
	private static final BigDecimal LIMITE_PIX_DIARIO = BigDecimal.valueOf(20000);
	private static final BigDecimal LIMITE_DEPOSITO = BigDecimal.valueOf(50000);
	private static final String AMARELO = "\u001B[33m";
	private static final String VERDE = "\u001B[32m";
	private static final String RESETAR = "\u001B[0m";

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
		log.info(VERDE + "extrato requisitado para conta {}, pelo usuário {}. filtros: inicio {}, fim {}" + RESETAR,
				conta.getId(), conta.getUsuario().getUsername(), inicio, fim);
		if (inicio != null && fim != null) {
			if (fim.isBefore(inicio)) {
				log.warn(AMARELO + "data fim está antes de inicio!" + RESETAR);
				throw new ValorInvalidoException("a data inicial não pode ser posterior à data final!");
			}
			log.info(VERDE + "extrato retornado com sucesso para conta {}" + RESETAR, conta.getId());
			return repTransacao
					.buscarExtratoPorPeriodo(conta.getId(), inicio.atStartOfDay(), fim.atTime(23, 59, 59), pageable)
					.map(MapeamentoDeTransacao::transacaoParaDto);
		}
		log.info(VERDE + "extrato retornado com sucesso para conta {}" + RESETAR, conta.getId());
		return repTransacao.findByContaOrigemIdOrContaDestinoId(conta.getId(), conta.getId(), pageable)
				.map(MapeamentoDeTransacao::transacaoParaDto);
	}

	public RelatorioDto relatorioMensal() {
		Conta conta = contaDoUsuarioAutenticado();
		log.info(VERDE + "relatório mensal requisitado! conta: {}, usuário: {} " + RESETAR, conta.getId(),
				conta.getUsuario().getUsername());

		LocalDate hoje = LocalDate.now();
		LocalDateTime inicioMes = hoje.withDayOfMonth(1).atStartOfDay();
		LocalDateTime fimMes = hoje.withDayOfMonth(hoje.lengthOfMonth()).atTime(23, 59, 59);

		BigDecimal recebido = repTransacao.totalRecebidoMes(conta.getId(), inicioMes, fimMes);
		BigDecimal enviado = repTransacao.totalEnviadoMes(conta.getId(), inicioMes, fimMes);
		Long pix = repTransacao.quantidadePixMes(TipoTransacao.PIX, conta.getId(), inicioMes, fimMes);

		BigDecimal movimentado = recebido.subtract(enviado);

		log.info(VERDE + "relatório mensal gerado para conta {}" + RESETAR, conta.getId());
		return new RelatorioDto(conta.getSaldo(), recebido, enviado, pix, movimentado);
	}

	@Transactional
	public TransacaoResponseDto transferencia(TransferenciaRequestDto transferenciaRequest) {
		log.info(VERDE + "transferencia requisitada" + RESETAR);
		validarSenha(transferenciaRequest.senha());
		validarLimite(transferenciaRequest.valor(), LIMITE_TRANSFERENCIA, "Tranferência");
		Conta conta = contaDoUsuarioAutenticado();
		validarMovimentacaoDeSaida(conta);
		Conta contaDestino = repConta.findById(transferenciaRequest.idContaDestino())
				.orElseThrow(() -> new ContaNaoEncontradaException("conta destino não encontrada!"));

		if (conta.getId().equals(contaDestino.getId())) {
			log.warn(AMARELO + "transferência inválida! contaOrigem:{}, contaDestino:{}, valor:{}" + RESETAR,
					conta.getId(), contaDestino.getId(), transferenciaRequest.valor());
			throw new TransferenciaInvalidaException("transferência inválida!");
		}

		BigDecimal saldoatual = conta.getSaldo();
		sacar(conta, transferenciaRequest.valor());
		depositar(contaDestino, transferenciaRequest.valor());

		Transacao transacaoSalva = realizarTransacao(TipoTransacao.TRANSFERENCIA, contaDestino, conta,
				transferenciaRequest.valor(), transferenciaRequest.descricao(), saldoatual, conta.getSaldo());

		repConta.save(conta);
		repConta.save(contaDestino);
		log.info(VERDE + "transferência realizada! contaOrigem:{}, contaDestino:{}, valor:{}" + RESETAR, conta.getId(),
				contaDestino.getId(), transferenciaRequest.valor());
		return MapeamentoDeTransacao.transacaoParaDto(transacaoSalva);
	}

	@Transactional
	public TransacaoResponseDto pix(PixRequestDto pixRequest) {
		log.info(VERDE + "pix requisitado" + RESETAR);
		validarSenha(pixRequest.senha());
		validarLimite(pixRequest.valor(), LIMITE_PIX, "PIX");
		Conta conta = contaDoUsuarioAutenticado();
		validarLimitePixDiario(conta, pixRequest.valor());
		validarMovimentacaoDeSaida(conta);
		Conta contaDestino = repConta.findByChavePix(pixRequest.chavePix())
				.orElseThrow(() -> new ContaNaoEncontradaException("conta destino não encontrada!"));

		if (conta.getChavePix().equals(contaDestino.getChavePix())) {
			log.warn(AMARELO + "pix inválido! contaOrigem:{}, contaDestino:{}, valor:{}" + RESETAR, conta.getId(),
					contaDestino.getId(), pixRequest.valor());
			throw new TransferenciaInvalidaException("transferência inválida!");
		}

		BigDecimal saldoatual = conta.getSaldo();
		sacar(conta, pixRequest.valor());
		depositar(contaDestino, pixRequest.valor());

		Transacao transacaoSalva = realizarTransacao(TipoTransacao.PIX, contaDestino, conta, pixRequest.valor(),
				pixRequest.descricao(), saldoatual, conta.getSaldo());

		repConta.save(conta);
		repConta.save(contaDestino);
		log.info(VERDE + "pix realizado! contaOrigem:{}, contaDestino:{}, valor:{}" + RESETAR, conta.getId(),
				contaDestino.getId(), pixRequest.valor());
		return MapeamentoDeTransacao.transacaoParaDto(transacaoSalva);
	}

	@Transactional
	public TransacaoResponseDto saque(SaqueRequestDto saqueRequest) {
		log.info(VERDE + "saque requisitado!" + RESETAR);
		validarSenha(saqueRequest.senha());
		validarLimite(saqueRequest.valor(), LIMITE_SAQUE, "Saque");
		Conta conta = contaDoUsuarioAutenticado();
		validarMovimentacaoDeSaida(conta);

		BigDecimal saldoatual = conta.getSaldo();
		sacar(conta, saqueRequest.valor());

		Transacao transacaoSalva = realizarTransacao(TipoTransacao.SAQUE, null, conta, saqueRequest.valor(),
				saqueRequest.descricao(), saldoatual, conta.getSaldo());

		repConta.save(conta);
		log.info(VERDE + "saque realizado! conta: {}, usuário: {}, valor: {}" + RESETAR, conta.getId(),
				conta.getUsuario().getUsername(), saqueRequest.valor());
		return MapeamentoDeTransacao.transacaoParaDto(transacaoSalva);
	}

	@Transactional
	public TransacaoResponseDto deposito(DepositoRequestDto depositoRequest) {
		log.info(VERDE + "depósito requisitado!" + RESETAR);
		validarSenha(depositoRequest.senha());
		validarLimite(depositoRequest.valor(), LIMITE_DEPOSITO, "Depósito");
		Conta contaDeposito = contaDoUsuarioAutenticado();

		BigDecimal saldoAtual = contaDeposito.getSaldo();
		depositar(contaDeposito, depositoRequest.valor());

		Transacao transacaoSalva = realizarTransacao(TipoTransacao.DEPOSITO, contaDeposito, null,
				depositoRequest.valor(), depositoRequest.descricao(), saldoAtual, contaDeposito.getSaldo());

		repConta.save(contaDeposito);
		log.info(VERDE + "depósito realizado!  conta: {}, usuário: {}, valor: {}" + RESETAR, contaDeposito.getId(),
				contaDeposito.getUsuario().getUsername(), depositoRequest.valor());
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
			log.warn(AMARELO + "limite de pix diários da conta {} excedido! limite: {}, realizados: {}" + RESETAR,
					conta.getId(), LIMITE_PIX_DIARIO, soma);
			throw new ValorInvalidoException("limite diário de PIX excedido!");
		}
	}

	private void validarLimite(BigDecimal valor, BigDecimal limite, String operacao) {
		if (valor.compareTo(limite) > 0) {
			log.warn(AMARELO + "operacao {} excede o limite. valor={}, limite={}" + RESETAR, operacao, valor, limite);
			throw new ValorInvalidoException(operacao + " excede o limite permitido!");
		}
	}

	private void depositar(Conta conta, BigDecimal valor) {
		if (conta.getStatus() == StatusDaConta.ENCERRADA) {
			log.warn(AMARELO + "conta {} está encerrada!" + RESETAR, conta.getId());
			throw new ContaEncerradaException(
					"indisponível, a conta ID:" + conta.getId() + " está " + conta.getStatus());
		}
		BigDecimal saldoatual = conta.getSaldo();
		conta.setSaldo(saldoatual.add(valor));
	}

	private void sacar(Conta conta, BigDecimal valor) {
		BigDecimal saldoAtual = conta.getSaldo();
		if (saldoAtual.compareTo(valor) < 0) {
			log.warn(AMARELO + "saldo da conta {} insuficiente para o saque de {}" + RESETAR, conta.getId(), valor);
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
		log.debug("buscando conta do usuário {}", login);
		Conta conta = repConta.findByUsuarioLogin(login)
				.orElseThrow(() -> new ContaNaoEncontradaException("Conta não encontrada!"));
		log.debug("conta {} encontrada!", conta.getId());
		return conta;
	}

	private void validarMovimentacaoDeSaida(Conta conta) {
		if (conta.getStatus() == StatusDaConta.BLOQUEADA || conta.getStatus() == StatusDaConta.ENCERRADA) {
			log.warn(AMARELO + "indisponível, a conta está {}" + RESETAR, conta.getStatus());
			throw new ContaBloqueadaException("indisponível, sua conta está " + conta.getStatus());
		}
	}

	private void validarSenha(String senha) {
		String login = SecurityContextHolder.getContext().getAuthentication().getName();
		Usuario usuario = repUsuario.findByLogin(login)
				.orElseThrow(() -> new UsuarioNaoEncontradoException("usuario não encontado!"));
		if (!passwordEncoder.matches(senha, usuario.getPassword())) {
			log.warn(AMARELO + "senha do usuário {} incorreta!" + RESETAR, login);
			throw new SenhaInvalidaException("senha inválida!");
		}
	}
}
