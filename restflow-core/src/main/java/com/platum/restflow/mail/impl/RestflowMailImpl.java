package com.platum.restflow.mail.impl;

import java.util.ArrayList;
import java.util.List;

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

import io.vertx.core.buffer.Buffer;
import io.vertx.ext.mail.MailAttachment;
import io.vertx.ext.mail.MailClient;
import io.vertx.ext.mail.MailConfig;
import io.vertx.ext.mail.MailMessage;
import io.vertx.ext.mail.StartTLSOptions;

public class RestflowMailImpl implements RestflowMail {
	
	private final  Logger logger = LoggerFactory.getLogger(getClass());
	
	private Restflow restflow;

	private MailClient mailClient;
	
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
			String tls = env.getProperty(MAIL_TLS_PROPERTY);
			from = env.getProperty(MAIL_USERNAME_PROPERTY);
			String password = env.getProperty(MAIL_PASSWORD_PROPERTY);
			if(StringUtils.isNotEmpty(host)) {
				try {
					MailConfig config = new MailConfig();
					config.setHostname(host);
					if(StringUtils.isNotEmpty(port)) {
						config.setPort(Integer.parseInt(port));
					}
					if(StringUtils.isNotEmpty(tls) && BooleanUtils.toBoolean(tls)) {
						config.setStarttls(StartTLSOptions.REQUIRED);
					}
					config.setUsername(from);
					config.setPassword(password);
					mailClient = MailClient.createNonShared(restflow.vertx(), config);					
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
	public Promise<String> sendEmail(Message email) {
		MailMessage message = new MailMessage()
								.setFrom(StringUtils.isEmpty(email.from()) ? from : email.from())
								.setTo(email.to())
								.setCc(email.cc());
		Promise<String> promise = PromiseFactory.getPromiseInstance();
		if(email.bodyIsHtml()) {
			message.setHtml(email.body());
		} else {
			message.setText(email.body());
		}
		if(email.hasAttachments()) {
			List<MailAttachment> mailAttachs =  new ArrayList<>();
			email.attachments().stream().forEach(attach -> {
				mailAttachs.add(new MailAttachment()
									.setContentId(attach.contentId())
									.setContentType(attach.contentType())
									.setDisposition(attach.disposition())
									.setDescription(attach.description())
									.setData(Buffer.buffer(attach.data())));
			});
			message.setAttachment(mailAttachs);
		}
		mailClient.sendMail(message, handler -> {
			if(handler.succeeded()) {
				promise.resolve(handler.result().getMessageID());
			} else {
				promise.reject(handler.cause());
			}
		});
		return promise;
	}

}
