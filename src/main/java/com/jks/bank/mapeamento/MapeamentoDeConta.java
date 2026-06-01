package com.jks.bank.mapeamento;

import com.jks.bank.dto.ContaResponseDto;
import com.jks.bank.entidades.Conta;

public class MapeamentoDeConta {

	public static ContaResponseDto ContaParadtoResponse(Conta conta) {
		return new ContaResponseDto(conta.getId(), conta.getAgencia(), conta.getNumero(), conta.getChavePix(),
				conta.getSaldo(), conta.getStatus(), conta.getDataDaCriacao());
	}
}
