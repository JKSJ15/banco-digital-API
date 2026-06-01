package com.jks.bank.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequestDto(@NotBlank(message = "refresh Token inválido!") String refreshToken) {
}
