package com.talkweb.schedule.dao.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.base.dao.impl.MyBatisBaseDaoImpl;
import com.talkweb.schedule.dao.ScheduleCommonDao;
@Repository
public class ScheduleCommonDaoImpl extends MyBatisBaseDaoImpl implements
		ScheduleCommonDao {
	
	@Override
	public JSONObject getScheduleGradePlacementInfo(HashMap<String, Object> map) {
		return selectOne("getScheduleGradePlacementInfo",map);
	}

	@Override
	public List<JSONObject> getScheduleGradeList(HashMap<String, Object> map) {
		return selectList("getScheduleGradeList",map);
	}

	@Override
	public List<JSONObject> getScheduleGradeListWithTeaching(HashMap<String, Object> map) {
		return selectList("getScheduleGradeListWithTeaching",map);
	}

	@Override
	public List<JSONObject> getScheduleSubjectList(HashMap<String, Object> map) {
		return selectList("getScheduleSubjectList",map);
	}

	@Override
	public List<JSONObject> getScheduleSubjectWithTeaching(HashMap<String, Object> map) {
		return selectList("getScheduleSubjectWithTeaching",map);
	}

	@Override
	public List<JSONObject> getScheduleTeacherList(HashMap<String, Object> map) {
		return selectList("getScheduleTeacherList",map);
	}
	
	@Override
	public List<JSONObject> getTeachingTaskGradeList(HashMap<String, Object> map) {		
		return selectList("getTeachingTaskGradeList",map);
	}
	
	@Override
	public List<JSONObject> getScheduleSubjectGroupList(HashMap<String, Object> map) {
		return selectList("getScheduleSubjectGroupList",map);
	}
	
	@Override
	public List<JSONObject> getScheduleSubjectGroupTclassList(HashMap<String, Object> map) {
		return selectList("getScheduleSubjectGroupTclassList",map);
	}

	@Override
	public List<JSONObject> getScheduleGroundList(JSONObject object) {
		return selectList("getScheduleGroundList",object);
	}

	@Override
	public List<JSONObject> getScheduleSubjectTclassList(JSONObject object) {
		return selectList("getScheduleSubjectTclassList",object);
	}

	@Override
	public List<JSONObject> getScheduleAllGroundList(JSONObject object) {
		// TODO Auto-generated method stub
		return selectList("getScheduleAllGroundList",object);
	}

	@Override
	public JSONObject getSchedule(Map<String, Object> param) {
		// TODO Auto-generated method stub
		return selectOne("getLatestPublishedSchedule", param);
	}

	@Override
	public String selectPlacementIdInSchedule(String placementId) {
		// TODO Auto-generated method stub
		return selectOne("selectPlacementIdInSchedule", placementId);
	}
	
		@Override
	public List<JSONObject> getSchedualListByTeacher(Map<String, Object> params) {
		// TODO Auto-generated method stub
		return selectList("getSchedualListByTeacher", params);
	}

	@Override
	public JSONObject getLatestPublishedScheduleByStudent(JSONObject params) {
		// TODO Auto-generated method stub
		return selectOne("getLatestPublishedScheduleByStudent", params);
	}
}