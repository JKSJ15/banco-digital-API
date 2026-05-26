package com.jks.bank.dto;

import java.math.BigDecimal;

import com.jks.bank.entidades.TipoTransacao;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record TransacaoDto(
		@NotNull TipoTransacao tipo,

		@NotNull @Positive BigDecimal valor,

		@NotBlank @Size(max = 255) String descricao,

		@NotNull Long idContaOrigem,

		Long idContaDestino) {}
