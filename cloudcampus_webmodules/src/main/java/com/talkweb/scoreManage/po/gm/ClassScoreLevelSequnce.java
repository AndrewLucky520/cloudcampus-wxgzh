package com.talkweb.scoreManage.po.gm;

import com.alibaba.fastjson.JSONObject;

// 班级总分等级顺列统计表
public class ClassScoreLevelSequnce {
	private String kslc; // 考试轮次
	private String xxdm; // 学校代码
	private String bh; // 班级代码
	private String djxllb; // 序列类别
	private String djxl; // 等级顺序
	private Integer rs; // 人数
	private Integer ljrs; // 累计人数
	private Float bl; // 比例
	private Integer pm; // 排名
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

	public Float getBl() {
		return bl;
	}

	public void setBl(Float bl) {
		this.bl = bl;
	}

	public Integer getPm() {
		return pm;
	}

	public void setPm(Integer pm) {
		this.pm = pm;
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
