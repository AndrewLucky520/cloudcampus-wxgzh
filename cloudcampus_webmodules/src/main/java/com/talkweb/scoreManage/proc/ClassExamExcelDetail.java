package com.talkweb.scoreManage.proc;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSONObject;

/**
 * 
 * @author WXQ
 *
 */
public class ClassExamExcelDetail implements Serializable {
	private static final long serialVersionUID = 7937143986911817053L;
	private String examId;
	private String schoolId;
	private String stuId;
	private String classId;
	private String stuName;
	private String className;
	private String xnxq;
	private List<String> cellIds = new ArrayList<String>();
	private List<String> cellValues = new ArrayList<String>();
	private List<JSONObject> errorlist = new ArrayList<JSONObject>();

	public List<JSONObject> getErrorlist() {
		return errorlist;
	}

	public void setErrorlist(List<JSONObject> errorlist) {
		this.errorlist = errorlist;
	}

	public String getXnxq() {
		return xnxq;
	}

	public void setXnxq(String xnxq) {
		this.xnxq = xnxq;
	}

	public String getClassId() {
		return classId;
	}

	public void setClassId(String classId) {
		this.classId = classId;
	}

	public String getExamId() {
		return examId;
	}

	public void setExamId(String examId) {
		this.examId = examId;
	}

	public String getSchoolId() {
		return schoolId;
	}

	public void setSchoolId(String schoolId) {
		this.schoolId = schoolId;
	}

	public String getStuId() {
		return stuId;
	}

	public String getStuName() {
		return stuName;
	}

	public void setStuName(String stuName) {
		this.stuName = stuName;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public void setStuId(String stuId) {
		this.stuId = stuId;
	}

	public List<String> getCellIds() {
		return cellIds;
	}

	public void setCellIds(List<String> cellIds) {
		this.cellIds = cellIds;
	}

	public List<String> getCellValues() {
		return cellValues;
	}

	public void setCellValues(List<String> cellValues) {
		this.cellValues = cellValues;
	}

	@Override
	public String toString() {
		return JSONObject.toJSONString(this);
	}

}
