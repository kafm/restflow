package com.platum.restflow.exceptions;

public class RestflowException extends RuntimeException {

	private static final long serialVersionUID = -6936445622623967685L;
	
	private Object[] params;
	
	public RestflowException(String msg) {
		super(msg);
	}

	public RestflowException(String msg, Throwable cause) {
		super(msg, cause);
	}
	
	public RestflowException(Throwable cause) {
		    super(cause);
    }

	public Object[] params() {
		return params;
	}

	public void params(Object... params) {
		this.params = params;
	}
	
	
}
