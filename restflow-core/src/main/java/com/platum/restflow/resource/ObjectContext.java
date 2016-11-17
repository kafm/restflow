package com.platum.restflow.resource;

import com.platum.restflow.utils.promise.PromiseHandler;
import com.platum.restflow.utils.promise.PromiseResult;

import io.vertx.core.Handler;

public interface ObjectContext<T> {

	void save(PromiseHandler<PromiseResult<T>> handler);
	
	void save(ResourceMethod method, PromiseHandler<PromiseResult<T>> handler);
	
	void destroy(PromiseHandler<PromiseResult<Void>> handler);
	
	void get(Handler<T> handler);

	ResourceService<T> service();
	
	ResourceService<T> serviceFor(String resourceName);
	
	T object();
	
	Params params();
		
	Resource resource();
	
	boolean isNew();
	
	ObjectContext<T> isNew(boolean isNew);
	
	boolean ignore();
	
	ObjectContext<T> ignore(boolean ignore);
	
	boolean partial();
	
}
