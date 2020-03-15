package com.platum.restflow.components;

import com.platum.restflow.resource.ResourceMethod;
import com.platum.restflow.utils.promise.Promise;

import io.vertx.ext.web.RoutingContext;

public interface Filter {
	
	public Promise<Void> filter(ResourceMethod method, RoutingContext context);
	
}
