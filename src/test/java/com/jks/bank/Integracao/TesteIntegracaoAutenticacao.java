package com.jks.bank.Integracao;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.jks.bank.entidades.Conta;
import com.jks.bank.entidades.Usuario;
import com.jks.bank.repositorios.RepositorioConta;
import com.jks.bank.repositorios.RepositorioUsuario;
import com.jks.bank.servicos.ServicoAutenticacao;
import com.jks.bank.servicos.ServicoJwt;
import com.jks.bank.servicos.ServicoRefreshToken;
import com.jks.bank.util.RetornaEntidades;

import jakarta.transaction.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
public class TesteIntegracaoAutenticacao {
	@Value("${api.security.token.secret}")
	String secret;
	@Autowired
	ServicoAutenticacao servicoAutenticacao;
	@Autowired
	MockMvc mvc;
	@Autowired
	RepositorioUsuario repositorioUsuario;
	@Autowired
	RepositorioConta RepositorioConta;
	@Autowired
	ServicoJwt jwt;
	@Autowired
	ServicoRefreshToken servicoRefresh;
	@Autowired
	PasswordEncoder encoder;

	Conta conta;
	Usuario usuario;
	Usuario usuario2;
	String tokenAcesso;
	String refreshToken;
	String tokenAcessoExpirado;
	String refreshTokenExpirado;
	Algorithm algorithm;

	@BeforeEach
	void metodo() {
		algorithm = Algorithm.HMAC256(secret);
		usuario = RetornaEntidades.gerarUsuario();
		conta = RetornaEntidades.gerarConta(usuario);
		usuario.setConta(conta);
		usuario.setSenha(encoder.encode(usuario.getPassword()));
		repositorioUsuario.save(usuario);
		RepositorioConta.save(conta);
		tokenAcesso = jwt.criarTokenDeAcesso(usuario);
		refreshToken = jwt.criarRefreshToken(usuario);
		servicoRefresh.gerarEntidadeRefreshToken(refreshToken, usuario);
	}

	// POST - REGISTER
	@Test
	@DisplayName("register_retornaNO_CONTENT_quandoSucesso")
	public void register_retornaNO_CONTENT_quandoSucesso() throws Exception {
		String registerJson = """
					{
					"login":"alcantara@gmail",
				    "senha":"123",
				    "nome":"alcantara",
					"cpf":"00000000001",
					"dataNascimento":"2007-10-27",
					"telefone":"0000000000",
					"cep":"55730000"
					}
				""";
		mvc.perform(post("/auth/registro").contentType(MediaType.APPLICATION_JSON).content(registerJson))
				.andExpect(status().isNoContent());
	}

	@Test
	@DisplayName("register_retornaBAD_REQUEST_quandoCpfJaExiste")
	public void register_retornaBAD_REQUEST_quandoCpfJaExiste() throws Exception {
		String registerJson = """
					{
					"login":"alcantara@gmail",
				    "senha":"123",
				    "nome":"alcantara",
					"cpf":"00000000000",
					"dataNascimento":"2007-10-27",
					"telefone":"0000000000",
					"cep":"55730000"
					}
				""";
		mvc.perform(post("/auth/registro").contentType(MediaType.APPLICATION_JSON).content(registerJson))
				.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("register_retornaBAD_REQUEST_quandoTelefoneJaExiste")
	public void register_retornaBAD_REQUEST_quandoTelefoneJaExiste() throws Exception {
		String registerJson = """
					{
					"login":"alcantara@gmail",
				    "senha":"123",
				    "nome":"alcantara",
					"cpf":"00000000001",
					"dataNascimento":"2007-10-27",
					"telefone":"1234567890",
					"cep":"55730000"
					}
				""";
		mvc.perform(post("/auth/registro").contentType(MediaType.APPLICATION_JSON).content(registerJson))
				.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("register_retornaBAD_REQUEST_quandoUsuarioJaExiste")
	public void register_retornaBAD_REQUEST_quandoUsuarioJaExiste() throws Exception {
		String registerJson = """
					{
					"login":"Joao@gmail",
				    "senha":"123",
				    "nome":"joao",
					"cpf":"00000000000",
					"dataNascimento":"2007-10-27",
					"telefone":"1234567890",
					"cep":"55730000"
					}
				""";
		mvc.perform(post("/auth/registro").contentType(MediaType.APPLICATION_JSON).content(registerJson))
				.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("register_retornaBAD_REQUEST_quandoDadosInvalidos")
	public void register_retornaBAD_REQUEST_quandoDadosInvalidos() throws Exception {
		String registerJson = """
					{
				    "senha":"123",
					"cpf":"00000000001",
					"dataNascimento:"2007-10-27",
					"telefone":"0000000000",
					"cep":"55730000"
					}
				""";
		mvc.perform(post("/auth/registro").contentType(MediaType.APPLICATION_JSON).content(registerJson))
				.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("register_retornaBAD_REQUEST_quandoSemDados")
	public void register_retornaBAD_REQUEST_quandoSemDados() throws Exception {
		mvc.perform(post("/auth/registro").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest());
	}

	// POST - LOGIN
	@Test
	@DisplayName("login_retornaOk_quandoSucesso")
	public void login_retornaOk_quandoSucesso() throws Exception {
		String loginJson = """
					{
					"login":"Joao@gmail",
				    "senha":"123"
					}
				""";
		mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(loginJson))
				.andExpect(status().isOk()).andExpect(jsonPath("$.tokenAcesso").isNotEmpty())
				.andExpect(jsonPath("$.refreshToken").isNotEmpty());
		;
	}

	@Test
	@DisplayName("login_retornaUNAUTHORIZED_quandoSenhaIncorreta")
	void login_retornaUNAUTHORIZED_quandoSenhaIncorreta() throws Exception {
		String json = """
				{
				    "login":"Joao@gmail",
				    "senha":"senhaErrada"
				}
				""";

		mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("login_retornaUNAUTHORIZED_quandoLoginIncorreto")
	void login_retornaUNAUTHORIZED_quandoLoginIncorreto() throws Exception {
		String json = """
				{
				    "login":"Joao@NaoSeiOresto",
				    "senha":"123"
				}
				""";

		mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("login_retornaBAD_REQUEST_quandoDadosInvalidos")
	public void login_retornaBAD_REQUEST_quandoDadosInvalidos() throws Exception {
		String loginJson = """
					{
					"login":"Joao@gmail"
					}
				""";
		mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(loginJson))
				.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("login_retornaUNAUTHORIZED_quandoUsuarioNaoEncontrado")
	public void login_retornaUNAUTHORIZED_quandoUsuarioNaoEncontrado() throws Exception {
		String loginJson = """
					{
					"login":"naoUsuario",
				    "senha":"nadacomnada"
					}
				""";
		mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(loginJson))
				.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("login_retornaBAD_REQUEST_quandoSemDados")
	public void login_retornaBAD_REQUEST_quandoSemDados() throws Exception {
		mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest());
	}

	// POST - REFRESH
	@Test
	@DisplayName("refresh_retornaOk_quandoSucesso")
	public void refresh_retornaOk_quandoSucesso() throws Exception {
		String refreshJson = """
					{
					"refreshToken":"%s"
					}
				""".formatted(refreshToken);
		mvc.perform(post("/auth/refresh").header("Authorization", "Bearer " + tokenAcesso)
				.contentType(MediaType.APPLICATION_JSON).content(refreshJson)).andExpect(status().isOk())
				.andExpect(jsonPath("$.tokenAcesso").isNotEmpty()).andExpect(jsonPath("$.refreshToken").isNotEmpty());
	}

	@Test
	@DisplayName("refresh_retornaUNAUTHORIZED_quandoTokenAcessoExpirado")
	public void refresh_retornaUNAUTHORIZED_quandoTokenAcessoExpirado() throws Exception {
		tokenAcessoExpirado = JWT.create().withJWTId(UUID.randomUUID().toString()).withIssuedAt(new Date())
				.withExpiresAt(Instant.now().minusSeconds(200)).withIssuer("BancoDigitalAPI")
				.withSubject(usuario.getUsername()).sign(algorithm);
		String refreshJson = """
					{
					"refreshToken":"%s"
					}
				""".formatted(refreshToken);
		mvc.perform(post("/auth/refresh").header("Authorization", "Bearer " + tokenAcessoExpirado)
				.contentType(MediaType.APPLICATION_JSON).content(refreshJson)).andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("refresh_retornaFORBIDDEN_quandoRefreshTokenExpirado")
	public void refresh_retornaFORBIDDEN_quandoRefreshTokenExpirado() throws Exception {
		refreshTokenExpirado = JWT.create().withJWTId(UUID.randomUUID().toString()).withIssuedAt(new Date())
				.withExpiresAt(Date.from(Instant.now().minusSeconds(200))).withIssuer("BancoDigitalAPI")
				.withSubject(usuario.getUsername()).sign(algorithm);
		String refreshJson = """
					{
					"refreshToken":"%s"
					}
				""".formatted(refreshTokenExpirado);
		mvc.perform(post("/auth/refresh").header("Authorization", "Bearer " + tokenAcesso)
				.contentType(MediaType.APPLICATION_JSON).content(refreshJson)).andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("refresh_retornaFORBIDDEN_quandoRefreshTokenInvalido")
	public void refresh_retornaFORBIDDEN_quandoRefreshTokenInvalido() throws Exception {
		String refreshJson = """
					{
					"refreshToken":"inválido"
					}
				""";
		mvc.perform(post("/auth/refresh").header("Authorization", "Bearer " + tokenAcesso)
				.contentType(MediaType.APPLICATION_JSON).content(refreshJson)).andExpect(status().isForbidden());
	}

	@DisplayName("refresh_retornaUNAUTHORIZED_quandoTokenAcessoInvalido")
	public void refresh_retornaUNAUTHORIZED_quandoTokenAcessoInvalido() throws Exception {
		String refreshJson = """
					{
					"refreshToken":"%s"
					}
				""".formatted(refreshToken);
		mvc.perform(post("/auth/refresh").header("Authorization", "Bearer tokenInvalido")
				.contentType(MediaType.APPLICATION_JSON).content(refreshJson)).andExpect(status().isForbidden());
	}

	@DisplayName("refresh_retornaUNAUTHORIZED_quandoSemTokenAcesso")
	public void refresh_retornaUNAUTHORIZED_quandoSemTokens() throws Exception {
		String refreshJson = """
					{
					"refreshToken":"%s"
					}
				""".formatted(refreshToken);
		mvc.perform(post("/auth/refresh").contentType(MediaType.APPLICATION_JSON).content(refreshJson))
				.andExpect(status().isForbidden());
	}

	@DisplayName("refresh_retornaBAD_REQUEST_quandoSemRefreshToken")
	public void refresh_retornaBAD_REQUEST_quandoSemRefreshToken() throws Exception {
		mvc.perform(post("/auth/refresh").header("Authorization", "Bearer " + tokenAcesso))
				.andExpect(status().isBadRequest());
	}

	// POST - SAIR
	@Test
	@DisplayName("sair_retornaOK_quandoSucesso")
	public void sair_retornaOK_quandoSucesso() throws Exception {
		String refreshJson = """
					{
					"refreshToken":"inválido"
					}
				""";
		mvc.perform(post("/auth/sair").header("Authorization", "Bearer " + tokenAcesso))
				.andExpect(status().isNoContent());
		mvc.perform(post("/auth/refresh").header("Authorization", "Bearer " + tokenAcesso)
				.contentType(MediaType.APPLICATION_JSON).content(refreshJson)).andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("sair_retornaUNAUTHORIZED_quandoTokenAcessoExpirado")
	public void sair_retornaUNAUTHORIZED_quandoTokenAcessoExpirado() throws Exception {
		tokenAcessoExpirado = JWT.create().withJWTId(UUID.randomUUID().toString()).withIssuedAt(new Date())
				.withExpiresAt(Instant.now().minusSeconds(200)).withIssuer("BancoDigitalAPI")
				.withSubject(usuario.getUsername()).sign(algorithm);
		mvc.perform(post("/auth/sair").header("Authorization", "Bearer " + tokenAcessoExpirado))
				.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("sair_retornaUNAUTHORIZED_quandoSemTokenAcesso")
	public void sair_retornaUNAUTHORIZED_quandoSemTokenAcesso() throws Exception {
		mvc.perform(post("/auth/sair")).andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("sair_retornaUNAUTHORIZED_quandoTokenInvalido")
	public void sair_retornaUNAUTHORIZED_quandoTokenInvalido() throws Exception {
		mvc.perform(post("/auth/sair").header("Authorization", "Bearer tokenInvalido"))
				.andExpect(status().isUnauthorized());
	}
}
