package com.talkweb.placementtask.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

	/**
	* 	业务表为 t_pl_dezy_subjectcomp

	*@author :武洋
	*@date 2017-04-06 15:02:42
	*@version :V1.0 
	*/

public class TPlDezySubjectcomp{

	private String placementId;
	private String schoolId;
	private String usedGrade;
	/**
	*行政班代码，可以通过行政班直接查行政班的组合构成
	*/
	private String classId;
	private String subjectCompId;
	private String compName;
	private int compNum;
	/**
	*构成该志愿组合的科目id，多个以逗号分隔
	*/
	private String compFrom;
	/**
	 * 学考科目
	 */
	private String proSubIds;
	/**
	 * 未完成的科目列表
	 */
	private List<String> unfinishOptSubTasks = new ArrayList<String>();
	private List<String> unfinishProSubTasks = new ArrayList<String>();
	
	private List<Integer> finishSeqs = new ArrayList<Integer>();
	/**
	 * 已完成科目所在的班级序列
	 */
	private Map<String,Integer> finishSubTseqMap  = new HashMap<String, Integer>(); 
	/**
	 * 已完成科目所在的班级
	 */
	private Map<String,String> finishSubTclassMap  = new HashMap<String, String>(); 
	
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
	public String getSubjectCompId(){
		return this.subjectCompId;
	}
	public void setSubjectCompId(String subjectCompId){
		this.subjectCompId=subjectCompId;
	}
	public String getCompName(){
		return this.compName;
	}
	public void setCompName(String compName){
		this.compName=compName;
	}
	public int getCompNum(){
		return this.compNum;
	}
	public void setCompNum(int compNum){
		this.compNum=compNum;
	}
	public String getCompFrom(){
		return this.compFrom;
	}
	public void setCompFrom(String compFrom){
		this.compFrom=compFrom;
	}
	public String getProSubIds() {
		return proSubIds;
	}
	public void setProSubIds(String proSubIds) {
		this.proSubIds = proSubIds;
	}
	public Map<String, Integer> getFinishSubTseqMap() {
		return finishSubTseqMap;
	}
	public void setFinishSubTseqMap(Map<String, Integer> finishSubTseqMap) {
		this.finishSubTseqMap = finishSubTseqMap;
	}
	public List<String> getUnfinishOptSubTasks() {
		return unfinishOptSubTasks;
	}
	public void setUnfinishOptSubTasks(List<String> unfinishOptSubTasks) {
		this.unfinishOptSubTasks = unfinishOptSubTasks;
	}
	public List<String> getUnfinishProSubTasks() {
		return unfinishProSubTasks;
	}
	public void setUnfinishProSubTasks(List<String> unfinishProSubTasks) {
		this.unfinishProSubTasks = unfinishProSubTasks;
	}
	public List<Integer> getFinishSeqs() {
		return finishSeqs;
	}
	public void setFinishSeqs(List<Integer> finishSeqs) {
		this.finishSeqs = finishSeqs;
	}
	public Map<String, String> getFinishSubTclassMap() {
		return finishSubTclassMap;
	}
	public void setFinishSubTclassMap(Map<String, String> finishSubTclassMap) {
		this.finishSubTclassMap = finishSubTclassMap;
	}
	public TPlDezySubjectcomp deepCopy() {
		// TODO Auto-generated method stub
		TPlDezySubjectcomp sp = new TPlDezySubjectcomp();
		sp.setClassId(classId);
		sp.setCompFrom(compFrom);
		sp.setCompName(compName);
		sp.setCompNum(compNum);
		sp.setPlacementId(placementId);
		sp.setSchoolId(schoolId);
		sp.setSubjectCompId(subjectCompId);
		sp.setUsedGrade(usedGrade);
		
		return sp;
	}

}