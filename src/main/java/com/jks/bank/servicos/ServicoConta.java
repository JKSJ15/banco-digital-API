package com.jks.bank.servicos;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.jks.bank.dto.ContaResponseDto;
import com.jks.bank.entidades.Conta;
import com.jks.bank.entidades.Usuario;
import com.jks.bank.exceptions.ContaNaoEncontradaException;
import com.jks.bank.exceptions.UsuarioNaoEncontradoException;
import com.jks.bank.mapeamento.MapeamentoDeConta;
import com.jks.bank.repositorios.RepositorioConta;
import com.jks.bank.repositorios.RepositorioUsuario;

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

	public void bloquearConta() {
		Conta conta = contaDoUsuarioAutenticado();
		conta.bloquearConta();
	}

	public void desbloquearConta() {
		Conta conta = contaDoUsuarioAutenticado();
		conta.desbloquearConta();
	}

	private Conta contaDoUsuarioAutenticado() {
		String login = SecurityContextHolder.getContext().getAuthentication().getName();
		Usuario usuario = repUsuario.findByLogin(login)
				.orElseThrow(() -> new UsuarioNaoEncontradoException("usuario não encontado!"));
		Conta conta = repConta.findByUsuario(usuario.getId())
				.orElseThrow(() -> new ContaNaoEncontradaException("Conta não encontrada!"));
		return conta;
	}
}
