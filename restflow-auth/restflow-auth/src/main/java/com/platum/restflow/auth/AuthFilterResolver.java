package com.platum.restflow.auth;

import com.platum.restflow.components.Filter;
import com.platum.restflow.resource.ResourceMethod;

import io.vertx.ext.web.RoutingContext;


public class AuthFilterResolver implements Filter {

	private AuthFilter filter;
	
	public AuthFilterResolver() {
		filter = AuthFactory.getAuthFilter();
	}
	
	@Override
	public void filter(ResourceMethod method, RoutingContext context) {
		if(filter == null) {
			context.next();
		} else {
			filter.filter(method, context);
		}
	}

}
