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

	public ServicoApiCep(ApiCep clienteViaCep) {
		this.clienteViaCep = clienteViaCep;
	}

	public CepResponseDto buscarEndereco(String cep) {
		log.info("buscando endereco do cep {}", cep);
		cep = cep.replaceAll("\\D", "");
		if (cep.length() != 8) {
			log.warn("cep {} inválido!", cep);
			throw new CepInvalidoException("cep inválido!");
		}
		return clienteViaCep.buscarCep(cep);
	}
}
