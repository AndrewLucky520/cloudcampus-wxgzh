package com.talkweb.timetable.arrangement.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.Account;
import com.talkweb.accountcenter.thrift.AccountStructConstants;
import com.talkweb.accountcenter.thrift.Classroom;
import com.talkweb.accountcenter.thrift.Grade;
import com.talkweb.accountcenter.thrift.LessonInfo;
import com.talkweb.accountcenter.thrift.School;
import com.talkweb.accountcenter.thrift.User;
import com.talkweb.common.tools.BeanTool;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.timetable.arrangement.algorithm.CourseGene;
import com.talkweb.timetable.arrangement.algorithm.RuleConflict;
import com.talkweb.timetable.arrangement.domain.ArrangeClass;
import com.talkweb.timetable.arrangement.domain.ArrangeGrid;
import com.talkweb.timetable.arrangement.domain.RuleClassGroup;
import com.talkweb.timetable.arrangement.domain.RuleCourse;
import com.talkweb.timetable.arrangement.domain.RuleGround;
import com.talkweb.timetable.arrangement.domain.RulePosition;
import com.talkweb.timetable.arrangement.domain.RuleResearchMeeting;
import com.talkweb.timetable.arrangement.domain.RuleTeacher;
import com.talkweb.timetable.arrangement.service.ArrangeDataService;
import com.talkweb.timetable.dynamicProgram.entity.GridPoint;
import com.talkweb.timetable.dynamicProgram.entity.ScheduleTable;
import com.talkweb.timetable.dynamicProgram.rule.SchRuleClassGroup;
import com.talkweb.timetable.dynamicProgram.rule.SchRuleCourse;
import com.talkweb.timetable.dynamicProgram.rule.SchRuleGround;
import com.talkweb.timetable.dynamicProgram.rule.SchRulePosition;
import com.talkweb.timetable.dynamicProgram.rule.SchRuleResearchMeeting;
import com.talkweb.timetable.dynamicProgram.rule.SchRuleTeacher;
import com.talkweb.timetable.dynamicProgram.rule.ScheduleRule;
import com.talkweb.timetable.service.TimetableService;

@Service
public class ArrangeDataServiceImpl implements ArrangeDataService {

	@Autowired
	private TimetableService timetableService;
	@Autowired
	private AllCommonDataService commonService;

	/**
	 * 根据老师ID取老师信息
	 * 
	 * @param schoolId
	 *            学校
	 * @param teacherId
	 *            老师ID
	 * @return
	 */
	public User getTeacherInfo(long schoolId, String teacherId,String xnxq) {
		return this.commonService.getUserById(schoolId,
				Long.parseLong(teacherId), xnxq);
	}

	/**
	 * 取一个课表的规则设置
	 * 
	 * @param schoolId
	 *            学校
	 * @param timetableId
	 *            课表ID
	 * @return
	 */
	public RuleConflict getRuleConflict(String schoolId, String timetableId) {
		// 冲突规则
		RuleConflict ruleConflict = new RuleConflict();

		// 科目规则
		Map<String, RuleCourse> ruleCourses = this.getRuleCourses(schoolId,
				timetableId);

		// 教师规则
		Map<String, RuleTeacher> ruleTeachers = this.getRuleTeachers(schoolId,
				timetableId);

		// 合班
		List<RuleClassGroup> ruleClassGroups = this.getRuleClassGroups(
				schoolId, timetableId);

		// 教师的教研活动
		List<RuleResearchMeeting> ruleResearchMeetings = this
				.getRuleResearchMeetings(schoolId, timetableId);

		Map<String, RuleGround> ruleGrounds = this.getRuleGrounds(schoolId,
				timetableId);
		ruleConflict.setRuleCourses(ruleCourses);
		ruleConflict.setRuleTeachers(ruleTeachers);
		ruleConflict.setRuleClassGroups(ruleClassGroups);
		ruleConflict.setRuleResearchMeetings(ruleResearchMeetings);
		ruleConflict.setRuleGrounds(ruleGrounds);

		return ruleConflict;

	}

	public Map<String, RuleGround> getRuleGrounds(String schoolId,
			String timetableId) {
		// TODO Auto-generated method stub
		Map<String, RuleGround> rsMap = new HashMap<String, RuleGround>();
		List<JSONObject> ruleGroundList = this.timetableService
				.getRuleGroundList(schoolId, timetableId);

		for (JSONObject obj : ruleGroundList) {
			RuleGround rg = new RuleGround();
			String grades = obj.getString("UsedGradeIds");
			int num = obj.getIntValue("MaxClassNum");
			String subId = obj.getString("SubjectId");
			rg.setMaxClassNum(num);
			rg.setSubjectId(subId);
			rg.setUsedGradeIds(grades);

			if (grades.indexOf(",") != -1) {
				String[] gds = grades.split(",");
				for (String gs : gds) {
					rsMap.put(gs + "_" + subId, rg);
				}
			} else {
				rsMap.put(grades.trim() + "_" + subId, rg);
			}
		}
		return rsMap;
	}

	/**
	 * 取科目规则
	 * 
	 * @param schoolId
	 *            学校
	 * @param timetableId
	 *            课表ID
	 * @return
	 */
	public Map<String, RuleCourse> getRuleCourses(String schoolId,
			String timetableId) {
		// 科目规则
		Map<String, RuleCourse> ruleCourses = new HashMap<String, RuleCourse>();
		List<JSONObject> ruleCourseList = this.timetableService
				.getRuleCourseList(schoolId, timetableId);
		for (JSONObject rule : ruleCourseList) {
			String courseRuleId = rule.getString("CourseRuleId");
			String courseId = rule.getString("CourseId");
			int maxClassNum = rule.getIntValue("MaxClassNum");
			int maxAMNum = rule.getIntValue("AMMaxNum");
			int maxPMNum = rule.getIntValue("PMMaxNum");
			int courseLevel = rule.getIntValue("CourseLevel");
			String gradeId = rule.getString("GradeId");

			RuleCourse ruleCourse = new RuleCourse();
			ruleCourse.setCourseId(courseId);
			ruleCourse.setMaxClassNum(maxClassNum);
			ruleCourse.setMaxAMNum(maxAMNum);
			ruleCourse.setMaxPMNum(maxPMNum);
			ruleCourse.setCourseLevel(courseLevel);
			ruleCourse.setGradeId(gradeId);
			List<JSONObject> ruleCourseFixedList = this.timetableService
					.getRuleCourseFixedByRuleId(schoolId, courseRuleId);
			for (JSONObject ruleCourseFixed : ruleCourseFixedList) {
				int ruleType = ruleCourseFixed.getIntValue("RuleType");
				int day = ruleCourseFixed.getIntValue("DayOfWeek");
				int lesson = ruleCourseFixed.getIntValue("LessonOfDay");

				RulePosition rulePosition = new RulePosition();
				// 0：不排位置，1：必排位置
				rulePosition.setRuleType(ruleType);
				rulePosition.setDay(day);
				rulePosition.setLesson(lesson);

				ruleCourse.addPosition(rulePosition);

			}

			ruleCourses.put(
					ruleCourse.getGradeId() + "_" + ruleCourse.getCourseId(),
					ruleCourse);
		}

		return ruleCourses;

	}

	/**
	 * 取老师规则
	 * 
	 * @param schoolId
	 *            学校
	 * @param timetableId
	 *            课表ID
	 * @return
	 */
	public Map<String, RuleTeacher> getRuleTeachers(String schoolId,
			String timetableId) {
		//查询获取教师组规则
		Map<String,List<JSONObject>> ruleTeacherposs = new HashMap<String, List<JSONObject>>();
		try {
			ruleTeacherposs = this.timetableService.getRuleTeacherGroups(schoolId,timetableId);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// 教师规则
		Map<String, RuleTeacher> ruleTeachers = new HashMap<String, RuleTeacher>();
		List<JSONObject> ruleTeacherList = this.timetableService
				.getRuleTeacherList(schoolId, timetableId);
		for (JSONObject rule : ruleTeacherList) {
			String teacherRuleId = rule.getString("TeacherRuleId");
			String teacherId = rule.getString("TeacherId");
			int maxPerDay = rule.getIntValue("MaxPerDay");

			RuleTeacher ruleTeacher = new RuleTeacher();
			ruleTeacher.setTeacherId(teacherId);
			ruleTeacher.setMaxPerDay(maxPerDay);

			List<JSONObject> ruleTeacherFixedList = this.timetableService
					.getRuleTeacherFixedByRuleId(schoolId, teacherRuleId);
			for (JSONObject ruleTeacherFixed : ruleTeacherFixedList) {
				int ruleType = ruleTeacherFixed.getIntValue("RuleType");
				int day = ruleTeacherFixed.getIntValue("DayOfWeek");
				int lesson = ruleTeacherFixed.getIntValue("LessonOfDay");

				RulePosition rulePosition = new RulePosition();
				// 0：不排位置，1：必排位置
				rulePosition.setRuleType(ruleType);
				rulePosition.setDay(day);
				rulePosition.setLesson(lesson);

				ruleTeacher.addPosition(rulePosition);

			}
			ruleTeachers.put(teacherId, ruleTeacher);
		}
		for(Iterator<String> it = ruleTeacherposs.keySet().iterator();it.hasNext();){
			String teacherId = it.next();
			List<JSONObject> tgRules = ruleTeacherposs.get(teacherId);
			RuleTeacher ruleTeacher = new RuleTeacher();
			if(ruleTeachers.containsKey(teacherId)){
				ruleTeacher = ruleTeachers.get(teacherId);
			}
			for(JSONObject tgr:tgRules){
				int day = tgr.getIntValue("DayOfWeek");
				int lesson = tgr.getIntValue("LessonOfDay");
				RulePosition rulePosition = new RulePosition();
				// 0：不排位置，1：必排位置
				rulePosition.setRuleType(0);
				rulePosition.setDay(day);
				rulePosition.setLesson(lesson);
				ruleTeacher.addPosition(rulePosition);
			}
			ruleTeachers.put(teacherId, ruleTeacher);
		}
		return ruleTeachers;

	}

	/**
	 * 取教师的教研活动
	 * 
	 * @param schoolId
	 *            学校
	 * @param timetableId
	 *            课表ID
	 * @return
	 */
	public List<RuleResearchMeeting> getRuleResearchMeetings(String schoolId,
			String timetableId) {
		// 教师的教研活动
		List<RuleResearchMeeting> ruleResearchMeetings = new ArrayList<RuleResearchMeeting>();
		List<JSONObject> researchWorkList = this.timetableService
				.getResearchWorkList(schoolId, timetableId);
		for (JSONObject researchWork : researchWorkList) {
			String teacherId = researchWork.getString("TeacherId");
			int day = researchWork.getIntValue("DayOfWeek");
			int lesson = researchWork.getIntValue("LessonOfDay");

			RuleResearchMeeting ruleResearchMeeting = new RuleResearchMeeting();
			ruleResearchMeeting.setTeacherId(teacherId);
			ruleResearchMeeting.setTeacherName("");
			ruleResearchMeeting.setDay(day);
			ruleResearchMeeting.setLesson(lesson);

			ruleResearchMeetings.add(ruleResearchMeeting);

		}

		return ruleResearchMeetings;

	}

	/**
	 * 取合班课规则
	 * 
	 * @param schoolId
	 *            学校
	 * @param timetableId
	 *            课表ID
	 * @return
	 */
	public List<RuleClassGroup> getRuleClassGroups(String schoolId,
			String timetableId) {
		// 合班
		List<RuleClassGroup> ruleClassGroups = new ArrayList<RuleClassGroup>();
		List<JSONObject> ruleClassGroupList = this.timetableService
				.getRuleClassGroupList(schoolId, timetableId);
		for (JSONObject rule : ruleClassGroupList) {
			String classRuleId = rule.getString("ClassRuleId");
			String courseId = rule.getString("CourseId");
			String classGroupId = rule.getString("ClassGroupId");
			String classGroupName = rule.getString("ClassGroupName");

			RuleClassGroup ruleClassGroup = new RuleClassGroup();
			ruleClassGroup.setClassGroupId(classGroupId);
			ruleClassGroup.setClassGroupName(classGroupName);
			ruleClassGroup.setCourseId(courseId);

			List<JSONObject> ruleClassFixedList = this.timetableService
					.getRuleClassFixedByRuleId(schoolId, classRuleId);
			for (JSONObject ruleClassFixed : ruleClassFixedList) {
				String classId = ruleClassFixed.getString("ClassId");
				ruleClassGroup.addClassGroup(classId);
			}

			ruleClassGroups.add(ruleClassGroup);

		}

		return ruleClassGroups;

	}

	/**
	 * 保存课表到数据库
	 * 
	 * @param arrangeGrid
	 *            排课模型
	 * @return
	 */
	public boolean saveArrangeTimeTable(ArrangeGrid arrangeGrid) {
		String timetableId = arrangeGrid.getTimetableId();
		String schoolId = arrangeGrid.getSchoolId();

		Set<String> gradeIds = new HashSet<String>();
		List<Map<String, Object>> timeTableList = new ArrayList<Map<String, Object>>();
		Collection<ArrangeClass> classTimetables = arrangeGrid
				.getClassTimetables().values();
		for (ArrangeClass arrangeClass : classTimetables) {
			// 年级
			String gradeId = arrangeClass.getGradeId();
			gradeIds.add(gradeId);
			// 班级
			String classId = arrangeClass.getClassId();
			int totalMaxDays = arrangeClass.getTotalMaxDays();
			int totalMaxLesson = arrangeClass.getTotalMaxLesson();

			CourseGene[][] timetable = arrangeClass.getTimetable();
			for (int i = 0; i < totalMaxLesson; i++) {

				for (int j = 0; j < totalMaxDays; j++) {
					CourseGene courseGene = timetable[j][i];
					if (courseGene != null && !courseGene.isFixed()) {
						// courseGene.isOddEven()
						Map<String, Object> timeTableData = new HashMap<String, Object>();
						timeTableData.put("timetableId", timetableId);
						timeTableData.put("schoolId", schoolId);

						timeTableData.put("classId", courseGene.getClassId());
						timeTableData.put("courseId", courseGene.getCourseId());
						timeTableData.put("dayOfWeek", courseGene.getDay());
						timeTableData
								.put("lessonOfDay", courseGene.getLesson());
						if (courseGene.isMerge()) {
							System.out.println("fsdsd");
						}
						timeTableData.put("mcGroupId",
								courseGene.getMcGroupId());
						timeTableData.put("isAdvance", 0);
						if (courseGene.isOddEven()) {
							timeTableData.put("courseType", "1");
						} else {
							timeTableData.put("courseType", "0");
						}
						timeTableList.add(timeTableData);

						if (courseGene.getDwCourseGene() != null) {
							CourseGene dwCourse = courseGene.getDwCourseGene();
							timeTableData = new HashMap<String, Object>();
							timeTableData.put("timetableId", timetableId);
							timeTableData.put("schoolId", schoolId);

							timeTableData.put("classId", dwCourse.getClassId());
							timeTableData.put("courseId",
									dwCourse.getCourseId());
							timeTableData.put("dayOfWeek", dwCourse.getDay());
							timeTableData.put("lessonOfDay",
									dwCourse.getLesson());
							timeTableData.put("isAdvance", 0);
							timeTableData.put("courseType", "2");
							timeTableData.put("mcGroupId",
									dwCourse.getMcGroupId());
							timeTableList.add(timeTableData);
						}

					} else {
						// 空
						// System.out.print("	(空)");
					}
					// System.out.print("	");
				}
				// System.out.println("");
			}

		}

		boolean result = this.timetableService.saveTimetableBatch(
				arrangeGrid.getSchoolId(), arrangeGrid.getTimetableId(),
				timeTableList, gradeIds);

		return result;
	}
	@Override
	public boolean saveArrangeTimeTable(ScheduleTable bestTable) {
		// TODO Auto-generated method stub
		String timetableId = bestTable.getTimetableId();
		String schoolId = bestTable.getSchoolId();
		List<GridPoint> flessons = bestTable.getFinishLessons();
		Set<String> gradeIds = new HashSet<String>();
		List<Map<String, Object>> timeTableList = new ArrayList<Map<String, Object>>();
		System.out.println("【计算后条数为：】"+flessons.size());
		for (GridPoint gridPoint : flessons) {
			// 年级
			String gradeId = gridPoint.getGradeId();
			if(!gradeIds.contains(gradeId)){
				
				gradeIds.add(gradeId);
			}
			if(gridPoint.isAdvance()){
				continue;
			}
			// 班级
			Map<String, Object> timeTableData = new HashMap<String, Object>();
			timeTableData.put("timetableId", timetableId);
			timeTableData.put("schoolId", schoolId);

			timeTableData.put("classId", gridPoint.getClassId());
			timeTableData.put("courseId", gridPoint.getCourseId());
			timeTableData.put("dayOfWeek", gridPoint.getDay());
			timeTableData.put("lessonOfDay", gridPoint.getLesson());
			timeTableData.put("mcGroupId",
						gridPoint.getMcGroupId());
			timeTableData.put("isAdvance", 0);
			timeTableData.put("courseType", gridPoint.getCourseType());
			timeTableList.add(timeTableData);


		}
		System.out.println("【保存时条数为：】"+timeTableList.size());

		boolean result = this.timetableService.saveTimetableBatch(
				bestTable.getSchoolId(), bestTable.getTimetableId(),
				timeTableList, gradeIds);

		return result;
	}
	// 交换课程位置
	@Override
	public void changeLessonPosition(String schoolId, String classId,
			String timetableId, String[] fromCourseIds, Integer fromDay,
			Integer fromLesson, String[] toCourseIds, Integer toDay,
			Integer toLesson, String[] fromCourseTypes, String[] toCourseTypes,
			String[] fromMcgId, String[] toMcgId, double UnlessonCount,
			RuleConflict ruleConflict, List<JSONObject> arrangeList,int breakRule)
			throws Exception {

		long d1 = new Date().getTime();

		List<JSONObject> needInsert = new ArrayList<JSONObject>();
		List<JSONObject> needDelete = new ArrayList<JSONObject>();
		// 构建课表map
		HashMap<String, List<JSONObject>> arrangeMap = new HashMap<String, List<JSONObject>>();
		for (JSONObject arrange : arrangeList) {
			List<JSONObject> list = new ArrayList<JSONObject>();
			int dow = arrange.getIntValue("DayOfWeek");
			int lod = arrange.getIntValue("LessonOfDay");
			String cid = arrange.getString("ClassId");
			String key = dow + "," + lod + "," + cid;
			if (arrangeMap.containsKey(key)) {
				list = arrangeMap.get(key);
			} else {
				arrangeMap.put(key, list);
			}

			list.add(arrange);

		}
		// 从左边任务栏拖课
		if ((fromCourseTypes.length == 1 && UnlessonCount == 0.5)
				|| (fromCourseTypes.length == 2
						&& !fromCourseTypes[0].equals("0")
						&& fromCourseTypes[1].length() == 0
						&& toCourseTypes.length == 2
						&& toCourseTypes[1].length() == 0 && !toCourseTypes[0]
							.equals("0"))) {
			Integer toCurseType = 2;
			if (toCourseTypes.length == 1 && toCourseTypes[0].length() > 0
					&& !toCourseTypes[0].equals("0")) {
				toCurseType = Integer.parseInt(toCourseTypes[0]);
			}
			String tkey = toDay + "," + toLesson + "," + classId;
			List<JSONObject> tList = arrangeMap.get(tkey);
			if (tList != null && tList.size() > 0) {
				for (JSONObject to : tList) {
					if (to.getInteger("CourseType") == 1) {
						toCurseType = 1;
						break;
					}
					if (to.getInteger("CourseType") == 2) {
						toCurseType = 2;
						break;
					}
				}
			}
			String mcGroupId = null;
			List<String> needInsClas = new ArrayList<String>();

			if (fromMcgId[0].trim().length() > 0) {
				mcGroupId = fromMcgId[0];
				needInsClas = ruleConflict.getMergeClassByGroupId(mcGroupId);
			} else {
				needInsClas.add(classId);
			}
			toCurseType = 2 - toCurseType + 1;
			for (String _classId : needInsClas) {
				JSONObject czObj = new JSONObject();
				czObj.put("schoolId", schoolId);
				czObj.put("timetableId", timetableId);
				czObj.put("classId", _classId);
				czObj.put("courseId", fromCourseIds[0]);
				czObj.put("day", toDay);
				czObj.put("lesson", toLesson);
				czObj.put("courseType", toCurseType);
				czObj.put("isAdvance", 0);
				czObj.put("mcGroupId", mcGroupId);
				needInsert.add(czObj);
			}

		} else {
			// 对调课程
			List<String> relatedClassIds = new ArrayList<String>();
			for (int i = 0; i < fromMcgId.length; i++) {
				String mcgId = fromMcgId[i];
				if (mcgId != null && mcgId.trim().length() > 0) {

					List<String> cids = ruleConflict
							.getMergeClassByGroupId(mcgId);

					for (String cid : cids) {
						if (!relatedClassIds.contains(cid)) {
							relatedClassIds.add(cid);
						}
					}
				}
			}
			for (int i = 0; i < toMcgId.length; i++) {
				String mcgId = toMcgId[i];

				if (mcgId != null && mcgId.trim().length() > 0) {

					List<String> cids = ruleConflict
							.getMergeClassByGroupId(mcgId);

					for (String cid : cids) {
						if (!relatedClassIds.contains(cid)) {
							relatedClassIds.add(cid);
						}
					}
				}
			}
			// 无合班课情况
			if (relatedClassIds.size() == 0) {

				if (toCourseIds != null && toCourseIds.length > 0
						&& toCourseIds[0].trim().length() > 0) {
					// this.timetableService.deleteArrangeTimetable(schoolId,
					// timetableId, classId, toCourseIds, toDay, toLesson);
					addNeedInsertAndDelToList(schoolId, classId, timetableId,
							fromDay, fromLesson, toCourseIds, toDay, toLesson,
							toMcgId, ruleConflict, needInsert, needDelete,
							toCourseTypes);

				}
				addNeedInsertAndDelToList(schoolId, classId, timetableId,
						toDay, toLesson, fromCourseIds, fromDay, fromLesson,
						fromMcgId, ruleConflict, needInsert, needDelete,
						fromCourseTypes);
			} else {
				// 有合班的情况下 处理所有合班
				// 已排课表-正式课表 天次，节次-课程课时

				for (String mClassId : relatedClassIds) {
					String tkey = toDay + "," + toLesson + "," + mClassId;
					List<JSONObject> tList = arrangeMap.get(tkey);
					String fkey = fromDay + "," + fromLesson + "," + mClassId;
					List<JSONObject> fList = arrangeMap.get(fkey);
					if (fromDay != null && fromLesson != null) {
						if (tList != null && tList.size() > 0) {

							for (JSONObject o : tList) {
								addNeedInsertAndDelToList(
										schoolId,
										o.getString("ClassId"),
										timetableId,
										fromDay,
										fromLesson,
										new String[] { o.getString("CourseId") },
										o.getInteger("DayOfWeek"),
										o.getInteger("LessonOfDay"),
										new String[] { o.getString("McGroupId") },
										ruleConflict, needInsert, needDelete,
										new String[] { o
												.getString("CourseType") });
							}
						}
						if (fList != null && fList.size() > 0) {
							for (JSONObject o : fList) {
								addNeedInsertAndDelToList(
										schoolId,
										o.getString("ClassId"),
										timetableId,
										toDay,
										toLesson,
										new String[] { o.getString("CourseId") },
										o.getInteger("DayOfWeek"),
										o.getInteger("LessonOfDay"),
										new String[] { o.getString("McGroupId") },
										ruleConflict, needInsert, needDelete,
										new String[] { o
												.getString("CourseType") });
							}
						}
					} else {
						JSONObject czObj = new JSONObject();
						czObj.put("schoolId", schoolId);
						czObj.put("timetableId", timetableId);
						czObj.put("classId", mClassId);
						czObj.put("courseId", fromCourseIds[0]);
						czObj.put("day", toDay);
						czObj.put("lesson", toLesson);
						if (fromCourseTypes[0].trim().length() == 0) {
							fromCourseTypes[0] = "0";
						}
						czObj.put("courseType", fromCourseTypes[0]);
						czObj.put("isAdvance", 0);
						czObj.put("mcGroupId", fromMcgId[0]);
						needInsert.add(czObj);
					}

				}
			}

		}
		JSONObject needUpdate = new JSONObject();
		needUpdate.put("needInsert", needInsert);
		needUpdate.put("needDelete", needDelete);
		needUpdate.put("breakRule", breakRule);
		//long d2 = new Date().getTime();
		//System.out.println("【微调】-组装更新数据耗时：" + (d2 - d1));
		timetableService.updateArrangeTimetable(needUpdate);

		//long d3 = new Date().getTime();
		//System.out.println("【微调】-更新数据库耗时：" + (d3 - d2));
	}

	/**
	 * 单条处理
	 * 
	 * @param schoolId
	 * @param classId
	 * @param timetableId
	 * @param fromDay
	 * @param fromLesson
	 * @param toCourseIds
	 * @param toDay
	 * @param toLesson
	 * @param toMcgId
	 * @param ruleConflict
	 * @param needInsert
	 * @param needDelete
	 * @param toCourseTypes
	 * @throws Exception
	 */
	public void addNeedInsertAndDelToList(String schoolId, String classId,
			String timetableId, Integer fromDay, Integer fromLesson,
			String[] toCourseIds, Integer toDay, Integer toLesson,
			String[] toMcgId, RuleConflict ruleConflict,
			List<JSONObject> needInsert, List<JSONObject> needDelete,
			String[] toCourseTypes) throws Exception {
		for (int i = 0; i < toCourseIds.length; i++) {
			if (toCourseIds[i].trim().length() > 0) {

				int courseType = 0;
				if (toCourseTypes[i].trim().length() > 0) {
					courseType = Integer.parseInt(toCourseTypes[i].trim());
				}
				JSONObject czObj = new JSONObject();
				czObj.put("schoolId", schoolId);
				czObj.put("timetableId", timetableId);
				czObj.put("classId", classId);
				czObj.put("courseId", toCourseIds[i]);
				czObj.put("day", toDay);
				czObj.put("lesson", toLesson);
				needDelete.add(czObj);

				JSONObject add = new JSONObject();
				add = (JSONObject) BeanTool.castBeanToFirstLowerKey(czObj);
				add.put("day", fromDay);
				add.put("lesson", fromLesson);
				add.put("isAdvance", 0);
				String mcgId = "";
				if (toMcgId.length > i) {
					mcgId = toMcgId[i];
				}
				add.put("mcGroupId", mcgId);
				add.put("courseType", courseType);
				needInsert.add(add);
			}

		}
	}

	// 删除课程位置
	@Override
	public int deleteLessonPosition(String schoolId, String classId,
			String timetableId, String[] courseIds, int day, int lesson,
			String[] mcGroupIds, RuleConflict ruleConflict,int breakRule) {
		List<JSONObject> needDelete = new ArrayList<JSONObject>();
		for (int i = 0; i < courseIds.length; i++) {
			if (courseIds[i].trim().length() > 0) {

				String mcGroupId = null;
				if (mcGroupIds.length > i && mcGroupIds[i].trim().length() > 0) {
					mcGroupId = mcGroupIds[i];
				}

				JSONObject czObj = new JSONObject();
				czObj.put("schoolId", schoolId);
				czObj.put("timetableId", timetableId);
				czObj.put("classId", classId);
				czObj.put("courseId", courseIds[i]);
				czObj.put("day", day);
				czObj.put("lesson", lesson);
				czObj.put("mcGroupId", mcGroupId);

				needDelete.add(czObj);
			}
		}
		JSONObject needUpdate = new JSONObject();
		needUpdate.put("needDelete", needDelete);
		needUpdate.put("breakRule", breakRule);
		return timetableService.updateArrangeTimetable(needUpdate);
	}

	/**
	 * 取年级信息字典
	 * 
	 * @param xn
	 *            学年
	 * @param sch
	 *            学校
	 * @return
	 */
	public Map<String, Grade> getGradesDic(String xn, School sch,String xq) {
		Map<String, Grade> gradesDic = new HashMap<String, Grade>();
		List<Grade> grades = this.commonService.getGradeList(sch, xn+xq);
		for (Grade grade : grades) {
			String synj = this.commonService.getSynjByGrade(grade, xn);
			if (grade != null) {

				gradesDic.put(synj, grade);
			}
		}

		return gradesDic;
	}

	/**
	 * 取班级信息字典
	 * 
	 * @param schoolId
	 *            学校
	 * @param termInfoId
	 *            学年学期
	 * @return
	 */
	public Map<String, Classroom> getClassRoomsDic(School school,
			List<Grade> cxGrade,String xnxq) {
		Map<String, Classroom> classroomsDic = new HashMap<String, Classroom>();
		List<Classroom> classrooms = this.commonService.getSimpleClassList(
				school, cxGrade,xnxq);
		for (Classroom classroom : classrooms) {
			classroomsDic.put(String.valueOf(classroom.getId()), classroom);
		}
		return classroomsDic;
	}

	/**
	 * 取教师字典
	 * 
	 * @param school
	 *            学校
	 * @return
	 */
	public Map<String, Account> getTeachersDic(School school,String xnxq) {
		Map<String, Account> teachersDic = new HashMap<String, Account>();
		List<Account> teachers = this.commonService.getAllSchoolEmployees(school, xnxq, null);
		for (Account teacher : teachers) {
			if(teacher.getName()!=null&&teacher.getName().trim().length()>0){
				teachersDic.put(String.valueOf(teacher.getId()), teacher);
			}
		}

		return teachersDic;
	}

	/**
	 * 取科目字典
	 * 
	 * @param school
	 *            学校
	 * @return
	 */
	public Map<String, LessonInfo> getCoursesDic(School school,String xnxq) {
		Map<String, LessonInfo> coursesDic = new HashMap<String, LessonInfo>();
		List<LessonInfo> lessonInfos = this.commonService
				.getLessonInfoList(school,xnxq);
		for (LessonInfo lessonInfo : lessonInfos) {
			coursesDic.put(String.valueOf(lessonInfo.getId()), lessonInfo);
		}

		return coursesDic;
	}

	/**
	 * 根据年级对象取年级名称
	 * 
	 * @param grade
	 *            年级对象
	 * @return
	 */
	public String getGradeName(Grade grade) {
		String gradeName = AccountStructConstants.T_GradeLevelName.get(grade
				.getCurrentLevel());
		return gradeName;
	}

	@Override
	public List<JSONObject> getRuleTeachersInHasArrage(String schoolId,
			String timetableId) {
		//

		List<JSONObject> list = this.timetableService
				.getRuleTeachersInHasArrage(schoolId, timetableId);
		return list;
	}

	@Override
	public void changeLessonPositionForTeacher(String schoolId,
			String timetableId, JSONArray fromCourses, Integer fromDay,
			Integer fromLesson, JSONArray toCourses, Integer toDay,
			Integer toLesson, double unLessonCount, RuleConflict ruleConflict,
			List<JSONObject> arrangeList,int breakRule) {
		// TODO Auto-generated method stub
		List<JSONObject> needInsert = new ArrayList<JSONObject>();
		List<JSONObject> needDelete = new ArrayList<JSONObject>();
		HashMap<String, List<JSONObject>> arrangeMap = new HashMap<String, List<JSONObject>>();
		for (JSONObject arrange : arrangeList) {
			List<JSONObject> list = new ArrayList<JSONObject>();
			int dow = arrange.getIntValue("DayOfWeek");
			int lod = arrange.getIntValue("LessonOfDay");
			String cid = arrange.getString("ClassId");
			String key = dow + "," + lod + "," + cid;
			if (arrangeMap.containsKey(key)) {
				list = arrangeMap.get(key);
			} else {
				arrangeMap.put(key, list);
			}

			list.add(arrange);

		}

		try {
			getTeacherChangePosBySinglePoint(schoolId, timetableId,
					fromCourses, fromDay, fromLesson, toDay, toLesson,
					ruleConflict, needInsert, needDelete, arrangeMap);

			if (toCourses != null && toCourses.size() > 0) {
				getTeacherChangePosBySinglePoint(schoolId, timetableId,
						toCourses, toDay, toLesson, fromDay, fromLesson,
						ruleConflict, needInsert, needDelete, arrangeMap);
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		JSONObject needUpdate = new JSONObject();
		needUpdate.put("needInsert", needInsert);
		needUpdate.put("needDelete", needDelete);
		needUpdate.put("breakRule", breakRule);
		timetableService.updateArrangeTimetable(needUpdate);

	}

	/**
	 * @param schoolId
	 * @param timetableId
	 * @param fromCourses
	 * @param fromDay
	 * @param fromLesson
	 * @param toDay
	 * @param toLesson
	 * @param ruleConflict
	 * @param needInsert
	 * @param needDelete
	 * @param arrangeMap
	 * @throws Exception
	 */
	public void getTeacherChangePosBySinglePoint(String schoolId,
			String timetableId, JSONArray fromCourses, Integer fromDay,
			Integer fromLesson, Integer toDay, Integer toLesson,
			RuleConflict ruleConflict, List<JSONObject> needInsert,
			List<JSONObject> needDelete,
			HashMap<String, List<JSONObject>> arrangeMap) throws Exception {
		// 对调课程
		List<String> relatedClassIds = new ArrayList<String>();

		JSONArray tempCourses = new JSONArray();
		for (int i = 0; i < fromCourses.size(); i++) {
			JSONObject course = fromCourses.getJSONObject(i);
			String classId = course.getString("classId");
			if (course.containsKey("ClassId")) {
				classId = course.getString("ClassId");
			}
			
			List<JSONObject> flist = arrangeMap.get(fromDay+ "," +fromLesson+ "," + classId);
			for(JSONObject f:flist){
				JSONObject temp = new JSONObject();
				temp.put("McGroupId", f.get("McGroupId"));
				temp.put("classId", f.get("ClassId"));
				temp.put("courseId", f.get("CourseId"));
				temp.put("courseType", f.get("CourseType"));
				tempCourses.add(temp);
			}
		}
		fromCourses = tempCourses;
		HashMap<String, List<JSONObject>> classCourseMap = new HashMap<String, List<JSONObject>>();
		for (int i = 0; i < fromCourses.size(); i++) {
			JSONObject course = fromCourses.getJSONObject(i);
			String classId = course.getString("classId");
			if (course.containsKey("ClassId")) {
				classId = course.getString("ClassId");
			}
			String mcgId = course.getString("McGroupId");
			if (mcgId != null && mcgId.trim().length() > 0) {

				List<String> cids = ruleConflict.getMergeClassByGroupId(mcgId);

				for (String cid : cids) {
					if (!relatedClassIds.contains(cid)) {
						relatedClassIds.add(cid);
					}
				}
			}
			// 判断结束位置是否需要合班调整
			List<JSONObject> tlist = arrangeMap.get(toDay + "," + toLesson
					+ "," + classId);
			if (tlist != null && tlist.size() > 0) {
				for (int k = 0; k < tlist.size(); k++) {
					String toMcgId = tlist.get(k).getString("McGroupId");
					if (toMcgId != null && toMcgId.trim().length() > 0) {

						List<String> cids = ruleConflict
								.getMergeClassByGroupId(toMcgId);

						for (String cid : cids) {
							if (!relatedClassIds.contains(cid)) {
								relatedClassIds.add(cid);
							}
						}
					}

				}

			}

			if (classCourseMap.containsKey(classId)) {
				classCourseMap.get(classId).add(course);
			} else {
				List<JSONObject> list = new ArrayList<JSONObject>();
				list.add(course);
				classCourseMap.put(classId, list);
			}
		}
		// 无合班课情况
		if (relatedClassIds.size() == 0) {
			for (String classId : classCourseMap.keySet()) {
				List<JSONObject> flist = classCourseMap.get(classId);

				String[] fromCourseIds = new String[flist.size()];
				String[] fromMcgId = new String[flist.size()];
				String[] fromCourseTypes = new String[flist.size()];
				for (int k = 0; k < flist.size(); k++) {
					fromCourseIds[k] = flist.get(k).getString("courseId");
					fromMcgId[k] = flist.get(k).getString("McGroupId");
					fromCourseTypes[k] = flist.get(k).getString("courseType");
				}
				addNeedInsertAndDelToList(schoolId, classId, timetableId,
						toDay, toLesson, fromCourseIds, fromDay, fromLesson,
						fromMcgId, ruleConflict, needInsert, needDelete,
						fromCourseTypes);

				List<JSONObject> tlist = arrangeMap.get(toDay + "," + toLesson
						+ "," + classId);
				if (tlist != null && tlist.size() > 0) {
					String[] toCourseIds = new String[tlist.size()];
					String[] toMcgId = new String[tlist.size()];
					String[] toCourseTypes = new String[tlist.size()];
					for (int k = 0; k < tlist.size(); k++) {
						toCourseIds[k] = tlist.get(k).getString("CourseId");
						toMcgId[k] = tlist.get(k).getString("McGroupId");
						toCourseTypes[k] = tlist.get(k).getString("CourseType");
					}
					addNeedInsertAndDelToList(schoolId, classId, timetableId,
							fromDay, fromLesson, toCourseIds, toDay, toLesson,
							toMcgId, ruleConflict, needInsert, needDelete,
							toCourseTypes);
				}
			}
		} else {
			// 有合班的情况下 处理所有合班
			// 已排课表-正式课表 天次，节次-课程课时

			for (String mClassId : relatedClassIds) {
				String tkey = toDay + "," + toLesson + "," + mClassId;
				List<JSONObject> tList = arrangeMap.get(tkey);
				String fkey = fromDay + "," + fromLesson + "," + mClassId;
				List<JSONObject> fList = arrangeMap.get(fkey);
				if (tList != null && tList.size() > 0) {
					for (JSONObject o : tList) {
						addNeedInsertAndDelToList(schoolId,
								o.getString("ClassId"), timetableId, fromDay,
								fromLesson,
								new String[] { o.getString("CourseId") },
								o.getInteger("DayOfWeek"),
								o.getInteger("LessonOfDay"),
								new String[] { o.getString("McGroupId") },
								ruleConflict, needInsert, needDelete,
								new String[] { o.getString("CourseType") });
					}
				}
				if (fList != null && fList.size() > 0) {
					for (JSONObject o : fList) {
						addNeedInsertAndDelToList(schoolId,
								o.getString("ClassId"), timetableId, toDay,
								toLesson,
								new String[] { o.getString("CourseId") },
								o.getInteger("DayOfWeek"),
								o.getInteger("LessonOfDay"),
								new String[] { o.getString("McGroupId") },
								ruleConflict, needInsert, needDelete,
								new String[] { o.getString("CourseType") });
					}
				}

			}
		}
	}

	@Override
	public int deleteLessonPositionForTeacher(String schoolId,
			String timetableId, JSONArray classCourse, int day, int lesson,
			RuleConflict ruleConflict,int breakRule) {
		// TODO Auto-generated method stub

		List<JSONObject> needDelete = new ArrayList<JSONObject>();
		for (int i = 0; i < classCourse.size(); i++) {

			JSONObject cc = classCourse.getJSONObject(i);
			String classId = cc.getString("classId");
			String courseId = cc.getString("courseId");
			String mcGroupId = cc.getString("McGroupId");
			if (courseId.trim().length() > 0) {
				JSONObject czObj = new JSONObject();
				czObj.put("schoolId", schoolId);
				czObj.put("timetableId", timetableId);
				czObj.put("classId", classId);
				czObj.put("courseId", courseId);
				czObj.put("day", day);
				czObj.put("lesson", lesson);
				czObj.put("mcGroupId", mcGroupId);

				needDelete.add(czObj);
			}
		}
		JSONObject needUpdate = new JSONObject();
		needUpdate.put("needDelete", needDelete);
		needUpdate.put("breakRule", breakRule);
		return timetableService.updateArrangeTimetable(needUpdate);
	}

	@Override
	public ScheduleRule getSchRuleConflict(String schoolId, String timetableId) {
		// TODO Auto-generated method stub
		// 冲突规则
		ScheduleRule ruleConflict = new ScheduleRule();

		// 科目规则
		Map<String, SchRuleCourse> ruleCourses = this.getSchRuleCourses(
				schoolId, timetableId);

		// 教师规则
		Map<String, SchRuleTeacher> ruleTeachers = this.getSchRuleTeachers(
				schoolId, timetableId);

		// 合班
		List<SchRuleClassGroup> ruleClassGroups = this.getSchRuleClassGroups(
				schoolId, timetableId);

		// 教师的教研活动
		List<SchRuleResearchMeeting> ruleResearchMeetings = this
				.getSchRuleResearchMeetings(schoolId, timetableId);

		Map<String, SchRuleGround> ruleGrounds = this.getSchRuleGrounds(
				schoolId, timetableId);
		ruleConflict.setRuleCourses(ruleCourses);
		ruleConflict.setRuleTeachers(ruleTeachers);
		ruleConflict.setRuleClassGroups(ruleClassGroups);
		ruleConflict.setRuleResearchMeetings(ruleResearchMeetings);
		ruleConflict.setRuleGrounds(ruleGrounds);

		return ruleConflict;
	}

	@Override
	public Map<String, SchRuleCourse> getSchRuleCourses(String schoolId,
			String timetableId) {
		// TODO Auto-generated method stub
		// 科目规则
		Map<String, SchRuleCourse> ruleCourses = new HashMap<String, SchRuleCourse>();
		List<JSONObject> ruleCourseList = this.timetableService
				.getRuleCourseList(schoolId, timetableId);
		for (JSONObject rule : ruleCourseList) {
			String courseRuleId = rule.getString("CourseRuleId");
			String courseId = rule.getString("CourseId");
			int maxClassNum = rule.getIntValue("MaxClassNum");
			int maxAMNum = rule.getIntValue("AMMaxNum");
			int maxPMNum = rule.getIntValue("PMMaxNum");
			int courseLevel = rule.getIntValue("CourseLevel");
			String gradeId = rule.getString("GradeId");

			SchRuleCourse ruleCourse = new SchRuleCourse();
			ruleCourse.setCourseId(courseId);
			ruleCourse.setMaxClassNum(maxClassNum);
			ruleCourse.setMaxAMNum(maxAMNum);
			ruleCourse.setMaxPMNum(maxPMNum);
			ruleCourse.setCourseLevel(courseLevel);
			ruleCourse.setGradeId(gradeId);
			List<JSONObject> ruleCourseFixedList = this.timetableService
					.getRuleCourseFixedByRuleId(schoolId, courseRuleId);
			for (JSONObject ruleCourseFixed : ruleCourseFixedList) {
				int ruleType = ruleCourseFixed.getIntValue("RuleType");
				int day = ruleCourseFixed.getIntValue("DayOfWeek");
				int lesson = ruleCourseFixed.getIntValue("LessonOfDay");

				SchRulePosition rulePosition = new SchRulePosition();
				// 0：不排位置，1：必排位置
				rulePosition.setRuleType(ruleType);
				rulePosition.setDay(day);
				rulePosition.setLesson(lesson);

				ruleCourse.addPosition(rulePosition);

			}

			ruleCourses.put(
					ruleCourse.getGradeId() + "_" + ruleCourse.getCourseId(),
					ruleCourse);
		}

		return ruleCourses;
	}

	@Override
	public Map<String, SchRuleTeacher> getSchRuleTeachers(String schoolId,
			String timetableId) {
		// TODO Auto-generated method stub
		//查询获取教师组规则
		Map<String,List<JSONObject>> ruleTeacherposs = new HashMap<String, List<JSONObject>>();
		try {
			ruleTeacherposs = this.timetableService.getRuleTeacherGroups(schoolId,timetableId);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// 教师规则
		Map<String, SchRuleTeacher> ruleTeachers = new HashMap<String, SchRuleTeacher>();
		List<JSONObject> ruleTeacherList = this.timetableService
				.getRuleTeacherList(schoolId, timetableId);
		for (JSONObject rule : ruleTeacherList) {
			String teacherRuleId = rule.getString("TeacherRuleId");
			String teacherId = rule.getString("TeacherId");
			int maxPerDay = rule.getIntValue("MaxPerDay");

			SchRuleTeacher ruleTeacher = new SchRuleTeacher();
			ruleTeacher.setTeacherId(teacherId);
			ruleTeacher.setMaxPerDay(maxPerDay);

			List<JSONObject> ruleTeacherFixedList = this.timetableService
					.getRuleTeacherFixedByRuleId(schoolId, teacherRuleId);
			for (JSONObject ruleTeacherFixed : ruleTeacherFixedList) {
				int ruleType = ruleTeacherFixed.getIntValue("RuleType");
				int day = ruleTeacherFixed.getIntValue("DayOfWeek");
				int lesson = ruleTeacherFixed.getIntValue("LessonOfDay");

				SchRulePosition rulePosition = new SchRulePosition();
				// 0：不排位置，1：必排位置
				rulePosition.setRuleType(ruleType);
				rulePosition.setDay(day);
				rulePosition.setLesson(lesson);

				ruleTeacher.addPosition(rulePosition);
			}
			ruleTeachers.put(teacherId, ruleTeacher);
		}

		for(Iterator<String> it = ruleTeacherposs.keySet().iterator();it.hasNext();){
			String teacherId = it.next();
			List<JSONObject> tgRules = ruleTeacherposs.get(teacherId);
			SchRuleTeacher ruleTeacher = new SchRuleTeacher();
			if(ruleTeachers.containsKey(teacherId)){
				ruleTeacher = ruleTeachers.get(teacherId);
			}
			for(JSONObject tgr:tgRules){
				int day = tgr.getIntValue("DayOfWeek");
				int lesson = tgr.getIntValue("LessonOfDay");
				SchRulePosition rulePosition = new SchRulePosition();
				// 0：不排位置，1：必排位置
				rulePosition.setRuleType(0);
				rulePosition.setDay(day);
				rulePosition.setLesson(lesson);
				ruleTeacher.addPosition(rulePosition);
			}
			ruleTeachers.put(teacherId, ruleTeacher);
		}
		return ruleTeachers;
	}

	@Override
	public List<SchRuleResearchMeeting> getSchRuleResearchMeetings(
			String schoolId, String timetableId) {
		// TODO Auto-generated method stub
		// 教师的教研活动
		List<SchRuleResearchMeeting> ruleResearchMeetings = new ArrayList<SchRuleResearchMeeting>();
		List<JSONObject> researchWorkList = this.timetableService
				.getResearchWorkList(schoolId, timetableId);
		for (JSONObject researchWork : researchWorkList) {
			String teacherId = researchWork.getString("TeacherId");
			int day = researchWork.getIntValue("DayOfWeek");
			int lesson = researchWork.getIntValue("LessonOfDay");

			SchRuleResearchMeeting ruleResearchMeeting = new SchRuleResearchMeeting();
			ruleResearchMeeting.setTeacherId(teacherId);
			ruleResearchMeeting.setTeacherName("");
			ruleResearchMeeting.setDay(day);
			ruleResearchMeeting.setLesson(lesson);

			ruleResearchMeetings.add(ruleResearchMeeting);

		}

		return ruleResearchMeetings;
	}

	@Override
	public List<SchRuleClassGroup> getSchRuleClassGroups(String schoolId,
			String timetableId) {
		// TODO Auto-generated method stub
		// 合班
		List<SchRuleClassGroup> ruleClassGroups = new ArrayList<SchRuleClassGroup>();
		List<JSONObject> ruleClassGroupList = this.timetableService
				.getRuleClassGroupList(schoolId, timetableId);
		for (JSONObject rule : ruleClassGroupList) {
			String classRuleId = rule.getString("ClassRuleId");
			String courseId = rule.getString("CourseId");
			String classGroupId = rule.getString("ClassGroupId");
			String classGroupName = rule.getString("ClassGroupName");

			SchRuleClassGroup ruleClassGroup = new SchRuleClassGroup();
			ruleClassGroup.setClassGroupId(classGroupId);
			ruleClassGroup.setClassGroupName(classGroupName);
			ruleClassGroup.setCourseId(courseId);

			List<JSONObject> ruleClassFixedList = this.timetableService
					.getRuleClassFixedByRuleId(schoolId, classRuleId);
			for (JSONObject ruleClassFixed : ruleClassFixedList) {
				String classId = ruleClassFixed.getString("ClassId");
				ruleClassGroup.addClassGroup(classId);
			}

			ruleClassGroups.add(ruleClassGroup);

		}

		return ruleClassGroups;
	}

	@Override
	public Map<String, SchRuleGround> getSchRuleGrounds(String schoolId,
			String timetableId) {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
		Map<String, SchRuleGround> rsMap = new HashMap<String, SchRuleGround>();
		List<JSONObject> ruleGroundList = this.timetableService
				.getRuleGroundList(schoolId, timetableId);

		for (JSONObject obj : ruleGroundList) {
			SchRuleGround rg = new SchRuleGround();
			String grades = obj.getString("UsedGradeIds");
			int num = obj.getIntValue("MaxClassNum");
			String subId = obj.getString("SubjectId");
			rg.setMaxClassNum(num);
			rg.setSubjectId(subId);
			rg.setUsedGradeIds(grades);

			if (grades.indexOf(",") != -1) {
				String[] gds = grades.split(",");
				for (String gs : gds) {
					rsMap.put(gs + "_" + subId, rg);
				}
			} else {
				rsMap.put(grades.trim() + "_" + subId, rg);
			}
		}
		return rsMap;
	}

	
	

}
