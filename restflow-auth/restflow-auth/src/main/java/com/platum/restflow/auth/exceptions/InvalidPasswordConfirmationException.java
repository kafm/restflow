package com.platum.restflow.auth.exceptions;

import com.platum.restflow.exceptions.RestflowException;

public class InvalidPasswordConfirmationException extends RestflowException {

	public InvalidPasswordConfirmationException() {
		super("New password and it's confirmation does not match.");
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 4001095456806827960L;

}
