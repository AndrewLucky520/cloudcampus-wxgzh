package com.talkweb.student.dao;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.student.domain.business.TSsStudent;

/**
 * 
 * @ClassName: StudentManageDao.java
 * @version:2.0
 * @Description: 学生信息维护管理数据层接口定义
 * @author 吴安辉
 * @date 2015年3月9日
 */
public interface StudentManageDao {

	/**
	 * 添加学生
	 * @param student
	 * @return
	 */
	public int addStudent(TSsStudent student);
	
	/**
	 * 添加学生注册信息
	 * @param student
	 * @return
	 */
	public int addStudenrol(Map<String,String> student);
	
	/**
	 * 删除学生信息（设为不在校）
	 * @param student
	 * @return
	 */
	public int deleteStudent(List<String> student);
	
	/**
	 * 删除学生注册信息（设为不在校）
	 * @param student
	 * @return
	 */
	public int deleteStudenrol(List<String> student);
	
	/**
	 * 查询学生信息列表
	 * @param param
	 * @return
	 */
	public List<JSONObject> getStudentList(Map<String,Object> param);
	
	/**
	 * 通过学生学号查询学生信息
	 * @param xh
	 * @return
	 */
	public TSsStudent getStudentById(String xh);
	
	/**
	 * 修改学生信息
	 * @param student
	 * @return
	 */
	public int updateStudent(TSsStudent student);
	
	/**
	 * 修改学生注册表
	 * @param student
	 * @return
	 */
	public int updateStudenrol(Map<String,String> student);
	
	/**
	 * 通过班级班号查询班级的学制信息
	 * @param bh
	 * @return
	 */
	public Map<String,Object> getXzInfoByBh(String bh);
	
	/**
	 * 查询指定学生的注册信息中最大的学年信息
	 * @param xh
	 * @return
	 */
	public Map<String,Object> getMaxXnxqByXh(String xh);
	
	/**
	 * 删除指定学生的指定学年学期的注册信息
	 * @param param
	 * @return
	 */
	public int deleteStudenrolByXh(Map<String,Object> param);
	
}
