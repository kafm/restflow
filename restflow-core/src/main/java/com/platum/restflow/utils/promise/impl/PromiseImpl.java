package com.platum.restflow.utils.promise.impl;

import com.platum.restflow.utils.promise.Promise;
import com.platum.restflow.utils.promise.PromiseHandler;
import com.platum.restflow.utils.promise.PromiseResult;

import io.vertx.core.Future;

public class PromiseImpl<T>  implements Promise<T>{
	
	private Future<T> future;
	
	private PromiseResult<T> promiseResult;
	
	private PromiseHandler<T> successHandler;
	
	private PromiseHandler<Throwable> errorHandler;
	
	private PromiseHandler<PromiseResult<T>> allwaysHandler;
		
	public PromiseImpl(Future<T> future) {
		this.future = future;
		setDefaultHandler();
	}
	
	@Override
	public Promise<T> success(PromiseHandler<T> successHandler) {
		this.successHandler = successHandler;
		if(future.isComplete() && future.succeeded()) {
			processSuccess(future.result());
		}
		return this;		
	}
	
	@Override
	public Promise<T> error(PromiseHandler<Throwable> errorHandler) {
		this.errorHandler = errorHandler;
		if(future.isComplete() && future.failed()) {
			processFailure(future.cause());
		}
		return this;
	}
	
	@Override
	public Promise<T> allways(PromiseHandler<PromiseResult<T>> allwaysHandler) {
		this.allwaysHandler = allwaysHandler;
		if(future.isComplete()) {
			processAllways();
		}
		return this;
	} 

	@Override
	public void resolve() {
		future.complete(null);
	}
	
	@Override
	public void resolve(T result) {
		future.complete(result);
	}
	
	@Override
	public void reject(Throwable e) {
		future.fail(e);
	}
	
	@Override
	public void reject(String msg) {
		future.fail(msg);
	}
	
	private void setDefaultHandler() {
		future.setHandler(result -> {
			if(result.succeeded()) {
				processSuccess(result.result());
			} else {
				processFailure(result.cause());
			}
			processAllways();			
		});
	}
	
	private void processSuccess(T result) {
		promiseResult = new PromiseResult<T>(result);
		if(successHandler != null) {
			try {
				successHandler.handle(result);
			} catch(Throwable e) {
				processFailure(e);
			}
		}
	}
	
	private void  processFailure(Throwable e) {
		promiseResult = new PromiseResult<T>(e);
		if(errorHandler != null) {
			errorHandler.handle(e);
		}
	}
	
	private void processAllways() {
		if(allwaysHandler != null) {
			allwaysHandler.handle(promiseResult);
		}
	}
}
