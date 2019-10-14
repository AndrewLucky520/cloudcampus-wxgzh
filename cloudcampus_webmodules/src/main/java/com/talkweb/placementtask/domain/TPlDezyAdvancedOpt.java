package com.talkweb.placementtask.domain;

	/**
	* 	业务表为 t_pl_dezy_advacnedOpt

	*@author :武洋
	*@date 2017-04-06 15:02:42
	*@version :V1.0 
	*/

public class TPlDezyAdvancedOpt{

	private String placementId;
	private String schoolId;
	private String usedGrade;
	/**
	 * 行政班科目组
	 */
	private String subjectIds;
	private String classId;
	private String className;
	
	private String expClassId;
	private String expClassName;
	
	
	
	public String getPlacementId(){
		return this.placementId;
	}
	public void setPlacementId(String placementId){
		this.placementId=placementId;
	}
	public String getSchoolId(){
		return this.schoolId;
	}
	public void setSchoolId(String schoolId){
		this.schoolId=schoolId;
	}
	public String getUsedGrade(){
		return this.usedGrade;
	}
	public void setUsedGrade(String usedGrade){
		this.usedGrade=usedGrade;
	}
	
	public String getClassId(){
		return this.classId;
	}
	public void setClassId(String classId){
		this.classId=classId;
	}
	public String getSubjectIds() {
		return subjectIds;
	}
	public void setSubjectIds(String subjectIds) {
		this.subjectIds = subjectIds;
	}
	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}
	public String getExpClassId() {
		return expClassId;
	}
	public void setExpClassId(String expClassId) {
		this.expClassId = expClassId;
	}
	public String getExpClassName() {
		return expClassName;
	}
	public void setExpClassName(String expClassName) {
		this.expClassName = expClassName;
	}
	
	
}