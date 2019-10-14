package com.talkweb.scoreManage.business;

/****
 * 学生基本信息包括，姓名，学号，班级信息存放。
 * 
 * @author guoyuanbing
 *
 */
public class StudentBaseInfo {

	private String classsName;// 班级名称
	private Long studentNo;// 学生学号
	private String studentName;// 姓名

	private Long classId;// 班级编号

	private String enrolYear;// 入学年度

	public String getEnrolYear() {
		return enrolYear;
	}

	public void setEnrolYear(String enrolYear) {
		this.enrolYear = enrolYear;
	}

	public Long getClassId() {
		return classId;
	}

	public void setClassId(Long classId) {
		this.classId = classId;
	}

	public String getClasssName() {
		return classsName;
	}

	public void setClasssName(String classsName) {
		this.classsName = classsName;
	}

	public Long getStudentNo() {
		return studentNo;
	}

	public void setStudentNo(Long studentNo) {
		this.studentNo = studentNo;
	}

	public String getStudentName() {
		return studentName;
	}

	public void setStudentName(String studentName) {
		this.studentName = studentName;
	}

}
