package com.platum.restflow.resource.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.platum.restflow.exceptions.ResflowNotExistsException;
import com.platum.restflow.exceptions.RestflowException;
import com.platum.restflow.exceptions.RestflowForbiddenException;
import com.platum.restflow.exceptions.RestflowNotAllowedException;
import com.platum.restflow.exceptions.RestflowObjectValidationException;
import com.platum.restflow.exceptions.RestflowUnauthorizedException;
import com.platum.restflow.exceptions.RestflowValidationException;
import com.platum.restflow.utils.promise.PromiseHandler;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class ResourceFailureHandlerImpl implements PromiseHandler<RoutingContext> {

	private final  Logger logger = LoggerFactory.getLogger(getClass());
	
	@Override
	public void handle(RoutingContext context) {
		Throwable exception = context.failure();
		HttpServerResponse response = context.response();
		int code = 0; 
		boolean noData = false;
		if(exception instanceof RestflowUnauthorizedException) {
			code = HttpResponseStatus.UNAUTHORIZED.code();	
		} else if(exception instanceof RestflowForbiddenException) {
			code = HttpResponseStatus.FORBIDDEN.code();	
		} else if(exception instanceof RestflowNotAllowedException) {
			code = HttpResponseStatus.METHOD_NOT_ALLOWED.code();
		} else if(exception instanceof ResflowNotExistsException) {
			code = HttpResponseStatus.NOT_FOUND.code();
			noData = true;
		} else if(exception instanceof RestflowObjectValidationException) {
			code = HttpResponseStatus.BAD_REQUEST.code();
		} else if(RestflowException.class.isAssignableFrom(exception.getClass())) {
			code = HttpResponseStatus.BAD_REQUEST.code();
		} else {
			code = HttpResponseStatus.INTERNAL_SERVER_ERROR.code();
		}
		response.setStatusCode(code);
		if(noData) {
			response.end();
		} else {
			response.end(getJsonError(exception, code).toString());
		}
	}
	
	protected JsonObject getJsonError(Throwable exception, int code) {
		//TODO localized message
		if(logger.isDebugEnabled()) {
			logger.debug("Request error found.", exception);
		}
		JsonObject json = new JsonObject()               
		        .put("timestamp", System.nanoTime())
		        .put("status",code)
		        .put("error", exception.getMessage());
		if(exception instanceof RestflowObjectValidationException) {
			List<RestflowValidationException> exceptions = 
					((RestflowObjectValidationException) exception).getValidationErrors();
			List<String> errors = new ArrayList<>();
			if(exceptions != null && exceptions.size() > 0) {
				exceptions.stream().forEach(e -> {
					errors.add(e.getMessage());
				});
			}
			json.put("validationErrors", errors);
		}
		return json;		
	}
	
}


