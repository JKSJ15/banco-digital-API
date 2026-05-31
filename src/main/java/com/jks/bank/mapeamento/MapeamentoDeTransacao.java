package com.jks.bank.mapeamento;

import com.jks.bank.dto.TransacaoResponseDto;
import com.jks.bank.entidades.Transacao;

public class MapeamentoDeTransacao {
	public static TransacaoResponseDto transacaoParaDto(Transacao transacao) {
		return new TransacaoResponseDto(transacao.getId(), transacao.getTipo(), transacao.getValor(),
				transacao.getData(), transacao.getDescricao(),
				transacao.getContaOrigem() != null ? transacao.getContaOrigem().getId() : null,
				transacao.getContaDestino() != null ? transacao.getContaDestino().getId() : null,
				transacao.getSaldoAnterior(), transacao.getSaldoPosterior());
	}
}
