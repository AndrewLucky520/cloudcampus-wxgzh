package com.talkweb.salary.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSONObject;
/**
 * 单个教师的一条工资信息，包括工资单Id，学校Id，教师Id，工资条目Id集合，工资发放金额集合
 * @author WXQ
 *
 */
public class SalDetail implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String salaryId;
	private String schoolId;
	private String teacherId;
	private String classId;
	private String xn;
	private String xqm;
	private List<String> salComponentIds = new ArrayList<String>();
	private List<String> salNums = new ArrayList<String>();
	private List<JSONObject> errorlist=new ArrayList<JSONObject>();
	
	
	public List<JSONObject> getErrorlist() {
		return errorlist;
	}
	public void setErrorlist(List<JSONObject> errorlist) {
		this.errorlist = errorlist;
	}
	public String getXn() {
		return xn;
	}
	public void setXn(String xn) {
		this.xn = xn;
	}
	public String getXqm() {
		return xqm;
	}
	public void setXqm(String xqm) {
		this.xqm = xqm;
	}
	public String getClassId() {
		return classId;
	}
	public void setClassId(String classId) {
		this.classId = classId;
	}
	public String getSalaryId() {
		return salaryId;
	}
	public void setSalaryId(String salaryId) {
		this.salaryId = salaryId;
	}
	public String getSchoolId() {
		return schoolId;
	}
	public void setSchoolId(String schoolId) {
		this.schoolId = schoolId;
	}
	public String getTeacherId() {
		return teacherId;
	}
	public void setTeacherId(String teacherId) {
		this.teacherId = teacherId;
	}
	public List<String> getSalComponentIds() {
		return salComponentIds;
	}
	public void setSalComponentIds(List<String> salComponentIds) {
		this.salComponentIds = salComponentIds;
	}
	public List<String> getSalNums() {
		return salNums;
	}
	public void setSalNums(List<String> salNums) {
		this.salNums = salNums;
	}
	@Override
	public String toString() {
		return "SalDetail [salComponentIds=" + salComponentIds + ", salNums="
				+ salNums + "]";
	}
	
}
