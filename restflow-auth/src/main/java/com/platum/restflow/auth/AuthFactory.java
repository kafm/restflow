package com.platum.restflow.auth;

import java.lang.annotation.Annotation;
import java.util.List;

import com.platum.restflow.auth.annotation.UseAuthFilter;
import com.platum.restflow.auth.annotation.UseAuthManager;
import com.platum.restflow.components.ComponentFactory;
import com.platum.restflow.exceptions.RestflowException;

public class AuthFactory {

	private static final List<AuthManager> authManagers = ComponentFactory.getComponent(AuthManager.class);
	
	private static final List<AuthFilter> authFilters = ComponentFactory.getComponent(AuthFilter.class);
	
	
	public static AuthManager getAuthManager() {
		if(authManagers == null || authManagers.isEmpty()) {
			throw new RestflowException("No auth managers services found.");
		}
		return authManagers.stream()
						  .filter(m -> isPrincipalManager(m))
						  .findAny()
						  .orElse(authManagers.get(0));
	}
	
	public static AuthFilter getAuthFilter() {
		if(authFilters == null || authFilters.isEmpty()) {
			throw new RestflowException("No auth filter services found.");
		}
		return authFilters.stream()
						  .filter(m -> isPrincipalFilter(m))
						  .findAny()
						  .orElse(authFilters.get(0));
	}
	
	private static <T extends Annotation> boolean isPrincipalManager(Object object) {
		if(object != null) {
			UseAuthManager an = object.getClass().getAnnotation(UseAuthManager.class);
			return an != null && an.primary();
		}
		return false;
	}
	
	private static <T extends Annotation> boolean isPrincipalFilter(Object object) {
		if(object != null) {
			UseAuthFilter an = object.getClass().getAnnotation(UseAuthFilter.class);
			return an != null && an.primary();
		}
		return false;
	}

}
