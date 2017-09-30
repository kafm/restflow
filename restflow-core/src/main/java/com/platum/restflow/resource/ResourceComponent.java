package com.platum.restflow.resource;

public interface ResourceComponent<T> extends Cloneable {
	
	ResourceMetadata<T> metadata();
	
	void close();
}