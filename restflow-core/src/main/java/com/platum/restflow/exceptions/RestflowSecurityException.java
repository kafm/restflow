package com.platum.restflow.exceptions;

@SuppressWarnings("serial")
public class RestflowSecurityException extends RestflowException {

	public RestflowSecurityException(String msg) {
		super(msg);
	}

	public RestflowSecurityException(String msg, Throwable cause) {
		super(msg, cause);
	}
	
	public RestflowSecurityException(Throwable cause) {
		    super(cause);
    }
}
