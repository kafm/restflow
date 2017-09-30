package com.platum.restflow.exceptions;

import java.util.List;

@SuppressWarnings("serial")
public class RestflowListOfException extends RestflowException {

	private List<Throwable> errors;
	
	public RestflowListOfException(List<Throwable> errors) {
		this("Errors occurred", errors);
	}
	
	public RestflowListOfException(String message, List<Throwable> errors) {
		super(message);
		this.errors = errors;
	}

	public List<Throwable> getErrors() {
		return errors;
	}

}
