package com.platum.restflow.mail;

import com.platum.restflow.utils.promise.Promise;

public interface RestflowMail {
	
	public static final String MAIL_HOST_PROPERTY= "restflow.smtp.host";
	
	public static final String MAIL_PORT_PROPERTY= "restflow.smtp.port";
	
	public static final String MAIL_TLS_PROPERTY= "restflow.smtp.tls";
	
	public static final String MAIL_USERNAME_PROPERTY= "restflow.smtp.username";
	
	public static final String MAIL_PASSWORD_PROPERTY= "restflow.smtp.password";

	public RestflowMail config();
	
	public Promise<String> sendEmail(Message email);

}
