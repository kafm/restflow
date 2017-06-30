package com.platum.restflow.resource.impl;

import org.apache.commons.lang3.Validate;

import com.platum.restflow.resource.ObjectContext;
import com.platum.restflow.resource.Params;
import com.platum.restflow.resource.Resource;
import com.platum.restflow.resource.ResourceFactory;
import com.platum.restflow.resource.ResourceMetadata;
import com.platum.restflow.resource.ResourceMethod;
import com.platum.restflow.resource.ResourceService;
import com.platum.restflow.utils.promise.Promise;
import com.platum.restflow.utils.promise.PromiseHandler;
import com.platum.restflow.utils.promise.PromiseResult;

import io.vertx.core.Handler;

public class ObjectContextImpl<T> implements ObjectContext<T> {
	
	protected T object;
	
	protected Params params;
	
	protected Resource resource;
	
	protected ResourceService<T> service;
	
	protected ResourceMethod contextMethod;

	protected boolean ignore;
	
	protected boolean partial;
	
	protected boolean isNew;
	
	protected String lang;
	
	public ObjectContextImpl(Resource resource, ResourceService<T> service, T object) {
		this(resource, service, object, null);
	}
	
	public ObjectContextImpl(Resource resource, ResourceService<T> service, Params params) {
		this(resource, service, null, params);
	}
	
	public ObjectContextImpl(Resource resource, ResourceService<T> service, T object, Params params) {
		this.resource = resource;
		this.service = service;
		this.params = params;
		this.object = object;
	}

	@Override
	public void save(PromiseHandler<PromiseResult<T>> handler) {
		save(null, handler);
	}
	
	@Override
	public void save(ResourceMethod method, PromiseHandler<PromiseResult<T>> handler) {
		Promise<T> promise = null;
		ResourceMethod m = method == null ? contextMethod : method;
		if(isNew) {
			promise = service.insert(m, object);
		} else if(partial) {
			promise = service.partialUpdate(m, object);
		} else {
			if(m == null) {
				promise = service.update(object);
			} else {
				promise = service.update(m, object);
			}
		}
		ignore = (method == null);
		promise.allways(handler);
	}

	@Override
	public void destroy(PromiseHandler<PromiseResult<Void>> handler) {
		service.delete(contextMethod, object)	
		.allways(handler);
	}

	@Override
	public void get(Handler<T> handler) {
		handler.handle(object);
	}

	@Override
	public ResourceService<T> service() {
		return service;
	}
	
	@Override
	public ResourceService<T> serviceFor(String resourceName) {
		Validate.notEmpty(resourceName);
		ResourceMetadata<T> metadata = service.metadata();
		Resource resource = metadata.restflow()
							.getResource(resourceName);
		ResourceService<T> s =  ResourceFactory
				 					.getServiceInstance(metadata.restflow(), resource);
		s.authorization(service.authorization());
		return s;
	}

	@Override
	public T object() {
		return object;
	}
	
	@Override
	public Params params() {
		return params;
	}

	@Override
	public Resource resource() {
		return resource;
	}
	
	@Override
	public boolean isNew() {
		return isNew;
	}

	@Override
	public ObjectContextImpl<T> isNew(boolean isNew) {
		this.isNew = isNew;
		return this;
	}
	
	@Override
	public boolean ignore() {
		return ignore;
	}

	@Override
	public ObjectContextImpl<T> ignore(boolean ignore) {
		this.ignore = ignore;
		return this;
	}
	
	@Override
	public boolean partial() {
		return this.partial();
	}
	
	public ObjectContextImpl<T> partial(boolean partial) {
		this.partial = partial;
		return this;
	}
	
	public ObjectContextImpl<T> contextMethod(ResourceMethod method) {
		contextMethod = method;
		return this;
	}

	@Override
	public String lang() {
		return lang;
	}

	@Override
	public ObjectContextImpl<T> lang(String lang) {
		this.lang = lang;
		return this;
	}
	
}
