package com.talkweb.salary.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSONObject;

public class SalItems implements Serializable{

	/**
     * 导入教师excel的表头--系统字段
     */
	private String[] teacherTitleId = null;

    /**
     * 教师姓名在表头中的索引位置
     */
    private Integer headNameIndex = 0;
    
    /**
     * 导入教师excel的表头--系统字段字段名
     */
    private String[] teacherTitleName = null;
    
    /**
     * 表头行数
     */
    private int headRowNum = 1;
    /**
     * 导入教师excel的表头--表头字段名
     */
    private String[] headTitleName = null;
    /**
     * Excel表中教师名称
     */
    private String[] excelTeachers = null;

    /**
     * 教师名称匹配成功行信息
     */
    private List<SalDetail> successInfos = null;
    /**
     * 教师名称匹配失败行信息
     */
    private List<JSONObject> errorInfos = null;
    /**
     *封装的表头
     */
    private List<SalExcel> l_salExcel = new ArrayList<SalExcel>();
    private String sessionId = null;
	public String[] getTeacherTitleId() {
		return teacherTitleId;
	}
	public void setTeacherTitleId(String[] teacherTitleId) {
		this.teacherTitleId = teacherTitleId;
	}
	public Integer getHeadNameIndex() {
		return headNameIndex;
	}
	public void setHeadNameIndex(Integer headNameIndex) {
		this.headNameIndex = headNameIndex;
	}
	public String[] getTeacherTitleName() {
		return teacherTitleName;
	}
	public void setTeacherTitleName(String[] teacherTitleName) {
		this.teacherTitleName = teacherTitleName;
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
	public String[] getExcelTeachers() {
		return excelTeachers;
	}
	public void setExcelTeachers(String[] excelTeachers) {
		this.excelTeachers = excelTeachers;
	}
	public List<SalDetail> getSuccessInfos() {
		return successInfos;
	}
	public void setSuccessInfos(List<SalDetail> successInfos) {
		this.successInfos = successInfos;
	}
	public List<JSONObject> getErrorInfos() {
		return errorInfos;
	}
	public void setErrorInfos(List<JSONObject> errorInfos) {
		this.errorInfos = errorInfos;
	}
	public List<SalExcel> getL_salExcel() {
		return l_salExcel;
	}
	public void setL_salExcel(List<SalExcel> l_salExcel) {
		this.l_salExcel = l_salExcel;
	}
	public String getSessionId() {
		return sessionId;
	}
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
    
}
