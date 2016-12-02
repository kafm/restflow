package com.platum.restflow.resource;

import java.util.List;
import java.util.Map;

import com.platum.restflow.AuthMetadata;
import com.platum.restflow.resource.annotation.HookInterceptor;
import com.platum.restflow.resource.annotation.HookType;
import com.platum.restflow.resource.query.QueryFilter;
import com.platum.restflow.resource.query.QueryModifier;
import com.platum.restflow.resource.transaction.RepositoryTransaction;
import com.platum.restflow.utils.promise.Promise;



public interface ResourceService<T> extends ResourceComponent<T> {
		
	<I> Promise<T> get(I id);
	
	Promise<T> get(ResourceMethod method, Params params);
	
	Promise<List<ResourceObject>> find(ResourceMethod method, Params params);
	
	Promise<List<ResourceObject>> find(QueryFilter filter);
	
	Promise<List<ResourceObject>> find(QueryFilter filter, QueryModifier modifier);
	
	Promise<List<ResourceObject>> find(ResourceMethod method, QueryFilter filter);
	
	Promise<List<ResourceObject>> find(ResourceMethod method, QueryFilter filter, QueryModifier modifier);
	
	Promise<Long> count(QueryFilter filter);
	
	Promise<Long> count(ResourceMethod method, QueryFilter filter);
	
	Promise<T> insert(T object);
	
	Promise<T> insert(ResourceMethod method, T object);
	
	Promise<T> insert(ResourceMethod method, T object, Params params);
	
	Promise<T> update(T object);
	
	Promise<T> update(ResourceMethod method, T object);
	
	Promise<T> update(ResourceMethod method, T object, Params params);
	
	Promise<T> partialUpdate(ResourceMethod method, T object);
	
	Promise<T> partialUpdate(ResourceMethod method, T object, Params params);
			
	<I> Promise<Void> delete(I id);
	
	Promise<Void> delete(ResourceMethod method, Params params); 
	
	Promise<Void> delete(ResourceMethod method, T object); 
	
	Promise<Void> batchUpdate(ResourceMethod method, List<T> objects);
	
	ResourceService<T> withTransation(RepositoryTransaction<?> transation);
	
	RepositoryTransaction<?> transaction();
	
	ResourceService<T> withLang(String lang);
	
	String lang();
	
	ResourceService<T> authorization(AuthMetadata auth);
	
	AuthMetadata authorization();
	
	ResourceService<T> hooks(Map<HookType, HookInterceptor> hooks); 
	
	Map<HookType, HookInterceptor> hooks();
	
}
