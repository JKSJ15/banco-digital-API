package com.jks.bank.Integracao;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.auth0.jwt.algorithms.Algorithm;
import com.jks.bank.controles.ControleTransacoes;
import com.jks.bank.entidades.Conta;
import com.jks.bank.entidades.StatusDaConta;
import com.jks.bank.entidades.TipoTransacao;
import com.jks.bank.entidades.Usuario;
import com.jks.bank.repositorios.RepositorioConta;
import com.jks.bank.repositorios.RepositorioUsuario;
import com.jks.bank.servicos.ServicoAutenticacao;
import com.jks.bank.servicos.ServicoJwt;
import com.jks.bank.servicos.ServicoRefreshToken;
import com.jks.bank.servicos.ServicoTransacoes;
import com.jks.bank.util.RetornaEntidades;

import jakarta.transaction.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class TesteIntegracaoTransacoes {
	@Value("${api.security.token.secret}")
	String secret;
	@Autowired
	MockMvc mvc;
	@Autowired
	ServicoJwt servicoJwt;
	@Autowired
	ServicoTransacoes servicoTransacoes;
	@Autowired
	ServicoAutenticacao servicoAutenticacao;
	@Autowired
	ControleTransacoes controleTransacoes;
	@Autowired
	RepositorioUsuario repositorioUsuario;
	@Autowired
	RepositorioConta repositorioConta;
	@Autowired
	PasswordEncoder encoder;
	@Autowired
	ServicoRefreshToken servicoRefresh;

	Algorithm augorithm;
	Usuario usuario;
	Usuario usuario2;
	String tokenAcesso;
	String refreshToken;
	String tokenAcesso2;
	String refreshToken2;
	Conta conta;
	Conta conta2;

	@BeforeEach
	void metodo() {
		augorithm = Algorithm.HMAC256(secret);

		usuario = RetornaEntidades.gerarUsuario();
		usuario2 = Usuario.builder().withCpf("101010101010").withDataNasc(LocalDate.of(2007, 10, 20))
				.withLogin("alcantara@gmail").withNome("alcantara").withSenha("123").withTelefone("2020202020")
				.withContaBloqueada(false).build();
		conta2 = Conta.builder().withDataDaCriacao(LocalDate.now()).withSaldo(BigDecimal.ZERO).withCep("55730000")
				.withStatus(StatusDaConta.ATIVA).withAgencia("001").withChavePix(UUID.randomUUID().toString())
				.withNumero(String.valueOf(ThreadLocalRandom.current().nextInt(100000, 999999))).withUsuario(usuario2)
				.build();
		conta = RetornaEntidades.gerarConta(usuario);

		usuario.setConta(conta);
		usuario.setSenha(encoder.encode(usuario.getPassword()));
		usuario2.setConta(conta2);
		usuario2.setSenha(encoder.encode(usuario.getPassword()));

		tokenAcesso = servicoJwt.criarTokenDeAcesso(usuario);
		refreshToken = servicoJwt.criarRefreshToken(usuario);

		tokenAcesso2 = servicoJwt.criarTokenDeAcesso(usuario2);
		refreshToken2 = servicoJwt.criarRefreshToken(usuario2);

		repositorioUsuario.save(usuario);
		repositorioConta.save(conta);
		repositorioUsuario.save(usuario2);
		repositorioConta.save(conta2);
		servicoRefresh.gerarEntidadeRefreshToken(refreshToken, usuario);
		servicoRefresh.gerarEntidadeRefreshToken(refreshToken2, usuario2);
	}

	// GET - RELATORIO
	@Test
	@DisplayName("relatorio_retornaOK_quandoSucesso")
	void relatorio_retornaOK_quandoSucesso() throws Exception {
		mvc.perform(get("/transacao/relatorio").header("Authorization", "Bearer " + tokenAcesso))
				.andExpect(status().isOk()).andExpect(jsonPath("$.saldoAtual").value(conta.getSaldo()));
	}

	@Test
	@DisplayName("relatorio_retornaUNAUTHORIZED_quandoSemToken")
	void relatorio_retornaUNAUTHORIZED_quandoSemToken() throws Exception {
		mvc.perform(get("/transacao/relatorio")).andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("relatorio_retornaUNAUTHORIZED_quandoTokenInvalido")
	void relatorio_retornaUNAUTHORIZED_quandoTokenInvalido() throws Exception {
		mvc.perform(get("/transacao/relatorio").header("Authorization", "Bearer invalido"))
				.andExpect(status().isUnauthorized());
	}

	// GET - EXTRATO
	@Test
	@DisplayName("extrato_retornaOK_quandoSucesso")
	void extrato_retornaOK_quandoSucesso() throws Exception {
		mvc.perform(get("/transacao/extrato").header("Authorization", "Bearer " + tokenAcesso))
				.andExpect(status().isOk());
	}

	@Test
	@DisplayName("extrato_retornaUNAUTHORIZED_quandoSemToken")
	void extrato_retornaUNAUTHORIZED_quandoSemToken() throws Exception {
		mvc.perform(get("/transacao/extrato")).andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("extrato_retornaUNAUTHORIZED_quandoTokenInvalido")
	void extrato_retornaUNAUTHORIZED_quandoTokenInvalido() throws Exception {
		mvc.perform(get("/transacao/extrato").header("Authorization", "Bearer invalido"))
				.andExpect(status().isUnauthorized());
	}

	// POST - SAQUE
	@Test
	@DisplayName("saque_retornaOK_quandoSucesso")
	void saque_retornaOK_quandoSucesso() throws Exception {
		conta.setSaldo(BigDecimal.valueOf(300));
		repositorioConta.save(conta);
		String saqueJson = """
				{
				    "senha":"123",
				    "valor":"10",
				    "descricao":"f"
				}
				""";
		mvc.perform(post("/transacao/saque").header("Authorization", "Bearer " + tokenAcesso)
				.contentType(MediaType.APPLICATION_JSON).content(saqueJson)).andExpect(status().isOk())
				.andExpect(jsonPath("$.tipo").value(TipoTransacao.SAQUE.name()));
	}

	@Test
	@DisplayName("saque_retornaBAD_REQUEST_quandoSaldoInsuficiente")
	void saque_retornaBAD_REQUEST_quandoSaldoInsuficiente() throws Exception {
		String saqueJson = """
				{
				    "senha":"123",
				    "valor":"10",
				    "descricao":"f"
				}
				""";
		mvc.perform(post("/transacao/saque").header("Authorization", "Bearer " + tokenAcesso)
				.contentType(MediaType.APPLICATION_JSON).content(saqueJson)).andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("saque_retornaBAD_REQUEST_quandoSenhaIncorreta")
	void saque_retornaBAD_REQUEST_quandoSenhaIncorreta() throws Exception {
		String saqueJson = """
				{
				    "senha":"1234",
				    "valor":"10",
				    "descricao":"f"
				}
				""";
		mvc.perform(post("/transacao/saque").header("Authorization", "Bearer " + tokenAcesso)
				.contentType(MediaType.APPLICATION_JSON).content(saqueJson)).andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("saque_retornaBAD_REQUEST_quandoDadosFaltando")
	void saque_retornaBAD_REQUEST_quandoDadosFaltando() throws Exception {
		String saqueJson = """
				{
				    "senha":"123",
				    "valor":"10"
				}
				""";
		mvc.perform(post("/transacao/saque").header("Authorization", "Bearer " + tokenAcesso)
				.contentType(MediaType.APPLICATION_JSON).content(saqueJson)).andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("saque_retornaUNAUTHORIZED_quandoTokenAcessoInvalido")
	void saque_retornaUNAUTHORIZED_quandoTokenAcessoInvalido() throws Exception {
		String saqueJson = """
				{
				    "senha":"123",
				    "valor":"10",
				    "descricao":"f"
				}
				""";
		mvc.perform(post("/transacao/saque").header("Authorization", "Bearer invalido")
				.contentType(MediaType.APPLICATION_JSON).content(saqueJson)).andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("saque_retornaBAD_REQUEST_quandoSaqueAcimaLimite")
	void saque_retornaBAD_REQUEST_quandoSaqueAcimaLimite() throws Exception {
		String saqueJson = """
				{
				    "senha":"123",
				    "valor":"5001",
				    "descricao":"f"
				}
				""";
		mvc.perform(post("/transacao/saque").header("Authorization", "Bearer " + tokenAcesso)
				.contentType(MediaType.APPLICATION_JSON).content(saqueJson)).andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("saque_retornaBAD_REQUEST_quandoSaqueNegativo")
	void saque_retornaBAD_REQUEST_quandoSaqueNegativo() throws Exception {
		String saqueJson = """
				{
				    "senha":"123",
				    "valor":"-50",
				    "descricao":"f"
				}
				""";
		mvc.perform(post("/transacao/saque").header("Authorization", "Bearer " + tokenAcesso)
				.contentType(MediaType.APPLICATION_JSON).content(saqueJson)).andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("saque_retornaBAD_REQUEST_quandoSaqueZero")
	void saque_retornaBAD_REQUEST_quandoSaqueZero() throws Exception {
		String saqueJson = """
				{
				    "senha":"123",
				    "valor":"0",
				    "descricao":"f"
				}
				""";
		mvc.perform(post("/transacao/saque").header("Authorization", "Bearer " + tokenAcesso)
				.contentType(MediaType.APPLICATION_JSON).content(saqueJson)).andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("saque_retornaFORBIDDEN_quandoContaOrigemBloqueada")
	void saque_retornaFORBIDDEN_quandoContaOrigemBloqueada() throws Exception {
		conta.setStatus(StatusDaConta.BLOQUEADA);
		repositorioConta.save(conta);
		String saqueJson = """
				{
				    "senha":"123",
				    "valor":"10",
				    "descricao":"f"
				}
				""";
		mvc.perform(post("/transacao/saque").header("Authorization", "Bearer " + tokenAcesso)
				.contentType(MediaType.APPLICATION_JSON).content(saqueJson)).andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("saque_retornaFORBIDDEN_quandoContaOrigemEncerrada")
	void saque_retornaFORBIDDEN_quandoContaOrigemEncerrada() throws Exception {
		conta.setStatus(StatusDaConta.ENCERRADA);
		repositorioConta.save(conta);
		String saqueJson = """
				{
				    "senha":"123",
				    "valor":"10",
				    "descricao":"f"
				}
				""";
		mvc.perform(post("/transacao/saque").header("Authorization", "Bearer " + tokenAcesso)
				.contentType(MediaType.APPLICATION_JSON).content(saqueJson)).andExpect(status().isForbidden());
	}

	// POST - DEPÓSITO

	@Test
	@DisplayName("deposito_retornaOK_quandoSucesso")
	void deposito_retornaOK_quandoSucesso() throws Exception {
		String depositoJson = """
				{
				    "senha":"123",
				    "valor":"10",
				    "descricao":"f"
				}
				""";
		mvc.perform(post("/transacao/deposito").header("Authorization", "Bearer " + tokenAcesso)
				.contentType(MediaType.APPLICATION_JSON).content(depositoJson)).andExpect(status().isOk())
				.andExpect(jsonPath("$.tipo").value(TipoTransacao.DEPOSITO.name()));
	}

	@Test
	@DisplayName("deposito_retornaUNAUTHORIZED_quandoTokenAcessoInvalido")
	void deposito_retornaUNAUTHORIZED_quandoTokenAcessoInvalido() throws Exception {
		String depositoJson = """
				{
				    "senha":"123",
				    "valor":"10",
				    "descricao":"f"
				}
				""";
		mvc.perform(post("/transacao/deposito").header("Authorization", "Bearer invalido")
				.contentType(MediaType.APPLICATION_JSON).content(depositoJson)).andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("deposito_retornaBAD_REQUEST_quandoValorZero")
	void deposito_retornaBAD_REQUEST_quandoValorZero() throws Exception {
		String depositoJson = """
				{
				    "senha":"123",
				    "valor":"0",
				    "descricao":"f"
				}
				""";
		mvc.perform(post("/transacao/deposito").header("Authorization", "Bearer " + tokenAcesso)
				.contentType(MediaType.APPLICATION_JSON).content(depositoJson)).andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("deposito_retornaBAD_REQUEST_quandoValorNegativo")
	void deposito_retornaBAD_REQUEST_quandoValorNegativo() throws Exception {
		String depositoJson = """
				{
				    "senha":"123",
				    "valor":"-10",
				    "descricao":"f"
				}
				""";
		mvc.perform(post("/transacao/deposito").header("Authorization", "Bearer " + tokenAcesso)
				.contentType(MediaType.APPLICATION_JSON).content(depositoJson)).andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("deposito_retornaBAD_REQUEST_quandoSenhaIncorreta")
	void deposito_retornaBAD_REQUEST_quandoSenhaIncorreta() throws Exception {
		String depositoJson = """
				{
				    "senha":"1234",
				    "valor":"10",
				    "descricao":"f"
				}
				""";
		mvc.perform(post("/transacao/deposito").header("Authorization", "Bearer " + tokenAcesso)
				.contentType(MediaType.APPLICATION_JSON).content(depositoJson)).andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("deposito_retornaBAD_REQUEST_quandoValorExcedeLimite")
	void deposito_retornaBAD_REQUEST_quandoValorExcedeLimite() throws Exception {
		String depositoJson = """
				{
				    "senha":"1234",
				    "valor":"50001",
				    "descricao":"f"
				}
				""";
		mvc.perform(post("/transacao/deposito").header("Authorization", "Bearer " + tokenAcesso)
				.contentType(MediaType.APPLICATION_JSON).content(depositoJson)).andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("deposito_retornUNALTHORIZED_quandoTokenAcessoInvalido")
	void deposito_retornUNALTHORIZED_quandoTokenAcessoInvalido() throws Exception {
		String depositoJson = """
				{
				    "senha":"1234",
				    "valor":"500",
				    "descricao":"f"
				}
				""";
		mvc.perform(post("/transacao/deposito").header("Authorization", "Bearer invalido")
				.contentType(MediaType.APPLICATION_JSON).content(depositoJson)).andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("deposito_retornaFORBIDDEN_quandoContaDestinoEncerrada")
	void deposito_retornaFORBIDDEN_quandoContaDestinoEncerrada() throws Exception {
		conta.setStatus(StatusDaConta.ENCERRADA);
		conta.setSaldo(BigDecimal.valueOf(1000));
		repositorioConta.save(conta);
		String depositoJson = """
				{
				    "senha":"123",
				    "valor":"500",
				    "descricao":"f"
				}
				""";
		mvc.perform(post("/transacao/deposito").header("Authorization", "Bearer " + tokenAcesso)
				.contentType(MediaType.APPLICATION_JSON).content(depositoJson)).andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("deposito_retornaOK_quandoContaDestinoBloqueada")
	void deposito_retornaOK_quandoContaDestinoBloqueada() throws Exception {
		conta.setStatus(StatusDaConta.BLOQUEADA);
		conta.setSaldo(BigDecimal.valueOf(1000));
		repositorioConta.save(conta);
		String depositoJson = """
				{
				    "senha":"123",
				    "valor":"500",
				    "descricao":"f"
				}
				""";
		mvc.perform(post("/transacao/deposito").header("Authorization", "Bearer " + tokenAcesso)
				.contentType(MediaType.APPLICATION_JSON).content(depositoJson)).andExpect(status().isOk());
	}

	// POST - PIX

	@Test
	@DisplayName("pix_retornaOK_quandoSucesso")
	void pix_retornaOK_quandoSucesso() throws Exception {
		conta.setSaldo(BigDecimal.valueOf(10));
		repositorioConta.save(conta);
		String pixJson = """
				{
				    "senha":"123",
				    "valor":"10",
				    "descricao":"f",
				    "chavePix":"%s"
				}
				""".formatted(conta2.getChavePix());
		mvc.perform(post("/transacao/pix").header("Authorization", "Bearer " + tokenAcesso)
				.contentType(MediaType.APPLICATION_JSON).content(pixJson)).andExpect(status().isOk())
				.andExpect(jsonPath("$.tipo").value(TipoTransacao.PIX.name()));
	}

	@Test
	@DisplayName("pix_retornaUNAUTHORIZED_quandoTokenAcessoInvalido")
	void pix_retornaUNAUTHORIZED_quandoTokenAcessoInvalido() throws Exception {
		String pixJson = """
				{
				    "senha":"123",
				    "valor":"10",
				    "descricao":"f",
				    "chavePix":"%s"
				}
				""";
		mvc.perform(post("/transacao/pix").header("Authorization", "Bearer invalido")
				.contentType(MediaType.APPLICATION_JSON).content(pixJson)).andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("pix_retornaBAD_REQUEST_quandoSemSaldo")
	void pix_retornaBAD_REQUEST_quandoSemSaldo() throws Exception {
		String pixJson = """
				{
				    "senha":"123",
				    "valor":"10",
				    "descricao":"f",
				    "chavePix":"%s"
				}
				""".formatted(conta2.getChavePix());
		mvc.perform(post("/transacao/pix").header("Authorization", "Bearer " + tokenAcesso)
				.contentType(MediaType.APPLICATION_JSON).content(pixJson)).andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("pix_retornaBAD_REQUEST_quandoValorAcimaDoLimitePix")
	void pix_retornaBAD_REQUEST_quandoValorAcimaDoLimitePix() throws Exception {
		conta.setSaldo(BigDecimal.valueOf(10002));
		repositorioConta.save(conta);
		String pixJson = """
				{
				    "senha":"123",
				    "valor":"10001",
				    "descricao":"f",
				    "chavePix":"%s"
				}
				""".formatted(conta2.getChavePix());
		mvc.perform(post("/transacao/pix").header("Authorization", "Bearer " + tokenAcesso)
				.contentType(MediaType.APPLICATION_JSON).content(pixJson)).andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("pix_retornaBAD_REQUEST_quandoSenhaIncorreta")
	void pix_retornaBAD_REQUEST_quandoSenhaIncorreta() throws Exception {
		String pixJson = """
				{
				    "senha":"1234",
				    "valor":"1",
				    "descricao":"f",
				    "chavePix":"%s"
				}
				""".formatted(conta2.getChavePix());
		mvc.perform(post("/transacao/pix").header("Authorization", "Bearer " + tokenAcesso)
				.contentType(MediaType.APPLICATION_JSON).content(pixJson)).andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("pix_retornaBAD_REQUEST_quandoValorNegativo")
	void pix_retornaBAD_REQUEST_quandoValorNegativo() throws Exception {
		String pixJson = """
				{
				    "senha":"123",
				    "valor":"-1",
				    "descricao":"f",
				    "chavePix":"%s"
				}
				""".formatted(conta2.getChavePix());
		mvc.perform(post("/transacao/pix").header("Authorization", "Bearer " + tokenAcesso)
				.contentType(MediaType.APPLICATION_JSON).content(pixJson)).andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("pix_retornaBAD_REQUEST_quandoValorZero")
	void pix_retornaBAD_REQUEST_quandoValorZero() throws Exception {
		String pixJson = """
				{
				    "senha":"123",
				    "valor":"0",
				    "descricao":"f",
				    "chavePix":"%s"
				}
				""".formatted(conta2.getChavePix());
		mvc.perform(post("/transacao/pix").header("Authorization", "Bearer " + tokenAcesso)
				.contentType(MediaType.APPLICATION_JSON).content(pixJson)).andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("pix_retornaBAD_REQUEST_quandoExcedePixPorDia")
	void pix_retornaBAD_REQUEST_quandoExcedePixPorDia() throws Exception {
		conta.setSaldo(BigDecimal.valueOf(40000));
		repositorioConta.save(conta);
		String pixJson = """
				{
				    "senha":"123",
				    "valor":"10000",
				    "descricao":"f",
				    "chavePix":"%s"
				}
				""".formatted(conta2.getChavePix());
		mvc.perform(post("/transacao/pix").header("Authorization", "Bearer " + tokenAcesso)
				.contentType(MediaType.APPLICATION_JSON).content(pixJson)).andExpect(status().isOk());
		mvc.perform(post("/transacao/pix").header("Authorization", "Bearer " + tokenAcesso)
				.contentType(MediaType.APPLICATION_JSON).content(pixJson)).andExpect(status().isOk());
		mvc.perform(post("/transacao/pix").header("Authorization", "Bearer " + tokenAcesso)
				.contentType(MediaType.APPLICATION_JSON).content(pixJson)).andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("pix_retornaBAD_REQUEST_quandoPixParaSi")
	void pix_retornaBAD_REQUEST_quandoPixParaSi() throws Exception {
		conta.setSaldo(BigDecimal.valueOf(1000));
		repositorioConta.save(conta);
		String pixJson = """
				{
				    "senha":"123",
				    "valor":"10000",
				    "descricao":"f",
				    "chavePix":"%s"
				}
				""".formatted(conta.getChavePix());
		mvc.perform(post("/transacao/pix").header("Authorization", "Bearer " + tokenAcesso)
				.contentType(MediaType.APPLICATION_JSON).content(pixJson)).andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("pix_retornaBAD_REQUEST_quandoChaveNaoExiste")
	void pix_retornaBAD_REQUEST_quandoChaveNaoExiste() throws Exception {
		conta.setSaldo(BigDecimal.valueOf(1000));
		repositorioConta.save(conta);
		String pixJson = """
				{
				    "senha":"123",
				    "valor":"10000",
				    "descricao":"f",
				    "chavePix":"naoExiste"
				}
				""";
		mvc.perform(post("/transacao/pix").header("Authorization", "Bearer " + tokenAcesso)
				.contentType(MediaType.APPLICATION_JSON).content(pixJson)).andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("pix_retornaFORBIDDEN_quandoContaOrigemBloqueada")
	void pix_retornaFORBIDDEN_quandoContaOrigemBloqueada() throws Exception {
		conta.setStatus(StatusDaConta.BLOQUEADA);
		repositorioConta.save(conta);
		String pixJson = """
				{
				    "senha":"123",
				    "valor":"10000",
				    "descricao":"f",
				    "chavePix":"%s"
				}
				""".formatted(conta2.getChavePix());
		mvc.perform(post("/transacao/pix").header("Authorization", "Bearer " + tokenAcesso)
				.contentType(MediaType.APPLICATION_JSON).content(pixJson)).andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("pix_retornaFORBIDDEN_quandoContaOrigemEncerrada")
	void pix_retornaFORBIDDEN_quandoContaOrigemEncerrada() throws Exception {
		conta.setStatus(StatusDaConta.ENCERRADA);
		repositorioConta.save(conta);
		String pixJson = """
				{
				    "senha":"123",
				    "valor":"10000",
				    "descricao":"f",
				    "chavePix":"%s"
				}
				""".formatted(conta2.getChavePix());
		mvc.perform(post("/transacao/pix").header("Authorization", "Bearer " + tokenAcesso)
				.contentType(MediaType.APPLICATION_JSON).content(pixJson)).andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("pix_retornaFORBIDDEN_quandoContaDestinoEncerrada")
	void pix_retornaFORBIDDEN_quandoContaDestinoEncerrada() throws Exception {
		conta2.setStatus(StatusDaConta.ENCERRADA);
		conta.setSaldo(BigDecimal.valueOf(1000));
		repositorioConta.save(conta2);
		repositorioConta.save(conta);
		String pixJson = """
				{
				    "senha":"123",
				    "valor":"1",
				    "descricao":"f",
				    "chavePix":"%s"
				}
				""".formatted(conta2.getChavePix());
		mvc.perform(post("/transacao/pix").header("Authorization", "Bearer " + tokenAcesso)
				.contentType(MediaType.APPLICATION_JSON).content(pixJson)).andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("pix_retornaOK_quandoContaDestinoBloqueada")
	void pix_retornaOK_quandoContaDestinoBloqueada() throws Exception {
		conta2.setStatus(StatusDaConta.BLOQUEADA);
		conta.setSaldo(BigDecimal.valueOf(1000));
		repositorioConta.save(conta2);
		repositorioConta.save(conta);
		String pixJson = """
				{
				    "senha":"123",
				    "valor":"1",
				    "descricao":"f",
				    "chavePix":"%s"
				}
				""".formatted(conta2.getChavePix());
		mvc.perform(post("/transacao/pix").header("Authorization", "Bearer " + tokenAcesso)
				.contentType(MediaType.APPLICATION_JSON).content(pixJson)).andExpect(status().isOk());
	}

	// POST - TRANSFERÊNCIA
	@Test
	@DisplayName("transferencia_retornaOK_quandoSucesso")
	void transferencia_retornaOK_quandoSucesso() throws Exception {
		conta.setSaldo(BigDecimal.valueOf(10));
		repositorioConta.save(conta);
		String pixJson = """
				{
				    "senha":"123",
				    "valor":"10",
				    "descricao":"f",
				    "idContaDestino":"%s"
				}
				""".formatted(conta2.getId());
		mvc.perform(post("/transacao/transferencia").header("Authorization", "Bearer " + tokenAcesso)
				.contentType(MediaType.APPLICATION_JSON).content(pixJson)).andExpect(status().isOk())
				.andExpect(jsonPath("$.tipo").value(TipoTransacao.TRANSFERENCIA.name()));
	}

	@Test
	@DisplayName("transferencia_retornaBAD_REQUEST_quandoValorNegativo")
	void transferencia_retornaBAD_REQUEST_quandoValorNegativo() throws Exception {
		String pixJson = """
				{
				    "senha":"123",
				    "valor":"-10",
				    "descricao":"f",
				    "idContaDestino":"%s"
				}
				""".formatted(conta2.getId());
		mvc.perform(post("/transacao/transferencia").header("Authorization", "Bearer " + tokenAcesso)
				.contentType(MediaType.APPLICATION_JSON).content(pixJson)).andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("transferencia_retornaBAD_REQUEST_quandoValorZero")
	void transferencia_retornaBAD_REQUEST_quandoValorZero() throws Exception {
		String pixJson = """
				{
				    "senha":"123",
				    "valor":"0",
				    "descricao":"f",
				    "idContaDestino":"%s"
				}
				""".formatted(conta2.getId());
		mvc.perform(post("/transacao/transferencia").header("Authorization", "Bearer " + tokenAcesso)
				.contentType(MediaType.APPLICATION_JSON).content(pixJson)).andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("transferencia_retornaBAD_REQUEST_quandoValorExcedeLimite")
	void transferencia_retornaBAD_REQUEST_quandoValorExcedeLimite() throws Exception {
		conta.setSaldo(BigDecimal.valueOf(10001));
		repositorioConta.save(conta);
		String pixJson = """
				{
				    "senha":"123",
				    "valor":"10001",
				    "descricao":"f",
				    "idContaDestino":"%s"
				}
				""".formatted(conta2.getId());
		mvc.perform(post("/transacao/transferencia").header("Authorization", "Bearer " + tokenAcesso)
				.contentType(MediaType.APPLICATION_JSON).content(pixJson)).andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("transferencia_retornaBAD_REQUEST_quandoSenhaErrada")
	void transferencia_retornaBAD_REQUEST_quandoSenhaErrada() throws Exception {
		conta.setSaldo(BigDecimal.valueOf(10));
		repositorioConta.save(conta);
		String pixJson = """
				{
				    "senha":"1243",
				    "valor":"10",
				    "descricao":"f",
				    "idContaDestino":"%s"
				}
				""".formatted(conta2.getId());
		mvc.perform(post("/transacao/transferencia").header("Authorization", "Bearer " + tokenAcesso)
				.contentType(MediaType.APPLICATION_JSON).content(pixJson)).andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("transferencia_retornaBAD_REQUEST_quandoParaSi")
	void transferencia_retornaBAD_REQUEST_quandoParaSi() throws Exception {
		conta.setSaldo(BigDecimal.valueOf(10));
		repositorioConta.save(conta);
		String pixJson = """
				{
				    "senha":"123",
				    "valor":"10",
				    "descricao":"f",
				    "idContaDestino":"%s"
				}
				""".formatted(conta.getId());
		mvc.perform(post("/transacao/transferencia").header("Authorization", "Bearer " + tokenAcesso)
				.contentType(MediaType.APPLICATION_JSON).content(pixJson)).andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("transferencia_retornaNOT_FOUND_quandoIdDestinoNaoExiste")
	void transferencia_retornaNOT_FOUND_quandoIdDestinoNaoExiste() throws Exception {
		String pixJson = """
				{
				    "senha":"123",
				    "valor":"10",
				    "descricao":"f",
				    "idContaDestino":"3000"
				}
				""".formatted(conta2.getId());
		mvc.perform(post("/transacao/transferencia").header("Authorization", "Bearer " + tokenAcesso)
				.contentType(MediaType.APPLICATION_JSON).content(pixJson)).andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("transferencia_retornaFORBIDDEN_quandoContaOrigemBloqueada")
	void transferencia_retornaFORBIDDEN_quandoContaOrigemBloqueada() throws Exception {
		conta.setStatus(StatusDaConta.BLOQUEADA);
		conta.setSaldo(BigDecimal.valueOf(10));
		repositorioConta.save(conta);
		String pixJson = """
				{
				    "senha":"123",
				    "valor":"10",
				    "descricao":"f",
				    "idContaDestino":"%s"
				}
				""".formatted(conta.getId());
		mvc.perform(post("/transacao/transferencia").header("Authorization", "Bearer " + tokenAcesso)
				.contentType(MediaType.APPLICATION_JSON).content(pixJson)).andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("transferencia_retornaFORBIDDEN_quandoContaOrigemEncerrada")
	void transferencia_retornaFORBIDDEN_quandoContaOrigemEncerrada() throws Exception {
		conta.setStatus(StatusDaConta.ENCERRADA);
		conta.setSaldo(BigDecimal.valueOf(10));
		repositorioConta.save(conta);
		String pixJson = """
				{
				    "senha":"123",
				    "valor":"10",
				    "descricao":"f",
				    "idContaDestino":"%s"
				}
				""".formatted(conta.getId());
		mvc.perform(post("/transacao/transferencia").header("Authorization", "Bearer " + tokenAcesso)
				.contentType(MediaType.APPLICATION_JSON).content(pixJson)).andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("transferencia_retornaOK_quandoContaDestinoBloqueada")
	void transferencia_retornaFORBIDDEN_quandoContaDestinoBloqueada() throws Exception {
		conta2.setStatus(StatusDaConta.BLOQUEADA);
		conta.setSaldo(BigDecimal.valueOf(10));
		repositorioConta.save(conta);
		repositorioConta.save(conta2);
		String pixJson = """
				{
				    "senha":"123",
				    "valor":"10",
				    "descricao":"f",
				    "idContaDestino":"%s"
				}
				""".formatted(conta2.getId());
		mvc.perform(post("/transacao/transferencia").header("Authorization", "Bearer " + tokenAcesso)
				.contentType(MediaType.APPLICATION_JSON).content(pixJson)).andExpect(status().isOk());
	}

	@Test
	@DisplayName("transferencia_retornaFORBIDDEN_quandoContaDestinoEncerrada")
	void transferencia_retornaFORBIDDEN_quandoContaDestinoEncerrada() throws Exception {
		conta2.setStatus(StatusDaConta.ENCERRADA);
		conta.setSaldo(BigDecimal.valueOf(10));
		repositorioConta.save(conta);
		repositorioConta.save(conta2);
		String pixJson = """
				{
				    "senha":"123",
				    "valor":"10",
				    "descricao":"f",
				    "idContaDestino":"%s"
				}
				""".formatted(conta2.getId());
		mvc.perform(post("/transacao/transferencia").header("Authorization", "Bearer " + tokenAcesso)
				.contentType(MediaType.APPLICATION_JSON).content(pixJson)).andExpect(status().isForbidden());
	}
}
