package com.talkweb.teacher.dao;

import java.util.List;
import java.util.Map;

/**
 * @version 2.0
 * @Description 任课教师列表数据接口
 * @author 雷智
 * @date 2015年3月24日
 */

public interface TeacherClassDao {
	/**
	 * 任课教师设置列表查询
	 */
	public List<Map<String, Object>> queryTeacherList(Map<String, Object> param);

	/**
	 * 任课教师修改查询
	 */
	public List<Map<String, Object>> queryTeacherOne(Map<String, Object> param);

	/**
	 * 保存单个班级课程任课教师列表
	 */
	public int saveOneClassTeacher(Map<String, Object> param);

	/**
	 * 获取课程信息
	 */
	public List<Map<String, Object>> querylessoninfo();

	/**
	 * 删除已有信息
	 */
	public int saveDelete(Map<String, Object> del);

}
