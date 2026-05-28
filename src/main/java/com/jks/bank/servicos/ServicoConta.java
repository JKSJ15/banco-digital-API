package com.jks.bank.servicos;

import java.math.BigDecimal;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.jks.bank.dto.ContaResponseDto;
import com.jks.bank.dto.SenhaDto;
import com.jks.bank.entidades.Conta;
import com.jks.bank.entidades.Usuario;
import com.jks.bank.exceptions.ContaComDinheiroException;
import com.jks.bank.exceptions.ContaNaoEncontradaException;
import com.jks.bank.exceptions.UsuarioNaoEncontradoException;
import com.jks.bank.mapeamento.MapeamentoDeConta;
import com.jks.bank.repositorios.RepositorioConta;
import com.jks.bank.repositorios.RepositorioUsuario;

import jakarta.transaction.Transactional;

@Service
public class ServicoConta {
	private final RepositorioConta repConta;
	private final RepositorioUsuario repUsuario;
	private final MapeamentoDeConta mapeadorConta;

	public ServicoConta(RepositorioConta repConta, RepositorioUsuario repUsuario, MapeamentoDeConta mapeadorConta) {
		super();
		this.repConta = repConta;
		this.repUsuario = repUsuario;
		this.mapeadorConta = mapeadorConta;
	}

	public ContaResponseDto contaUsuario() {
		Conta conta = contaDoUsuarioAutenticado();
		return mapeadorConta.ContaParadtoResponse(conta);
	}

	@Transactional
	public void bloquearConta(SenhaDto senha) {
		Conta conta = contaDoUsuarioAutenticado();
		conta.bloquearConta();
	}

	@Transactional
	public void desbloquearConta(SenhaDto senha) {
		Conta conta = contaDoUsuarioAutenticado();
		conta.desbloquearConta();
	}

	@Transactional
	public void encerrarConta(SenhaDto senha) {
		Conta conta = contaDoUsuarioAutenticado();
		if (conta.getSaldo().compareTo(BigDecimal.ZERO) > 0) {
			throw new ContaComDinheiroException("não foi possível encerrar sua conta, pois ela contém dinheiro!");
		}
		conta.encerrarConta();
	}

	// CÓDIGO INTERNO

	private Conta contaDoUsuarioAutenticado() {
		String login = SecurityContextHolder.getContext().getAuthentication().getName();
		Usuario usuario = repUsuario.findByLogin(login)
				.orElseThrow(() -> new UsuarioNaoEncontradoException("usuario não encontado!"));
		Conta conta = repConta.findByUsuario(usuario.getId())
				.orElseThrow(() -> new ContaNaoEncontradaException("Conta não encontrada!"));
		return conta;
	}

	// private void validarSenha() {
	// if (!passwordEncoder.matches(
	// senha.senha(),
	// usuario.getSenha()
	// )) {
	// throw new SenhaInvalidaException("senha inválida!");}}
}
