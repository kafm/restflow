package com.platum.restflow.resource;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "download")
@XmlAccessorType (XmlAccessType.FIELD)
public class DownloadMethod extends AbstractMethod {

	@XmlAttribute
	private boolean active = false;

	public boolean isActive() {
		return active;
	}

	public DownloadMethod setActive(boolean active) {
		this.active = active;
		return this;
	}
	
}
