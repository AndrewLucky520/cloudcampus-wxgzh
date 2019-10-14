package com.talkweb.scoreManage.po.gm;

import com.alibaba.fastjson.JSONObject;

// 综合成绩设置表
public class SynthScore {
	private String kslc; // 考试轮次代码
	private String xxdm; // 学校代码
	private String nj; // 使用年级
	private String fzdm; // 分组代码
	private String kmdm; // 科目代码
	private String dykm; // 综合成绩来源模块列表
	private String xnxq; // 学年学期

	private Object otherData;

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

	public String getNj() {
		return nj;
	}

	public void setNj(String nj) {
		this.nj = nj;
	}

	public String getFzdm() {
		return fzdm;
	}

	public void setFzdm(String fzdm) {
		this.fzdm = fzdm;
	}

	public String getKmdm() {
		return kmdm;
	}

	public void setKmdm(String kmdm) {
		this.kmdm = kmdm;
	}

	public String getDykm() {
		return dykm;
	}

	public void setDykm(String dykm) {
		this.dykm = dykm;
	}

	public String getXnxq() {
		return xnxq;
	}

	public void setXnxq(String xnxq) {
		this.xnxq = xnxq;
	}

	public Object getOtherData() {
		return otherData;
	}

	public void setOtherData(Object otherData) {
		this.otherData = otherData;
	}

	public String toString() {
		return JSONObject.toJSONString(this);
	}
}
