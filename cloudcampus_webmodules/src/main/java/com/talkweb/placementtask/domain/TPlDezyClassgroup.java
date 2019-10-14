package com.talkweb.placementtask.domain;

import java.io.Serializable;

	/**
	* 	业务表为 t_pl_dezy_classgroup

	*@author :武洋
	*@date 2017-04-06 15:02:42
	*@version :V1.0 
	*/

public class TPlDezyClassgroup implements Serializable{

	/**
		 * 
		 */
		private static final long serialVersionUID = 750916838004562142L;
	private String placementId;
	private String schoolId;
	private String usedGrade;
	private String subjectGroupId;
	private String classtGroupId;
	private String classIds;
	private String classGroupName;
	
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
	public String getClasstGroupId(){
		return this.classtGroupId;
	}
	public void setClasstGroupId(String classtGroupId){
		this.classtGroupId=classtGroupId;
	}
	public String getClassIds(){
		return this.classIds;
	}
	public void setClassIds(String classIds){
		this.classIds=classIds;
	}
	public String getClassGroupName() {
		return classGroupName;
	}
	public void setClassGroupName(String classGroupName) {
		this.classGroupName = classGroupName;
	}
	public TPlDezyClassgroup deepCopy() {
		// TODO Auto-generated method stub
		TPlDezyClassgroup cg = new TPlDezyClassgroup();
		cg.setClassGroupName(classGroupName);
		cg.setClassIds(classIds);
		cg.setClasstGroupId(classtGroupId);
		cg.setPlacementId(placementId);
		cg.setSchoolId(schoolId);
		cg.setSubjectGroupId(subjectGroupId);
		cg.setUsedGrade(usedGrade);
		
		return cg;
	}

}