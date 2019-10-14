package com.talkweb.schedule.entity;

import java.io.Serializable;

	/**
	* 	业务表为 t_sch_student_tclass_relationship

	*@author :武洋
	*@date 2016-11-28 16:25:52
	*@version :V1.0 
	*/

public class TSchStudentTclassRelationship implements Serializable{

	/**
		 * 
		 */
		private static final long serialVersionUID = 4964901543022444385L;
	private String schoolYear;
	private String termInfoId;
	private String schoolId;
	private String scheduleId;
	private String gradeId;
	private String studentId;
	/**
	*组合上课及按行政班上课时此字段固定填入 “-999” 
	 *            
	*/
	private String subjectId;
	/**
	*走班时此字段为空
	*/
	private String tclassId;
	/**
	*	10：三三固定组合
	*	20：非固定组合
	*	31：学考，32：选考
	*	41：分层：第一层，42：第二层，43：第三层
	*	50:按原行政班 
	*/
	private int subjectLevel;
	/**
	*0:未进班 1：已进班
	*/
	private int inTclassState;
	private String notInReason;
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
	public String getStudentId(){
		return this.studentId;
	}
	public void setStudentId(String studentId){
		this.studentId=studentId;
	}
	public String getSubjectId(){
		return this.subjectId;
	}
	public void setSubjectId(String subjectId){
		this.subjectId=subjectId;
	}
	public String getTclassId(){
		return this.tclassId;
	}
	public void setTclassId(String tclassId){
		this.tclassId=tclassId;
	}
	public int getSubjectLevel(){
		return this.subjectLevel;
	}
	public void setSubjectLevel(int subjectLevel){
		this.subjectLevel=subjectLevel;
	}
	public int getInTclassState(){
		return this.inTclassState;
	}
	public void setInTclassState(int inTclassState){
		this.inTclassState=inTclassState;
	}
	public String getNotInReason(){
		return this.notInReason;
	}
	public void setNotInReason(String notInReason){
		this.notInReason=notInReason;
	}

}