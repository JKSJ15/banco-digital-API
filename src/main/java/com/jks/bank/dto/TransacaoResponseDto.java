package com.jks.bank.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.jks.bank.entidades.TipoTransacao;

public record TransacaoResponseDto(Long id, TipoTransacao tipo, BigDecimal valor, LocalDateTime data, String descricao,
		Long idContaOrigem, Long idContaDestino, BigDecimal saldoAnterior, BigDecimal saldoPosterior) {
}
