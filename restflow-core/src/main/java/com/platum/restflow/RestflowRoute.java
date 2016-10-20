package com.platum.restflow;

import org.apache.commons.lang3.Validate;

import com.platum.restflow.exceptions.RestflowException;
import com.platum.restflow.resource.ResourceFactory;
import com.platum.restflow.resource.ResourceMethod;
import com.platum.restflow.utils.promise.PromiseHandler;

import io.vertx.core.Handler;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class RestflowRoute {
	
	private static final PromiseHandler<RoutingContext> failureHandler = ResourceFactory.getResourceFailureHandler();
	
	private Router router;
	
	private String basicUrl;
	
	private ResourceMethod method;
	
	private RestflowHttpMethod httpMethod;
	
	private Handler<RoutingContext> handler;
	
	private String url;
	
	public Route deploy() {
		Validate.notNull(router, "Router cannot be null");
		Validate.notNull(method, "Method cannot be null");
		Validate.notNull(basicUrl, "BasicUrl cannot be null");
		Validate.notNull(httpMethod, "Http method cannot be null");
		String methodUrl = method.getUrl();
		url = basicUrl + httpMethod.parseUrl(methodUrl);
		Route route = getRoute(url);
		route.handler(handler)
			 .failureHandler(routingContext -> failureHandler.handle(routingContext));
		return route;
	}
	
	public Route getRoute() {
		return getRoute(url);
	}
	
	public Route getRoute(String url) {
		if(httpMethod.isPost()) {
			router.route(url).handler(BodyHandler.create());
			return router.post(url);
		} else if(httpMethod.isPut()) {
			router.route(url).handler(BodyHandler.create());
			return router.put(url);
		} else if(httpMethod.isPatch()) {
			router.route(url).handler(BodyHandler.create());
			return router.patch(url);
		} else if(httpMethod.isDelete()) {
			return router.delete(url);
		} else if(httpMethod.isGet()) {
			return router.get(url);
		} else {
			throw new RestflowException("Http Method "+httpMethod.toString()+" is not supported");
		}
	}
	
	public String url() {
		return url;
	}

	public RestflowRoute router(Router router) {
		this.router = router;
		return this;
	}

	public RestflowRoute basicUrl(String basicUrl) {
		this.basicUrl = basicUrl;
		return this;
	}

	public RestflowRoute method(ResourceMethod method) {
		this.method = method;
		return this;
	}
	
	public ResourceMethod method() {
		return method;
	}

	public RestflowRoute httpMethod(RestflowHttpMethod httpMethod, Handler<RoutingContext> handler) {
		this.httpMethod = httpMethod;
		this.handler = handler;
		return this;
	}

}
