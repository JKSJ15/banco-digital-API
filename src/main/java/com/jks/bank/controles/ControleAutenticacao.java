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
import com.jks.bank.exceptions.NaoAutorizadoException;
import com.jks.bank.repositorios.RepositorioUsuario;
import com.jks.bank.servicos.ServicoAutenticacao;
import com.jks.bank.servicos.ServicoRefreshToken;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticação", description = "Endpoints para registro, login, renovação de token e logout de usuários")
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

	@Operation(summary = "Autenticar usuário", description = "Realiza o login com login e senha. Retorna um token de acesso (válido por 15 minutos) "
			+ "e um refresh token (válido por 3 dias).")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Login realizado com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TokensResponse.class), examples = @ExampleObject(value = """
					{
					  "tokenAcesso": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
					  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
					}
					"""))),
			@ApiResponse(responseCode = "400", description = "Campos obrigatórios ausentes ou fora do padrão de validação", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
					{
					  "mensagem": "login não pode ser vazio",
					  "tempo": "2025-06-10T14:30:00",
					  "status": "BAD_REQUEST",
					  "codigo": 400
					}
					"""))),
			@ApiResponse(responseCode = "401", description = "Credenciais inválidas — login ou senha incorretos", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
					{
					  "mensagem": "usuário não registrado!",
					  "tempo": "2025-06-10T14:30:00",
					  "status": "UNAUTHORIZED",
					  "codigo": 401
					}
					"""))) })
	@PostMapping("/login")
	public ResponseEntity<TokensResponse> login(@RequestBody @Valid LoginRequestDto request) {
		return ResponseEntity.ok(servicoAutenticacao.login(request));
	}

	@Operation(summary = "Registrar novo usuário", description = "Cria um novo usuário e gera sua conta bancária automaticamente. "
			+ "O CEP é validado via ViaCEP. O usuário deve ter no mínimo 18 anos. "
			+ "CPF, login e telefone devem ser únicos. "
			+ "A conta é criada com agência 001, número aleatório de 6 dígitos e chave Pix UUID.")
	@ApiResponses({
			@ApiResponse(responseCode = "204", description = "Usuário e conta criados com sucesso — sem corpo na resposta"),
			@ApiResponse(responseCode = "400", description = "Dados inválidos: CEP inexistente, usuário menor de 18 anos, campos fora do padrão, "
					+ "CPF/login/telefone já cadastrados", content = @Content(mediaType = "application/json", examples = {
							@ExampleObject(name = "CEP inválido", value = """
									{
									  "mensagem": "cep inválido!",
									  "tempo": "2025-06-10T14:30:00",
									  "status": "BAD_REQUEST",
									  "codigo": 400
									}
									"""), @ExampleObject(name = "Idade insuficiente", value = """
									{
									  "mensagem": "voce ainda não possui a idade necessária para criar uma conta!",
									  "tempo": "2025-06-10T14:30:00",
									  "status": "BAD_REQUEST",
									  "codigo": 400
									}
									"""), @ExampleObject(name = "CPF já cadastrado", value = """
									{
									  "mensagem": "cpf já cadastrado!",
									  "tempo": "2025-06-10T14:30:00",
									  "status": "BAD_REQUEST",
									  "codigo": 400
									}
									"""), @ExampleObject(name = "Login já cadastrado", value = """
									{
									  "mensagem": "usuario já cadastrado!",
									  "tempo": "2025-06-10T14:30:00",
									  "status": "BAD_REQUEST",
									  "codigo": 400
									}
									"""), @ExampleObject(name = "Telefone já cadastrado", value = """
									{
									  "mensagem": "telefone já cadastrado!",
									  "tempo": "2025-06-10T14:30:00",
									  "status": "BAD_REQUEST",
									  "codigo": 400
									}
									""") })) })
	@PostMapping("/registro")
	public ResponseEntity<Void> registro(@RequestBody @Valid RegistroRequestDto request) {
		servicoAutenticacao.registro(request);
		return ResponseEntity.noContent().build();
	}

	@Operation(summary = "Renovar tokens de acesso", description = "Recebe um refresh token válido e retorna um novo par de tokens (acesso + refresh). "
			+ "O token utilizado é invalidado imediatamente (rotação de token). "
			+ "O refresh token tem validade de 3 dias.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Tokens renovados com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TokensResponse.class), examples = @ExampleObject(value = """
					{
					  "tokenAcesso": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
					  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
					}
					"""))),
			@ApiResponse(responseCode = "400", description = "Campos obrigatórios ausentes ou fora do padrão de validação", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
					{
					  "mensagem": "refreshToken não pode ser vazio",
					  "tempo": "2025-06-10T14:30:00",
					  "status": "BAD_REQUEST",
					  "codigo": 400
					}
					"""))),
			@ApiResponse(responseCode = "403", description = "Refresh token inválido, expirado ou não encontrado no sistema", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
					{
					  "mensagem": "refresh token inválido!",
					  "tempo": "2025-06-10T14:30:00",
					  "status": "FORBIDDEN",
					  "codigo": 403
					}
					"""))) })
	@PostMapping("/refresh")
	public ResponseEntity<TokensResponse> refresh(@RequestBody @Valid RefreshRequestDto request) {
		return ResponseEntity.ok(servicoAutenticacao.refresh(request));
	}

	@Operation(summary = "Encerrar sessão do usuário", description = "Realiza o logout do usuário autenticado, invalidando todos os seus refresh tokens persistidos. "
			+ "Requer token de acesso JWT válido no header Authorization.")
	@ApiResponses({
			@ApiResponse(responseCode = "204", description = "Logout realizado com sucesso — sem corpo na resposta"),
			@ApiResponse(responseCode = "401", description = "Token de acesso ausente, inválido ou expirado", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
					{
					  "mensagem": "usuário não autenticado!",
					  "tempo": "2025-06-10T14:30:00",
					  "status": "UNAUTHORIZED",
					  "codigo": 401
					}
					"""))) })
	@PostMapping("/sair")
	public ResponseEntity<Void> sair(Authentication autenticacao) {
		servicoRefreshToken.deletarPeloUsuario(usuarioAutenticado());
		return ResponseEntity.noContent().build();
	}

	// MÉTODOS INTERNOS
	private Usuario usuarioAutenticado() {
		String login = SecurityContextHolder.getContext().getAuthentication().getName();
		return repUsuario.findByLogin(login).orElseThrow(() -> new NaoAutorizadoException("usuário não autenticado!"));
	}
}