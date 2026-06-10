package com.jks.bank.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.jks.bank.entidades.TipoTransacao;

import io.swagger.v3.oas.annotations.media.Schema;

public record TransacaoResponseDto(@Schema(example = "1")Long id,
		@Schema(example = "PIX")TipoTransacao tipo,
		@Schema(example = "300.5")BigDecimal valor,
		@Schema(example = "2026-06-10")LocalDateTime data,
		@Schema(example = "pix da viagem!")String descricao,
		@Schema(example = "1")Long idContaOrigem,
		@Schema(example = "2")Long idContaDestino,
		@Schema(example = "300.5")BigDecimal saldoAnterior,
		@Schema(example = "0")BigDecimal saldoPosterior) {
}
