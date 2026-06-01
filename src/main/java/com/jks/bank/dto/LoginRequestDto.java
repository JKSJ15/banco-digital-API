package com.jks.bank.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequestDto(@NotBlank(message = "login inválido!") String login,
		@NotBlank(message = "senha inválida!") String senha) {
}
