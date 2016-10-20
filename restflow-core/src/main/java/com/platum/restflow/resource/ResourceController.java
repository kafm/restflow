package com.platum.restflow.resource;

import java.lang.reflect.Field;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.reflect.FieldUtils;

import com.platum.restflow.RestflowHttpMethod;
import com.platum.restflow.RestflowRoute;
import com.platum.restflow.exceptions.RestflowException;
import com.platum.restflow.resource.impl.AbstractResourceComponent;
import com.platum.restflow.resource.property.ResourceProperty;

import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * TODO refactor to use RestflowController
 * */
public class ResourceController<T> extends AbstractResourceComponent<T>{
			
	public ResourceController(ResourceMetadata<T> metadata) {
		super(metadata);
	}
			
	public ResourceController<T> get(RestflowRoute route, ResourceMethod method) {
		if(RestflowHttpMethod.GET.equalValue(method.getUrl())) {
			route
			.httpMethod(RestflowHttpMethod.GET, routingContext -> {
				ResourceHttpHelper<T> helper = new ResourceHttpHelper<T>(metadata, routingContext);
				helper.logRequest(method)
				.service()
				.find(method, helper.getFilterFromRequest(), helper.getModifierFromRequest())
				.success(data -> { 
					helper.end(HttpResponseStatus.OK, data);
				})
				.error(error -> {
					helper.fail(error);
				});
			});			
		} else if(RestflowHttpMethod.GET_WITH_ID.equalValue(method.getUrl())) {
			route.httpMethod(RestflowHttpMethod.GET_WITH_ID, routingContext -> {
				ResourceHttpHelper<T> helper = new ResourceHttpHelper<T>(metadata, routingContext);
				helper.logRequest(method)
				.service()
				.get(method, new Params().addParam(metadata.idPropertyName()
								, helper.getRequestIdParam()))
				.success(data -> { 
					helper.end(HttpResponseStatus.OK, data);
				})
				.error(error -> {
					helper.fail(error);
				});
			});			
		} else {
			route.httpMethod(RestflowHttpMethod.GET, routingContext -> {
				ResourceHttpHelper<T> helper = new ResourceHttpHelper<T>(metadata, routingContext);
				helper.logRequest(method)
				.service()
				.find(method, helper.getFilterFromRequest(), helper.getModifierFromRequest())
				.success(data -> { 
					helper.end(HttpResponseStatus.OK, data);
				})
				.error(error -> {
					helper.fail(error);
				});
			});
		}
		return this;
	}

	public ResourceController<T> post(RestflowRoute route, ResourceMethod method) {
		if(RestflowHttpMethod.POST.equalValue(method.getUrl())) {
			route.httpMethod(RestflowHttpMethod.POST, routingContext -> {
				ResourceHttpHelper<T> helper = new ResourceHttpHelper<T>(metadata, routingContext);									   
				helper.logRequest(method)
				.service()
				.insert(method, helper.getRequestResourceObject())
				.success(data -> { 
					helper.end(HttpResponseStatus.CREATED, data);
				})
				.error(error -> {
					helper.fail(error);
				});
			});
		} else {
			route.httpMethod(RestflowHttpMethod.POST, routingContext -> {
				ResourceHttpHelper<T> helper = new ResourceHttpHelper<T>(metadata, routingContext);									   
				helper.logRequest(method)
				.service()
				.insert(method, helper.getRequestResourceObject(), helper.getParamsFromRequest())
				.success(data -> { 
					helper.end(HttpResponseStatus.CREATED, data);
				})
				.error(error -> {
					helper.fail(error);
				});
			});
		}
			
		return this;
	}

	public ResourceController<T> put(RestflowRoute route, ResourceMethod method) {
		if(RestflowHttpMethod.PUT_WITH_ID.equalValue(method.getUrl())) {
			route.httpMethod(RestflowHttpMethod.PUT_WITH_ID, routingContext -> {
				ResourceHttpHelper<T> helper = new ResourceHttpHelper<T>(metadata, routingContext);		
				helper.logRequest(method)
				.service()
				.update(method, setObjectId(helper.getRequestResourceObject(), helper.getRequestIdParam()))
				.success(data -> { 
					helper.end(HttpResponseStatus.OK, data);
				})
				.error(error -> {
					helper.fail(error);
				});
			})	;		
		} else {
			route.httpMethod(RestflowHttpMethod.PUT,routingContext -> {
				ResourceHttpHelper<T> helper = new ResourceHttpHelper<T>(metadata, routingContext);		
				helper.logRequest(method)
				.service()
				.update(method, setObjectId(helper.getRequestResourceObject(), 
							helper.getRequestIdParam()), helper.getParamsFromRequest())
				.success(data -> { 
					helper.end(HttpResponseStatus.OK, data);
				})
				.error(error -> {
					helper.fail(error);
				});
			});
		}
		return this;
	}

	public ResourceController<T> patch(RestflowRoute route, ResourceMethod method) {
		route
		.httpMethod(RestflowHttpMethod.PATCH, routingContext -> {
			ResourceHttpHelper<T> helper = new ResourceHttpHelper<T>(metadata, routingContext);		
			helper.logRequest(method)
			.service()
			.parcialUpdate(method, setObjectId(helper.getRequestResourceObject(), helper.getRequestIdParam()),
							helper.getParamsFromRequest())
			.success(data -> { 
				helper.end(HttpResponseStatus.OK, data);
			})
			.error(error -> {
				helper.fail(error);
			});
		});
		return this;
	}

	public ResourceController<T> delete(RestflowRoute route, ResourceMethod method) {
		if(RestflowHttpMethod.DELETE_WITH_ID.equalValue(method.getUrl())) {
			route.httpMethod(RestflowHttpMethod.DELETE_WITH_ID, routingContext -> {
				ResourceHttpHelper<T> helper = new ResourceHttpHelper<T>(metadata, routingContext);		
				helper.logRequest(method)
				.service()
				.delete(method, new Params().addParam(metadata.idPropertyName(), helper.getRequestIdParam()))
				.success(data -> { 
					helper.end(HttpResponseStatus.OK, data);
				})
				.error(error -> {
					helper.fail(error);
				});
			});			
		} else {
			route.httpMethod(RestflowHttpMethod.DELETE, routingContext -> {
				ResourceHttpHelper<T> helper = new ResourceHttpHelper<T>(metadata, routingContext);		
				helper.logRequest(method)
				.service()
				.delete(method, helper.getParamsFromRequest())
				.success(data -> { 
					helper.end(HttpResponseStatus.OK, data);
				})
				.error(error -> {
					helper.fail(error);
				});
			});
		} 
		return this;
	}

	protected T setObjectId(T object, Object id) {
		if(id != null) {
			Resource resource = metadata.resource();
			Validate.notNull(resource, "Service without resource.");
			ResourceProperty idProperty = resource.getIdPropertyAsObject();
			if(idProperty != null) {
				if(object instanceof ResourceObject) {
					((ResourceObject) object).setIdProperty(idProperty)
											 .setId(id);
				} else {
					try {
						Field idField = FieldUtils.getField(metadata.idClass(), 
											idProperty.getName(), true);
						FieldUtils.writeField(idField, object, id, true);	
					} catch(Throwable e) {
						throw new RestflowException("Cannot set id field.", e);
					}
				}	
			}		
		}
		return object;
	}
		
	@Override
	public void close() {
		//TODO log yourself
	}
	
}