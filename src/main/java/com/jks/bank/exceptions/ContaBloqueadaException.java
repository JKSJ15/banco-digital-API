package com.jks.bank.exceptions;

public class ContaBloqueadaException extends RuntimeException{
	private static final long serialVersionUID = 1L;

	public ContaBloqueadaException(String message) {
		super(message);
	}

}
