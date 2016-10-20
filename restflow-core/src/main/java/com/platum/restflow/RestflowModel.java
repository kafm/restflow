/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.platum.restflow;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.Validate;

import com.platum.restflow.exceptions.ResflowNotExistsException;
import com.platum.restflow.exceptions.RestflowDuplicatedRefException;
import com.platum.restflow.resource.Resource;

/**
 * Class responsible for handling model definitions. 
 * A model it's a representation of a restflow configuration. A restflow configuration it's composed by:
 * 
 * <ul>
 * <li>Resources - REST resources</li>
 * <li>Connections - Configuration of connections to datasources</li>
 * </ul>
 * 
 * Please note that it's not mandatory to have the connections definition in a model. A resource may use a datasource connection defined in another  {@link RestflowModel} instance.  
 * 
 * @author Kevin Martins
 */
@XmlRootElement(name = "model")
@XmlAccessorType (XmlAccessType.FIELD)
public class RestflowModel {
	
	public static final String DEFAULT_VERSION = "v1";
	
	private String version;

	@XmlElementWrapper(name = "datasources")
	@XmlElement(name="datasource")
	private List<DatasourceDetails> datasources;

	@XmlElementWrapper
	@XmlElement(name="resource")
	private List<Resource> resources;
	
	/**
	 * Constructor that creates a new {@link RestflowModel} model instance with default version of 1. 
	 */
	public RestflowModel() {
		version = DEFAULT_VERSION;
	}
	
	/**
	 * Constructor that creates a new {@link RestflowModel} model with version passed as parameter.
	 * @param version
	 */
	public RestflowModel(String version) {
		setVersion(version);
	}
		
	/**
	 * Sets the  version of the model.
	 * @param version model's version to be set
	 * @return the same  {@link RestflowModel} instance
	 * @throws IllegalArgumentException if the version is {@code null}
	 */
	public RestflowModel setVersion(String version) {
		Validate.notEmpty(version);
		this.version = version;
		return this;
	}
	
	/**
	 * Gets the version of the model.
	 * @return the version of the model
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * 
	 * @param datasources
	 * @return
	 */
	public RestflowModel setDatasources(List<DatasourceDetails> datasources) {
		this.datasources = datasources;
		return this;
	}
	
	/*
	 * 
	 */
	public RestflowModel createDatasource(DatasourceDetails datasource) {
		Validate.notNull(datasource, "Cannot add a null datasource.");
		Validate.notEmpty(datasource.getName(), "Datasource name cannot be null or empty.");
		if(datasources == null) {
			datasources = new ArrayList<>();
		} else if(getDatasource(datasource.getName()) != null) {
			throw new RestflowDuplicatedRefException("Datasource ["+datasource.getName()+"] already exists.");
		}
		datasources.add(datasource);
		return this;
	}
	
	/**
	 * 
	 * @param datasource
	 * @return
	 */
	public RestflowModel updateDatasource(DatasourceDetails datasource) {
		Validate.notNull(datasource, "Cannot update a null datasource.");
		Validate.notEmpty(datasource.getName(), "Datasource name cannot be null or empty.");
		if(resources != null) {
			ListIterator<DatasourceDetails> iterator = datasources.listIterator();
			while(iterator.hasNext()) {
				if(iterator.next().getName()
						.equals(datasource.getName())) {
					iterator.set(datasource);
					return this;
				}
			}
		}
		throw new ResflowNotExistsException("Datasource does not exists.");
	}
	
	/**
	 * 
	 * @param name
	 * @return
	 */
	public RestflowModel removeDatasource(String name) {
		datasources.removeIf(datasource -> 
					datasource.getName().equals(name));
		return this;
	}
	
	/**
	 * 
	 * @return
	 */
	public List<DatasourceDetails> getDatasources() {
		return datasources;
	}
	
	/**
	 * 
	 * @param name
	 * @return
	 */
	public DatasourceDetails getDatasource(String name) {
		try {
			return datasources.stream()
				   .filter(datasource -> datasource.getName().equals(name))
				   .findAny()
				   .orElse(null);
		} catch(Throwable e) {
			return null;
		}
	}
	
	/**
	 * Sets model's resources. 
	 * @param resources list of {@link Resource}
	 * @return the same  {@link RestflowModel} instance
	 */
	public RestflowModel setResources(List<Resource> resources) {
		this.resources = resources;
		return this;
	}
		
	/**
	 * 
	 * @param resource
	 * @return
	 */
	public RestflowModel createResource(Resource resource) {
		Validate.notNull(resource, "Cannot add a null resource.");
		Validate.notEmpty(resource.getName(), "Resource name cannot be null or empty.");
		if(resources == null) {
			resources = new ArrayList<>();
		} else if(getResource(resource.getName()) != null) {
			throw new RestflowDuplicatedRefException("Resource ["+resource.getName()+"] already exists.");
		}
		resources.add(resource);
		return this;
	}
	
	/**
	 * 
	 * @param resource
	 * @return
	 */
	public RestflowModel updateResource(Resource resource) {
		Validate.notNull(resource, "Cannot update a null resource.");
		Validate.notEmpty(resource.getName(), "Resource name cannot be null or empty.");
		if(resources != null) {
			ListIterator<Resource> iterator = resources.listIterator();
			while(iterator.hasNext()) {
				if(iterator.next().getName()
						.equals(resource.getName())) {
					iterator.set(resource);
					return this;
				}
			}
		}
		throw new ResflowNotExistsException("Resource does not exists.");
	}
	
	/**
	 * 
	 * @param name
	 * @return
	 */
	public RestflowModel removeResource(String name) {
		if(resources != null) {
			ListIterator<Resource> iterator = resources.listIterator();
			while(iterator.hasNext()) {
				if(iterator.next().getName()
						.equals(name)) {
					iterator.remove();
					return this;
				}
			}
		}
		throw new ResflowNotExistsException("Resource does not exists.");
	}
	
	/**
	 * Get all resources configured in this model.
	 * @return list of {@link Resource} 
	 */
	public List<Resource> getResources() {
		return resources;
	}
	
	/**
	 * 
	 * @param name
	 * @return
	 */
	public Resource getResource(String name) {
		try {
			return resources.stream()
					.filter(resource -> resource.getName().equals(name))
					.findAny()
					.orElse(null);			
		} catch(Throwable e) {
			return null;
		}
	}

	@Override
	public String toString() {
		return "RestflowModel [version=" + version + ", datasources=" + datasources + ", resources=" + resources + "]";
	}
	
		
}
