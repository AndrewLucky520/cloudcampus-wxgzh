package com.talkweb.scoreManage.proc;

import java.io.Serializable;

import com.alibaba.fastjson.JSONObject;

public class ExcelErrorInfo implements Serializable {
	private static final long serialVersionUID = 6694470790294584949L;

	private String title; // 标题
	private String titleEnName; // 标题代码
	private String oldValue; // 对应的值
	private String err; // 错误信息

	public ExcelErrorInfo(String title, String oldValue, String err) {
		super();
		this.title = title;
		this.oldValue = oldValue;
		this.err = err;
	}

	public ExcelErrorInfo(String title, String titleEnName, String oldValue, String err) {
		super();
		this.title = title;
		this.titleEnName = titleEnName;
		this.oldValue = oldValue;
		this.err = err;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitleEnName() {
		return titleEnName;
	}

	public void setTitleEnName(String titleEnName) {
		this.titleEnName = titleEnName;
	}

	public String getOldValue() {
		return oldValue;
	}

	public void setOldValue(String oldValue) {
		this.oldValue = oldValue;
	}

	public String getErr() {
		return err;
	}

	public void setErr(String err) {
		this.err = err;
	}

	public String toString() {
		return JSONObject.toJSONString(this);
	}
}
