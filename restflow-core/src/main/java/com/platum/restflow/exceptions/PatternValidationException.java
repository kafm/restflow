package com.platum.restflow.exceptions;

import com.platum.restflow.resource.property.ResourceProperty;

@SuppressWarnings("serial")
public class PatternValidationException extends RestflowValidationException {
	
	public PatternValidationException(String message) {
		super(message);
	}

	public ResourceProperty getContextProperty() {
		return contextProperty;
	}

	public PatternValidationException setContextProperty(ResourceProperty contextProperty) {
		this.contextProperty = contextProperty;
		return this;
	}
	
}
