package com.platum.restflow;

import java.util.Arrays;
import java.util.List;

public abstract class RestflowDefaultConfig {
	
	public static final String DEFAULT_ID_PARAM = "id";
	
	public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
	
	protected static final String[] LOG_EXT = {"xml","properties","yml","yaml"};
	
	protected static final List<String> XML_EXTS = Arrays.asList("xml","XML");
	
	protected static final List<String> ALTERNATIVE_LOG_LOCATIONS = Arrays.asList(
			"file:logback.",
			"file:config/logback.",
			"file:${RESTFLOW}/logback.",
			"file:${RESTFLOW}/config/logback.",		
			"file:${RESTFLOW}/config/logback."
	);
	
	protected static final List<String> DEFAULT_CONFIG_PATHS = Arrays.asList(
			"classpath:"+RestflowEnvironment.DEFAULT_CONFIG_FILE,
			"classpath:config/"+RestflowEnvironment.DEFAULT_CONFIG_FILE, 
			"file:./"+RestflowEnvironment.DEFAULT_CONFIG_FILE,
			"file:./config/"+RestflowEnvironment.DEFAULT_CONFIG_FILE,
			"file:${RESTFLOW}/"+RestflowEnvironment.DEFAULT_CONFIG_FILE,
			"file:${RESTFLOW}/config/"+RestflowEnvironment.DEFAULT_CONFIG_FILE	
	);
			
	protected static final List<String> DEFAULT_MODELS_PATH = Arrays.asList(
			"file:./models/"
			, "file:${RESTFLOW}/models/"
			, "classpath:models/"
	);
	
	protected static final List<String> DEFAULT_MESSAGES_PATH = Arrays.asList(
			"file:./i18n/"
			, "file:./i18n/messages/"
			, "file:${RESTFLOW}/i18n/"
			, "file:${RESTFLOW}/i18n/messages/"
			, "classpath:"
	);

}
