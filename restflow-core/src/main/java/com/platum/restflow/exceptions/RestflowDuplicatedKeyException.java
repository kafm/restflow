package com.platum.restflow.exceptions;

@SuppressWarnings("serial")
public class RestflowDuplicatedKeyException extends RestflowException {
	
	public RestflowDuplicatedKeyException(String msg) {
		super(msg);
	}

	public RestflowDuplicatedKeyException(String msg, Throwable cause) {
		super(msg, cause);
	}
	
	public RestflowDuplicatedKeyException(Throwable cause) {
		    super(cause);
    }

}
