package com.talkweb.scoreManage.po.gm;

import com.alibaba.fastjson.JSONObject;

public class TopGroupBj {
	private String kslc; // 考试轮次代码
	private String xxdm; // 学校代码
	private String bmfz; // 分组代码
	private String bh; // 班级代码
	private String fw; // 范围
	private String xnxq; // 学年学期

	private TopGroup topGroup = new TopGroup();

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

	public String getBmfz() {
		return bmfz;
	}

	public void setBmfz(String bmfz) {
		this.bmfz = bmfz;
	}

	public String getBh() {
		return bh;
	}

	public void setBh(String bh) {
		this.bh = bh;
	}

	public String getFw() {
		return fw;
	}

	public void setFw(String fw) {
		this.fw = fw;
	}

	public String getXnxq() {
		return xnxq;
	}

	public void setXnxq(String xnxq) {
		this.xnxq = xnxq;
	}

	public TopGroup getTopGroup() {
		return topGroup;
	}

	public void setTopGroup(TopGroup topGroup) {
		this.topGroup = topGroup;
	}

	public String toString() {
		return JSONObject.toJSONString(this);
	}
}
