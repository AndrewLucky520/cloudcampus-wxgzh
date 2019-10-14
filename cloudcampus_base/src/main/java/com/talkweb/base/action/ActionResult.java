package com.talkweb.base.action;

import java.util.Map;

public abstract class ActionResult {

	private int code;
	private Map<String,Object> data;
	
	public ActionResult() {
		super();
	}
	public ActionResult(int code, Map<String, Object> data) {
		super();
		this.code = code;
		this.data = data;
	}
	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
	public Map<String, Object> getData() {
		return data;
	}
	public void setData(Map<String, Object> data) {
		this.data = data;
	}
	
	
}
