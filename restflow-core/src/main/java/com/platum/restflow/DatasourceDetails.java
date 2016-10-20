package com.platum.restflow;

import java.util.Map;
import java.util.Properties;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.platum.restflow.resource.xml.DatasourcePropertiesAdapter;

@XmlRootElement(name = "datasource")
@XmlAccessorType (XmlAccessType.FIELD)
public class DatasourceDetails {
	
	@XmlAttribute(name = "name")
	private String name;
	
	@XmlAttribute(name = "class")
	private String implClass;
	
	@XmlJavaTypeAdapter(DatasourcePropertiesAdapter.class)
	private Properties properties;

	public String getName() {
		return name;
	}

	public DatasourceDetails setName(String name) {
		this.name = name;
		return this;
	}

	public Properties getProperties() {
		return properties;
	}

	public DatasourceDetails setProperties(Properties properties) {
		this.properties = properties;
		return this;
	}
	
	public DatasourceDetails setProperties(Map<String, Object> properties) {
		this.properties = new Properties();
		this.properties.putAll(properties);
		return this;
	}

	public String getImplClass() {
		return implClass;
	}

	public DatasourceDetails setImplClass(String implClass) {
		this.implClass = implClass;
		return this;
	}

	@Override
	public String toString() {
		return "DatasourceDetails [name=" + name + ", implClass=" + implClass + ", properties=" + properties + "]";
	}
	
}
