package com.platum.restflow.exceptions;

@SuppressWarnings("serial")
public class RestflowForbiddenException extends RestflowSecurityException {


	public RestflowForbiddenException() {
		super("Forbidden access");
	}

	public RestflowForbiddenException(String msg) {
		super(msg);
	}

	public RestflowForbiddenException(String msg, Throwable cause) {
		super(msg, cause);
	}
	
	public RestflowForbiddenException(Throwable cause) {
		    super(cause);
    }
}
