package com.platum.restflow.components;

import com.platum.restflow.resource.ResourceMethod;

import io.vertx.ext.web.RoutingContext;

public interface Filter {
	
	public void filter(ResourceMethod method, RoutingContext context);
	
}
