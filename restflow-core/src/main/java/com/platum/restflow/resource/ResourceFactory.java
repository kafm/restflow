package com.platum.restflow.resource;


import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import com.google.common.collect.Maps;
import com.platum.restflow.DatasourceDetails;
import com.platum.restflow.FileSystemDetails;
import com.platum.restflow.Restflow;
import com.platum.restflow.exceptions.RestflowException;
import com.platum.restflow.resource.annotation.HookManager;
import com.platum.restflow.resource.annotation.QueryBuilderImpl;
import com.platum.restflow.resource.impl.ResourceFailureHandlerImpl;
import com.platum.restflow.resource.impl.ResourceServiceImpl;
import com.platum.restflow.resource.impl.SimpleResourceFileSystem;
import com.platum.restflow.resource.impl.jdbc.JdbcRepository;
import com.platum.restflow.resource.query.QueryBuilder;
import com.platum.restflow.utils.ClassUtils;
import com.platum.restflow.utils.promise.PromiseHandler;

import io.vertx.ext.web.RoutingContext;

public class ResourceFactory {	
	
	private static final Map<String, Class<? extends QueryBuilder>> queryBuilders = Maps.newHashMap();
	
	private static  HookManager hookManager;
	
	public static <T> ResourceMetadata<T> getResourceMetadataInstance(Restflow restflow, Resource resource) {
		Validate.notNull(restflow, "Restflow cannot be null");
		Validate.notNull(resource, "Restflow resource cannot be null");
		return new ResourceMetadata<T>(restflow, resource, getResourceClass(resource));
	}
	
	public static <T> ResourceService<T> getServiceInstance(Restflow restflow, Resource resource) {
		return getServiceInstance(getResourceMetadataInstance(restflow, resource));
	}
		
	@SuppressWarnings("unchecked")
	public static <T> ResourceService<T> getServiceInstance(ResourceMetadata<T> resourceMetadata) {
		ResourceService<T> service = getComponentInstance(ResourceServiceImpl.class, resourceMetadata);
		if(hookManager == null) {	
			hookManager = HookManager.newInstance(resourceMetadata.getBasePackage())
									 .populate();
		}
		if(!hookManager.isEmpty()) {
			service.hooks(hookManager.getInterceptor(resourceMetadata.resource().getName()));
		}
		return service;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> ResourceRepository<T> getRespositoryInstance(ResourceMetadata<T> resourceMetadata) {
		Validate.notNull(resourceMetadata, "Restflow metadata cannot be null");
		DatasourceDetails datasource = resourceMetadata.datasource();
		if(datasource == null) {
			return null;
		}
		String implClass = datasource.getImplClass();
		if(implClass == null) {
			implClass = JdbcRepository.class.getName();
		}
		return (ResourceRepository<T>) 
				getComponentInstance(ClassUtils.getClassByName(implClass), resourceMetadata);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> ResourceController<T> getControllerInstance(ResourceMetadata<T> resourceMetadata) {
		Validate.notNull(resourceMetadata, "Restflow metadata cannot be null");
		return getComponentInstance(ResourceController.class, resourceMetadata);
	}

	public static <T> ResourceFileSystem getFileSystemInstance(ResourceMetadata<T> resourceMetadata) {
		Validate.notNull(resourceMetadata, "Restflow metadata cannot be null");
		FileSystemDetails fs = resourceMetadata.fileSystem();
		if(fs == null) {
			return null;
		}
		String implClass = fs.getImplClass();
		if(implClass == null) {
			implClass = SimpleResourceFileSystem.class.getName();
		}
		return (ResourceFileSystem) 
				getComponentInstance(ClassUtils.getClassByName(implClass), resourceMetadata);
	}
	
	public static <T> Class<? extends QueryBuilder> getQueryBuilderClass(ResourceMetadata<T> resourceMetadata) {
		String implClassName = resourceMetadata.datasource().getImplClass();
		if(StringUtils.isEmpty(implClassName)) {
			return null;
		}
		Class<? extends QueryBuilder> builder = queryBuilders.get(implClassName);
		if(builder == null) {
			Class<?> implClass = ClassUtils.getClassByName(implClassName);
			if(implClass != null) {
				QueryBuilderImpl qBuilderAnnotation = implClass.getAnnotation(QueryBuilderImpl.class);
				if(qBuilderAnnotation != null) {
					builder = qBuilderAnnotation.value();
					if(builder != null) {
						queryBuilders.put(implClassName, builder);	
					}
				}
			}
		}
		return builder;
	}
	
	public static PromiseHandler<RoutingContext> getResourceFailureHandler() {
		return new ResourceFailureHandlerImpl();
	}
	
	protected static <T, C> T getComponentInstance(Class<T> componentClass, C constructorObject) {
		try {
			return componentClass
					.getDeclaredConstructor(constructorObject.getClass())
					.newInstance(constructorObject);
		} catch (Throwable e) {
			throw new RestflowException("It was impossible to create resource component ["+componentClass+"].", e);
		}
	}
	
	protected static <T> Class<T> getResourceClass(Resource resource) {
		String resourceClass = resource.getResourceClass();
		return StringUtils.isNotEmpty(resourceClass)
							? ClassUtils.getClassByName(resourceClass) 
							: ClassUtils.castClass(ResourceObject.class);	
	}
	
}
