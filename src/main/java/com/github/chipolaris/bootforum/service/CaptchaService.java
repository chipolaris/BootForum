package com.github.chipolaris.bootforum.service;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Calendar;
import java.util.Random;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class CaptchaService {

	private static final Logger logger = LoggerFactory.getLogger(CaptchaService.class);
	
	@Value("#{applicationProperties['Captcha.cacheSize']}")
	private Integer cacheSize;
	
	@Value("#{applicationProperties['Captcha.captchaMinimumLength']}")
	private Short captchaMinimumLength;
	
	@Value("#{applicationProperties['Captcha.captchaMaximumLength']}")
	private Short captchaMaxiumLength;
	
	private Captcha[] cache;
	
	private final Random random = new Random();
	
	/**
	 * Refresh captcha cache based on configured schedule
	 * 
	 * 		see the following site to make cron value from property file work
	 *  	http://websystique.com/spring/spring-propertysource-value-annotations-example/
	 */
	@Scheduled(cron="${Captcha.refresherCron}")
	public void scheduledTask() {
		this.cache = buildCache(this.cacheSize);
	}
	
	/**
	 * build captcha cache on bean initialized/system start up
	 */
	@PostConstruct
	public void init() {
		
		logger.info("Start generating captcha cache: " + Calendar.getInstance().getTime());
		
		this.cache = buildCache(this.cacheSize);
		
		logger.info("Done generating captcha cache: " + Calendar.getInstance().getTime());
	}

	private Captcha[] buildCache(int cacheSize) {
		
		Captcha[] captchas = new Captcha[cacheSize];
		
		for(int i = 0; i < cacheSize; i++) {
			String chars = RandomStringUtils.randomAlphanumeric(captchaMinimumLength, captchaMaxiumLength + 1);
			byte[] bytes = drawImage(chars);
			Captcha captcha = new Captcha(chars, bytes);
			captchas[i] = captcha;
		}
		
		return captchas;
	}

	private byte[] drawImage(String chars) {
        BufferedImage bufferedImg = new BufferedImage(chars.length() * 12, 34, BufferedImage.TYPE_INT_RGB);  
        Graphics2D g = bufferedImg.createGraphics();
        g.setFont(new Font("default", Font.BOLD, 16));
        g.drawString(chars, 7, 20); 
        
        // TODO: consider draw some random lines in the image to make it harder to detect characters
        
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
			ImageIO.write(bufferedImg, "png", os);
		} 
        catch (IOException e) {
			logger.error("Unable to generate captcha image: ", e);
		} 
        
        return os.toByteArray();
	}
	
	/**
	 * @return
	 */
	public ServiceResponse<Captcha> getRandomCaptcha() {
		
		ServiceResponse<Captcha> response = new ServiceResponse<>();
		
		response.setDataObject(cache[random.nextInt(this.cache.length)]);
		
		return response;
	}
	
	/**
	 * bean class to hold Captcha information
	 * @author chi
	 *
	 */
	public class Captcha {
		
		private String chars;
		private byte[] bytes;
		
		public Captcha(String chars, byte[] bytes) {
			this.chars = chars;
			this.bytes = bytes;
		}
		
		public String getChars() {
			return chars;
		}
		public void setChars(String chars) {
			this.chars = chars;
		}
		public byte[] getBytes() {
			return bytes;
		}
		public void setBytes(byte[] bytes) {
			this.bytes = bytes;
		}
	}
}
