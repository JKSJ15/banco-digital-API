package com.jks.bank.exceptions;

public class TelefoneJaExisteException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public TelefoneJaExisteException(String message) {
		super(message);
	}
}
