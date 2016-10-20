package com.platum.restflow.exceptions;

@SuppressWarnings("serial")
public class RestflowFieldConversionValidationException extends RestflowValidationException {
	
	public RestflowFieldConversionValidationException(String message) {
		super(message);
	}

	public RestflowFieldConversionValidationException(String message, Throwable e) {
		super(message, e);
	}
}
