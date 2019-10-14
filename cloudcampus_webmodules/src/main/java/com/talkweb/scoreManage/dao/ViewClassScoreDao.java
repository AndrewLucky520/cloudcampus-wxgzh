package com.talkweb.scoreManage.dao;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.scoreManage.po.ce.ClassExamSubjectScore;
import com.talkweb.scoreManage.po.ce.CustomScore;
import com.talkweb.scoreManage.po.ce.CustomScoreInfo;
import com.talkweb.scoreManage.po.ce.StudentTotalScoreStatistics;

public interface ViewClassScoreDao {
	List<Long> getClassIdsInClassExam(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	List<Long> getClassExamSubjectIdList(String termInfoId, Integer autoIncr, Map<String, Object> map);

	List<ClassExamSubjectScore> getClassExamSubjectScoreList(String termInfoId, Integer autoIncr, Map<String, Object> map);

	List<StudentTotalScoreStatistics> getClassExamTotalScore(String termInfoId, Integer autoIncr, Map<String, Object> map);

	List<CustomScore> getAllCustomExcel(String termInfoId, Integer autoIncr, Map<String, Object> map);

	List<CustomScoreInfo> getAllCustomDetail(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	List<JSONObject> getClassExamListByTeacher(String termInfoId, Map<String, Object> map);
	
	List<Long> getClassIdFromClassExam(String termInfoId, Integer autoIncr, Map<String, Object> map);
}
