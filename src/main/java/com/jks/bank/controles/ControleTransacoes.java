package com.jks.bank.controles;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jks.bank.dto.DepositoRequestDto;
import com.jks.bank.dto.SaqueRequestDto;
import com.jks.bank.dto.TransacaoResponseDto;
import com.jks.bank.servicos.ServicoTransacoes;

@RestController
@RequestMapping("/transacao")
public class ControleTransacoes {
	private final ServicoTransacoes servTransacoes;
	
	public ControleTransacoes(ServicoTransacoes servTransacoes) {
		super();
		this.servTransacoes = servTransacoes;
	}
	
	@PostMapping("/deposito")
	public ResponseEntity<TransacaoResponseDto> deposito(@RequestBody DepositoRequestDto depositoRequest){
		return ResponseEntity.ok(servTransacoes.deposito(depositoRequest));
	}
	@PostMapping("/saque")
	public ResponseEntity<TransacaoResponseDto> saque(@RequestBody SaqueRequestDto saqueRequest){
		return ResponseEntity.ok(servTransacoes.saque(saqueRequest));
	}
	@PostMapping("/pix")
	public ResponseEntity<TransacaoResponseDto> pix(){
		return ResponseEntity.ok(servTransacoes.pix());
	}
	@PostMapping("/transferencia")
	public ResponseEntity<TransacaoResponseDto> transferencia(){
		return ResponseEntity.ok(servTransacoes.transferencia());
	}
	@GetMapping("/extrato")
	public ResponseEntity<Page<TransacaoResponseDto>> pegaExtrato(Pageable pageable) {
		return ResponseEntity.ok(servTransacoes.extrato(pageable));
	}
}
