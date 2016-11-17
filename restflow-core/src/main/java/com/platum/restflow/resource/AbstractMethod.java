package com.platum.restflow.resource;

import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang3.StringUtils;

public abstract class AbstractMethod {
	
	protected boolean internal;
	
	protected boolean authRequired;

	protected String roles;
	
	@XmlTransient
	protected String[] rolesArr;
	
	public boolean isInternal() {
		return internal;
	}

	public AbstractMethod setInternal(boolean internal) {
		this.internal = internal;
		return this;
	}
	
	public String getRoles() {
		return roles;
	}
	
	public boolean hasRoles() {
		return roles != null && StringUtils.isNotEmpty(roles);
	}

	public boolean isAuthRequired() {
		return authRequired;
	}

	public AbstractMethod setAuthRequired(boolean authRequired) {
		this.authRequired = authRequired;
		return this;
	}

	public AbstractMethod setRoles(String roles) {
		this.roles = roles;
		rolesArr = stringToArray(roles);
		return this;
	}
	
	public String[] getRolesAsArray() 
	{
		return rolesArr;
	}
	
	private String[] stringToArray(String params) 
	{
		if(!StringUtils.isEmpty(params)) {
			return params.replaceAll(" ", "").split(",");
		}
		return null;
	}
}
