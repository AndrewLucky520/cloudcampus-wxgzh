package com.talkweb.exammanagement.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSONObject;

public class ArrangeExamLog implements Serializable{
	private static final long serialVersionUID = -2741697623815059503L;
	
	private int type;
	private String gradeName;
	
	private List<String> message = new ArrayList<String>();

	private int completedNumOfStuds = 0;
	private int totalNumOfStuds = 0;
	
	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getGradeName() {
		return gradeName;
	}

	public void setGradeName(String gradeName) {
		this.gradeName = gradeName;
	}

	public void addMessage(String message) {
		this.message.add(message);
	}

	public List<String> getMessage() {
		return message;
	}

	public int getCompletedNumOfStuds() {
		return completedNumOfStuds;
	}

	public void setCompletedNumOfStuds(int completedNumOfStuds) {
		this.completedNumOfStuds = completedNumOfStuds;
	}

	public int getTotalNumOfStuds() {
		return totalNumOfStuds;
	}

	public void setTotalNumOfStuds(int totalNumOfStuds) {
		this.totalNumOfStuds = totalNumOfStuds;
	}

	public String toString() {
		return JSONObject.toJSONString(this);
	}
}
