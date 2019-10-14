package com.talkweb.scoreManage.po.gm;

import com.alibaba.fastjson.JSONObject;

public class ScoreRankDistribute {
	private String kslc; // 考试轮次代码
	private String xxdm; // 学校代码
	private String nj; // 使用年级
	private String fzdm; // 分组代码
	private String pmfbdm; // 分布代码
	private Integer pmfbsx; // 排名上限
	private Integer pmfbxx; // 排名下限
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

	public String getPmfbdm() {
		return pmfbdm;
	}

	public void setPmfbdm(String pmfbdm) {
		this.pmfbdm = pmfbdm;
	}

	public Integer getPmfbsx() {
		return pmfbsx;
	}

	public void setPmfbsx(Integer pmfbsx) {
		this.pmfbsx = pmfbsx;
	}

	public Integer getPmfbxx() {
		return pmfbxx;
	}

	public void setPmfbxx(Integer pmfbxx) {
		this.pmfbxx = pmfbxx;
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
