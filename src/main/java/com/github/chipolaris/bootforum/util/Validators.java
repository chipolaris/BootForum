package com.github.chipolaris.bootforum.util;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.springframework.security.web.util.matcher.IpAddressMatcher;

/**
 * 
 * Utility class to provide general validators
 *
 */
public class Validators {

	/**
	 * validate email format
	 * @param email
	 * @return
	 * 
	 * original code from https://stackoverflow.com/questions/624581/what-is-the-best-java-email-address-validation-method/24320945
	 */
	public static boolean isValidEmailAddress(String email) {
		
		boolean result = true;
		try {
			InternetAddress emailAddr = new InternetAddress(email);
			emailAddr.validate();
		} 
		catch (AddressException ex) {
			result = false;
		}
		return result;
	}
	
	public static boolean isValidIPAddress(String ipAddress) {
		
		boolean result = true;
		try {
			new IpAddressMatcher(ipAddress);
		}
		catch(Exception e) {
			result = false;
		}
		
		return result;
	}
}
