package com.jks.bank.exceptions;

public class ValorInvalidoException extends RuntimeException{
	private static final long serialVersionUID = 1L;

	public ValorInvalidoException(String message) {
		super(message);
	}
}
