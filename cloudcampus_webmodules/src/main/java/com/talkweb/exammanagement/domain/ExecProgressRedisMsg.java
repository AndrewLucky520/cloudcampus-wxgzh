package com.talkweb.exammanagement.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSONObject;

public class ExecProgressRedisMsg implements Serializable {
	private static final long serialVersionUID = -6503563788505173148L;
	
	private int code = 1;
	private double progress = 0;
	private String message = null;
	private String errorMsg = null;
	private List<ArrangeExamLog> logList = new ArrayList<ArrangeExamLog>();
	
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
	
	public List<ArrangeExamLog> getLogList() {
		return logList;
	}

	public void setLogList(List<ArrangeExamLog> logList) {
		this.logList = logList;
	}

	public String toString(){
		return JSONObject.toJSONString(this);
	}
}
