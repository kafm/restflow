package com.platum.restflow.utils.promise;

public class PromiseResult<T> {
	
	private Throwable cause;
	
	private T result;

	public PromiseResult(Throwable cause) {
		this.cause = cause;
	}

	public PromiseResult(T result) {
		this.result = result;
	}
	
	public Throwable cause() {
		return cause;
	}

	public T result() {
		return result;
	}

	public boolean succeeded() {
		return result != null;
	}
	
	public boolean failed() {
		return cause != null;
	}
	
}
