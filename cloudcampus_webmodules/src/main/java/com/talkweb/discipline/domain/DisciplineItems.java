package com.talkweb.discipline.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSONObject;

public class DisciplineItems implements Serializable{
 
	private static final long serialVersionUID = -8916808411946684973L;
 
	private String[] classTitleId = null;

    private Integer headNameIndex = 0;

    private String[] classTitleName = null;

    private int headRowNum = 1;
 
    private String[] headTitleName = null;
 
    private String[] excelclasses = null;
 
    private List<DisciplineDetail> successInfos = null;
 
    private List<JSONObject> errorInfos = null;
 
    private List<DisciplineExcel> l_disciExcel = new ArrayList<DisciplineExcel>();
    
    private String sessionId = null;
 
	public Integer getHeadNameIndex() {
		return headNameIndex;
	}
	public void setHeadNameIndex(Integer headNameIndex) {
		this.headNameIndex = headNameIndex;
	}
 
	public int getHeadRowNum() {
		return headRowNum;
	}
	public void setHeadRowNum(int headRowNum) {
		this.headRowNum = headRowNum;
	}
	public String[] getHeadTitleName() {
		return headTitleName;
	}
	public void setHeadTitleName(String[] headTitleName) {
		this.headTitleName = headTitleName;
	}
 
	public List<JSONObject> getErrorInfos() {
		return errorInfos;
	}
	public void setErrorInfos(List<JSONObject> errorInfos) {
		this.errorInfos = errorInfos;
	}
 
	public String getSessionId() {
		return sessionId;
	}
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	public String[] getClassTitleId() {
		return classTitleId;
	}
	public void setClassTitleId(String[] classTitleId) {
		this.classTitleId = classTitleId;
	}
	public String[] getClassTitleName() {
		return classTitleName;
	}
	public void setClassTitleName(String[] classTitleName) {
		this.classTitleName = classTitleName;
	}
	public String[] getExcelclasses() {
		return excelclasses;
	}
	public void setExcelclasses(String[] excelclasses) {
		this.excelclasses = excelclasses;
	}
	public List<DisciplineDetail> getSuccessInfos() {
		return successInfos;
	}
	public void setSuccessInfos(List<DisciplineDetail> successInfos) {
		this.successInfos = successInfos;
	}
	public List<DisciplineExcel> getL_disciExcel() {
		return l_disciExcel;
	}
	public void setL_disciExcel(List<DisciplineExcel> l_disciExcel) {
		this.l_disciExcel = l_disciExcel;
	}
	
 
 
}
