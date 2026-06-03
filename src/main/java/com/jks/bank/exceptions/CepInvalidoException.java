package com.jks.bank.exceptions;

public class CepInvalidoException extends RuntimeException{
	private static final long serialVersionUID = 1L;

	public CepInvalidoException(String message) {
		super(message);
	}

}
