package com.platum.restflow.utils.promise;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.Validate;

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
	
	public static <T> Promise<List<PromiseResult<T>>> whenAll(List<Promise<T>> promises) {
		Promise<List<PromiseResult<T>>> promise = getPromiseInstance();
		try {
			Validate.notEmpty(promises, "Promise list is empty.");
			int expectedRes = promises.size();
			List<PromiseResult<T>> results = new ArrayList<>();
			promises.stream().forEach(p -> {
				p.allways(h -> {
					results.add(h);
					if(results.size() == expectedRes) {
						promise.resolve(results);
					}
				});
			});			
		} catch(Throwable e) {
			promise.reject(e);
		}
		return promise;
	}
}
