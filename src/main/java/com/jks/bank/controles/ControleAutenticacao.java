package com.jks.bank.controles;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jks.bank.dto.LoginRequestDto;
import com.jks.bank.dto.RefreshRequestDto;
import com.jks.bank.dto.TokensResponse;
import com.jks.bank.dto.RegistroRequestDto;
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
	public ResponseEntity<TokensResponse> login(@RequestBody LoginRequestDto request) {
		return ResponseEntity.ok(servicoAutenticacao.login(request));
	}

	@PostMapping("/registro")
	public ResponseEntity<Void> registro(@RequestBody RegistroRequestDto request) {
		servicoAutenticacao.registro(request);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/refresh")
	public ResponseEntity<TokensResponse> refresh(@RequestBody RefreshRequestDto request) {
		servicoAutenticacao.refresh(request);
		return ResponseEntity.ok().build();
	}
}
