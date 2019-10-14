package com.talkweb.scoreManage.po.sar;

import com.alibaba.fastjson.JSONObject;

public class SettingBJ {
	private String xxdm; // 学校代码
	private String dm; // 代码
	private Integer szz; // 设置值

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

	public Integer getSzz() {
		return szz;
	}

	public void setSzz(Integer szz) {
		this.szz = szz;
	}

	public String toString() {
		return JSONObject.toJSONString(this);
	}
}
