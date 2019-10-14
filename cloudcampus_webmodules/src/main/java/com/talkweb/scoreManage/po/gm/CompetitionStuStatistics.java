package com.talkweb.scoreManage.po.gm;

import com.alibaba.fastjson.JSONObject;

// 竞赛学生成绩分析结果表
public class CompetitionStuStatistics {
	private String xxdm; // 学校代码
	private String kslc; // 考试轮次
	private String fzdm; // 分组代码
	private String kmdm; // 科目代码
	private String kmzmc; // 科目组名称
	private Float pjf; // 平均分
	private Integer yxrs; // 优秀人数
	private Float yxl; // 优秀率
	private Integer hgrs; // 合格人数
	private Float hgl; // 合格率
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

	public String getFzdm() {
		return fzdm;
	}

	public void setFzdm(String fzdm) {
		this.fzdm = fzdm;
	}

	public String getKmdm() {
		return kmdm;
	}

	public void setKmdm(String kmdm) {
		this.kmdm = kmdm;
	}

	public String getKmzmc() {
		return kmzmc;
	}

	public void setKmzmc(String kmzmc) {
		this.kmzmc = kmzmc;
	}

	public Float getPjf() {
		return pjf;
	}

	public void setPjf(Float pjf) {
		this.pjf = pjf;
	}

	public Integer getYxrs() {
		return yxrs;
	}

	public void setYxrs(Integer yxrs) {
		this.yxrs = yxrs;
	}

	public Float getYxl() {
		return yxl;
	}

	public void setYxl(Float yxl) {
		this.yxl = yxl;
	}

	public Integer getHgrs() {
		return hgrs;
	}

	public void setHgrs(Integer hgrs) {
		this.hgrs = hgrs;
	}

	public Float getHgl() {
		return hgl;
	}

	public void setHgl(Float hgl) {
		this.hgl = hgl;
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
