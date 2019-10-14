package com.talkweb.placementtask.domain;

import java.io.Serializable;

import com.alibaba.fastjson.JSON;

public class ScoreList implements Serializable {
	private static final long serialVersionUID = -8604483686224682055L;

	private String examId;
	private String examName;

	public String getExamId() {
		return examId;
	}

	public void setExamId(String examId) {
		this.examId = examId;
	}

	public String getExamName() {
		return examName;
	}

	public void setExamName(String examName) {
		this.examName = examName;
	}

	@Override
	public String toString() {
		return JSON.toJSONString(this);
	}
}
