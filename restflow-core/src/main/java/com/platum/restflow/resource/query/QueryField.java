package com.platum.restflow.resource.query;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.platum.restflow.exceptions.InvalidQueryFieldException;

public class QueryField {
	
	private String field;
	
	private QueryOperation measure;
	
	private String stringRep;
	
	public QueryField(String field, QueryOperation measure) {
		field(field);
		measure( measure);
	}
	
	public QueryField(String field) {
		field(field);
	}
	
	public static QueryField fromString(String fieldStr) {
		if(StringUtils.isEmpty(fieldStr)) {
			throw new InvalidQueryFieldException("Field string cannot be empty.");
		}
		Pattern pattern = Pattern.compile("\\$([A-Za-z0-9\\-\\_]+)\\(([A-Za-z0-9\\-\\_]+)\\)");
		Matcher matcher = pattern.matcher(fieldStr);
		if(matcher.find()) {
			String measure = matcher.group(1);
			String field = matcher.group(2);
			QueryOperation aggrOp = QueryOperation.fromString("$"+measure);
			if(aggrOp == null) {
				throw new InvalidQueryFieldException("Field measure ["+measure+"] is not supported.");
			}
			return new QueryField(field, aggrOp);
		} else {
			return new QueryField(fieldStr);
		}
	}
	
	public QueryField field(String field) {
		this.field = field;
		return this;
	}
	
	public String field() {
		return field;
	}
	
	public QueryField measure(QueryOperation measure) {
		this.measure = measure;
		return this;
	}
	
	public QueryOperation measure() {
		return measure;
	}
	
	public String toStringRepresentation() {
		if(stringRep == null) {
			if(measure != null) {
				stringRep = measure.toString().toLowerCase()+
					   StringUtils.capitalize(field);
			} else {
				stringRep = field;
			}
		}
		return stringRep;
	}
		
}
