package com.jks.bank.exceptions.gerenciadorglobalexceptions;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.jks.bank.exceptions.ContaBloqueadaException;
import com.jks.bank.exceptions.ContaComDinheiroException;
import com.jks.bank.exceptions.ContaEncerradaException;
import com.jks.bank.exceptions.ContaNaoEncontradaException;
import com.jks.bank.exceptions.CorpoException;
import com.jks.bank.exceptions.IdadeNaoPermitidaException;
import com.jks.bank.exceptions.RefreshTokenInvalidoException;
import com.jks.bank.exceptions.SaldoInsuficienteException;
import com.jks.bank.exceptions.SenhaInvalidaException;
import com.jks.bank.exceptions.TransferenciaInvalidaException;
import com.jks.bank.exceptions.UsuarioJaExisteException;
import com.jks.bank.exceptions.UsuarioNaoEncontradoException;
import com.jks.bank.exceptions.ValorInvalidoException;

@RestControllerAdvice
public class GerenciadorGlobalExceptions {

	@ExceptionHandler(value = UsuarioNaoEncontradoException.class)
	public ResponseEntity<CorpoException> usaUsuarioNaoEncontradoException(UsuarioNaoEncontradoException e) {
		CorpoException corpo = new CorpoException(e.getMessage(), LocalDateTime.now(), HttpStatus.NOT_FOUND,
				HttpStatus.NOT_FOUND.value());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(corpo);
	}

	@ExceptionHandler(value = ContaNaoEncontradaException.class)
	public ResponseEntity<CorpoException> usaContaNaoEncontradaException(ContaNaoEncontradaException e) {
		CorpoException corpo = new CorpoException(e.getMessage(), LocalDateTime.now(), HttpStatus.NOT_FOUND,
				HttpStatus.NOT_FOUND.value());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(corpo);
	}

	@ExceptionHandler(value = ContaBloqueadaException.class)
	public ResponseEntity<CorpoException> usaContaBloqueadaException(ContaBloqueadaException e) {
		CorpoException corpo = new CorpoException(e.getMessage(), LocalDateTime.now(), HttpStatus.FORBIDDEN,
				HttpStatus.FORBIDDEN.value());
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(corpo);
	}

	@ExceptionHandler(value = ContaEncerradaException.class)
	public ResponseEntity<CorpoException> usaContaEncerradaException(ContaEncerradaException e) {
		CorpoException corpo = new CorpoException(e.getMessage(), LocalDateTime.now(), HttpStatus.FORBIDDEN,
				HttpStatus.FORBIDDEN.value());
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(corpo);
	}

	@ExceptionHandler(value = SaldoInsuficienteException.class)
	public ResponseEntity<CorpoException> usaSaldoInsuficienteException(SaldoInsuficienteException e) {
		CorpoException corpo = new CorpoException(e.getMessage(), LocalDateTime.now(), HttpStatus.BAD_REQUEST,
				HttpStatus.BAD_REQUEST.value());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(corpo);
	}

	@ExceptionHandler(value = ValorInvalidoException.class)
	public ResponseEntity<CorpoException> usaValorInvalidoException(ValorInvalidoException e) {
		CorpoException corpo = new CorpoException(e.getMessage(), LocalDateTime.now(), HttpStatus.BAD_REQUEST,
				HttpStatus.BAD_REQUEST.value());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(corpo);
	}

	@ExceptionHandler(value = TransferenciaInvalidaException.class)
	public ResponseEntity<CorpoException> usaTransferenciaInvalidaException(TransferenciaInvalidaException e) {
		CorpoException corpo = new CorpoException(e.getMessage(), LocalDateTime.now(), HttpStatus.BAD_REQUEST,
				HttpStatus.BAD_REQUEST.value());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(corpo);
	}

	@ExceptionHandler(value = ContaComDinheiroException.class)
	public ResponseEntity<CorpoException> usaContaComDinheiroException(ContaComDinheiroException e) {
		CorpoException corpo = new CorpoException(e.getMessage(), LocalDateTime.now(), HttpStatus.BAD_REQUEST,
				HttpStatus.BAD_REQUEST.value());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(corpo);
	}

	@ExceptionHandler(value = RefreshTokenInvalidoException.class)
	public ResponseEntity<CorpoException> usaRefreshTokenInvalidoException(RefreshTokenInvalidoException e) {
		CorpoException corpo = new CorpoException(e.getMessage(), LocalDateTime.now(), HttpStatus.FORBIDDEN,
				HttpStatus.FORBIDDEN.value());
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(corpo);
	}

	@ExceptionHandler(value = IdadeNaoPermitidaException.class)
	public ResponseEntity<CorpoException> usaIdadeNaoPermitidaException(IdadeNaoPermitidaException e) {
		CorpoException corpo = new CorpoException(e.getMessage(), LocalDateTime.now(), HttpStatus.BAD_REQUEST,
				HttpStatus.BAD_REQUEST.value());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(corpo);
	}

	@ExceptionHandler(value = UsuarioJaExisteException.class)
	public ResponseEntity<CorpoException> usaUsuarioJaExisteException(UsuarioJaExisteException e) {
		CorpoException corpo = new CorpoException(e.getMessage(), LocalDateTime.now(), HttpStatus.BAD_REQUEST,
				HttpStatus.BAD_REQUEST.value());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(corpo);
	}

	@ExceptionHandler(value = SenhaInvalidaException.class)
	public ResponseEntity<CorpoException> usaSenhaInvalidaException(SenhaInvalidaException e) {
		CorpoException corpo = new CorpoException(e.getMessage(), LocalDateTime.now(), HttpStatus.BAD_REQUEST,
				HttpStatus.BAD_REQUEST.value());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(corpo);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<CorpoException> usaMethodArgumentNotValidException(MethodArgumentNotValidException e) {
		CorpoException corpo = new CorpoException(e.getMessage(), LocalDateTime.now(), HttpStatus.BAD_REQUEST,
				HttpStatus.BAD_REQUEST.value());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(corpo);
	}
}
