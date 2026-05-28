package com.jks.bank.exceptions;

public class TransferenciaInvalidaException extends RuntimeException{
	private static final long serialVersionUID = 1L;

	public TransferenciaInvalidaException(String message) {
		super(message);
	}
}
