package com.talkweb.scoreManage.po.gm;

import com.alibaba.fastjson.JSONObject;

// 年级单科排名分布
public class ScoreRankStatisticsMk {
	private String kslc; // 考试轮次
	private String xxdm; // 学校代码
	private String bh; // 班号
	private String kmdm; // 科目代码
	private String pmfbdm; // 分布代码
	private Integer rs; // 人数
	private Integer ljrs; // 在排名下限之上人数
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

	public String getKmdm() {
		return kmdm;
	}

	public void setKmdm(String kmdm) {
		this.kmdm = kmdm;
	}

	public String getPmfbdm() {
		return pmfbdm;
	}

	public void setPmfbdm(String pmfbdm) {
		this.pmfbdm = pmfbdm;
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
