package com.jks.bank.controles;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
	public ResponseEntity<Void> deposito(){
		return ResponseEntity.noContent().build();
	}
	@PostMapping("/saque")
	public ResponseEntity<Void> saque(){
		return ResponseEntity.noContent().build();
	}
	@PostMapping("/pix")
	public ResponseEntity<Void> pix(){
		return ResponseEntity.noContent().build();
	}
	@PostMapping("/transferencia")
	public ResponseEntity<Void> transferencia(){
		return ResponseEntity.noContent().build();
	}
}
