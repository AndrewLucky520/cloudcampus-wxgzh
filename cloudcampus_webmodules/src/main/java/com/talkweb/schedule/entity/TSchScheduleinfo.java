package com.talkweb.schedule.entity;

	/**
	* 	业务表为 t_sch_scheduleinfo

	*@author :武洋
	*@date 2016-12-16 16:07:56
	*@version :V1.0 
	*/

public class TSchScheduleinfo{

	private String schoolYear;
	private String termInfoId;
	private String schoolId;
	private String scheduleId;
	private String scheduleName;
	private int maxDaysForWeek;
	private String halfAtLastDay;
	private String sameForAll;
	/**
	*0：未发布，1：已发布
	*/
	private String published;
	private String createTime;
	/**
	*0：未排，1：已排
	*/
	private String isArrange;
	private String arrangeResult;
	private String dayOfStart;
	public String getSchoolYear(){
		return this.schoolYear;
	}
	public void setSchoolYear(String schoolYear){
		this.schoolYear=schoolYear;
	}
	public String getTermInfoId(){
		return this.termInfoId;
	}
	public void setTermInfoId(String termInfoId){
		this.termInfoId=termInfoId;
	}
	public String getSchoolId(){
		return this.schoolId;
	}
	public void setSchoolId(String schoolId){
		this.schoolId=schoolId;
	}
	public String getScheduleId(){
		return this.scheduleId;
	}
	public void setScheduleId(String scheduleId){
		this.scheduleId=scheduleId;
	}
	public String getScheduleName(){
		return this.scheduleName;
	}
	public void setScheduleName(String scheduleName){
		this.scheduleName=scheduleName;
	}
	public int getMaxDaysForWeek(){
		return this.maxDaysForWeek;
	}
	public void setMaxDaysForWeek(int maxDaysForWeek){
		this.maxDaysForWeek=maxDaysForWeek;
	}
	public String getHalfAtLastDay(){
		return this.halfAtLastDay;
	}
	public void setHalfAtLastDay(String halfAtLastDay){
		this.halfAtLastDay=halfAtLastDay;
	}
	public String getSameForAll(){
		return this.sameForAll;
	}
	public void setSameForAll(String sameForAll){
		this.sameForAll=sameForAll;
	}
	public String getPublished(){
		return this.published;
	}
	public void setPublished(String published){
		this.published=published;
	}
	public String getCreateTime(){
		return this.createTime;
	}
	public void setCreateTime(String createTime){
		this.createTime=createTime;
	}
	public String getIsArrange(){
		return this.isArrange;
	}
	public void setIsArrange(String isArrange){
		this.isArrange=isArrange;
	}
	public String getArrangeResult(){
		return this.arrangeResult;
	}
	public void setArrangeResult(String arrangeResult){
		this.arrangeResult=arrangeResult;
	}
	public String getDayOfStart(){
		return this.dayOfStart;
	}
	public void setDayOfStart(String dayOfStart){
		this.dayOfStart=dayOfStart;
	}

}