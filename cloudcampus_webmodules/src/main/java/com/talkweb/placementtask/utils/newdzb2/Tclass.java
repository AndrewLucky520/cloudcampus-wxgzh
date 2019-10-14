package com.talkweb.placementtask.utils.newdzb2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 教学班
 * @author lenovo
 *
 */
public final class Tclass{
		
	public Tclass(Subject subjectBean,Integer studentCount,Integer seq){
		this.tclassId = UUID.randomUUID().toString();
		this.subjectId = subjectBean.subjectId;
		this.studentCount = studentCount;
		this.isOpt = subjectBean.isOpt;
		this.seq = seq;
	}
	
	public Integer seq = null;
	public String tclassId = null;
	public Integer subjectId = null;
	/**
	 * 是否是选考
	 * 1.选考 0.学考
	 */
	public Integer isOpt = null;
	/**
	 * 班级学生数
	 */
	public Integer studentCount = null;
	
	/**
	 * 科目组合，人数Map
	 */
	public Map<String,Integer> fromSubjectCombinationsMap = new HashMap<String,Integer>();
	
	/**
	 * 学生IDs
	 */
	public List<Student> students = new ArrayList();
	/**
	 * 场地ID
	 */
	public String groundId = null;

}