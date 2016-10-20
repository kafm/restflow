package com.platum.restflow.auth.exceptions;

import com.platum.restflow.exceptions.RestflowException;

public class InvalidCredentialsException extends RestflowException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6234263124563611464L;
	
	public InvalidCredentialsException() {
		this("Wrong username or password.");
	}
	
	public InvalidCredentialsException(String msg) {
		super(msg);
	}
	
	public InvalidCredentialsException(Throwable e) {
		super("Wrong username or password.", e);
	} 
	
}
