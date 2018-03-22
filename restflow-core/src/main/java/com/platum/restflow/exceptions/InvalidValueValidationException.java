package com.platum.restflow.exceptions;

import com.platum.restflow.resource.property.ResourceProperty;

@SuppressWarnings("serial")
public class InvalidValueValidationException extends RestflowValidationException {

	
	public InvalidValueValidationException(String message) {
		super(message);
	}

	public ResourceProperty getContextProperty() {
		return contextProperty;
	}

	public InvalidValueValidationException setContextProperty(ResourceProperty contextProperty) {
		this.contextProperty = contextProperty;
		return this;
	}
	
}
