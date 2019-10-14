package com.talkweb.scoreManage.po.gm;

import com.alibaba.fastjson.JSONObject;

public class DegreeInfoNj {
	private String kslcdm; // 开始轮次代码
	private String nj; // 使用年级
	private String xxdm; // 学校代码
	private String mrszflag; // 成绩分析默认标志
	private String xnxq; // 学年学期

	public String getKslcdm() {
		return kslcdm;
	}

	public void setKslcdm(String kslcdm) {
		this.kslcdm = kslcdm;
	}

	public String getNj() {
		return nj;
	}

	public void setNj(String nj) {
		this.nj = nj;
	}

	public String getXxdm() {
		return xxdm;
	}

	public void setXxdm(String xxdm) {
		this.xxdm = xxdm;
	}

	public String getMrszflag() {
		return mrszflag;
	}

	public void setMrszflag(String mrszflag) {
		this.mrszflag = mrszflag;
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
