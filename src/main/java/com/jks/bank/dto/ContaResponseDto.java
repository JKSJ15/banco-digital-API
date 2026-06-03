package com.jks.bank.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.jks.bank.entidades.StatusDaConta;

public record ContaResponseDto(Long id, String agencia, String numero, String chavePix, BigDecimal saldo,
		StatusDaConta status, LocalDate dataCriacao, String cep, String bairro, String localidade, String uf,
		String estado, String regiao) {
}
