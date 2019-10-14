package com.talkweb.schedule.entity;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSONObject;

public class TSchTClassInfoExternal {
	private String tclassId;
	private String tclassName;

	private List<Long> studentIdList = new ArrayList<Long>();
	private List<TSchSubjectInfo> subjectList = new ArrayList<TSchSubjectInfo>();

	public String getTclassId() {
		return tclassId;
	}

	public void setTclassId(String tclassId) {
		this.tclassId = tclassId;
	}

	public String getTclassName() {
		return tclassName;
	}

	public void setTclassName(String tclassName) {
		this.tclassName = tclassName;
	}

	public List<Long> getStudentIdList() {
		return studentIdList;
	}

	public List<TSchSubjectInfo> getSubjectList() {
		return subjectList;
	}

	public void addSubjectInfo(long subjectId, int subjectLevel) {
		TSchSubjectInfo subjectInfo = new TSchSubjectInfo(subjectId, subjectLevel);
		this.subjectList.add(subjectInfo);
	}
	
	public void setStudentIdList(List<Long> studentIdList) {
		this.studentIdList = studentIdList;
	}

	public void setSubjectList(List<TSchSubjectInfo> subjectList) {
		this.subjectList = subjectList;
	}
	
	public int getStudentSize(){
		return this.studentIdList == null ? 0 : this.studentIdList.size();
	}

	public String toString(){
		return JSONObject.toJSONString(this);
	}
	
	public class TSchSubjectInfo {
		private Long subjectId;
		private int subjectLevel;

		public TSchSubjectInfo(Long subjectId, int subjectLevel) {
			this.subjectId = subjectId;
			this.subjectLevel = subjectLevel;
		}
		
		public Long getSubjectId() {
			return subjectId;
		}

		public void setSubjectId(Long subjectId) {
			this.subjectId = subjectId;
		}

		public int getSubjectLevel() {
			return subjectLevel;
		}

		public void setSubjectLevel(int subjectLevel) {
			this.subjectLevel = subjectLevel;
		}
		
		public String toString(){
			return JSONObject.toJSONString(this);
		}
	}
}
