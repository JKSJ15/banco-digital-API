package com.jks.bank.dto;

import java.math.BigDecimal;

public record SaqueRequestDto (
		BigDecimal valor, 
		String senha,
		String descricao
		) {}
