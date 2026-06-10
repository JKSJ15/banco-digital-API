package com.jks.bank.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Dados necessários para validação da senha do usuário.")
public record SenhaDto(
		@NotBlank(message = "senha inválida!") @Schema(description = "Senha da conta utilizada para autenticação ou autorização de operações.", example = "123") String senha
) {
}
