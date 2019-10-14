package com.talkweb.scoreManage.po.gm;

import com.alibaba.fastjson.JSONObject;

// 总分对应成绩分布种类
public class Zfmfqj {
	private String kslc; // 考试轮次代码
	private String xxdm; // 学校代码
	private String nj; // 年级
	private String fzdm; // 分组代码
	private String dm; // 代码
	private Integer mf; // 满分
	private String flag; // 标识
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

	public String getDm() {
		return dm;
	}

	public void setDm(String dm) {
		this.dm = dm;
	}

	public Integer getMf() {
		return mf;
	}

	public void setMf(Integer mf) {
		this.mf = mf;
	}

	public String getFlag() {
		return flag;
	}

	public void setFlag(String flag) {
		this.flag = flag;
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
