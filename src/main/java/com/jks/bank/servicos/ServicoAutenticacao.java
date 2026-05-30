package com.jks.bank.servicos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.jks.bank.dto.LoginRequestDto;
import com.jks.bank.dto.RefreshRequestDto;
import com.jks.bank.dto.RefreshResponseDto;
import com.jks.bank.dto.RegistroRequestDto;
import com.jks.bank.entidades.Conta;
import com.jks.bank.entidades.RefreshToken;
import com.jks.bank.entidades.StatusDaConta;
import com.jks.bank.entidades.Usuario;
import com.jks.bank.exceptions.IdadeNaoPermitidaException;
import com.jks.bank.exceptions.UsuarioJaExisteException;
import com.jks.bank.exceptions.UsuarioNaoEncontradoException;
import com.jks.bank.repositorios.RepositorioConta;
import com.jks.bank.repositorios.RepositorioRefreshToken;
import com.jks.bank.repositorios.RepositorioUsuario;

@Service
public class ServicoAutenticacao {
	private final PasswordEncoder passwordEncoder;
	private final RepositorioUsuario repositorioUsuario;
	private final RepositorioConta repositorioConta;
	private final ServicoJwt servicoJwt;
	private final ServicoRefreshToken servicoRefreshToken;
	private final RepositorioRefreshToken repositorioRefreshToken;

	public ServicoAutenticacao(PasswordEncoder passwordEncoder, RepositorioUsuario repositorioUsuario,
			RepositorioConta repositorioConta, ServicoJwt servicoJwt, ServicoRefreshToken servicoRefreshToken,
			RepositorioRefreshToken repositorioRefreshToken) {
		super();
		this.passwordEncoder = passwordEncoder;
		this.repositorioUsuario = repositorioUsuario;
		this.repositorioConta = repositorioConta;
		this.servicoJwt = servicoJwt;
		this.servicoRefreshToken = servicoRefreshToken;
		this.repositorioRefreshToken = repositorioRefreshToken;
	}

	public RefreshResponseDto login(LoginRequestDto request) {
		return null;
	}

	public void registro(RegistroRequestDto request) {
		validarRequestRegistro(request);

		Conta conta = gerarConta();

		String senhaCriptografada = passwordEncoder.encode(request.senha());
		Usuario usuario = Usuario.builder().withCpf(request.cpf()).withDataNasc(request.dataNascimento())
				.withLogin(request.login()).withNome(request.nome()).withSenha(senhaCriptografada).withConta(conta)
				.build();
		conta.setUsuario(usuario);

		repositorioUsuario.save(usuario);
		repositorioConta.save(conta);
	}

	public RefreshResponseDto refresh(RefreshRequestDto request) {
		servicoRefreshToken.validarEntidadeRefreshToken(request.refreshToken());
		RefreshToken refresh = servicoRefreshToken.encontrarEntidadeRefreshToken(request.refreshToken());
		servicoJwt.validarRefreshToken(request.refreshToken(), refresh.getUsuario());

		Usuario usuarioPortador = repositorioUsuario.findById(refresh.getUsuario().getId())
				.orElseThrow(() -> new UsuarioNaoEncontradoException("usuário não encontrado!"));

		repositorioRefreshToken.delete(refresh);

		String tokenAcesso = servicoJwt.criarRefreshToken(usuarioPortador);
		String refreshToken = servicoJwt.criarTokenDeAcesso(usuarioPortador);

		servicoRefreshToken.gerarEntidadeRefreshToken(refreshToken, usuarioPortador);

		RefreshResponseDto refreshResponse = new RefreshResponseDto(tokenAcesso, refreshToken);
		return refreshResponse;
	}

	private void validarRequestRegistro(RegistroRequestDto request) {
		if (ChronoUnit.YEARS.between(LocalDate.now(), request.dataNascimento()) < 18) {
			throw new IdadeNaoPermitidaException("voce ainda não possui a idade necessária para criar uma conta!");
		}
		Optional<Usuario> usuario = repositorioUsuario.findByLogin(request.login());
		if (usuario.isPresent()) {
			throw new UsuarioJaExisteException("usuario já cadastrado!");
		}
	}

	private Conta gerarConta() {
		return Conta.builder().withDataDaCriacao(LocalDate.now()).withSaldo(BigDecimal.ZERO)
				.withStatus(StatusDaConta.ATIVA).withAgencia(001l).withChavePix(UUID.randomUUID().toString())
				.withNumero(String.valueOf(ThreadLocalRandom.current().nextInt(100000, 999999))).build();
	}

}
