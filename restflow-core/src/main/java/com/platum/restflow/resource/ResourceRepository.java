package com.platum.restflow.resource;

import java.util.List;

import com.platum.restflow.AuthMetadata;
import com.platum.restflow.resource.query.QueryField;
import com.platum.restflow.resource.transaction.RepositoryTransaction;

public interface ResourceRepository<T> extends ResourceComponent<T> {
		
	T get(ResourceMethod method, Params params);
	
	List<ResourceObject> find(ResourceMethod method, Params params, QueryField... fields);
	
	long count(ResourceMethod method, Params params);
	
	T insert(ResourceMethod method, T object);
	
	T update(ResourceMethod method, T object);
	
	T insert(ResourceMethod method, T object, Params params);
	
	T update(ResourceMethod method, T object, Params params);
		
	void delete(ResourceMethod method, T object);
	
	void delete(ResourceMethod method, Params params);
	
	void batchUpdate(ResourceMethod method, List<T> objects);
	
	<E> RepositoryTransaction<E> newTransaction();
	
	boolean hasTransationSupport();
	
	<E> ResourceRepository<T> withTransaction(RepositoryTransaction<E> transaction);
	
	Resource resource();
	
	ResourceRepository<T> withAuthorization(AuthMetadata auth);
	
}
