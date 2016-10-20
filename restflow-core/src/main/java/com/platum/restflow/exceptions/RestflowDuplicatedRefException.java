package com.platum.restflow.exceptions;

@SuppressWarnings("serial")
public class RestflowDuplicatedRefException extends RestflowException {
	
	public RestflowDuplicatedRefException(String msg) {
		super(msg);
	}

	public RestflowDuplicatedRefException(String msg, Throwable cause) {
		super(msg, cause);
	}
	
	public RestflowDuplicatedRefException(Throwable cause) {
		    super(cause);
    }

}
