package com.platum.restflow.auth.impl;

import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

import com.platum.restflow.auth.AuthFilter;
import com.platum.restflow.auth.annotation.UseAuthFilter;
import com.platum.restflow.exceptions.RestflowForbiddenException;
import com.platum.restflow.exceptions.RestflowUnauthorizedException;
import com.platum.restflow.resource.ResourceMethod;
import com.platum.restflow.resource.ResourceObject;

import io.vertx.ext.web.RoutingContext;

@UseAuthFilter
public class JwtAuthFilter implements AuthFilter {

	private static JwtResolver jwtResolver;
		
	@Override
	public void config(Properties properties) {
		jwtResolver = new JwtResolver().load(properties);
	}
	
	@Override
	public void filter(ResourceMethod method, RoutingContext context) {
		String token = context.request().getHeader(AUTH_HEADER);
		if(method != null && 
				(method.hasRoles() || method.isAuthRequired())) {
			if(StringUtils.isEmpty(token)) {
				throw new RestflowUnauthorizedException();
			} else {
				try {
					ResourceObject object = jwtResolver.decode(token);
					if(!hasPermission(method, getRolesFromObject(object))) {
						throw new RestflowForbiddenException();
					}
					context.put(AUTH_HEADER, object);
				} catch(Throwable e) {
					throw new RestflowUnauthorizedException(e);
				}
			}
		} 
	}

}
