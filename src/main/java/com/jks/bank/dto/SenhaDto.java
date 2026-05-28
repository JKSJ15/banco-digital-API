package com.jks.bank.dto;

import jakarta.validation.constraints.NotBlank;

public record SenhaDto(
		@NotBlank String senha
		) {}
