package com.platum.restflow.utils.promise;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
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
			Map<Promise<T>, PromiseResult<T>> resultsMap = new HashMap<>();
			promises.stream().forEach(p -> {
				p.allways(h -> {
					resultsMap.put(p, h);
					if(resultsMap.size() == expectedRes) {
						promise.resolve(getOrganizePromiseResults(promises, resultsMap));
					}
				});
			});			
		} catch(Throwable e) {
			promise.reject(e);
		}
		return promise;
	}
	
	private static <T> List<PromiseResult<T>> getOrganizePromiseResults(List<Promise<T>> promises, Map<Promise<T>, PromiseResult<T>> resultsMap) {
		List<PromiseResult<T>> results = new ArrayList<>();
		if(!CollectionUtils.isEmpty(promises) && resultsMap != null) {
			promises.forEach(p -> {
				results.add(resultsMap.get(p));
			});
		}
		return results;
	}
}
