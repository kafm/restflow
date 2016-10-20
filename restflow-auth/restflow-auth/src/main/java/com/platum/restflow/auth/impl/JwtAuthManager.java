package com.platum.restflow.auth.impl;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.auth0.jwt.internal.org.apache.commons.lang3.Validate;
import com.platum.restflow.Restflow;
import com.platum.restflow.RestflowEnvironment;
import com.platum.restflow.auth.AuthDetails;
import com.platum.restflow.auth.AuthManager;
import com.platum.restflow.auth.annotation.UseAuthManager;
import com.platum.restflow.auth.exceptions.InvalidCredentialsException;
import com.platum.restflow.auth.exceptions.InvalidPasswordConfirmationException;
import com.platum.restflow.resource.Params;
import com.platum.restflow.resource.Resource;
import com.platum.restflow.resource.ResourceFactory;
import com.platum.restflow.resource.ResourceMethod;
import com.platum.restflow.resource.ResourceObject;
import com.platum.restflow.resource.ResourceService;
import com.platum.restflow.utils.promise.Promise;
import com.platum.restflow.utils.promise.PromiseFactory;

@UseAuthManager
public class JwtAuthManager implements AuthManager
{	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private ResourceService<ResourceObject> service;
	
	private boolean apply;
	
	private JwtResolver jwtResolver;
	
	@Override
	public void config(Restflow restflow, Resource authResource) {
		RestflowEnvironment env = restflow.getEnvironment();
		if(authResource == null) {
			if(logger.isWarnEnabled()) {
				logger.warn("Resource not provided. Auth Manager cannot be used.");
			}
		} else {
			try {
				service = ResourceFactory.getServiceInstance(restflow, authResource);
				jwtResolver = new JwtResolver()
								.load(env.getPropertiesByPrefix(JwtResolver.PROPERTIES_PREFIX));
				apply = true;
			} catch(Throwable e) {
				logger.error("Cannot start Auth Manager due to exception.", e);
			}
		}
	}
	
	@Override
	public boolean authApplies() {
		return apply;
	}

	@Override
	public Promise<ResourceObject> authenticate(ResourceMethod method, AuthDetails authDetails) {
		Validate.notNull(method);
		Validate.notNull(authDetails);
		Promise<ResourceObject> promise = PromiseFactory.getPromiseInstance();
		if(StringUtils.isEmpty(authDetails.getUserName()) || StringUtils.isEmpty(authDetails.getPassword())) {
			promise.reject(new InvalidCredentialsException("No username or password provided."));
		} else {
			service.get(method, new Params().addParam(USERNAME_PARAM, authDetails.getUserName())
					.addParam(PASSWORD_PARAM, authDetails.getPassword()))
			.success(obj -> {
				promise.resolve(obj);
			})
			.error(error -> {
				promise.reject(new InvalidCredentialsException(error));
			});			
		}
		return promise;
	}

	@Override
	public Promise<Void> changePassword(ResourceMethod method, ResourceObject object, AuthDetails authDetails) {
		Validate.notNull(method);
		Validate.notNull(authDetails);
		Promise<Void> promise = PromiseFactory.getPromiseInstance();
		if(StringUtils.isEmpty(authDetails.getPassword())) {
			promise.reject(new InvalidCredentialsException());
		} else if(StringUtils.isEmpty(authDetails.getPassword()) || StringUtils.isEmpty(authDetails.getPasswordConfirm()) 
					|| !authDetails.getNewPassword().equals(authDetails.getPasswordConfirm())) {
			promise.reject(new InvalidPasswordConfirmationException());
		}		
		object.setProperty(PASSWORD_PARAM, authDetails.getNewPassword());
		service.update(method, object)
		.allways(res -> {
			if(res.failed()) {
				promise.reject(res.cause());
			} else {
				promise.resolve();
			}
		});
		return promise;
	}
	
	@Override
	public Promise<String> getAuthorization(ResourceObject object) {
		Promise<String> promise = PromiseFactory.getPromiseInstance();
		promise.resolve(jwtResolver.encode(object));
		return promise;
	}
	
	@Override
	public Promise<ResourceObject> resolveAuthorization(String authCode) {
		Promise<ResourceObject> promise = PromiseFactory.getPromiseInstance();
		promise.resolve(jwtResolver.decode(authCode));
		return promise;		
	}

	@Override
	public Promise<Void> revoke(ResourceMethod method, String token) {
		Promise<Void> promise = PromiseFactory.getPromiseInstance();
		if(method == null) {
			promise.resolve();
		} else {
			service.update(method, jwtResolver.decode(token))
			.allways(res -> {
				if(res.failed()) {
					promise.reject(res.cause());
				} else {
					promise.resolve();
				}
			});
		}
		return promise;
	}
		
}
