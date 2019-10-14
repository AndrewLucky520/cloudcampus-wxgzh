package com.talkweb.scoreManage.po.gm;

import com.alibaba.fastjson.JSONObject;

// 班级总分统计结果
public class ScoreClassStatistics {
	private String kslc; // 考试轮次代码
	private String xxdm; // 学校代码
	private String bh; // 班号
	private Integer ckrs; // 参考人数
	private Integer tjrs; // 统计人数
	private Float pjf; // 平均分
	private Float pjffc; // 平均分分差
	private Float bzc; // 标准差
	private Float zgf; // 最高分
	private Float zdf; // 最低分
	private Float bzf; // 标准分
	private Float yxl; // 优秀率
	private Integer yxrs; // 优秀人数
	private Float yxlc; // 优秀率差
	private Integer yxlpm; // 优秀率排名
	private Float hgl; // 合格率
	private Integer hgrs; // 合格人数
	private Float hglc; // 合格率差
	private Integer hglpm; // 合格率排名
	private Float dfl; // 低分率
	private Integer dfrs; // 低分人数
	private Float dflc; // 低分率差
	private Integer dflpm; // 低分率排名
	private Integer jzsrs; // 尖子生人数
	private Integer qnsrs; // 潜能生人数
	private Integer pm; // 平均总分排名
	private Float mf; // 满分
	private Float ldxs; // 难度系数
	private Float qfd; // 区分度
	private Float jxxg; // 效果分
	private Integer ldpm; // 难度排名
	private Integer bzcpm; // 标准差排名
	private Integer qfdpm; // 区分度排名
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

	public String getBh() {
		return bh;
	}

	public void setBh(String bh) {
		this.bh = bh;
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

	public Float getZgf() {
		return zgf;
	}

	public void setZgf(Float zgf) {
		this.zgf = zgf;
	}

	public Float getZdf() {
		return zdf;
	}

	public void setZdf(Float zdf) {
		this.zdf = zdf;
	}

	public Float getBzf() {
		return bzf;
	}

	public void setBzf(Float bzf) {
		this.bzf = bzf;
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

	public Integer getYxlpm() {
		return yxlpm;
	}

	public void setYxlpm(Integer yxlpm) {
		this.yxlpm = yxlpm;
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

	public Integer getHglpm() {
		return hglpm;
	}

	public void setHglpm(Integer hglpm) {
		this.hglpm = hglpm;
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

	public Integer getDflpm() {
		return dflpm;
	}

	public void setDflpm(Integer dflpm) {
		this.dflpm = dflpm;
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

	public Integer getPm() {
		return pm;
	}

	public void setPm(Integer pm) {
		this.pm = pm;
	}

	public Float getMf() {
		return mf;
	}

	public void setMf(Float mf) {
		this.mf = mf;
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

	public Float getJxxg() {
		return jxxg;
	}

	public void setJxxg(Float jxxg) {
		this.jxxg = jxxg;
	}

	public Integer getLdpm() {
		return ldpm;
	}

	public void setLdpm(Integer ldpm) {
		this.ldpm = ldpm;
	}

	public Integer getBzcpm() {
		return bzcpm;
	}

	public void setBzcpm(Integer bzcpm) {
		this.bzcpm = bzcpm;
	}

	public Integer getQfdpm() {
		return qfdpm;
	}

	public void setQfdpm(Integer qfdpm) {
		this.qfdpm = qfdpm;
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
