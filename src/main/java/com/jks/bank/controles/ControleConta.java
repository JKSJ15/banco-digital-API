package com.jks.bank.controles;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jks.bank.dto.ContaResponseDto;
import com.jks.bank.servicos.ServicoConta;

@RestController
@RequestMapping("/conta")
public class ControleConta {
	private final ServicoConta servConta;

	public ControleConta(ServicoConta servConta) {
		super();
		this.servConta = servConta;
	}

	@GetMapping()
	public ResponseEntity<ContaResponseDto> contaUsuario() {
		return ResponseEntity.ok(servConta.contaUsuario());
	}

	@GetMapping("/bloquear")
	public ResponseEntity<Void> bloquearConta() {
		servConta.bloquearConta();
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/desbloquear")
	public ResponseEntity<Void> desbloquearConta() {
		servConta.desbloquearConta();
		return ResponseEntity.noContent().build();
	}
}
