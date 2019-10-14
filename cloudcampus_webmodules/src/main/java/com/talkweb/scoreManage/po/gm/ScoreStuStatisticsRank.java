package com.talkweb.scoreManage.po.gm;

import com.alibaba.fastjson.JSONObject;

// 学生总分统计结果
public class ScoreStuStatisticsRank {
	private String kslc; // 考试轮次代码
	private String xxdm; // 学校代码
	private String xh; // 学号
	private Float zf; // 总分
	private String djxl; // 一般科目等级序列
	private String djxl2; // 带综合科目的等级序列
	private String djxl3; // 语数外科目等级序列
	private Integer bjpm; // 班级排名
	private Integer njpm; // 年级排名
	private Integer bjpmgz; // 班级排名跟踪
	private Integer njpmgz; // 年级排名跟踪
	private String dbkslc; // 对比考试轮次代码
	private String dbxnxq; // 对比学年学期
	private String sftj; // 是否参与统计
	private Float bzzcj; // 标准成绩
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

	public Float getZf() {
		return zf;
	}

	public void setZf(Float zf) {
		this.zf = zf;
	}

	public String getDjxl() {
		return djxl;
	}

	public void setDjxl(String djxl) {
		this.djxl = djxl;
	}

	public String getDjxl2() {
		return djxl2;
	}

	public void setDjxl2(String djxl2) {
		this.djxl2 = djxl2;
	}

	public String getDjxl3() {
		return djxl3;
	}

	public void setDjxl3(String djxl3) {
		this.djxl3 = djxl3;
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

	public String getDbkslc() {
		return dbkslc;
	}

	public void setDbkslc(String dbkslc) {
		this.dbkslc = dbkslc;
	}

	public String getDbxnxq() {
		return dbxnxq;
	}

	public void setDbxnxq(String dbxnxq) {
		this.dbxnxq = dbxnxq;
	}

	public String getSftj() {
		return sftj;
	}

	public void setSftj(String sftj) {
		this.sftj = sftj;
	}

	public Float getBzzcj() {
		return bzzcj;
	}

	public void setBzzcj(Float bzzcj) {
		this.bzzcj = bzzcj;
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
