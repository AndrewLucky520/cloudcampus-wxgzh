package com.talkweb.placementtask.vo;

import com.talkweb.common.tools.ExcelVOAttribute;

/**
 * 学选交叉信息导入实体
 * @author hushowly@foxmail.com
 */
public class AcademicElectiveCross {
	
	@ExcelVOAttribute(name= "序列", column="A")
	private String idx;
	
	@ExcelVOAttribute(name= "学选", column="B")
	private String isOpt;
	
	@ExcelVOAttribute(name= "原本应上课序列", column="C")
	private String seq;
	
	@ExcelVOAttribute(name= "交叉科目", column="D")
	private String subIds;
	
	@ExcelVOAttribute(name= "重建", column="E")
	private String isSeqRebuild;
	
	@ExcelVOAttribute(name= "重建的序列", column="F")
	private String reSeq;

	public String getIdx() {
		return idx;
	}

	public void setIdx(String idx) {
		this.idx = idx;
	}

	public String getIsOpt() {
		return isOpt;
	}

	public void setIsOpt(String isOpt) {
		this.isOpt = isOpt;
	}

	public String getSeq() {
		return seq;
	}

	public void setSeq(String seq) {
		this.seq = seq;
	}

	public String getSubIds() {
		return subIds;
	}

	public void setSubIds(String subIds) {
		this.subIds = subIds;
	}

	public String getIsSeqRebuild() {
		return isSeqRebuild;
	}

	public void setIsSeqRebuild(String isSeqRebuild) {
		this.isSeqRebuild = isSeqRebuild;
	}

	public String getReSeq() {
		return reSeq;
	}

	public void setReSeq(String reSeq) {
		this.reSeq = reSeq;
	}
}
