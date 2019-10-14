package com.talkweb.schedule.entity;

import java.io.Serializable;

	/**
	* 	业务表为 t_sch_task

	*@author :武洋
	*@date 2016-11-28 16:25:52
	*@version :V1.0 
	*/

public class TSchTask  implements Serializable{

	/**
		 * 
		 */
		private static final long serialVersionUID = -4169250579001418353L;
	private String schoolId;
	private String scheduleId;
	private String gradeId;
	private String tclassId;
	private String subjectId;
	private double weekNum;
	private int nearNum;
	private String groundId;
	private int isModify;
	private String mcGroupId;
	
	public String getMcGroupId() {
		return mcGroupId;
	}
	public void setMcGroupId(String mcGroupId) {
		this.mcGroupId = mcGroupId;
	}
	public int getIsModify() {
		return isModify;
	}
	public void setIsModify(int isModify) {
		this.isModify = isModify;
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
	public String getTclassId(){
		return this.tclassId;
	}
	public void setTclassId(String tclassId){
		this.tclassId=tclassId;
	}
	public String getSubjectId(){
		return this.subjectId;
	}
	public void setSubjectId(String subjectId){
		this.subjectId=subjectId;
	}
	public double getWeekNum(){
		return this.weekNum;
	}
	public void setWeekNum(double weekNum){
		this.weekNum=weekNum;
	}
	public int getNearNum(){
		return this.nearNum;
	}
	public void setNearNum(int nearNum){
		this.nearNum=nearNum;
	}
	public String getGroundId(){
		return this.groundId;
	}
	public void setGroundId(String groundId){
		this.groundId=groundId;
	}
	public String getGradeId() {
		return gradeId;
	}
	public void setGradeId(String gradeId) {
		this.gradeId = gradeId;
	}

}