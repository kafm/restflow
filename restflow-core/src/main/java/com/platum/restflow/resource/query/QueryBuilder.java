package com.platum.restflow.resource.query;

import com.platum.restflow.RestflowHttpMethod;
import com.platum.restflow.resource.Params;
import com.platum.restflow.resource.Resource;
import com.platum.restflow.resource.ResourceMethod;

public interface QueryBuilder extends Cloneable {
	
	Params params();
	
	QueryBuilder params(Params params);
	
	ResourceMethod method();
	
	QueryBuilder method(ResourceMethod method);
	
	Resource resource();
	
	QueryBuilder resource(Resource resource);
	
	boolean supportsCrudAutoGen();
	
	QueryBuilder generate(RestflowHttpMethod httpMethod);
	
	QueryBuilder generateInsert();
	
	QueryBuilder generateInsertIfNotFound();
	
	QueryBuilder generateUpdate();
	
	QueryBuilder generateUpdateIfNotFound();
	
	QueryBuilder generateDelete();
	
	QueryBuilder generateDeleteIfNotFound();
	
	QueryBuilder generateGet();
	
	QueryBuilder generateGetIfNotFound();
	
	QueryBuilder generateGetById();
	
	QueryBuilder generateGetByIdIfNotFound();
	
	QueryBuilder generateCount();
	
	QueryBuilder generateCountIfNotFound();
	
	QueryBuilder wrap();
	
	QueryBuilder wrapIfAllowed();
	
	QueryBuilder completeQueryWithFilter(QueryFilter filter);
	
	QueryBuilder completeQueryWithModifier(QueryModifier modifier);
	
}
