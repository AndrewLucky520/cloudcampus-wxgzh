package com.talkweb.placementtask.domain;

	/**
	* 	业务表为 t_pl_dezy_subjectgroup

	*@author :武洋
	*@date 2017-04-06 15:02:43
	*@version :V1.0 
	*/

public class TPlDezySubjectgroup{

	private String placementId;
	private String schoolId;
	private String usedGrade;
	private String subjectGroupId;
	private String groupName;
	private String subjectIds;
	private int proLesson;
	private int optLesson;
	/**
	*1:开设学考；-1：不开设学考
	*/
	private int isProExist;
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
	public void subjectGroupId(String subjectGroupId){
		this.subjectGroupId=subjectGroupId;
	}
	public String getGroupName(){
		return this.groupName;
	}
	public void setGroupName(String groupName){
		this.groupName=groupName;
	}
	public String getSubjectIds(){
		return this.subjectIds;
	}
	public void setSubjectIds(String subjectIds){
		this.subjectIds=subjectIds;
	}
	public int getProLesson(){
		return this.proLesson;
	}
	public void setProLesson(int proLesson){
		this.proLesson=proLesson;
	}
	public int getOptLesson(){
		return this.optLesson;
	}
	public void setOptLesson(int optLesson){
		this.optLesson=optLesson;
	}
	public int getIsProExist(){
		return this.isProExist;
	}
	public void setIsProExist(int isProExist){
		this.isProExist=isProExist;
	}

}