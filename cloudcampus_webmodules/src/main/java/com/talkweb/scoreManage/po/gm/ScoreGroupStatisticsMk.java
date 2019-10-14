package com.talkweb.scoreManage.po.gm;

import com.alibaba.fastjson.JSONObject;

// 年级分组单科成绩统计结果
public class ScoreGroupStatisticsMk {
	private String kslc; // 考试轮次代码
	private String xxdm; // 学校代码
	private String fzdm; // 分组代码
	private String kmdm; // 科目代码
	private Integer ckrs; // 参考人数
	private Integer tjrs; // 统计人数
	private Float pjf; // 平均分
	private Float pjffc; // 平均分分差
	private Float bzc; // 标准差
	private Float yxl; // 优秀率
	private Integer yxrs; // 优秀人数
	private Float yxlc; // 优秀率差
	private Float hgl; // 合格率
	private Integer hgrs; // 合格人数
	private Float hglc; // 合格率差
	private Float dfl; // 低分率
	private Integer dfrs; // 低分人数
	private Float dflc; // 低分率差
	private Float ldxs; // 难度系数
	private Float qfd; // 区分度
	private Integer jzsrs; // 尖子生人数
	private Integer qnsrs; // 潜能生人数
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

	public Integer getCkrs() {
		return ckrs;
	}

	public void setCkrs(Integer ckrs) {
		this.ckrs = ckrs;
	}

	public Integer getTjrs() {
		return tjrs;
	}

	public void setTjrs(Integer tjrs) {
		this.tjrs = tjrs;
	}

	public Float getPjf() {
		return pjf;
	}

	public void setPjf(Float pjf) {
		this.pjf = pjf;
	}

	public Float getPjffc() {
		return pjffc;
	}

	public void setPjffc(Float pjffc) {
		this.pjffc = pjffc;
	}

	public Float getBzc() {
		return bzc;
	}

	public void setBzc(Float bzc) {
		this.bzc = bzc;
	}

	public Float getYxl() {
		return yxl;
	}

	public void setYxl(Float yxl) {
		this.yxl = yxl;
	}

	public Integer getYxrs() {
		return yxrs;
	}

	public void setYxrs(Integer yxrs) {
		this.yxrs = yxrs;
	}

	public Float getYxlc() {
		return yxlc;
	}

	public void setYxlc(Float yxlc) {
		this.yxlc = yxlc;
	}

	public Float getHgl() {
		return hgl;
	}

	public void setHgl(Float hgl) {
		this.hgl = hgl;
	}

	public Integer getHgrs() {
		return hgrs;
	}

	public void setHgrs(Integer hgrs) {
		this.hgrs = hgrs;
	}

	public Float getHglc() {
		return hglc;
	}

	public void setHglc(Float hglc) {
		this.hglc = hglc;
	}

	public Float getDfl() {
		return dfl;
	}

	public void setDfl(Float dfl) {
		this.dfl = dfl;
	}

	public Integer getDfrs() {
		return dfrs;
	}

	public void setDfrs(Integer dfrs) {
		this.dfrs = dfrs;
	}

	public Float getDflc() {
		return dflc;
	}

	public void setDflc(Float dflc) {
		this.dflc = dflc;
	}

	public Float getLdxs() {
		return ldxs;
	}

	public void setLdxs(Float ldxs) {
		this.ldxs = ldxs;
	}

	public Float getQfd() {
		return qfd;
	}

	public void setQfd(Float qfd) {
		this.qfd = qfd;
	}

	public Integer getJzsrs() {
		return jzsrs;
	}

	public void setJzsrs(Integer jzsrs) {
		this.jzsrs = jzsrs;
	}

	public Integer getQnsrs() {
		return qnsrs;
	}

	public void setQnsrs(Integer qnsrs) {
		this.qnsrs = qnsrs;
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
