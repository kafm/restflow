package com.platum.restflow.exceptions;

@SuppressWarnings("serial")
public class ResflowNotExistsException extends RestflowException {

	public ResflowNotExistsException(String msg) {
		super(msg);
	}

	public ResflowNotExistsException(String msg, Throwable cause) {
		super(msg, cause);
	}
	
	public ResflowNotExistsException(Throwable cause) {
		    super(cause);
    }

}
