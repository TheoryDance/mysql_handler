package com.grand.mysql_handler.utils;

import java.io.IOException;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class SecurityUtils {

	public static String base64Encode(String str) {
		return new BASE64Encoder().encodeBuffer(str.getBytes());
	}
	
	public static String base64Decode(String str) {
		String ccc = null;
		try {
			ccc = new String(new BASE64Decoder().decodeBuffer(str));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ccc;
	}
}
