package com.platum.restflow.exceptions;

@SuppressWarnings("serial")
public class RestflowUnauthorizedException extends RestflowSecurityException {

	public RestflowUnauthorizedException() {
		super("Authentication required");
	}
	
	public RestflowUnauthorizedException(String msg) {
		super(msg);
	}

	public RestflowUnauthorizedException(String msg, Throwable cause) {
		super(msg, cause);
	}
	
	public RestflowUnauthorizedException(Throwable cause) {
		    super(cause);
    }
}
