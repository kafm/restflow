package com.platum.restflow.auth.impl;

import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

import com.platum.restflow.auth.AuthFilter;
import com.platum.restflow.auth.annotation.UseAuthFilter;
import com.platum.restflow.resource.ResourceMethod;
import com.platum.restflow.resource.ResourceObject;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.ext.web.RoutingContext;

@UseAuthFilter
public class JwtAuthFilter implements AuthFilter {

	private JwtResolver jwtResolver;
	
	@Override
	public void config(Properties properties) {
		jwtResolver = new JwtResolver().load(properties);
	}
	
	@Override
	public void filter(ResourceMethod method, RoutingContext context) {
		String token = context.request().getHeader(AUTH_HEADER);
		if(method.hasRoles()) {
			if(StringUtils.isEmpty(token)) {
				context.response().setStatusCode(
						HttpResponseStatus.UNAUTHORIZED.code()).end();
			} else {
				try {
					ResourceObject object = jwtResolver.decode(token);
					if(!hasPermission(method, getRolesFromObject(object))) {
						context.response().setStatusCode(
								HttpResponseStatus.FORBIDDEN.code()).end();
					}
				} catch(Throwable e) {
					context.response().setStatusCode(
							HttpResponseStatus.UNAUTHORIZED.code());
				}
			}
		} 
		context.next();
	}

}
