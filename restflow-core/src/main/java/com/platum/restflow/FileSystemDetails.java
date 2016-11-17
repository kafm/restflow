package com.platum.restflow;

import java.util.Map;
import java.util.Properties;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.platum.restflow.resource.xml.KeyValuePropertiesAdapter;

@XmlRootElement(name = "filesystem")
@XmlAccessorType (XmlAccessType.FIELD)
public class FileSystemDetails {
		
	@XmlAttribute(name = "name")
	private String name;
	
	@XmlAttribute(name = "class")
	private String implClass;
	
	@XmlJavaTypeAdapter(KeyValuePropertiesAdapter.class)
	private Properties properties;
	
	public String getName() {
		return name;
	}

	public FileSystemDetails setName(String name) {
		this.name = name;
		return this;
	}

	public Properties getProperties() {
		return properties;
	}

	public FileSystemDetails setProperties(Properties properties) {
		this.properties = properties;
		return this;
	}
	
	public FileSystemDetails setProperties(Map<String, Object> properties) {
		this.properties = new Properties();
		this.properties.putAll(properties);
		return this;
	}

	public String getImplClass() {
		return implClass;
	}

	public FileSystemDetails setImplClass(String implClass) {
		this.implClass = implClass;
		return this;
	}

}
