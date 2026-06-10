package com.jks.bank.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Credenciais utilizadas para autenticação do usuário.")
public record LoginRequestDto(
		@NotBlank(message = "login inválido!") @Schema(description = "Login do usuário, podendo ser e-mail ou nome de usuário cadastrado.", example = "teste@email.com") String login,
		@NotBlank(message = "senha inválida!") @Schema(description = "Senha de acesso da conta.", example = "123") String senha
) {
}
