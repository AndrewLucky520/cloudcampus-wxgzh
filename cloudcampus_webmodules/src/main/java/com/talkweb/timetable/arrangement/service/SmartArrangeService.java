package com.talkweb.timetable.arrangement.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.Account;
import com.talkweb.accountcenter.thrift.Classroom;
import com.talkweb.accountcenter.thrift.Grade;
import com.talkweb.accountcenter.thrift.LessonInfo;
import com.talkweb.accountcenter.thrift.School;
import com.talkweb.timetable.arrangement.algorithm.RuleConflict;
import com.talkweb.timetable.arrangement.exception.ArrangeTimetableException;


/**
 * 智能排课核心业务逻辑
 * 
 * @author Li xi yuan
 *
 */
public interface SmartArrangeService {

	
	/**
	 * 开始自动排课算法
	 * 
	 * @param session 当前请求的session对象
	 * @param school 学校ID
	 * @param timetableId 课表ID
	 * @param gradeIds 年级ID
	 * @param progressMap 
	 * @param runParams 
	 * @throws ArrangeTimetableException
	 */
	void autoArrangeTimeTable(HttpSession session, School school, String timetableId, List<String> gradeIds, JSONObject runParams) throws ArrangeTimetableException;
	
	/**
	 * 更新排课进度
	 * 
	 * @param session 当前请求的session对象
	 * @param code  0 开始 1 正在编排 2 成功结束 -1 失败结束
	 * @param progress 进度值，即百分比的数值
	 * @param msg 当前进度的名称/处理消息
	 */
	void updateArrangeProgress(String jsessionID,  int code, double progress, String msg);

	
	/**
	 * 根据班级取微调数据
	 * 
	 * @param school 学校
	 * @param timetableId 课表ID
	 * @param gradeId 年级ID
	 * @param classId 班级ID
	 * @param breakRule 
	 * @param progDataMap 
	 * @return
	 */
	JSONObject getForFineTuningDataByClass(School school, String timetableId, String gradeId, String classId,  String sessionID, int breakRule);
	
	
	/**
	 * 根据班级取临时调课数据
	 * 
	 * @param school 学校
	 * @param timetableId 课表ID
	 * @param gradeId 年级ID
	 * @param classId 班级ID
	 * @param progDataMap 
	 * @param weekOfEnd 
	 * @param week 
	 * @param breakRule 
	 * @return
	 */
	JSONObject getTempAjustForFineTuningDataByClass(School school, String timetableId, String gradeId, String classId, String sessionID, String week, int weekOfEnd, int breakRule);
	
	
	public JSONObject loadDataByMapTask(  String sessionID,
			String schoolId, String timetableId, String termInfo,
			School school);
	
	public JSONObject loadDataByMap(String sessionID, String termInfo, School school) ;

	JSONObject getTempAjustForFineTuningDataByGrade(School school,
			String timetableId, String gradeId, String id, String week,
			int weekOfEnd);
	/**
	 * 根据教师取临时调课数据
	 * @param school
	 * @param timetableId
	 * @param teacherId
	 * @param progDataMap
	 * @param sessionID
	 * @param week
	 * @param weekOfEnd
	 * @return
	 */
	JSONObject getTempAjustForFineTuningDataByTeacher(School school, String timetableId, String teacherId ,String sessionID, String week, int weekOfEnd,int breakRule);
	
	/**
	 * 根据年级取微调数据
	 * 
	 * @param school 学校
	 * @param timetableId 课表ID
	 * @param gradeId 年级ID
	 * @return
	 */
	JSONObject getForFineTuningDataByGrade(School school, String timetableId, String gradeId, String sessionID,int breakRule);
	
	/**
	 * 根据课表的一个位置取相关的排课冲突数据
	 * @param school 学校
	 * @param timetableId 课表ID
	 * @param gradeId 年级ID
	 * @param classId 班级ID
	 * @param courseIds 课目ID
	 * @param dayOfWeek 周期
	 * @param lessonOfDay 节次
	 * @param sessionKey 
	 * @param progDataMap 
	 * @return
	 */
	List<JSONObject> getPositionConflicts(School school, String timetableId,String gradeId, String classId, String[] courseIds,int[] courseTypes, int dayOfWeek, int lessonOfDay,  String sessionKey,String[] mcGroupIds,int breakRule);

	/**
	 * 根据老师取课表信息
	 * @param school 学校
	 * @param timetableId 课表ID
	 * @param teacherId 老师ID
	 * @return
	 */
	JSONObject getTimetableByTeacher(School school, String timetableId, String teacherId,List<JSONObject> arrangeList,List<JSONObject> taskList,
			Map<String, Account> teacherDic,JSONObject timetableInfo,Map<String, Classroom> classroomsDic
			,Map<String, LessonInfo> coursesDic, Map<String, Grade> gradeDic,HashMap<String, HashMap<String, HashMap<String, String>>> taskMap);

	/**
	 * 临时调课--对调课程
	 * @param school
	 * @param timetableId
	 * @param classId
	 * @param fromDayOfWeek
	 * @param fromLessonOfDay
	 * @param fromCourseIds
	 * @param fromCourseTypes 
	 * @param fromMcGroupId 
	 * @param toDayOfWeek
	 * @param toLessonOfDay
	 * @param toCourseIds
	 * @param toCourseTypes 
	 * @param toMcGroupId 
	 * @param progDataMap
	 * @param id
	 * @param week
	 * @param weekOfEnd
	 * @param groupId 
	 * @return
	 */
	JSONObject updateTemporaryLessonPosition(School school, String timetableId,
			String classId, Integer fromDayOfWeek, Integer fromLessonOfDay,
			String fromCourseIds, String fromCourseTypes, String fromMcGroupId, Integer toDayOfWeek, Integer toLessonOfDay,
			String toCourseIds, String toCourseTypes, String toMcGroupId,
			String id, int week, int weekOfEnd, String groupId);
	/**
	 * 临时调课--按教师对调课程
	 * @param school
	 * @param timetableId
	 * @param classId
	 * @param fromDayOfWeek
	 * @param fromLessonOfDay
	 * @param fromCourseIds
	 * @param fromCourseTypes 
	 * @param toDayOfWeek
	 * @param toLessonOfDay
	 * @param toCourseIds
	 * @param toCourseTypes 
	 * @param progDataMap
	 * @param id
	 * @param week
	 * @param weekOfEnd
	 * @param groupId 
	 * @return
	 */
	JSONObject updateTemporaryLessonPositionByTeacher(School school, String timetableId,
			  Integer fromDayOfWeek, Integer fromLessonOfDay,
			JSONArray  fromCourseArr, Integer toDayOfWeek, Integer toLessonOfDay,
			JSONArray  toCourseArr,  
			String id, int week, int weekOfEnd, String groupId);

	/**
	 * 获取可代课教师
	 * @param school
	 * @param timetableId
	 * @param classCourse
	 * @param progDataMap
	 * @param id
	 * @param week
	 * @param weekOfEnd
	 * @param dayOfWeek
	 * @param lessonOfDay
	 * @param breakRule 
	 * @return
	 */
	JSONObject getCanAdjustTeacherList(School school, String timetableId,
			JSONArray classCourse,  
			String id, int week, int weekOfEnd, int dayOfWeek, int lessonOfDay, int breakRule);

	/**
	 * 设置代课教师
	 * @param school
	 * @param timetableId
	 * @param classCourse
	 * @param progDataMap
	 * @param id
	 * @param week
	 * @param weekOfEnd
	 * @param dayOfWeek
	 * @param lessonOfDay
	 * @param toTeachers 
	 * @param fromTeachers 
	 * @param groupId 
	 * @return
	 */
	JSONObject updateAdjustBySet(School school, String timetableId,
			JSONArray classCourse, 
			String id, int week, int weekOfEnd, int dayOfWeek, int lessonOfDay, String fromTeachers,
			String toTeachers,String aType,String setCourseId, String groupId);

	/**
	 * 撤销操作记录 直至
	 * @param school
	 * @param timetableId
	 * @param classCourse
	 * @param progDataMap
	 * @param id
	 * @param week
	 * @param weekOfEnd
	 * @param dayOfWeek
	 * @param lessonOfDay
	 * @param groupId 
	 * @return
	 */
	JSONObject updateAdjustByDelAdjust(School school, String timetableId,
			JSONArray classCourse,  String id, int week, int weekOfEnd, int dayOfWeek, int lessonOfDay,String selfStuCode, String groupId);

	JSONObject getTempAjustPositionConflicts(School school, String timetableId,
			int dayOfWeek, int lessonOfDay, String gradeId, String classId,
			String courseIds, String id, String week, int weekOfEnd,String McGroupIds,int breakRule);

	JSONObject getAdjustRecordForClassList(School school, String timetableId,  String sessionID, String week,int astType);

	JSONObject updateAdjustRecordPublished(School school, String timetableId,
			 String week, JSONArray courses,HttpSession session);

	JSONObject getForFineTuningDataByTeacher(School school, String timetableId,
			String teacherId,  String id,int breakRule);

	JSONObject getIntelligentCourseInfo(String schoolId, String timetableId,
			List<String> gradeIds, School school,
			String sessionID, String xn);

	List<JSONObject> getClssCurrentTimeTable(JSONObject timetable,
			String classId, String xnxq, School sch);

	List<JSONObject> getTeaCurrentTimeTable(JSONObject timetable,
			String userId, String xnxq, School sch);
	
	void fillTeachersToArrangeList(RuleConflict ruleConflict,
			List<JSONObject> arrangeList,
			HashMap<String, HashMap<String, HashMap<String, String>>> taskMap);
}
