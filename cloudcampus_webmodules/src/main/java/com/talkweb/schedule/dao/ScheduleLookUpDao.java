package com.talkweb.schedule.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;

public interface ScheduleLookUpDao {
	
	List<JSONObject> getSchedule(Map<String, String> map);

	int updateSchedule(JSONObject object);

	int addSchedule(Map<String, Object> map);
	
	void deleteSchedule(JSONObject object);
	
	public List<JSONObject> getClassTb(Map<String,Object> param);
	
	public List<JSONObject> getClassTbTeachers(Map<String,Object> param);
	
	public List<JSONObject> getClassTbZOU(Map<String,Object> param);
	
	public List<JSONObject> getTeacherTb(Map<String,Object> param);
	
	public List<JSONObject> getStudentTb(Map<String,Object> param);
	
	public List<JSONObject> getGroundTb(Map<String,Object> param);
	
	public List<JSONObject> getGradeTbs(Map<String,Object> param);
	
	public List<JSONObject> getStudentClassTbs(Map<String,Object> param);
	
	public JSONObject getNewTimetablePrintSet(Map<String,Object> param);
	
	public JSONObject getScheduleTimetableById(Map<String,Object> param);
	
	public void updateNewTimetablePrintSet(Map<String,Object> param);
	
	public void insertNewTimetablePrintSet(Map<String, Object> param);
	
	/**
	 * SELECT DISTINCT(a.scheduleId),scheduleName,createTime FROM  t_sch_scheduleinfo b LEFT JOIN t_sch_tclass_schedulelist a 
				ON a.schoolId=b.schoolId AND a.scheduleId=b.scheduleId AND b.termInfoId=#{termInfoId} AND b.schoolYear=#{schoolYear}
			WHERE a.tclassId=#{tclassId}
	 * @param object
	 * @return
	 */
	public List<JSONObject> queryStuScheduleList(JSONObject object);
	
	/**
	 * SELECT DISTINCT a.type,b.subjectId,b.tclassId,b.groundId,c.groundName
			FROM (t_sch_tclass a LEFT JOIN t_sch_task b 
				ON a.schoolId=b.schoolId AND a.scheduleId=b.scheduleId AND a.gradeId=b.gradeId AND a.tclassId=b.tclassId),t_sch_groundinfo c
			WHERE b.schoolId = c.schoolId AND b.scheduleId = c.scheduleId AND b.groundId=c.groundId 
				AND a.schoolId=#{schoolId} AND a.scheduleId=#{scheduleId} AND a.schoolYear=#{schoolYear} AND a.termInfoId=#{termInfoId} 
				<if test="subjectIdTclassIdMapList!=null">
					AND CONCAT(b.subjectId,b.tclassId) in
					<foreach collection="subjectIdTclassIdMapList" item="item" open="(" close=")" separator=",">
						'${item}'
					</foreach>
				</if>
	 * @param obj
	 * @return
	 */
	public List<JSONObject> queryStudentSelfExt1Timetable(JSONObject object);
	
	/**
	 * SELECT a.subjectId,a.tclassId, dayOfWeek,lessonOfDay,courseType,CONCAT(a.subjectId,a.tclassId) as identity
			FROM  ${db_name_schedule}.t_sch_student_tclass_relationship a LEFT JOIN ${db_name_schedule}.t_sch_tclass_schedulelist b
				ON a.schoolId=b.schoolId AND a.scheduleId=b.scheduleId AND a.tclassId=b.tclassId
			WHERE a.schoolId=#{schoolId} AND a.scheduleId=#{scheduleId} AND b.studentId=#{studentId}
				 AND a.schoolYear=#{schoolYear} AND a.termInfoId=#{termInfoId}
	 * @param obj
	 * @return
	 */
	public List<JSONObject> queryStudentSelfTimetable(JSONObject obj);

	List<JSONObject> getClassTbZOU1(HashMap<String, Object> param);
	
	//新高考成绩模块
	List<JSONObject> queryStuInTclass(Map<String, Object> param);
	
	//新高考教学考评模块
	List<JSONObject> evalOfSubInSchedule(Map<String, Object> param);

	//教师课时统计
	List<JSONObject> getTeacherLessonsStatistics(JSONObject params);

	List<JSONObject> getStudentTbForApp(Map<String, Object> param);
}
