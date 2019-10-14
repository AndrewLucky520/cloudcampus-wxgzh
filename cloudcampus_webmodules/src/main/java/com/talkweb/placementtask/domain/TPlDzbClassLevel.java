package com.talkweb.placementtask.domain;

	/**
	* 	业务表为 t_pl_dezy_tclassfrom

	*@author :武洋
	*@date 2017-04-06 15:02:43
	*@version :V1.0 
	*/

public class TPlDzbClassLevel{

	private String placementId;
	private String schoolId;
	private String classId;
	private String subLevelName;
	private int subLevel;
	private int classType;
	
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
	
	public String getClassId(){
		return this.classId;
	}
	public void setClassId(String classId){
		this.classId=classId;
	}
	public String getSubLevelName() {
		return subLevelName;
	}
	public void setSubLevelName(String subLevelName) {
		this.subLevelName = subLevelName;
	}
	public int getSubLevel() {
		return subLevel;
	}
	public void setSubLevel(int subLevel) {
		this.subLevel = subLevel;
	}
	public int getClassType() {
		return classType;
	}
	public void setClassType(int classType) {
		this.classType = classType;
	}

}