package com.talkweb.base.action;

import java.util.List;
import java.util.Map;

public class ScoreActionResult extends ActionResult {

	private List<Object> subjectInfo;

	public ScoreActionResult() {
		super();
	}

	public ScoreActionResult(List<Object> subjectInfo) {
		super();
		this.subjectInfo = subjectInfo;
	}

	public ScoreActionResult(int code, Map<String, Object> data) {
		super(code, data);
	}

	public ScoreActionResult(int code, Map<String, Object> data,List<Object> subjectInfo) {
		super(code, data);
		this.subjectInfo = subjectInfo;
	}

	public List<Object> getSubjectInfo() {
		return subjectInfo;
	}

	public void setSubjectInfo(List<Object> subjectInfo) {
		this.subjectInfo = subjectInfo;
	}
	
	
}
