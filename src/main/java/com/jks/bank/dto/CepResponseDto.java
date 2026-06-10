package com.jks.bank.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados de endereço retornados pela API ViaCEP a partir de um CEP informado.")
public record CepResponseDto(@Schema(description = "CEP do endereço", example = "73730000") String cep,
		@Schema(description = "Nome da rua, avenida ou logradouro", example = "Quadra 12") String logradouro,
		@Schema(description = "Informações complementares do endereço", example = "Lote 15") String complemento,
		@Schema(description = "Unidade administrativa vinculada ao endereço", example = "") String unidade,
		@Schema(description = "Bairro do endereço", example = "Setor Central") String bairro,
		@Schema(description = "Cidade do endereço", example = "Santo Antônio do Descoberto") String localidade,
		@Schema(description = "Sigla da unidade federativa", example = "GO") String uf,
		@Schema(description = "Nome completo do estado", example = "Goiás") String estado,
		@Schema(description = "Região geográfica do Brasil", example = "Centro-Oeste") String regiao,
		@Schema(description = "Código IBGE do município", example = "5219753") String ibge,
		@Schema(description = "Código GIA (utilizado principalmente em São Paulo)", example = "") String gia,
		@Schema(description = "DDD telefônico da localidade", example = "61") String ddd,
		@Schema(description = "Código SIAFI do município", example = "9493") String siafi,
		@Schema(description = "Indica se ocorreu erro na consulta do CEP", example = "false") Boolean erro) {
}
