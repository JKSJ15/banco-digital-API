package com.jks.bank.servicos;

import org.springframework.stereotype.Service;

import com.jks.bank.repositorios.RepositorioTransacao;

@Service
public class ServicoTransacoes {
	private final RepositorioTransacao repTransacao;

	public ServicoTransacoes(RepositorioTransacao repTransacao) {
		super();
		this.repTransacao = repTransacao;
	}
	
	
}
