package com.jks.bank.exceptions;

public class SenhaInvalidaException extends RuntimeException{
	private static final long serialVersionUID = 1L;

	public SenhaInvalidaException(String message) {
		super(message);
	}
}
