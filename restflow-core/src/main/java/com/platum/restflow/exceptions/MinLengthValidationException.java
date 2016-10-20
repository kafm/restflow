package com.platum.restflow.exceptions;

import com.platum.restflow.resource.property.ResourceProperty;

@SuppressWarnings("serial")
public class MinLengthValidationException extends RestflowValidationException {
	
	private ResourceProperty contextProperty;
	
	public MinLengthValidationException(String message) {
		super(message);
	}

	public ResourceProperty getContextProperty() {
		return contextProperty;
	}

	public MinLengthValidationException setContextProperty(ResourceProperty contextProperty) {
		this.contextProperty = contextProperty;
		return this;
	}
	
}
