package com.platum.restflow.resource.query;

import java.util.Collections;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Maps;

public enum QueryOperation {
	AND, OR, 
	EQUAL, NOT_EQUAL, 
	GREATER_THAN, GREATER_THAN_OR_EQUAL,
	LESS_THAN, LESS_THAN_OR_EQUAL,
	LIKE, NOT_LIKE, IN, NOT_IN,
	SUM, MIN, MAX, AVG, COUNT, 
	DAY, WEEK, MONTH, QUARTER, YEAR;
	
	public static final Map<String, QueryOperation> OPERATIONS;
	
	static
    {
		Map<String, QueryOperation> map = Maps.newHashMap();
			map.put("$and", AND);
			map.put("$or", OR);
			map.put("$eq", EQUAL);
			map.put("$ne", NOT_EQUAL);
			map.put("$gt", GREATER_THAN);
			map.put("$gte", GREATER_THAN_OR_EQUAL);
			map.put("$lt", LESS_THAN);
			map.put("$lte", LESS_THAN_OR_EQUAL);
			map.put("$li", LIKE);
			map.put("$nl", NOT_LIKE); 
			map.put("$in", IN);
			map.put("$nin", NOT_IN); 		
			map.put("$sum", SUM);
			map.put("$min", MIN);
			map.put("$max", MAX);
			map.put("$avg", AVG);
			map.put("$count", COUNT);
			map.put("$day", DAY);
			map.put("$week", WEEK);
			map.put("$month", MONTH);
			map.put("$quarter", QUARTER);
			map.put("$year", YEAR);
		OPERATIONS = Collections.unmodifiableMap(map);
    }

    public static QueryOperation fromString(String input) {
    	if(StringUtils.isNotEmpty(input)) {
    		String op = input.contains(":") ? input.split(":")[0] : input;
    		return OPERATIONS.get(op);
    	}
    	return null;
    	
    
    }
    
    public boolean isAggregation() {
    	return (this.equals(COUNT) || this.equals(SUM) 
    			|| this.equals(MIN)
    			|| this.equals(MAX) || this.equals(AVG));
    }
    
    public boolean isDate() {
    	return (this.equals(YEAR) || this.equals(QUARTER) 
    			|| this.equals(MONTH)
    			|| this.equals(WEEK) || this.equals(DAY));    	
    }
    
    public boolean isAndOr() {
    	return (this.equals(AND) || this.equals(OR));
    }
    
    public boolean in(QueryOperation... operations) {
    	for(QueryOperation operation : operations) {
    		if(this.equals(operation)) {
    			return true;
    		}
    	}
    	return false;
    }

}
