package com.talkweb.timetable.dynamicProgram.entity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.talkweb.accountcenter.thrift.Account;
import com.talkweb.accountcenter.thrift.Classroom;
import com.talkweb.accountcenter.thrift.Grade;
import com.talkweb.accountcenter.thrift.LessonInfo;

public class ScheduleDatadic implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 703119113732004193L;

	/**
	 * 学年
	 */
	private String xn;
	/**
	 * 学期
	 */
	private String xq;
	/**
	 * 使用年级-年级
	 */
	private Map<String, Grade> gradesDic = new HashMap<String, Grade>();
	
	public String getXn() {
		return xn;
	}

	public void setXn(String xn) {
		this.xn = xn;
	}

	public String getXq() {
		return xq;
	}

	public void setXq(String xq) {
		this.xq = xq;
	}

	/**
	 * 年级id-使用年级
	 */
	private Map<Long,String> gradeIdSynjMap = new HashMap<Long, String>();

	public Map<Long, String> getGradeIdSynjMap() {
		return gradeIdSynjMap;
	}
	
	private Map<String, Grade> gradesDicSrc = new HashMap<String, Grade>();
	/**
	 * 年级id-年级
	 */
	private Map<Long, Grade> gradeIdGradeDic = new HashMap<Long, Grade>();
	
	public Map<String, Grade> getGradesDic() {
		return gradesDic;
	}

	public void setGradesDic(Map<String, Grade> gradesDic) {
		this.gradesDic = gradesDic;

		for(Iterator<String> it = gradesDic.keySet().iterator();it.hasNext();){
			String synj = it.next();
			Grade grade = gradesDic.get(synj);
			
			this.gradeIdSynjMap.put(grade.getId(), synj);
			
			this.gradesDicSrc.put(String.valueOf(grade.getId()), grade);
			
			this.gradeIdGradeDic.put(grade.getId(), grade);
			
			this.gradeLevGradeDic.put(grade.getCurrentLevel().getValue(), grade);
		}
	}

	public Map<String, Grade> getGradesDicSrc() {
		return gradesDicSrc;
	}

	public void setGradesDicSrc(Map<String, Grade> gradesDicSrc) {
		this.gradesDicSrc = gradesDicSrc;
	}

	public Map<Long, Grade> getGradeIdGradeDic() {
		return gradeIdGradeDic;
	}

	public void setGradeIdGradeDic(Map<Long, Grade> gradeIdGradeDic) {
		this.gradeIdGradeDic = gradeIdGradeDic;
	}

	public Map<Integer, Grade> getGradeLevGradeDic() {
		return gradeLevGradeDic;
	}

	public void setGradeLevGradeDic(Map<Integer, Grade> gradeLevGradeDic) {
		this.gradeLevGradeDic = gradeLevGradeDic;
	}

	public Map<String, Classroom> getClassroomsDic() {
		return classroomsDic;
	}

	public void setClassroomsDic(Map<String, Classroom> classroomsDic) {
		this.classroomsDic = classroomsDic;
	}

	public Map<String, Account> getTeachersDic() {
		return teachersDic;
	}

	public void setTeachersDic(Map<String, Account> teachersDic) {
		this.teachersDic = teachersDic;
	}

	public Map<String, LessonInfo> getCoursesDic() {
		return coursesDic;
	}

	public void setCoursesDic(Map<String, LessonInfo> coursesDic) {
		this.coursesDic = coursesDic;
	}

	public void setGradeIdSynjMap(Map<Long, String> gradeIdSynjMap) {
		this.gradeIdSynjMap = gradeIdSynjMap;
	}

	private Map<Integer, Grade> gradeLevGradeDic = new HashMap<Integer, Grade>();

	private Map<String, Classroom> classroomsDic = new HashMap<String, Classroom>();

	private Map<String, Account> teachersDic = new HashMap<String, Account>();

	private Map<String, LessonInfo> coursesDic = new HashMap<String, LessonInfo>();

	// 取年级信息
	public Grade getGradeBySynj(String gradeId) {
		return this.gradesDic.get(gradeId);
	}

	// 根据主键获取年级
	public Grade getGradeInfoByGradeId(String gradeId) {
		return this.gradesDicSrc.get(gradeId);
	}

	// 取班级信息
	public Classroom getClassById(String classId) {
		return this.classroomsDic.get(classId);
	}

	public String getClassNameById (String classId){
		String name = "";
		if(classroomsDic.containsKey(classId) ){
			name=  classroomsDic.get(classId).getClassName();
		}
		return name ;
	}
	// 取教师信息
	public String getTeacherNameById(String teacherId) {
		String name = "";
		if(this.teachersDic.containsKey(teacherId)){
			name = this.teachersDic.get(teacherId).getName();
		}
		return name;
	}

	// 取科目信息
	public String getCourseNameById(String courseId) {
		String name = "";
		if(this.coursesDic.containsKey(courseId)){
			name = this.coursesDic.get(courseId).getName();
		}
		return name;
	}

	/**
	 * 根据班级id获取年级等级
	 * @param classId
	 * @return
	 */
	public String getGradeLevelByClassId(String classId) {
		// TODO Auto-generated method stub
		Classroom clr =  this.getClassroomsDic().get(classId);
		if(clr!=null){
			Grade gd = this.gradeIdGradeDic.get(clr.getGradeId());
			if(gd!=null){
				return gd.getCurrentLevel().getValue()+"";
			}
		}
		return null;
	}
	/**
	 * 根据班级获取年级的使用年级
	 * @param classId
	 * @return
	 */
	public String getGradeSynjByClassId(String classId) {
		// TODO Auto-generated method stub
		Classroom clr =  this.getClassroomsDic().get(classId);
		if(clr!=null){
			return this.gradeIdSynjMap.get(clr.getGradeId());
		}
		return null;
	}
	
}
