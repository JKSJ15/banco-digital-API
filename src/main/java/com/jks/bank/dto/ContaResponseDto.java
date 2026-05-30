package com.jks.bank.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.jks.bank.entidades.StatusDaConta;

public record ContaResponseDto(Long id, Long agencia, String numero, String chavePix, BigDecimal saldo,
		StatusDaConta status, LocalDate dataCriacao) {
}
