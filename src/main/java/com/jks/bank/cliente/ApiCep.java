package com.jks.bank.cliente;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.jks.bank.dto.CepResponseDto;

@Component
public class ApiCep {
	private final RestClient restClient;

	public ApiCep(RestClient restClient) {
		super();
		this.restClient = restClient;
	}

	public CepResponseDto buscarCep(String cep) {
		try {
		return restClient.get()
				.uri("https://viacep.com.br/ws/{cep}/json/", cep)
				.retrieve()
				.body(CepResponseDto.class);
		} catch (Exception e) {
			 return null;
		}
	}
}
