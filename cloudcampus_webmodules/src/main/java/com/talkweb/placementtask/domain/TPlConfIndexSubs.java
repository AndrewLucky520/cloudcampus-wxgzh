package com.talkweb.placementtask.domain;
/**
 * 保存学选交叉时 各科目在交叉时的上课序列
 * @author Administrator
 *
 */
public class TPlConfIndexSubs {

	private String placementId;
	private String schoolId;
	private String usedGrade;
	private int idx;
	/**
	 * 0:未重建 1:已重建
	 */
	private int isSeqRebuild;
	private int reSeq ;
	private int seq ;
	/**
	 * 0:未重建 1:已重建  
	 */
	private int isIdxRebuild;
	/**
	 * 实际上课的课时索引 
	 */
	private int reIdx;
	public String getPlacementId() {
		return placementId;
	}
	public void setPlacementId(String placementId) {
		this.placementId = placementId;
	}
	public String getSchoolId() {
		return schoolId;
	}
	public void setSchoolId(String schoolId) {
		this.schoolId = schoolId;
	}
	public String getUsedGrade() {
		return usedGrade;
	}
	public void setUsedGrade(String usedGrade) {
		this.usedGrade = usedGrade;
	}
	 
	public int getIsOpt() {
		return isOpt;
	}
	public void setIsOpt(int isOpt) {
		this.isOpt = isOpt;
	}
	public String getSubIds() {
		return subIds;
	}
	public void setSubIds(String subIds) {
		this.subIds = subIds;
	}
	private int isOpt;
	private String subIds;
	public int getIdx() {
		return idx;
	}
	public void setIdx(int idx) {
		this.idx = idx;
	}
	public int getIsSeqRebuild() {
		return isSeqRebuild;
	}
	public void setIsSeqRebuild(int isSeqRebuild) {
		this.isSeqRebuild = isSeqRebuild;
	}
	 
	public int getIsIdxRebuild() {
		return isIdxRebuild;
	}
	public void setIsIdxRebuild(int isIdxRebuild) {
		this.isIdxRebuild = isIdxRebuild;
	}
	public int getReIdx() {
		return reIdx;
	}
	public void setReIdx(int reIdx) {
		this.reIdx = reIdx;
	}
	public int getReSeq() {
		return reSeq;
	}
	public void setReSeq(int reSeq) {
		this.reSeq = reSeq;
	}
	public int getSeq() {
		return seq;
	}
	public void setSeq(int seq) {
		this.seq = seq;
	}
	
}
