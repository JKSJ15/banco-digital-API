package com.jks.bank.exceptions.gerenciadorglobalexceptions;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.jks.bank.exceptions.CorpoException;
import com.jks.bank.exceptions.UsuarioNaoEncontradoException;

@RestControllerAdvice
public class GerenciadorGlobalExceptions {

	@ExceptionHandler(value = UsuarioNaoEncontradoException.class)
	public ResponseEntity<CorpoException> usaUsuarioNaoEncontradoException(UsuarioNaoEncontradoException e) {
		CorpoException corpo = new CorpoException(e.getMessage(), LocalDateTime.now(), HttpStatus.NOT_FOUND,
				HttpStatus.NOT_FOUND.value());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(corpo);
	}
}
