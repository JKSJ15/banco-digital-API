package com.jks.bank.exceptions;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;

public class CorpoException {
	private String mensagem;
	private LocalDateTime tempo;
	private HttpStatus erro;
	private int valor;

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
