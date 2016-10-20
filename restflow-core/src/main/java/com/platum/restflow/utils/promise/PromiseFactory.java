package com.platum.restflow.utils.promise;

import com.platum.restflow.utils.promise.impl.PromiseImpl;

import io.vertx.core.Future;

public class PromiseFactory {

	public static <T> Promise<T> getPromiseInstance() {
		Future<T> future = Future.future();
		return getPromiseInstance(future);
	}
	
	public static <T> Promise<T> getPromiseInstance(Future<T> future) {
		return new PromiseImpl<T>(future);
	}
}
