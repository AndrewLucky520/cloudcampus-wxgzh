package com.talkweb.commondata.dao.impl;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.base.dao.impl.MyBatisBaseDaoImpl;
import com.talkweb.commondata.dao.LessonManageDao;

@Repository
public class LessonManageDaoImpl extends MyBatisBaseDaoImpl implements
		LessonManageDao {

	@Override
	public List<JSONObject> getLessonList(String schoolId,String termInfoId) {
		JSONObject json = new JSONObject();
		json.put("schoolId", schoolId);
		json.put("termInfoId", termInfoId);
		return selectList("getLessonList",json);
	}
	
	@Override
	public int insertLesson(JSONObject param) {
		return insert("insertLesson",param);
	}
	
	@Override
	public int insertSchoolLesson(JSONObject param) {
		return insert("insertSchoolLesson",param);
	}

	@Override
	public int updateLesson(JSONObject param) {
		return update("updateLesson",param);
	}

	@Override
	public int deleteLesson(JSONObject json) {
		return delete("deleteLesson",json);
	}

	@Override
	public List<JSONObject> getLessonByName(JSONObject param) {
		return selectList("getLessonByName",param);
	}

}