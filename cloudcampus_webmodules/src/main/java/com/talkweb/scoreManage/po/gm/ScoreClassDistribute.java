package com.talkweb.scoreManage.po.gm;

import com.alibaba.fastjson.JSONObject;

// 班级总分分布
public class ScoreClassDistribute {
	private String kslc; // 考试轮次
	private String xxdm; // 学校代码
	private String bh; // 班号
	private String dm; // 代码
	private String fbdm; // 分布代码
	private Integer rs; // 区间人数
	private Integer ljrs; // 在分数下限之上人数
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

	public String getBh() {
		return bh;
	}

	public void setBh(String bh) {
		this.bh = bh;
	}

	public String getDm() {
		return dm;
	}

	public void setDm(String dm) {
		this.dm = dm;
	}

	public String getFbdm() {
		return fbdm;
	}

	public void setFbdm(String fbdm) {
		this.fbdm = fbdm;
	}

	public Integer getRs() {
		return rs;
	}

	public void setRs(Integer rs) {
		this.rs = rs;
	}

	public Integer getLjrs() {
		return ljrs;
	}

	public void setLjrs(Integer ljrs) {
		this.ljrs = ljrs;
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
