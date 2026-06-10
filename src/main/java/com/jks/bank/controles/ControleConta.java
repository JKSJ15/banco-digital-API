package com.jks.bank.controles;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jks.bank.dto.ContaResponseDto;
import com.jks.bank.dto.SenhaDto;
import com.jks.bank.servicos.ServicoConta;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/conta")
public class ControleConta {
	private final ServicoConta servConta;

	public ControleConta(ServicoConta servConta) {
		super();
		this.servConta = servConta;
	}

	@Operation(summary = "Consultar dados da conta", description = """
			Retorna os dados completos da conta do usuário autenticado.

			Inclui:
			- ID da conta
			- Agência
			- Número da conta
			- Chave Pix
			- Saldo atual
			- Status da conta
			- Data de criação
			- CEP cadastrado
			- Dados de endereço obtidos via ViaCEP

			Caso a API ViaCEP esteja indisponível, os dados da conta
			continuam sendo retornados normalmente e os campos de endereço
			serão preenchidos com null.
			""")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Dados da conta retornados com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ContaResponseDto.class), examples = @ExampleObject(value = """
					{
					  "id": 2,
					  "agencia": "001",
					  "numero": "923366",
					  "chavePix": "511afa11-e655-4ff9-98c4-953a8b65a2e1",
					  "saldo": 30474.50,
					  "status": "ATIVA",
					  "dataCriacao": "2026-05-31",
					  "cep": "55730000",
					  "bairro": "",
					  "localidade": "Bom Jardim",
					  "uf": "PE",
					  "estado": "Pernambuco",
					  "regiao": "Nordeste"
					}
					"""))),

			@ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
					{
					  "mensagem": "não autorizado!",
					  "tempo": "2026-06-10T10:00:00",
					  "status": "UNAUTHORIZED",
					  "codigo": 401
					}
					"""))),

			@ApiResponse(responseCode = "404", description = "Conta não encontrada", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
					{
					  "mensagem": "Conta não encontrada!",
					  "tempo": "2026-06-10T10:00:00",
					  "status": "NOT_FOUND",
					  "codigo": 404
					}
					"""))) })
	@GetMapping()
	public ResponseEntity<ContaResponseDto> contaUsuario() {
		return ResponseEntity.ok(servConta.contaUsuario());
	}

	@ApiResponses({ @ApiResponse(responseCode = "204", description = "Conta bloqueada com sucesso"),
			@ApiResponse(responseCode = "400", description = "Senha inválida", content = @Content(mediaType = "application/json", examples = {
					@ExampleObject(name = "Senha inválida", value = """
							{
							  "mensagem": "senha inválida!",
							  "tempo": "2026-06-10T10:00:00",
							  "status": "BAD_REQUEST",
							  "codigo": 400
							}
							""") })),
			@ApiResponse(responseCode = "404", description = "Conta não encontrada", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
					{
					  "mensagem": "Conta não encontrada!",
					  "tempo": "2026-06-10T10:00:00",
					  "status": "NOT_FOUND",
					  "codigo": 404
					}
					"""))) })
	@PostMapping("/bloquear")
	public ResponseEntity<Void> bloquearConta(@RequestBody @Valid SenhaDto senha) {
		servConta.bloquearConta(senha);
		return ResponseEntity.noContent().build();
	}

	@ApiResponses({ @ApiResponse(responseCode = "204", description = "Conta desbloqueada com sucesso"),
			@ApiResponse(responseCode = "400", description = "Senha inválida", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
					{
					  "mensagem": "senha inválida!",
					  "tempo": "2026-06-10T10:00:00",
					  "status": "BAD_REQUEST",
					  "codigo": 400
					}
					"""))),
			@ApiResponse(responseCode = "404", description = "Conta não encontrada", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
					{
					  "mensagem": "Conta não encontrada!",
					  "tempo": "2026-06-10T10:00:00",
					  "status": "NOT_FOUND",
					  "codigo": 404
					}
					"""))) })
	@PostMapping("/desbloquear")
	public ResponseEntity<Void> desbloquearConta(@RequestBody @Valid SenhaDto senha) {
		servConta.desbloquearConta(senha);
		return ResponseEntity.noContent().build();
	}

	@ApiResponses({ @ApiResponse(responseCode = "204", description = "Conta encerrada com sucesso"),
			@ApiResponse(responseCode = "400", description = "Senha inválida ou conta possui saldo", content = @Content(mediaType = "application/json", examples = {
					@ExampleObject(name = "Senha inválida", value = """
							{
							  "mensagem": "senha inválida!",
							  "tempo": "2026-06-10T10:00:00",
							  "status": "BAD_REQUEST",
							  "codigo": 400
							}
							"""), @ExampleObject(name = "Conta possui saldo", value = """
							{
							  "mensagem": "não foi possível encerrar sua conta, pois ela contém dinheiro!",
							  "tempo": "2026-06-10T10:00:00",
							  "status": "BAD_REQUEST",
							  "codigo": 400
							}
							""") })),
			@ApiResponse(responseCode = "404", description = "Conta não encontrada", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
					{
					  "mensagem": "Conta não encontrada!",
					  "tempo": "2026-06-10T10:00:00",
					  "status": "NOT_FOUND",
					  "codigo": 404
					}
					"""))) })
	@PostMapping("/encerrar")
	public ResponseEntity<Void> encerrarConta(@RequestBody @Valid SenhaDto senha) {
		servConta.encerrarConta(senha);
		return ResponseEntity.noContent().build();
	}
}
