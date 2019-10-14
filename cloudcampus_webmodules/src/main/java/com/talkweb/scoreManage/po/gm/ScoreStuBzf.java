package com.talkweb.scoreManage.po.gm;

import com.alibaba.fastjson.JSONObject;

// 学生总分标准分
public class ScoreStuBzf {
	private String kslc; // 考试轮次
	private String xxdm; // 学校代码
	private String xh; // 学号
	private Float bzf; // 标准分
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

	public String getXh() {
		return xh;
	}

	public void setXh(String xh) {
		this.xh = xh;
	}

	public Float getBzf() {
		return bzf;
	}

	public void setBzf(Float bzf) {
		this.bzf = bzf;
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
