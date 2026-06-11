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
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/conta")
@Tag(name = "Conta", description = "Endpoints para consulta e gerenciamento da conta bancária do usuário autenticado")
@SecurityRequirement(name = "bearerAuth")
public class ControleConta {
	private final ServicoConta servConta;

	public ControleConta(ServicoConta servConta) {
		super();
		this.servConta = servConta;
	}

	@Operation(summary = "Consultar dados da conta", description = "Retorna os dados completos da conta do usuário autenticado, incluindo agência, número, "
			+ "chave Pix, saldo, status, data de criação e endereço derivado do CEP via ViaCEP. "
			+ "Caso a consulta do CEP falhe, os campos de endereço são retornados como nulos.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Dados da conta retornados com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ContaResponseDto.class), examples = {
					@ExampleObject(name = "Com endereço", value = """
							{
							  "id": 7,
							  "agencia": "001",
							  "numero": "482910",
							  "chavePix": "e3b0c442-98fc-1c14-9afb-f4c8996fb924",
							  "saldo": 850.00,
							  "status": "ATIVA",
							  "dataDaCriacao": "2025-01-15",
							  "cep": "01310100",
							  "bairro": "Bela Vista",
							  "localidade": "São Paulo",
							  "uf": "SP",
							  "estado": "São Paulo",
							  "regiao": "Sudeste"
							}
							"""), @ExampleObject(name = "Sem endereço (ViaCEP indisponível)", value = """
							{
							  "id": 7,
							  "agencia": "001",
							  "numero": "482910",
							  "chavePix": "e3b0c442-98fc-1c14-9afb-f4c8996fb924",
							  "saldo": 850.00,
							  "status": "ATIVA",
							  "dataDaCriacao": "2025-01-15",
							  "cep": "01310100",
							  "bairro": null,
							  "localidade": null,
							  "uf": null,
							  "estado": null,
							  "regiao": null
							}
							""") })),
			@ApiResponse(responseCode = "404", description = "Usuário ou conta não encontrados", content = @Content(mediaType = "application/json", examples = {
					@ExampleObject(name = "Usuário não encontrado", value = """
							{
							  "mensagem": "usuario não encontado!",
							  "timestamp": "2025-06-10T14:30:00",
							  "status": "NOT_FOUND",
							  "codigo": 404
							}
							"""), @ExampleObject(name = "Conta não encontrada", value = """
							{
							  "mensagem": "Conta não encontrada!",
							  "timestamp": "2025-06-10T14:30:00",
							  "status": "NOT_FOUND",
							  "codigo": 404
							}
							""") })) })
	@GetMapping()
	public ResponseEntity<ContaResponseDto> contaUsuario() {
		return ResponseEntity.ok(servConta.contaUsuario());
	}

	@Operation(summary = "Bloquear conta", description = "Bloqueia a conta do usuário autenticado. Enquanto bloqueada, operações de saída "
			+ "(saque, Pix e transferência) são recusadas. Depósitos recebidos de terceiros continuam permitidos. "
			+ "Requer confirmação de senha. O bloqueio pode ser revertido pelo endpoint /desbloquear.")
	@ApiResponses({
			@ApiResponse(responseCode = "204", description = "Conta bloqueada com sucesso — sem corpo na resposta"),
			@ApiResponse(responseCode = "400", description = "Campos obrigatórios ausentes, fora do padrão de validação ou senha incorreta", content = @Content(mediaType = "application/json", examples = {
					@ExampleObject(name = "Senha inválida", value = """
							{
							  "mensagem": "senha inválida!",
							  "timestamp": "2025-06-10T14:30:00",
							  "status": "BAD_REQUEST",
							  "codigo": 400
							}
							"""), @ExampleObject(name = "Campo inválido", value = """
							{
							  "mensagem": "senha não pode ser vazia",
							  "timestamp": "2025-06-10T14:30:00",
							  "status": "BAD_REQUEST",
							  "codigo": 400
							}
							""") })),
			@ApiResponse(responseCode = "404", description = "Usuário ou conta não encontrados", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
					{
					  "mensagem": "Conta não encontrada!",
					  "timestamp": "2025-06-10T14:30:00",
					  "status": "NOT_FOUND",
					  "codigo": 404
					}
					"""))) })
	@PostMapping("/bloquear")
	public ResponseEntity<Void> bloquearConta(@RequestBody @Valid SenhaDto senha) {
		servConta.bloquearConta(senha);
		return ResponseEntity.noContent().build();
	}

	@Operation(summary = "Desbloquear conta", description = "Remove o bloqueio da conta do usuário autenticado, restaurando todas as funcionalidades. "
			+ "Requer confirmação de senha.")
	@ApiResponses({
			@ApiResponse(responseCode = "204", description = "Conta desbloqueada com sucesso — sem corpo na resposta"),
			@ApiResponse(responseCode = "400", description = "Campos obrigatórios ausentes, fora do padrão de validação ou senha incorreta", content = @Content(mediaType = "application/json", examples = {
					@ExampleObject(name = "Senha inválida", value = """
							{
							  "mensagem": "senha inválida!",
							  "timestamp": "2025-06-10T14:30:00",
							  "status": "BAD_REQUEST",
							  "codigo": 400
							}
							"""), @ExampleObject(name = "Campo inválido", value = """
							{
							  "mensagem": "senha não pode ser vazia",
							  "timestamp": "2025-06-10T14:30:00",
							  "status": "BAD_REQUEST",
							  "codigo": 400
							}
							""") })),
			@ApiResponse(responseCode = "404", description = "Usuário ou conta não encontrados", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
					{
					  "mensagem": "Conta não encontrada!",
					  "timestamp": "2025-06-10T14:30:00",
					  "status": "NOT_FOUND",
					  "codigo": 404
					}
					"""))) })
	@PostMapping("/desbloquear")
	public ResponseEntity<Void> desbloquearConta(@RequestBody @Valid SenhaDto senha) {
		servConta.desbloquearConta(senha);
		return ResponseEntity.noContent().build();
	}

	@Operation(summary = "Encerrar conta", description = "Encerra definitivamente a conta do usuário autenticado. "
			+ "Esta operação é irreversível. Requer confirmação de senha e saldo igual a R$ 0,00. "
			+ "Caso haja saldo disponível, a operação é recusada — transfira ou saque o valor antes de encerrar.")
	@ApiResponses({
			@ApiResponse(responseCode = "204", description = "Conta encerrada com sucesso — sem corpo na resposta"),
			@ApiResponse(responseCode = "400", description = "Campos obrigatórios ausentes, fora do padrão de validação, senha incorreta ou conta com saldo positivo", content = @Content(mediaType = "application/json", examples = {
					@ExampleObject(name = "Conta com saldo", value = """
							{
							  "mensagem": "não foi possível encerrar sua conta, pois ela contém dinheiro!",
							  "timestamp": "2025-06-10T14:30:00",
							  "status": "BAD_REQUEST",
							  "codigo": 400
							}
							"""), @ExampleObject(name = "Senha inválida", value = """
							{
							  "mensagem": "senha inválida!",
							  "timestamp": "2025-06-10T14:30:00",
							  "status": "BAD_REQUEST",
							  "codigo": 400
							}
							""") })),
			@ApiResponse(responseCode = "404", description = "Usuário ou conta não encontrados", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
					{
					  "mensagem": "Conta não encontrada!",
					  "timestamp": "2025-06-10T14:30:00",
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
