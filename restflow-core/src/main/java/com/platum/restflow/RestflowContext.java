package com.platum.restflow;

import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

import com.platum.restflow.i18n.MessageProvider;
import com.platum.restflow.i18n.impl.ResourceBundleMessageProvider;

public class RestflowContext {

	private static final String DEFAULT_MESSAGES_NAME = "messages";
	
	private MessageProvider messageProvider;
	
	public RestflowContext configMessageProvider() {
		return configMessageProvider(DEFAULT_MESSAGES_NAME);
	}
	
	public RestflowContext configMessageProvider(String baseName) {
		return configMessageProvider(baseName, null);
	}

	public RestflowContext configMessageProvider(String baseName, Locale locale) {
		return configMessageProvider(baseName, locale, null);
	}
	
	public RestflowContext configMessageProvider(String baseName, Locale locale, ClassLoader classLoader) {
		if(StringUtils.isEmpty(baseName)) {
			baseName = DEFAULT_MESSAGES_NAME;
		}
		if(locale != null) {
			if(classLoader != null) {
				messageProvider = new ResourceBundleMessageProvider(baseName, locale, classLoader);
			}
			else {
				messageProvider = new ResourceBundleMessageProvider(baseName, locale);		
			}
		} else {
			messageProvider = new ResourceBundleMessageProvider(baseName);
		}
		return this;
	}
	
	public MessageProvider getMessageProvider() {
		return messageProvider;
	}

}
