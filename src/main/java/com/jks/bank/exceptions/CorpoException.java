package com.jks.bank.exceptions;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;

import io.swagger.v3.oas.annotations.media.Schema;


@Schema(description = "Corpo padrão de erro retornado pela API")
public class CorpoException {
	@Schema(example = "senha inválida!")
	String mensagem;
	@Schema(example = "2026-06-10T10:30:15")
	LocalDateTime tempo;
	@Schema(example = "BAD_REQUEST")
	HttpStatus erro;
	@Schema(example = "400")
	int valor;

	public CorpoException(String mensagem, LocalDateTime tempo, HttpStatus erro, int valor) {
		super();
		this.mensagem = mensagem;
		this.tempo = tempo;
		this.erro = erro;
		this.valor = valor;
	}

	public String getMensagem() {
		return mensagem;
	}

	public void setMensagem(String mensagem) {
		this.mensagem = mensagem;
	}

	public LocalDateTime getTempo() {
		return tempo;
	}

	public void setTempo(LocalDateTime tempo) {
		this.tempo = tempo;
	}

	public HttpStatus getErro() {
		return erro;
	}

	public void setErro(HttpStatus erro) {
		this.erro = erro;
	}

	public int getValor() {
		return valor;
	}

	public void setValor(int valor) {
		this.valor = valor;
	}
}
