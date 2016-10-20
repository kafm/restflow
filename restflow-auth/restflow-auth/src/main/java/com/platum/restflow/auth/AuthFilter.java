package com.platum.restflow.auth;

import java.util.Collection;
import java.util.Properties;
import java.util.stream.Stream;

import com.auth0.jwt.internal.org.apache.commons.lang3.Validate;
import com.platum.restflow.components.Filter;
import com.platum.restflow.exceptions.RestflowException;
import com.platum.restflow.resource.ResourceMethod;
import com.platum.restflow.resource.ResourceObject;

public interface AuthFilter extends Filter {
	
	static final String AUTH_HEADER = "Authorization";
	
	static final String BEARER_AUTH_HEADER = "Bearer";
	
	static final String ROLES_ATTR = "roles";

	void config(Properties properties);
	
	default String[] getRolesFromObject(ResourceObject object) {
		if(object != null) {
			Object roles = object.get(ROLES_ATTR);
			if(roles instanceof String) {
				return ((String) roles).split(",");
			} else if(roles instanceof String[]) {
				return (String[]) roles;
			} else if(roles instanceof Collection<?>) {
				return ((Collection<?>) roles).stream()
					    .map(role -> role != null ? role.toString() : null)
					    .toArray(size -> new String[size]);
			} else {
				throw new RestflowException("Invalid roles object ["+object.getClass()+"].");
			}
		}
		return new String[0];
	}
	
	default boolean hasPermission(ResourceMethod method, String[] roles) {
		Validate.notNull(method);
		if(method.hasRoles()) {
			if(roles == null || roles.length == 0) {
				return false;
			}
			return Stream.of(roles)
					.anyMatch(role -> 
						roleExists(method.getRolesAsArray(), role));
		}
		return true;
	}
	
	default boolean roleExists(String[] roles, String role) {
		if(roles != null && roles.length > 0) {
			return Stream.of(roles)
					.anyMatch(aRole -> aRole.equalsIgnoreCase(role));
		}
		return false;
	}
	
}
