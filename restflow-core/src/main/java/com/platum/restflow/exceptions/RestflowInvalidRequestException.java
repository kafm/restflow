package com.platum.restflow.exceptions;

public class RestflowInvalidRequestException extends RestflowException {

	private static final long serialVersionUID = -6936445622623967685L;
	
	public RestflowInvalidRequestException(String msg) {
		super(msg);
	}

	public RestflowInvalidRequestException(String msg, Throwable cause) {
		super(msg, cause);
	}
	
	public RestflowInvalidRequestException(Throwable cause) {
		    super(cause);
    }
}
