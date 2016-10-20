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
public class ResourceMethod implements Serializable, Cloneable {
	
	private static final long serialVersionUID = 1860661592152932194L;
	
	@XmlAttribute
	private String name;
	
	private String url;

	private String query;
	
	private boolean wrap;

	@XmlAttribute
	private boolean internal;
	
	@XmlAttribute
	private boolean upload;
	
	@XmlTransient
	private String[] params;
	
	private String roles;
	
	@XmlTransient
	private String[] rolesArr;

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
		return url;
	}

	public ResourceMethod setUrl(String impl) {
		this.url = impl;
		return this;
	}

	public boolean isInternal() {
		return internal;
	}

	public ResourceMethod setInternal(boolean internal) {
		this.internal = internal;
		return this;
	}

	public boolean isUpload() {
		return upload;
	}

	public ResourceMethod setUpload(boolean upload) {
		this.upload = upload;
		return this;
	}
	
	public ResourceMethod setParams(String[] params) {
		this.params = params;
		return this;
	}

	public String[] getParams() {
		return params;
	}

	public String getRoles() {
		return roles;
	}
	
	public boolean hasRoles() {
		return roles != null && StringUtils.isNotEmpty(roles);
	}

	public ResourceMethod setRoles(String roles) {
		this.roles = roles;
		rolesArr = stringToArray(roles);
		return this;
	}
	
	public String[] getRolesAsArray() 
	{
		return rolesArr;
	}
	
	public boolean isWrap() {
		return wrap;
	}

	public ResourceMethod setWrap(boolean wrap) {
		this.wrap = wrap;
		return this;
	}

	@Override
	public String toString() {
		return "ResourceMethod [name=" + name + ", query=" + query + ", url=" + url + ", internal=" + internal
				+ ", upload=" + upload + ", params=" + params + Arrays.toString(params) + ", roles="
				+ roles + ", rolesArr=" + Arrays.toString(rolesArr) + "]";
	}
	
	public ResourceMethod clone() {
		try {
			return (ResourceMethod) super.clone(); 
		} catch(Throwable e) {
			throw new RuntimeException("Error when clonning object.", e);
		}
	}

	private String[] stringToArray(String params) 
	{
		if(!StringUtils.isEmpty(params)) {
			return params.replaceAll(" ", "").split(",");
		}
		return null;
	}
	
}
