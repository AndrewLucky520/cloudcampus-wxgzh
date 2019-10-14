package com.talkweb.placementtask.domain;

	/**
	* 	业务表为 t_pl_dezy_settings

	*@author :武洋
	*@date 2017-04-06 15:02:42
	*@version :V1.0 
	*/

public class TPlDezySettings{

	private String placementId;
	private String schoolId;
	private String usedGrade;
	/**
	*1:重新分班；-1：不重新分班
	*/
	private int rePlacement;
	private int classNum;
	private int classRoundNum;
	private int minTeacherNum;
	/**
	*1:存在；-1不存在，6选3；
	*/
	private int isTechExist;
	/**
	 * 0：行政班，1：走班
	 */
	private int formOfTech;
	private int combinedSubNum;
	/**
	*1:是；-1否
	*/
	private int isLearnExist;
	private int gradeSumLesson;
	private int fixedSumLesson;
	/**
	*1:不跨组方案；2：跨组方案
	*/
	private int placeAlgMethod;
	private String wfId;
	
	/**
	 * 大走班设置
	 */
	private int avgClassNum;
	private int maxClassNum;
	
	
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
	public int getRePlacement(){
		return this.rePlacement;
	}
	public void setRePlacement(int rePlacement){
		this.rePlacement=rePlacement;
	}
	public int getClassNum(){
		return this.classNum;
	}
	public void setClassNum(int classNum){
		this.classNum=classNum;
	}
	public int getClassRoundNum(){
		return this.classRoundNum;
	}
	public void setClassRoundNum(int classRoundNum){
		this.classRoundNum=classRoundNum;
	}
	public int getMinTeacherNum(){
		return this.minTeacherNum;
	}
	public void setMinTeacherNum(int minTeacherNum){
		this.minTeacherNum=minTeacherNum;
	}
	public int getIsTechExist(){
		return this.isTechExist;
	}
	public void setIsTechExist(int isTechExist){
		this.isTechExist=isTechExist;
	}
	public int getIsLearnExist(){
		return this.isLearnExist;
	}
	public void setIsLearnExist(int isLearnExist){
		this.isLearnExist=isLearnExist;
	}
	public int getGradeSumLesson(){
		return this.gradeSumLesson;
	}
	public void setGradeSumLesson(int gradeSumLesson){
		this.gradeSumLesson=gradeSumLesson;
	}
	public int getFixedSumLesson(){
		return this.fixedSumLesson;
	}
	public void setFixedSumLesson(int fixedSumLesson){
		this.fixedSumLesson=fixedSumLesson;
	}
	public int getPlaceAlgMethod(){
		return this.placeAlgMethod;
	}
	public void setPlaceAlgMethod(int placeAlgMethod){
		this.placeAlgMethod=placeAlgMethod;
	}
	public String getWfId() {
		return wfId;
	}
	public void setWfId(String wfId) {
		this.wfId = wfId;
	}
	public int getFormOfTech() {
		return formOfTech;
	}
	public void setFormOfTech(int formOfTech) {
		this.formOfTech = formOfTech;
	}
	public int getCombinedSubNum() {
		return combinedSubNum;
	}
	public void setCombinedSubNum(int combinedSubNum) {
		this.combinedSubNum = combinedSubNum;
	}
	public int getAvgClassNum() {
		return avgClassNum;
	}
	public void setAvgClassNum(int avgClassNum) {
		this.avgClassNum = avgClassNum;
	}
	public int getMaxClassNum() {
		return maxClassNum;
	}
	public void setMaxClassNum(int maxClassNum) {
		this.maxClassNum = maxClassNum;
	}

}