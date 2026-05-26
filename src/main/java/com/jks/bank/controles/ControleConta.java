package com.jks.bank.controles;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jks.bank.servicos.ServicoConta;

@RestController
@RequestMapping("/conta")
public class ControleConta {
	private final ServicoConta servConta;

	public ControleConta(ServicoConta servConta) {
		super();
		this.servConta = servConta;
	}

	@GetMapping("/extrato")
	public ResponseEntity<Void> pegaExtrato() {
		return ResponseEntity.ok().build();
	}

	@GetMapping("/bloquear")
	public ResponseEntity<Void> bloquearConta() {
		return ResponseEntity.noContent().build();
	}
}
