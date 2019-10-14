package com.talkweb.schedule.entity;

import java.io.Serializable;

	/**
	* 	业务表为 t_sch_schedule_gradeset

	*@author :武洋
	*@date 2016-11-28 16:25:52
	*@version :V1.0 
	*/

public class TSchScheduleGradeset implements Serializable{

	/**
		 * 
		 */
		private static final long serialVersionUID = -5527944254393468704L;
	private String schoolYear;
	private String termInfoId;
	private String schoolId;
	private String scheduleId;
	private String gradeId;
	private int amLessonNum;
	private int pmLessonNum;
	/**
	*0：未生成，1：已设置上课节次天数 
	 *            2：已生成教学班 
	 *            3：已生成教学任务 
	 *            4：已设置排课规则 
	 *            5：已智能排课 
	 *            
	*/
	private int state;
	/**
	*存储此字段，用于切换走班类型时数据不用清除 
	 *            1：微走班；2：中走班；3：大走班；
	*/
	private int placementType;
	private String placementId;
	private String placementTermInfo;
	private int gradeAlgMethod;
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
	public String getGradeId(){
		return this.gradeId;
	}
	public void setGradeId(String gradeId){
		this.gradeId=gradeId;
	}
	public int getAmLessonNum(){
		return this.amLessonNum;
	}
	public void setAmLessonNum(int amLessonNum){
		this.amLessonNum=amLessonNum;
	}
	public int getPmLessonNum(){
		return this.pmLessonNum;
	}
	public void setPmLessonNum(int pmLessonNum){
		this.pmLessonNum=pmLessonNum;
	}
	public int getState(){
		return this.state;
	}
	public void setState(int state){
		this.state=state;
	}
	public int getPlacementType(){
		return this.placementType;
	}
	public void setPlacementType(int placementType){
		this.placementType=placementType;
	}
	public String getPlacementId(){
		return this.placementId;
	}
	public void setPlacementId(String placementId){
		this.placementId=placementId;
	}
	public String getPlacementTermInfo(){
		return this.placementTermInfo;
	}
	public void setPlacementTermInfo(String placementTermInfo){
		this.placementTermInfo=placementTermInfo;
	}
	public int getGradeAlgMethod() {
		return gradeAlgMethod;
	}
	public void setGradeAlgMethod(int gradeAlgMethod) {
		this.gradeAlgMethod = gradeAlgMethod;
	}

}