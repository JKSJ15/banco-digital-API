package com.jks.bank.servicos;

import org.springframework.stereotype.Service;

import com.jks.bank.repositorios.RepositorioConta;

@Service
public class ServicoConta {
	private final RepositorioConta repConta;

	public ServicoConta(RepositorioConta repConta) {
		super();
		this.repConta = repConta;
	}
}
