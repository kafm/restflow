package com.platum.restflow.exceptions;

@SuppressWarnings("serial")
public class RestflowQueryException extends RestflowException {

	public RestflowQueryException(String msg) {
		super(msg);
	}
	
	public RestflowQueryException(String msg, Throwable cause) {
		super(msg, cause);
	}
	
	public RestflowQueryException(Throwable cause) {
		    super(cause);
    }

}
