package com.jks.bank.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegistroRequestDto(@NotBlank(message = "nome não pode ser nulo!") String nome,
		@NotBlank(message = "cpf não pode ser nulo!") String cpf,
		@NotBlank(message = "login não pode ser nulo!") String login,
		@NotBlank(message = "senha inválida!") String senha,
		@NotNull(message = "data de nascimento não pode ser nula!") LocalDate dataNascimento,
		@NotBlank(message = "telefone não pode ser nulo!") String telefone) {
}
