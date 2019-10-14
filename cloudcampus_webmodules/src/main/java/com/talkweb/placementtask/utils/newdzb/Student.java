package com.talkweb.placementtask.utils.newdzb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
/**
 * 
 * @author lenovo
 *
 */
public class Student {
	//账号ID
	public String accountId = null;
	//行政班ID
	public String classId = null;
	//年级
	public String useGrade = null;
	//年级ID
	public String gradeId = null;
	//志愿名称
	public String zhName = null;
	//志愿科目
	public String subjectIds = null;
	//
	public Integer order =0;
	public Student(){}
	public static Map<String,List<Student>> getStudentsMap(List<JSONObject> jsons){
		Map<String,List<Student>> studentsMap = new HashMap();
		for(JSONObject json:jsons){
			Student student = Student.getStudent(json);
			List<Student> students = studentsMap.get(student.subjectIds);
			if(students == null){
				students = new ArrayList();
				studentsMap.put(student.subjectIds,students);
			}
			students.add(student);
		}
		return studentsMap;
		
	}
	public static Student getStudent(JSONObject json){
		Student student = JSONObject.toJavaObject(json, Student.class);
		return student;
	}
	public Student(String accountId, String classId, String gradeId,
			String subjectIds) {
		super();
		this.accountId = accountId;
		this.classId = classId;
		this.gradeId = gradeId;
		this.subjectIds = subjectIds;
	}
	
	
	
}
