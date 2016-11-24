package com.platum.restflow.exceptions;

@SuppressWarnings("serial")
public class InvalidCreditCardValidationException extends InvalidValueValidationException {

	public InvalidCreditCardValidationException(String message) {
		super(message);
	}

}
