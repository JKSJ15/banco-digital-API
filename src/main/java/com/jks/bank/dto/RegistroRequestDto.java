package com.jks.bank.dto;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Dados necessários para o cadastro de um novo usuário no sistema.")
public record RegistroRequestDto(
		@NotBlank(message = "nome não pode ser nulo!") @Schema(description = "Nome completo do usuário.", example = "João da Silva") String nome,
		@NotBlank(message = "cpf não pode ser nulo!") @Schema(description = "CPF do usuário contendo apenas números.", example = "99999999999") String cpf,
		@NotBlank(message = "login não pode ser nulo!") @Schema(description = "Login utilizado para autenticação no sistema.", example = "teste@email.com") String login,
		@NotBlank(message = "senha inválida!") @Schema(description = "Senha de acesso da conta.", example = "123") String senha,
		@NotNull(message = "data de nascimento não pode ser nula!") @Schema(description = "Data de nascimento do usuário.", example = "2000-10-27") LocalDate dataNascimento,
		@NotBlank(message = "telefone não pode ser nulo!") @Schema(description = "Telefone do usuário contendo DDD e apenas números.", example = "61999999999") String telefone,
		@NotBlank(message = "cep não pode ser nulo!") @Schema(description = "CEP utilizado para consulta automática do endereço.", example = "73730000") String cep
) {
}
