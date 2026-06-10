package com.jks.bank.servicos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.jks.bank.cliente.ApiCep;
import com.jks.bank.dto.CepResponseDto;
import com.jks.bank.exceptions.CepInvalidoException;

@Service
public class ServicoApiCep {
	private final ApiCep clienteViaCep;
	private static final Logger log = LoggerFactory.getLogger(ServicoApiCep.class);
	private static final String AMARELO = "\u001B[33m";
	private static final String VERDE = "\u001B[32m";
	private static final String RESETAR = "\u001B[0m";

	public ServicoApiCep(ApiCep clienteViaCep) {
		this.clienteViaCep = clienteViaCep;
	}

	public CepResponseDto buscarEndereco(String cep) {
		log.info(VERDE + "buscando endereco do cep {}" + RESETAR, cep);
		cep = cep.replaceAll("\\D", "");
		if (cep.length() != 8) {
			log.warn(AMARELO + "cep {} inválido!" + RESETAR, cep);
			throw new CepInvalidoException("cep inválido!");
		}
		return clienteViaCep.buscarCep(cep);
	}
}
