package com.talkweb.scoreManage.po.gm;

import java.io.Serializable;

import com.alibaba.fastjson.JSONObject;

public class CompetitionStu implements Serializable {
	private static final long serialVersionUID = -8184194735430541054L;
	
	private String xxdm; // 学校代码
	private String kslc; // 考试轮次代码
	private String xh; // 学生代码
	private String kmdm; // 科目代码
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

	public String getKmdm() {
		return kmdm;
	}

	public void setKmdm(String kmdm) {
		this.kmdm = kmdm;
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
