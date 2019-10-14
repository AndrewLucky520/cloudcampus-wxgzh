package com.talkweb.scoreManage.business;

import java.util.List;

/****
 * 班级成绩分析表输入参数模型
 * 
 * @author gyb
 *
 */
public class ClassScoreInParam {
	private String xnxq;// 学年学期,
	private List<String> synj;// 使用年级，多个年级用逗号分隔
	private String kslc;// 考试代码
	private List<String> fzdm;// 班级分组代码
	private List<String> bh;// 班级，多个班级用逗号分隔
	private List<String> mkdm;// 科目，多个科目已用逗号分隔
	private String xxdm;// 学校代码
	private String subSql;// 拼接SQL
	private String schoolYear;// 学年

	public String getSchoolYear() {
		return schoolYear;
	}

	public void setSchoolYear(String schoolYear) {
		this.schoolYear = schoolYear;
	}

	public String getSubSql() {
		return subSql;
	}

	public void setSubSql(String subSql) {
		this.subSql = subSql;
	}

	public int getTermInfoRange() {
		return termInfoRange;
	}

	public void setTermInfoRange(int termInfoRange) {
		this.termInfoRange = termInfoRange;
	}

	private int termInfoRange;// 学年学期范围 “0”本学期，“1”本学年，“2”历年

	public String getXxdm() {
		return xxdm;
	}

	public void setXxdm(String xxdm) {
		this.xxdm = xxdm;
	}

	public void setFzdm(List<String> fzdm) {
		this.fzdm = fzdm;
	}

	public String getXnxq() {
		return xnxq;
	}

	public void setXnxq(String xnxq) {
		this.xnxq = xnxq;
	}

	public String getKslc() {
		return kslc;
	}

	public void setKslc(String kslc) {
		this.kslc = kslc;
	}

	public List<String> getFzdm() {
		return fzdm;
	}

	public List<String> getBh() {
		return bh;
	}

	public void setBh(List<String> bh) {
		this.bh = bh;
	}

	public List<String> getMkdm() {
		return mkdm;
	}

	public void setMkdm(List<String> mkdm) {
		this.mkdm = mkdm;
	}

	@Override
	public String toString() {
		return "ClassScoreInParam [xnxq=" + xnxq + ", synj=" + synj + ", kslc=" + kslc + ", fzdm=" + fzdm + ", bh=" + bh
				+ ", mkdm=" + mkdm + "]";
	}

	public List<String> getSynj() {
		return synj;
	}

	public void setSynj(List<String> synj) {
		this.synj = synj;
	}
}
