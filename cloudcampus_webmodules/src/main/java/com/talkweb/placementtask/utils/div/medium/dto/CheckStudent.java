package com.talkweb.placementtask.utils.div.medium.dto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

import com.google.common.collect.ArrayListMultimap;
import com.talkweb.placementtask.utils.div.dto.ClassResult;
import com.talkweb.placementtask.utils.div.dto.Student;

public class CheckStudent {
	private Student student;
	private List<ClassResult> adClassList = new ArrayList<>();
	
	private ArrayListMultimap<String, ClassResult> subjectId2TeachClassResultMap = ArrayListMultimap.create();
	private ArrayListMultimap<String, ClassResult> classId2TeachClassResultMap = ArrayListMultimap.create();
	
	public CheckStudent(Student student) {
		this.student = student;
	}
	
	public Student getStudent() {
		return student;
	}
	public void setStudent(Student student) {
		this.student = student;
	}
	public List<ClassResult> getAdClassList() {
		return adClassList;
	}
	public void setAdClassList(List<ClassResult> adClassList) {
		this.adClassList = adClassList;
	}

	public ArrayListMultimap<String, ClassResult> getSubjectId2TeachClassResultMap() {
		return subjectId2TeachClassResultMap;
	}

	public void setSubjectId2TeachClassResultMap(ArrayListMultimap<String, ClassResult> subjectId2TeachClassResultMap) {
		this.subjectId2TeachClassResultMap = subjectId2TeachClassResultMap;
	}

	public ArrayListMultimap<String, ClassResult> getClassId2TeachClassResultMap() {
		return classId2TeachClassResultMap;
	}

	public void setClassId2TeachClassResultMap(ArrayListMultimap<String, ClassResult> classId2TeachClassResultMap) {
		this.classId2TeachClassResultMap = classId2TeachClassResultMap;
	}

	@Override
	public String toString() {
		
		StringBuffer sb= new StringBuffer("CheckStudent student="+this.getStudent().toString()+"\n");
		sb.append("adClassList:\n");
		for (ClassResult classResult : adClassList) {
			sb.append(classResult.getId());
		}
		
		sb.append("\nsubjectId2TeachClassResultMap:");
		for (Entry<String, Collection<ClassResult>> entry : this.subjectId2TeachClassResultMap.asMap().entrySet()) {
			sb.append("\n"+entry.getKey()+"科目教学班:"+entry.getValue());
		}
		
		return sb.toString();
	}

	
}
