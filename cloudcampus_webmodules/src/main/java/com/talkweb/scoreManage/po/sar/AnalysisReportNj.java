package com.talkweb.scoreManage.po.sar;

import com.alibaba.fastjson.JSONObject;

// 年级成绩报告结果表
public class AnalysisReportNj {
	private String xxdm; // 学校代码
	private String kslc; // 考试轮次
	private String nj; // 使用年级
	private String fzdm; // 分组代码
	private String bjtsy; // 班级角度提示语
	private String pjfdbb; // 平均分对比表
	private String yxldbb; // 优秀率对比表
	private String hgldbb; // 合格率对比表
	private String ddzdbb; // 等第值对比表
	private String pmdbb; // 排名对比表
	private String kmtsy; // 学科角度提示语
	private String kmdbb; // 学科对比表
	private String jstsy; // 教师角度提示语
	private String jskmdbb; // 教师学科对比表
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

	public String getNj() {
		return nj;
	}

	public void setNj(String nj) {
		this.nj = nj;
	}

	public String getFzdm() {
		return fzdm;
	}

	public void setFzdm(String fzdm) {
		this.fzdm = fzdm;
	}

	public String getBjtsy() {
		return bjtsy;
	}

	public void setBjtsy(String bjtsy) {
		this.bjtsy = bjtsy;
	}

	public String getPjfdbb() {
		return pjfdbb;
	}

	public void setPjfdbb(String pjfdbb) {
		this.pjfdbb = pjfdbb;
	}

	public String getYxldbb() {
		return yxldbb;
	}

	public void setYxldbb(String yxldbb) {
		this.yxldbb = yxldbb;
	}

	public String getHgldbb() {
		return hgldbb;
	}

	public void setHgldbb(String hgldbb) {
		this.hgldbb = hgldbb;
	}

	public String getDdzdbb() {
		return ddzdbb;
	}

	public void setDdzdbb(String ddzdbb) {
		this.ddzdbb = ddzdbb;
	}

	public String getPmdbb() {
		return pmdbb;
	}

	public void setPmdbb(String pmdbb) {
		this.pmdbb = pmdbb;
	}

	public String getKmtsy() {
		return kmtsy;
	}

	public void setKmtsy(String kmtsy) {
		this.kmtsy = kmtsy;
	}

	public String getKmdbb() {
		return kmdbb;
	}

	public void setKmdbb(String kmdbb) {
		this.kmdbb = kmdbb;
	}

	public String getJstsy() {
		return jstsy;
	}

	public void setJstsy(String jstsy) {
		this.jstsy = jstsy;
	}

	public String getJskmdbb() {
		return jskmdbb;
	}

	public void setJskmdbb(String jskmdbb) {
		this.jskmdbb = jskmdbb;
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
