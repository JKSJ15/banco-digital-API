package com.jks.bank.dto;

import com.jks.bank.entidades.StatusDaConta;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CriarContaDto(
	    @NotBlank
	    String agencia,

	    @NotNull
	    Long numero,

	    @NotNull
	    StatusDaConta status,

	    @NotNull
	    Long usuarioId

	) {}
