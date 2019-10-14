package com.talkweb.scoreManage.po.sar;

import com.alibaba.fastjson.JSONObject;

// 学生成绩报告结果表
public class AnalysisReportStu {
	private String xxdm; // 学校代码
	private String kslc; // 考试轮次
	private String xh; // 学号
	private String cjd; // 成绩单
	private String jbtsy; // 进步提示语
	private String tbtsy; // 退步提示语
	private String kmylsj; // 科目优劣数据
	private String kmyltsy; // 科目优劣提示语
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

	public String getXh() {
		return xh;
	}

	public void setXh(String xh) {
		this.xh = xh;
	}

	public String getCjd() {
		return cjd;
	}

	public void setCjd(String cjd) {
		this.cjd = cjd;
	}

	public String getJbtsy() {
		return jbtsy;
	}

	public void setJbtsy(String jbtsy) {
		this.jbtsy = jbtsy;
	}

	public String getTbtsy() {
		return tbtsy;
	}

	public void setTbtsy(String tbtsy) {
		this.tbtsy = tbtsy;
	}

	public String getKmylsj() {
		return kmylsj;
	}

	public void setKmylsj(String kmylsj) {
		this.kmylsj = kmylsj;
	}

	public String getKmyltsy() {
		return kmyltsy;
	}

	public void setKmyltsy(String kmyltsy) {
		this.kmyltsy = kmyltsy;
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
