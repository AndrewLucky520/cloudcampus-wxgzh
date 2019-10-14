package com.talkweb.common.utils;

import org.apache.commons.codec.binary.Base64;


public class Base64Util {

	public static String decrypt(String string) throws Exception {
		return new String(Base64.decodeBase64(string), "utf-8").trim();
	}
	
	public static String encrypt(String string) throws Exception {
		return Base64.encodeBase64String(string.getBytes("utf-8")).trim();
	}
	
	public static void main(String [] args) throws Exception{
		System.out.println(Base64.encodeBase64String("438102001020030030".getBytes("utf-8")).trim());
		System.out.println(Base64.encodeBase64String("���".getBytes("utf-8")).trim());
		System.out.println(Base64Util.decrypt("6KKB5ZiJ56mX5a626ZW/"));
	}
}
