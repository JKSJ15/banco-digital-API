package com.jks.bank.Integracao;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
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
import com.jks.bank.controles.ControleConta;
import com.jks.bank.entidades.Conta;
import com.jks.bank.entidades.StatusDaConta;
import com.jks.bank.entidades.Usuario;
import com.jks.bank.repositorios.RepositorioConta;
import com.jks.bank.repositorios.RepositorioUsuario;
import com.jks.bank.servicos.ServicoConta;
import com.jks.bank.servicos.ServicoJwt;
import com.jks.bank.util.RetornaEntidades;

import jakarta.transaction.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
public class TesteIntegracaoConta {
	@Value("${api.security.token.secret}")
	String secretTest;
	@Autowired
	ControleConta controleConta;
	@Autowired
	ServicoConta servicoConta;
	@Autowired
	RepositorioConta repositorioConta;
	@Autowired
	MockMvc mvc;
	@Autowired
	ServicoJwt jwt;
	@Autowired
	RepositorioUsuario repositorioUsuario;
	@Autowired
	PasswordEncoder encoder;

	Conta conta;
	Usuario usuario;
	String tokenAcesso;
	Algorithm algorithm;
	String tokenAcessoExpirado;

	@BeforeEach
	void metodo() {
		algorithm = Algorithm.HMAC256(secretTest);
		usuario = RetornaEntidades.gerarUsuario();
		conta = RetornaEntidades.gerarConta(usuario);
		usuario.setConta(conta);
		usuario.setSenha(encoder.encode(usuario.getPassword()));
		repositorioUsuario.save(usuario);
		repositorioConta.save(conta);
		tokenAcesso = jwt.criarTokenDeAcesso(usuario);
	}

	@Test
	@DisplayName("conta_RetornaOK_QuandoSucesso")
	public void conta_RetornaOK_QuandoSucesso() throws Exception {
		mvc.perform(get("/conta").header("Authorization", "Bearer " + tokenAcesso)).andExpect(status().isOk())
				.andExpect(jsonPath("$.agencia").value(conta.getAgencia()))
				.andExpect(jsonPath("$.id").value(conta.getId()))
				.andExpect(jsonPath("$.status").value(conta.getStatus().name()))
				.andExpect(jsonPath("$.numero").value(conta.getNumero()));
	}

	@Test
	@DisplayName("conta_RetornaUNALTHORIZED_QuandoSemToken")
	public void conta_RetornaUNALTHORIZED_QuandoSemToken() throws Exception {
		mvc.perform(get("/conta")).andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("conta_RetornaUNALTHORIZED_QuandoTokenInvalido")
	public void conta_RetornaUNALTHORIZED_QuandoTokenInvalido() throws Exception {
		mvc.perform(get("/conta").header("Authorization", "Bearer tokeninvalido")).andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("conta_RetornaUNALTHORIZED_QuandoTokenExpirado")
	public void conta_RetornaUNALTHORIZED_QuandoTokenExpirado() throws Exception {
		tokenAcessoExpirado = JWT.create().withJWTId(UUID.randomUUID().toString()).withIssuedAt(new Date())
				.withExpiresAt(Instant.now().minusSeconds(200)).withIssuer("BancoDigitalAPI")
				.withSubject(usuario.getUsername()).sign(algorithm);
		mvc.perform(get("/conta").header("Authorization", "Bearer " + tokenAcessoExpirado))
				.andExpect(status().isUnauthorized());
	}

	// POST - BLOQUEAR
	@Test
	@DisplayName("bloquear_RetornaOK_QuandoSucesso")
	public void bloquear_RetornaOK_QuandoSucesso() throws Exception {
		String senhaJson = """
				{
				    "senha":"123"
				}
				""";
		mvc.perform(post("/conta/bloquear").header("Authorization", "Bearer " + tokenAcesso)
				.contentType(MediaType.APPLICATION_JSON).content(senhaJson)).andExpect(status().isNoContent());
		Conta contaAtualizada = repositorioConta.findById(conta.getId()).get();
		assertTrue(contaAtualizada.getStatus() == StatusDaConta.BLOQUEADA);
	}

	@Test
	@DisplayName("bloquear_RetornaUNALTHORIZED_QuandoTokenExpirado")
	public void bloquear_RetornaUNALTHORIZED_QuandoTokenExpirado() throws Exception {
		tokenAcessoExpirado = JWT.create().withJWTId(UUID.randomUUID().toString()).withIssuedAt(new Date())
				.withExpiresAt(Instant.now().minusSeconds(200)).withIssuer("BancoDigitalAPI")
				.withSubject(usuario.getUsername()).sign(algorithm);
		mvc.perform(post("/conta/bloquear").header("Authorization", "Bearer " + tokenAcessoExpirado))
				.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("bloquear_RetornaBAD_REQUEST_QuandoSenhaInvalida")
	public void bloquear_RetornaBAD_REQUEST_QuandoSenhaInvalida() throws Exception {
		mvc.perform(post("/conta/bloquear").header("Authorization", "Bearer " + tokenAcesso))
				.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("bloquear_RetornaBAD_REQUEST_QuandoSenhaErrada")
	public void bloquear_RetornaBAD_REQUEST_QuandoSenhaErrada() throws Exception {
		String senhaJson = """
				{
				    "senha":"senhaerrada"
				}
				""";
		mvc.perform(post("/conta/bloquear").header("Authorization", "Bearer " + tokenAcesso)
				.contentType(MediaType.APPLICATION_JSON).content(senhaJson)).andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("bloquear_RetornaUNALTHORIZED_QuandoTokenInvalido")
	public void bloquear_RetornaUNALTHORIZED_QuandoTokenInvalido() throws Exception {
		String senhaJson = """
				{
				    "senha":"123"
				}
				""";
		mvc.perform(post("/conta/bloquear").header("Authorization", "Bearer tokenInvalido")
				.contentType(MediaType.APPLICATION_JSON).content(senhaJson)).andExpect(status().isUnauthorized());
	}

	// POST - DESBLOQUEAR
	@Test
	@DisplayName("desbloquear_RetornaOK_QuandoSucesso")
	public void desbloquear_RetornaOK_QuandoSucesso() throws Exception {
		String senhaJson = """
				{
				    "senha":"123"
				}
				""";
		mvc.perform(post("/conta/desbloquear").header("Authorization", "Bearer " + tokenAcesso)
				.contentType(MediaType.APPLICATION_JSON).content(senhaJson)).andExpect(status().isNoContent());
		Conta contaAtualizada = repositorioConta.findById(conta.getId()).get();
		assertTrue(contaAtualizada.getStatus() == StatusDaConta.ATIVA);
	}

	@Test
	@DisplayName("desbloquear_RetornaUNALTHORIZED_QuandoTokenExpirado")
	public void desbloquear_RetornaUNALTHORIZED_QuandoTokenExpirado() throws Exception {
		tokenAcessoExpirado = JWT.create().withJWTId(UUID.randomUUID().toString()).withIssuedAt(new Date())
				.withExpiresAt(Instant.now().minusSeconds(200)).withIssuer("BancoDigitalAPI")
				.withSubject(usuario.getUsername()).sign(algorithm);
		mvc.perform(post("/conta/desbloquear").header("Authorization", "Bearer " + tokenAcessoExpirado))
				.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("desbloquear_RetornaBAD_REQUEST_QuandoSenhaInvalida")
	public void desbloquear_RetornaBAD_REQUEST_QuandoSenhaInvalida() throws Exception {
		mvc.perform(post("/conta/desbloquear").header("Authorization", "Bearer " + tokenAcesso))
				.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("desbloquear_RetornaBAD_REQUEST_QuandoSenhaErrada")
	public void desbloquear_RetornaBAD_REQUEST_QuandoSenhaErrada() throws Exception {
		String senhaJson = """
				{
				    "senha":"senhaerrada"
				}
				""";
		mvc.perform(post("/conta/desbloquear").header("Authorization", "Bearer " + tokenAcesso)
				.contentType(MediaType.APPLICATION_JSON).content(senhaJson)).andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("desbloquear_RetornaUNALTHORIZED_QuandoTokenInvalido")
	public void desbloquear_RetornaUNALTHORIZED_QuandoTokenInvalido() throws Exception {
		String senhaJson = """
				{
				    "senha":"123"
				}
				""";
		mvc.perform(post("/conta/desbloquear").header("Authorization", "Bearer tokenInvalido")
				.contentType(MediaType.APPLICATION_JSON).content(senhaJson)).andExpect(status().isUnauthorized());
	}

	// POST - ENCERRAR
	@Test
	@DisplayName("encerrar_RetornaOK_QuandoSucesso")
	public void encerrar_RetornaOK_QuandoSucesso() throws Exception {
		String senhaJson = """
				{
				    "senha":"123"
				}
				""";
		mvc.perform(post("/conta/encerrar").header("Authorization", "Bearer " + tokenAcesso)
				.contentType(MediaType.APPLICATION_JSON).content(senhaJson)).andExpect(status().isNoContent());
		Conta contaAtualizada = repositorioConta.findById(conta.getId()).get();
		assertTrue(contaAtualizada.getStatus() == StatusDaConta.ENCERRADA);
	}

	@Test
	@DisplayName("encerrar_RetornaUNALTHORIZED_QuandoTokenExpirado")
	public void encerrar_RetornaUNALTHORIZED_QuandoTokenExpirado() throws Exception {
		tokenAcessoExpirado = JWT.create().withJWTId(UUID.randomUUID().toString()).withIssuedAt(new Date())
				.withExpiresAt(Instant.now().minusSeconds(200)).withIssuer("BancoDigitalAPI")
				.withSubject(usuario.getUsername()).sign(algorithm);
		mvc.perform(post("/conta/encerrar").header("Authorization", "Bearer " + tokenAcessoExpirado))
				.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("encerrar_RetornaBAD_REQUEST_QuandoSenhaInvalida")
	public void encerrar_RetornaBAD_REQUEST_QuandoSenhaInvalida() throws Exception {
		mvc.perform(post("/conta/encerrar").header("Authorization", "Bearer " + tokenAcesso))
				.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("encerrar_RetornaBAD_REQUEST_QuandoContaComSaldo")
	public void encerrar_RetornaBAD_REQUEST_QuandoContaComSaldo() throws Exception {
		conta.setSaldo(BigDecimal.valueOf(20));
		repositorioConta.save(conta);
		String senhaJson = """
				{
				    "senha":"123"
				}
				""";
		mvc.perform(post("/conta/encerrar").header("Authorization", "Bearer " + tokenAcesso)
				.contentType(MediaType.APPLICATION_JSON).content(senhaJson)).andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("encerrar_RetornaBAD_REQUEST_QuandoSenhaErrada")
	public void encerrar_RetornaBAD_REQUEST_QuandoSenhaErrada() throws Exception {
		String senhaJson = """
				{
				    "senha":"senhaerrada"
				}
				""";
		mvc.perform(post("/conta/encerrar").header("Authorization", "Bearer " + tokenAcesso)
				.contentType(MediaType.APPLICATION_JSON).content(senhaJson)).andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("encerrar_RetornaUNALTHORIZED_QuandoTokenInvalido")
	public void encerrar_RetornaUNALTHORIZED_QuandoTokenInvalido() throws Exception {
		String senhaJson = """
				{
				    "senha":"123"
				}
				""";
		mvc.perform(post("/conta/encerrar").header("Authorization", "Bearer tokenInvalido")
				.contentType(MediaType.APPLICATION_JSON).content(senhaJson)).andExpect(status().isUnauthorized());
	}
}
