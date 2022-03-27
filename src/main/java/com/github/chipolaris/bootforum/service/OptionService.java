package com.github.chipolaris.bootforum.service;

import javax.annotation.Resource;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.github.chipolaris.bootforum.domain.CommentOption;
import com.github.chipolaris.bootforum.domain.DisplayOption;
import com.github.chipolaris.bootforum.domain.EmailOption;
import com.github.chipolaris.bootforum.domain.RegistrationOption;
import com.github.chipolaris.bootforum.domain.RemoteIPFilterOption;
import com.github.chipolaris.bootforum.domain.RemoteIPFilterOption.FilterType;
import com.github.chipolaris.bootforum.event.DisplayOptionLoadEvent;
import com.github.chipolaris.bootforum.event.EmailOptionLoadEvent;
import com.github.chipolaris.bootforum.event.RegistrationOptionLoadEvent;
import com.github.chipolaris.bootforum.event.RemoteIPFilterOptionLoadEvent;

@Service
@Transactional
public class OptionService {

	private static final Logger logger = LoggerFactory.getLogger(OptionService.class);
	
	@Resource
	private GenericService genericService;
	
	@Resource
	private ApplicationEventPublisher applicationEventPublisher;
	
	private CommentOption commentOption;
	
	public CommentOption getCommentOption() {
		return commentOption;
	}
	
	public void init() {
	
		createRegistrationOption();
		createEmailOption();
		createDisplayOption();
		createRemoteIPFilterOption();
		createCommentOption();
	}
	
	private void createRegistrationOption() {
		
		RegistrationOption registrationOption = genericService.findEntity(RegistrationOption.class, 1L).getDataObject();
		
		if(registrationOption != null) {
			logger.info("Registration Option's already initialized");
		}
		else {
			
			logger.info("Registration Option is NOT initialized. Creating..");
			
			registrationOption = new RegistrationOption();
			registrationOption.setId(1L);
			
			registrationOption.setEnableCaptcha(true);
			registrationOption.setEnableEmailConfirm(true);
			registrationOption.setRegistrationEmailSubject(REGISTRATION_EMAIL_SUBJECT);
			registrationOption.setRegistrationEmailTemplate(REGISTRATION_EMAIL_TEMPLATE);
			registrationOption.setPasswordResetEmailSubject(PASSWORD_RESET_EMAIL_SUBJECT);
			registrationOption.setPasswordResetEmailTemplate(PASSWORD_RESET_EMAIL_TEMPLATE);
			
			genericService.saveEntity(registrationOption);
		}
		
		applicationEventPublisher.publishEvent(new RegistrationOptionLoadEvent(this, registrationOption));
	}
	
	/*
	 * Default values on first time system initialization
	 */
	private static final String REGISTRATION_EMAIL_SUBJECT = "Confirm account registration at BootForum";
	
	private static final String REGISTRATION_EMAIL_TEMPLATE = 
			"<p><strong>Hi #username</strong>,</p>"
		  +	"<p>This email&nbsp;<strong>#email</strong>&nbsp;has been used for account registration on <strong>BootForum</strong>.</p>"
		  +	"<p>If that wasn&#39;t your intention, kindly ignore this email. Otherwise, please lick on this link #confirm-url to activate your account.</p>"
		  +	"<p>Regards,</p>";
	
	private static final String PASSWORD_RESET_EMAIL_SUBJECT = "Password reset requested at BootForum";
	
	private static final String PASSWORD_RESET_EMAIL_TEMPLATE =
			"<p><strong>Hi #username</strong>,</p>"
		  + "<p>Here is the <strong>#reset-url</strong> to reset your password in <strong>BootForum</strong></p>"
		  + "<p>If you didn&#39;t request this, kindly ignore this email.</p>"
		  + "<p>Regards,</p>";
	
	private void createEmailOption() {

		EmailOption emailOption = genericService.findEntity(EmailOption.class, 1L).getDataObject();
		
		if(emailOption != null) {
			logger.info("Email Option's already initialized");
		}
		else {
			
			logger.info("Email Option is NOT initialized. Creating..");
			
			emailOption = new EmailOption();
			emailOption.setId(1L);
			
			emailOption.setCreateBy("system");
			emailOption.setHost("to-be-update");
			emailOption.setPort(0);
			emailOption.setUsername("to-be-update");
			emailOption.setPassword("to-be-update");
			emailOption.setTlsEnable(true);
			
			genericService.saveEntity(emailOption);
		}
		
		applicationEventPublisher.publishEvent(new EmailOptionLoadEvent(this, emailOption));
	}
	
	private void createDisplayOption() {
		
		DisplayOption displayOption = genericService.findEntity(DisplayOption.class, 1L).getDataObject();
		
		if(displayOption != null) {
			logger.info("Display Option's already initialized");
		}
		else {
			
			logger.info("Display Option is NOT initialized. Creating..");
			
			displayOption = new DisplayOption();
			displayOption.setId(1L);
			
			// default values
			displayOption.setThemeColor("w3-theme-light-blue");
			displayOption.setThemeComponent("saga");
			
			displayOption.setShowMostCommentsDiscussions(true);
			displayOption.setNumMostCommentsDiscussions(5);
			
			displayOption.setShowMostRecentDiscussions(true);
			displayOption.setNumMostRecentDiscussions(5);
			
			displayOption.setShowMostViewsDiscussions(true);
			displayOption.setNumMostViewsDiscussions(5);
			
			displayOption.setShowDiscussionsForTag(true);
			displayOption.setNumDiscussionsPerTag(5);
			
			genericService.saveEntity(displayOption);
		}
		
		applicationEventPublisher.publishEvent(new DisplayOptionLoadEvent(this, displayOption));
	}
	
	private void createRemoteIPFilterOption() {
		
		RemoteIPFilterOption remoteIPFilterOption = genericService.findEntity(RemoteIPFilterOption.class, 1L).getDataObject();
		
		if(remoteIPFilterOption != null) {
			logger.info("Remote IP Filter Option's already initialized");
		}
		else {
			
			logger.info("Remote IP Filter Option is NOT initialized. Creating..");
			
			remoteIPFilterOption = new RemoteIPFilterOption();
			
			remoteIPFilterOption.setId(1L);
			remoteIPFilterOption.setFilterType(FilterType.NONE);
			
			genericService.saveEntity(remoteIPFilterOption);
		}
		
		applicationEventPublisher.publishEvent(new RemoteIPFilterOptionLoadEvent(this, remoteIPFilterOption));
	}
	
	
	private void createCommentOption() {
		
		commentOption = genericService.findEntity(CommentOption.class, 1L).getDataObject();
		
		if(commentOption != null) {
			logger.info("Comment Option's already initialized");
		}
		else {
			
			logger.info("Comment Option is NOT initialized. Creating..");
			
			commentOption = new CommentOption();
			commentOption.setId(1L);
			
			// default values
			commentOption.setMinCharDiscussionTitle(1);
			commentOption.setMaxCharDiscussionTitle(80);
			commentOption.setMinCharDiscussionContent(1);
			commentOption.setMaxCharDiscussionContent(10000*1024); // 10,000KB ~ 10MB
			commentOption.setMaxDiscussionThumbnail(5);
			commentOption.setMaxDiscussionAttachment(5);
			commentOption.setMaxByteDiscussionThumbnail(5000*1024); // 1000KB ~ 5MB
			commentOption.setMaxByteDiscussionAttachment(5000*1024); // 1000KB ~ 5MB
			
			commentOption.setMinCharCommentTitle(1);
			commentOption.setMaxCharCommentTitle(80);
			commentOption.setMinCharCommentContent(1);
			commentOption.setMaxCharCommentContent(10000*1024); // 10,000KB ~ 10MB
			commentOption.setMaxCommentThumbnail(3);
			commentOption.setMaxCommentAttachment(3);
			commentOption.setMaxByteCommentThumbnail(5000*1024); // 1000KB ~ 5MB
			commentOption.setMaxByteCommentAttachment(5000*1024); // 1000KB ~ 5MB
			
			genericService.saveEntity(commentOption);
		}
	}
}
