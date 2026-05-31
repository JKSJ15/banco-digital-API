package com.jks.bank.servicos;

import java.math.BigDecimal;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.jks.bank.dto.ContaResponseDto;
import com.jks.bank.dto.SenhaDto;
import com.jks.bank.entidades.Conta;
import com.jks.bank.entidades.Usuario;
import com.jks.bank.exceptions.ContaComDinheiroException;
import com.jks.bank.exceptions.ContaNaoEncontradaException;
import com.jks.bank.exceptions.SenhaInvalidaException;
import com.jks.bank.exceptions.UsuarioNaoEncontradoException;
import com.jks.bank.mapeamento.MapeamentoDeConta;
import com.jks.bank.repositorios.RepositorioConta;
import com.jks.bank.repositorios.RepositorioUsuario;

import jakarta.transaction.Transactional;

@Service
public class ServicoConta {
	private final RepositorioConta repConta;
	private final RepositorioUsuario repUsuario;
	private final PasswordEncoder passwordEncoder;

	public ServicoConta(RepositorioConta repConta, RepositorioUsuario repUsuario, PasswordEncoder passwordEncoder) {
		super();
		this.repConta = repConta;
		this.repUsuario = repUsuario;
		this.passwordEncoder = passwordEncoder;
	}

	public ContaResponseDto contaUsuario() {
		Conta conta = contaDoUsuarioAutenticado();
		return MapeamentoDeConta.ContaParadtoResponse(conta);
	}

	@Transactional
	public void bloquearConta(SenhaDto senha) {
		validarSenha(senha.senha());
		Conta conta = contaDoUsuarioAutenticado();
		conta.bloquearConta();
	}

	@Transactional
	public void desbloquearConta(SenhaDto senha) {
		validarSenha(senha.senha());
		Conta conta = contaDoUsuarioAutenticado();
		conta.desbloquearConta();
	}

	@Transactional
	public void encerrarConta(SenhaDto senha) {
		validarSenha(senha.senha());

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
		Conta conta = repConta.findByUsuarioId(usuario.getId())
				.orElseThrow(() -> new ContaNaoEncontradaException("Conta não encontrada!"));
		return conta;
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
