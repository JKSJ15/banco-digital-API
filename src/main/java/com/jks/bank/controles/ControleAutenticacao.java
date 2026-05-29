package com.jks.bank.controles;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jks.bank.dto.LoginRequestDto;
import com.jks.bank.dto.LoginResponseDto;
import com.jks.bank.dto.RefreshRequestDto;
import com.jks.bank.dto.RefreshResponseDto;
import com.jks.bank.dto.RegistroRequestDto;
import com.jks.bank.dto.RegistroResponseDto;
import com.jks.bank.servicos.ServicoAutenticacao;

@RestController
@RequestMapping("/auth")
public class ControleAutenticacao {
	private final ServicoAutenticacao servicoAutenticacao;

	public ControleAutenticacao(ServicoAutenticacao servicoAutenticacao) {
		super();
		this.servicoAutenticacao = servicoAutenticacao;
	}

	@PostMapping("/login")
	public ResponseEntity<LoginResponseDto> login(LoginRequestDto request) {
		servicoAutenticacao.login(request);
		return ResponseEntity.ok().build();
	}

	@PostMapping("/registro")
	public ResponseEntity<RegistroResponseDto> registro(RegistroRequestDto request) {
		servicoAutenticacao.registro(request);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/refresh")
	public ResponseEntity<RefreshResponseDto> refresh(RefreshRequestDto request) {
		servicoAutenticacao.refresh(request);
		return ResponseEntity.ok().build();
	}
}
