package com.talkweb.placementtask.domain;

/**
 * 走班科目学时设置
 * 对应t_pl_medium_subject_set实体类
 */
public class TPlMediumSubjectSet {
	private String placementId;
	private String schoolId;
	private String termInfo;
	private String subjectId;
	private Integer optLesson;
	private Integer proLesson;
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
	public String getTermInfo() {
		return termInfo;
	}
	public void setTermInfo(String termInfo) {
		this.termInfo = termInfo;
	}
	public String getSubjectId() {
		return subjectId;
	}
	public void setSubjectId(String subjectId) {
		this.subjectId = subjectId;
	}
	public Integer getOptLesson() {
		return optLesson;
	}
	public void setOptLesson(Integer optLesson) {
		this.optLesson = optLesson;
	}
	public Integer getProLesson() {
		return proLesson;
	}
	public void setProLesson(Integer proLesson) {
		this.proLesson = proLesson;
	}
	

}
