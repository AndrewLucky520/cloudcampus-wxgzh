package com.talkweb.scoreManage.po.gm;

import com.alibaba.fastjson.JSONObject;

// 成绩统计条件表
public class ScoreStatTerm {
	private String xxdm; // 学校代码
	private String qk; // 缺考
	private String wb; // 舞弊
	private String lf; // 零分

	public String getXxdm() {
		return xxdm;
	}

	public void setXxdm(String xxdm) {
		this.xxdm = xxdm;
	}

	public String getQk() {
		return qk;
	}

	public void setQk(String qk) {
		this.qk = qk;
	}

	public String getWb() {
		return wb;
	}

	public void setWb(String wb) {
		this.wb = wb;
	}

	public String getLf() {
		return lf;
	}

	public void setLf(String lf) {
		this.lf = lf;
	}

	public String toString() {
		return JSONObject.toJSONString(this);
	}
}
