package com.jks.bank.controles;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jks.bank.dto.DepositoRequestDto;
import com.jks.bank.dto.PixRequestDto;
import com.jks.bank.dto.RelatorioDto;
import com.jks.bank.dto.SaqueRequestDto;
import com.jks.bank.dto.TransacaoResponseDto;
import com.jks.bank.dto.TransferenciaRequestDto;
import com.jks.bank.servicos.ServicoTransacoes;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/transacao")
@Tag(name = "Transações", description = "Endpoints para movimentações financeiras: depósito, saque, Pix, transferência, extrato e relatório mensal")
@SecurityRequirement(name = "bearerAuth")
public class ControleTransacoes {

	private final ServicoTransacoes servTransacoes;

	public ControleTransacoes(ServicoTransacoes servTransacoes) {
		super();
		this.servTransacoes = servTransacoes;
	}

	@Operation(summary = "Realizar depósito", description = "Deposita um valor na conta do usuário autenticado. "
			+ "Requer confirmação de senha. Limite máximo por operação: R$ 50.000,00. "
			+ "A conta não pode estar encerrada para receber depósitos.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Depósito realizado com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransacaoResponseDto.class), examples = @ExampleObject(value = """
					{
					  "id": 42,
					  "tipo": "DEPOSITO",
					  "valor": 500.00,
					  "descricao": "depósito mensal",
					  "saldoAnterior": 1000.00,
					  "saldoPosterior": 1500.00,
					  "data": "2025-06-10T14:30:00",
					  "contaOrigem": null,
					  "contaDestino": 7
					}
					"""))),
			@ApiResponse(responseCode = "400", description = "Senha incorreta, valor inválido ou acima do limite de R$ 50.000,00", content = @Content(mediaType = "application/json", examples = {
					@ExampleObject(name = "Senha inválida", value = """
							{
							  "mensagem": "senha inválida!",
							  "timestamp": "2025-06-10T14:30:00",
							  "status": "BAD_REQUEST",
							  "codigo": 400
							}
							"""), @ExampleObject(name = "Limite excedido", value = """
							{
							  "mensagem": "Depósito excede o limite permitido!",
							  "timestamp": "2025-06-10T14:30:00",
							  "status": "BAD_REQUEST",
							  "codigo": 400
							}
							""") })),
			@ApiResponse(responseCode = "403", description = "Conta encerrada — não é possível receber depósitos", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
					{
					  "mensagem": "indisponível, a conta ID:7 está ENCERRADA",
					  "timestamp": "2025-06-10T14:30:00",
					  "status": "FORBIDDEN",
					  "codigo": 403
					}
					"""))),
			@ApiResponse(responseCode = "404", description = "Conta do usuário autenticado não encontrada", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
					{
					  "mensagem": "Conta não encontrada!",
					  "timestamp": "2025-06-10T14:30:00",
					  "status": "NOT_FOUND",
					  "codigo": 404
					}
					"""))) })
	@PostMapping("/deposito")
	public ResponseEntity<TransacaoResponseDto> deposito(@RequestBody @Valid DepositoRequestDto depositoRequest) {
		return ResponseEntity.ok(servTransacoes.deposito(depositoRequest));
	}

	@Operation(summary = "Realizar saque", description = "Saca um valor da conta do usuário autenticado. "
			+ "Requer confirmação de senha. Limite máximo por operação: R$ 5.000,00. "
			+ "A conta deve estar ativa (não bloqueada ou encerrada) e possuir saldo suficiente.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Saque realizado com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransacaoResponseDto.class), examples = @ExampleObject(value = """
					{
					  "id": 43,
					  "tipo": "SAQUE",
					  "valor": 200.00,
					  "descricao": "saque no caixa",
					  "saldoAnterior": 1500.00,
					  "saldoPosterior": 1300.00,
					  "data": "2025-06-10T15:00:00",
					  "contaOrigem": 7,
					  "contaDestino": null
					}
					"""))),
			@ApiResponse(responseCode = "400", description = "Senha incorreta, valor inválido, acima do limite de R$ 5.000,00 ou saldo insuficiente", content = @Content(mediaType = "application/json", examples = {
					@ExampleObject(name = "Saldo insuficiente", value = """
							{
							  "mensagem": "saldo insuficiente!",
							  "tempo": "2025-06-10T15:00:00",
							  "status": "BAD_REQUEST",
							  "codigo": 400
							}
							"""), @ExampleObject(name = "Limite excedido", value = """
							{
							  "mensagem": "Saque excede o limite permitido!",
							  "tempo": "2025-06-10T15:00:00",
							  "status": "BAD_REQUEST",
							  "codigo": 400
							}
							"""), @ExampleObject(name = "Senha inválida", value = """
							{
							  "mensagem": "senha inválida!",
							  "timestamp": "2025-06-10T15:00:00",
							  "status": "BAD_REQUEST",
							  "codigo": 400
							}
							""") })),
			@ApiResponse(responseCode = "403", description = "Conta bloqueada ou encerrada — operação de saída não permitida", content = @Content(mediaType = "application/json", examples = {
					@ExampleObject(name = "Conta bloqueada", value = """
							{
							  "mensagem": "indisponível, sua conta está BLOQUEADA",
							  "tempo": "2025-06-10T15:00:00",
							  "status": "FORBIDDEN",
							  "codigo": 403
							}
							"""), @ExampleObject(name = "Conta encerrada", value = """
							{
							  "mensagem": "indisponível, sua conta está ENCERRADA",
							  "tempo": "2025-06-10T15:00:00",
							  "status": "FORBIDDEN",
							  "codigo": 403
							}
							""") })),
			@ApiResponse(responseCode = "404", description = "Conta do usuário autenticado não encontrada", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
					{
					  "mensagem": "Conta não encontrada!",
					  "tempo": "2025-06-10T15:00:00",
					  "status": "NOT_FOUND",
					  "codigo": 404
					}
					"""))) })
	@PostMapping("/saque")
	public ResponseEntity<TransacaoResponseDto> saque(@RequestBody @Valid SaqueRequestDto saqueRequest) {
		return ResponseEntity.ok(servTransacoes.saque(saqueRequest));
	}

	@Operation(summary = "Enviar Pix", description = "Transfere um valor via Pix para a conta identificada pela chave Pix informada. "
			+ "Requer confirmação de senha. Limites: R$ 10.000,00 por transação e R$ 20.000,00 acumulado no dia. "
			+ "A conta de origem deve estar ativa e com saldo suficiente. "
			+ "Não é permitido enviar Pix para a própria chave.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Pix realizado com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransacaoResponseDto.class), examples = @ExampleObject(value = """
					{
					  "id": 44,
					  "tipo": "PIX",
					  "valor": 150.00,
					  "descricao": "pagamento aluguel",
					  "saldoAnterior": 1300.00,
					  "saldoPosterior": 1150.00,
					  "data": "2025-06-10T15:30:00",
					  "contaOrigem": 7,
					  "contaDestino": 12
					}
					"""))),
			@ApiResponse(responseCode = "400", description = "Senha incorreta, valor inválido, limite por transação (R$ 10.000,00) ou limite diário (R$ 20.000,00) excedido, saldo insuficiente, ou Pix para a própria conta", content = @Content(mediaType = "application/json", examples = {
					@ExampleObject(name = "Limite por transação", value = """
							{
							  "mensagem": "PIX excede o limite permitido!",
							  "tempo": "2025-06-10T15:30:00",
							  "status": "BAD_REQUEST",
							  "codigo": 400
							}
							"""), @ExampleObject(name = "Limite diário excedido", value = """
							{
							  "mensagem": "limite diário de PIX excedido!",
							  "tempo": "2025-06-10T15:30:00",
							  "status": "BAD_REQUEST",
							  "codigo": 400
							}
							"""), @ExampleObject(name = "Saldo insuficiente", value = """
							{
							  "mensagem": "saldo insuficiente!",
							  "tempo": "2025-06-10T15:30:00",
							  "status": "BAD_REQUEST",
							  "codigo": 400
							}
							"""), @ExampleObject(name = "Pix para a própria conta", value = """
							{
							  "mensagem": "transferência inválida!",
							  "tempo": "2025-06-10T15:30:00",
							  "status": "BAD_REQUEST",
							  "codigo": 400
							}
							""") })),
			@ApiResponse(responseCode = "403", description = "Conta de origem bloqueada ou encerrada", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
					{
					  "mensagem": "indisponível, sua conta está BLOQUEADA",
					  "tempo": "2025-06-10T15:30:00",
					  "status": "FORBIDDEN",
					  "codigo": 403
					}
					"""))),
			@ApiResponse(responseCode = "404", description = "Chave Pix não encontrada", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
					{
					  "mensagem": "conta destino não encontrada!",
					  "tempo": "2025-06-10T15:30:00",
					  "status": "NOT_FOUND",
					  "codigo": 404
					}
					"""))) })
	@PostMapping("/pix")
	public ResponseEntity<TransacaoResponseDto> pix(@RequestBody @Valid PixRequestDto pixRequest) {
		return ResponseEntity.ok(servTransacoes.pix(pixRequest));
	}

	@Operation(summary = "Realizar transferência", description = "Transfere um valor para outra conta pelo ID da conta destino. "
			+ "Requer confirmação de senha. Limite máximo por operação: R$ 10.000,00. "
			+ "A conta de origem deve estar ativa e com saldo suficiente. "
			+ "Não é permitido transferir para a própria conta.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Transferência realizada com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransacaoResponseDto.class), examples = @ExampleObject(value = """
					{
					  "id": 45,
					  "tipo": "TRANSFERENCIA",
					  "valor": 300.00,
					  "descricao": "divisão de conta",
					  "saldoAnterior": 1150.00,
					  "saldoPosterior": 850.00,
					  "data": "2025-06-10T16:00:00",
					  "contaOrigem": 7,
					  "contaDestino": 9
					}
					"""))),
			@ApiResponse(responseCode = "400", description = "Senha incorreta, valor inválido, acima do limite de R$ 10.000,00, saldo insuficiente ou transferência para a própria conta", content = @Content(mediaType = "application/json", examples = {
					@ExampleObject(name = "Limite excedido", value = """
							{
							  "mensagem": "Tranferência excede o limite permitido!",
							  "tempo": "2025-06-10T16:00:00",
							  "status": "BAD_REQUEST",
							  "codigo": 400
							}
							"""), @ExampleObject(name = "Transferência para si mesmo", value = """
							{
							  "mensagem": "transferência inválida!",
							  "tempo": "2025-06-10T16:00:00",
							  "status": "BAD_REQUEST",
							  "codigo": 400
							}
							"""), @ExampleObject(name = "Saldo insuficiente", value = """
							{
							  "mensagem": "saldo insuficiente!",
							  "tempo": "2025-06-10T16:00:00",
							  "status": "BAD_REQUEST",
							  "codigo": 400
							}
							""") })),
			@ApiResponse(responseCode = "403", description = "Conta de origem bloqueada ou encerrada", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
					{
					  "mensagem": "indisponível, sua conta está BLOQUEADA",
					  "tempo": "2025-06-10T16:00:00",
					  "status": "FORBIDDEN",
					  "codigo": 403
					}
					"""))),
			@ApiResponse(responseCode = "404", description = "Conta destino não encontrada", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
					{
					  "mensagem": "conta destino não encontrada!",
					  "tempo": "2025-06-10T16:00:00",
					  "status": "NOT_FOUND",
					  "codigo": 404
					}
					"""))) })
	@PostMapping("/transferencia")
	public ResponseEntity<TransacaoResponseDto> transferencia(
			@RequestBody @Valid TransferenciaRequestDto transferenciaRequest) {
		return ResponseEntity.ok(servTransacoes.transferencia(transferenciaRequest));
	}

	@Operation(summary = "Consultar extrato", description = "Retorna o extrato paginado da conta do usuário autenticado. "
			+ "Pode ser filtrado por período com os parâmetros `inicio` e `fim` (formato: yyyy-MM-dd). "
			+ "Se nenhum filtro for informado, retorna todas as transações. "
			+ "A data inicial não pode ser posterior à data final.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Extrato retornado com sucesso", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
					{
					  "content": [
					    {
					      "id": 44,
					      "tipo": "PIX",
					      "valor": 150.00,
					      "descricao": "pagamento aluguel",
					      "saldoAnterior": 1300.00,
					      "saldoPosterior": 1150.00,
					      "data": "2025-06-10T15:30:00",
					      "contaOrigem": 7,
					      "contaDestino": 12
					    }
					  ],
					  "pageable": {
					    "pageNumber": 0,
					    "pageSize": 10
					  },
					  "totalElements": 1,
					  "totalPages": 1,
					  "last": true
					}
					"""))),
			@ApiResponse(responseCode = "400", description = "Intervalo de datas inválido — data inicial posterior à data final", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
					{
					  "mensagem": "a data inicial não pode ser posterior à data final!",
					  "tempo": "2025-06-10T16:30:00",
					  "status": "BAD_REQUEST",
					  "codigo": 400
					}
					"""))),
			@ApiResponse(responseCode = "404", description = "Conta do usuário autenticado não encontrada", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
					{
					  "mensagem": "Conta não encontrada!",
					  "tempo": "2025-06-10T16:30:00",
					  "status": "NOT_FOUND",
					  "codigo": 404
					}
					"""))) })
	@GetMapping("/extrato")
	public ResponseEntity<Page<TransacaoResponseDto>> pegaExtrato(@Parameter(hidden = true) Pageable pageable,
			@Parameter(description = "Data inicial do filtro (formato: yyyy-MM-dd)", example = "2025-06-01") @RequestParam(required = false) LocalDate inicio,
			@Parameter(description = "Data final do filtro (formato: yyyy-MM-dd)", example = "2025-06-30") @RequestParam(required = false) LocalDate fim) {
		return ResponseEntity.ok(servTransacoes.extrato(pageable, inicio, fim));
	}

	@Operation(summary = "Consultar relatório mensal", description = "Retorna um resumo financeiro do mês corrente para a conta do usuário autenticado. "
			+ "Inclui: saldo atual, total recebido, total enviado, quantidade de Pix realizados "
			+ "e saldo movimentado líquido (recebido − enviado).")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Relatório gerado com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = RelatorioDto.class), examples = @ExampleObject(value = """
					{
					  "saldoAtual": 850.00,
					  "totalRecebido": 500.00,
					  "totalEnviado": 650.00,
					  "quantidadePix": 3,
					  "movimentadoLiquido": -150.00
					}
					"""))),
			@ApiResponse(responseCode = "404", description = "Conta do usuário autenticado não encontrada", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
					{
					  "mensagem": "Conta não encontrada!",
					  "tempo": "2025-06-10T16:30:00",
					  "status": "NOT_FOUND",
					  "codigo": 404
					}
					"""))) })
	@GetMapping("/relatorio")
	public ResponseEntity<RelatorioDto> pegaExtrato() {
		return ResponseEntity.ok(servTransacoes.relatorioMensal());
	}
}