package com.jks.bank.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados necessários para realizar uma transferência via PIX.")
public record PixRequestDto(
		@NotNull(message = "chave pix não pode ser nula!") @Schema(description = "Chave PIX da conta destinatária.", example = "00ae7bdf-2648-45fe-8bf4-8fb008117c35") String chavePix,
		@NotNull(message = "valor não pode ser nulo!") @Positive(message = "valor inválido!") @Schema(description = "Valor a ser transferido.", example = "300.50") BigDecimal valor,
		@NotBlank(message = "senha inválida!") @Schema(description = "Senha da conta para autorização da transferência.", example = "123") String senha,
		@Size(max = 255, message = "descrição não pode ultrapassar 255 caracteres!") @Schema(description = "Descrição opcional para identificação da transferência.", example = "PIX referente à viagem.") String descricao
) {
}
