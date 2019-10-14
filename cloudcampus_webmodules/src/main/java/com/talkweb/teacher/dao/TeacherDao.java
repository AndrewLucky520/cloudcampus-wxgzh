package com.talkweb.teacher.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.teacher.domain.page.THrTeacher;

/**
 * @version 2.0
 * @Description: 教师用户数据接口
 * @author 雷智
 * @date 2015年3月4日
 */
public interface TeacherDao {
	/**
	 * 添加一个教师信息
	 * 
	 * @return
	 */
	public int addTeacher(THrTeacher teacher);

	/**
	 * 删除一个教师信息
	 * 
	 * @return
	 */
	public int deleteTeacher(String id);

	/**
	 * 修改一个教师信息
	 * 
	 * @return
	 */
	public int updateTeacher(THrTeacher teacher);

	/**
	 * 查询一个教师信息
	 * 
	 * @return
	 */
	public THrTeacher queryTeacherOne(String id);

	/**
	 * 查询教师列表
	 * 
	 * @return
	 */
	public List<THrTeacher> queryTeacherList();

	/**
	 * 根据条件查询教师列表
	 * 
	 * @return
	 */
	public List<THrTeacher> queryTeacherInLimit(Map<String, Object> limit);

	/**
	 * 根据id集合删除多个教师
	 * 
	 * @return
	 */
	public int deleteTeacherByIdList(List<String> IdList);
	
	/**
	 * 根据用户系统id获取职工号
	 * @param userSysId 用户系统Id
	 * @return 职工号
	 */
	public String getZGHByUserSysId(String userSysId);

	/**
	 * 获取教师列表
	 * @param map
	 * @return
	 */
    public List<JSONObject> getAllTeaBySchoolNum(HashMap map);

    public void insertTeaList(List<THrTeacher> needInsert);

}
