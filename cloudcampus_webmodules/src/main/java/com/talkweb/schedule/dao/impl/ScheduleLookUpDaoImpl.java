package com.talkweb.schedule.dao.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.base.dao.impl.MyBatisBaseDaoImpl;
import com.talkweb.schedule.dao.ScheduleLookUpDao;
@Repository
public class ScheduleLookUpDaoImpl extends MyBatisBaseDaoImpl implements ScheduleLookUpDao {
	
	@Override
	public List<JSONObject> getSchedule(Map<String, String> map) {
		return selectList("getSchedule", map);
	}

	@Override
	public int updateSchedule(JSONObject object) {
		return update("updateSchedule", object);
	}
	
	@Override
	public int addSchedule(Map<String, Object> map) {
		return insert("addSchedule", map);
	}
	
	@Override
	public void deleteSchedule(JSONObject object) {
		delete("deleteSchedule", object);
	}

	@Override
	public List<JSONObject> getClassTb(Map<String, Object> param) {
		// TODO Auto-generated method stub
		return selectList("getClassTb",param);
	}

	@Override
	public List<JSONObject> getClassTbTeachers(Map<String, Object> param) {
		// TODO Auto-generated method stub
		return selectList("getClassTbTeachers",param);
	}

	@Override
	public List<JSONObject> getClassTbZOU(Map<String, Object> param) {
		// TODO Auto-generated method stub
		return selectList("getClassTbZOU",param);
	}

	@Override
	public List<JSONObject> getTeacherTb(Map<String, Object> param) {
		// TODO Auto-generated method stub
		return selectList("getTeacherTb",param);
	}

	@Override
	public List<JSONObject> getStudentTb(Map<String, Object> param) {
		// TODO Auto-generated method stub
		return selectList("getnewStudentTb",param);
	}

	@Override
	public List<JSONObject> getGroundTb(Map<String, Object> param) {
		// TODO Auto-generated method stub
		return selectList("getGroundTb",param);
	}

	@Override
	public List<JSONObject> getGradeTbs(Map<String, Object> param) {
		// TODO Auto-generated method stub
		return selectList("getGradeTbs",param);
	}

	@Override
	public List<JSONObject> getStudentClassTbs(Map<String, Object> param) {
		// TODO Auto-generated method stub
		return selectList("getStudentClassTbs",param);
	}

	@Override
	public JSONObject getNewTimetablePrintSet(Map<String, Object> param) {
		// TODO Auto-generated method stub
		return selectOne("getNewTimetablePrintSet",param);
	}

	@Override
	public void updateNewTimetablePrintSet(Map<String, Object> param) {
		// TODO Auto-generated method stub
		update("updateNewTimetablePrintSet",param);
	}

	@Override
	public JSONObject getScheduleTimetableById(Map<String, Object> param) {
		// TODO Auto-generated method stub
		return  selectOne("getScheduleTimetableById",param);
	}

	@Override
	public void insertNewTimetablePrintSet(Map<String, Object> param) {
		// TODO Auto-generated method stub
		insert("insertNewTimetablePrintSet",param);
	}

	@Override
	public List<JSONObject> queryStuScheduleList(JSONObject object) {
		// TODO Auto-generated method stub
		return selectList("com.talkweb.schedule.dao.ScheduleLookUpDao.queryStuScheduleList",object);
	}

	@Override
	public List<JSONObject> queryStudentSelfTimetable(JSONObject obj) {
		// TODO Auto-generated method stub
		return selectList("com.talkweb.schedule.dao.ScheduleLookUpDao.queryStudentSelfTimetable",obj);
	}

	@Override
	public List<JSONObject> queryStudentSelfExt1Timetable(JSONObject object) {
		// TODO Auto-generated method stub
		return selectList("com.talkweb.schedule.dao.ScheduleLookUpDao.queryStudentSelfExt1Timetable",object);
	}

	@Override
	public List<JSONObject> getClassTbZOU1(HashMap<String, Object> param) {
		// TODO Auto-generated method stub
		return selectList("com.talkweb.schedule.dao.ScheduleLookUpDao.getClassTbZOU1",param);
	}

	@Override
	public List<JSONObject> queryStuInTclass(Map<String, Object> param) {
		// TODO Auto-generated method stub
		return selectList("com.talkweb.schedule.dao.ScheduleLookUpDao.queryStuInTclass",param);
	}

	@Override
	public List<JSONObject> evalOfSubInSchedule(Map<String, Object> param) {
		// TODO Auto-generated method stub
		return selectList("com.talkweb.schedule.dao.ScheduleLookUpDao.evalOfSubInSchedule",param);
	}

	@Override
	public List<JSONObject> getTeacherLessonsStatistics(JSONObject params) {
		// TODO Auto-generated method stub
		return selectList("com.talkweb.schedule.dao.ScheduleLookUpDao.getTeacherLessonsStatistics",params);
	}

	@Override
	public List<JSONObject> getStudentTbForApp(Map<String, Object> params) {
		// TODO Auto-generated method stub
		return selectList("com.talkweb.schedule.dao.ScheduleLookUpDao.getnewStudentTbForApp",params);
	}

}
