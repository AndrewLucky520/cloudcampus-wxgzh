package com.talkweb.scoreManage.po.gm;

import com.alibaba.fastjson.JSONObject;

// 学生单科统计结果
public class ScoreStuStatisticsRankMk {
	private String kslc; // 考试轮次代码
	private String xxdm; // 学校代码
	private String xh; // 学号
	private String kmdm; // 科目代码
	private Float cj; // 排名成绩
	private String dj; // 成绩等级
	private Integer bjpm; // 班级排名
	private Integer njpm; // 年级排名
	private Integer bjpmgz; // 班级排名跟踪
	private Integer njpmgz; // 年级排名跟踪
	private String sftj; // 是否参与统计
	private Float bzcj; // 标准成绩
	private Integer bzbjpm; // 标准班级排名
	private Integer bznjpm; // 标准年级排名
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

	public String getXh() {
		return xh;
	}

	public void setXh(String xh) {
		this.xh = xh;
	}

	public String getKmdm() {
		return kmdm;
	}

	public void setKmdm(String kmdm) {
		this.kmdm = kmdm;
	}

	public Float getCj() {
		return cj;
	}

	public void setCj(Float cj) {
		this.cj = cj;
	}

	public String getDj() {
		return dj;
	}

	public void setDj(String dj) {
		this.dj = dj;
	}

	public Integer getBjpm() {
		return bjpm;
	}

	public void setBjpm(Integer bjpm) {
		this.bjpm = bjpm;
	}

	public Integer getNjpm() {
		return njpm;
	}

	public void setNjpm(Integer njpm) {
		this.njpm = njpm;
	}

	public Integer getBjpmgz() {
		return bjpmgz;
	}

	public void setBjpmgz(Integer bjpmgz) {
		this.bjpmgz = bjpmgz;
	}

	public Integer getNjpmgz() {
		return njpmgz;
	}

	public void setNjpmgz(Integer njpmgz) {
		this.njpmgz = njpmgz;
	}

	public String getSftj() {
		return sftj;
	}

	public void setSftj(String sftj) {
		this.sftj = sftj;
	}

	public Float getBzcj() {
		return bzcj;
	}

	public void setBzcj(Float bzcj) {
		this.bzcj = bzcj;
	}

	public Integer getBzbjpm() {
		return bzbjpm;
	}

	public void setBzbjpm(Integer bzbjpm) {
		this.bzbjpm = bzbjpm;
	}

	public Integer getBznjpm() {
		return bznjpm;
	}

	public void setBznjpm(Integer bznjpm) {
		this.bznjpm = bznjpm;
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
