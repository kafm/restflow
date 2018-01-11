package com.platum.restflow.resource.impl.jdbc;


import java.math.BigDecimal;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sql2o.ResultSetHandler;

import com.platum.restflow.exceptions.RestflowException;
import com.platum.restflow.resource.Resource;
import com.platum.restflow.resource.ResourceMetadata;
import com.platum.restflow.resource.ResourceObject;
import com.platum.restflow.resource.property.ResourceProperty;
import com.platum.restflow.resource.property.ResourcePropertyValidator;
import com.platum.restflow.resource.query.QueryField;
import com.platum.restflow.utils.ClassUtils;

public class JdbcResourceObjectMapper<T> implements ResultSetHandler<T> {
	
	private final  Logger logger = LoggerFactory.getLogger(getClass());
	
	private ResourceProperty idProperty;
	
	private List<ResourceProperty> properties;
	
	private Class<?> clazz;
	
	private QueryField[] fields;
	
	private boolean cast;
		
	public JdbcResourceObjectMapper(ResourceMetadata<?> metadata,  QueryField... fields) {
		Resource resource = metadata.resource();
		this.properties = resource.getProperties();
		this.clazz = metadata.resourceClass();
		this.cast = (fields != null && fields.length > 0) ||
						ResourceObject.class.equals(clazz);
		this.fields = fields;
		this.idProperty = resource.getIdPropertyAsObject(); 
	}

	@Override
	public T handle(ResultSet rs) throws SQLException {
		ResultSetMetaData metadata = rs.getMetaData();
		int numColumns = metadata.getColumnCount();
		T object = getObjectInstance();
		if(cast && idProperty != null) {
			((ResourceObject) object).setIdProperty(idProperty);
		}
		for(int i = 1; i <= numColumns; i++) {
			String column = metadata.getColumnLabel(i);
			setValue(object, column, rs);
		}
		return object;
	}
	
	private void setValue(T object, String column, ResultSet rs) throws SQLException {
		ResourceProperty property = getProperty(column);
		if(property != null) {
			setValue(object, property, column, rs);
			return;
		} else if(fields != null && fields.length > 0) {
			QueryField field = Stream.of(fields)
								.filter(f -> f.toStringRepresentation().equalsIgnoreCase(column))
								.findAny()
								.orElse(null);
			if(field != null) {
				String label = field.measure() != null 
								? "$"+field.measure()+"("+field.field()+")"
								: field.field();
				setValue(object, label, rs.getObject(column));
				return;
			}
		} else {
			setValue(object, column, rs.getObject(column));
		}
		if(logger.isDebugEnabled()) {
			logger.debug("No property match found for column "+column);
		}
	}
		
	private void setValue(T object, ResourceProperty property, String column, ResultSet rs) throws SQLException {
		Object value = null;
		if(property.isRepeating()) {
			value = resolveRepeatingValue(property, column, rs);
		} else {
			switch(property.getType()) {
			case BOOLEAN:
				value = rs.getBoolean(column);
				break;
			case INTEGER:
				value = rs.getInt(column);
				break;
			case LONG:
				value = rs.getLong(column);
				break;
			case DECIMAL:
				value =  rs.getBigDecimal(column);
				break;					
			case DATE:
				value = timestampToDateString(rs.getTimestamp(column), property.getFormat());
				break;
			default:
				value = rs.getString(column);
			}			
		}
		if(value != null){
			setValue(object, property.getName(), value);
		}
	}

	private void setValue(T object, String field, Object value) {
		try {
			if(cast) {
				((ResourceObject) object).setProperty(field, value);
			} else {
				FieldUtils.writeField(object, field, value, true);
			}
			
		} catch (Throwable e) {
			throw new RestflowException("It was not possible to set field ["+field+"] with value ["+value+"]", e);
		}
	}
	
	private Object resolveRepeatingValue(ResourceProperty property, String column, ResultSet rs) throws SQLException {
		Array sqlArr = rs.getArray(column);
		if(sqlArr != null) {
			ResultSet rsArr = sqlArr.getResultSet();
			switch(property.getType()) {
			case BOOLEAN:
				List<Boolean> booleanList = new ArrayList<>();
				while(rsArr.next()) {
					booleanList.add(rsArr.getBoolean(2));
				}			
				return booleanList;
			case INTEGER:
				List<Integer> integerList = new ArrayList<>();
				while(rsArr.next()) {
					integerList.add(rsArr.getInt(2));
				}			
				return integerList; 
			case LONG:
				List<Long> longList = new ArrayList<>();
				while(rsArr.next()) {
					longList.add(rsArr.getLong(2));
				}			
				return longList; 
			case DECIMAL:
				List<BigDecimal> decimalList = new ArrayList<>();
				while(rsArr.next()) {
					decimalList.add(rsArr.getBigDecimal(2));
				}			
				return decimalList; 				
			case DATE:
				List<String> dateList = new ArrayList<>();
				while(rsArr.next()) {
					dateList.add(timestampToDateString( rsArr.getTimestamp(2), property.getFormat()));
				}			
				return dateList;
			default:
				List<String> stringList = new ArrayList<>();
				while(rsArr.next()) {
					stringList.add(rsArr.getString(2));
				}			
				return stringList;
			}			
		}
		return null;
	}
	
	private String timestampToDateString(Timestamp timestamp, String format) {
		if(timestamp != null) {
			String formatStr = StringUtils.isEmpty(format)?
							ResourcePropertyValidator.DEFAULT_DATE_FORMAT : format;
			return new SimpleDateFormat(formatStr).format(new Date(timestamp.getTime()));
		}
		return null;
	}
	
	private ResourceProperty getProperty(String column) {
		try {
			return properties.stream()
					  .filter(p -> p.getColumn().equalsIgnoreCase(column))	
					  .findAny()
					  .orElse(null);
		} catch(Throwable e) {}
		return null;
	}
	
	private T  getObjectInstance() {
		return ClassUtils.newInstance(clazz);
	}


}
