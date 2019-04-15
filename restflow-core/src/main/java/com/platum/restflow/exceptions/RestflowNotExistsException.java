package com.platum.restflow.exceptions;

@SuppressWarnings("serial")
public class RestflowNotExistsException extends RestflowException {

	public RestflowNotExistsException(String msg) {
		super(msg);
	}

	public RestflowNotExistsException(String msg, Throwable cause) {
		super(msg, cause);
	}
	
	public RestflowNotExistsException(Throwable cause) {
		    super(cause);
    }

}
