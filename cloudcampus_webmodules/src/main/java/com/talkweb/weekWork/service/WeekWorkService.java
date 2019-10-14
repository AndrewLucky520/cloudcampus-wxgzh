package com.talkweb.weekWork.service;

import java.util.Date;
import java.util.List;

import com.alibaba.fastjson.JSONObject;

public interface WeekWorkService {

	boolean getFillManInfo(JSONObject param);

	Date createOrGetStartWeekByTermInfo(JSONObject param);

	String getCurrentTermWeek(JSONObject param);

	JSONObject updateOrGetWeeklyRecordDetail(JSONObject param);

	List<JSONObject> getAllTeacherList(JSONObject param) throws Exception;

	int insertReportStyle(JSONObject param) throws Exception;

	void deleteReportStyle(JSONObject param) throws Exception;

	void updateReportPerson(JSONObject param);

	void deleteReportPerson(JSONObject param);

	List<JSONObject> getWeeklyRecordList(JSONObject param);

	void updateWeeklyRecordDetail(JSONObject param);

	List<JSONObject> getDepartmentInfoByDataBase(JSONObject param);

	int insertTerminfoAndStartWeek(JSONObject param);

	List<JSONObject> getReportingPersonList(JSONObject param);

	JSONObject getAppRecordDetail(JSONObject param);

	List<JSONObject> getBaseRecordDetail(JSONObject param);

	List<JSONObject> getDepartmentList(JSONObject param);

	List<JSONObject> getMaxWeekFromRecord(JSONObject param);

	List<JSONObject> getMaxWeekFromRecord2(JSONObject param);

	int delWeeklyRecordDetail(JSONObject param);

	JSONObject getWeeklyRecordDetailAllDepartment(JSONObject param) throws Exception;

	JSONObject getDepartmentById(JSONObject param);
	
	List<JSONObject> getNoticePerson(JSONObject param);
	
	

}
