package com.platum.restflow;

import java.util.List;
import java.util.Locale;
import java.util.Locale.LanguageRange;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.platum.restflow.exceptions.RestflowException;
import com.platum.restflow.i18n.MessageProvider;
import com.platum.restflow.i18n.impl.ResourceBundleMessageProvider;

import io.vertx.core.http.HttpServerRequest;

public class RestflowContext {

	private final  Logger logger = LoggerFactory.getLogger(getClass());
	
	private static final String DEFAULT_MESSAGES_NAME = "messages";
	
	private static final String LOCALE_HEADER = "Accept-Language";
	
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
	
	public String getLangRequestFromRequest(HttpServerRequest request) {
		return request.getHeader(LOCALE_HEADER);
	}
	
	public String getLangMessage(Throwable e, String langRequest) {
		String msg = null;
		if(StringUtils.isEmpty(langRequest)) {
			msg = messageProvider.getMessage(e.getClass().getName());
		} else {
			try {
				List<LanguageRange> langs = Locale.LanguageRange.parse(langRequest);
				for(LanguageRange lang : langs) {
					Object[] params = null;
					if(e instanceof RestflowException) {
						params = ((RestflowException) e).params();
					}
					msg = messageProvider.getMessage(e.getClass().getName(), 
							Locale.forLanguageTag(lang.getRange()), params);
					if(StringUtils.isNotEmpty(msg)) {
						if(logger.isDebugEnabled()) {
							logger.debug("Message from locale: "+msg);
						}
						break;
					}
				}				
			} catch(Throwable ex) {
				if(logger.isWarnEnabled()) {
					logger.warn("Unable to convert message to locale due to exception.", ex);
				}
			}
		}
		return StringUtils.isEmpty(msg) ? e.getMessage() : msg;
	}
	
	public String getLangMessage(String message, String langRequest, Object... params) {
		String msg = null;
		if(StringUtils.isEmpty(langRequest)) {
			msg = messageProvider.getMessage(message, params);
		} else {
			try {
				List<LanguageRange> langs = Locale.LanguageRange.parse(langRequest);
				for(LanguageRange lang : langs) {
					msg = messageProvider.getMessage(message, 
							Locale.forLanguageTag(lang.getRange()), params);
					if(StringUtils.isNotEmpty(msg)) {
						if(logger.isDebugEnabled()) {
							logger.debug("Message from locale: "+msg);
						}
						break;
					}
				}				
			} catch(Throwable ex) {
				if(logger.isWarnEnabled()) {
					logger.warn("Unable to convert message to locale due to exception.", ex);
				}
			}
		}
		return StringUtils.isEmpty(msg) ? message : msg;
	}

}
