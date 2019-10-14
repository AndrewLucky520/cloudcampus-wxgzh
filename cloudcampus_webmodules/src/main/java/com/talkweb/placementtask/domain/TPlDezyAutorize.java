package com.talkweb.placementtask.domain;

	/**
	* 	业务表为 t_pl_dezy_settings

	*@author :武洋
	*@date 2017-04-06 15:02:42
	*@version :V1.0 
	*/

public class TPlDezyAutorize{

	private String schoolId;
	private int combinedSubNum;
	private int authorized;

	public String getSchoolId() {
		return schoolId;
	}
	public void setSchoolId(String schoolId) {
		this.schoolId = schoolId;
	}
	public int getCombinedSubNum() {
		return combinedSubNum;
	}
	public void setCombinedSubNum(int combinedSubNum) {
		this.combinedSubNum = combinedSubNum;
	}
	public int getAuthorized() {
		return authorized;
	}
	public void setAuthorized(int authorized) {
		this.authorized = authorized;
	}
}