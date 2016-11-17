package com.platum.restflow.mail;

import java.util.ArrayList;
import java.util.List;

public class Message {
	
	private String from;
	
	private String to;
	
	private List<String> cc;
	
	private List<String> bcc;
	
	private String subject;
	
	private String body;
	
	private boolean bodyIsHtml;
	
	private List<Attachment> attachments;

	public String from() {
		return from;
	}

	public Message from(String from) {
		this.from = from;
		return this;
	}

	public String to() {
		return to;
	}

	public Message to(String to) {
		this.to = to;
		return this;
	}

	public List<String> cc() {
		return cc;
	}

	public Message cc(List<String> cc) {
		this.cc = cc;
		return this;
	}
	
	public Message addCc(String cc) {
		if(this.cc == null) {
			this.cc = new ArrayList<>();
		}
		this.cc.add(cc);
		return this;
	}
	
	public List<String> bcc() {
		return cc;
	}

	public Message bcc(List<String> bcc) {
		this.bcc = bcc;
		return this;
	}
	
	public Message addBcc(String bcc) {
		if(this.bcc == null) {
			this.bcc = new ArrayList<>();
		}
		this.bcc.add(bcc);
		return this;
	}

	public String subject() {
		return subject;
	}

	public Message subject(String subject) {
		this.subject = subject;
		return this;
	}
	
	public String body() {
		return body;
	}

	public Message body(String body) {
		this.body = body;
		return this;
	}

	public boolean bodyIsHtml() {
		return bodyIsHtml;
	}

	public Message bodyIsHtml(boolean bodyIsHtml) {
		this.bodyIsHtml = bodyIsHtml;
		return this;
	}

	public List<Attachment> attachments() {
		return attachments;
	}

	public Message attachments(List<Attachment> attachments) {
		this.attachments = attachments;
		return this;
	}
	
	public Message addAttachment(Attachment attachment) {
		if(attachments == null) {
			attachments = new ArrayList<>();
		}
		attachments.add(attachment);
		return this;
	}
	
	public boolean hasAttachments() {
		return attachments != null && !attachments.isEmpty();
	}
	
	
	
}
