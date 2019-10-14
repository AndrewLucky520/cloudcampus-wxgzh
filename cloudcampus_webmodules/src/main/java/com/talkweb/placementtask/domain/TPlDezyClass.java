package com.talkweb.placementtask.domain;

	/**
	* 	业务表为 t_pl_dezy_class

	*@author :武洋
	*@date 2017-04-06 15:02:42
	*@version :V1.0 
	*/

public class TPlDezyClass{

	private String placementId;
	private String schoolId;
	private String usedGrade;
	private String subjectGroupId;
	/**
	 * 班级类型为6时，写死-999，班级类型为7时，写入开班的科目
	 */
	private String subjectId;
	/**
	*班级类型为6的时候取代码；为7的时候写死-999
	*/
	private String classGroupId;
	private String tclassId;
	/**
	*6：行政班；7：走班教学班
	*/
	private int tclassType;
	/**
	*0：行政班；1：选考 ；2：学考
	*/
	private int tclassLevel;
	private int tclassNum;
	private String groundId;
	private String groundName;
	private String tclassName;
	private String oriClassName;
	/**
	*0:无序列；1：第一序列；2：第二序列；3：第三序列
	*/
	private int classSeq;
	
	/**
	 * 
	 * 0:平行班,1：实验班,-1:其它
	 */
	private int isExpClass = 0;
	
	
	/**
	 * 
	 * subLevel：{1：A层, 2:B层, ...}
	 * 
	 */
	private int subLevel = 0;
	
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
	public int getTclassType(){
		return this.tclassType;
	}
	public void setTclassType(int tclassType){
		this.tclassType=tclassType;
	}
	public int getTclassLevel(){
		return this.tclassLevel;
	}
	public void setTclassLevel(int tclassLevel){
		this.tclassLevel=tclassLevel;
	}
	public int getTclassNum(){
		return this.tclassNum;
	}
	public void setTclassNum(int tclassNum){
		this.tclassNum=tclassNum;
	}
	public String getGroundId(){
		return this.groundId;
	}
	public void setGroundId(String groundId){
		this.groundId=groundId;
	}
	public String getGroundName(){
		return this.groundName;
	}
	public void setGroundName(String groundName){
		this.groundName=groundName;
	}
	public int getClassSeq(){
		return this.classSeq;
	}
	public void setClassSeq(int classSeq){
		this.classSeq=classSeq;
	}
	public String getSubjectId() {
		return subjectId;
	}
	public void setSubjectId(String subjectId) {
		this.subjectId = subjectId;
	}
	public String getTclassName() {
		return tclassName;
	}
	public void setTclassName(String tclassName) {
		this.tclassName = tclassName;
	}
	public TPlDezyClass deepCopy() {
		// TODO Auto-generated method stub
		TPlDezyClass tcl = new TPlDezyClass();
		tcl.setClassGroupId(classGroupId);
		tcl.setClassSeq(classSeq);
		tcl.setGroundId(groundId);
		tcl.setGroundName(groundName);
		tcl.setPlacementId(placementId);
		tcl.setSchoolId(schoolId);
		tcl.setSubjectGroupId(subjectGroupId);
		tcl.setSubjectId(subjectId);
		tcl.setTclassId(tclassId);
		tcl.setTclassLevel(tclassLevel);
		tcl.setTclassName(tclassName);
		tcl.setTclassNum(tclassNum);
		tcl.setTclassType(tclassType);
		tcl.setUsedGrade(usedGrade);
		tcl.setIsExpClass(isExpClass);
		return tcl;
	}
	public int getIsExpClass() {
		return isExpClass;
	}
	public void setIsExpClass(int isExpClass) {
		this.isExpClass = isExpClass;
	}
	public String getOriClassName() {
		return oriClassName;
	}
	public void setOriClassName(String oriClassName) {
		this.oriClassName = oriClassName;
	}
	public int getSubLevel() {
		return subLevel;
	}
	public void setSubLevel(int subLevel) {
		this.subLevel = subLevel;
	}

}