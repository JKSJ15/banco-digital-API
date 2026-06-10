package com.jks.bank.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Relatório consolidado da movimentação financeira da conta no último mês.")
public record RelatorioDto(
		@Schema(description = "Saldo atual disponível na conta.", example = "300.50") BigDecimal saldoAtual,
		@Schema(description = "Valor total recebido pela conta nos últimos 30 dias.", example = "20000.00") BigDecimal recebidoNoUltimoMes,
		@Schema(description = "Valor total enviado pela conta nos últimos 30 dias.", example = "4000.00") BigDecimal enviadoNoUltimoMes,
		@Schema(description = "Quantidade de transferências PIX realizadas no último mês.", example = "7") Long numeroPixNoMes,
		@Schema(description = "Valor total movimentado na conta durante o último mês.", example = "16000.00") BigDecimal saldoMovimentadoMes
) {
}
