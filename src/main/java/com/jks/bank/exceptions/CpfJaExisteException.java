package com.jks.bank.exceptions;

public class CpfJaExisteException extends RuntimeException{
	private static final long serialVersionUID = 1L;

	public CpfJaExisteException(String message) {
		super(message);
	}
}
