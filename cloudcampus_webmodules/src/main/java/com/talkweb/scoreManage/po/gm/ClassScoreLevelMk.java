package com.talkweb.scoreManage.po.gm;

import com.alibaba.fastjson.JSONObject;

// 班级科目等级统计表
public class ClassScoreLevelMk {
	private String kslc; // 考试轮次
	private String xxdm; // 学校代码
	private String bh; // 班号
	private String kmdm; // 科目代码
	private String djdm; // 等级代码
	private String dj; // 等级名称
	private Integer rs; // 人数
	private Float bl; // 比率
	private Float lc; // 率差
	private Integer pm; // 排名
	private Integer ljrs; // 累计人数
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

	public String getDjdm() {
		return djdm;
	}

	public void setDjdm(String djdm) {
		this.djdm = djdm;
	}

	public String getDj() {
		return dj;
	}

	public void setDj(String dj) {
		this.dj = dj;
	}

	public Integer getRs() {
		return rs;
	}

	public void setRs(Integer rs) {
		this.rs = rs;
	}

	public Float getBl() {
		return bl;
	}

	public void setBl(Float bl) {
		this.bl = bl;
	}

	public Float getLc() {
		return lc;
	}

	public void setLc(Float lc) {
		this.lc = lc;
	}

	public Integer getPm() {
		return pm;
	}

	public void setPm(Integer pm) {
		this.pm = pm;
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
