package com.jks.bank.dto;

import java.math.BigDecimal;

import com.jks.bank.entidades.StatusDaConta;

public record ContaResponseDto(Long id, String agencia, Long numero, BigDecimal saldo, StatusDaConta status) {
}
