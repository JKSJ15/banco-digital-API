package com.jks.bank.servicos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.jks.bank.dto.CepResponseDto;
import com.jks.bank.dto.LoginRequestDto;
import com.jks.bank.dto.RefreshRequestDto;
import com.jks.bank.dto.RegistroRequestDto;
import com.jks.bank.dto.TokensResponse;
import com.jks.bank.entidades.Conta;
import com.jks.bank.entidades.RefreshToken;
import com.jks.bank.entidades.StatusDaConta;
import com.jks.bank.entidades.Usuario;
import com.jks.bank.exceptions.CepInvalidoException;
import com.jks.bank.exceptions.CpfJaExisteException;
import com.jks.bank.exceptions.IdadeNaoPermitidaException;
import com.jks.bank.exceptions.NaoAutorizadoException;
import com.jks.bank.exceptions.RefreshTokenInvalidoException;
import com.jks.bank.exceptions.TelefoneJaExisteException;
import com.jks.bank.exceptions.UsuarioJaExisteException;
import com.jks.bank.repositorios.RepositorioConta;
import com.jks.bank.repositorios.RepositorioRefreshToken;
import com.jks.bank.repositorios.RepositorioUsuario;

@Service
public class ServicoAutenticacao {
	private static final Logger log = LoggerFactory.getLogger(ServicoAutenticacao.class);
	private final ServicoApiCep servicoCep;
	private final PasswordEncoder passwordEncoder;
	private final RepositorioUsuario repositorioUsuario;
	private final RepositorioConta repositorioConta;
	private final ServicoJwt servicoJwt;
	private final ServicoRefreshToken servicoRefreshToken;
	private final RepositorioRefreshToken repositorioRefreshToken;
	private final AuthenticationManager gerenciadorAutenticacao;
	private static final String AMARELO = "\u001B[33m";
	private static final String VERDE = "\u001B[32m";
	private static final String RESETAR = "\u001B[0m";

	public ServicoAutenticacao(ServicoApiCep servicoCep, PasswordEncoder passwordEncoder,
			RepositorioUsuario repositorioUsuario, RepositorioConta repositorioConta, ServicoJwt servicoJwt,
			ServicoRefreshToken servicoRefreshToken, RepositorioRefreshToken repositorioRefreshToken,
			AuthenticationManager gerenciadorAutenticacao) {
		super();
		this.servicoCep = servicoCep;
		this.passwordEncoder = passwordEncoder;
		this.repositorioUsuario = repositorioUsuario;
		this.repositorioConta = repositorioConta;
		this.servicoJwt = servicoJwt;
		this.servicoRefreshToken = servicoRefreshToken;
		this.repositorioRefreshToken = repositorioRefreshToken;
		this.gerenciadorAutenticacao = gerenciadorAutenticacao;
	}

	public TokensResponse login(LoginRequestDto request) {
		log.debug("tentativa de login para usuário {}", request.login());
		var auth = new UsernamePasswordAuthenticationToken(request.login(), request.senha());
		gerenciadorAutenticacao.authenticate(auth);
		log.info(VERDE + "login realizado! usuário: {}" + RESETAR, request.login());

		Usuario usuario = repositorioUsuario.findByLogin(request.login())
				.orElseThrow(() -> new NaoAutorizadoException("usuário não registrado!"));

		String refreshToken = servicoJwt.criarRefreshToken(usuario);
		String tokenAcesso = servicoJwt.criarTokenDeAcesso(usuario);
		servicoRefreshToken.gerarEntidadeRefreshToken(refreshToken, usuario);
		TokensResponse tokensResponse = new TokensResponse(tokenAcesso, refreshToken);
		return tokensResponse;
	}

	public void registro(RegistroRequestDto request) {
		log.debug("refresh token recebido para renovação");
		validarRequestRegistro(request);
		validarCep(request.cep());

		Conta conta = gerarConta(request);

		String senhaCriptografada = passwordEncoder.encode(request.senha());
		Usuario usuario = Usuario.builder().withCpf(request.cpf()).withDataNasc(request.dataNascimento())
				.withLogin(request.login()).withNome(request.nome()).withSenha(senhaCriptografada).withConta(conta)
				.withTelefone(request.telefone()).withContaBloqueada(false).build();
		conta.setUsuario(usuario);

		repositorioUsuario.save(usuario);
		repositorioConta.save(conta);
		log.info(VERDE + "registro realizado! usuário:{}, conta:{}, agencia:{}, numero:{}" + RESETAR,
				usuario.getUsername(), conta.getId(), conta.getAgencia(), conta.getNumero());
	}

	public TokensResponse refresh(RefreshRequestDto request) {
		log.info(VERDE + "refresh requisitado!" + RESETAR);
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
		log.info(VERDE + "refresh realizado! usuário: {}" + RESETAR, usuarioPortador.getUsername());
		return tokensResponse;
	}

	// MÉTODOS INTERNOS

	private void validarCep(String cep) {
		CepResponseDto endereco = servicoCep.buscarEndereco(cep);

		if (endereco == null || Boolean.TRUE.equals(endereco.erro())) {
			log.warn(AMARELO + "cep {} inválido!" + RESETAR, cep);
			throw new CepInvalidoException("cep inválido!");
		}
	}

	private void validarRequestRegistro(RegistroRequestDto request) {
		if (repositorioUsuario.existsByCpf(request.cpf())) {
			throw new CpfJaExisteException("cpf já cadastrado!");
		}
		if (repositorioUsuario.existsByTelefone(request.telefone())) {
			throw new TelefoneJaExisteException("telefone já cadastrado!");
		}
		if (ChronoUnit.YEARS.between(request.dataNascimento(), LocalDate.now()) < 18) {
			log.warn(AMARELO + "usuário menor que 18 anos!" + RESETAR);
			throw new IdadeNaoPermitidaException("voce ainda não possui a idade necessária para criar uma conta!");
		}
		Optional<Usuario> usuario = repositorioUsuario.findByLogin(request.login());
		if (usuario.isPresent()) {
			log.warn(AMARELO + "usuário: {}, já está cadastrado!" + RESETAR, request.login());
			throw new UsuarioJaExisteException("usuario já cadastrado!");
		}
	}

	private Conta gerarConta(RegistroRequestDto request) {
		return Conta.builder().withDataDaCriacao(LocalDate.now()).withSaldo(BigDecimal.ZERO).withCep(request.cep())
				.withStatus(StatusDaConta.ATIVA).withAgencia("001").withChavePix(UUID.randomUUID().toString())
				.withNumero(String.valueOf(ThreadLocalRandom.current().nextInt(100000, 999999))).build();
	}

}
