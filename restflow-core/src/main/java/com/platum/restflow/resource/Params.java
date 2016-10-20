package com.platum.restflow.resource;

import java.util.HashMap;
import java.util.Map;

public class Params {
		
	private Map<String, Object> params = new HashMap<>();

	public Map<String, Object> getParams() {
		return params;
	}

	public Params setParams(Map<String, Object> params) {
		this.params = params;
		return this;
	}
	
	public Params addParam(String param, Object value) {
		params.put(param, value);
		return this;
	}
	
	public boolean isEmpty() {
		return params.isEmpty();
	}
	
	public String[] getParamNames() {
		return params.keySet().toArray(new String[params.size()]);
	}

	public void merge(Params params) {
		if(params != null && !params.isEmpty()) {
			this.params.putAll(params.params);	
		}
	}

	public Object size() {
		return params.size();
	}

	@Override
	public String toString() {
		return "Params [params=" + params + "]";
	}

}
