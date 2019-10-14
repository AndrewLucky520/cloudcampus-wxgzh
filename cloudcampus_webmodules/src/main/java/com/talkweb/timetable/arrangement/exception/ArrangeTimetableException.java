package com.talkweb.timetable.arrangement.exception;

import java.util.ArrayList;
import java.util.List;

public class ArrangeTimetableException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6591469027109887355L;

	private int code = 1;
	
	private List<String>errorList = new ArrayList<String>();
	
	public ArrangeTimetableException(String message){
		super(message);
	}
	
	public ArrangeTimetableException(String message, Throwable cause){
		super(message, cause);
	}
	
	public ArrangeTimetableException(int code, String message){
		super(message);
		this.code = code;
	}
	
	public ArrangeTimetableException(int code, List<String> errorList){
		super();
		this.code = code;
		this.errorList = errorList;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public List<String> getErrorList() {
		return errorList;
	}

	public void setErrorList(List<String> errorList) {
		this.errorList = errorList;
	}
	
	
	
	
	
}
