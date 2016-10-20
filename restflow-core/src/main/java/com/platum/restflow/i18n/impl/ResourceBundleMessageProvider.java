package com.platum.restflow.i18n.impl;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import org.apache.commons.lang3.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Maps;
import com.platum.restflow.i18n.MessageProvider;
import com.platum.restflow.utils.ClassUtils;

public class ResourceBundleMessageProvider implements MessageProvider {
	
	private final Log logger = LogFactory.getLog(getClass());
	
	private String baseName;
	
	private ClassLoader classLoader;
	
	private Locale defaultLocale;

	public ResourceBundleMessageProvider(String baseName) {
		this(baseName, Locale.getDefault());
	}
	
	public ResourceBundleMessageProvider(String baseName, Locale defaultLocale) {
		this(baseName, defaultLocale, null);
	}
	
	public ResourceBundleMessageProvider(String baseName, Locale defaultLocale, ClassLoader classLoader) {
		Validate.notEmpty(baseName);
		this.baseName = baseName;
		this.defaultLocale = defaultLocale;
		this.classLoader = classLoader;
	}

	@Override
	public Map<String, String> getEntries() {
		return getEntries(baseName, defaultLocale);
	}

	@Override
	public Map<String, String> getEntries(String baseName) {
		return getEntries(baseName, defaultLocale);
	}

	@Override
	public Map<String, String> getEntries(String baseName, Locale locale) {
		ResourceBundle bundle = getResourceBundle(baseName, locale, classLoader);
		Map<String, String> entries = Maps.newHashMap();
		if(bundle != null) {
			Set<String> keys = bundle.keySet();
			for(String key : keys) {
				entries.put(key, bundle.getString(key));
			}
		}
		return entries;
	}

	@Override
	public String getMessage(String message) {
		return getMessage(message, defaultLocale);
	}

	@Override
	public String getMessage(String message, Locale locale) {
		return getMessage(message, baseName, locale);
	}

	@Override
	public String getMessage(String message, String baseName, Locale locale) {
		return getMessage(message, baseName, locale, (Object[]) null);
	}

	@Override
	public String getMessage(String message, String baseName, Object... params) {
		return getMessage(message, baseName, defaultLocale, params);
	}

	@Override
	public String getMessage(String message, Locale locale, Object... params) {
		return getMessage(message, baseName, locale, params);
	}

	@Override
	public String getMessage(String message, String baseName, Locale locale, Object... params) {
		ResourceBundle bundle = getResourceBundle(baseName, locale, classLoader);
		if(bundle != null) {
			String text = bundle.getString(message);
			if(text != null) {
				if(params == null || params.length == 0) {
					return text;
				} else {
					return MessageFormat.format(text, params);
				}
			}
		}
		return null;
	}
	
	@Override
	public void setAdditionalClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	@Override	
	public String getBaseName() {
		return baseName;
	}

	@Override
	public Locale getDefaultLocale() {
		return defaultLocale;
	}
	
	private ResourceBundle getResourceBundle(String baseName, Locale locale, ClassLoader classLoader) {
		try
		{
			if(locale == null) {
				locale = Locale.getDefault();
			}
			if(classLoader == null) {
				classLoader = ClassUtils.getDefaultClassLoader();
			}
			return ResourceBundle.getBundle(baseName, locale, classLoader);
		}
		catch(Throwable e) {
			if(locale == Locale.ROOT) {
				e.printStackTrace();
				if(logger.isDebugEnabled()) {
					logger.debug("Resource bundle ["+baseName+"] not found.");
				}
				return null;
			} else if(locale == Locale.getDefault()) {
				return getResourceBundle(baseName, Locale.ROOT, classLoader);
			}
			else {
				return getResourceBundle(baseName, Locale.getDefault(), classLoader);
			}
		}
	}
	
	
}
