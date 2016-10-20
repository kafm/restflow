package com.platum.restflow.resource.impl.jdbc;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;

import com.platum.restflow.exceptions.RestflowException;

@XmlAccessorType (XmlAccessType.FIELD)
public class SqlErrorCodeMap {
	
	@XmlElement
    @XmlList
	private List<Integer> codes;
	
	private Class<? extends RestflowException> exception;
	
	private String message;

	public List<Integer> getCode() {
		return codes;
	}

	public void setCodes(List<Integer> codes) {
		this.codes = codes;
	}

	public Class<? extends RestflowException> getException() {
		return exception;
	}

	public void setException(Class<? extends RestflowException> exception) {
		this.exception = exception;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
}
