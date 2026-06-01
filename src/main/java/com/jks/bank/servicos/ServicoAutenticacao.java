package com.jks.bank.servicos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.jks.bank.dto.LoginRequestDto;
import com.jks.bank.dto.RefreshRequestDto;
import com.jks.bank.dto.RegistroRequestDto;
import com.jks.bank.dto.TokensResponse;
import com.jks.bank.entidades.Conta;
import com.jks.bank.entidades.RefreshToken;
import com.jks.bank.entidades.StatusDaConta;
import com.jks.bank.entidades.Usuario;
import com.jks.bank.exceptions.IdadeNaoPermitidaException;
import com.jks.bank.exceptions.RefreshTokenInvalidoException;
import com.jks.bank.exceptions.UsuarioJaExisteException;
import com.jks.bank.exceptions.UsuarioNaoEncontradoException;
import com.jks.bank.exceptions.ValorInvalidoException;
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
	private final AuthenticationManager gerenciadorAutenticacao;

	public ServicoAutenticacao(PasswordEncoder passwordEncoder, RepositorioUsuario repositorioUsuario,
			RepositorioConta repositorioConta, ServicoJwt servicoJwt, ServicoRefreshToken servicoRefreshToken,
			RepositorioRefreshToken repositorioRefreshToken, AuthenticationManager gerenciadorAutenticacao) {
		super();
		this.passwordEncoder = passwordEncoder;
		this.repositorioUsuario = repositorioUsuario;
		this.repositorioConta = repositorioConta;
		this.servicoJwt = servicoJwt;
		this.servicoRefreshToken = servicoRefreshToken;
		this.repositorioRefreshToken = repositorioRefreshToken;
		this.gerenciadorAutenticacao = gerenciadorAutenticacao;
	}

	public TokensResponse login(LoginRequestDto request) {
		var auth = new UsernamePasswordAuthenticationToken(request.login(), request.senha());
		gerenciadorAutenticacao.authenticate(auth);

		Usuario usuario = repositorioUsuario.findByLogin(request.login())
				.orElseThrow(() -> new UsuarioNaoEncontradoException("usuário não encontrado!"));

		String refreshToken = servicoJwt.criarRefreshToken(usuario);
		String tokenAcesso = servicoJwt.criarTokenDeAcesso(usuario);
		servicoRefreshToken.gerarEntidadeRefreshToken(refreshToken, usuario);
		TokensResponse tokensResponse = new TokensResponse(tokenAcesso, refreshToken);
		return tokensResponse;
	}

	public void registro(RegistroRequestDto request) {
		validarRequestRegistro(request);

		Conta conta = gerarConta();

		String senhaCriptografada = passwordEncoder.encode(request.senha());
		Usuario usuario = Usuario.builder().withCpf(request.cpf()).withDataNasc(request.dataNascimento())
				.withLogin(request.login()).withNome(request.nome()).withSenha(senhaCriptografada).withConta(conta)
				.withTelefone(request.telefone()).withContaBloqueada(false).build();
		conta.setUsuario(usuario);

		repositorioUsuario.save(usuario);
		repositorioConta.save(conta);
	}

	public TokensResponse refresh(RefreshRequestDto request) {
		servicoRefreshToken.validarEntidadeRefreshToken(request.refreshToken());
		RefreshToken refresh = servicoRefreshToken.encontrarEntidadeRefreshToken(request.refreshToken());
		servicoJwt.validarRefreshToken(request.refreshToken(), refresh.getUsuario());

		Usuario usuarioPortador = repositorioUsuario.findById(refresh.getUsuario().getId())
				.orElseThrow(() -> new RefreshTokenInvalidoException("refresh token inválido!"));

		repositorioRefreshToken.delete(refresh);

		String refreshToken = servicoJwt.criarRefreshToken(usuarioPortador);
		String tokenAcesso = servicoJwt.criarTokenDeAcesso(usuarioPortador);

		servicoRefreshToken.gerarEntidadeRefreshToken(refreshToken, usuarioPortador);

		TokensResponse tokensResponse = new TokensResponse(tokenAcesso, refreshToken);
		return tokensResponse;
	}

	// MÉTODOS INTERNOS
	private void validarRequestRegistro(RegistroRequestDto request) {
		if (request.dataNascimento() == null) {
			throw new ValorInvalidoException("data de nascimento é obrigatória");
		}
		if (ChronoUnit.YEARS.between(request.dataNascimento(), LocalDate.now()) < 18) {
			throw new IdadeNaoPermitidaException("voce ainda não possui a idade necessária para criar uma conta!");
		}
		Optional<Usuario> usuario = repositorioUsuario.findByLogin(request.login());
		if (usuario.isPresent()) {
			throw new UsuarioJaExisteException("usuario já cadastrado!");
		}
	}

	private Conta gerarConta() {
		return Conta.builder().withDataDaCriacao(LocalDate.now()).withSaldo(BigDecimal.ZERO)
				.withStatus(StatusDaConta.ATIVA).withAgencia("001").withChavePix(UUID.randomUUID().toString())
				.withNumero(String.valueOf(ThreadLocalRandom.current().nextInt(100000, 999999))).build();
	}

}
