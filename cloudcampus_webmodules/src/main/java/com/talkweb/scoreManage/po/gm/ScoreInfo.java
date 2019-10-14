package com.talkweb.scoreManage.po.gm;

import java.io.Serializable;

import com.alibaba.fastjson.JSONObject;

public class ScoreInfo implements Serializable {
	private static final long serialVersionUID = 8967082704610339526L;
	
	private String kslc; // 考试轮次
	private String xxdm; // 学校代码
	private String kmdm; // 科目代码
	private String xh; // 学号
	private String bh; // 班号
	private String nj; // 年级
	private Float cj; // 成绩
	private Integer mf; // 满分
	private String tsqk; // 特殊情况
	private String bz; // 备注
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

	public String getKmdm() {
		return kmdm;
	}

	public void setKmdm(String kmdm) {
		this.kmdm = kmdm;
	}

	public String getXh() {
		return xh;
	}

	public void setXh(String xh) {
		this.xh = xh;
	}

	public String getBh() {
		return bh;
	}

	public void setBh(String bh) {
		this.bh = bh;
	}

	public String getNj() {
		return nj;
	}

	public void setNj(String nj) {
		this.nj = nj;
	}

	public Float getCj() {
		return cj;
	}

	public void setCj(Float cj) {
		this.cj = cj;
	}

	public Integer getMf() {
		return mf;
	}

	public void setMf(Integer mf) {
		this.mf = mf;
	}

	public String getTsqk() {
		return tsqk;
	}

	public void setTsqk(String tsqk) {
		this.tsqk = tsqk;
	}

	public String getBz() {
		return bz;
	}

	public void setBz(String bz) {
		this.bz = bz;
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
