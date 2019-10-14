package com.talkweb.teacher.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.teacher.domain.page.THrTeacher;

/**
 * @version 2.0
 * @Description: 教师业务逻辑接口
 * @author 雷智
 * @date 2015年3月5日
 */
public interface TeacherService {
	/**
	 * 根据id查询教师信息
	 * 
	 * @return THrTeacher对象
	 */
	public THrTeacher queryTeacherById(String zgh);

	/**
	 * 根据id删除教师信息
	 * 
	 * @return Map对象，对象包含查询结果列表和返回码（0正确，负数为错误）
	 */
	public Map<String, Object> delTeacherById(String id);

	/**
	 * 添加教师信息
	 * 
	 * @return Map对象，对象包含添加返回消息和返回码（0正确，负数为错误）
	 */
	public Map<String, Object> addTeacher(THrTeacher teacher);

	/**
	 * 修改教师信息
	 * 
	 * @return Map对象，对象包含修改消息和返回码（0正确，负数为错误）
	 */
	public Map<String, Object> updateTeacher(THrTeacher teacher);

	/**
	 * 按条件查询教师信息
	 * 
	 * @return Map对象，对象包含教师列表和返回码（0正确，负数为错误）
	 */
	public List<THrTeacher> queryTeacherByLimit(Map<String, Object> limit);

	/**
	 * 按id集合删除教师信息
	 * 
	 * @return Map对象，对象包含教师删除和返回码（0正确，负数为错误）
	 */
	public Map<String, Object> deleteTeacherByIdList(List<String> IdList);

	/**
	 * 获取教师集合等
	 * @param map
	 * @return
	 */
    public JSONObject getAllTeaBySchoolNum(HashMap map);

    /**
     * 批量插入教师
     * @param needInsert
     */
    public void insertTeaList(List<THrTeacher> needInsert);
}
