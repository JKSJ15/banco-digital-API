package com.jks.bank.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record TransferenciaRequestDto(@NotNull(message = "id destino não pode ser nulo!") @Schema(example = "2") Long idContaDestino,
		@NotNull(message = "valor não pode ser nulo!") @Positive(message = "valor inválido") @Schema(example = "300.5") BigDecimal valor,
		@NotBlank(message = "senha inválida!") @Schema(example = "123") String senha,
		@Size(max = 255, message = "descrição não pode ultrapassar 255 caracteres!") @Schema(example = "transferência para abreu encanador!") String descricao) {

}
