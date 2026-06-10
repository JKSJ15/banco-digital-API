package com.jks.bank.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Dados necessários para solicitar um novo token de acesso utilizando um refresh token válido.")
public record RefreshRequestDto(
		@NotBlank(message = "refresh Token inválido!")
		@Schema(description = "JWT Refresh Token utilizado para obter um novo Access Token. Deve ser enviado no cabeçalho Authorization com o prefixo Bearer.",
		example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBlbWFpbC5jb20ifQ.signature") String refreshToken) {
}
