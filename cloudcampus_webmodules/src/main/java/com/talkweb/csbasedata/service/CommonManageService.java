package com.talkweb.csbasedata.service;

import java.util.List;

import com.alibaba.fastjson.JSONObject;

public interface CommonManageService {

	/** 获取学校下的年级 **/
	List<JSONObject> getGradeFromSchool(JSONObject param);

	/** 获取年级下的班级 **/
	List<JSONObject> getClassFromGrade(JSONObject param);

	/** 获取学校下的科目 **/
	List<JSONObject> getLessonFromSchool(JSONObject param);

	/** 根据教师姓名查询教师信息 **/
	List<JSONObject> getTeacherByName(JSONObject param);
	/** 获取学校下的年级level **/
	List<JSONObject> getCurrentLevelFromSchool(JSONObject param);
	/** 根据教师姓名查询教师信息account **/
	List<JSONObject> getTeacherAccountByName(JSONObject param);
	/**根据accountId 将登陆状态从未激活改为正常 **/
	void toActive(JSONObject param)throws Exception;
}