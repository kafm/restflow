package com.platum.restflow.resource.impl.jdbc;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

@XmlAccessorType (XmlAccessType.FIELD)
public class DatabaseSqlErrors {
	
	private String names;
	
	@XmlElementWrapper(name = "errors")
	@XmlElement(name = "map")
	private List<SqlErrorCodeMap> errorCodeMap;

	public String getNames() {
		return names;
	}

	public String[] getNamesAsArray() {
		if(names == null) return new String[0];
		return names.split(",");
	}

	public DatabaseSqlErrors setName(String name) {
		this.names = name;
		return this;
	}

	public List<SqlErrorCodeMap> getErrorCodeMap() {
		return errorCodeMap;
	}

	public DatabaseSqlErrors setErrorCodeMap(List<SqlErrorCodeMap> errorCodeMap) {
		this.errorCodeMap = errorCodeMap;
		return this;
	}

	@Override
	public String toString() {
		return "DatabaseSqlErrors [names=" + names + ", errorCodeMap=" + errorCodeMap + "]";
	}
	
}
