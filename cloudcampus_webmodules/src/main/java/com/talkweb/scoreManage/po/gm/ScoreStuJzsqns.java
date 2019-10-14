package com.talkweb.scoreManage.po.gm;

import com.alibaba.fastjson.JSONObject;

// 总分尖子/潜能生表
public class ScoreStuJzsqns {
	private String kslc; // 考试轮次
	private String xxdm; // 学校代码
	private String xh; // 学号
	private String lb; // 类别
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

	public String getLb() {
		return lb;
	}

	public void setLb(String lb) {
		this.lb = lb;
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
