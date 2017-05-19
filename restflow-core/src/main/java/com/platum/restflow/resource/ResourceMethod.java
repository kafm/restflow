package com.platum.restflow.resource;

import java.io.Serializable;
import java.util.Arrays;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang3.StringUtils;


@XmlRootElement(name = "method")
@XmlAccessorType (XmlAccessType.FIELD)
public class ResourceMethod extends AbstractMethod implements Serializable, Cloneable {
	
	private static final long serialVersionUID = 1860661592152932194L;
	
	@XmlAttribute
	private String name;
	
	private String url;

	private String query;
	
	private boolean wrap;
	
	private boolean collection = true;
	
	@XmlTransient
	private String[] params;

	public String getName() {
		return StringUtils.isEmpty(name)? url : name;
	}

	public ResourceMethod setName(String name) {
		this.name = name;
		return this;
	}

	public String getQuery() {
		return query;
	}

	public ResourceMethod setQuery(String query) {
		this.query = query;
		return this;
	}

	public String getUrl() {
		return StringUtils.isEmpty(url) ? name : url;
	}

	public ResourceMethod setUrl(String impl) {
		this.url = impl;
		return this;
	}
	
	public ResourceMethod setParams(String[] params) {
		this.params = params;
		return this;
	}

	public String[] getParams() {
		return params;
	}

	public boolean isWrap() {
		return wrap;
	}

	public ResourceMethod setWrap(boolean wrap) {
		this.wrap = wrap;
		return this;
	}

	public boolean isCollection() {
		return collection;
	}

	public void setCollection(boolean collection) {
		this.collection = collection;
	}

	@Override
	public String toString() {
		return "ResourceMethod [name=" + name + ", query=" + query + ", url=" + url + ", internal=" + internal
				+ ", params=" + params + Arrays.toString(params) + ", roles="
				+ getRoles() + ", rolesArr=" + Arrays.toString(getRolesAsArray()) + "]";
	}
	
	public ResourceMethod clone() {
		try {
			return (ResourceMethod) super.clone(); 
		} catch(Throwable e) {
			throw new RuntimeException("Error when clonning object.", e);
		}
	}
	
}
