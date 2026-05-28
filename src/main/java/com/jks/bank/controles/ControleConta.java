package com.jks.bank.controles;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jks.bank.dto.ContaResponseDto;
import com.jks.bank.dto.SenhaDto;
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

	@PostMapping("/bloquear")
	public ResponseEntity<Void> bloquearConta(SenhaDto senha) {
		servConta.bloquearConta(senha);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/desbloquear")
	public ResponseEntity<Void> desbloquearConta(SenhaDto senha) {
		servConta.desbloquearConta(senha);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/encerrar")
	public ResponseEntity<Void> encerrarConta(SenhaDto senha) {
		servConta.encerrarConta(senha);
		return ResponseEntity.noContent().build();
	}
}
