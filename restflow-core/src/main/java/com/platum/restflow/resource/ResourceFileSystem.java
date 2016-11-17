package com.platum.restflow.resource;

import com.platum.restflow.utils.promise.Promise;

public interface ResourceFileSystem {

	public Promise<Void> create(ResourceFile file);
			
	public Promise<Void> save(ResourceFile file);
		
	public Promise<Void> destroy(Object id);

	public Promise<ResourceFile> get(Object id);
	
}
