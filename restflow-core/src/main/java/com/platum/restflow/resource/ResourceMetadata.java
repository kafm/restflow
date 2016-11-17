package com.platum.restflow.resource;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.reflect.FieldUtils;

import com.google.common.collect.Maps;
import com.platum.restflow.DatasourceDetails;
import com.platum.restflow.FileSystemDetails;
import com.platum.restflow.Restflow;
import com.platum.restflow.RestflowEnvironment;
import com.platum.restflow.exceptions.RestflowException;
import com.platum.restflow.resource.property.ResourceProperty;
import com.platum.restflow.resource.property.ResourcePropertyType;
import com.platum.restflow.resource.query.QueryBuilder;
import com.platum.restflow.utils.ClassUtils;

public class ResourceMetadata<T> {
	
	protected static final  Map<ResourcePropertyType, Class<?>> propertyClassMap;
	
	static
    {
		Map<ResourcePropertyType, Class<?>> map = Maps.newHashMap();
		map.put(ResourcePropertyType.STRING, String.class);
		map.put(ResourcePropertyType.INTEGER, Integer.class);
		map.put(ResourcePropertyType.LONG, Long.class);
		map.put(ResourcePropertyType.DECIMAL, BigDecimal.class);
		map.put(ResourcePropertyType.DATE, Date.class);
		map.put(ResourcePropertyType.EMAIL, String.class);
		map.put(ResourcePropertyType.IPV4, String.class);
		map.put(ResourcePropertyType.IPV6, String.class);
		map.put(ResourcePropertyType.URL, String.class);
		map.put(ResourcePropertyType.CREDITCARD, String.class);
	    propertyClassMap = Collections.unmodifiableMap(map);
    }
	
	protected Restflow restflow;
	
	protected Resource resource;
	
	protected Class<T> resourceClass;
	
	protected Class<? extends QueryBuilder> queryBuilderClass;
	
	public ResourceMetadata(Restflow restflow, Resource resource, Class<T> resourceClass) {
		this.restflow = restflow;
		this.resource = resource;
		this.resourceClass = resourceClass;
		queryBuilderClass = ResourceFactory.getQueryBuilderClass(this);
	}
	
	public T setObjectId(T object, Object id) {
		if(id != null) {
			Validate.notNull(resource, "Service without resource.");
			ResourceProperty idProperty = resource.getIdPropertyAsObject();
			if(idProperty != null) {
				if(object instanceof ResourceObject) {
					((ResourceObject) object).setIdProperty(idProperty)
											 .setId(id);
				} else {
					try {
						Field idField = FieldUtils.getField(idClass(), 
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

	@SuppressWarnings("unchecked")
	public <I> I getObjectId(T object) {
		Validate.notNull(resource, "Service without resource.");
		ResourceProperty idProperty = resource.getIdPropertyAsObject();
		I id = null;
		if(idProperty != null) {
			if(object instanceof ResourceObject) {
				ResourceObject rObj = (ResourceObject) object;
				if(StringUtils.isEmpty(rObj.getIdProperty())) {
					rObj.setIdProperty(idProperty);
				}
				return rObj.getId();
			} else {
				try {
					Field idField = FieldUtils.getField(idClass(), 
										idProperty.getName(), true);
					return (I) FieldUtils.readField(idField, object);
				} catch(Throwable e) {
					throw new RestflowException("Cannot set id field.", e);
				}
			}	
		}
		return id;
	}

	public Restflow restflow() {
		return restflow;
	}
	
	public Resource resource() {
		return resource;
	}
	
	public Class<T> resourceClass() {
		return resourceClass;
	}
	
	public DatasourceDetails datasource() {
		return restflow.getDatasource(resource.getDatasource());
	}
	
	public FileSystemDetails fileSystem() {
		return restflow.getFileSystem(resource.getFileSystem());
	}
	
	public <I> Class<I> idClass() {
		return getPropertyClass(resource.getIdPropertyAsObject());
	}
	
	public String idPropertyName() {
		return resource.getIdProperty();
	}
	
	public String getBasePackage() {
		return restflow.getEnvironment().getProperty(RestflowEnvironment.BASE_PACKAGE_PROPERTY);
	}
	
	@SuppressWarnings("unchecked")
	public static  <I> Class<I> getPropertyClass(ResourceProperty property) {
		if(property != null) {
			return (Class<I>) propertyClassMap.get(property.getType());
		} 
		return (Class<I>) Object.class;
	}
	
	public QueryBuilder getQueryBuilderInstance() {
		if(queryBuilderClass == null) {
			throw new RestflowException("Query builder for datasource not found.");
		} 
		QueryBuilder builder = ClassUtils.newInstance(queryBuilderClass);
		return builder.resource(resource);
	}

}
