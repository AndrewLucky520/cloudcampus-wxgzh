package com.talkweb.weekWork.pojo;

import java.io.Serializable;

import com.alibaba.fastjson.JSONObject;

public class ReportingPersonnel implements Serializable {
	private static final long serialVersionUID = -229087956462288736L;
	
	private long schoolId;
	private String departmentId;
	private Long teacherId;

	public long getSchoolId() {
		return schoolId;
	}

	public void setSchoolId(long schoolId) {
		this.schoolId = schoolId;
	}

	public String getDepartmentId() {
		return departmentId;
	}

	public void setDepartmentId(String departmentId) {
		this.departmentId = departmentId;
	}

	public Long getTeacherId() {
		return teacherId;
	}

	public void setTeacherId(Long teacherId) {
		this.teacherId = teacherId;
	}
	
	public String toString(){
		return JSONObject.toJSONString(this);
	}
}
