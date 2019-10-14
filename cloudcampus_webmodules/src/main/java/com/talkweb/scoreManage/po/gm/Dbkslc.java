package com.talkweb.scoreManage.po.gm;

import com.alibaba.fastjson.JSONObject;

public class Dbkslc {
	private String kslc; // 考试轮次代码
	private String xxdm; // 学校代码
	private String dbkslc; // 对比考试轮次代码
	private String dbxnxq; // 对比考试轮次的学年学期
	private String xnxq; // 学年学期

	public String getKslc() {
		return kslc;
	}

	public void setKslc(String kslc) {
		this.kslc = kslc;
	}

	public String getXxdm() {
		return xxdm;
	}

	public void setXxdm(String xxdm) {
		this.xxdm = xxdm;
	}

	public String getDbkslc() {
		return dbkslc;
	}

	public void setDbkslc(String dbkslc) {
		this.dbkslc = dbkslc;
	}

	public String getDbxnxq() {
		return dbxnxq;
	}

	public void setDbxnxq(String dbxnxq) {
		this.dbxnxq = dbxnxq;
	}

	public String getXnxq() {
		return xnxq;
	}

	public void setXnxq(String xnxq) {
		this.xnxq = xnxq;
	}

	public String toString() {
		return JSONObject.toJSONString(this);
	}
}
