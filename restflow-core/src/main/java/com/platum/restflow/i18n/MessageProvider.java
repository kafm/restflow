package com.platum.restflow.i18n;

import java.util.Locale;
import java.util.Map;

public interface MessageProvider {
	
	Map<String, String> getEntries();
	
	Map<String, String> getEntries(String baseName);
	
	Map<String, String> getEntries(String baseName, Locale locale);
	
	String getMessage(String message);
	
	String getMessage(String message, Object... params);
	
	String getMessage(String message, Locale locale);
	
	String getMessage(String message, String baseName, Locale locale);
	
	String getMessage(String message, String baseName, Object... params);
	
	String getMessage(String message,  Locale locale, Object... params);
	
	String getMessage(String message, String baseName, Locale locale, Object... params);
	
	void setAdditionalClassLoader(ClassLoader classLoader);
	
	String getBaseName();

	public Locale getDefaultLocale();
}
