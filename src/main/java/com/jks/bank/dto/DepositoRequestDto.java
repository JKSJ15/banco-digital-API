package com.jks.bank.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados necessários para realizar um depósito em conta.")
public record DepositoRequestDto(
		@NotNull(message = "valor não pode ser nulo!") @Positive(message = "valor inválido!") @Schema(description = "Valor a ser depositado na conta.", example = "300.50") BigDecimal valor,
		@NotBlank(message = "senha inválida!") @Schema(description = "Senha da conta para autorização da operação.", example = "123") String senha,
		@Size(max = 255, message = "descricao não pode ultrapassar 255 caracteres!") @Schema(description = "Descrição opcional para identificar o depósito.", example = "Depósito referente ao salário do mês.") String descricao
) {
}
