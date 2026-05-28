package com.jks.bank.exceptions;

public class ContaEncerradaException extends RuntimeException{
	private static final long serialVersionUID = 1L;

	public ContaEncerradaException(String message) {
		super(message);
	}
}
