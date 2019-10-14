package com.talkweb.placementtask.domain;

import java.util.ArrayList;
import java.util.List;

	/**
	* 	业务表为 t_pl_dezy_subjectcompGroup

	*@author :武洋
	*@date 2017-04-06 15:02:42
	*@version :V1.0 
	*/

public class TPlDezySubjectcompGroup{

	/**
	*行政班代码，可以通过行政班直接查行政班的组合构成
	*/
	private String classId;
	private List<String> subjectCompId = new ArrayList<String>();
	private int groupNum = 0;
	 
	private List<TPlDezySubjectcomp> childComps = new ArrayList<TPlDezySubjectcomp>();

	public String getClassId() {
		return classId;
	}

	public void setClassId(String classId) {
		this.classId = classId;
	}

	public List<String> getSubjectCompId() {
		return subjectCompId;
	}

	public void setSubjectCompId(List<String> subjectCompId) {
		this.subjectCompId = subjectCompId;
	}

	public int getGroupNum() {
		return groupNum;
	}

	public void setGroupNum(int groupNum) {
		this.groupNum = groupNum;
	}

	public List<TPlDezySubjectcomp> getChildComps() {
		return childComps;
	}

	public void setChildComps(List<TPlDezySubjectcomp> childComps) {
		this.childComps = childComps;
	}
	
	public void addChildComp(TPlDezySubjectcomp comp){
		this.groupNum+= comp.getCompNum();
		this.childComps.add(comp);
		this.subjectCompId.add(comp.getSubjectCompId());
	}
}