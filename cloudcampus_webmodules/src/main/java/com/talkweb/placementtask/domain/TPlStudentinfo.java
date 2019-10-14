package com.talkweb.placementtask.domain;

import java.io.Serializable;

	/**
	* 	业务表为 t_pl_studentinfo

	*@author :武洋
	*@date 2018-01-25 14:50:50
	*@version :V1.0 
	*/

public class TPlStudentinfo implements Serializable{

	/**
		 * 
		 */
		private static final long serialVersionUID = -5596724127031173540L;
	private String placementId;
	private String schoolId;
	private String teachingClassId;
	private String accountId;
	private String classId;
	private String openClassInfoId;
	private String openClassTaskId;
	private int type;
	private String termInfo;
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
	public String getTeachingClassId(){
		return this.teachingClassId;
	}
	public void setTeachingClassId(String teachingClassId){
		this.teachingClassId=teachingClassId;
	}
	public String getAccountId(){
		return this.accountId;
	}
	public void setAccountId(String accountId){
		this.accountId=accountId;
	}
	public String getClassId(){
		return this.classId;
	}
	public void setClassId(String classId){
		this.classId=classId;
	}
	public String getOpenClassInfoId(){
		return this.openClassInfoId;
	}
	public void setOpenClassInfoId(String openClassInfoId){
		this.openClassInfoId=openClassInfoId;
	}
	public String getOpenClassTaskId(){
		return this.openClassTaskId;
	}
	public void setOpenClassTaskId(String openClassTaskId){
		this.openClassTaskId=openClassTaskId;
	}
	public int getType(){
		return this.type;
	}
	public void setType(int type){
		this.type=type;
	}
	public String getTermInfo(){
		return this.termInfo;
	}
	public void setTermInfo(String termInfo){
		this.termInfo=termInfo;
	}

}