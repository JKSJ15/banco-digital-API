package com.jks.bank.exceptions;

public class ContaComDinheiroException extends RuntimeException{
	private static final long serialVersionUID = 1L;

	public ContaComDinheiroException(String message) {
		super(message);
	}
}
