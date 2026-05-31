package com.jks.bank.configuracoes;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.jks.bank.entidades.Usuario;
import com.jks.bank.exceptions.UsuarioNaoEncontradoException;
import com.jks.bank.repositorios.RepositorioUsuario;
import com.jks.bank.servicos.ServicoJwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class FiltroSeguranca extends OncePerRequestFilter {
	private final ServicoJwt jwt;
	private final RepositorioUsuario usuarioRepositorio;

	public FiltroSeguranca(ServicoJwt jwt, RepositorioUsuario usuarioRepositorio) {
		super();
		this.jwt = jwt;
		this.usuarioRepositorio = usuarioRepositorio;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		var token = recoverToken(request);
		if (token != null) {
			String login = jwt.validarTokenDeAcesso(token);
			if (login != null) {
				Usuario usuario = usuarioRepositorio.findByLogin(login)
						.orElseThrow(() -> new UsuarioNaoEncontradoException("usuário não encontrado"));
				var auth = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
				SecurityContextHolder.getContext().setAuthentication(auth);
			}
		}
		filterChain.doFilter(request, response);
	}

	private String recoverToken(HttpServletRequest request) {
		String cabecalhoAuth = request.getHeader("Authorization");
		if (cabecalhoAuth == null || !cabecalhoAuth.startsWith("Bearer ")) {
			return null;
		}
		return cabecalhoAuth.replace("Bearer ", "");
	}

}
