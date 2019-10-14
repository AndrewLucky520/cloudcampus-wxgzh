package com.talkweb.placementtask.domain;

	/**
	* 	业务表为 t_pl_dezy_subject_set

	*@author :武洋
	*@date 2017-04-06 15:02:42
	*@version :V1.0 
	*/

public class TPlDezySubjectSet{

	private String placementId;
	private String schoolId;
	private String usedGrade;
	private String subjectGroupId;
	private String subjectId;
	private int optLesson; 
	private int proLesson;
	private int classLimit;
	private int stuNum;
	private int stuNumOfPro;
	private int proClassLimit;
	private int teacherNum;
	
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
	public void setSubjectGroupId(String subjectGroupId){
		this.subjectGroupId=subjectGroupId;
	}
	public String getSubjectId(){
		return this.subjectId;
	}
	public void setSubjectId(String subjectId){
		this.subjectId=subjectId;
	}
	public int getOptLesson(){
		return this.optLesson;
	}
	public void setOptLesson(int optLesson){
		this.optLesson=optLesson;
	}
	public int getProLesson(){
		return this.proLesson;
	}
	public void setProLesson(int proLesson){
		this.proLesson=proLesson;
	}
	public int getIsProExist(){
		return this.isProExist;
	}
	public void setIsProExist(int isProExist){
		this.isProExist=isProExist;
	}
	public int getClassLimit() {
		return classLimit;
	}
	public void setClassLimit(int classLimit) {
		this.classLimit = classLimit;
	}
	public int getStuNum() {
		return stuNum;
	}
	public void setStuNum(int stuNum) {
		this.stuNum = stuNum;
	}
	public int getProClassLimit() {
		return proClassLimit;
	}
	public void setProClassLimit(int proClassLimit) {
		this.proClassLimit = proClassLimit;
	}
	public int getStuNumOfPro() {
		return stuNumOfPro;
	}
	public void setStuNumOfPro(int stuNumOfPro) {
		this.stuNumOfPro = stuNumOfPro;
	}
	public int getTeacherNum() {
		return teacherNum;
	}
	public void setTeacherNum(int teacherNum) {
		this.teacherNum = teacherNum;
	}

}