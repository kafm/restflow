package com.platum.restflow.resource.impl.jdbc;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "sql-exceptions-mapping")
@XmlAccessorType (XmlAccessType.FIELD)
public class ErrorMapping {

	@XmlElement(name="database")
	List<DatabaseSqlErrors> errors;

	public List<DatabaseSqlErrors> getErrors() {
		return errors;
	}

	public void setErrors(List<DatabaseSqlErrors> errors) {
		this.errors = errors;
	}
	
}
