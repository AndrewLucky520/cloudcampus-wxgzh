package com.talkweb.placementtask.domain;

	/**
	* 	业务表为 t_pl_dezy_tclass_subcomp

	*@author :武洋
	*@date 2017-04-06 15:02:43
	*@version :V1.0 
	*/

public class TPlDezyTclassSubcomp{

	private String placementId;
	private String schoolId;
	private String usedGrade;
	private String subjectCompId;
	private String tclassId;
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
	public String getSubjectCompId(){
		return this.subjectCompId;
	}
	public void setSubjectCompId(String subjectCompId){
		this.subjectCompId=subjectCompId;
	}
	public String getTclassId(){
		return this.tclassId;
	}
	public void setTclassId(String tclassId){
		this.tclassId=tclassId;
	}
	public TPlDezyTclassSubcomp deepCopy() {
		// TODO Auto-generated method stub
		TPlDezyTclassSubcomp tsp = new TPlDezyTclassSubcomp();
		tsp.setPlacementId(placementId);
		tsp.setSchoolId(schoolId);
		tsp.setSubjectCompId(subjectCompId);
		tsp.setTclassId(tclassId);
		tsp.setUsedGrade(usedGrade);
		return tsp;
	}

}