package com.platum.restflow.exceptions;

import com.platum.restflow.resource.property.ResourceProperty;

@SuppressWarnings("serial")
public class MaxLengthValidationException extends RestflowValidationException {
	
	public MaxLengthValidationException(String message) {
		super(message);
	}

	public ResourceProperty getContextProperty() {
		return contextProperty;
	}

	public MaxLengthValidationException setContextProperty(ResourceProperty contextProperty) {
		this.contextProperty = contextProperty;
		return this;
	}
	
}
