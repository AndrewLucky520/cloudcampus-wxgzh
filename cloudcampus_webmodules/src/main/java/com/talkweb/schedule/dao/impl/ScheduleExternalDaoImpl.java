package com.talkweb.schedule.dao.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.base.dao.impl.MyBatisBaseDaoImpl;
import com.talkweb.schedule.dao.ScheduleExternalDao;
import com.talkweb.schedule.entity.TSchScheduleGradeset;
import com.talkweb.schedule.entity.TSchStudentTclassRelationship;
import com.talkweb.schedule.entity.TSchTask;
import com.talkweb.schedule.entity.TSchTclass;
@Repository
public class ScheduleExternalDaoImpl extends MyBatisBaseDaoImpl implements ScheduleExternalDao {

	private static final String MYBATIS_PREFIX = "com.talkweb.schedule.dao.ScheduleExternalDao.";
	
	@Override
	public List<JSONObject> getScheduleForExam(Map<String, Object> map) {
		return selectList(MYBATIS_PREFIX+"getScheduleForExamManager", map);
	}

	@Override
	public List<JSONObject> getSubjectForExam(Map<String, Object> map) {
		return selectList(MYBATIS_PREFIX+"getSubjectForExam", map);
	}

	@Override
	public List<JSONObject> getStudentForExam(Map<String, Object> map) {
		return selectList(MYBATIS_PREFIX+"getStudentForExam", map);
	}

	@Override
	public List<JSONObject> getTclassForExam(Map<String, Object> map) {
		// TODO Auto-generated method stub
		return selectList(MYBATIS_PREFIX+"getTclassForExam",map);
	}

	@Override
	public List<JSONObject> getExamTclassSubMap(Map<String, Object> map) {
		// TODO Auto-generated method stub
		return selectList(MYBATIS_PREFIX+"getExamTclassSubMap",map);
	}
	
	
	
	
	@Override
	public List<JSONObject> querySubjLevelAndIdInClass(Map<String, Object> map) {
		List<JSONObject> result = selectList(MYBATIS_PREFIX + "querySubjLevelAndIdInClass", map);
		if(result == null) {
			return new ArrayList<JSONObject>();
		}
		return result;
	}
	
	@Override
	public List<Long> querySubjIdInTSchTask(Map<String, Object> map) {
		List<Long> result = selectList(MYBATIS_PREFIX + "querySubjIdInTSchTask", map);
		if(result == null) {
			return new ArrayList<Long>();
		}
		return result;
	}
	
	@Override
	public TSchScheduleGradeset queryPlacementInfoByGrade(Map<String, Object> map) {
		return selectOne(MYBATIS_PREFIX + "queryPlacementInfoByGrade", map);
	}
	
	@Override
	public List<JSONObject> queryTClassInfoList(Map<String, Object> map) {
		List<JSONObject> result = selectList(MYBATIS_PREFIX + "queryTClassInfoList", map);
		if(result == null) {
			return new ArrayList<JSONObject>();
		}
		return result;
	}
	
	@Override
	public List<TSchTask> queryTSchTask(Map<String, Object> map) {
		List<TSchTask> result = selectList(MYBATIS_PREFIX + "queryTSchTask", map);
		if(result == null) {
			return new ArrayList<TSchTask>();
		}
		return result;
	}
	
	@Override
	public List<TSchStudentTclassRelationship> queryStudentInfoList(Map<String, Object> map) {
		List<TSchStudentTclassRelationship> result = selectList(MYBATIS_PREFIX + "queryStudentInfoList", map);
		if(result == null) {
			return new ArrayList<TSchStudentTclassRelationship>();
		}
		return result;
	}

	@Override
	public List<TSchTclass> getTclassList(Map<String, Object> map) {
		// TODO Auto-generated method stub
		return selectList(MYBATIS_PREFIX+"queryTclassList", map);
	}
}
