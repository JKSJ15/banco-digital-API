package com.jks.bank.servicos;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import com.jks.bank.entidades.Usuario;
import com.jks.bank.exceptions.UsuarioNaoEncontradoException;
import com.jks.bank.repositorios.RepositorioUsuario;

@Service
public class ServicoUsuario implements UserDetailsService {
	private final RepositorioUsuario repUsuario;

	public ServicoUsuario(RepositorioUsuario repUsuario) {
		super();
		this.repUsuario = repUsuario;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsuarioNaoEncontradoException {
		Usuario usuario = repUsuario.findByLogin(username)
				.orElseThrow();
		return usuario;
	}

}
