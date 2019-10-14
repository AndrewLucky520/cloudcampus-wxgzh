package com.talkweb.timetable.arrangement.service;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.Account;
import com.talkweb.accountcenter.thrift.Classroom;
import com.talkweb.accountcenter.thrift.Grade;
import com.talkweb.accountcenter.thrift.LessonInfo;
import com.talkweb.accountcenter.thrift.School;
import com.talkweb.accountcenter.thrift.User;
import com.talkweb.timetable.arrangement.algorithm.RuleConflict;
import com.talkweb.timetable.arrangement.domain.ArrangeGrid;
import com.talkweb.timetable.arrangement.domain.RuleClassGroup;
import com.talkweb.timetable.arrangement.domain.RuleCourse;
import com.talkweb.timetable.arrangement.domain.RuleGround;
import com.talkweb.timetable.arrangement.domain.RuleResearchMeeting;
import com.talkweb.timetable.arrangement.domain.RuleTeacher;
import com.talkweb.timetable.dynamicProgram.entity.ScheduleTable;
import com.talkweb.timetable.dynamicProgram.rule.SchRuleClassGroup;
import com.talkweb.timetable.dynamicProgram.rule.SchRuleCourse;
import com.talkweb.timetable.dynamicProgram.rule.SchRuleGround;
import com.talkweb.timetable.dynamicProgram.rule.SchRuleResearchMeeting;
import com.talkweb.timetable.dynamicProgram.rule.SchRuleTeacher;
import com.talkweb.timetable.dynamicProgram.rule.ScheduleRule;

public interface ArrangeDataService {

	/**
	 * 保存课表到数据库
	 * @param arrangeGrid 排课模型
	 * @return
	 */
	boolean saveArrangeTimeTable(ArrangeGrid arrangeGrid);
	
	/**
	 * 根据老师ID取老师信息
	 * @param schoolId 学校
	 * @param teacherId 老师ID
	 * @return
	 */
	User getTeacherInfo(long schoolId,String teacherId,String xnxq);
	
	/**
	 * 取一个课表的规则设置
	 * @param schoolId 学校
	 * @param timetableId 课表ID
	 * @return
	 */
	RuleConflict getRuleConflict(String schoolId, String timetableId);
	
	/**
	 * 取科目规则
	 * @param schoolId 学校
	 * @param timetableId 课表ID
	 * @return
	 */
	Map<String, RuleCourse> getRuleCourses(String schoolId, String timetableId);
	
	/**
	 * 取老师规则
	 * @param schoolId 学校
	 * @param timetableId 课表ID
	 * @return
	 */
	Map<String, RuleTeacher> getRuleTeachers(String schoolId, String timetableId);
	
	/**
	 * 取已排老师位置
	 * @param schoolId 学校
	 * @param timetableId 课表ID
	 * @return
	 */
	List<JSONObject> getRuleTeachersInHasArrage(String schoolId, String timetableId);
	
	/**
	 * 取教师的教研活动
	 * @param schoolId 学校
	 * @param timetableId 课表ID
	 * @return
	 */
	List<RuleResearchMeeting> getRuleResearchMeetings(String schoolId, String timetableId);
	
	/**
	 * 取合班课规则
	 * @param schoolId 学校
	 * @param timetableId 课表ID
	 * @return
	 */
	List<RuleClassGroup> getRuleClassGroups(String schoolId, String timetableId);
	
	//交换课程位置
	void changeLessonPosition(String schoolId, String classId, String timetableId,
			String[] fromCourseIds, Integer fromDay, Integer fromLesson,
			String[] toCourseIds, Integer toDay, Integer toLesson, String[] fromCourseTypes,
			String[] toCourseTypes, String[] fromMcgId, String[] toMcgId, 
			double unLessonCount, RuleConflict ruleConflict, List<JSONObject> arrangeList,int breakRule)  throws Exception;
	
	//删除课程位置
	int deleteLessonPosition(String schoolId, String classId, String timetableId,
			String[] courseIds, int day, int lesson, String[] mcGroupId, RuleConflict ruleConflict,int breakRule);
	
	/**
	 * 取年级信息字典
	 * @param xn 学年
	 * @param sch 学校
	 * @return
	 */
	Map<String, Grade> getGradesDic(String xn,School sch,String xq);
	
	/**
	 * 取班级信息字典
	 * @param school 学校
	 * @param cxGrade 学年学期
	 * @return
	 */
	Map<String, Classroom> getClassRoomsDic(School school, List<Grade> cxGrade,String xnxq);	
	
	/**
	 * 取教师字典
	 * @param school 学校
	 * @return
	 */
	Map<String, Account> getTeachersDic(School school,String xnxq);
	
	/**
	 * 取科目字典
	 * @param school 学校
	 * @return
	 */
	Map<String, LessonInfo> getCoursesDic(School school,String xnxq);
	
	/**
	 * 根据年级对象取年级名称
	 * @param grade 年级对象
	 * @return
	 */
	String getGradeName(Grade grade);

	/**
	 * 按教师对调课表
	 * @param schoolId
	 * @param timetableId
	 * @param fromCourses
	 * @param fromDayOfWeek
	 * @param fromLessonOfDay
	 * @param toCourses
	 * @param toDayOfWeek
	 * @param toLessonOfDay
	 * @param unLessonCount
	 * @param ruleConflict
	 * @param arrangeList
	 */
	void changeLessonPositionForTeacher(String schoolId, String timetableId,
			JSONArray fromCourses, Integer fromDayOfWeek,
			Integer fromLessonOfDay, JSONArray toCourses, Integer toDayOfWeek,
			Integer toLessonOfDay, double unLessonCount,
			RuleConflict ruleConflict, List<JSONObject> arrangeList,int breakRule);

	int deleteLessonPositionForTeacher(String schoolId, String timetableId,
			JSONArray classCourse, int dayOfWeek, int lessonOfDay,
			RuleConflict ruleConflict,int breakRule);
	
	Map<String, RuleGround> getRuleGrounds(String schoolId,
			String timetableId) ;
	
	
	/**
	 * 取一个课表的规则设置2
	 * @param schoolId 学校
	 * @param timetableId 课表ID
	 * @return
	 */
	ScheduleRule getSchRuleConflict(String schoolId, String timetableId);
	
	/**
	 * 取科目规则
	 * @param schoolId 学校
	 * @param timetableId 课表ID
	 * @return
	 */
	Map<String, SchRuleCourse> getSchRuleCourses(String schoolId, String timetableId);
	
	/**
	 * 取老师规则
	 * @param schoolId 学校
	 * @param timetableId 课表ID
	 * @return
	 */
	Map<String, SchRuleTeacher> getSchRuleTeachers(String schoolId, String timetableId);
	
	
	/**
	 * 取教师的教研活动2
	 * @param schoolId 学校
	 * @param timetableId 课表ID
	 * @return
	 */
	List<SchRuleResearchMeeting> getSchRuleResearchMeetings(String schoolId, String timetableId);
	
	/**
	 * 取合班课规则2
	 * @param schoolId 学校
	 * @param timetableId 课表ID
	 * @return
	 */
	List<SchRuleClassGroup> getSchRuleClassGroups(String schoolId, String timetableId);
	/**
	 * 取场地规则 2
	 * @param schoolId
	 * @param timetableId
	 * @return
	 */
	Map<String, SchRuleGround> getSchRuleGrounds(String schoolId,
			String timetableId) ;

	boolean saveArrangeTimeTable(ScheduleTable bestTable);
}
