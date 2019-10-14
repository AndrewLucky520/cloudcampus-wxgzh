package com.talkweb.commondata.service.impl;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.commondata.dao.LessonManageDao;
import com.talkweb.commondata.service.LessonManageService;

@Service("lessonManageService")
public class LessonManageServiceImpl implements LessonManageService {
	
	@Autowired
	private LessonManageDao lessonManageDao;

	/**
	 * 获取科目信息列表(科目名称、科目简称)
	 * 
	 * @param schoolId
	 *            学校代码
	 * 
	 * @return 科目信息List
	*/
	public List<JSONObject> getLessonList(String schoolId,String termInfoId) {
		return lessonManageDao.getLessonList(schoolId,termInfoId);
	}

	/**
	 * 更新科目信息(科目名称、科目简称)
	 * 
	 * @param schoolId
	 *            学校代码
	 * @param lessonName
	 *            科目名称
	 * @param lessonSimpleName
	 *            科目简称
	 * @return Integer
	 * @update 2018/07/12 zhanghuihui15222 删除科目判断重复
	*/
	public int updateLesson(JSONObject object) {
		JSONObject param = new JSONObject();
		param.put("schoolId", object.getString("schoolId"));
		param.put("termInfoId", object.getString("termInfoId"));
		String simpleName = object.getString("lessonSimpleName");	
		param.put("simpleName", simpleName);		
		int count = 0;
		String lessonName = object.getString("lessonName");
		param.put("name", lessonName);	
		String lessonId = object.getString("lessonId");
		if(!StringUtils.isBlank(lessonId)){
			param.put("id", lessonId);
		}else{
			param.put("id", "");
		}
		/*List<JSONObject> list = lessonManageDao.getLessonByName(param);
		if(!(list==null || list.size()<1)){
			return -1;
		}*/
		if (StringUtils.isEmpty(lessonId))
		{
			param.put("uuid", UUID.randomUUID().toString());
			param.put("status", 1);
			Date time = new Date();
			param.put("autoCreateTime", time);
			param.put("autoUpdateTime", time);
	        param.put("lessonType", 0);	
	        count = lessonManageDao.insertLesson(param);
	        String id = param.getString("id");
	        JSONObject sl = new JSONObject();
	        sl.put("lessonId", Long.parseLong(id));
	        sl.put("schoolId", object.getString("schoolId"));
	        sl.put("termInfoId", object.getString("termInfoId"));
	        lessonManageDao.insertSchoolLesson(sl);
		}else{
			count = lessonManageDao.updateLesson(param);
		}	
		return count;
	}

	/**
	 * 删除科目信息
	 * 
	 * @param schoolId
	 *            学校代码
	 * @param lessonIds
	 *            科目代码
	 * @return Integer
	*/
	public int deleteLesson(JSONObject param) {
		String lessonIds = param.getString("lessonIds");
		String[] lessonArray = lessonIds.split(",");
		List<String> list = Arrays.asList(lessonArray);
		JSONObject json = new JSONObject();
		json.put("list", list);
		json.put("termInfoId", param.getString("termInfoId"));
		return lessonManageDao.deleteLesson(json);
	}
	
}