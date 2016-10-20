package com.platum.restflow.exceptions;

@SuppressWarnings("serial")
public class RestflowNotAllowedException extends RestflowSecurityException {

	public RestflowNotAllowedException(String msg) {
		super(msg);
	}

	public RestflowNotAllowedException(String msg, Throwable cause) {
		super(msg, cause);
	}
	
	public RestflowNotAllowedException(Throwable cause) {
		    super(cause);
    }
}
