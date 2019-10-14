package com.talkweb.timetable.arrangement.domain;

public class TCRCATemporaryAdjustGroupInfo {

	
	private String xn;
	private String xqm;
	private String SchoolId;
	
	private String TimetableId;
	private String GroupId;
	private int GroupStep;
	private int GroupMinWeek;
	private int GroupMaxWeek;
	private int NotStartOperate;
	private int NotStartCancel;
	//是否为跨班组
	private int IsCrossClass;
	
	
	public String getXn() {
		return xn;
	}
	public void setXn(String xn) {
		this.xn = xn;
	}
	public String getXqm() {
		return xqm;
	}
	public void setXqm(String xqm) {
		this.xqm = xqm;
	}
	public String getSchoolId() {
		return SchoolId;
	}
	public void setSchoolId(String schoolId) {
		SchoolId = schoolId;
	}
	public String getTimetableId() {
		return TimetableId;
	}
	public void setTimetableId(String timetableId) {
		TimetableId = timetableId;
	}
	public String getGroupId() {
		return GroupId;
	}
	public void setGroupId(String groupId) {
		GroupId = groupId;
	}
	public int getGroupStep() {
		return GroupStep;
	}
	public void setGroupStep(int groupStep) {
		GroupStep = groupStep;
	}
	public int getGroupMinWeek() {
		return GroupMinWeek;
	}
	public void setGroupMinWeek(int groupMinWeek) {
		GroupMinWeek = groupMinWeek;
	}
	public int getGroupMaxWeek() {
		return GroupMaxWeek;
	}
	public void setGroupMaxWeek(int groupMaxWeek) {
		GroupMaxWeek = groupMaxWeek;
	}
	public int getNotStartOperate() {
		return NotStartOperate;
	}
	public void setNotStartOperate(int notStartOperate) {
		NotStartOperate = notStartOperate;
	}
	public int getNotStartCancel() {
		return NotStartCancel;
	}
	public void setNotStartCancel(int notStartCancel) {
		NotStartCancel = notStartCancel;
	}
	public int getIsCrossClass() {
		return IsCrossClass;
	}
	public void setIsCrossClass(int isCrossClass) {
		IsCrossClass = isCrossClass;
	}
}
