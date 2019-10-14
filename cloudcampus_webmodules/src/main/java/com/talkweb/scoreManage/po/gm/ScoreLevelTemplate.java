package com.talkweb.scoreManage.po.gm;

import com.alibaba.fastjson.JSONObject;

// 成绩等级模板表
public class ScoreLevelTemplate {
	private String kslc; // 考试轮次代码
	private String xxdm; // 学校代码
	private String nj; // 使用年级
	private String kmdm; // 科目代码
	private String dm; // 等级代码
	private String djmc; // 等级名称
	private Float bl; // 折算比例
	private Float cj; // 折算成绩
	private Integer rs; // 人数
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

	public String getKmdm() {
		return kmdm;
	}

	public void setKmdm(String kmdm) {
		this.kmdm = kmdm;
	}

	public String getDm() {
		return dm;
	}

	public void setDm(String dm) {
		this.dm = dm;
	}

	public String getDjmc() {
		return djmc;
	}

	public void setDjmc(String djmc) {
		this.djmc = djmc;
	}

	public Float getBl() {
		return bl;
	}

	public void setBl(Float bl) {
		this.bl = bl;
	}

	public Float getCj() {
		return cj;
	}

	public void setCj(Float cj) {
		this.cj = cj;
	}

	public Integer getRs() {
		return rs;
	}

	public void setRs(Integer rs) {
		this.rs = rs;
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
