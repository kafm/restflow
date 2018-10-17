package com.platum.restflow.mail.impl;

import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.platum.restflow.Restflow;
import com.platum.restflow.RestflowEnvironment;
import com.platum.restflow.mail.Message;
import com.platum.restflow.mail.RestflowMail;
import com.platum.restflow.utils.promise.Promise;
import com.platum.restflow.utils.promise.PromiseFactory;

public class RestflowMailImpl implements RestflowMail {
	
	private final  Logger logger = LoggerFactory.getLogger(getClass());
	
	private Restflow restflow;

	private Session session;
	
	private String from;
	
	public RestflowMailImpl(Restflow restflow) {
		this.restflow = restflow;
	}
	
	@Override
	public RestflowMail config() {
		RestflowEnvironment env = restflow.getEnvironment();
		if(env != null) {
			String host = env.getProperty(MAIL_HOST_PROPERTY);
			String port = env.getProperty(MAIL_PORT_PROPERTY);
			Boolean tls = BooleanUtils.toBooleanObject(env.getProperty(MAIL_TLS_PROPERTY));
			String userName = env.getProperty(MAIL_USERNAME_PROPERTY);
			String password = env.getProperty(MAIL_PASSWORD_PROPERTY);
			from = env.getProperty(MAIL_FROM_PROPERTY);
			if(StringUtils.isEmpty(from)) from = userName;
			if(StringUtils.isNotEmpty(host)) {
				try {
					Properties props = new Properties();
					props.put("mail.smtp.starttls.enable", tls.toString());
					props.put("mail.smtp.host", host);
					if(StringUtils.isNotEmpty(port)) {
						props.put("mail.smtp.port", port);				
					}
					if(StringUtils.isNotEmpty(password)) {
						props.put("mail.smtp.auth", "true");
						session = Session.getInstance(props,
								  new javax.mail.Authenticator() 
								  {
									protected PasswordAuthentication getPasswordAuthentication() 
									{
										return new PasswordAuthentication(userName, password);
									}
								 });
					} else {
						session = Session.getInstance(props);
					}	
					if(logger.isInfoEnabled()) {
						logger.info("Mail service started with host "+host+":"+port+", tls:"+tls+" and user "+from);
					}
				} catch(Throwable e) {
					if(logger.isDebugEnabled()) {
						logger.debug("Error when start email service.", e);
					}
				}

			}
		}
		return this;
	}

	@Override
	public Promise<Void> sendEmail(Message email) {
		Promise<Void> promise = PromiseFactory.getPromiseInstance();
		restflow.vertx().executeBlocking(future -> {
			try {
				MimeMessage message = new MimeMessage(session);
				message.setFrom(new InternetAddress(from));
		        if(StringUtils.isNotEmpty(email.to()))
		        {
		        	String[] tos = email.to().split(",");
		        	for(String to : tos) {
		        		message.addRecipient(javax.mail.Message.RecipientType.TO, 
		        								new InternetAddress(to));
		        	}		        	 	
		        }
		        if(email.cc() != null && !email.cc().isEmpty())
		        {
		        	for(String cc : email.cc()) {
		        		message.addRecipient(javax.mail.Message.RecipientType.CC, 
		        								new InternetAddress(cc));
		        	}		        	 	
		        }
		        if(email.bcc() != null && !email.bcc().isEmpty())
		        {
		        	for(String bcc : email.bcc()) {
		        		message.addRecipient(javax.mail.Message.RecipientType.BCC, 
		        								new InternetAddress(bcc));
		        	}		        	 	
		        }
				message.setSubject(email.subject());
				if(email.bodyIsHtml()) {
					message.setContent(email.body(), "text/html; charset=\"ISO-8859-1\"");
				} else {
					message.setContent(email.body(), "text/plain");
				}
				resolveTransport().sendMessage(message, message.getAllRecipients());
				future.complete();
			} catch(Throwable e) {
				future.fail(e);
			}
		}, res -> {
			if(res.succeeded()) {
				promise.resolve();
			} else {
				promise.reject(res.cause());
			}
		});
		return promise;
	}

	private Transport resolveTransport() throws MessagingException {
		Transport transport = session.getTransport("smtp");
		if (!transport.isConnected())
		    transport.connect();
		return transport;
	}
}
