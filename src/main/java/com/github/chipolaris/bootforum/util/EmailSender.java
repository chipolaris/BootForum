package com.github.chipolaris.bootforum.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Utility class to sends email.
 */
@Service
public class EmailSender {
	
	private static final Logger logger = LoggerFactory.getLogger(EmailSender.class);
	
	private JavaMailSenderImpl javaMailSender;

	@Value("${Email.smtp.host}")
	private String host;

	@Value("${Email.smtp.port}")
	private Integer port;
	
	@Value("${Email.smtp.username}")
	private String username;
	
	@Value("${Email.smtp.password}")
	private String password;
	
	@PostConstruct
	public void init() {
		
		/* 
		 * instantiate and initialize javaMailSender with properties that
		 * taken from the config properties
		 */
		javaMailSender = new JavaMailSenderImpl();
		javaMailSender.setHost(this.host);
		javaMailSender.setPort(this.port);
		javaMailSender.setUsername(this.username);
		javaMailSender.setPassword(this.password);
		
		Properties javaMailProperties = new Properties();
		javaMailProperties.put("mail.smtp.auth", true);
		javaMailProperties.put("mail.smtp.starttls.enable", true);
		javaMailSender.setJavaMailProperties(javaMailProperties);
	}
	
	/**
	 * Send email to a single email address
	 * 
	 * @param fromAddress email address of sender
	 * @param toAddress email address of single recipient
	 * @param subject email subject
	 * @param messageText email body
	 * @param sendAsHtml HTML format enable/disable
	 * @throws Exception whenever something exceptional happens.
	 */
	public void sendEmail(String fromAddress,
			String toAddress, String subject, String messageText, boolean sendAsHtml) throws Exception {
		
		send(fromAddress, new String[]{toAddress}, null, 
				null, subject, messageText, null, null, sendAsHtml);	
	}

	/**
	 * Send email to a single email address with attachment
	 * 
	 * @param fromAddress email address of sender
	 * @param toAddress email address of single recipient
	 * @param subject email subject
	 * @param messageText email body
	 * @param attachment file to be attached
	 * @param attachmentName name of the attached file ?
	 * @throws Exception whenever something exceptional happens.
	 */
	public void sendEmail(String fromAddress,
			String toAddress, String subject, String messageText, 
			File attachment, String attachmentName) throws Exception {
		
		send(fromAddress, new String[]{toAddress}, null, 
				null, subject, messageText, attachment != null ? new FileSystemResource(
						attachment) : null, attachmentName, false);	
	}

	/**
	 * Send email to a single email address with attachment (byte[])
	 * 
	 * @param fromAddress email address of sender
	 * @param toAddress email address of single recipient
	 * @param subject email subject
	 * @param messageText email body
	 * @param attachmentBytes bytes of content to be attached
	 * @param attachmentName name of the attached file ?
	 * @throws Exception whenever something exceptional happens.
	 */
	public void sendEmail(String fromAddress,
			String toAddress, String subject, String messageText, 
			byte[] attachmentBytes, String attachmentName) throws Exception {
		
		send(fromAddress, new String[]{toAddress}, null, 
				null, subject, messageText, 
				attachmentBytes != null ? new ByteArrayResource(attachmentBytes) : null, 
				attachmentName, false);	
	}
	
	/**
	 * Send email to a list (array) of email addresses
	 * 
	 * @param fromAddress email address of sender
	 * @param toAddresses email addresses of multiple recipients
	 * @param subject email subject
	 * @param messageText email body
	 * @throws Exception whenever something exceptional happens.
	 */
	public void sendEmail(String fromAddress, String[] toAddresses,
			String subject, String messageText) throws Exception {

		send(fromAddress, toAddresses, null, null, subject, messageText, null,
				null, false);
	}

	/**
	 * Send email to a collection of email addresses
	 * 
	 * @param fromAddress email address of sender
	 * @param toAddresses email addresses of multiple recipients
	 * @param subject email subject
	 * @param messageText email body
	 * @throws Exception whenever something exceptional happens.
	 */
	public void sendEmail(String fromAddress, Collection<String> toAddresses,
			String subject, String messageText) throws Exception {

		send(fromAddress, toAddresses.toArray(new String[toAddresses.size()]), null, null, subject, messageText, null,
				null, false);
	}
	
	/**
	 * Send email to list (array) of email addresses as well as CC's and BCC's
	 * 
	 * @param fromAddress email address of sender
	 * @param toAddresses email addresses for To: field
	 * @param ccAddresses email addresses for Cc: field
	 * @param bccAddresses email addresses for Bcc: field
	 * @param subject email subject
	 * @param messageText email body
	 * @throws Exception whenever something exceptional happens.
	 */
	public void sendEmail(String fromAddress, String[] toAddresses,
			String[] ccAddresses, String[] bccAddresses, String subject,
			String messageText) throws Exception {

		send(fromAddress, toAddresses, ccAddresses, bccAddresses, subject,
				messageText, null, null, false);

	}
	
	/**
	 * Send email to list (array) of email addresses as well as CC's and BCC's;
	 * This one allows choice to send as HTML or Text
	 * 
	 * @param fromAddress email address of sender
	 * @param toAddresses email addresses for To: field
	 * @param ccAddresses email addresses for Cc: field
	 * @param bccAddresses email addresses for Bcc: field
	 * @param subject email subject
	 * @param messageText email body
	 * @param sendAsHtml flag indicating whether or not MIME type is to be HTML or plain text
	 * @throws Exception whenever something exceptional happens.
	 */
	public void sendEmail(String fromAddress, String[] toAddresses,
			String[] ccAddresses, String[] bccAddresses, String subject,
			String messageText, boolean sendAsHtml) throws Exception {

		send(fromAddress, toAddresses, ccAddresses, bccAddresses, subject,
				messageText, null, null, sendAsHtml);

	}
	
	/**
	 * Send email to collections of email addresses as well as CC's and BCC's
	 * 
	 * @param fromAddress email address of sender
	 * @param toAddresses email addresses for To: field
	 * @param ccAddresses email addresses for Cc: field
	 * @param bccAddresses email addresses for Bcc: field
	 * @param subject email subject
	 * @param messageText email body
	 * @throws Exception whenever something exceptional happens.
	 */
	public void sendEmail(String fromAddress, Collection<String> toAddresses,
			Collection<String> ccAddresses, Collection<String> bccAddresses, String subject,
			String messageText) throws Exception {

		send(fromAddress, toAddresses.toArray(new String[toAddresses.size()]), 
				ccAddresses.toArray(new String[ccAddresses.size()]), 
				bccAddresses.toArray(new String[bccAddresses.size()]), subject,
				messageText, null, null, false);
	}

	/**
	 * Send email with attachment to a list (array) of email addresses
	 * 
	 * @param fromAddress email address of sender
	 * @param toAddresses email addresses for To: field
	 * @param subject email subject
	 * @param messageText email body
	 * @param attachment file to attach
	 * @param attachmentName name of attachment
	 * @throws Exception whenever something exceptional happens.
	 */
	public void sendEmailWithAttachment(String fromAddress,
			String[]  toAddresses, String subject, String messageText, 
			File attachment, String attachmentName, long applicationID) 
			throws Exception {

		send(fromAddress, toAddresses, null, null, subject, messageText,
				attachment != null ? new FileSystemResource(attachment) : null,
				attachmentName, false);
	}

	/**
	 * Send email with attachment to a collection of email addresses
	 * 
	 * @param fromAddress email address of sender
	 * @param toAddresses email addresses for To: field
	 * @param subject email subject
	 * @param messageText email body
	 * @param attachment file to attach
	 * @param attachmentName name of attachment
	 * @param applicationID id of pre-application
	 * @throws Exception whenever something exceptional happens.
	 */
	public void sendEmailWithAttachment(String fromAddress,
			Collection<String> toAddresses, String subject, String messageText, 
			File attachment, String attachmentName, long applicationID) 
			throws Exception {

		send(fromAddress, toAddresses.toArray(new String[toAddresses.size()]), null, null, subject, messageText,
				attachment != null ? new FileSystemResource(attachment) : null,
				attachmentName, false);
	}
	
	/**
	 * Send email with attachment to list (array) of email addresses as well as CC's and BCC's
	 * 
	 * @param fromAddress email address of sender
	 * @param toAddresses email addresses for To: field
	 * @param ccAddresses email addresses for Cc: field
	 * @param bccAddresses email addresses for Bcc: field
	 * @param subject email subject
	 * @param messageText email body
	 * @param attachment file to attach
	 * @param attachmentName name of attachment
	 * @throws Exception whenever something exceptional happens.
	 */
	public void sendEmailWithAttachment(String fromAddress,
			String[]  toAddresses, String[]  ccAddresses,
			String[] bccAddresses, String subject, String messageText, 
			File attachment, String attachmentName) 
			throws Exception {

		send(fromAddress, toAddresses, ccAddresses, bccAddresses, subject,
				messageText, attachment != null ? new FileSystemResource(
						attachment) : null, attachmentName, false);
	}

	/**
	 * Send email with attachment to collections of email addresses as well as CC's and BCC's
	 * 
	 * @param fromAddress email address of sender
	 * @param toAddresses email addresses for To: field
	 * @param ccAddresses email addresses for Cc: field
	 * @param bccAddresses email addresses for Bcc: field
	 * @param subject email subject
	 * @param messageText email body
	 * @param attachment file to attach
	 * @param attachmentName name of attachment
	 * @throws Exception whenever something exceptional happens.
	 */
	public void sendEmailWithAttachment(String fromAddress,
			Collection<String> toAddresses, Collection<String> ccAddresses,
			Collection<String> bccAddresses, String subject, String messageText, 
			File attachment, String attachmentName) 
			throws Exception {

		send(fromAddress, toAddresses.toArray(new String[toAddresses.size()]), 
				ccAddresses.toArray(new String[ccAddresses.size()]), 
				bccAddresses.toArray(new String[bccAddresses.size()]), subject,
				messageText, attachment != null ? new FileSystemResource(
						attachment) : null, attachmentName, false);
	}	
	
	/**
	 * Send email with attachment (byte[]) to a list (array) of email addresses
	 * 
	 * @param fromAddress email address of sender
	 * @param toAddresses email addresses for To: field
	 * @param subject email subject
	 * @param messageText email body
	 * @param attachmentBytes bytes of content to attach
	 * @param attachmentName name of attachment
	 * @throws Exception whenever something exceptional happens.
	 */
	public void sendEmailWithAttachment(String fromAddress,
			String[]  toAddresses, String subject, String messageText, 
			byte[] attachmentBytes, String attachmentName) 
			throws Exception {
		
		send(fromAddress, toAddresses, null,
				null, subject, messageText,
				attachmentBytes != null ? new ByteArrayResource(attachmentBytes) : null, 
				attachmentName, false);
	}

	/**
	 * Send email with attachment (byte[]) to a collection of email addresses
	 * 
	 * @param fromAddress email address of sender
	 * @param toAddresses email addresses for To: field
	 * @param subject email subject
	 * @param messageText email body
	 * @param attachmentBytes bytes of content to attach
	 * @param attachmentName name of attachment
	 * @throws Exception whenever something exceptional happens.
	 */
	public void sendEmailWithAttachment(String fromAddress,
			Collection<String> toAddresses, String subject, String messageText, 
			byte[] attachmentBytes, String attachmentName) 
			throws Exception {
		
		send(fromAddress, toAddresses.toArray(new String[toAddresses.size()]), null,
				null, subject, messageText,
				attachmentBytes != null ? new ByteArrayResource(attachmentBytes) : null, 
				attachmentName, false);
	}
	
	/**
	 * Send email with attachment (byte[]) to a list (array) of email addresses as well as CC's and BCC's
	 * 
	 * @param fromAddress email address of sender
	 * @param toAddresses email addresses for To: field
	 * @param ccAddresses email addresses for Cc: field
	 * @param bccAddresses email addresses for Bcc: field
	 * @param subject email subject
	 * @param messageText email body
	 * @param attachmentBytes bytes of content to attach
	 * @param attachmentName name of attachment
	 * @throws Exception whenever something exceptional happens.
	 */
	public void sendEmailWithAttachment(String fromAddress,
			String[]  toAddresses, String[]  ccAddresses,
			String[] bccAddresses, String subject, String messageText, 
			byte[] attachmentBytes, String attachmentName) 
			throws Exception {
		
			send(fromAddress, toAddresses, ccAddresses,
					bccAddresses, subject, messageText,
					attachmentBytes != null ? new ByteArrayResource(attachmentBytes) : null, 
					attachmentName, false);
	}

	/**
	 * Send email with attachment (byte[]) to a collections of email addresses as well as CC's and BCC's
	 * 
	 * @param fromAddress email address of sender
	 * @param toAddresses email addresses for To: field
	 * @param ccAddresses email addresses for Cc: field
	 * @param bccAddresses email addresses for Bcc: field
	 * @param subject email subject
	 * @param messageText email body
	 * @param attachmentBytes bytes of content to attach
	 * @param attachmentName name of attachment
	 * @throws Exception whenever something exceptional happens.
	 */
	public void sendEmailWithAttachment(String fromAddress,
			Collection<String> toAddresses, Collection<String> ccAddresses,
			Collection<String> bccAddresses, String subject, String messageText, 
			byte[] attachmentBytes, String attachmentName) 
			throws Exception {
		
			send(fromAddress, toAddresses.toArray(new String[toAddresses.size()]), 
					ccAddresses.toArray(new String[ccAddresses.size()]),
					bccAddresses.toArray(new String[bccAddresses.size()]), subject, messageText,
					attachmentBytes != null ? new ByteArrayResource(attachmentBytes) : null, 
					attachmentName, false);
	}
	

	/**
	 * Private helper method
	 * 
	 * @param fromAddress email address of sender
	 * @param toAddresses email addresses for To: field
	 * @param ccAddresses email addresses for Cc: field
	 * @param bccAddresses email addresses for Bcc: field
	 * @param subject email subject
	 * @param messageText email body
	 * @param attachment input stream for content to attach
	 * @param attachmentName name of attachment
	 * @param sendAsHtml flag indicating whether to send as HTML or plain text.
	 * @throws Exception whenever something exceptional happens.
	 */
	private void send(String fromAddress,
			String[] toAddresses, String[] ccAddresses,
			String[] bccAddresses, String subject, String messageText, 
			InputStreamSource attachment, String attachmentName, boolean sendAsHtml) 
			throws Exception {

		MimeMessage mimeMessage = javaMailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

		prepareMimeMessageHelper(helper, fromAddress, toAddresses, ccAddresses,
				bccAddresses, subject, messageText, sendAsHtml);

		if (attachment != null && attachmentName != null) {
			helper.addAttachment(attachmentName, attachment);
		}

		// finally, send the message
		javaMailSender.send(mimeMessage);
	}

	/**
	 * Private helper method
	 * 
	 * @param helper some {@code MimeMessageHelper} object
	 * @param fromAddress email address of sender
	 * @param toAddresses email addresses for To: field
	 * @param ccAddresses email addresses for Cc: field
	 * @param bccAddresses email addresses for Bcc: field
	 * @param subject email subject
	 * @param messageText email body
	 * @param sendAsHtml flag indicating whether to send as HTML or plain text.
	 * @throws MessagingException whenever something exceptional happens.
	 */
	private void prepareMimeMessageHelper(MimeMessageHelper helper,
			String fromAddress, String[] toAddresses, String[] ccAddresses,
			String[] bccAddresses, String subject, String messageText, boolean sendAsHtml)
			throws MessagingException {
		helper.setFrom(fromAddress);
		toAddresses = removeEmptyElements(toAddresses);
		ccAddresses = removeEmptyElements(ccAddresses);
		bccAddresses = removeEmptyElements(bccAddresses);
		
		if (toAddresses.length == 0) {
			logger.info("toAdresses' length is 0");
		}
		if (toAddresses != null && !(toAddresses.length == 1 && toAddresses[0] == null)) {
			helper.setTo(toAddresses);
		}
		else {
			throw new MessagingException("At least one To Address must be specified");
		}
		
		if(ccAddresses != null) {
			helper.setCc(ccAddresses);
		}
		
		if(bccAddresses != null) {
			helper.setBcc(bccAddresses);
		}
		
		helper.setSubject(subject);
		helper.setText(messageText, sendAsHtml);
	}
	
	/**
	 * Private helper method
	 * 
	 * @param str_arr
	 * @return array with empty elements removed.
	 */
	private String[] removeEmptyElements(String[] str_arr) {
		ArrayList<String> arrList = new ArrayList<String>();
		
		if (str_arr != null) {
			for (String str : str_arr) {
				if (!"".equals(str)) {
					arrList.add(str);
				}
			}
		}
		
		if(arrList.size() <= 0) {
			return null;
		}
		
		return arrList.toArray(new String[0]);
	}
}