package com.talkweb.scoreManage.dao;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.scoreManage.po.ce.ClassExamInfo;
import com.talkweb.scoreManage.po.ce.ClassExamRelative;
import com.talkweb.scoreManage.po.ce.ClassExamSubjectScore;
import com.talkweb.scoreManage.po.ce.ClassTotalScoreStatistics;
import com.talkweb.scoreManage.po.ce.CustomScore;
import com.talkweb.scoreManage.po.ce.CustomScoreInfo;
import com.talkweb.scoreManage.po.ce.StudentTotalScoreStatistics;

public interface ClassScoreCrudDao {
	List<JSONObject> getExamList(String termInfoId, Map<String, Object> map);

	boolean ifExistsSameNameInClassExamInfo(String termInfoId, Map<String, Object> map);

	ClassExamInfo getClassExamInfoById(String termInfoId, Map<String, Object> map);

	int createExam(String termInfoId, Object obj);

	int updateClassExam(String termInfoId, Map<String, Object> map);

	int deleteExam(String termInfoId, Integer autoIncr, Map<String, Object> map);

	void insertCustomScore(String termInfoId, Integer autoIncr, List<CustomScore> l_excel);

	int insertCustomScoreInfo(String termInfoId, Integer autoIncr, List<CustomScoreInfo> l_params);

	void deleteCustomScore(String termInfoId, Integer autoIncr, Map<String, Object> map);

	void deleteCustomScoreInfo(String termInfoId, Integer autoIncr, Map<String, Object> map);

	void deleteClassExamClassListTwo(String termInfoId, Integer autoIncr, Map<String, Object> map);

	void insertClassExamRelative(String termInfoId, Integer autoIncr, List<ClassExamRelative> l_eclass);

	List<JSONObject> getInfoFromClassExamSubjectScore(String termInfoId, Integer autoIncr, JSONObject params);

	int insertClassExamScoreBatch(String termInfoId, Integer autoIncr, List<ClassExamSubjectScore> scoreList, int type);

	int deleteClassExamSubjectList(String termInfoId, Integer autoIncr, Map<String, Object> map);

	int deleteClassExamClassList(String termInfoId, Integer autoIncr, Map<String, Object> map);

	int addClassExamSubjectList(String termInfoId, Integer autoIncr, Object obj);

	int addClassExamClassList(String termInfoId, Integer autoIncr, Object obj);

	List<StudentTotalScoreStatistics> selectClassExamSubjectScore(String termInfoId, Integer autoIncr,
			Map<String, Object> map);

	int addClassExamStudentTotalScore(String termInfoId, Integer autoIncr, List<StudentTotalScoreStatistics> list);

	int addClassExamClassTotalScore(String termInfoId, Integer autoIncr, List<ClassTotalScoreStatistics> list);

	List<Long> selectClassExamClassIdList(String termInfoId, Integer autoIncr, Map<String, Object> map);

	List<Long> selectClassExamSubjectIdList(String termInfoId, Integer autoIncr, Map<String, Object> map);

	List<StudentTotalScoreStatistics> selectClassExamTotalScore(String termInfoId, Integer autoIncr,
			Map<String, Object> map);

	List<ClassExamSubjectScore> selectClassExamEverSubjectScore(String termInfoId, Integer autoIncr,
			Map<String, Object> map);
}
