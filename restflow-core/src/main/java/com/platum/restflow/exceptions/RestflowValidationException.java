package com.platum.restflow.exceptions;

import com.platum.restflow.resource.property.ResourceProperty;

@SuppressWarnings("serial")
public class RestflowValidationException extends RestflowException {
	
	protected ResourceProperty contextProperty;
	
	public RestflowValidationException(String message) {
		super(message);
	}
	
	public RestflowValidationException(String message, Throwable e) {
		super(message, e);
	}
	
	public ResourceProperty getContextProperty() {
		return contextProperty;
	}

	public RestflowValidationException setContextProperty(ResourceProperty contextProperty) {
		this.contextProperty = contextProperty;
		return this;
	}

}
