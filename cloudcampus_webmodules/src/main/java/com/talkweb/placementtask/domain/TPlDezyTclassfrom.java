package com.talkweb.placementtask.domain;

	/**
	* 	业务表为 t_pl_dezy_tclassfrom

	*@author :武洋
	*@date 2017-04-06 15:02:43
	*@version :V1.0 
	*/

public class TPlDezyTclassfrom{

	private String placementId;
	private String schoolId;
	private String usedGrade;
	private String subjectGroupId;
	/**
	*班级类型为6的时候取代码；为7的时候写死-999
	*/
	private String classGroupId;
	private String tclassId;
	private String classId;
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
	public String getSubjectGroupId(){
		return this.subjectGroupId;
	}
	public void setSubjectGroupId(String subjectGroupId){
		this.subjectGroupId=subjectGroupId;
	}
	public String getClassGroupId(){
		return this.classGroupId;
	}
	public void setClassGroupId(String classGroupId){
		this.classGroupId=classGroupId;
	}
	public String getTclassId(){
		return this.tclassId;
	}
	public void setTclassId(String tclassId){
		this.tclassId=tclassId;
	}
	public String getClassId(){
		return this.classId;
	}
	public void setClassId(String classId){
		this.classId=classId;
	}

}