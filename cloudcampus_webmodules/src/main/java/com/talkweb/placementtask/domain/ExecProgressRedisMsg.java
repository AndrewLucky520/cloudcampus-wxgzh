package com.talkweb.placementtask.domain;

import java.io.Serializable;

import com.alibaba.fastjson.JSON;

public class ExecProgressRedisMsg implements Serializable {
	private static final long serialVersionUID = 3466478893955350989L;

	private int code = 1;
	private double progress = 0;
	private String message = null;
	private String errorMsg = null;

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public double getProgress() {
		return progress;
	}

	public void setProgress(double progress) {
		this.progress = progress;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}
	
	@Override
	public String toString(){
		return JSON.toJSONString(this);
	}
}
