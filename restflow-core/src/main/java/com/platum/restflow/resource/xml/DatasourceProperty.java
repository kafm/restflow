package com.platum.restflow.resource.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="property")
@XmlAccessorType (XmlAccessType.FIELD)
public class DatasourceProperty {
	
    @XmlAttribute
    private String name;
    
	@XmlAttribute
    private String value;
	
    public String getName() {
		return name;
	}

	public DatasourceProperty setName(String name) {
		this.name = name;
		return this;
	}

	public String getValue() {
		return value;
	}

	public DatasourceProperty setValue(String value) {
		this.value = value;
		return this;
	}
      
}
