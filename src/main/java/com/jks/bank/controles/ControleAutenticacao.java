package com.jks.bank.controles;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jks.bank.dto.LoginRequestDto;
import com.jks.bank.dto.RefreshRequestDto;
import com.jks.bank.dto.RegistroRequestDto;
import com.jks.bank.dto.TokensResponse;
import com.jks.bank.entidades.Usuario;
import com.jks.bank.exceptions.UsuarioNaoEncontradoException;
import com.jks.bank.repositorios.RepositorioUsuario;
import com.jks.bank.servicos.ServicoAutenticacao;
import com.jks.bank.servicos.ServicoRefreshToken;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class ControleAutenticacao {
	private final ServicoAutenticacao servicoAutenticacao;
	private final ServicoRefreshToken servicoRefreshToken;
	private final RepositorioUsuario repUsuario;

	public ControleAutenticacao(ServicoAutenticacao servicoAutenticacao, ServicoRefreshToken servicoRefreshToken,
			RepositorioUsuario repUsuario) {
		super();
		this.servicoAutenticacao = servicoAutenticacao;
		this.servicoRefreshToken = servicoRefreshToken;
		this.repUsuario = repUsuario;
	}

	@PostMapping("/login")
	public ResponseEntity<TokensResponse> login(@RequestBody @Valid LoginRequestDto request) {
		return ResponseEntity.ok(servicoAutenticacao.login(request));
	}

	@PostMapping("/registro")
	public ResponseEntity<Void> registro(@RequestBody @Valid RegistroRequestDto request) {
		servicoAutenticacao.registro(request);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/refresh")
	public ResponseEntity<TokensResponse> refresh(@RequestBody @Valid RefreshRequestDto request) {
		return ResponseEntity.ok(servicoAutenticacao.refresh(request));
	}

	@PostMapping("/sair")
	public ResponseEntity<Void> sair(Authentication autenticacao) {
		servicoRefreshToken.deletarPeloUsuario(usuarioAutenticado());
		return ResponseEntity.noContent().build();
	}

	// MÉTODOS INTERNOS
	private Usuario usuarioAutenticado() {
		String login = SecurityContextHolder.getContext().getAuthentication().getName();
		return repUsuario.findByLogin(login)
				.orElseThrow(() -> new UsuarioNaoEncontradoException("Usuário não encontrado"));
	}
}
