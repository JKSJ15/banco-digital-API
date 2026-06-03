package com.jks.bank.servicos;

import org.springframework.stereotype.Service;

import com.jks.bank.cliente.ApiCep;
import com.jks.bank.dto.CepResponseDto;
import com.jks.bank.exceptions.CepInvalidoException;

@Service
public class ServicoApiCep {
	private final ApiCep clienteViaCep;

	public ServicoApiCep(ApiCep clienteViaCep) {
		this.clienteViaCep = clienteViaCep;
	}

	public CepResponseDto buscarEndereco(String cep) {
		cep = cep.replaceAll("\\D", "");
		if (cep.length() != 8) {
			throw new CepInvalidoException("cep inválido!");
		}
		return clienteViaCep.buscarCep(cep);
	}
}
