package com.talkweb.scoreManage.business;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.scoreManage.proc.ClassExamExcelDetail;
import com.talkweb.scoreManage.proc.ClassExamExcelTitle;

public class CustomItems implements Serializable {
	private static final long serialVersionUID = 5463395255780166718L;

	/**
	 * 导入教师excel的表头--系统字段
	 */
	private String[] teacherTitleId = null;

	/**
	 * 教师姓名在表头中的索引位置
	 */
	private Integer headNameIndex = 0;

	/**
	 * 班级在表头中的索引位置
	 */
	private Integer headClassNameIndex = 0;
	
	/**
	 * 身份证在表头中的索引位置
	 */
	private Integer headIDCardIndex = null;
	
	/**
	 * 手机号在表头中的索引位置
	 */
	private Integer headPhoneIndex = null;

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
	private List<ClassExamExcelDetail> successInfos = null;
	/**
	 * 教师名称匹配失败行信息
	 */
	private List<JSONObject> errorInfos = null;
	/**
	 * 封装的表头
	 */
	private List<ClassExamExcelTitle> l_ClassExamExcelTitle = new ArrayList<ClassExamExcelTitle>();
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

	public Integer getHeadIDCardIndex() {
		return headIDCardIndex;
	}

	public void setHeadIDCardIndex(Integer headIDCardIndex) {
		this.headIDCardIndex = headIDCardIndex;
	}

	public Integer getHeadPhoneIndex() {
		return headPhoneIndex;
	}

	public void setHeadPhoneIndex(Integer headPhoneIndex) {
		this.headPhoneIndex = headPhoneIndex;
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

	public List<ClassExamExcelDetail> getSuccessInfos() {
		return successInfos;
	}

	public void setSuccessInfos(List<ClassExamExcelDetail> successInfos) {
		this.successInfos = successInfos;
	}

	public List<JSONObject> getErrorInfos() {
		return errorInfos;
	}

	public void setErrorInfos(List<JSONObject> errorInfos) {
		this.errorInfos = errorInfos;
	}

	public List<ClassExamExcelTitle> getL_ClassExamExcelTitle() {
		return l_ClassExamExcelTitle;
	}

	public void setL_ClassExamExcelTitle(List<ClassExamExcelTitle> l_ClassExamExcelTitle) {
		this.l_ClassExamExcelTitle = l_ClassExamExcelTitle;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public Integer getHeadClassNameIndex() {
		return headClassNameIndex;
	}

	public void setHeadClassNameIndex(Integer headClassNameIndex) {
		this.headClassNameIndex = headClassNameIndex;
	}

	public String toString() {
		return JSONObject.toJSONString(this);
	}
}
