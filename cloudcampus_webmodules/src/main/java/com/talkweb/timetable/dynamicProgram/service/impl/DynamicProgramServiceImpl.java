package com.talkweb.timetable.dynamicProgram.service.impl;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.Classroom;
import com.talkweb.accountcenter.thrift.Grade;
import com.talkweb.accountcenter.thrift.School;
import com.talkweb.base.common.CacheExpireTime;
import com.talkweb.common.tools.UUIDUtil;
import com.talkweb.common.tools.sort.Sort;
import com.talkweb.common.tools.sort.SortEnum;
import com.talkweb.commondata.dao.RedisOperationDAO;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.scoreManage.action.ScoreUtil;
import com.talkweb.timetable.arrangement.exception.ArrangeTimetableException;
import com.talkweb.timetable.arrangement.service.ArrangeDataService;
import com.talkweb.timetable.dao.TimetableDao;
import com.talkweb.timetable.dynamicProgram.core.CourseCompare;
import com.talkweb.timetable.dynamicProgram.entity.ScheduleClass;
import com.talkweb.timetable.dynamicProgram.entity.ScheduleDatadic;
import com.talkweb.timetable.dynamicProgram.entity.ScheduleTable;
import com.talkweb.timetable.dynamicProgram.entity.ScheduleTask;
import com.talkweb.timetable.dynamicProgram.entity.ScheduleTaskGroup;
import com.talkweb.timetable.dynamicProgram.enums.CourseLevel;
import com.talkweb.timetable.dynamicProgram.rule.SchRuleClassGroup;
import com.talkweb.timetable.dynamicProgram.rule.SchRuleCourse;
import com.talkweb.timetable.dynamicProgram.rule.SchRuleGround;
import com.talkweb.timetable.dynamicProgram.rule.SchRulePosition;
import com.talkweb.timetable.dynamicProgram.rule.SchRuleResearchMeeting;
import com.talkweb.timetable.dynamicProgram.rule.SchRuleTeacher;
import com.talkweb.timetable.dynamicProgram.rule.ScheduleRule;
import com.talkweb.timetable.dynamicProgram.service.DynamicProgramService;
import com.talkweb.timetable.service.TimetableService;

@Service
public class DynamicProgramServiceImpl implements DynamicProgramService {

	@Autowired
	private ArrangeDataService arrangeDataService;
	@Autowired
	private TimetableService timetableService;
	@Autowired
	private AllCommonDataService commonService;
	@Autowired
	private TimetableDao timetableDao;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private CourseCompare courseCompare = new CourseCompare();
	@Resource(name = "redisOperationDAOSDRTempDataImpl")
	private RedisOperationDAO redisOperationDAO;

	@SuppressWarnings("unchecked")
	@Override
	public void startTask(HttpSession session, School school,
			String timetableId, List<String> gradeIds, JSONObject runParams)
			throws ArrangeTimetableException {
		// TODO Auto-generated method stub
		String schoolId = school.getId() + "";
		int isTeachingSync = runParams.getIntValue("isTeachingSync");
		int lessonDistrubute = runParams.getIntValue("lessonDistrubute");
		int isTryFinish = runParams.getIntValue("isTryFinish");
		int teaSpNum = runParams.getIntValue("teaSpNum");

		// 冲突规则
		ScheduleRule ruleConflict = new ScheduleRule();
		ruleConflict.setIsTeachingSync(isTeachingSync);
		ruleConflict.setLessonDistrubute(lessonDistrubute);
		ruleConflict.setIsTryFinish(isTryFinish);
		ruleConflict.setTeaSpNum(teaSpNum);
		
		ScheduleDatadic schDataDic = new ScheduleDatadic();
		// 课表参数信息
		JSONObject timetableInfo = this.timetableService
				.getArrangeTimetableInfo(school.getId() + "", timetableId);
		if (timetableInfo == null) {
			this.updateArrangeProgress(session.getId(), -1, 0, "编排失败，您选择的课表不存在");
			throw new ArrangeTimetableException("您选择的课表竟然不存在哦");
		}
		School sch = (School) session.getAttribute("school");
		String schoolYear = timetableInfo.getString("SchoolYear");
		String termName = timetableInfo.getString("TermName");
		String termInfo = schoolYear + termName;
		sch = commonService.getSchoolById(sch.getId(), termInfo);
		// 全校 -使用年级-年级map
		schDataDic.setGradesDic(this.arrangeDataService.getGradesDic(
				schoolYear, sch, termName));
		List<Long> clearClassIds = new ArrayList<Long>();
		for (String grade : gradeIds) {

			Grade gd = schDataDic.getGradeBySynj(grade);
			if (gd != null) {
				clearClassIds.addAll(gd.getClassIds());
			}
		}
		String subsql = "";
		if (clearClassIds.size() > 0) {
			subsql = "  classid in ( ";
			for (Long cl : clearClassIds) {
				subsql += "'" + cl + "',";
			}
			subsql = subsql.substring(0, subsql.length() - 1);
			subsql = subsql + ")";
		}
		

		int maxDaysForWeek = timetableInfo.getIntValue("MaxDaysForWeek");
		boolean halfAtLastDay = timetableInfo.getBooleanValue("halfAtLastDay");
		String termInfoId = schoolYear.concat(termName);

		// 建立所有班级课表容器
		ScheduleTable arrangeGrid = new ScheduleTable();
		arrangeGrid.setSchoolId(schoolId);
		arrangeGrid.setTimetableId(timetableId);
		arrangeGrid.setMaxDays(maxDaysForWeek);
		arrangeGrid.setHalfAtLastDay(halfAtLastDay);
		arrangeGrid.setProcessId(session.getId());

		schDataDic.setXn(schoolYear);
		schDataDic.setXq(termName);

		// 科目规则
		this.updateArrangeProgress(session.getId(), 0, 5, "加载科目规则");
		Map<String, SchRuleCourse> ruleCourses = this.arrangeDataService
				.getSchRuleCourses(schoolId, timetableId);

		// 教师规则
		this.updateArrangeProgress(session.getId(), 0, 10, "加载教师规则");
		Map<String, SchRuleTeacher> ruleTeachers = this.arrangeDataService
				.getSchRuleTeachers(schoolId, timetableId);

		// 合班
		this.updateArrangeProgress(session.getId(), 0, 15, "加载合班规则");
		List<SchRuleClassGroup> ruleClassGroups = this.arrangeDataService
				.getSchRuleClassGroups(schoolId, timetableId);

		// 单双周
		JSONObject courseGroup = this.timetableService.getEvenOddWeekGroupList(
				schoolId, timetableId);

		// 教师的教研活动
		this.updateArrangeProgress(session.getId(), 0, 20, "加载教研活动规则");
		List<SchRuleResearchMeeting> ruleResearchMeetings = this.arrangeDataService
				.getSchRuleResearchMeetings(schoolId, timetableId);

		this.updateArrangeProgress(session.getId(), 0, 25, "加载基础数据");

		ruleConflict.setRuleCourses(ruleCourses);
		ruleConflict.setRuleTeachers(ruleTeachers);
		ruleConflict.setRuleClassGroups(ruleClassGroups);
		ruleConflict.setRuleResearchMeetings(ruleResearchMeetings);

		// 从接口读取基础数据字典

		// 使用classid-班级map
		schDataDic.setClassroomsDic(this.arrangeDataService.getClassRoomsDic(
				school, null, termInfoId));
		// lessonId-LessonInfo map
		schDataDic.setCoursesDic(this.arrangeDataService.getCoursesDic(school,
				termInfoId));
		// teacherId-教师 map
		schDataDic.setTeachersDic(this.arrangeDataService.getTeachersDic(
				school, termInfoId));

		ruleConflict.setScheduleDatadic(schDataDic);
		// 教师已排课程
		List<JSONObject> otherGrade = this.arrangeDataService
				.getRuleTeachersInHasArrage(schoolId, timetableId);

		Map<String, SchRuleGround> schRuleGround = this.arrangeDataService
				.getSchRuleGrounds(school.getId() + "", timetableId);

		// 课表内 -使用年级-T_CRCA_GradeSet map
		Map<String, JSONObject> gradeSets = new HashMap<String, JSONObject>();

		List<JSONObject> gradeSetList = this.timetableService.getGradeSetList(
				schoolId, timetableId, gradeIds);
		for (JSONObject gradeSet : gradeSetList) {
			String gradeId = gradeSet.getString("GradeId");
			gradeSets.put(gradeId, gradeSet);
		}
		ruleTeachers = MergeRuleAndArrangeResult(ruleTeachers, otherGrade,
				schRuleGround, schDataDic, gradeIds,ruleConflict);
		ruleConflict.setRuleGrounds(schRuleGround);
		// 根据选择的年级取年级下所有的班级
		List<Classroom> classInfos = commonService.getSimpleClassList(sch,
				null, termInfoId);
		List<String> classIds = new ArrayList<String>();
		for (Classroom classInfo : classInfos) {
			// 年级代码
			String classId = String.valueOf(classInfo.getId());
			String synj = schDataDic.getGradeSynjByClassId(classId);
			String gradeLevl = schDataDic.getGradeLevelByClassId(classId);
			if (synj == null) {
				continue;
			}
			// 对应的年级参数
			JSONObject gradeSet = gradeSets.get(synj);
			if (gradeSet == null) {
				continue;
			}
			classIds.add(classId);
			int amLessonNum = gradeSet.getIntValue("AMLessonNum");
			int pmLessonNum = gradeSet.getIntValue("PMLessonNum");

			ScheduleClass schClass = new ScheduleClass(synj, gradeLevl,
					classId, schDataDic.getClassNameById(classId), amLessonNum,
					pmLessonNum, halfAtLastDay, maxDaysForWeek);

			// 将班级课表模型 放入总容器
			arrangeGrid.addClassIdScheduleMap(schClass);
			// 添加年级上午最大排课数
			ruleConflict.addToGradeLevelMaxAmNumMap(gradeLevl, amLessonNum);

		}
		this.updateArrangeProgress(session.getId(), 0, 30, "加载教学任务");
		// 教师的教学任务 teacherId,arrangeTeacher map
		// classId_lessonId -- arrangeCourse map
		// 根据班级取教学任务
		List<JSONObject> taskList = this.timetableService.getTaskByTimetable(
				schoolId, timetableId, classIds);
		if (taskList == null || taskList.size() == 0) {
			this.updateArrangeProgress(session.getId(), -1, 0,
					"编排失败，所选的年级下没有教学任务");
			return;
		}
		arrangeGrid.setScheduleDatadic(schDataDic);
		arrangeGrid.setScheduleRule(ruleConflict);
		// 任务--去重 合并教师
		HashMap<String, JSONObject> taskTeacherMap = new HashMap<String, JSONObject>();
		// 组合任务 key为 synj,courseId,taskNum value为
		// {groupTaskList:List<>,taskNum,courseId,maxSpNum,amNum,pmNum,teacherIds}
		HashMap<String, JSONObject> taskGroupMap = new HashMap<String, JSONObject>();
		// 教师map classId_courseId --> teacherIds
		HashMap<String, List<String>> classTaskTeacherMap = new HashMap<String, List<String>>();
		// 组装数据
		generateTaskMapsByList(arrangeGrid, ruleConflict, maxDaysForWeek,
				halfAtLastDay, gradeSets, taskList, taskTeacherMap,
				taskGroupMap, classTaskTeacherMap);

		this.updateArrangeProgress(session.getId(), 0, 33, "加载预排课数据");

		// 读取预排课安排
		List<JSONObject> advanceArrangeList = this.timetableService
				.getAdvanceArrangeList(schoolId, timetableId);

		arrangeGrid.setClassTaskTeacherMap(classTaskTeacherMap);
		arrangeGrid.preArrange(advanceArrangeList, courseGroup,
				classTaskTeacherMap);
		arrangeGrid.setSingleDoubleCourseMap(courseGroup);
		arrangeGrid.preArrangeSdWeekCourse(courseGroup);
		ScheduleTable bestTable = null;
		try {
			this.updateArrangeProgress(session.getId(), 0, 40, "正在智能排课...");
			long d1 = new Date().getTime();
			// bestTable = DynamicProcess.getBestScheduleTable(arrangeGrid,
			// session.getId(), progressMap);
			if (bestTable == null) {
				arrangeGrid.setRetryTimes(4);
				//校验规则
				JSONObject checkObj = arrangeGrid.preCheckValid();
				int ckCode = checkObj.getIntValue("code");
				if(ckCode<0){
					String msg = checkObj.getString("rsMsg");
					this.updateArrangeProgress(session.getId(), -1, 0, "校验教学任务不通过,原因：|"+msg);
					return;
				}
				//校验课时、单双周等
				arrangeGrid.startArrange();
				bestTable = arrangeGrid;
			}
			long d2 = new Date().getTime();
			System.out.println("【智能排课】-排课耗时：" + (d2 - d1));
		} catch (Exception e) {
			e.printStackTrace();
			this.updateArrangeProgress(session.getId(), -1, 0, "编排失败，请稍后重试");
		}
		if (bestTable != null) {
			this.updateArrangeProgress(session.getId(), 0, 100, "正在保存排课结果...");
			this.timetableService.clearTimetable(schoolId, timetableId, subsql,schoolYear,termName);
			this.arrangeDataService.saveArrangeTimeTable(bestTable);

			String rsMsg = "排课成功结束";
			if (bestTable.getErrorInfos().size() > 0
					|| bestTable.getProgramProgress() < 1) {
				HashMap<Integer, String> gradeLevToNameMap = new HashMap<Integer, String>();
				HashMap<String, HashMap<String, String>> errInfo = new HashMap<String, HashMap<String, String>>();
				logger.error("--------------------------排课失败的科目信息-----------------------------------");
				for (String errorInfo : bestTable.getErrorInfos()) {
					logger.error(errorInfo);
					String[] errs = errorInfo.split(":");
					Long sid = Long.parseLong(errs[3]);
					int glevel = Integer.parseInt(errs[4]);
					if (sid <= 3) {
						System.out.println("[智能排课]主课未完成班级：" + errs[2]);

					}
					if (errInfo.containsKey(errs[0])) {

						errInfo.get(errs[0]).put(errs[1], errs[2]);
						gradeLevToNameMap.put(glevel, errs[0]);
					} else {
						HashMap<String, String> classs = new HashMap<String, String>();
						classs.put(errs[1], errs[2]);
						errInfo.put(errs[0], classs);
					}
				}
				StringBuffer sb = new StringBuffer();
				DecimalFormat df = new DecimalFormat("0.00");
				String rate = df.format(bestTable.getProgramProgress() * 100)
						+ "%";
				if (bestTable.getProgramProgress() >= 1
						&& bestTable.getErrorInfos().size() > 0) {

					sb.append("智能编排已完成 ,其中以下班级课程需到微调页面进行调整:|");
				} else {
					sb.append("智能编排已完成,完成率为：" + rate + ",其中以下班级课程需到微调页面进行调整:|");

				}
				for (int i = 0; i < 30; i++) {
					if (!gradeLevToNameMap.containsKey(i)) {
						continue;
					}
					String grade = gradeLevToNameMap.get((Integer) i);
					HashMap<String, String> classs = errInfo.get(grade);
					List<JSONObject> list = new ArrayList<JSONObject>();
					for (Iterator<String> cla = classs.keySet().iterator(); cla
							.hasNext();) {
						JSONObject obj = new JSONObject();
						String cid = cla.next();
						obj.put("cName", classs.get(cid));
						obj.put("cid", cid);

						list.add(obj);
					}
					try {
						list = (List<JSONObject>) Sort.sort(
								SortEnum.ascEnding0rder, list, "cName");
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					// list = ScoreUtil
					// .sorStuScoreList(list, "cid", "asc", "", "");
					sb.append(grade);

					for (int j = 0; j < list.size(); j++) {
						if (j < list.size() - 1) {
							sb.append(list.get(j).getString("cName") + ",");
						} else {
							sb.append(list.get(j).getString("cName") + ";|");
						}
					}

				}

				rsMsg = sb.toString();
				rsMsg = rsMsg.substring(0, rsMsg.length() - 2) + ".";

				String[] gradeErrs = rsMsg.split("\\|");

			}
			timetableService.updateAutoArrangeResult(timetableId, schoolId,
					rsMsg);
			this.updateArrangeProgress(session.getId(), 2, 100, rsMsg);
		}

	}

	/**
	 * 组装任务数据
	 * 
	 * @param arrangeGrid
	 * @param schDataDic
	 * @param maxDaysForWeek
	 * @param halfAtLastDay
	 * @param gradeSets
	 * @param taskList
	 * @param taskTeacherMap
	 * @param taskGroupMap
	 * @param classTaskTeacherMap
	 */
	@SuppressWarnings("unchecked")
	private void generateTaskMapsByList(ScheduleTable arrangeGrid,
			ScheduleRule schRule, int maxDaysForWeek, boolean halfAtLastDay,
			Map<String, JSONObject> gradeSets, List<JSONObject> taskList,
			HashMap<String, JSONObject> taskTeacherMap,
			HashMap<String, JSONObject> taskGroupMap,
			HashMap<String, List<String>> classTaskTeacherMap) {

		ScheduleDatadic schDataDic = schRule.getScheduleDatadic();
		// 构建排课结构
		List<ScheduleTaskGroup> unfinishTaskGroups = new ArrayList<ScheduleTaskGroup>();
		List<ScheduleTask> unfinishTasks = new ArrayList<ScheduleTask>();
		HashMap<String, List<String>> taskIdTeacherList = new HashMap<String, List<String>>();
		HashMap<String, ScheduleTask> taskIdTask = new HashMap<String, ScheduleTask>();
		ScoreUtil.sorStuScoreList(taskList, "TeacherId", "asc", "", "");
		// 用于控制教师课时平均
		Map<String, JSONObject> teacherParam = new HashMap<String, JSONObject>();
		HashMap<String, ScheduleTask> classCourseTaskMap = new HashMap<String, ScheduleTask>();
		for (JSONObject task : taskList) {
			String taskId = task.getString("TaskId");
			String teacherId = task.getString("TeacherId");
			String courseId = task.getString("CourseId");
			String classId = task.getString("ClassId");
			String synj = schDataDic.getGradeSynjByClassId(classId);
			String gradeLevel = schDataDic.getGradeLevelByClassId(classId);
			if (gradeLevel == null || synj == null
					|| gradeSets.get(synj) == null) {
				continue;
			}
			JSONObject gradeSet = gradeSets.get(synj);
			int AMLessonNum = gradeSet.getIntValue("AMLessonNum");
			int PMLessonNum = gradeSet.getIntValue("PMLessonNum");
			double WeekNum = task.getDouble("WeekNum");
			if (WeekNum == 0) {
				continue;
			}
			// 放入教师课时平均参数
			JSONObject tep = new JSONObject();
			if (teacherParam.containsKey(teacherId)) {
				tep = teacherParam.get(teacherId);
				int lastmaxday = tep.getIntValue("maxDays");
				double lastTaskNum = tep.getDoubleValue("totalTaskNum");
				if (maxDaysForWeek > lastmaxday) {

					tep.put("maxDays", maxDaysForWeek);
				}
				int lastmaxles = tep.getIntValue("maxLessons");
				if ((AMLessonNum + PMLessonNum) > lastmaxles) {

					tep.put("maxDays", AMLessonNum + PMLessonNum);
				}
				tep.put("totalTaskNum", (lastTaskNum + WeekNum));
			} else {
				tep.put("maxDays", maxDaysForWeek);
				tep.put("maxLessons", AMLessonNum + PMLessonNum);
				tep.put("totalTaskNum", WeekNum);
				teacherParam.put(teacherId, tep);
			}
			// 任务去重等
			int NearNum = task.getIntValue("NearNum");
			if (!taskIdTeacherList.containsKey(taskId)) {
				// 此处无重复任务
				List<String> teacherIds = new ArrayList<String>();
				CourseLevel courseLevel = schRule.getCourseLevelByGradeCourse(
						gradeLevel, courseId);
				CourseLevel orCourseLevel = schRule.getOrCourseLevelByGradeCourse(
						gradeLevel, courseId);
				// 创建任务
				ScheduleTask schTask = new ScheduleTask();

				schTask.setClassId(classId);
				schTask.setArrangedTaskNum(0);
				schTask.setClassAmNum(AMLessonNum);
				schTask.setClassPmNum(PMLessonNum);
				
				schTask.setCourseId(courseId);
				
				
				schTask.setCourseLevel(courseLevel);
				//扩展科目规则 尽量上午（含下午课程）与只尽量上午
				if(orCourseLevel.equals(CourseLevel.AmButNotAll)){
					schTask.setOrCourseLevel(CourseLevel.AmFirst);
					schTask.setCourseLevel(CourseLevel.AmFirst);
					schTask.setAllAmFirst(false);
				}else{
					schTask.setOrCourseLevel(orCourseLevel);
				}
				schTask.setCourseOrder(0);
				schTask.setGradeId(synj);
				schTask.setGradeLevel(gradeLevel);
				schTask.setHalfAtLast(halfAtLastDay);
				schTask.setMaxDays(maxDaysForWeek);
				schTask.setNeedCourseNumControl(!(WeekNum > maxDaysForWeek));
				schTask.setSpNum(NearNum);
				schTask.setTaskId(taskId);
				schTask.setTaskNum(WeekNum);
				//对于设置了各节次均可的课程设置上午排课最大比例
				courseLevel = schTask.getCourseLevel();
				orCourseLevel = schTask.getOrCourseLevel();

				double amMinPercent = schRule
						.getCourseAmMinPercentByGradeCourse(gradeLevel,
								courseId,schTask);
				schTask.setAmMinPercent(amMinPercent);
				double amMaxPercent = schRule
						.getCourseAmMaxPercentByGradeCourse(gradeLevel,
								courseId,schTask);
				double pmMaxCourseNum = schRule
						.getCoursePmMaxNumByGradeCourse(gradeLevel,
								courseId,schTask);
				schTask.setAmMaxPercent(amMaxPercent);
				schTask.setPmMaxNum(pmMaxCourseNum);
//				}
				classCourseTaskMap.put(classId + "_" + courseId, schTask);
				taskIdTask.put(taskId, schTask);
				if (teacherId != null && teacherId.trim().length() > 0) {

					teacherIds.add(teacherId);
					task.put("TeacherIds", teacherIds);
					taskIdTeacherList.put(taskId, teacherIds);

				} else {
					unfinishTasks.add(schTask);
				}

			} else {
				if (teacherId != null && teacherId.trim().length() > 0) {

					List<String> teacherIds = taskIdTeacherList.get(taskId);
					if (!teacherIds.contains(teacherId)) {

						teacherIds.add(teacherId);
					}
				}
			}
		}
		for (Iterator<String> taskKey = taskIdTeacherList.keySet().iterator(); taskKey
				.hasNext();) {
			String taskId = taskKey.next();
			List<String> teacherArr = taskIdTeacherList.get(taskId);
			String teacherIds = ArrayUtils.toString(teacherArr);
			ScheduleTask schTask = taskIdTask.get(taskId);
			// 放入每个班级、课程的老师
			classTaskTeacherMap.put(
					schTask.getClassId() + "_" + schTask.getCourseId(),
					teacherArr);
			schTask.setTeacherIds(teacherArr);
			String synj = schTask.getGradeId();
			String courseId = schTask.getCourseId();
			double WeekNum = schTask.getTaskNum();
			// 写错了 重新写 键应包含teacherIds
			String groupKey = synj + "," + courseId + "," + WeekNum + ","
					+ teacherIds;

			JSONObject taskGroupJSON = new JSONObject();
			List<ScheduleTask> groupTaskList = new ArrayList<ScheduleTask>();
			if (taskGroupMap.containsKey(groupKey)) {
				taskGroupJSON = taskGroupMap.get(groupKey);
				groupTaskList = (List<ScheduleTask>) taskGroupJSON
						.get("groupTaskList");
			} else {
				taskGroupJSON.put("teacherIds", teacherArr);
			}
			groupTaskList.add(schTask);

			taskGroupJSON.put("groupTaskList", groupTaskList);
			taskGroupJSON.put("courseId", courseId);
			taskGroupJSON.put("amNum", schTask.getClassAmNum());
			taskGroupJSON.put("pmNum", schTask.getClassPmNum());
			taskGroupJSON.put("taskNum", WeekNum);
			if (taskGroupJSON.containsKey("maxSpNum")
					&& taskGroupJSON.getIntValue("maxSpNum") < schTask
							.getSpNum()) {
				taskGroupJSON.put("maxSpNum", schTask.getSpNum());
			}
			taskGroupMap.put(groupKey, taskGroupJSON);
		}

		for (Iterator<String> tkey = taskGroupMap.keySet().iterator(); tkey
				.hasNext();) {
			String keyVal = tkey.next();
			JSONObject taskGroupJSON = taskGroupMap.get(keyVal);
			List<ScheduleTask> groupTaskList = (List<ScheduleTask>) taskGroupJSON
					.get("groupTaskList");
			List<String> teacherIds = (List<String>) taskGroupJSON
					.get("teacherIds");
			// 任务数只有一个班时 不创建任务组
			if (groupTaskList.size() == 1) {
				unfinishTasks.add(groupTaskList.get(0));
			} else {
				ScheduleTaskGroup schTaskGroup = new ScheduleTaskGroup();
				String groupId = UUIDUtil.getUUID();
				for (ScheduleTask task : groupTaskList) {
					task.setTaskGroupId(groupId);
				}
				schTaskGroup.setAvgTaskNum(taskGroupJSON
						.getDoubleValue("taskNum"));
				schTaskGroup.setChildTasks(groupTaskList);
				schTaskGroup.setClassAmNum(taskGroupJSON.getIntValue("amNum"));
				schTaskGroup.setClassPmNum(taskGroupJSON.getIntValue("pmNum"));
				schTaskGroup.setCourseId(taskGroupJSON.getString("courseId"));
				schTaskGroup
						.setGradeLevel(groupTaskList.get(0).getGradeLevel());
				schTaskGroup.setGradeId(groupTaskList.get(0).getGradeId());
				schTaskGroup.setCourseLevel(schRule
						.getCourseLevelByGradeCourse(
								schTaskGroup.getGradeLevel(),
								schTaskGroup.getCourseId()));
				// schTaskGroup.setCourseOrder(courseOrder);
				// schTaskGroup.setCurrentLc(0);
				schTaskGroup.setGroupMaxSpNum(taskGroupJSON
						.getIntValue("maxSpNum"));
				schTaskGroup.setGroupTaskNum(schTaskGroup.getAvgTaskNum()
						* groupTaskList.size());
				schTaskGroup.setHalfAtLast(groupTaskList.get(0).isHalfAtLast());
				schTaskGroup.setMaxDays(groupTaskList.get(0).getMaxDays());
				schTaskGroup.setNeedCourseNumControl(groupTaskList.get(0)
						.isNeedCourseNumControl());
				schTaskGroup.setTaskGroupId(groupId);
				schTaskGroup.setTeacherIds(teacherIds);
				schTaskGroup.init(schRule);

				unfinishTaskGroups.add(schTaskGroup);
			}
		}
		

		Map<String, Map<Integer, Integer>> teacherDayNumMap = new HashMap<String, Map<Integer, Integer>>();
		// 根据规则位置数量控制每个教师最大上课数-每天
		for (String teacherId : teacherParam.keySet()) {
			if (teacherId == null) {
				continue;
			}
			JSONObject teaJSON = teacherParam.get(teacherId);
			int maxls = teaJSON.getIntValue("maxLessons");
			double maxTaskNum = teaJSON.getDoubleValue("totalTaskNum");
			Map<Integer, Integer> teaDayMap = new HashMap<Integer, Integer>();
			if (schRule.getIsTeachingSync() == 1) {
				int avgnum = (int) Math.ceil(maxTaskNum / maxDaysForWeek);
				if (avgnum <= 5) {
					avgnum = 5;
				}
				for (int i = 0; i < maxDaysForWeek; i++) {
					teaDayMap.put(i, avgnum);
				}
			} else {
				SchRuleTeacher sch = schRule.getRuleTeachers().get(teacherId);
				if (sch != null && sch.getPositions().size() > 0) {
					HashMap<Integer, Integer> dayRuleNum = new HashMap<Integer, Integer>();
					int sumpos = 0;
					for (int i = 0; i < maxDaysForWeek; i++) {
						int unpos = 0;
						for (int j = 0; j < maxls; j++) {
							if (sch.getPositions().containsKey(i + "," + j)) {
								if (sch.getPositions().get(i + "," + j)
										.getRuleType() == 0) {
									unpos++;
									sumpos++;
								}
							}
						}
						dayRuleNum.put(i, unpos);
					}
					int allCanArrange = maxDaysForWeek * maxls - sumpos;
					// 开始计算没规则的天
					for (int day : dayRuleNum.keySet()) {
						int dayCanArrange = maxls - dayRuleNum.get(day);
						int dayAvg = (int) Math
								.ceil((dayCanArrange * 1f / allCanArrange)
										* maxTaskNum);
						if (dayCanArrange * maxTaskNum % allCanArrange == 0) {
							dayAvg++;
						}
						teaDayMap.put(day, dayAvg);
					}
				} else {
					int avgnum = (int) Math.ceil(maxTaskNum / maxDaysForWeek) + 1;
					if (maxTaskNum % maxDaysForWeek == 0) {
						avgnum++;
					}
					for (int i = 0; i < maxDaysForWeek; i++) {
						teaDayMap.put(i, avgnum);
					}
				}
			}
			//

			teacherDayNumMap.put(teacherId, teaDayMap);
		}
		schRule.setTeacherDayNumMap(teacherDayNumMap);
		unfinishTaskGroups = SortCourseGroup(unfinishTaskGroups);
		arrangeGrid.setUnfinishTaskGroups(unfinishTaskGroups);
		arrangeGrid.setUnfinishTasks(unfinishTasks);
		arrangeGrid.setClassCourseTaskMap(classCourseTaskMap, schRule);
	}

	private List<ScheduleTaskGroup> SortCourseGroup(
			List<ScheduleTaskGroup> unfinishTaskGroups) {
		// TODO Auto-generated method stub
		List<ScheduleTaskGroup> temp1 = new ArrayList<ScheduleTaskGroup>();
		List<ScheduleTaskGroup> temp2 = new ArrayList<ScheduleTaskGroup>();

		for (ScheduleTaskGroup stg : unfinishTaskGroups) {
			if (stg.getAvgTaskNum() >= stg.getMaxDays()) {
				temp1.add(stg);
			} else {
				temp2.add(stg);
			}
		}
		try{
			
			System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
			Collections.sort(temp2, courseCompare);
			Collections.reverse(temp2);
		}catch (Exception e){
			e.printStackTrace();
		}

		unfinishTaskGroups = new ArrayList<ScheduleTaskGroup>();
		unfinishTaskGroups.addAll(temp1);
		unfinishTaskGroups.addAll(temp2);
		return unfinishTaskGroups;
	}

	/**
	 * 更新排课进度
	 * 
	 * @param session
	 *            当前请求的session对象
	 * @param code
	 *            0 开始 1 正在编排 2 成功结束 -1 失败结束
	 * @param progress
	 *            进度值，即百分比的数值
	 * @param msg
	 *            当前进度的名称/处理消息
	 */
	@Override
	public void updateArrangeProgress(String jsessionID, int code,
			double progress, String msg) {

		JSONObject rs = new JSONObject();
		rs.put("arrangeCode", code);
		rs.put("arrangeProgress", progress);
		rs.put("arrangeMsg", msg);

		Object arrangeKey = "timetable." + jsessionID
				+ ".courseSmtArrange.progress";
		try {
			redisOperationDAO.set(arrangeKey, rs,
					CacheExpireTime.temporaryDataDefaultExpireTime
							.getTimeValue());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 合并已排结果与教师规则
	 * 
	 * @param ruleTeachers
	 * @param otherGrade
	 * @param schDataDic
	 * @param schRuleGround
	 * @param gradeIds
	 * @param ruleConflict 
	 * @return
	 */
	private Map<String, SchRuleTeacher> MergeRuleAndArrangeResult(
			Map<String, SchRuleTeacher> ruleTeachers,
			List<JSONObject> otherGrade,
			Map<String, SchRuleGround> schRuleGround,
			ScheduleDatadic schDataDic, List<String> gradeIds, ScheduleRule ruleConflict) {
		Map<String,Map<Integer,Integer>> teacherDayArMap = new HashMap<String, Map<Integer,Integer>>();
		// TODO Auto-generated method stub
		for (JSONObject arrange : otherGrade) {
			String teacherId = arrange.getString("TeacherId");
			int day = arrange.getIntValue("DayOfWeek");
			int lesson = arrange.getIntValue("LessonOfDay");
			String classId = arrange.getString("ClassId");
			String gradeId = schDataDic.getGradeSynjByClassId(classId);
			String subjectId = arrange.getString("SubjectId");
			if (gradeIds.contains(gradeId)) {
				continue;
			}
			SchRulePosition rulePosition = new SchRulePosition();

			rulePosition.setDay(day);
			rulePosition.setLesson(lesson);
			rulePosition.setRuleType(0);

			if (ruleTeachers.containsKey(teacherId)) {

				ruleTeachers.get(teacherId).addPosition(rulePosition);

			} else {
				SchRuleTeacher rt = new SchRuleTeacher();
				rt.setTeacherId(teacherId);
				List<SchRulePosition> positions = new ArrayList<SchRulePosition>();
				positions.add(rulePosition);
				rt.setPositions(positions);

				ruleTeachers.put(teacherId, rt);
			}
			ruleTeachers.get(teacherId).addArrangedPosition(day);

			if (schRuleGround.containsKey(gradeId + "_" + subjectId)) {
				SchRuleGround sg = schRuleGround.get(gradeId + "_" + subjectId);
				sg.addArrangedPosition(day, lesson);
			}
			
			if(teacherDayArMap.containsKey(teacherId)){
				Map<Integer,Integer> map = teacherDayArMap.get(teacherId);
				if(map.containsKey(day)){
					map.put(day, map.get(day)+1 );
				}else{
					map.put(day, 1);
				}
			}else{
				Map<Integer,Integer> map = new HashMap<Integer, Integer>();
				map.put(day, 1);
			}
		}
		ruleConflict.setTeacherDayArNumMap(teacherDayArMap);
		return ruleTeachers;
	}
}
