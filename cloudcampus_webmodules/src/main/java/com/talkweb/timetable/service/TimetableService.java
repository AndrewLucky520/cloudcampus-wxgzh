package com.talkweb.timetable.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.Classroom;
import com.talkweb.accountcenter.thrift.Grade;
import com.talkweb.accountcenter.thrift.School;
import com.talkweb.timetable.arrangement.domain.TCRCATemporaryAdjustGroupInfo;
import com.talkweb.timetable.arrangement.domain.TCRCATemporaryAdjustGroupParam;
import com.talkweb.timetable.bo.TimetableGradeSetBO;
import com.talkweb.timetable.domain.CourseSetInfor;
import com.talkweb.timetable.domain.ImportTaskParameter;

public interface TimetableService {
	
	List<JSONObject> getLatestPublishedTimetable(JSONObject object);
	
	/**
	 * 课表上课时间设置、查询和更新
	 */
	int addTimetableSchedule(List<JSONObject> object);
	int updataTimetableSchedule(List<JSONObject> object);
	List<HashMap<String,Object>> getTimetableSchedule(String timetableId,String pycc);
	
	/**-----TAB页:课表视图-----**/	
	List<JSONObject> getTimetable(Map<String, String> map);

	void deleteTimetable(JSONObject object);

	int updateTimetable(JSONObject object);

	int addTimetable(Map<String, Object> map);
	
	/**-----TAB页:公共-----**/
	String getTimetableXnxq(String timetableId,String schoolId);
	
	String convertSYNJByGrade(long schoolId,Grade grade,String termInfo);
	
	/**-----TAB页:复制-----**/		
	List<JSONObject> getLastTimetableList(JSONObject object);

	void copyTimetable(JSONObject object);
	
	int addTimetableList(List<JSONObject> timetableList);
	
	void setImportTeachingTasks(List<JSONObject> taskList, School school,
			String termInfo);
	
	void setImportMonoWeeks(List<JSONObject> monoList, String schoolId, 
			String timetableId);
	
	/**-----TAB页:设置课表-----**/
	JSONObject getTimetableSection(JSONObject object);

	String updateTimetableSection(JSONObject object);
	
	/**-----TAB页:设置教学任务-----**/		
		
	int updateTeachingTasks(JSONObject object);

	int updateTeacherAndTask(JSONObject object);
	
	void deleteTeachingTask(JSONObject request);

	JSONObject getTeachingTask(JSONObject object);
	
	List<JSONObject> getTeacherByCondition(JSONObject object);
	
	int addImportTeachers(School sch, List<JSONObject> list,
			ImportTaskParameter stt);
	
	void writebackTeacherList(List<JSONObject> targetList, School school,
			String termInfo);

	List<Long> getTeacherIds(JSONObject object);
	
	/**-----TAB页:设置排课规则-----**/		
	int updateCourseRule(JSONObject object);

	JSONObject getCourseRule(JSONObject object);
	
	JSONObject getRuleTeacherList(JSONObject object);
	
	void deleteCourseRule(JSONObject object);

	JSONObject getTeacherRule(JSONObject object);
	
	int updateTeacherRule(JSONObject object);
	
	void deleteTeacherRule(JSONObject object);

	String addTeacherGroup(JSONObject object);

	int updateTeacherGroup(JSONObject object);

	void deleteTeacherGroup(JSONObject object);

	int addClassGroup(JSONObject object);

	List<JSONObject> getClassGroup(JSONObject object);

	int deleteClass(JSONObject object);

	int deleteClassGroup(JSONObject object);
	
	JSONArray getTeachingTaskCourse(JSONObject object);
	
	int addMonoWeek(JSONObject object);
	
	JSONObject getMonoWeek(JSONObject object);

	void deleteMonoWeek(JSONObject object);
	
	JSONArray getGroundRuleList(JSONObject object);

	int insertGroundRule(JSONObject object);
	
	int updateGroundRule(JSONObject object);

	void deleteGroundRule(JSONObject object);

	JSONObject getSubjectAndGradeList(JSONObject object);
	
	/**-----TAB页:预排课-----**/		
	List<JSONObject> getWalkthroughTaskCourse(JSONObject object);
	
	int updateCourseWalkthrough(JSONObject object);

	JSONObject getCourseWalkthrough(JSONObject object);

	List<JSONObject> getScheduledCourseList(JSONObject object);

	int deleteCourseWalkthrough(JSONObject object);
	
	int addActivityWalkthroughGroup(JSONObject object);

	JSONObject getActivityWalkthrough(JSONObject object);

	int updateActivityWalkthrough(JSONObject object);
	
	int updateActivityWalkthroughMember(JSONObject object);

	JSONObject getScheduledActivity(JSONObject object);
	
	void deleteActivityWalkthrough(JSONObject object);

	void deleteActivityWalkthroughGroup(JSONObject object);
	
	int updateActivityWalkthroughGroup(JSONObject object);
	
	/**-----TAB页:课表APP查询-----**/
	JSONObject getClassTimetableByDay(JSONObject object);
	
	JSONObject getTeacherTimetableByDay(JSONObject object);
	
	List<JSONObject> getAppClassList(HashMap<String,Object> map);
    /**---------------------------**/
	
	JSONObject getTimetableSections(String schoolId, String timetableId, List<String> gradeIds,School sch);
	
	JSONObject getArrangeTimetableInfo(String schoolId, String timetableId);
	
	JSONObject getGradeSet(String schoolId, String timetableId, String gradeId);
	
	List<JSONObject> getGradeSetList(String schoolId, String timetableId, List<String> gradeIds);
	
	List<Classroom> getClassByGradeIds(String schoolId, String termInfoId, List<String> gradeIds);
	
	List<JSONObject> getTaskByTimetable(String schoolId, String timetableId, List<String> classIds);
	
	List<JSONObject> getTaskByTimetableClassId(String schoolId, String timetableId, String classId);
	
	List<JSONObject> getTaskByTimetable(String schoolId, String timetableId);

	List<JSONObject> getRuleCourseList(String schoolId, String timetableId);
	
	List<JSONObject> getRuleCourseFixedByRuleId(String schoolId, String courseRuleId);
	
	List<JSONObject> getRuleTeacherList(String schoolId, String timetableId);
	
	List<JSONObject> getRuleTeacherFixedByRuleId(String schoolId,String teacherRuleId);
	
	List<JSONObject> getRuleClassGroupList(String schoolId, String timetableId);
	
	List<JSONObject> getRuleClassFixedByRuleId(String schoolId, String classRuleId);
	
	List<JSONObject> getResearchWorkList(String schoolId, String timetableId);
	
	List<JSONObject> getAdvanceArrangeList(String schoolId, String timetableId);
	
	boolean saveTimetableBatch(String schoolId, String timetableId, List<Map<String, Object>> timeTableList, Set<String> gradeIds);
	
	List<JSONObject> getArrangeTimetableList(String schoolId, String timetableId,String classId);
	
	int addArrangeTimetable(String schoolId, String timetableId, String classId, String courseId, int day, int lesson, int courseType, String mcGroupId);
	
	int deleteArrangeTimetable(String schoolId, String timetableId, String classId, String[] courseIds, int day, int lesson, String mcGroupId);
	
	List<JSONObject> getTeacherTimetable(HashMap<String, Object> map);
	
	List<JSONObject> getMaxLessonNum(HashMap<String, Object> map);
	
	List<JSONObject> getClassTimetable(HashMap<String, Object> map);
	List<JSONObject> getTimetablePie(HashMap<String, Object> map);
	
	List<JSONObject> getGradeSet(HashMap<String, Object> map);
	
	int clearTimetable(String schoolId, String timetableId, String subsql, String xn, String xq);
	
	JSONObject  getEvenOddWeekGroupList(String schoolId, String timetableId);
	List<JSONObject> getTimeByTeacher(HashMap<String, Object> map);
	List<JSONObject> getByTeacherTime(HashMap<String, Object> map);
	List<JSONObject> getTeacherResearch(HashMap<String, Object> map);

	List<JSONObject> getRuleTeachersInHasArrage(String schoolId,
			String timetableId);
			
	int updateTimetablePrintSet(JSONObject object);
	
	JSONObject getTimetablePrintSet(JSONObject object);		

	List<JSONObject> getTimetableGradeList(HashMap<String, Object> reqMap);

	List<JSONObject> getTimetableSubjectList(HashMap<String, Object> reqMap);
	
	List<JSONObject> getTimetableTeacherList(HashMap<String, Object> reqMap);
	
	List<String> getTaskClassList(JSONObject object);

	JSONObject getTimetableForWeekList(HashMap<String, Object> cxMap);

	List<JSONObject> getTemporaryAjustList(HashMap<String, Object> cxMap);
	/**
	 * 批量插入实体
	 * @param dataList
	 */
	<T> void insertMultiEntity(List<T> dataList);

	List<JSONObject> getTemporaryAjustRecord(HashMap<String, Object> cxMap);

	void updateAutoArrangeResult(String timetableId, String schoolId,
			String rsMsg);

	int updateArrangeTimetable(JSONObject needUpdate);
	
	int insertArrangeTimetableBatch(List<JSONObject> needInsert);
	
	int deleteArrangeTimetable(List<JSONObject> needDel);

	List<JSONObject> getDuplicatePubGrades(HashMap<String, Object> cxMap);

	JSONObject getTimetableForAppClass(long schoolId, String xnxq, String classId);

	JSONObject getTimetableForAppClassList(long id, String xnxq,
			String teacherId, boolean needClass , String classId);

	List<JSONObject> getRuleGroundList(String schoolId, String timetableId);


	int deleteArrangeTimetableHigh(List<JSONObject> needDelete);

	int updateTimetableCheckStatus(HashMap<String, Object> cxMap);

	Map<String, List<JSONObject>> getRuleTeacherGroups(String schoolId,
			String timetableId) throws Exception;

	HashMap<String,TCRCATemporaryAdjustGroupInfo> getTempAjustGroupList(
			HashMap<String, Object> cxMap);

	HashMap<String,HashMap<Integer,TCRCATemporaryAdjustGroupParam>> getTempAjustGroupParamMap(HashMap<String, Object> cxMap);

	TCRCATemporaryAdjustGroupInfo getSingleTempAjustGroup(
			HashMap<String, Object> cxMap);	

	
	List<TCRCATemporaryAdjustGroupParam> getSingleTempAjustGroupParam(HashMap<String, Object> cxMap);
	
	double checkTotalCourse(JSONObject object);
	HashMap<String, CourseSetInfor> checkSingleCourse(JSONObject object);
	
	List<JSONObject> getTimetableByTeacherId(JSONObject param);
	
	
	TimetableGradeSetBO getTimetableGradeSetById(String timetableId);
	
	/** 微信模板消息	 */
	public void sendWxTemplateMsg(JSONObject params);
	/** timetable详情	 */
	JSONObject getTimetableDetailById(HashMap<String, Object> param);
	
}