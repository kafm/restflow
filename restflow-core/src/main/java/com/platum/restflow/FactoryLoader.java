package com.platum.restflow;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.platum.restflow.utils.ResourceUtils;

public class FactoryLoader {

	private static final String FACTORY_PROPERTIES_FILE = "META-INF/restflow.factories";

	private static final Logger logger = LoggerFactory.getLogger(FactoryLoader.class);
	
	private static Map<String, List<String>> impls;
	
	public static void load() {
		List<URL> urls = ResourceUtils.getUrlsFromClassPath(FACTORY_PROPERTIES_FILE);
		if(urls != null && !urls.isEmpty()) {
			impls = new HashMap<>();
			urls.stream().forEach(factoryDef -> {
				Properties properties = new Properties();
				try {
					properties.load(factoryDef.openStream());
					properties.keySet().stream().forEach(key -> {
						String implNames = properties.getProperty(key.toString());
						if(StringUtils.isNotEmpty(implNames)) {
							List<String> implList = impls.containsKey(key) ? impls.get(key) 
									: new ArrayList<>();
							implList.addAll(Arrays.asList(implNames.split(",")));
							impls.put(key.toString(), implList);
						}
					});
				} catch (Exception e) {
					if(logger.isWarnEnabled()) {
						logger.warn("Unable to load factory properties.", e);
					}
				}
			});
			impls = new ImmutableMap.Builder<String, List<String>>()
					  .putAll(impls)
					  .build();	
		}
	}
	
	public static List<String> getImpls(String className) {
		Validate.notEmpty(className);
		if(impls != null) {
			return impls.get(className);
		}
		return null;
	}
	
}


