package com.talkweb.common.tools;

import java.util.Random;

public class GenerateRandomPassword {

	public static String generateRandomPassword(){
		
	     StringBuffer buffer = new StringBuffer("0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"); 
		 StringBuffer sb = new StringBuffer(); 
		 Random r = new Random(); 
		 int range = buffer.length(); 
		 for (int i = 0; i < 6; i ++) { 
		    sb.append(buffer.charAt(r.nextInt(range)));
		 }
		 return sb.toString(); 
	}
}
