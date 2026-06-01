package com.jks.bank.dto;

import jakarta.validation.constraints.NotBlank;

public record SenhaDto(@NotBlank(message = "senha inválida!") String senha) {
}
