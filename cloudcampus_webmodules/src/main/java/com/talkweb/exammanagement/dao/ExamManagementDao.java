package com.talkweb.exammanagement.dao;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.exammanagement.domain.ArrangeExamClassInfo;
import com.talkweb.exammanagement.domain.ArrangeExamPlaceInfo;
import com.talkweb.exammanagement.domain.ArrangeExamRule;
import com.talkweb.exammanagement.domain.ArrangeExamSubjectInfo;
import com.talkweb.exammanagement.domain.ExamManagement;
import com.talkweb.exammanagement.domain.ExamPlan;
import com.talkweb.exammanagement.domain.ExamSubject;
import com.talkweb.exammanagement.domain.StudNotTakingExam;
import com.talkweb.exammanagement.domain.TestNumberInfo;

public interface ExamManagementDao {
	/********************************************主页*************************************************/
	List<ExamManagement> getExamManagementList(Map<String, Object> map, String termInfo);
	
	ExamManagement getExamManagementListById(Map<String, Object> map, String termInfo);
	
	boolean ifExsitsSameNameInExamManagement(Object em, String termInfo);
	
	Integer insertExamManagement(Object em, String termInfo);
	
	Integer updateExamManagement(Object em, String termInfo);
	
	Integer updateExamManagementStatus(Object em, String termInfo);
	
	Integer deleteExamManagement(Map<String, Object> map, String termInfo);
	
	
	/*********************************************考试计划************************************************/
	List<ExamPlan> getExamPlanList(Map<String, Object> map, String termInfo, Integer autoIncr);
	
	List<ExamSubject> getExamSubjectList(Map<String, Object> map, String termInfo, Integer autoIncr);
	
	Integer deleteExamPlan(Map<String, Object> map, String termInfo, Integer autoIncr);
	
	Integer deleteExamSubject(Map<String, Object> map, String termInfo, Integer autoIncr);
	
	Integer insertExamPlan(Object obj, String termInfo, Integer autoIncr);
	
	Integer updateExamPlan(Object obj, String termInfo, Integer autoIncr);
	
	Integer insertExamSubjectBatch(Object obj, String termInfo, Integer autoIncr);
	
	/*********************************************安排考务************************************************/
	List<JSONObject> getExamPlaceInfoInArrange(Map<String, Object> map, String termInfo, Integer autoIncr);
	
	List<ArrangeExamRule> getArrangeExamRule(Map<String, Object> map, String termInfo, Integer autoIncr);
	
	List<ArrangeExamClassInfo> getArrangeExamClassInfo(Map<String, Object> map, String termInfo, Integer autoIncr);
	
	List<ArrangeExamSubjectInfo> getArrangeExamSubjectInfo(Map<String, Object> map, String termInfo, Integer autoIncr);
	
	List<JSONObject> getExamSubjectGroupList(Map<String, Object> map, String termInfo, Integer autoIncr);

	List<ArrangeExamPlaceInfo> getArrangeExamPlaceInfo(Map<String, Object> map, String termInfo, Integer autoIncr);
	
	int deleteArrangeExamRule(Map<String, Object> map, String termInfo, Integer autoIncr);
	
	int deleteArrangeExamClassInfo(Map<String, Object> map, String termInfo, Integer autoIncr);
	
	int deleteArrangeExamSubjectInfo(Map<String, Object> map, String termInfo, Integer autoIncr);
	
	int deleteArrangeExamPlaceInfo(Map<String, Object> map, String termInfo, Integer autoIncr);
	
	int insertArrExamRule(Object obj, String termInfo, Integer autoIncr);
	
	int insertArrExamClassInfoBatch(Object obj, String termInfo, Integer autoIncr);
	
	int insertArrExamSubjInfoBatch(Object obj, String termInfo, Integer autoIncr);
	
	int insertArrExamPlaceInfoBatch(Object obj, String termInfo, Integer autoIncr);
	
	int updateRemainNumOfStuds(Object obj, String termInfo, Integer autoIncr);
	
	int insertArrExamResultBatch(List<?> list, String termInfo, Integer autoIncr);
	
	int insertTestNumberInfoBatch(List<?> list, String termInfo, Integer autoIncr);
	
	int insertStudsWaitingBatch(List<?> list, String termInfo, Integer autoIncr);
	
	List<TestNumberInfo> getTestNumberInfo(Map<String, Object> map, String termInfo, Integer autoIncr);
	
	int deleteTestNumberInfo(Map<String, Object> map, String termInfo, Integer autoIncr);
	
	int deleteArrExamResult(Map<String, Object> map, String termInfo, Integer autoIncr);
	
	List<StudNotTakingExam> getStudsNotTakingExam(Map<String, Object> map, String termInfo, Integer autoIncr);
	
	
	int deleteExamPlaceInfo(Map<String, Object> map, String termInfo, Integer autoIncr);
	
	int deleteStudsNotTakingExam(Map<String, Object> map, String termInfo, Integer autoIncr);
	
	int deleteStudsWaiting(Map<String, Object> map, String termInfo, Integer autoIncr);
	
	/*********************************************产生考号*************************************************/
	String produceTestNumber(ExamPlan ep, Long serialNumber, String xn);
	
	/*********************************************考试成绩代码************************************************/
	List<JSONObject> getScoreList(Map<String, Object> map);
	
	List<JSONObject> getScoreInfo(Map<String, Object> map);

	List<JSONObject> getCommPlaceList(Object obj, String termInfo,
			Integer autoIncr);

	List<JSONObject> getArrangeExamSubjectAndsubject(JSONObject request,
			String termInfo, Integer autoIncr);
	
	JSONObject getDegreeinfo(Map<String, Object> map);
	
}
