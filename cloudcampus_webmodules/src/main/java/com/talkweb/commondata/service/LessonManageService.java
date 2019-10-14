package com.talkweb.commondata.service;

import java.util.List;

import com.alibaba.fastjson.JSONObject;

public interface LessonManageService {

	/** -----更新科目信息----- **/
	public int updateLesson(JSONObject param)throws Exception;

	/** -----删除科目信息----- **/
	public int deleteLesson(JSONObject param)throws Exception;
	
	/** -----查询科目列表----- 
	 * @param termInfoId **/
	public List<JSONObject> getLessonList(String schoolId, String termInfoId)throws Exception;
	
}