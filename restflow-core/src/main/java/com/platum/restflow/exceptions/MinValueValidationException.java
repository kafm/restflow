package com.platum.restflow.exceptions;

import com.platum.restflow.resource.property.ResourceProperty;

@SuppressWarnings("serial")
public class MinValueValidationException extends RestflowValidationException {

	public MinValueValidationException(String message) {
		super(message);
	}

	public ResourceProperty getContextProperty() {
		return contextProperty;
	}

	public MinValueValidationException setContextProperty(ResourceProperty contextProperty) {
		this.contextProperty = contextProperty;
		return this;
	}
	
}
