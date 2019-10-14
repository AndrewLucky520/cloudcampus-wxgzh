package com.talkweb.timetable.arrangement.domain;

public class TCRCATemporaryAdjustGroupParam {

	
	private String xn;
	private String xqm;
	private String SchoolId;
	
	private String TimetableId;
	private String GroupId;
	private int Week;
	private int GroupMoveTimes;
	private int GroupStep;
	
	
	
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
	public int getWeek() {
		return Week;
	}
	public void setWeek(int week) {
		Week = week;
	}
	public int getGroupMoveTimes() {
		return GroupMoveTimes;
	}
	public void setGroupMoveTimes(int groupMoveTimes) {
		GroupMoveTimes = groupMoveTimes;
	}
	public int getGroupStep() {
		return GroupStep;
	}
	public void setGroupStep(int groupStep) {
		GroupStep = groupStep;
	}
	 
}
