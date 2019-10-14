package com.talkweb.scoreManage.po.gm;

import com.alibaba.fastjson.JSONObject;

// 成绩等级序列
public class ScoreLevelSequnce {
	private String kslc; // 考试轮次代码
	private String xxdm; // 学校代码
	private String nj; // 年级
	private String djxllb; // 类别
	private String djxl; // 等级序列
	private Integer djqz; // 等级序列权重
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

	public String getDjxllb() {
		return djxllb;
	}

	public void setDjxllb(String djxllb) {
		this.djxllb = djxllb;
	}

	public String getDjxl() {
		return djxl;
	}

	public void setDjxl(String djxl) {
		this.djxl = djxl;
	}

	public Integer getDjqz() {
		return djqz;
	}

	public void setDjqz(Integer djqz) {
		this.djqz = djqz;
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
