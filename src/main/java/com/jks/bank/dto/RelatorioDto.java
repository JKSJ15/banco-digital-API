package com.jks.bank.dto;

import java.math.BigDecimal;

public record RelatorioDto(BigDecimal saldoAtual, BigDecimal recebidoNoUltimoMes, BigDecimal enviadoNoUltimoMes,
		Long numeroPixNoMes, BigDecimal saldoMovimentadoMes) {
}
