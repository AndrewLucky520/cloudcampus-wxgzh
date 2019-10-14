package com.talkweb.scoreManage.po.gm;

import com.alibaba.fastjson.JSONObject;

public class TopGroup {
	private String kslc; // 考试轮次代码
	private String xxdm; // 学校代码
	private String bmfz; // 分组代码
	private String fzmc; // 分组名称
	private String nj; // 年级
	private String bjlxm; // 班级类型代码
	private Integer fw; // 统计范围
	private String fs; // 设置方式
	private String ssfz; // 所属分组
	private String lb; // 类别
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

	public String getBmfz() {
		return bmfz;
	}

	public void setBmfz(String bmfz) {
		this.bmfz = bmfz;
	}

	public String getFzmc() {
		return fzmc;
	}

	public void setFzmc(String fzmc) {
		this.fzmc = fzmc;
	}

	public String getNj() {
		return nj;
	}

	public void setNj(String nj) {
		this.nj = nj;
	}

	public String getBjlxm() {
		return bjlxm;
	}

	public void setBjlxm(String bjlxm) {
		this.bjlxm = bjlxm;
	}

	public Integer getFw() {
		return fw;
	}

	public void setFw(Integer fw) {
		this.fw = fw;
	}

	public String getFs() {
		return fs;
	}

	public void setFs(String fs) {
		this.fs = fs;
	}

	public String getSsfz() {
		return ssfz;
	}

	public void setSsfz(String ssfz) {
		this.ssfz = ssfz;
	}

	public String getLb() {
		return lb;
	}

	public void setLb(String lb) {
		this.lb = lb;
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
