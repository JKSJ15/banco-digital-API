package com.jks.bank.exceptions;

public class RefreshTokenInvalidoException extends RuntimeException{
	private static final long serialVersionUID = 1L;

	public RefreshTokenInvalidoException(String message) {
		super(message);
	}

}
