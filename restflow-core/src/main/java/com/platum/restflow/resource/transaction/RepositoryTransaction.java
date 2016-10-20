package com.platum.restflow.resource.transaction;

public interface RepositoryTransaction<T> {
	
	public RepositoryTransaction<T> connection(T connection);
	
	public T connection();
	
	public void commit();
	
	public void rollback();

}
