package com.jks.bank.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record TransferenciaRequestDto (
		@NotNull Long idContaDestino,
		
		@NotNull @Positive(message = "valor inválido") BigDecimal valor,
		
		@NotBlank(message = "senha inválida!") String senha,
		
		@Size(max = 255) String descricao
		){

}
