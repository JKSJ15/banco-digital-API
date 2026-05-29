package com.jks.bank.mapeamento;

import com.jks.bank.dto.TransacaoResponseDto;
import com.jks.bank.entidades.Transacao;

public class MapeamentoDeTransacao {
	public static TransacaoResponseDto transacaoParaDto(Transacao transacao) {
		return new TransacaoResponseDto(transacao.getId(), transacao.getTipo(), transacao.getValor(),
				transacao.getData(), transacao.getDescricao(), transacao.getContaOrigem().getId(),
				transacao.getContaDestino().getId(), transacao.getSaldoAnterior(), transacao.getSaldoPosterior());
	}
}
