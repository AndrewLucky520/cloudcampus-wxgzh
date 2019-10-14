package com.talkweb.scoreManage.po.gm;

import com.alibaba.fastjson.JSONObject;

// 单科成绩分布
public class ScoreDistributeKm {
	private String kslc; // 考试轮次代码
	private String xxdm; // 学校代码
	private String dm; // 代码
	private String fbdm; // 分布代码
	private Integer fbsx; // 分数上限
	private Integer fbxx; // 分数下限
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

	public String getDm() {
		return dm;
	}

	public void setDm(String dm) {
		this.dm = dm;
	}

	public String getFbdm() {
		return fbdm;
	}

	public void setFbdm(String fbdm) {
		this.fbdm = fbdm;
	}

	public Integer getFbsx() {
		return fbsx;
	}

	public void setFbsx(Integer fbsx) {
		this.fbsx = fbsx;
	}

	public Integer getFbxx() {
		return fbxx;
	}

	public void setFbxx(Integer fbxx) {
		this.fbxx = fbxx;
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
