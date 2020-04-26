package com.github.chipolaris.bootforum.scheduled;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.github.chipolaris.bootforum.dao.GenericDAO;
import com.github.chipolaris.bootforum.domain.PasswordReset;

@Component
public class ScheduledTasks {

	private static final Logger logger = LoggerFactory.getLogger(ScheduledTasks.class);
	
	@Resource
	private GenericDAO genericDAO;
	
	@Value("${Scheduled.cleanPasswordReset.timePassed.minutes}")
	private Integer timePassedMinutes;
	
	@Scheduled(fixedRateString = "${Scheduled.cleanPasswordReset.interval.miliseconds}", 
			initialDelayString = "${Scheduled.cleanPasswordReset.initialDelay.miliseconds}")
	@Transactional(readOnly = false)
	public void cleanPasswordReset() {
		
		logger.info("Cleanup PasswordReset records");
		
		Calendar cal = Calendar.getInstance();
		
		cal.add(Calendar.MINUTE, -timePassedMinutes);
		
		Date threshold = cal.getTime();
		
		Integer deletedCount = genericDAO.deleteLessThan(PasswordReset.class, Collections.singletonMap("createDate", threshold));
		
		logger.info(String.format("%d PasswordReset records deleted", deletedCount));
	}
}
