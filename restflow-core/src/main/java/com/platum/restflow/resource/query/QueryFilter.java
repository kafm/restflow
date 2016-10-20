package com.platum.restflow.resource.query;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.platum.restflow.exceptions.InvalidQueryFieldException;
import com.platum.restflow.exceptions.RestflowQueryException;

public class QueryFilter {

    private Object leftToken;
    
    private Object rightToken;
    
    private QueryOperation operation;
    	
	public QueryFilter() {
		
	}
	
	protected QueryFilter(Object leftToken, QueryOperation operation, Object rightToken) {
		this.leftToken = leftToken;
		this.operation = operation;
		this.rightToken = rightToken;
		assertComposedOperation(leftToken, operation, rightToken);
	}
	
	public static QueryFilter fromJson(String json) {
		if(StringUtils.isEmpty(json)) {
			return new QueryFilter();
		}
		ObjectMapper mapper = new ObjectMapper();
		try {
			@SuppressWarnings("unchecked")
			Map<String, Object> map = mapper.readValue(json, LinkedHashMap.class);	
			return fromMap(map);
		} catch (IOException e) {
			throw new RestflowQueryException("Could not parse json query.", e);
		}
	}
	
	public static QueryFilter fromMap(Map<String, Object> map) {
		if(map == null || map.isEmpty()) {
			return new QueryFilter();
		}
		return fromMap(map, null);
	}

	@SuppressWarnings("unchecked")
	protected static QueryFilter fromMap(Map<String, Object> map, final String rootField) {
		QueryFilter filter = null;
		Set<Entry<String, Object>> entries = map.entrySet();
		for(Entry<String, Object> entry : entries) {
			 String field = entry.getKey();
			 QueryOperation operation = QueryOperation.fromString(field);
			 Object value = entry.getValue();
			 Map<String, Object> valueMap = value instanceof Map<?, ?>
			 								?  (Map<String, Object>) value : null;
			 if(operation != null) {
				 if(operation.isAndOr()) {
					 QueryFilter right = fromMap(valueMap);
					 filter = createOrAppend(filter, right, operation);
				 } else {
					 if(StringUtils.isEmpty(rootField)) {
						 throw new RestflowQueryException("At this stage field cannot be null.["+entry+"]");
					 }
					 filter = createOrAppend(filter, create(rootField, operation, value));
				 }
			 } else if(valueMap != null) {
				 filter = createOrAppend(filter, fromMap(valueMap, field));
			 } else {
				 if(operation == null) {
					 operation = QueryOperation.EQUAL;
				 }
				 filter = createOrAppend(filter, create(field, operation, value));
			 }
		}
		return filter;
	}
	
	protected static QueryFilter createOrAppend(QueryFilter filter, QueryFilter otherFilter) {
		return filter == null
				? otherFilter 
				: filter.logicCondition(QueryOperation.AND, otherFilter);
	}
	
	protected static QueryFilter createOrAppend(QueryFilter filter, QueryFilter otherFilter, QueryOperation operation) {
		return filter == null
				? otherFilter 
				: filter.logicCondition(operation, otherFilter);
	}
				
	public QueryFilter equal(String field, Object value) {
		filter(field, QueryOperation.EQUAL, value);
		return this;
	}
	
	public QueryFilter notEqual(String field, Object value) {
		filter(field, QueryOperation.NOT_EQUAL, value);
		return this;
	}

	public QueryFilter greaterThan(String field, Object value) {
		filter(field, QueryOperation.GREATER_THAN, value);
		return this;
	}
	
	public QueryFilter greaterThanOrEqual(String field, Object value) {
		filter(field, QueryOperation.GREATER_THAN_OR_EQUAL, value);
		return this;
	}
	
	public QueryFilter lessThan(String field, Object value) {
		filter(field, QueryOperation.LESS_THAN, value);
		return this;
	}
	
	public QueryFilter lessThanOrEqual(String field, Object value) {
		filter(field, QueryOperation.LESS_THAN_OR_EQUAL, value);
		return this;
	}
	
	public QueryFilter like(String field, String value) {
		filter(field, QueryOperation.LIKE, value);
		return this;
	}
	
	public QueryFilter notLike(String field, String value) {
		filter(field, QueryOperation.NOT_LIKE, value);
		return this;
	}
	
	public QueryFilter in(String field, Object... value) {
		filter(field, QueryOperation.IN, value);
		return this;
	}
	
	public QueryFilter notIn(String field, Object... value) {
		filter(field, QueryOperation.NOT_IN, value);
		return this;
	}
	
	public QueryFilter filter(String field, QueryOperation operation, Object value) {
		if(!composed()) {
			this.leftToken = field;
			this.operation = operation == null ? QueryOperation.EQUAL : operation;
			this.rightToken = this.operation.in(QueryOperation.IN, QueryOperation.NOT_IN) 
					 				&& !value.getClass().isArray()
					 				? new Object[]{value} : value;		
		} else {
			and(create(field, operation, value));
		}
		return this;
	}
	
	public QueryFilter or(QueryFilter... filters) {
		return logicCondition(QueryOperation.OR, filters);
	}
	
	public QueryFilter and(QueryFilter... filters) {
		return logicCondition(QueryOperation.AND, filters);
	}
	
	public boolean composed() {
		return (leftToken != null && rightToken != null && operation != null);
	}
	
	public Object leftToken() {
		return leftToken;
	}
	
	public QueryOperation operation() {
		return operation;
	}
	
	public Object rightToken() {
		return rightToken;
	}

	@Override
	public String toString() {
		return "QueryFilter [leftToken=" + leftToken + ", rightToken=" + rightToken + ", operation=" + operation + "]";
	}

	protected QueryFilter logicCondition(QueryOperation operation, QueryFilter... filters) {
		assertComposedOperation(leftToken, operation, rightToken);
		leftToken = create(leftToken, this.operation, this.rightToken);
		this.operation = operation;
		rightToken = create(operation, filters);
		return this;
	}
		
	protected static QueryFilter create(QueryOperation operation, QueryFilter[] tokens) {
		if(tokens != null && tokens.length > 0) {
			QueryFilter leftToken = null;
			QueryFilter rightToken = null ;	
			for(int i = 0; i < tokens.length; i++) {
				if(leftToken == null || i == 0) {
					leftToken = tokens[i];
				} else if(rightToken == null) {
					rightToken = tokens[i];
				} else {
					rightToken = create(rightToken, operation, tokens[i]);
				}	
			}
			if(rightToken == null) {
				return leftToken;
			}
			return create(leftToken, operation, rightToken);
		} 
		return null;
	}
	
	protected static QueryFilter create(Object leftToken, QueryOperation operation, Object rightToken) {
		assertComposedOperation(leftToken, operation, rightToken);
		if((leftToken instanceof QueryFilter 
				&& rightToken instanceof QueryFilter && operation.isAndOr())
			|| (leftToken instanceof String 
				&& !(rightToken instanceof QueryFilter) && !operation.isAndOr() 
					&& !operation.isAggregation())) {
				return new QueryFilter(leftToken, operation, rightToken);
		} 
		throw new InvalidQueryFieldException("Invalid fields ["+leftToken.toString()+", "+rightToken.toString()+"] "
				+ "or operation ["+operation.toString()+"].");
	}
	
	private static void assertComposedOperation(Object leftToken, QueryOperation operation, Object rightToken) {
		if(leftToken == null || operation == null) {
			throw new RestflowQueryException("Invalida query requested");
		}
	}
	
}

