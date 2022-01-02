package com.github.chipolaris.bootforum.domain;

import java.security.Key;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class CryptoConverter implements AttributeConverter<String, String> {

    private static final String ALGORITHM = "AES/ECB/PKCS5Padding";
    private static final String DEFAULT_KEY = "MySuperSecretKey";
    private static final String KEY_ALGORITHM = "AES";
	
	private static final byte[] KEY;
	static {
		String configuredEncryptionKey = System.getProperty("encryptionKey");
		
		if(configuredEncryptionKey != null) {
			KEY = configuredEncryptionKey.getBytes();
		}
		else {
			KEY = DEFAULT_KEY.getBytes(); 
		}
	}
	
	@Override
	public String convertToDatabaseColumn(String plainText) {
		
		if(plainText == null) {
			return null;
		}
		
		Key key = new SecretKeySpec(KEY, KEY_ALGORITHM);
		try {
			Cipher c = Cipher.getInstance(ALGORITHM);
			c.init(Cipher.ENCRYPT_MODE, key);
			return Base64.getEncoder().encodeToString(c.doFinal(plainText.getBytes()));
		} 
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String convertToEntityAttribute(String encryptedData) {
		
		if(encryptedData == null) {
			return null;
		}
		
		Key key = new SecretKeySpec(KEY, KEY_ALGORITHM);
		try {
			Cipher c = Cipher.getInstance(ALGORITHM);
			c.init(Cipher.DECRYPT_MODE, key);
			return new String(c.doFinal(Base64.getDecoder().decode(encryptedData)));
		} 
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
