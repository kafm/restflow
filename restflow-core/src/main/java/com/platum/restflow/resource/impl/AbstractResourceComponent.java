package com.platum.restflow.resource.impl;

import com.platum.restflow.exceptions.RestflowException;
import com.platum.restflow.resource.ResourceComponent;
import com.platum.restflow.resource.ResourceMetadata;

public abstract class AbstractResourceComponent<T> implements ResourceComponent<T> {
	
	protected ResourceMetadata<T> metadata;

	public AbstractResourceComponent(ResourceMetadata<T> metadata) {
		if(metadata == null) {
			throw new RestflowException("Resource metadata cannot be null.");
		}
		this.metadata = metadata;
	}
	
	public ResourceMetadata<T> metadata() {
		return metadata;
	}
}