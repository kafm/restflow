package com.platum.restflow.exceptions;

public class RestflowException extends RuntimeException {

	private static final long serialVersionUID = -6936445622623967685L;
	
	public RestflowException(String msg) {
		super(msg);
	}

	public RestflowException(String msg, Throwable cause) {
		super(msg, cause);
	}
	
	public RestflowException(Throwable cause) {
		    super(cause);
    }
}
