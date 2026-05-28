package com.jks.bank.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record PixRequestDto (
		@NotBlank String chavePix,
		
		@NotNull @Positive BigDecimal valor,
		
		@NotBlank String senha,
		
		@Size(max = 255) String descricao
		){}
