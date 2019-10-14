package com.talkweb.teacher.service;

import java.util.List;
import java.util.Map;

public interface TeacherClassService {
	/**
	 * 任课教师设置列表查询
	 */
	public List<Map<String, Object>> getTeacherList(Map<String, Object> param);

	/**
	 * 任课教师设置列表查询
	 */
	public List<Map<String, Object>> queryTeacherOne(Map<String, Object> param);

	/**
	 * 保存单个班级课程任课教师列表
	 */
	public boolean saveOneClassTeacher(Map<String, Object> param);

	/**
	 * 获取课程信息
	 */
	public List<Map<String, Object>> querylessoninfo();

	/**
	 * 删除待添加班级信息
	 */
	public int saveDelete(Map<String, Object> del);
}
