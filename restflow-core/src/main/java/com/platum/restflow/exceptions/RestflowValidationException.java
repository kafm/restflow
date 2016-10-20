package com.platum.restflow.exceptions;

@SuppressWarnings("serial")
public class RestflowValidationException extends RestflowException {
	
	public RestflowValidationException(String message) {
		super(message);
	}
	
	public RestflowValidationException(String message, Throwable e) {
		super(message, e);
	}

}
