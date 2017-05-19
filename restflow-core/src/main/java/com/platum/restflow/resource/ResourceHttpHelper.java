package com.platum.restflow.resource;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.platum.restflow.AuthMetadata;
import com.platum.restflow.RestflowDefaultConfig;
import com.platum.restflow.exceptions.ResflowObjectConversionException;
import com.platum.restflow.exceptions.RestflowException;
import com.platum.restflow.exceptions.RestflowInvalidRequestException;
import com.platum.restflow.resource.query.QueryFilter;
import com.platum.restflow.resource.query.QueryModifier;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;

public class ResourceHttpHelper<T> {
	
	protected static final String CONTENT_TYPE_PARAM = "content-type";
	
	protected static final String JSON_RESPONSE_TYPE = "application/json";
	
	protected static final String AUTH_HEADER = "Authorization";
	
	protected static final String QUERY_REQUEST_PARAM = "query";
	
	protected static final String FIELDS_REQUEST_PARAM = "fields";
	
	protected static final String SORT_REQUEST_PARAM = "sort";
	
	protected static final String LIMIT_REQUEST_PARAM = "limit";
	
	protected static final String OFFSET_REQUEST_PARAM = "offset";
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private RoutingContext context;
	
	private ResourceService<T> service;
	
	private ResourceMetadata<T> metadata;
	
	
	public ResourceHttpHelper(ResourceMetadata<T> metadata, RoutingContext context) {
		this.context = context;
		this.metadata = metadata;
	}
	
	public ResourceHttpHelper<T> end(HttpResponseStatus status) {
		return end(status, null);
	}
	
	public ResourceHttpHelper<T> end(HttpResponseStatus status, Object data) {
		try {
			HttpServerResponse response = context.response()
									       .putHeader(CONTENT_TYPE_PARAM, JSON_RESPONSE_TYPE)
									       .setStatusCode(status.code());
			if(data != null) {
				response.end(Json.encode(data));
			} else {
				response.end();
			}
		} catch(Throwable e) {
			if(logger.isDebugEnabled()) {
				logger.debug("Error resolving data to status ["+status.toString()+"]", e);
			}
			fail(e);
		}
		return this;		
	}
	
	public ResourceHttpHelper<T> fail(Throwable error) {
		context.fail(error);
		return this;
	}
	
	public ResourceService<T> service() {
		try {
			if(service == null) {
				service = ResourceFactory.getServiceInstance(metadata);
			}			
		} catch(Throwable e) {
			if(logger.isDebugEnabled()) {
				logger.debug("Error resolving service.", e);
			}
			throw new RestflowException("Cannot resolve resource service.");
		}
		service.authorization((AuthMetadata) context.get(AUTH_HEADER));
		return service;
	}
	
	public QueryFilter getFilterFromRequest() {
		return getFilterFromRequest(context);
	}

	public QueryModifier getModifierFromRequest() {
		return getModifierFromRequest(context);
	}
	
	public static QueryFilter getFilterFromRequest(RoutingContext context) {
		String query = context.request().getParam(QUERY_REQUEST_PARAM);
		if(query == null || StringUtils.isEmpty(query)) {
			return null;
		}
		return QueryFilter.fromJson(query);		
	}
	
	public static QueryModifier getModifierFromRequest(RoutingContext context) {
		HttpServerRequest request = context.request();
		return QueryModifier.fromJson(request.getParam(FIELDS_REQUEST_PARAM),
				request.getParam(SORT_REQUEST_PARAM), request.getParam(LIMIT_REQUEST_PARAM),
				request.getParam(OFFSET_REQUEST_PARAM));	
	}
	
	public Params getParamsFromRequest() {
		Params params = new Params();
		HttpServerRequest request = context.request();
		MultiMap paramsMap = context.request().params();
		if(paramsMap != null && !paramsMap.isEmpty()) {
			paramsMap.names().stream()
			.forEach(paramName -> {
				params.addParam(paramName, request.getParam(paramName));
			});
		}
		return params;
	};
	
	public Object getRequestIdParam() {
		String id = context.request().getParam(RestflowDefaultConfig.DEFAULT_ID_PARAM);
		if(!StringUtils.isEmpty(id)) {
			try {
				return ConvertUtils.convert(id, metadata.idClass());
			} catch(Throwable e) {
				throw new RestflowInvalidRequestException("Id ["+id+"] parameter is not valid.");
			}
		}
		return null;
	};
	
	@SuppressWarnings("unchecked")
	public <E> E getRequestResourceObject() {
		String json = context.getBodyAsString();
		if(json != null) {
			ObjectMapper mapper = new ObjectMapper();
			try {
				return (E) mapper.readValue(json, metadata.resourceClass);
			} catch (Throwable e) {
				throw new ResflowObjectConversionException(
						"It was impossible to convert request data to object(s).", e);
			}
		}
		throw new RestflowInvalidRequestException("Request body is empty.");
	}
	
	public ResourceHttpHelper<T> logRequest(ResourceMethod method) {
		if(logger.isInfoEnabled()) {
			logger.info("Request for method ["+method.getName()+"] with url "+method.getUrl());
		}
		return this;
	}
	
}
