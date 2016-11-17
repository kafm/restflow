package com.platum.restflow.resource;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "upload")
@XmlAccessorType (XmlAccessType.FIELD)
public class UploadMethod {
	
	@XmlAttribute
	private boolean active = false;
	
	@XmlAttribute
	private String useResource;

	public boolean isActive() {
		return active;
	}

	public UploadMethod setActive(boolean active) {
		this.active = active;
		return this;
	}

	public String getUseResource() {
		return useResource;
	}

	public void setUseResource(String useResource) {
		this.useResource = useResource;
	}

}
