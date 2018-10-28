package com.platum.restflow.resource;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.reflect.FieldUtils;

import com.platum.restflow.RestflowDefaultConfig;
import com.platum.restflow.exceptions.RestflowException;
import com.platum.restflow.exceptions.RestflowFieldConversionValidationException;
import com.platum.restflow.resource.property.ResourceProperty;
import com.platum.restflow.utils.ClassUtils;

public class ResourceObject extends CaseInsensitiveMap<String, Object> implements Serializable {

	private static final long serialVersionUID = -7357638347355290212L;
	
	private String uid;
	
	private String idProperty;
	
	private Class<?> idClass; 
	
	public ResourceObject() {
		uid = UUID.randomUUID().toString();
	}
	
	public String getUid() {
		return uid;
	}

	public <T> T convertToClass(Class<T> clazz) {
		Validate.notNull(clazz, "Class cannot be null.");
		T object = ClassUtils.newInstance(clazz);
		getProperties().entrySet().stream()
		.forEach(property  -> {
			try {
				FieldUtils.writeField(object, property.getKey(), property.getValue(), true);
			} catch (Throwable e) {
				throw new RestflowException("It was not possible to set field ["+property.getKey()+
						"] with value ["+property.getValue()+"]", e);
			}
		});
		return object;
	}
	
	@SuppressWarnings("unchecked")
	public <I> I getId() {
		if(StringUtils.isEmpty(idProperty)) 
			return (I) getProperty("id");
		return (I) getProperty(idProperty, idClass);
	}
	
	public ResourceObject setId(Object id) {
		if(!StringUtils.isEmpty(idProperty)) {
			put(idProperty, id);
			return this;
		}
		throw new RestflowException("No id property reference provided.");
	}
	
	public Object getProperty(String name) {
		return StringUtils.isEmpty(name) ? null : get(name);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getProperty(String name, Class<T> clazz) {
		Object value = getProperty(name);
		if(value != null) {
			if(clazz == null) clazz = (Class<T>) String.class;
			return (T) ConvertUtils.convert(value, clazz);
		}
		return null;
	}
	
	public ResourceObject setProperty(String name, Object value) {
		put(name, value);
		return this;
	}
	
	public String getString(String name) {
		Object val = get(name);
		return (val != null) ? val.toString() : null;
	}
	
	public ResourceObject setString(String name, String value) {
		put(name, value.toString());
		return this;
	}
	
	public Integer getInteger(String name) {
		Object value = get(name);
		if(value != null) {
			if(value instanceof String) {
				return Integer.parseInt((String) value);	
			} else if(value instanceof Long) {
				return((Long) value).intValue(); 
			}
		}  else {
			return 0;
		}
		return (Integer) value;
	}
	
	public ResourceObject setInteger(String name, Integer value) {
		put(name, value);
		return this;
	}
	
	public Long getLong(String name) {
		Object value = get(name);
		if(value != null) {
			if(value instanceof String) {
				return Long.parseLong((String) value);	
			} else if(value instanceof Integer) {
				return ((Integer) value).longValue(); 
			} 
		} else {
			return 0L;
		}
		return (Long) value;
	}
	
	public ResourceObject setLong(String name, Long value) {
		put(name, value);
		return this;
	}
	
	public BigDecimal getDecimal(String name) {
		return (BigDecimal) get(name);
	}
	
	public ResourceObject setDecimal(String name, BigDecimal value) {
		put(name, value);
		return this;
	}
	
	public Boolean getBoolean(String name) {
		Object value = get(name);
		if(value == null) return false;
		return (Boolean) value;
	}
	
	public ResourceObject setBoolean(String name, Boolean value) {
		put(name, value);
		return this;
	}

	public Date getDate(String name) {
		return getDate(name, null);
	}
	
	public Date getDate(String name, String format) {
		Object p =  get(name);
		if(p != null && !(p instanceof Date)) {
			try {
				format = StringUtils.isEmpty(format)
						? RestflowDefaultConfig.DEFAULT_DATE_FORMAT : format;
				return new SimpleDateFormat(format).parse(p.toString());
			} catch (ParseException e) {
				throw new RestflowFieldConversionValidationException("Invalid date"+p.toString()+" or format "+format);
			}
		} 
		return (Date) p;
	}
	
	public ResourceObject setDate(String name, Date value) {
		put(name, value);
		return this;
	}
	
	@SuppressWarnings("unchecked")
	public <T> List<T> getList(String name) {
		return (List<T>) get(name);
	}

	public List<ResourceObject> getListOfObjects(String name, ResourceProperty idProperty) {
		List<Map<String, Object>> list = getList(name);
		if(list == null) return null;
		List<ResourceObject> objList = new ArrayList<ResourceObject>();
		for(Map<String,Object> map : list)
		{
			ResourceObject obj = 
					new ResourceObject().setIdProperty(idProperty);
			obj.putAll(map);
			objList.add(obj);
		}
		return objList;
	}
		
	public <T> ResourceObject setList(String name, List<T> value) {
		put(name, value);
		return this;
	}
		
	public Boolean hasProperty(String name) {
		return containsKey(name);
	}
	
	public Map<String, Object> getProperties() {
		return this;
	}

	public ResourceObject setProperties(Map<String, Object> properties) {
		this.putAll(properties);
		return this;
	}

	public ResourceObject setIdProperty(ResourceProperty property) {
		if(property != null) {
			this.idProperty = property.getName();
			this.idClass = ResourceMetadata.getPropertyClass(property);
		}
		return this;
	}
	
	public String getIdProperty() {
		return idProperty;
	}
	
}
