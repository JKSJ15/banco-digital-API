package com.jks.bank.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record DepositoRequestDto (
		@NotNull(message = "valor não pode ser nulo!") @Positive(message = "valor inválido!") BigDecimal valor, 
		
		@NotBlank(message = "senha inválida!") String senha,
		
		@Size(max = 255, message = "descricao não pode ultrapassar 255 caracteres!") String descricao
		) {}
