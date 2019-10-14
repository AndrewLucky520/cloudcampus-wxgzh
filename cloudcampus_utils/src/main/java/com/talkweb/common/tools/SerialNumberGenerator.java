package com.talkweb.common.tools;

public class SerialNumberGenerator {

	
	public static String generateSerialNumber(int index){
		
		String serialNum = "";
		
		if(index < 10){
			serialNum = "0" + index;
		}else{
			serialNum = String.valueOf(index);
		}
		
		return serialNum;
	}
	
	
	public static void main(String[] args) {
		
		System.out.println(SerialNumberGenerator.generateSerialNumber(12));
		System.out.println(SerialNumberGenerator.generateSerialNumber(4));
	}
}
