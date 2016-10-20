package com.platum.restflow.mail;

public class Attachment {
	
	private byte[] data;
	
	private String disposition;
	
	private String description;
	
	private String contentId;
	
	private String contentType;

	public byte[] data() {
		return data;
	}

	public Attachment data(byte[] data) {
		this.data = data;
		return this;
	}

	public String disposition() {
		return disposition;
	}

	public Attachment disposition(String disposition) {
		this.disposition = disposition;
		return this;
	}
	
	public String description() {
		return description;
	}

	public Attachment description(String description) {
		this.description = description;
		return this;
	}

	public String contentId() {
		return contentId;
	}

	public Attachment contentId(String contentId) {
		this.contentId = contentId;
		return this;
	}

	public String contentType() {
		return contentType;
	}

	public Attachment contentType(String contentType) {
		this.contentType = contentType;
		return this;
	}
	
	
}
