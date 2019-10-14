package com.talkweb.scoreManage.po.gm;

import com.alibaba.fastjson.JSONObject;

// 总分区间设置
public class Zfqjsz {
	private String kslc; // 考试轮次代码
	private String xxdm; // 学校代码
	private String nj; // 使用年级
	private String fzdm; // 分组代码
	private String fs; // 总分区间设置方式
	private Integer bl; // 间隔分值比例
	private Integer zhqjbl; // 最后区间比例
	private Integer fz; // 间隔分值
	private Integer zhqjfz; // 最后区间分值
	private String zdytext; // 自定义间隔值
	private Integer dkbl; // 单科间隔比例
	private Integer dkzhqjbl; // 单科最后区间比例
	private String xnxq; // 学年学期
	
	private Object otherData;

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

	public String getFs() {
		return fs;
	}

	public void setFs(String fs) {
		this.fs = fs;
	}

	public Integer getBl() {
		return bl;
	}

	public void setBl(Integer bl) {
		this.bl = bl;
	}

	public Integer getZhqjbl() {
		return zhqjbl;
	}

	public void setZhqjbl(Integer zhqjbl) {
		this.zhqjbl = zhqjbl;
	}

	public Integer getFz() {
		return fz;
	}

	public void setFz(Integer fz) {
		this.fz = fz;
	}

	public Integer getZhqjfz() {
		return zhqjfz;
	}

	public void setZhqjfz(Integer zhqjfz) {
		this.zhqjfz = zhqjfz;
	}

	public String getZdytext() {
		return zdytext;
	}

	public void setZdytext(String zdytext) {
		this.zdytext = zdytext;
	}

	public Integer getDkbl() {
		return dkbl;
	}

	public void setDkbl(Integer dkbl) {
		this.dkbl = dkbl;
	}

	public Integer getDkzhqjbl() {
		return dkzhqjbl;
	}

	public void setDkzhqjbl(Integer dkzhqjbl) {
		this.dkzhqjbl = dkzhqjbl;
	}

	public String getXnxq() {
		return xnxq;
	}

	public void setXnxq(String xnxq) {
		this.xnxq = xnxq;
	}

	public Object getOtherData() {
		return otherData;
	}

	public void setOtherData(Object otherData) {
		this.otherData = otherData;
	}

	public String toString() {
		return JSONObject.toJSONString(this);
	}
}
