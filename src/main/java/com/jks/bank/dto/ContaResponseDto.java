package com.jks.bank.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.jks.bank.entidades.StatusDaConta;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados da conta bancária e do endereço associado ao titular.")
public record ContaResponseDto(
		@Schema(description = "Identificador único da conta.", example = "1") Long id,
		@Schema(description = "Número da agência bancária.", example = "001") String agencia,
		@Schema(description = "Número da conta bancária.", example = "358688") String numero,
		@Schema(description = "Chave PIX vinculada à conta.", example = "f6c5ad84-ed19-4b94-9077-a82efc037f57") String chavePix,
		@Schema(description = "Saldo atual disponível na conta.", example = "300.50") BigDecimal saldo,
		@Schema(description = "Status atual da conta bancária.", example = "ATIVA") StatusDaConta status,
		@Schema(description = "Data de criação da conta.", example = "2026-06-10") LocalDate dataCriacao,
		@Schema(description = "CEP do endereço do titular.", example = "73730000") String cep,
		@Schema(description = "Bairro do endereço do titular.", example = "Vila Aruana") String bairro,
		@Schema(description = "Cidade do endereço do titular.", example = "Mimoso de Goiás") String localidade,
		@Schema(description = "Sigla da unidade federativa do endereço.", example = "GO") String uf,
		@Schema(description = "Nome completo do estado do endereço.", example = "Goiás") String estado,
		@Schema(description = "Região geográfica do endereço.", example = "Centro-Oeste") String regiao

) {
}
