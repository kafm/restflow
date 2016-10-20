package com.platum.restflow.exceptions;

@SuppressWarnings("serial")
public class ResflowObjectConversionException extends RestflowException {

	public ResflowObjectConversionException(String msg) {
		super(msg);
	}

	public ResflowObjectConversionException(String msg, Throwable cause) {
		super(msg, cause);
	}
	
	public ResflowObjectConversionException(Throwable cause) {
		    super(cause);
    }

}
