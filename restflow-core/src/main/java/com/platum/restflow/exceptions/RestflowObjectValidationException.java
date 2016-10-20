package com.platum.restflow.exceptions;

import java.util.List;

@SuppressWarnings("serial")
public class RestflowObjectValidationException extends RestflowValidationException {

	private List<RestflowValidationException> validationErrors;
	
	public RestflowObjectValidationException(List<RestflowValidationException> validationErrors) {
		this("Object validation errors occurred", validationErrors);
	}
	
	public RestflowObjectValidationException(String message, List<RestflowValidationException> validationErrors) {
		super(message);
		this.validationErrors = validationErrors;
	}

	public List<RestflowValidationException> getValidationErrors() {
		return validationErrors;
	}

}
