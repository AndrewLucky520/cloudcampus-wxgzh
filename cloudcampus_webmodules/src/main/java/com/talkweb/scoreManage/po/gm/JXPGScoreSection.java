package com.talkweb.scoreManage.po.gm;

import com.alibaba.fastjson.JSONObject;

public class JXPGScoreSection {
	private String xxdm; // 学校代码
	private String dm; // 代码
	private String fs; // 设置方式
	private String mc; // 名称
	private Float fzbfb; // 总分/人数百分比
	private String flag; // 标志

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

	public String getFs() {
		return fs;
	}

	public void setFs(String fs) {
		this.fs = fs;
	}

	public String getMc() {
		return mc;
	}

	public void setMc(String mc) {
		this.mc = mc;
	}

	public Float getFzbfb() {
		return fzbfb;
	}

	public void setFzbfb(Float fzbfb) {
		this.fzbfb = fzbfb;
	}

	public String getFlag() {
		return flag;
	}

	public void setFlag(String flag) {
		this.flag = flag;
	}

	public String toString() {
		return JSONObject.toJSONString(this);
	}
}
