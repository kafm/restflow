package com.platum.restflow.resource;

public interface ResourceComponent<T> {
	
	ResourceMetadata<T> metadata();
	
	void close();
}