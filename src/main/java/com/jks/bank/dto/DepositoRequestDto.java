package com.jks.bank.dto;

import java.math.BigDecimal;

public record DepositoRequestDto (
		BigDecimal valor, 
		String senha,
		String descricao
		) {}
