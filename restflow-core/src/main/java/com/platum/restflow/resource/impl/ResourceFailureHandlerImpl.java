package com.platum.restflow.resource.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.platum.restflow.RestflowContext;
import com.platum.restflow.exceptions.ResflowNotExistsException;
import com.platum.restflow.exceptions.RestflowException;
import com.platum.restflow.exceptions.RestflowForbiddenException;
import com.platum.restflow.exceptions.RestflowListOfException;
import com.platum.restflow.exceptions.RestflowNotAllowedException;
import com.platum.restflow.exceptions.RestflowObjectValidationException;
import com.platum.restflow.exceptions.RestflowUnauthorizedException;
import com.platum.restflow.exceptions.RestflowValidationException;
import com.platum.restflow.utils.promise.PromiseHandler;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class ResourceFailureHandlerImpl implements PromiseHandler<RoutingContext> {
	
	private final  Logger logger = LoggerFactory.getLogger(getClass());
	
	private RestflowContext context;
	
	public ResourceFailureHandlerImpl(RestflowContext context) {
		super();
		this.context = context;
	}
	
	@Override
	public void handle(RoutingContext routingContext) {
		Throwable exception = routingContext.failure();
		exception.printStackTrace();
		HttpServerResponse response = routingContext.response();
		HttpServerRequest request = routingContext.request();
		int code = 0; 
		if(exception instanceof RestflowUnauthorizedException) {
			code = HttpResponseStatus.UNAUTHORIZED.code();	
		} else if(exception instanceof RestflowForbiddenException) {
			code = HttpResponseStatus.FORBIDDEN.code();	
		} else if(exception instanceof RestflowNotAllowedException) {
			code = HttpResponseStatus.METHOD_NOT_ALLOWED.code();
		} else if(exception instanceof ResflowNotExistsException) {
			code = HttpResponseStatus.BAD_REQUEST.code();
		} else if(exception instanceof RestflowListOfException) {
			code = HttpResponseStatus.BAD_REQUEST.code();
		} else if(exception instanceof RestflowObjectValidationException) {
			code = HttpResponseStatus.BAD_REQUEST.code();
		} else if(exception instanceof RestflowException) {
			code = HttpResponseStatus.BAD_REQUEST.code();
		} else if(RestflowException.class.isAssignableFrom(exception.getClass())) {
			code = HttpResponseStatus.BAD_REQUEST.code();
		} else {
			code = HttpResponseStatus.INTERNAL_SERVER_ERROR.code();
		}
		response.setStatusCode(code);
		response.end(getJsonError(exception, code, 
				context.getLangRequestFromRequest(request)).toString());
	}

	protected JsonObject getJsonError(Throwable exception, int code, String langRequest) {
		if(logger.isDebugEnabled()) {
			logger.debug("Request error found.", exception);
		}
		JsonObject json = new JsonObject()               
		        .put("timestamp", System.nanoTime())
		        .put("status",code)
		        .put("error", context.getLangMessage(exception, langRequest));
		if(exception instanceof RestflowObjectValidationException) {
			List<RestflowValidationException> exceptions = 
					((RestflowObjectValidationException) exception).getValidationErrors();
			List<String> errors = new ArrayList<>();
			if(exceptions != null && exceptions.size() > 0) {
				exceptions.stream().forEach(e -> {
					errors.add(context.getLangMessage(e, langRequest));
				});
			}
			json.put("validationErrors", errors);
		} else if(exception instanceof RestflowListOfException) {
			List<Throwable> exceptions = ((RestflowListOfException) exception).getErrors();
			List<String> errors = new ArrayList<>();
			if(exceptions != null && exceptions.size() > 0) {
				exceptions.stream().forEach(e -> {
					errors.add(context.getLangMessage(e, langRequest));
				});
			}
			json.put("validationErrors", errors);			
		}
		return json;		
	}

}


