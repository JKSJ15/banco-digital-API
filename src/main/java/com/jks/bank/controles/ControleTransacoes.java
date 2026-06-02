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

import jakarta.validation.Valid;

@RestController
@RequestMapping("/transacao")
public class ControleTransacoes {
	private final ServicoTransacoes servTransacoes;

	public ControleTransacoes(ServicoTransacoes servTransacoes) {
		super();
		this.servTransacoes = servTransacoes;
	}

	@PostMapping("/deposito")
	public ResponseEntity<TransacaoResponseDto> deposito(@RequestBody @Valid DepositoRequestDto depositoRequest) {
		return ResponseEntity.ok(servTransacoes.deposito(depositoRequest));
	}

	@PostMapping("/saque")
	public ResponseEntity<TransacaoResponseDto> saque(@RequestBody @Valid SaqueRequestDto saqueRequest) {
		return ResponseEntity.ok(servTransacoes.saque(saqueRequest));
	}

	@PostMapping("/pix")
	public ResponseEntity<TransacaoResponseDto> pix(@RequestBody @Valid PixRequestDto pixRequest) {
		return ResponseEntity.ok(servTransacoes.pix(pixRequest));
	}

	@PostMapping("/transferencia")
	public ResponseEntity<TransacaoResponseDto> transferencia(
			@RequestBody @Valid TransferenciaRequestDto transferenciaRequest) {
		return ResponseEntity.ok(servTransacoes.transferencia(transferenciaRequest));
	}

	@GetMapping("/extrato")
	public ResponseEntity<Page<TransacaoResponseDto>> pegaExtrato(Pageable pageable,
			@RequestParam(required = false) LocalDate inicio, @RequestParam(required = false) LocalDate fim) {
		return ResponseEntity.ok(servTransacoes.extrato(pageable, inicio, fim));
	}

	@GetMapping("/relatorio")
	public ResponseEntity<RelatorioDto> pegaExtrato() {
		return ResponseEntity.ok(servTransacoes.relatorioMensal());
	}
}
