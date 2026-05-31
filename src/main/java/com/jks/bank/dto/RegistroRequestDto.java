package com.jks.bank.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegistroRequestDto(@NotBlank String nome, @NotBlank String cpf, @NotBlank String login,
		@NotBlank String senha, @NotNull LocalDate dataNascimento, @NotBlank String telefone) {
}
