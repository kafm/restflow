package com.platum.restflow.utils.promise;

public interface Promise<T> {
	
	default Promise<T> then(PromiseHandler<T> successHandler) {
		then(successHandler, null);
		return this;
	}
	
	default Promise<T> then(PromiseHandler<T> successHandler, PromiseHandler<Throwable> errorHandler) {
		then(successHandler, errorHandler, null);
		return this;		
	}
	
	default Promise<T> then(PromiseHandler<T> successHandler, 
			PromiseHandler<Throwable> errorHandler, PromiseHandler<PromiseResult<T>> allwaysHandler) {
		success(successHandler);
		error(errorHandler);
		allways(allwaysHandler);
		return this;			
	}
	
	default Promise<T> wrap(Promise<T> rootPromise) {
		rootPromise.allways(res -> {
			if(res.succeeded()) {
				resolve(res.result());
			} else {
				reject(res.cause());
			}
		});
		return this;
	}
	
	Promise<T> success(PromiseHandler<T> successHandler);
	
	Promise<T> error(PromiseHandler<Throwable> errorHandler);
	
	Promise<T> allways(PromiseHandler<PromiseResult<T>> allwaysHandler);
	
	void resolve(T result);
	
	void resolve();
	
	void reject(Throwable e);
	
	void reject(String msg);
	
}
