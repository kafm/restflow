package com.platum.restflow.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import com.platum.restflow.RestflowEnvironment;
import com.platum.restflow.exceptions.RestflowException;

public class RestflowEnvironmentImpl implements RestflowEnvironment{
		
	private Properties properties;
	
	public RestflowEnvironmentImpl() {
		properties = new Properties();
	}

	@Override
	public String getProperty(String property) {
		return properties.getProperty(property);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getProperty(String property, Class<?> clazz) {
		Validate.notNull(clazz);
		Validate.notBlank(property);
		String value = getProperty(property);
		if(StringUtils.isEmpty(value)) {
			return null;
		}
		return (T) ConvertUtils.convert(value, clazz);
	}

	@Override
	public Properties getPropertiesByPrefix(String prefix) {
		Validate.notEmpty(prefix);
		Properties prefixProperties = new Properties();
		Set<String> propertiesName = properties.stringPropertyNames();
		for(String name : propertiesName) {
			if(name.startsWith(prefix)) {
				prefixProperties.setProperty(name, properties.getProperty(name));
			}
		}
		return prefixProperties;
	}

	@Override
	public RestflowEnvironment setProperty(String property, String value) {
		properties.setProperty(property, value);
		return this;
	}

	@Override
	public RestflowEnvironment setProperties(Properties properties) {
		properties.putAll(properties);
		return this;
	}

	@Override
	public RestflowEnvironment load(InputStream in) {
		Validate.notNull(in);
		try {
			properties.load(in);
		} catch (IOException e) {
			throw new RestflowException("Unable to load messages from input stream.");
		}
		return this;
	}
	
	@Override
	public Map<String, Object> getPropertiesAsMap(Properties properties) {
		Map<String, Object> map = new HashMap<>();
		Set<String> propertiesName = properties.stringPropertyNames();
		propertiesName.stream()
						.forEach(propertyName -> {
							createTree(propertyName.split("\\.")
									, properties.get(propertyName), map);
						});		
		return map;
	}

	@SuppressWarnings("unchecked")
	private void createTree(String[] keys, Object propertyValue, Map<String, Object> map) {
		int keysLength = keys.length;
		if(keys.length == 1) {
			map.put(keys[0], propertyValue);
		} else {
			Map<String, Object> valueMap = (Map<String, Object>) map.get(keys[0]);
			if(valueMap == null) {
				valueMap = new HashMap<>();
				map.put(keys[0], valueMap);
			}
			createTree(Arrays.copyOfRange(keys, 1, keysLength), propertyValue, valueMap);
		}
	}
	
}
