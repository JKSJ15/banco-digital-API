package com.jks.bank.mapeamento;

import com.jks.bank.dto.TransacaoResponseDto;
import com.jks.bank.entidades.Conta;
import com.jks.bank.entidades.Transacao;
import com.jks.bank.repositorios.RepositorioConta;

public class MapeamentoDeTransacao {
	private final RepositorioConta rep;

	public MapeamentoDeTransacao(RepositorioConta rep) {
		super();
		this.rep = rep;
	}

	public Transacao dtoParaTransacao(TransacaoResponseDto dto) {
		Conta destino = rep.findById(dto.idContaDestino()).orElseThrow();
		Conta origem = rep.findById(dto.idContaOrigem()).orElseThrow();
		return Transacao.builder().withTipo(dto.tipo()).withValor(dto.valor()).withData(dto.data())
				.withDescricao(dto.descricao()).withContaOrigem(origem).withContaDestino(destino)
				.withSaldoAnterior(dto.saldoAnterior()).withSaldoPosterior(dto.saldoPosterior()).build();
	}

	public TransacaoResponseDto transacaoParaDto(Transacao transacao) {
		return new TransacaoResponseDto(transacao.getId(), transacao.getTipo(), transacao.getValor(),
				transacao.getData(), transacao.getDescricao(), transacao.getContaOrigem().getId(), transacao.getContaDestino().getId(),
				transacao.getSaldoAnterior(), transacao.getSaldoPosterior());
	}
}
