package com.talkweb.student.service;

import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.student.domain.business.TSsStudent;

/**
 * 
 * @ClassName: StudentManageService.java
 * @version:2.0
 * @Description: 学生信息维护管理接口定义
 * @author 吴安辉
 * @date 2015年3月9日
 */
public interface StudentManageService {

	/**
	 * 添加学生
	 * @param student
	 * @return
	 */
	public Map<String,Object> addStudent(TSsStudent student,String xn,String xqm);
	
	/**
	 * 查询学生信息列表
	 * @param student
	 * @param xn
	 * @return
	 */
	public JSONObject getStudentList(Map<String,Object> param,String xn);
	
	/**
	 * 查询单个学生信息
	 * @param xh
	 * @return
	 */
	public TSsStudent getStudentById(String xh);
	
	/**
	 * 修改学生信息
	 * @param student 学生参数对象
	 * @param xn 学年
	 * @return
	 */
	public Map<String,Object> updateStudent(TSsStudent student,String xn);
	
	/**
	 * 删除学生信息
	 * @param xhs
	 * @return
	 */
	public Map<String,Object> deleteStudent(String[] xhs);
}
