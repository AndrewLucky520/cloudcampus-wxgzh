package com.talkweb.schedule.entity;

import java.io.Serializable;

	/**
	* 	业务表为 t_sch_tclass

	*@author :武洋
	*@date 2016-11-28 16:25:52
	*@version :V1.0 
	*/

public class TSchTclass implements Serializable{

	/**
		 * 
		 */
		private static final long serialVersionUID = 981960157256932370L;
	private String schoolYear;	
	private String termInfoId;
	private String schoolId;
	private String scheduleId;
	private String gradeId;
	private int classSeq;
	/**
	*沿用分班生成的班级代码 
	 *            
	*/
	private String tclassId;
	/**
	*存储此字段，用于切换走班类型时数据不用清除 
	 *            1：微走班；2：中走班；3：大走班；
	*/
	private int placementType;
	/**
	*1：三科组合；2：方案2的无固定组合；3：按志愿（单科）；4：按成绩分层（单科）；5：按政行班分班 
	 *             6:定二走一 行政班 7：定二走一 走班教学班
	 *            备注： 
	 *            中走班单科开班是按志愿分班的
	*/
	private int type;
	/**
	*关联分班结果的开班任务表
	*/
	private String openClassTaskId;
	private int classSize;
	/**
	*按开班类型分类： 
	 *            开班类型值为1为填0 
	 *            开班类型值为2时填0
	 *            开班类型值为3时，1：学考；2：选考 
	 *            开班类型值为4时，成绩层次代码（1/2/3层）
	 *            开班类型值为5时填0
	*/
	private int layCode;
	private String layName;
	
	private String subjectGroupId;
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
	public String getTclassId(){
		return this.tclassId;
	}
	public void setTclassId(String tclassId){
		this.tclassId=tclassId;
	}
	public int getPlacementType(){
		return this.placementType;
	}
	public void setPlacementType(int placementType){
		this.placementType=placementType;
	}
	public int getType(){
		return this.type;
	}
	public void setType(int type){
		this.type=type;
	}
	public String getOpenClassTaskId(){
		return this.openClassTaskId;
	}
	public void setOpenClassTaskId(String openClassTaskId){
		this.openClassTaskId=openClassTaskId;
	}
	public int getClassSize(){
		return this.classSize;
	}
	public void setClassSize(int classSize){
		this.classSize=classSize;
	}
	public int getLayCode(){
		return this.layCode;
	}
	public void setLayCode(int layCode){
		this.layCode=layCode;
	}
	public String getLayName(){
		return this.layName;
	}
	public void setLayName(String layName){
		this.layName=layName;
	}
	public String getSubjectGroupId() {
		return subjectGroupId;
	}
	public void setSubjectGroupId(String subjectGroupId) {
		this.subjectGroupId = subjectGroupId;
	}
	public int getClassSeq() {
		return classSeq;
	}
	public void setClassSeq(int classSeq) {
		this.classSeq = classSeq;
	}

}