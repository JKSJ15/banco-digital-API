package com.jks.bank.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Tokens JWT retornados após autenticação ou renovação de sessão.")
public record TokensResponse(
		@Schema(description = "JWT Access Token utilizado para acessar endpoints protegidos. Deve ser enviado no cabeçalho Authorization com o prefixo Bearer.", example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBlbWFpbC5jb20ifQ.signature") String tokenAcesso,
		@Schema(description = "JWT Refresh Token utilizado para obter um novo Access Token quando o atual expirar. Deve ser enviado ao endpoint /refresh no cabeçalho Authorization com o prefixo Bearer.", example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBlbWFpbC5jb20ifQ.signature") String refreshToken
) {
}
