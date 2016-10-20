package com.platum.restflow.resource.annotation;

import java.lang.reflect.Method;

import com.platum.restflow.exceptions.RestflowException;
import com.platum.restflow.resource.ObjectContext;
import com.platum.restflow.utils.promise.Promise;
import com.platum.restflow.utils.promise.PromiseFactory;

public class HookInterceptor {

	private Method method;
	
	private boolean transactional;
	
	public HookInterceptor(Method method, boolean transactional) {
		this.method = method;
		this.transactional = transactional;
	}
	
	public boolean transactional() {
		return transactional;
	}
	
	public Method method() {
		return method;
	}

	@SuppressWarnings("unchecked")
	public <T> Promise<T> invoke(ObjectContext<T> objectContext) {
		Promise<T> promise = PromiseFactory.getPromiseInstance();
		try {
			Object hookInstance = method.getDeclaringClass().newInstance();
			return (Promise<T>) method.invoke(hookInstance, objectContext);				
		} catch(Throwable e) {
			promise.reject(
					new RestflowException("Error invoking hook method ["+method.getName()+"]"
					, e));
		}		
		return promise;
	}

}
