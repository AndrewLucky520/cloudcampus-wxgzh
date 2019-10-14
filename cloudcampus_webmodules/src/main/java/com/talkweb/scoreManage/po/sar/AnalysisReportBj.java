package com.talkweb.scoreManage.po.sar;

import com.alibaba.fastjson.JSONObject;

// 班级成绩报告结果表
public class AnalysisReportBj {
	private String xxdm; // 学校代码
	private String kslc; // 考试轮次
	private String bh; // 班号
	private String ksqk; // 考试情况
	private String xsycqk; // 学生异常情况
	private String cjgzsj; // 成绩跟踪数据
	private String mcdrs; // 各科名次段人数
	private String zfdrs; // 各班总分段人数
	private String reportdata; // App班级报告结果数据
	private String xnxq; // 学年学期

	public String getXxdm() {
		return xxdm;
	}

	public void setXxdm(String xxdm) {
		this.xxdm = xxdm;
	}

	public String getKslc() {
		return kslc;
	}

	public void setKslc(String kslc) {
		this.kslc = kslc;
	}

	public String getBh() {
		return bh;
	}

	public void setBh(String bh) {
		this.bh = bh;
	}

	public String getKsqk() {
		return ksqk;
	}

	public void setKsqk(String ksqk) {
		this.ksqk = ksqk;
	}

	public String getXsycqk() {
		return xsycqk;
	}

	public void setXsycqk(String xsycqk) {
		this.xsycqk = xsycqk;
	}

	public String getCjgzsj() {
		return cjgzsj;
	}

	public void setCjgzsj(String cjgzsj) {
		this.cjgzsj = cjgzsj;
	}

	public String getMcdrs() {
		return mcdrs;
	}

	public void setMcdrs(String mcdrs) {
		this.mcdrs = mcdrs;
	}

	public String getZfdrs() {
		return zfdrs;
	}

	public void setZfdrs(String zfdrs) {
		this.zfdrs = zfdrs;
	}

	public String getReportdata() {
		return reportdata;
	}

	public void setReportdata(String reportdata) {
		this.reportdata = reportdata;
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
