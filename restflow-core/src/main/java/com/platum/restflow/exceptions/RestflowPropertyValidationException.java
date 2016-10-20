package com.platum.restflow.exceptions;

@SuppressWarnings("serial")
public class RestflowPropertyValidationException extends RestflowException {
	
	private String contextProperty;
	
	public RestflowPropertyValidationException(String message) {
		super(message);
	}

	public String getContextProperty() {
		return contextProperty;
	}

	public RestflowPropertyValidationException setContextProperty(String contextProperty) {
		this.contextProperty = contextProperty;
		return this;
	}
	
}
