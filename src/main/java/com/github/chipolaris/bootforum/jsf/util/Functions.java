package com.github.chipolaris.bootforum.jsf.util;

import java.util.HashMap;
import java.util.Map;

/**
 * create custom functions
 * 
 * ref: http://stackoverflow.com/questions/7079978/how-to-create-a-custom-el-function
 * @author chi
 *
 */
public final class Functions {

	private static Map<String, String> contentType2ImageIconMap;
	
	static {
		
		contentType2ImageIconMap = new HashMap<String, String>();
		// application/xxx
		contentType2ImageIconMap.put("application/x-zip-compressed", "document-zip.png");
		contentType2ImageIconMap.put("application/pdf", "document-pdf.png");
		contentType2ImageIconMap.put("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "document-word.png");
		contentType2ImageIconMap.put("application/msword", "document-word.png");
		contentType2ImageIconMap.put("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "document-xls.png");
		contentType2ImageIconMap.put("application/vnd.ms-excel", "document-xls.png");
		contentType2ImageIconMap.put("application/vnd.openxmlformats-officedocument.presentationml.presentation", "document-ppt.png");
		contentType2ImageIconMap.put("application/vnd.ms-powerpoint", "document-ppt.png");
		// image/xxx
		contentType2ImageIconMap.put("image/jpeg", "image-jpeg.png");
		contentType2ImageIconMap.put("image/gif", "image-gif.png");
		contentType2ImageIconMap.put("image/png", "image-png.png");
	}
	
    private Functions() {
        // Hide constructor.
    }
    
    public static String contentTypeIcon(String contentType) {
		String imageIcon = contentType2ImageIconMap.get(contentType);
		return imageIcon != null ? imageIcon : "document-generic.png";
    }
    
	public static String stringToColor(String str) {
		int hash = 0;
		for (int i = 0; i < str.length(); i++) {
			hash = str.charAt(i) + ((hash << 5) - hash);
		}
		String color = "#";
		for (int i = 0; i < 3; i++) {
			long value = (hash >> (i * 8)) & 0xFF;
			String temp = "00" + Long.toHexString(value);
			color += temp.substring(temp.length() - 2);
		}
		return color;
	}
}
