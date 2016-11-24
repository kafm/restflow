package com.platum.restflow;

import java.util.List;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.platum.restflow.components.ComponentFactory;
import com.platum.restflow.components.Filter;
import com.platum.restflow.exceptions.RestflowException;
import com.platum.restflow.resource.DownloadMethod;
import com.platum.restflow.resource.ResourceMethod;
import com.platum.restflow.resource.UploadMethod;

import io.vertx.core.Handler;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;


public class RestflowRoute {
	
	private static final List<Filter> filters = ComponentFactory.getFilterServices();
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private Router router;
	
	private String basicUrl;
	
	private ResourceMethod method;
	
	private DownloadMethod download;
	
	private UploadMethod upload;
	
	private RestflowHttpMethod httpMethod;
	
	private Handler<RoutingContext> handler;
	
	private String url;
	
	public Route deploy() {
		Validate.notNull(router, "Router cannot be null");
		Validate.isTrue(method != null || download != null || upload != null, "Method and download and upload cannot be null");
		Validate.notNull(basicUrl, "BasicUrl cannot be null");
		Validate.notNull(httpMethod, "Http method cannot be null");
		url = basicUrl;
		Route route = null;
		if(method != null) {
			url += httpMethod.parseUrl(method.getUrl());
			route = getRoute();
		} else if(download != null){
			url += httpMethod.parseUrl(RestflowHttpMethod.DOWNLOAD.value());
			route = getDownloadRoute();
		} else {
			url += httpMethod.parseUrl(RestflowHttpMethod.UPLOAD.value());
			route = getUploadRoute();
		}
		route.handler(context-> handleRoute(context));
			// .failureHandler(failureHandler::handle); 
		return route;
	}
	
	private void handleRoute(RoutingContext context) {
		if(filters != null && !filters.isEmpty()) {
			filters.stream().forEach(filter -> {
				filter.filter(method, context);	
			});
		} else if(logger.isDebugEnabled()) {
			logger.debug("No filter services found.");
		}
		if(!context.failed()) {
			handler.handle(context);
		}
	}
	
	public Route getRoute() {
		return getRoute(url);
	}
	
	public Route getRoute(String url) {
		Route route = null;
		if(httpMethod.isPost()) {
			router.route(url).handler(BodyHandler.create());
			route = router.post(url);
		} else if(httpMethod.isPut()) {
			router.route(url).handler(BodyHandler.create());
			route = router.put(url);
		} else if(httpMethod.isPatch()) {
			router.route(url).handler(BodyHandler.create());
			route = router.patch(url);
		} else if(httpMethod.isDelete()) {
			route = router.delete(url);
		} else if(httpMethod.isGet()) {
			route = router.get(url);
		} else {
			throw new RestflowException("Http Method "+httpMethod.toString()+" is not supported");
		}
		return route;
	}
	
	public Route getDownloadRoute() {
		return router.get(url);
	}
	
	public Route getUploadRoute() {
		return router.post(url);
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
	
	public RestflowRoute download(DownloadMethod download) {
		this.download = download;
		return this;
	}
	
	public DownloadMethod download() {
		return download;
	}
	
	public RestflowRoute upload(UploadMethod upload) {
		this.upload = upload;
		return this;
	}
	
	public UploadMethod upload() {
		return upload;
	}

	public RestflowRoute httpMethod(RestflowHttpMethod httpMethod, Handler<RoutingContext> handler) {
		this.httpMethod = httpMethod;
		this.handler = handler;
		return this;
	}
	
}
