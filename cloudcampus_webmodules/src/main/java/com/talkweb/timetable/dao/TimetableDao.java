package com.talkweb.timetable.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.timetable.arrangement.domain.TCRCATemporaryAdjustGroupInfo;
import com.talkweb.timetable.arrangement.domain.TCRCATemporaryAdjustGroupParam;
import com.talkweb.timetable.arrangement.domain.TCRCATemporaryAdjustResult;

public interface TimetableDao {
	/** --------- 课表上课时间设置  -----------**/
	int updateTimetableSchedule(List<JSONObject> object);
	List<HashMap<String,Object>> queryTimetableSchedule(String timetableId,String pycc);
	int insertTimetableSchedule(List<JSONObject> object);
	
	/**-----TAB页:课表基础信息-----**/		
	
	List<JSONObject> getTimetable(Map<String, String> map);

	void deleteTimetable(JSONObject object);

	int updateTimetable(JSONObject object);

	int addTimetable(Map<String, Object> map);
	
	String getTimetableXnxq(Map<String, String> object);
	
	JSONObject getTimetableSection(JSONObject object);

	int deleteGradeList(JSONObject object);
	
	int addGradeList(List<JSONObject> list);
		
    JSONObject getTotalNum(JSONObject object);	
    
	void copyTimetable(JSONObject object);
	
	void copyTimetableTwo(JSONObject object);

	int updateTimetableCheck(Map<String, Object> map);
	/**-----TAB页:课表详情信息-----**/	
		
	List<JSONObject> getTimetableList(JSONObject object);

	int insertTimetableList(List<JSONObject> list);
	
	int deleteTimetableList(JSONObject object);
	
	List<String> getAdvanceCourses(JSONObject object);
	
	List<JSONObject> getTimetablePartList(JSONObject object);
	
	/**-----TAB页:课表任务-----**/	
	List<JSONObject> getTeachingTasks(JSONObject object);

	int updateTeachingTask(JSONObject object);
	
	int addTeachingTask(JSONObject object);
	
	int deleteTeachingTask(JSONObject object);
	
	int deleteTeachingTaskById(JSONObject object);
	
	int deleteTaskTeachers(JSONObject object);
	
	int updateTaskTeachers(List<JSONObject> list);

	List<JSONObject> getTeachingTaskCourse(JSONObject object);

	List<JSONObject> getSetNumList(JSONObject object);
	
	List<JSONObject> getTeachingTaskImbody(JSONObject object);

	/**-----TAB页:科目规则-----**/	
	List<String> getRuleCourseIds(JSONObject object);
	
	List<JSONObject> getWalkthroughCourse(JSONObject object);

	List<String> getCourseRuleId(JSONObject object);
	
	int addCourseRule(JSONObject object);
	
	int deleteCourseRule(JSONObject object);
	
	int deleteCourseFixed(JSONObject object);

	int addCourseFixed(List<JSONObject> list);

	JSONObject getCourseRule(JSONObject object);
	
	/**-----TAB页:教师规则-----**/	
	List<String> getRuleTeacher(JSONObject object);

	List<JSONObject> getTeacherRule(JSONObject object);
	
	String getTeacherRuleId(JSONObject object);

	int updateTeacherRule(JSONObject object);

	int deleteTeacherRuleFixed(JSONObject object);

	int addTeacherRuleFixed(List<JSONObject> list);
	
	int deleteTeacherRule(JSONObject request);
	
	int updateTeacherGroup(JSONObject object);	

	int delTeacherGroupMembers(JSONObject object);

	int delTeacherGroupFixed(JSONObject object);

	int deleteTeacherGroup(JSONObject object);

	int addTeacherGroupMembers(List<JSONObject> list);

	int addTeacherGroupFixed(List<JSONObject> list);
	
	/**-----TAB页:合班规则-----**/	
	int addClassGroup(JSONObject object);
	
	void addFixedClassGroup(List<JSONObject> list);
	
	List<String> getClassGroup(JSONObject object);

	List<JSONObject> getFixedClassGroup(JSONObject object);

	int deleteClassGroup(JSONObject object);
	
	void updateClassGroupName(JSONObject object);

	int deleteClassFixed(JSONObject object);
	
	int addMonoWeek(JSONObject object);
	
	int updateMonoWeek(List<JSONObject> list);
	
	int updateMonoWeekClass(JSONObject classObj);
	
	int deleteMonoWeek(JSONObject object);
	
	List<JSONObject> getMonoWeekList(JSONObject object);
	
	List<JSONObject> getMonoTaskCourse(JSONObject object);
	
	/**-----TAB页:场地规则-----**/	
	List<JSONObject> getGroundRuleSum(Map<String,String> map);
	
	int insertGroundRule(JSONObject object);
	
	int updateGroundRule(JSONObject object);

	int deleteGroundRule(JSONObject object);
	
	List<JSONObject> getRoundruleCourse(JSONObject object);	
		
	/**-----TAB页:教研活动-----**/	
	List<JSONObject> getResearchWorksList(JSONObject object);

	int deleteResearchWork(JSONObject object);

	int deleteResearchFixed(JSONObject object);

	int deleteResearchTeach(JSONObject object);
	
	int updateResearchWork(JSONObject object);

	int addResearchFixed(List<JSONObject> teacherlist);

	int addResearchTeacher(List<JSONObject> fixedlist);
	
	/**-----TAB页:课表APP查询-----**/
	List<JSONObject> getTimetableByDay(JSONObject object);
	
	/**-----TAB页:其他DAO方法-----**/	
	JSONObject getArrangeTimetableInfo(Map<String, Object> map);
	
	List<JSONObject> getTaskByTimetable(Map<String, Object> map);
	
	List<JSONObject> getTaskTeacherByTaskId(Map<String, Object> map);
	 
	List<JSONObject> getArrangeGradeSetList(Map<String, Object> map);
	 
	List<JSONObject> getRuleCourseList(Map<String, Object> map);
	 
	List<JSONObject> getRuleCourseFixedByRuleId(Map<String, Object> map);
	 
	List<JSONObject> getRuleTeacherList(Map<String, Object> map);
	 
	List<JSONObject> getRuleTeacherFixedByRuleId(Map<String, Object> map);
	 
    List<JSONObject> getRuleClassGroupList(Map<String, Object> map);
	 
	List<JSONObject> getRuleClassFixedByRuleId(Map<String, Object> map);
	 
	List<JSONObject> getResearchWorkList(Map<String, Object> map);
	 
	List<JSONObject> getAdvanceArrangeList(Map<String, Object> map);
	 
	boolean saveTimetableBatch(String schoolId, String timetableId, List<Map<String, Object>> timeTableList, Set<String> gradeIds);
		
	List<JSONObject> getArrangeTimetableList(Map<String, Object> map);
	 
	int addArrangeTimetable(Map<String, Object> map);
		
	int deleteArrangeTimetable(Map<String, Object> map);
	List<JSONObject> getTeacherTimetable(HashMap<String, Object> map);
	List<JSONObject> getMaxLessonNum(HashMap<String, Object> map);
	List<JSONObject> getClassTimetable(HashMap<String, Object> map);
	List<JSONObject> getGradeSet(HashMap<String, Object> map);
	List<JSONObject> getTimetablePie(HashMap<String, Object> map);
	
	int clearTimetable(Map<String, Object> map);
	int clearArrangeTimetable(Map<String, Object> map);

	List<JSONObject> getEvenOddWeekGroupList(String schoolId, String timetableId);
	public List<JSONObject> getByTeacherTime(HashMap<String, Object> map);
	public List<JSONObject> getTimeByTeacher(HashMap<String, Object> map);
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
	 * 批量插入实体列表
	 * @param dataList
	 */
	<T> void insertMultiEntity(List<T> dataList);

	void deleteMultiTpTbRecord(List<TCRCATemporaryAdjustResult> needDeleteResult);

	List<JSONObject> getTemporaryAjustRecord(HashMap<String, Object> cxMap);

	void deleteMultiTmpRecord(List<String> needDelRecord);

	int updateAdjustRecordPublished(List<JSONObject> list);

	void delMutilTmpAjstResult(HashMap<String, Object> cxMap);

	void updateAutoArrangeResult(HashMap<String, Object> map);

	int addArrangeTimetableBatch(List<JSONObject> ni);

	List<JSONObject> getDuplicatePubGrades(HashMap<String, Object> cxMap);

	JSONObject getTimetableForAppClass(HashMap<String, String> cxMap);

	JSONObject getTimetableForAppClassList(HashMap<String, String> cxMap);

	List<JSONObject> getRuleGroundList(HashMap<String, Object> cxMap);

	int deleteArrangeTimetableHigh(List<JSONObject> needDelete);

	int updateTimetableCheckStatus(HashMap<String, Object> cxMap);

	List<JSONObject> getTeacherGroupRules(String schoolId, String timetableId) throws Exception;

	List<JSONObject> getTeacherRelatedGroups(HashMap<String, Object> cxMap) throws Exception;

	List<TCRCATemporaryAdjustGroupInfo> getTempAjustGroupList(
			HashMap<String, Object> cxMap);	
	
	TCRCATemporaryAdjustGroupInfo getSingleTempAjustGroup(
			HashMap<String, Object> cxMap);	

	List<TCRCATemporaryAdjustGroupParam> getTempAjustGroupParam(HashMap<String, Object> cxMap);
	
	List<TCRCATemporaryAdjustGroupParam> getSingleTempAjustGroupParam(HashMap<String, Object> cxMap);
	
	void updateCheckGroupExist(HashMap<String, Object> cxMap);
	
	void updateWeekAgoWeekOfEnd(HashMap<String,Object> cxMap);

	int updateAdjustResultPublished(List<JSONObject> needUpdateResultList);
	
	void deleteNoRecordAjst(HashMap<String, Object> cxMap);
	
	List<JSONObject> getTimetableByTeacherId(JSONObject param);
	
	List<JSONObject> getLatestTimetable(Map<String,Object> params);
	
	List<JSONObject> getClassTimetableList(Map<String,Object> param);
	
	JSONObject getTimetableDetailById(HashMap<String,Object> param);
	
	List<JSONObject> getTimetableSections(JSONObject object);
	
}