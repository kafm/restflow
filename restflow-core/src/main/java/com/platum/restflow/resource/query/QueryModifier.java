package com.platum.restflow.resource.query;

import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.platum.restflow.exceptions.RestflowQueryException;

public class QueryModifier {
	
	private QueryField[] fields;
	
	private SortModifier[] sort;
	
	private int limit = -1;
	
	private int offset = -1;
	
	public static QueryModifier fromJson(String fields, String sort, String limit, String offset) {
		QueryModifier modifier = new QueryModifier();
		try {
			if(StringUtils.isNotEmpty(fields)) {
				modifier.fields(fields.split(","));
			}
			if(StringUtils.isNotEmpty(sort)) {
				modifier.sort(sort.split(","));
			}
			if(StringUtils.isNotEmpty(limit)) {
				modifier.limit = NumberUtils.toInt(limit, -1);
			}
			if(StringUtils.isNotEmpty(offset)) {
				modifier.offset = NumberUtils.toInt(offset, -1);
			}
		} catch(Throwable e) {
			throw new RestflowQueryException("Could not parse modifier request.", e);
		} 
		return modifier;
	}
	
	public QueryField[] fields() {
		return fields;
	}

	public QueryModifier fields(QueryField[] fields) {
		this.fields = fields;
		return this;
	}

	public QueryModifier fields(String[] fields) {
		if(fields != null && fields.length > 0) {
			this.fields = new QueryField[fields.length];
			IntStream.range(0, fields.length)
			.forEach(i -> {
				this.fields[i] = QueryField.fromString(fields[i]);
			});
		}
		return this;
	}
	
	public SortModifier[] sort() {
		return sort;
	}

	public QueryModifier sort(String[] sort) {
		if(sort != null && sort.length > 0) {
			this.sort = new SortModifier[sort.length];
			IntStream.range(0, sort.length)
			.forEach(i -> {
				this.sort[i] = SortModifier.fromString(sort[i]);
			});
		}
		return this;
	}

	public int limit() {
		return limit;
	}

	public QueryModifier limit(int limit) {
		this.limit = limit;
		return this;
	}

	public int offset() {
		return offset;
	}

	public QueryModifier offset(int offset) {
		this.offset = offset;
		return this;
	}
	
}
