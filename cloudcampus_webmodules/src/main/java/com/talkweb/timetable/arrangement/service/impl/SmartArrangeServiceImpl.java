package com.talkweb.timetable.arrangement.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.jgap.Configuration;
import org.jgap.Gene;
import org.jgap.Genotype;
import org.jgap.IChromosome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.Account;
import com.talkweb.accountcenter.thrift.Classroom;
import com.talkweb.accountcenter.thrift.Grade;
import com.talkweb.accountcenter.thrift.LessonInfo;
import com.talkweb.accountcenter.thrift.School;
import com.talkweb.accountcenter.thrift.T_GradeLevel;
import com.talkweb.api.message.utils.MessageNoticeTypeEnum;
import com.talkweb.api.message.utils.MessageNoticeUserTypeEnum;
import com.talkweb.api.message.utils.MessageServiceEnum;
import com.talkweb.api.message.utils.MessageSmsEnum;
import com.talkweb.api.message.utils.MessageStatusEnum;
import com.talkweb.base.common.CacheExpireTime;
import com.talkweb.common.tools.BeanTool;
import com.talkweb.common.tools.GrepUtil;
import com.talkweb.common.tools.StringUtil;
import com.talkweb.common.tools.UUIDUtil;
import com.talkweb.common.tools.sort.Sort;
import com.talkweb.common.tools.sort.SortEnum;
import com.talkweb.commondata.dao.RedisOperationDAO;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.commondata.service.MotanService;
import com.talkweb.scoreManage.action.ScoreUtil;
import com.talkweb.timetable.arrangement.algorithm.ArrangeConfiguration;
import com.talkweb.timetable.arrangement.algorithm.ArrangeFitnessFunction;
import com.talkweb.timetable.arrangement.algorithm.ArrangeMutationOperator;
import com.talkweb.timetable.arrangement.algorithm.CourseGene;
import com.talkweb.timetable.arrangement.algorithm.RuleConflict;
import com.talkweb.timetable.arrangement.domain.ArrangeClass;
import com.talkweb.timetable.arrangement.domain.ArrangeCourse;
import com.talkweb.timetable.arrangement.domain.ArrangeGrid;
import com.talkweb.timetable.arrangement.domain.ArrangeTeacher;
import com.talkweb.timetable.arrangement.domain.RuleClassGroup;
import com.talkweb.timetable.arrangement.domain.RuleCourse;
import com.talkweb.timetable.arrangement.domain.RulePosition;
import com.talkweb.timetable.arrangement.domain.RuleResearchMeeting;
import com.talkweb.timetable.arrangement.domain.RuleTeacher;
import com.talkweb.timetable.arrangement.domain.TCRCATemporaryAdjustGroupInfo;
import com.talkweb.timetable.arrangement.domain.TCRCATemporaryAdjustGroupParam;
import com.talkweb.timetable.arrangement.domain.TCRCATemporaryAdjustRecord;
import com.talkweb.timetable.arrangement.domain.TCRCATemporaryAdjustResult;
import com.talkweb.timetable.arrangement.exception.ArrangeTimetableException;
import com.talkweb.timetable.arrangement.service.ArrangeDataService;
import com.talkweb.timetable.arrangement.service.SmartArrangeService;
import com.talkweb.timetable.dao.TimetableDao;
import com.talkweb.timetable.service.TimetableService;

/**
 * 智能排课算法核心业务逻辑
 * 
 * @author Li xi yuan
 *
 */
@Service
public class SmartArrangeServiceImpl implements SmartArrangeService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private ArrangeDataService arrangeDataService;
	@Autowired
	private TimetableService timetableService;
	@Autowired
	private AllCommonDataService commonService;
	@Autowired
	private TimetableDao timetableDao;

	@Resource(name="redisOperationDAOSDRTempDataImpl")
 	private RedisOperationDAO redisOperationDAO;
	// 种群大小
	private int populationSize = 10;
	// 最大进化次数
	private int maxEvolution = 20;

	/**
	 * 开始自动排课算法
	 * 
	 * @param session
	 *            当前请求的session对象
	 * @param school
	 *            学校ID
	 * @param timetableId
	 *            课表ID
	 * @param gradeIds
	 *            年级ID
	 * @throws ArrangeTimetableException
	 */
	public void autoArrangeTimeTable(HttpSession session, School school,
			String timetableId, List<String> gradeIds, JSONObject runParams)
			throws ArrangeTimetableException {
		//
		// schoolId = "1001";
		String schoolId = school.getId() + "";

		// 冲突规则
		RuleConflict ruleConflict = new RuleConflict();

		// 课表参数信息
		JSONObject timetableInfo = this.timetableService
				.getArrangeTimetableInfo(school.getId() + "", timetableId);
		if (timetableInfo == null) {
			this.updateArrangeProgress(session.getId(),  -1, 0,
					"编排失败，您选择的课表不存在");
			throw new ArrangeTimetableException("您选择的课表竟然不存在哦");
		}
		School sch = (School) session.getAttribute("school");
		String schoolYear = timetableInfo.getString("SchoolYear");
		String TermName = timetableInfo.getString("TermName");
		// 全校 -使用年级-年级map
		ruleConflict.setGradesDic(this.arrangeDataService.getGradesDic(
				schoolYear, sch,TermName));
		List<Long> clearClassIds = new ArrayList<Long>();
		for (String grade : gradeIds) {
			int njdm = Integer.parseInt(commonService.ConvertSYNJ2NJDM(grade,
					schoolYear));

			Grade gd = ruleConflict.getGradeByLevel(njdm);
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
		this.timetableService.clearTimetable(schoolId, timetableId, subsql,schoolYear,TermName);

		int maxDaysForWeek = timetableInfo.getIntValue("MaxDaysForWeek");
		boolean halfAtLastDay = timetableInfo.getBooleanValue("halfAtLastDay");
		String termName = timetableInfo.getString("TermName");
		String termInfoId = schoolYear.concat(termName);

		// 建立所有班级课表容器
		ArrangeGrid arrangeGrid = new ArrangeGrid();
		arrangeGrid.setSchoolId(schoolId);
		arrangeGrid.setTimetableId(timetableId);

		ruleConflict.setSchoolYear(schoolYear);
		ruleConflict.setTermName(termName);

		// 科目规则
		this.updateArrangeProgress(session.getId(),  0, 5, "加载科目规则");
		Map<String, RuleCourse> ruleCourses = this.arrangeDataService
				.getRuleCourses(schoolId, timetableId);

		// 教师规则
		this.updateArrangeProgress(session.getId(), 0, 10,
				"加载教师规则");
		Map<String, RuleTeacher> ruleTeachers = this.arrangeDataService
				.getRuleTeachers(schoolId, timetableId);
		// 教师已排课程
		List<JSONObject> otherGrade = this.arrangeDataService
				.getRuleTeachersInHasArrage(schoolId, timetableId);

		ruleTeachers = MergeRuleAndArrangeResult(ruleTeachers, otherGrade);
		// 合班
		this.updateArrangeProgress(session.getId(),    0, 15,
				"加载合班规则");
		List<RuleClassGroup> ruleClassGroups = this.arrangeDataService
				.getRuleClassGroups(schoolId, timetableId);

		// 单双周
		JSONObject courseGroup = this.timetableService.getEvenOddWeekGroupList(
				schoolId, timetableId);

		// 教师的教研活动
		this.updateArrangeProgress(session.getId(),   0, 20,
				"加载教研活动规则");
		List<RuleResearchMeeting> ruleResearchMeetings = this.arrangeDataService
				.getRuleResearchMeetings(schoolId, timetableId);

		this.updateArrangeProgress(session.getId(),   0, 25,
				"加载基础数据");

		ruleConflict.setRuleCourses(ruleCourses);
		ruleConflict.setRuleTeachers(ruleTeachers);
		ruleConflict.setRuleClassGroups(ruleClassGroups);
		ruleConflict.setRuleResearchMeetings(ruleResearchMeetings);

		// 从接口读取基础数据字典

		// 使用classid-班级map
		ruleConflict.setClassroomsDic(this.arrangeDataService.getClassRoomsDic(
				school, null, termInfoId));
		// lessonId-LessonInfo map
		ruleConflict.setCoursesDic(this.arrangeDataService
				.getCoursesDic(school, termInfoId));
		// teacherId-教师 map
		ruleConflict.setTeachersDic(this.arrangeDataService
				.getTeachersDic(school, termInfoId));
		ruleConflict.setRuleGrounds(this.arrangeDataService.getRuleGrounds(school.getId()+"",timetableId));

		// 课表内 -使用年级-T_CRCA_GradeSet map
		Map<String, JSONObject> gradeSets = new HashMap<String, JSONObject>();

		List<JSONObject> gradeSetList = this.timetableService.getGradeSetList(
				schoolId, timetableId, gradeIds);
		for (JSONObject gradeSet : gradeSetList) {
			String gradeId = gradeSet.getString("GradeId");
			gradeSets.put(gradeId, gradeSet);
		}
		// 班级id-synj映射 --用于arrangeCourse
		HashMap<String, String> classSynjMap = new HashMap<String, String>();
		HashMap<String, String> classGradeLevMap = new HashMap<String, String>();
		HashMap<String, String> gradeIdSynjMap = new HashMap<String, String>();
		// 根据选择的年级取年级下所有的班级
		List<Classroom> classInfos = this.timetableService.getClassByGradeIds(
				schoolId, termInfoId, gradeIds);
		List<String> classIds = new ArrayList<String>();
		for (Classroom classInfo : classInfos) {
			// 年级代码
			String gradeId = String.valueOf(classInfo.getGradeId());
			if (gradeId == null) {
				continue;
			}
			Grade grade = ruleConflict.findGradeInfoFromSrc(gradeId);
			if (grade == null) {
				continue;
			}
			String synj = this.commonService.getSynjByGrade(grade,
					ruleConflict.getSchoolYear());
			String classId = String.valueOf(classInfo.getId());
			T_GradeLevel gradeLevel = ruleConflict.getGradesDic().get(synj)
					.getCurrentLevel();
			gradeIdSynjMap.put(synj, gradeLevel.getValue() + "");

			classIds.add(classId);
			// 对应的年级参数
			JSONObject gradeSet = gradeSets.get(synj);
			if (gradeSet == null) {
				continue;
			}
			int amLessonNum = gradeSet.getIntValue("AMLessonNum");
			int pmLessonNum = gradeSet.getIntValue("PMLessonNum");

			ArrangeClass arrangeClass = new ArrangeClass(synj, classId,
					maxDaysForWeek, amLessonNum, pmLessonNum, ruleConflict);
			arrangeClass.setGradeId(synj);
			Grade gradeInfo = ruleConflict.findGradeInfo(synj);
			if (gradeInfo != null) {
				String gradeName = this.arrangeDataService
						.getGradeName(gradeInfo);
				arrangeClass.setGradeName(gradeName);
			}

			arrangeClass.setClassId(classId);
			classSynjMap.put(classId, synj);
			classGradeLevMap.put(classId, gradeInfo.getCurrentLevel()
					.getValue() + "");
			Classroom classroom = ruleConflict.findClassInfo(classId);
			if (classroom != null) {
				arrangeClass.setClassName(classroom.getClassName());
			}

			arrangeClass.setHalfAtLastDay(halfAtLastDay);
			// 将班级课表模型 放入总容器
			arrangeGrid.addArrangeClass(arrangeClass);
			//添加年级上午最大排课数
			ruleConflict.addToGradeLevelMaxAmNumMap(String.valueOf(gradeLevel.getValue()), amLessonNum);
			ruleConflict.addToGradeLevelMaxPmNumMap(String.valueOf(gradeLevel.getValue()), pmLessonNum);

		}
		// 重构ruleCourse的key
		// Map<String, RuleCourse> ruleCourses2 = ruleCourses;
		// for(Iterator<String> it =
		// ruleCourses.keySet().iterator();it.hasNext();){
		//
		// String key = it.next();
		// ruleCourses2.put(gradeIdSynjMap.get(key), ruleCourses.get(key));
		// }
		// ruleConflict.setRuleCourses(ruleCourses2);
		this.updateArrangeProgress(session.getId(),   0, 30,
				"加载教学任务");
		// 教师的教学任务 teacherId,arrangeTeacher map
		Map<String, ArrangeTeacher> taskTeachers = new HashMap<String, ArrangeTeacher>();
		// classId_lessonId -- arrangeCourse map
		Map<String, ArrangeCourse> taskCourses = new HashMap<String, ArrangeCourse>();
		// 根据班级取教学任务
		List<JSONObject> taskList = this.timetableService.getTaskByTimetable(
				schoolId, timetableId, classIds);
		if (taskList == null || taskList.size() == 0) {
			this.updateArrangeProgress(session.getId(), -1, 0,
					"编排失败，所选的年级下没有教学任务");
			return;
		}

		for (JSONObject task : taskList) {
			// 这里会自动去掉重复的情况
			String classId = task.getString("ClassId");
			String courseId = task.getString("CourseId");
			// 周课时
			double weekNum = task.getDoubleValue("WeekNum");
			// 连排次数
			int nearNum = task.getIntValue("NearNum");
			String teacherId = task.getString("TeacherId");
			String taskCourseKey = classId + "_" + courseId;
			ArrangeTeacher teacher = new ArrangeTeacher();
			if ( taskTeachers.containsKey(teacherId) ) {
				teacher = taskTeachers.get(teacherId);
			} else  {
				teacher.setTeacherId(teacherId);
				Account teacherUser = ruleConflict.findTeacherInfo(teacherId);
				if (teacherUser != null) {
					teacher.setTeacherName(teacherUser.getName());
				}

				taskTeachers.put(teacherId, teacher);
			}

			ArrangeCourse arrangeCourse = new ArrangeCourse();
			if (!taskCourses.containsKey(taskCourseKey)) {
				// 未安排科目
				arrangeCourse.setClassId(classId);
				arrangeCourse.setGradeId(classSynjMap.get(classId));
				arrangeCourse.setGradeLev(classGradeLevMap.get(classId));
				arrangeCourse.setUnitSize(nearNum);
				arrangeCourse.setCourseId(courseId);
				LessonInfo courseInfo = ruleConflict.findCourseInfo(courseId);
				if (courseInfo != null&&courseInfo.getName().trim().length()>0) {
					arrangeCourse.setCourseName(courseInfo.getName());
				}

				// 0：尽量上午，1：各节次均可，2:尽量下午
				if (ruleCourses.containsKey(gradeIdSynjMap.get(classSynjMap
						.get(classId)) + "_" + courseId)) {
					// 科目规则
					RuleCourse ruleCourse = ruleCourses.get(gradeIdSynjMap
							.get(classSynjMap.get(classId)) + "_" + courseId);
					arrangeCourse.setCourseLevel(ruleCourse.getCourseLevel());
				} else {
					if (Integer.valueOf(courseId) <= 3) {
						arrangeCourse.setCourseLevel(0);
					} else {

						double maxTask = arrangeCourse.getTaskLessons();
						int ft = (int) (maxTask * 10);
						if (ft % 10 == 0) {

							arrangeCourse.setCourseLevel(1);
						} else {

							arrangeCourse.setCourseLevel(2);
						}
					}
				}
				arrangeCourse.setTaskLessons(weekNum);

				taskCourses.put(taskCourseKey, arrangeCourse);

				ArrangeClass ac = arrangeGrid.getArrangeClass(arrangeCourse
						.getClassId());
				int maxNearNum = nearNum + ac.getMaxNearNum();
				ac.setMaxNearNum(maxNearNum);
			} else {
				arrangeCourse = taskCourses.get(taskCourseKey);

			}
			arrangeCourse.addTeacher(teacher.getTeacherId(), teacher);

			// 设置ArrangeTeacher的课程安排
			teacher.addCourse(arrangeCourse);

		}

		this.updateArrangeProgress(session.getId(), 0, 33,
				"加载预排课数据");

		// 读取预排课安排
		List<JSONObject> advanceArrangeList = this.timetableService
				.getAdvanceArrangeList(schoolId, timetableId);

		// 转换
		List<ArrangeTeacher> arrangeTeachers = new ArrayList<ArrangeTeacher>();
		for (ArrangeTeacher arrangeTeacher : taskTeachers.values()) {
			arrangeTeachers.add(arrangeTeacher);
		}

		try {

			this.startArrangeTimeTable(session, arrangeGrid, arrangeTeachers,
					ruleConflict, taskCourses, advanceArrangeList, courseGroup );

			// 控制台输出结果
			// this.print(arrangeGrid,arrangeTeachers);

			// 将结果保存到数据库中
			this.updateArrangeProgress(session.getId(),  1, 95,
					"保存排课结果");
			this.arrangeDataService.saveArrangeTimeTable(arrangeGrid);

			String rsMsg = "排课成功结束";
			if (arrangeGrid.getErrorInfos().size() > 0) {
				HashMap<String, HashMap<String, String>> errInfo = new HashMap<String, HashMap<String, String>>();
				logger.error("--------------------------排课失败的科目信息-----------------------------------");
				for (String errorInfo : arrangeGrid.getErrorInfos()) {
					logger.error(errorInfo);
					String[] errs = errorInfo.split(":");
					if (errInfo.containsKey(errs[0])) {

						errInfo.get(errs[0]).put(errs[1], errs[2]);
					} else {
						HashMap<String, String> classs = new HashMap<String, String>();
						classs.put(errs[1], errs[2]);
						errInfo.put(errs[0], classs);
					}
				}
				StringBuffer sb = new StringBuffer();
				sb.append("智能编排已完成，其中以下班级课程需到微调页面进行调整:|");
				for (Iterator<String> it = errInfo.keySet().iterator(); it
						.hasNext();) {
					String grade = it.next();
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
					list = ScoreUtil
							.sorStuScoreList(list, "cid", "asc", "", "");
					sb.append(grade);

					for (int j = 0; j < list.size(); j++) {
						if (j < list.size() - 1) {

							sb.append(list.get(j).getString("cName") + "班,");
						} else {

							sb.append(list.get(j).getString("cName") + "班;|");
						}
					}

				}

				rsMsg = sb.toString();
				rsMsg = rsMsg.substring(0, rsMsg.length() - 2) + ".";
			}
			timetableService.updateAutoArrangeResult(timetableId, schoolId, rsMsg);
			this.updateArrangeProgress(session.getId(),  2, 100,
					rsMsg);

		} catch (Exception e) {
			this.updateArrangeProgress(session.getId(),  -1, 0,
					"编排失败，" + e.getMessage());
			e.printStackTrace();
		}

	}

	/**
	 * 合并已排结果与教师规则
	 * 
	 * @param ruleTeachers
	 * @param otherGrade
	 * @return
	 */
	private Map<String, RuleTeacher> MergeRuleAndArrangeResult(
			Map<String, RuleTeacher> ruleTeachers, List<JSONObject> otherGrade) {
		// TODO Auto-generated method stub
		for (JSONObject arrange : otherGrade) {
			String teacherId = arrange.getString("TeacherId");
			int day = arrange.getIntValue("DayOfWeek");
			int lesson = arrange.getIntValue("LessonOfDay");
			
			RulePosition rulePosition = new RulePosition();
			
			rulePosition.setDay(day);
			rulePosition.setLesson(lesson);
			rulePosition.setRuleType(0);
			
			if (ruleTeachers.containsKey(teacherId)) {

				ruleTeachers.get(teacherId).addPosition(rulePosition);

			}else{
				RuleTeacher rt = new RuleTeacher();
				rt.setTeacherId(teacherId);
				List<RulePosition> positions = new ArrayList<RulePosition>();
				positions.add(rulePosition);
				rt.setPositions(positions );
				
				ruleTeachers.put(teacherId, rt);
			}
		}

		return ruleTeachers;
	}

	/**
	 * @param session
	 *            当前请求的session对象
	 * @param arrangeGrid
	 *            排课的模型对象
	 * @param arrangeTeachers
	 *            有教学任务的老师对象
	 * @param ruleConflict
	 *            冲突规则对象
	 * @param taskCourses
	 *            教学任务
	 * @param advanceArrangeList
	 *            预排课
	 * @param courseGroup
	 *            单双周设置
	 * @return
	 * @throws Exception
	 */
	public ArrangeGrid startArrangeTimeTable(HttpSession session,
			ArrangeGrid arrangeGrid, List<ArrangeTeacher> arrangeTeachers,
			RuleConflict ruleConflict, Map<String, ArrangeCourse> taskCourses,
			List<JSONObject> advanceArrangeList, JSONObject courseGroup ) throws Exception {
		// 排课前校验数据合法性
		this.updateArrangeProgress(session.getId(),   0, 35,
				"校验数据合法性");
		this.checkValidSetting(arrangeGrid, arrangeTeachers, ruleConflict);

		System.out.println("开始智能排课...");
		this.updateArrangeProgress(session.getId(),   1, 40,
				"开始智能排课...");

		Configuration config = new ArrangeConfiguration();

		// 设置是否保存适应度最大的个体
		config.setPreservFittestIndividual(true);

		// 设置适应度函数
		config.setFitnessFunction(new ArrangeFitnessFunction(config,
				ruleConflict));
		// 设置变异因子
		config.addGeneticOperator(new ArrangeMutationOperator(config, 12,
				arrangeGrid, ruleConflict));

		// 创建染色体样本
		

		return arrangeGrid;
	}

	
	/**
	 * 获取单个班级所有老师课时总数
	 * 
	 * @param arrangeClass
	 * @param arrangeTeachers
	 * @return
	 */
	private double getTotalTaskLesson(ArrangeClass arrangeClass,
			List<ArrangeTeacher> arrangeTeachers) {
		String classId = arrangeClass.getClassId();

		double totalTaskLesson = 0;

		List<String> courseIds = new ArrayList<String>();
		for (ArrangeTeacher arrangeTeacher : arrangeTeachers) {
			for (ArrangeCourse arrangeCourse : arrangeTeacher.getCourses()) {

				if (arrangeCourse.getClassId().equals(classId)) {
					if (courseIds.size() == 0
							|| !courseIds.contains(arrangeCourse.getCourseId())) {
						courseIds.add(arrangeCourse.getCourseId());
						totalTaskLesson += arrangeCourse.getTaskLessons();
					}
				}

			}

		}

		return totalTaskLesson;

	}

	// 判断参数设置的合法性
	public void checkValidSetting(ArrangeGrid arrangeGrid,
			List<ArrangeTeacher> arrangeTeachers, RuleConflict ruleConflict)
			throws ArrangeTimetableException {

		Collection<ArrangeClass> classTimetables = arrangeGrid
				.getClassTimetables().values();
		for (ArrangeClass arrangeClass : classTimetables) {
			String classId = arrangeClass.getClassName();
			int maxDays = arrangeClass.getTotalMaxDays();
			int maxLesson = arrangeClass.getTotalMaxLesson();
			double totalMaxLesson = maxDays * maxLesson;
			//
			double taskLessons = this.getTotalTaskLesson(arrangeClass,
					arrangeTeachers);
			if (taskLessons > totalMaxLesson) {
				throw new ArrangeTimetableException(classId + "班级 可排课时："
						+ totalMaxLesson + " ，实际计划课时：" + taskLessons);
			}

		}

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
	public void updateArrangeProgress(String jsessionID,  int code, double progress,
			String msg) {

		JSONObject rs = new JSONObject();
		rs.put("arrangeCode", code);
		rs.put("arrangeProgress", progress);
		rs.put("arrangeMsg", msg);
		
		Object arrangeKey = "timetable."  +jsessionID
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

	// 将排课结果打印到控制台
	public void printAll(ArrangeGrid arrangeGrid, Genotype population) {

		List<IChromosome> chromosomes = population.getPopulation()
				.getChromosomes();
		for (IChromosome chromosome : chromosomes) {
			arrangeGrid.resetAllTimetable();
			Gene[] genes = chromosome.getGenes();
			for (Gene gene : genes) {
				CourseGene courseGene = (CourseGene) gene;
				String classId = courseGene.getClassId();
				ArrangeClass arrangeClass = arrangeGrid
						.getArrangeClass(classId);
				arrangeClass.setCourse(courseGene);
			}

			this.print(arrangeGrid, null);
			System.out.println("=======================================");
		}
	}

	public void print(ArrangeGrid arrangeGrid,
			List<ArrangeTeacher> arrangeTeachers) {
		// 在控制台输出课表结果信息
		Collection<ArrangeClass> classTimetables = arrangeGrid
				.getClassTimetables().values();
		for (ArrangeClass arrangeClass : classTimetables) {
			int totalMaxDays = arrangeClass.getTotalMaxDays();
			int totalMaxLesson = arrangeClass.getTotalMaxLesson();
			System.out.println("年级：" + arrangeClass.getGradeId());
			System.out.println("--------------");
			String classId = arrangeClass.getClassId();
			System.out.println("班级：" + classId);
			if (arrangeTeachers != null) {
				this.totalTaskInfo(arrangeClass, arrangeTeachers);
			}
			System.out
					.println("---------------------------------------------------------------------");
			CourseGene[][] timetable = arrangeClass.getTimetable();
			for (int i = 0; i < totalMaxLesson; i++) {

				for (int j = 0; j < totalMaxDays; j++) {
					CourseGene courseGene = timetable[j][i];
					if (courseGene != null) {
						ArrangeCourse arrangeCourse = courseGene
								.getArrangeCourse();
						System.out.print("	" + arrangeCourse.getCourseName());

						if (courseGene.isOddEven()) {
							System.out
									.print("/"
											+ courseGene.getDwCourseGene()
													.getArrangeCourse()
													.getCourseName());
						}
						// System.out.print("("+i+","+j+")");
						// System.out.print(arrangeBlock.getCourse().getTeacherIds().values());
					} else {
						System.out.print("	(空)");
					}
					System.out.print("	");
				}
				System.out.println("");
			}

		}

	}

	/**
	 * 输出总教学任务的课时信息
	 * 
	 * @param arrangeClass
	 * @param arrangeTeachers
	 */
	private void totalTaskInfo(ArrangeClass arrangeClass,
			List<ArrangeTeacher> arrangeTeachers) {
		String classId = arrangeClass.getClassId();
		int totalMaxDays = arrangeClass.getTotalMaxDays();
		int totalMaxLesson = arrangeClass.getTotalMaxLesson();
		System.out.println("可安排课时数：" + totalMaxDays * totalMaxLesson);
		double totalTaskLesson = 0;

		for (ArrangeTeacher arrangeTeacher : arrangeTeachers) {
			for (ArrangeCourse arrangeCourse : arrangeTeacher.getCourses()) {

				if (arrangeCourse.getClassId().equals(classId)) {
					totalTaskLesson += arrangeCourse.getTaskLessons();
					// 各科目课时计划数
					System.out.print(arrangeCourse.getCourseName() + "：["
							+ arrangeCourse.getTaskLessons() + "]   ");
				}

			}

		}

		System.out.println("");
		System.out.println("总教学任务课时数：" + totalTaskLesson);

	}

	

	/**
	 * 根据课表的一个位置取相关的排课冲突数据
	 * 
	 * @param school
	 *            学校
	 * @param timetableId
	 *            课表ID
	 * @param gradeId
	 *            年级ID
	 * @param classId
	 *            班级ID
	 * @param courseIds
	 *            课目ID
	 * @param dayOfWeek
	 *            周期
	 * @param lessonOfDay
	 *            节次
	 * @return
	 */
	public List<JSONObject> getPositionConflicts(School school,
			String timetableId, String gradeId, String classId,
			String[] courseIds,int[] courseTypes, int dayOfWeek, int lessonOfDay, 
			String sessionID,String[] McGroupIds,int breakRule) {
		String schoolId = school.getId() + "";
		JSONObject timetableInfo = this.timetableService
				.getArrangeTimetableInfo(schoolId, timetableId);
		JSONObject gradeSet = this.timetableService.getGradeSet(schoolId,
				timetableId, gradeId);

		int maxDaysForWeek = timetableInfo.getIntValue("MaxDaysForWeek");
		int amLessonNum = gradeSet.getIntValue("AMLessonNum");
		int pmLessonNum = gradeSet.getIntValue("PMLessonNum");
		int maxLessonForDay = amLessonNum + pmLessonNum;

		String schoolYear = timetableInfo.getString("SchoolYear");
		String termName = timetableInfo.getString("TermName");
		String termInfoId = schoolYear.concat(termName);

		// List<JSONObject> taskList = this.timetableService.getTaskByTimetable(
		// schoolId, timetableId);

		List<JSONObject> arrangeList = this.timetableService
				.getArrangeTimetableList(schoolId, timetableId,null);

		JSONObject hcData = loadDataByMapTask(  sessionID, schoolId,
				timetableId, termInfoId, school);
		RuleConflict ruleConflict = (RuleConflict) hcData.get("ruleConflict"+schoolYear+termName);
		ruleConflict.setSchoolYear(schoolYear);
		ruleConflict.setTermName(termName);

		List<JSONObject> taskList = (List<JSONObject>) hcData.get("taskList");
		HashMap<String, HashMap<String, HashMap<String, String>>> taskMap = (HashMap<String, HashMap<String, HashMap<String, String>>>) hcData
				.get("taskMap");
		this.fillTeachersToArrangeList(ruleConflict , arrangeList, taskMap);

		Map<String, Map<String, String>> courseTeachers = this
				.getCourseTeachers(ruleConflict.getTeachersDic(), taskList);

		List<JSONObject> dataList = new ArrayList<JSONObject>();

		HashMap<String, List<JSONObject>> arrangeMap = new HashMap<String, List<JSONObject>>();
		for (JSONObject arrange : arrangeList) {
			List<JSONObject> list = new ArrayList<JSONObject>();
			int dow = arrange.getIntValue("DayOfWeek");
			int lod = arrange.getIntValue("LessonOfDay");
			String key = dow + "," + lod;
			if (arrangeMap.containsKey(key)) {
				list = arrangeMap.get(key);
			} else {
				arrangeMap.put(key, list);
			}

			list.add(arrange);

		}
		int inx = 0;
		List<JSONObject> tarLesson  = arrangeMap.get(dayOfWeek+","+lessonOfDay);
		for (String courseId : courseIds) {
			String key = classId + "_" + courseId;
			Map<String, String> teachers = courseTeachers.get(key);
			Classroom classroom = ruleConflict.getClassroomsDic().get(classId);
			if (classroom != null
					&& ruleConflict.getGradeIdGradeDic().containsKey(
							classroom.getGradeId())) {

				String gradeLev = ruleConflict.getGradeIdGradeDic()
						.get(classroom.getGradeId()).getCurrentLevel()
						.getValue()
						+ "";
				String mcGpId = "";
				if(McGroupIds.length>inx){
					mcGpId = McGroupIds[inx];
				}
				List<JSONObject> tarLessonTmp = GrepUtil.grepJsonKeyByVal(new String[]{"ClassId","CourseId"}, new String[]{classId,courseId}, tarLesson);
				
				if(tarLessonTmp.size()>0){
					JSONObject objNow = tarLessonTmp.get(0);
					Map<String, String> teacherparam = (Map<String, String>) objNow.get("teachers");
					List<JSONObject> confs = new ArrayList<JSONObject>();
					if(breakRule==0){
						
						confs.addAll(TimetableUtil.getConflictsWithSwap(classId, courseId,
								teacherparam  , maxDaysForWeek, maxLessonForDay, arrangeMap,
								ruleConflict, gradeLev, -100,
								objNow.getIntValue("IsAdvance"),dayOfWeek,lessonOfDay,objNow.getString("McGroupId")
								,false,objNow.getIntValue("CourseType"),objNow.getIntValue("DayOfWeek"),false) );
					}else{
						
						confs.addAll(TimetableUtil.getOnlyTeaConflictsWithSwap(classId, courseId,
								teacherparam  , maxDaysForWeek, maxLessonForDay, arrangeMap,
								ruleConflict, gradeLev, -100,
								objNow.getIntValue("IsAdvance"),dayOfWeek,lessonOfDay,objNow.getString("McGroupId")
								,false,objNow.getIntValue("CourseType"),objNow.getIntValue("DayOfWeek"),false));
					}
							
					
					dataList.addAll(confs);
//					dataList.addAll(confs);
				}
//				List<JSONObject> conflicts = TimetableUtil.getConflictsWithSwap(classId,
//						courseId, teachers, maxDaysForWeek, maxLessonForDay,
//						arrangeMap, ruleConflict, gradeLev, -100, 0, dayOfWeek, lessonOfDay, mcGpId,false, courseTypes[inx], dayOfWeek);
			}
			
			inx ++ ;
		}

		return dataList;
	}

	/**
	 * 根据班级取微调数据
	 * 
	 * @param school
	 *            学校
	 * @param timetableId
	 *            课表ID
	 * @param gradeId
	 *            年级ID
	 * @param classId
	 *            班级ID
	 * @return
	 */
	public JSONObject getForFineTuningDataByClass(School school,
			String timetableId, String gradeId, String classId,
			  String sessionID,int breakRule) {
		long d1 = new Date().getTime();
		String schoolId = school.getId() + "";
		JSONObject timetableInfo = this.timetableService
				.getArrangeTimetableInfo(schoolId, timetableId);

		String schoolYear = timetableInfo.getString("SchoolYear");
		String termName = timetableInfo.getString("TermName");
		String termInfoId = schoolYear.concat(termName);

		JSONObject gradeSet = this.timetableService.getGradeSet(schoolId,
				timetableId, gradeId);
		if (gradeSet == null) {
			throw new RuntimeException("当前选择的年级下无课表，请更换年级");
		}

		List<JSONObject> arrangeList = this.timetableService
				.getArrangeTimetableList(schoolId, timetableId,null);

		JSONObject hcData = loadDataByMapTask(  sessionID, schoolId,
				timetableId, termInfoId, school);
		RuleConflict ruleConflict = (RuleConflict) hcData.get("ruleConflict"+schoolYear+termName);
		ruleConflict.setSchoolYear(schoolYear);
		ruleConflict.setTermName(termName);

		List<JSONObject> taskList = (List<JSONObject>) hcData.get("taskList");
		HashMap<String, HashMap<String, HashMap<String, String>>> taskMap = (HashMap<String, HashMap<String, HashMap<String, String>>>) hcData
				.get("taskMap");
		this.fillTeachersToArrangeList(ruleConflict , arrangeList, taskMap);

		long d2 = new Date().getTime();

		//System.out.println("【课表微调】准备数据耗时：" + (d2 - d1));

		return this.getForFineTuningData(school, schoolId, timetableId,
				gradeId, classId, timetableInfo, gradeSet, taskList,
				arrangeList, ruleConflict, true, taskMap,breakRule);

	}

	/**
	 * 根据班级取临时调课数据
	 * 
	 * @param school
	 *            学校
	 * @param timetableId
	 *            课表ID
	 * @param gradeId
	 *            年级ID
	 * @param classId
	 *            班级ID
	 * @return
	 */
	public JSONObject getTempAjustForFineTuningDataByClass(School school,
			String timetableId, String gradeId, String classId,  String sessionID,
			String week, int weekOfEnd,int breakRule) {
		long d1 = new Date().getTime();
		String schoolId = school.getId() + "";
		JSONObject timetableInfo = this.timetableService
				.getArrangeTimetableInfo(schoolId, timetableId);

		String schoolYear = timetableInfo.getString("SchoolYear");
		String termName = timetableInfo.getString("TermName");
		String termInfoId = schoolYear.concat(termName);

		JSONObject gradeSet = this.timetableService.getGradeSet(schoolId,
				timetableId, gradeId);
		if (gradeSet == null) {
			throw new RuntimeException("当前选择的年级下无课表，请更换年级");
		}

		JSONObject hcData = loadDataByMapTask(  sessionID, schoolId,
				timetableId, termInfoId, school);
		RuleConflict ruleConflict = (RuleConflict) hcData.get("ruleConflict"+schoolYear+termName);
		ruleConflict.setSchoolYear(schoolYear);
		ruleConflict.setTermName(termName);

		List<JSONObject> taskList = (List<JSONObject>) hcData.get("taskList");
		List<JSONObject> arrangeList = (List<JSONObject>) hcData
				.get("arrangeList");
		HashMap<String, Object> cxMap = new HashMap<String, Object>();
		cxMap.put("timetableId", timetableId);
		cxMap.put("schoolId", school.getId() + "");
//		cxMap.put("gradeId", gradeId);
		cxMap.put("week", week.split("\\|")[0]);
		cxMap.put("weekOfEnd", weekOfEnd);
		cxMap.put("xn", schoolYear);
		cxMap.put("xq", termName);
		// 取 临时调课结果
		List<JSONObject> tempAjustList = timetableService
				.getTemporaryAjustList(cxMap);

		List<JSONObject> tempAjustRecord = timetableService
				.getTemporaryAjustRecord(cxMap);

		
		HashMap<String, TCRCATemporaryAdjustGroupInfo> groupMap = timetableService.getTempAjustGroupList(cxMap);
		HashMap<String, HashMap<Integer, TCRCATemporaryAdjustGroupParam>> groupParamMap = timetableService.getTempAjustGroupParamMap(cxMap);
		HashMap<String, HashMap<String, HashMap<String, String>>> taskMap = (HashMap<String, HashMap<String, HashMap<String, String>>>) hcData
				.get("taskMap");
		this.fillTeachersToArrangeList(ruleConflict , arrangeList, taskMap);

		long d2 = new Date().getTime();

		System.out.println("【临时调课】准备数据耗时：" + (d2 - d1));

		return this.getTempAjustForFineTuningData(school, schoolId,
				timetableId, gradeId, classId, timetableInfo, gradeSet,
				taskList, arrangeList, ruleConflict, true, taskMap,
				tempAjustList, week, weekOfEnd,breakRule,groupMap,
				tempAjustRecord,groupParamMap,ruleConflict);

	}
	/**
	 * 根据教师取临时调课数据
	 * 
	 * @param school
	 *            学校
	 * @param timetableId
	 *            课表ID
	 * @param gradeId
	 *            年级ID
	 * @param classId
	 *            班级ID
	 * @return
	 */
	public JSONObject getTempAjustForFineTuningDataByTeacher(School school,
			String timetableId, String teacherId,  String sessionID,
			String weekStr, int weekOfEnd,int breakRule) {
		String schoolId = school.getId() + "";
		JSONObject timetableInfo = this.timetableService
				.getArrangeTimetableInfo(schoolId, timetableId);
		
		String schoolYear = timetableInfo.getString("SchoolYear");
		String termName = timetableInfo.getString("TermName");
		List<JSONObject> gradeSets = this.timetableService.getGradeSetList(schoolId,
				timetableId, null);
		if (gradeSets == null) {
			throw new RuntimeException("当前选择的年级下无课表，请更换年级");
		}
		
		JSONObject hcData = loadDataByMapTask( sessionID, schoolId,
				timetableId, schoolYear+termName, school);
		RuleConflict ruleConflict = (RuleConflict) hcData.get("ruleConflict"+schoolYear+termName);
		ruleConflict.setSchoolYear(schoolYear);
		ruleConflict.setTermName(termName);
		
		List<JSONObject> arrangeList = (List<JSONObject>) hcData
				.get("arrangeList");
		List<JSONObject> taskList = (List<JSONObject>) hcData
				.get("taskList");
		int week = Integer.parseInt(weekStr.split("\\|")[0]);
		HashMap<String, Object> cxMap = new HashMap<String, Object>();
		cxMap.put("timetableId", timetableId);
		cxMap.put("schoolId", school.getId() + "");
//		cxMap.put("gradeId", gradeId);
		cxMap.put("week", week);
		cxMap.put("weekOfEnd", weekOfEnd);
		cxMap.put("xn", schoolYear);
		cxMap.put("xq", termName);
		// 取 临时调课结果
		List<JSONObject> tempAjustList = timetableService
				.getTemporaryAjustList(cxMap);
		
		HashMap<String, HashMap<String, HashMap<String, String>>> taskMap = (HashMap<String, HashMap<String, HashMap<String, String>>>) hcData
				.get("taskMap");
		this.fillTeachersToArrangeList(ruleConflict , arrangeList, taskMap);

		String weekDate = weekStr.split("\\|")[1];
		JSONObject data = new JSONObject();

		int maxDaysForWeek = timetableInfo.getIntValue("MaxDaysForWeek");

		
		Map<String, Account> teacherDic = ruleConflict.getTeachersDic();
		//过滤使用年级
		List<String> relatedGradeIds = new ArrayList<String>();
		// 已排课表-正式课表 天次，节次-课程课时
		HashMap<String, List<JSONObject>> arrangeMap = new HashMap<String, List<JSONObject>>();
		for (JSONObject arrange : arrangeList) {
			List<JSONObject> list = new ArrayList<JSONObject>();
			int dow = arrange.getIntValue("DayOfWeek");
			int lod = arrange.getIntValue("LessonOfDay");
			String key = dow + "," + lod;
			if (arrangeMap.containsKey(key)) {
				list = arrangeMap.get(key);
			} else {
				arrangeMap.put(key, list);
			}

			list.add(arrange);
		}
		for(JSONObject arrange :taskList){
			String classId = arrange.getString("ClassId");
			String TeacherId =   arrange.getString("TeacherId");
			if(TeacherId!=null&&TeacherId.trim().equals(teacherId)){
				//记录教师所教班级
				Classroom clar = ruleConflict.findClassInfo(classId);
				if(clar==null){
					continue;
				}
				String synj = ruleConflict.getGradeIdSynjMap().get(clar.getGradeId());
				if(synj==null){
					continue;
				}
				if(!relatedGradeIds.contains(synj)){
					relatedGradeIds.add(synj);
				}
			}
			
		}
		int amLessonNum = 0;
		int maxLessonForDay = 0;
		for(JSONObject gradeSet:gradeSets){
			String gradeId = gradeSet.getString("GradeId");
			if(!relatedGradeIds.contains(gradeId)){
				continue;
			}
			int temp = gradeSet.getIntValue("AMLessonNum");
			if(temp>amLessonNum){
				amLessonNum = temp;
			}
			int maxtemp = temp + gradeSet.getIntValue("PMLessonNum");
			if(maxtemp>maxLessonForDay){
				maxLessonForDay = maxtemp;
			}
		}
		if(amLessonNum==0){
			amLessonNum = 4;
		}
		if(maxLessonForDay==0){
			maxLessonForDay = 8;
		}
		// 临时调课课表-当前周-全年级
		HashMap<String, List<JSONObject>> curWeekTbAllNj = TimetableUtil
				.getSingleWeekTimeTableByParam(arrangeMap, tempAjustList, week,
						null,ruleConflict);
		// 临时调课课表-跨周-全年级
//		HashMap<String, List<JSONObject>> allWeekTbAllNj = TimetableUtil
//				.getAllWeekTimeTableByParam(curWeekTbAllNj, tempAjustList, week,
//						weekOfEnd,ruleConflict);
		
		List<JSONObject> tempAjustRecord = timetableService
				.getTemporaryAjustRecord(cxMap);

		
		HashMap<String, TCRCATemporaryAdjustGroupInfo> groupMap = timetableService.getTempAjustGroupList(cxMap);
		HashMap<String, HashMap<Integer, TCRCATemporaryAdjustGroupParam>> groupParamMap = timetableService.getTempAjustGroupParamMap(cxMap);
		
		List<JSONObject> arrangeNowAll = TimetableUtil.changeMapListToList(curWeekTbAllNj);
		arrangeNowAll =  TimetableUtil.getIsAdvancedPlusByAjstRs(tempAjustList,week,weekOfEnd,
				arrangeNowAll,tempAjustRecord,groupMap,groupParamMap,ruleConflict);
		TimetableUtil.loadConflictsByAdvancedPlus(arrangeNowAll,maxDaysForWeek,maxLessonForDay+1);
		// 已排课表-正式课表 天次，节次-课程课时--用于计算冲突
		arrangeMap = new HashMap<String, List<JSONObject>>();
		for (JSONObject arrange : arrangeNowAll) {
			List<JSONObject> list = new ArrayList<JSONObject>();
			int dow = arrange.getIntValue("DayOfWeek");
			int lod = arrange.getIntValue("LessonOfDay");
			String key = dow + "," + lod;
			if (arrangeMap.containsKey(key)) {
				list = arrangeMap.get(key);
			} else {
				arrangeMap.put(key, list);
			}

			list.add(arrange);

		}
		
		String[] weekDates = TimetableUtil.getWeekDatesByStart(weekDate,
				maxDaysForWeek);
		
		data.put("weekDate",weekDates);
		
		
		//教师课表
		List<JSONObject> teacherTimetable = new ArrayList<JSONObject>();
		if(breakRule==0){
			teacherTimetable = TimetableUtil.getTeacherTimetableWithConflict
			(ruleConflict,curWeekTbAllNj,arrangeMap, teacherId,
			maxDaysForWeek, maxLessonForDay,true);
		}else{
			teacherTimetable=	TimetableUtil.getOnlyTeacherTimetableWithConflict
					(ruleConflict,curWeekTbAllNj,arrangeMap,
							teacherId, maxDaysForWeek, maxLessonForDay,false);
			
		}
		this.loadSwapConflicts(teacherTimetable, 0);
		
		teacherTimetable = TimetableUtil.removeRepeatPositions(teacherTimetable);
		data.put("timetable",teacherTimetable);
	
		List<JSONObject> classTimetable = TimetableUtil.getAjustTimeTableByClass(ruleConflict,curWeekTbAllNj);
		
		List<JSONObject> adjustedList = TimetableUtil.getAdjustedListByAjustRecord
				(ruleConflict, week, weekOfEnd,tempAjustRecord,classTimetable);
		data.put("adjustedList",adjustedList);
		
		data.put("teacherId", teacherId);
		data.put("teacherName", teacherDic.get(teacherId).getName());

		data.put("totalMaxDays", maxDaysForWeek);
		data.put("amLessonNum", amLessonNum);
//		data.put("pmLessonNum", pmLessonNum);
		data.put("totalMaxLesson", maxLessonForDay);
		data.put("totalTaskLesson", maxDaysForWeek * maxLessonForDay);


		data.put("classTimetable", classTimetable);

		return data;
		
	}

	public JSONObject loadDataByMapTask(  String sessionID,
			String schoolId, String timetableId, String termInfo,
			School school) {
		// TODO Auto-generated method stub
		
		JSONObject data = new JSONObject();
		List<JSONObject> taskList = new ArrayList<JSONObject>();
		List<JSONObject> arrangeList = new ArrayList<JSONObject>();
		RuleConflict ruleConflict = new RuleConflict();
		HashMap<String, HashMap<String, HashMap<String, String>>> taskMap = new HashMap<String, HashMap<String, HashMap<String, String>>>();
		
		Object tbBaseKey = "timetable."+timetableId+".10.tbBaseDataCache";
		JSONObject tbBaseData = null;
		try {
			tbBaseData = (JSONObject) redisOperationDAO.get(tbBaseKey);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(tbBaseData==null){
			tbBaseData = new JSONObject();
		}
		 
		if (tbBaseData.containsKey("ruleConflict"+termInfo)) {
			ruleConflict = (RuleConflict) tbBaseData.get("ruleConflict"+termInfo);
		} else {
			ruleConflict = this.arrangeDataService.getRuleConflict(schoolId,
					timetableId);
			HashMap<String, Object> cxmap = new HashMap<String, Object>();
			cxmap.put("schoolId", schoolId);
			cxmap.put("timetableId", timetableId);
			List<JSONObject> gradeList = this.timetableService.getGradeSet(cxmap );
			for(JSONObject grade:gradeList){
				String synj = grade.getString("GradeId");
				String gradeLevel = this.commonService.ConvertSYNJ2NJDM(synj, termInfo.substring(0,4));
				if(gradeLevel!=null&&gradeLevel.trim().length()>0){
					
					//添加年级上午最大排课数
					ruleConflict.addToGradeLevelMaxAmNumMap( gradeLevel , grade.getIntValue("AMLessonNum"));
					ruleConflict.addToGradeLevelMaxPmNumMap( gradeLevel, grade.getIntValue("PMLessonNum"));
				}
			}
			JSONObject dataMap = loadDataByMap(  sessionID, termInfo, school);

			Map<String, Grade> gradeDic = (Map<String, Grade>) dataMap
					.get("gradeDic");
			Map<String, Classroom> classDic = (Map<String, Classroom>) dataMap
					.get("classDic");
			Map<String, LessonInfo> courseDic = (Map<String, LessonInfo>) dataMap
					.get("courseDic");
			Map<String, Account> teacherDic = (Map<String, Account>) dataMap
					.get("teacherDic");
			
			
			ruleConflict.setGradesDic(gradeDic);
			// 因为从深圳基础接口取该数据非常费时，暂屏蔽
			ruleConflict.setClassroomsDic(classDic);
			ruleConflict.setCoursesDic(courseDic);
			ruleConflict.setTeachersDic(teacherDic);
			tbBaseData.put("ruleConflict"+termInfo, ruleConflict);
		}
		data.put("ruleConflict"+termInfo, ruleConflict);
		
		Object tbKey = "timetable."+timetableId+".10.tbDataCache";
		JSONObject tbData = null;
		try {
			tbData = (JSONObject) redisOperationDAO.get(tbKey);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(tbData==null){
			tbData = new JSONObject();
		}
		
		if (tbData.containsKey("taskList")) {
			taskList = (List<JSONObject>) tbData.get("taskList");
		} else {
			taskList = this.timetableService.getTaskByTimetable(schoolId,
					timetableId);

			for(JSONObject obj:taskList){
				String ClassId = obj.getString("ClassId");
				String CourseId = obj.getString("CourseId");
				String McGroupId = ruleConflict.getMcGroupId(ClassId, CourseId);
				obj.put("McGroupId", McGroupId);
			}
			tbData.put("taskList", taskList);
		}
		if (tbData.containsKey("arrangeList")) {
			arrangeList = (List<JSONObject>) tbData.get("arrangeList");
			for(JSONObject arr:arrangeList){
				arr.put("IsAdvance", arr.get("orIsAdvance"));
			}
		} else {
			arrangeList = this.timetableService.getArrangeTimetableList(schoolId,
					timetableId,null);
			for(JSONObject arr:arrangeList){
				arr.put("orIsAdvance", arr.get("IsAdvance"));
			}

			tbData.put("arrangeList", arrangeList);
		}
		
		if (tbData.containsKey("taskMap")) {
			taskMap = (HashMap<String, HashMap<String, HashMap<String, String>>>) tbData
					.get("taskMap");
		}else{
			Map<String, Account> teaDic = ruleConflict.getTeachersDic();
			for (JSONObject task : taskList) {
				String classId = task.getString("ClassId");
				String courseId = task.getString("CourseId");
				String teacherId = task.getString("TeacherId");
				HashMap<String, HashMap<String, String>> courseMap = new HashMap<String, HashMap<String, String>>();
				if (taskMap.containsKey(classId)) {
					courseMap = taskMap.get(classId);
				} else {
					taskMap.put(classId, courseMap);
				}
				HashMap<String, String> teaMap = new HashMap<String, String>();
				if (courseMap.containsKey(courseId)) {
					teaMap = courseMap.get(courseId);
				} else {
					courseMap.put(courseId, teaMap);
				}
				if (!teaMap.containsKey(teacherId)&&teacherId!=null) {
					String actName = "";
					if (teaDic.containsKey(teacherId)) {
						actName = teaDic.get(teacherId).getName();
					}
					teaMap.put(teacherId, actName);
				}
			}

			tbData.put("taskMap", taskMap);
		}
			
		try {
			redisOperationDAO.set(tbBaseKey, tbBaseData,CacheExpireTime.sessionMinExpireTime.getTimeValue());
			redisOperationDAO.set(tbKey, tbData,CacheExpireTime.sessionMinExpireTime.getTimeValue());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		data.putAll(tbData);
		return data;
	}

	@SuppressWarnings("unchecked")
	public JSONObject loadDataByMap(String sessionID, String termInfo, School school) {
		// TODO Auto-generated method stub
		
		Object tbKey = "common."+sessionID+".09.baseCache";
		JSONObject data = new JSONObject();
//		try {
//			data = (JSONObject) redisOperationDAO.get(tbKey);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		if(data==null){
//			data = new JSONObject();
//		}
		school = commonService.getSchoolById(school.getId(), termInfo);
		long d1 = new Date().getTime();
		Map<String, Grade> gradeDic;
		if (data.containsKey("gradeDic")) {
			gradeDic = (Map<String, Grade>) data.get("gradeDic");
		} else {
			gradeDic = this.arrangeDataService.getGradesDic(termInfo.substring(0,4), school, termInfo.substring(4,5));
			
			data.put("gradeDic", gradeDic);
		}
			List<Grade> cxGrade = new ArrayList<Grade>();
			for(Grade gd:gradeDic.values()){
				cxGrade.add(gd);
			}
			
		Map<String, Classroom> classDic;
		if (data.containsKey("classDic")) {
			classDic = (Map<String, Classroom>) data.get("classDic");
		} else {
			classDic = this.arrangeDataService.getClassRoomsDic(school,cxGrade, termInfo);
			data.put("classDic", classDic);
		}

		Map<String, LessonInfo> courseDic;
		if (data.containsKey("courseDic")) {
			courseDic = (Map<String, LessonInfo>) data.get("courseDic");
		} else {
			courseDic = this.arrangeDataService.getCoursesDic(school,termInfo);
			data.put("courseDic", courseDic);
		}
			
			
		long d3 = new Date().getTime();
		Map<String, Account> teacherDic;
		if (data.containsKey("teacherDic")) {
			teacherDic = (Map<String, Account>) data.get("teacherDic");
		} else {
			teacherDic = this.arrangeDataService.getTeachersDic(school,termInfo);
			data.put("teacherDic", teacherDic);
		}
		
		try {
			redisOperationDAO.set(tbKey, data, CacheExpireTime.minExpireTime.getTimeValue());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		long d2 = new Date().getTime();

		System.out.println("[课表]【查询教师数据】耗时："+(d2-d3));
		System.out.println("[课表]【准备基础数据】耗时："+(d2-d1));
		
		return data;

	}

	/**
	 * 根据年级取微调数据
	 * 
	 * @param school
	 *            学校
	 * @param timetableId
	 *            课表ID
	 * @param gradeId
	 *            年级ID
	 * @return
	 */
	public JSONObject getForFineTuningDataByGrade(School school,
			String timetableId, String gradeId,  String sessionID,int breakRule) {
		String schoolId = school.getId() + "";
		JSONObject timetableInfo = this.timetableService
				.getArrangeTimetableInfo(schoolId, timetableId);

		String schoolYear = timetableInfo.getString("SchoolYear");
		String termName = timetableInfo.getString("TermName");
		String termInfoId = schoolYear.concat(termName);

		JSONObject gradeSet = this.timetableService.getGradeSet(schoolId,
				timetableId, gradeId);
		if (gradeSet == null) {
			throw new RuntimeException("当前选择的年级下无课表，请更换年级");
		}

		int maxDaysForWeek = timetableInfo.getIntValue("MaxDaysForWeek");

		int amLessonNum = gradeSet.getIntValue("AMLessonNum");
		int pmLessonNum = gradeSet.getIntValue("PMLessonNum");
		int maxLessonForDay = amLessonNum + pmLessonNum;

		List<JSONObject> arrangeList = this.timetableService
				.getArrangeTimetableList(schoolId, timetableId,null);

		JSONObject hcData = loadDataByMapTask(  sessionID, schoolId,
				timetableId, termInfoId, school);
		RuleConflict ruleConflict = (RuleConflict) hcData.get("ruleConflict"+termInfoId);
		ruleConflict.setSchoolYear(schoolYear);
		ruleConflict.setTermName(termName);

		List<JSONObject> taskList = (List<JSONObject>) hcData.get("taskList");
		HashMap<String, HashMap<String, HashMap<String, String>>> taskMap = (HashMap<String, HashMap<String, HashMap<String, String>>>) hcData
				.get("taskMap");
		this.fillTeachersToArrangeList(ruleConflict , arrangeList, taskMap);


		JSONObject resultData = new JSONObject();

		List<JSONObject> dataList = new ArrayList<JSONObject>();
		List<String> gradeIds = new ArrayList<String>();
		gradeIds.add(gradeId);
		List<Classroom> classInfos = this.timetableService.getClassByGradeIds(
				schoolId, termInfoId, gradeIds);
		for (Classroom classroom : classInfos) {
			String classId = String.valueOf(classroom.getId());
			JSONObject data = this.getForFineTuningData(school, schoolId,
					timetableId, gradeId, classId, timetableInfo, gradeSet,
					taskList, arrangeList, ruleConflict, false, taskMap,breakRule);

			dataList.add(data);

		}
		try {
			dataList = (List<JSONObject>) Sort.sort(SortEnum.ascEnding0rder, dataList, "className");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		resultData.put("totalMaxDays", maxDaysForWeek);
		resultData.put("amLessonNum", amLessonNum);
		resultData.put("pmLessonNum", pmLessonNum);
		resultData.put("totalMaxLesson", maxLessonForDay);
		resultData.put("dataList", dataList);

		return resultData;

	}

	/**
	 * 取微调数据
	 * 
	 * @param schoolId
	 *            学校ID
	 * @param timetableId
	 *            课表ID
	 * @param gradeId
	 *            年级ID
	 * @param classId
	 *            班级ID
	 * @param timetableInfo
	 *            课表信息
	 * @param gradeSet
	 *            年级
	 * @param taskList
	 *            教学任务
	 * @param arrangeList
	 *            已排课数据
	 * @param ruleConflict
	 *            冲突规则
	 * @param notForGrade
	 *            不为年级微调（true即班级微调）
	 * @return
	 */
	public JSONObject getForFineTuningData(School sch, String schoolId,
			String timetableId, String gradeId, String classId,
			JSONObject timetableInfo, JSONObject gradeSet,
			List<JSONObject> taskList, List<JSONObject> arrangeList,
			RuleConflict ruleConflict, boolean notForGrade,
			HashMap<String, HashMap<String, HashMap<String, String>>> taskMap,int breakRule) {

		JSONObject data = new JSONObject();
		
		long d9 = new Date().getTime();

		int maxDaysForWeek = timetableInfo.getIntValue("MaxDaysForWeek");

		int amLessonNum = gradeSet.getIntValue("AMLessonNum");
		int pmLessonNum = gradeSet.getIntValue("PMLessonNum");
		int maxLessonForDay = amLessonNum + pmLessonNum;

		Classroom classroom = ruleConflict.getClassroomsDic().get(classId);
		// Classroom classroom =
		// this.commonService.getClassById(Long.valueOf(schoolId),Long.parseLong(classId));

		Map<String, Account> teacherDic = ruleConflict.getTeachersDic();
		Map<String, Classroom> classroomsDic = ruleConflict.getClassroomsDic();
		Map<String, Grade> gradeDic = ruleConflict.getGradesDic();

		// Classroom classroom = classroomsDic.get(classId);
		Map<String, LessonInfo> coursesDic = ruleConflict.getCoursesDic();
		// 已排节次
		double totalTaskLessonCount = 0;
		double classLessonCount = 0;

		List<JSONObject> courseList = new ArrayList<JSONObject>();

		List<JSONObject> timetableList = new ArrayList<JSONObject>();

		classLessonCount = this.getClassLessonCount(classId, arrangeList);

		HashMap<String, List<JSONObject>> arrangeMap = new HashMap<String, List<JSONObject>>();
		for (JSONObject arrange : arrangeList) {
			List<JSONObject> list = new ArrayList<JSONObject>();
			int dow = arrange.getIntValue("DayOfWeek");
			int lod = arrange.getIntValue("LessonOfDay");
			String key = dow + "," + lod;
			if (arrangeMap.containsKey(key)) {
				list = arrangeMap.get(key);
			} else {
				arrangeMap.put(key, list);
			}

			list.add(arrange);

		}

		if (notForGrade) {
			Map<String, Map<String, String>> courseTeachers = this
					.getCourseTeachers(ruleConflict.getTeachersDic(), taskList);

			// 加载本班的教学计划--未排课程计算冲突
			List<String> courses = new ArrayList<String>();
			for (JSONObject task : taskList) {

				String _classId = task.getString("ClassId");
				if (!_classId.equals(classId)) {
					continue;
				}

				double weekNum = task.getDoubleValue("WeekNum");
				String courseId = task.getString("CourseId");
				
				String key = classId + "_" + courseId;
				if(ruleConflict.getMcGroupId(_classId, courseId)!=null){
					task.put("McGroupId", ruleConflict.getMcGroupId(_classId, courseId));
				}
				if(!courses.contains(key)){
					
					totalTaskLessonCount += weekNum;
				}

				JSONObject taskCourse = new JSONObject();
				taskCourse.put("courseId", courseId);
				LessonInfo courseInfo = ruleConflict.findCourseInfo(courseId);
				if (courseInfo != null&&courseInfo.getName().trim().length()>0) {
					// 课程名称
					taskCourse.put("courseName", courseInfo.getName());
					taskCourse.put("courseSimpleName",
							courseInfo.getSimpleName());
				}

				
				Map<String, String> teachers = courseTeachers.get(key);
				// 教师名称
				// taskCourse.put("teachers",
				// this.convertTeacherToList(teachers));
				taskCourse.put("teachers", this.convertTeacherToArrayList(sch,
						timetableId, teachers, arrangeList, taskList,
						teacherDic, timetableInfo, classroomsDic, coursesDic,
						gradeDic, taskMap));

				double courseLessonCount = this.getCourseLessonCount(classId,
						courseId, arrangeList);
				taskCourse.put("lessonCount", courseLessonCount);

				double unLessonCount = weekNum - courseLessonCount;
				taskCourse.put("unLessonCount", weekNum - courseLessonCount);

				
				Grade ddd = ruleConflict.findGradeInfo(gradeId);
				// 计算冲突位置
				if (classroom == null
						|| classroom.getGradeId() == 0
						|| ruleConflict.getGradeIdGradeDic().get(
								classroom.getGradeId()) == null) {
					logger.info(
							"[班级获取年纪错误]，classroom:{},grade:{}",
							classroom,classroom==null?"null":classroom.getGradeId( ));
					continue;
				} 				
				String gradeLev = ddd.getCurrentLevel().getValue() + "";
				
				// 计算冲突位置
				List<JSONObject> conflicts = new ArrayList<JSONObject>();
				if(task.getString("McGroupId")!=null&&task.getString("McGroupId").trim().length()>0){
					String mcgid = task.getString("McGroupId");
					List<String> classes = ruleConflict.getMergeClassByGroupId(mcgid);
					TimetableUtil.getConflictsByUnfinishTask(ruleConflict, maxDaysForWeek,
							maxLessonForDay, arrangeMap, courseTeachers,
							courseId, conflicts, classes);
				}
				if(breakRule==0){
					
					conflicts.addAll( TimetableUtil.getConflicts(classId,
							courseId, teachers, maxDaysForWeek, maxLessonForDay,
							arrangeMap, ruleConflict, gradeLev, unLessonCount,
							0,task.getString("McGroupId"),0,-1));
				}else{
					conflicts .addAll( TimetableUtil.getOnlyTeaConflicts(classId,
							courseId, teachers, maxDaysForWeek, maxLessonForDay,
							arrangeMap, ruleConflict, gradeLev, unLessonCount,
							0,task.getString("McGroupId"),0,-1));
				}

				taskCourse.put("conflicts", conflicts);
				
				taskCourse.put("McGroupId", task.getString("McGroupId") );

				if(!courses.contains(key)){
					
					courseList.add(taskCourse);
					courses.add(key);
				}

			}
		}

		// 已排课表中计算冲突
		for (JSONObject arrange : arrangeList) {
			String _classId = arrange.getString("ClassId");
			if (!classId.equals(_classId)) {
				// 不是本班的课表，则跳过
				continue;
			}

			String courseId = arrange.getString("CourseId");
			int day = arrange.getIntValue("DayOfWeek");
			int lesson = arrange.getIntValue("LessonOfDay");
			// 0：正课，1：单周课，2：双周课
			int courseType = arrange.getIntValue("CourseType");

			String mcGroupId = arrange.getString("McGroupId");
			JSONObject timetable = new JSONObject();
			timetable.put("courseId", courseId);
			timetable.put("IsAdvance", arrange.get("IsAdvance"));
			timetable.put("McGroupId", arrange.get("McGroupId"));
			LessonInfo courseInfo = ruleConflict.findCourseInfo(courseId);
			if (courseInfo != null&&courseInfo.getName().trim().length()>0) {
				// 课程名称
				timetable.put("courseName", courseInfo.getName());
				timetable.put(
						"courseSimpleName",
						courseInfo.getSimpleName() != null ? courseInfo
								.getSimpleName() : courseInfo.getName()
								.substring(0, 1));
			}else{
				timetable.put("courseName","[已删除]");
				timetable.put("courseSimpleName","[删]");
			}

			//
			Map<String, String> teachers = (Map<String, String>) arrange
					.get("teachers");
			timetable.put("teachers", TimetableUtil.convertTeacherToList(teachers));
			timetable.put("dayOfWeek", day);
			timetable.put("lessonOfDay", lesson);
			timetable.put("courseType", courseType);

			Grade ddd = ruleConflict.findGradeInfo(gradeId);

			if (notForGrade) {
				// 计算冲突位置
				if (classroom == null
						|| classroom.getGradeId() == 0
						|| ruleConflict.getGradeIdGradeDic().get(
								classroom.getGradeId()) == null) {
					logger.info(
							"[班级获取年纪错误]，classroom:{},grade:{}",
							classroom,
							(
									classroom==null?"null":classroom.getGradeId( )));
					continue;
				} 
				String gradeLev = ddd.getCurrentLevel().getValue() + "";
				List<JSONObject> conflicts = new ArrayList<JSONObject>();
				if(breakRule==0){
					
					conflicts = TimetableUtil.getConflicts(classId,
							courseId, teachers, maxDaysForWeek, maxLessonForDay,
							arrangeMap, ruleConflict, gradeLev, -100,
							arrange.getIntValue("IsAdvance"),arrange.getString("McGroupId"),
							arrange.getIntValue("CourseType"),arrange.getIntValue("DayOfWeek"));
				}else{
					conflicts = TimetableUtil.getOnlyTeaConflicts(classId,
							courseId, teachers, maxDaysForWeek, maxLessonForDay,
							arrangeMap, ruleConflict, gradeLev, -100,
							arrange.getIntValue("IsAdvance"),arrange.getString("McGroupId"),
							arrange.getIntValue("CourseType"),arrange.getIntValue("DayOfWeek"));
					
				}
				if(teachers==null||teachers.isEmpty()||mcGroupId!=null){
					if(breakRule==0){
						
						conflicts = TimetableUtil.getConflictsWithSwap(classId,
								courseId, teachers, maxDaysForWeek, maxLessonForDay,
								arrangeMap, ruleConflict, gradeLev, -100,
								arrange.getIntValue("IsAdvance"),day, lesson, 
								arrange.getString("McGroupId"), false,
								arrange.getIntValue("CourseType"),arrange.getIntValue("DayOfWeek"),false);
					}else{
						
						conflicts = TimetableUtil.getOnlyTeaConflictsWithSwap(classId,
								courseId, teachers, maxDaysForWeek, maxLessonForDay,
								arrangeMap, ruleConflict, gradeLev, -100,
								arrange.getIntValue("IsAdvance"),day, lesson, 
								arrange.getString("McGroupId"), false,
								arrange.getIntValue("CourseType"),arrange.getIntValue("DayOfWeek"),false);
						
					}
					
				}
				timetable.put("conflicts", conflicts);
			}

			timetableList.add(timetable);

		}

		
		data.put("classId", classId);
		data.put("className", classroom==null||classroom.getClassName()==null?"[已删除]":classroom.getClassName());
		data.put("lessonCount", classLessonCount);
		double unLessonCount = totalTaskLessonCount - classLessonCount;
		data.put("unLessonCount", unLessonCount < 0 ? 0 : unLessonCount);

		if (notForGrade) {
			data.put("totalMaxDays", maxDaysForWeek);
			data.put("amLessonNum", amLessonNum);
			data.put("pmLessonNum", pmLessonNum);
			data.put("totalMaxLesson", amLessonNum + pmLessonNum);
			data.put("totalTaskLesson", maxDaysForWeek * maxLessonForDay);

			long d1 = new Date().getTime();
			TimetableUtil.loadCourseConflictsByTimetable(courseList, timetableList);
			long d2 = new Date().getTime();

			//System.out.println("【课表微调】计算冲突三耗时：" + (d2 - d1));
			data.put("courseList", courseList);
		}

		if (notForGrade) {
			//
			long d1 = new Date().getTime();
			this.loadSwapConflicts(timetableList,breakRule);

			long d2 = new Date().getTime();

			//System.out.println("【课表微调】计算冲突四耗时：" + (d2 - d1));
			
			Grade ddd = ruleConflict.findGradeInfo(gradeId);
			if(ddd!=null){
				List<String> gradeClass = new ArrayList<String>();
				if(ddd.getClassIds()!=null){
					for(Long cid:ddd.getClassIds()){
						gradeClass.add(String.valueOf(cid));
					}
				}
				long d110 = new Date().getTime();
				List<JSONObject> classList = TimetableUtil.getClassFinishSituation(gradeClass,taskList,arrangeList,maxLessonForDay);
				long d111 = new Date().getTime();
				//System.out.println("【课表微调】计算排课是否完成:"+(d111-d110));
				data.put("classList", classList);
			}
		}

		data.put("timetable", timetableList);
		
		long d99 = new Date().getTime();
		//System.out.println("【课表微调】组装数据总耗时:"+(d99-d9));
		return data;

	}

	/**
	 * 取班级临时调课数据
	 * 
	 * @param schoolId
	 *            学校ID
	 * @param timetableId
	 *            课表ID
	 * @param gradeId
	 *            年级ID
	 * @param classId
	 *            班级ID
	 * @param timetableInfo
	 *            课表信息
	 * @param gradeSet
	 *            年级
	 * @param taskList
	 *            教学任务
	 * @param arrangeList
	 *            已排课数据
	 * @param ruleConflict
	 *            冲突规则
	 * @param notForGrade
	 *            不为年级微调（true即班级微调）
	 * @param tempAjustList
	 *            临时调课结果表数据
	 * @param week
	 *            当前周次
	 * @param weekOfEnd
	 *            结束周次
	 * @param groupMap 
	 * @param tempAjustRecord 
	 * @param groupParamMap 
	 * 
	 */
	public JSONObject getTempAjustForFineTuningData(School sch,
			String schoolId, String timetableId, String gradeId,
			String classId, JSONObject timetableInfo, JSONObject gradeSet,
			List<JSONObject> taskList, List<JSONObject> arrangeList,
			RuleConflict ruleConflict, boolean notForGrade,
			HashMap<String, HashMap<String, HashMap<String, String>>> taskMap,
			List<JSONObject> tempAjustList, String weekStr, int weekOfEnd,int breakRule,
			HashMap<String, TCRCATemporaryAdjustGroupInfo> groupMap,
			List<JSONObject> tempAjustRecord, 
			HashMap<String, HashMap<Integer, TCRCATemporaryAdjustGroupParam>> groupParamMap,
			RuleConflict ruleconflict) {
		int week = Integer.parseInt(weekStr.split("\\|")[0]);
		String weekDate = weekStr.split("\\|")[1];
		JSONObject data = new JSONObject();

		int maxDaysForWeek = timetableInfo.getIntValue("MaxDaysForWeek");

		int amLessonNum = gradeSet.getIntValue("AMLessonNum");
		int pmLessonNum = gradeSet.getIntValue("PMLessonNum");
		int maxLessonForDay = amLessonNum + pmLessonNum;

		Classroom classroom = ruleConflict.getClassroomsDic().get(classId);

		// 已排课表-正式课表 天次，节次-课程课时
		HashMap<String, List<JSONObject>> arrangeMap = new HashMap<String, List<JSONObject>>();
		for (JSONObject arrange : arrangeList) {
			List<JSONObject> list = new ArrayList<JSONObject>();
			int dow = arrange.getIntValue("DayOfWeek");
			int lod = arrange.getIntValue("LessonOfDay");
			String key = dow + "," + lod;
			if (arrangeMap.containsKey(key)) {
				list = arrangeMap.get(key);
			} else {
				arrangeMap.put(key, list);
			}

			list.add(arrange);

		}
		// 临时调课课表-当前周
		HashMap<String, List<JSONObject>> curWeekTb = TimetableUtil
				.getSingleWeekTimeTableByParam(arrangeMap, tempAjustList, week,
						classId,ruleConflict);
		
		// 临时调课课表-当前周-全年级	
		HashMap<String, List<JSONObject>> curWeekTbAllNj = TimetableUtil
				.getSingleWeekTimeTableByParam(arrangeMap, tempAjustList, week,
						null,ruleConflict);
		// 临时调课课表-跨周-全年级
		HashMap<String, List<JSONObject>> curAllWeekTbAllNj = TimetableUtil
				.getAllWeekTimeTableByParam(arrangeMap, tempAjustList, week,
						weekOfEnd,ruleConflict);
		
//		List<JSONObject> arrangeNow = TimetableUtil.changeMapListToList(curWeekTbAllNj);
		List<JSONObject> arrangeNowAll = TimetableUtil.changeMapListToList(curAllWeekTbAllNj);
		// 已排课表-正式课表 天次，节次-课程课时
		arrangeMap = new HashMap<String, List<JSONObject>>();
		for (JSONObject arrange : arrangeNowAll) {
			List<JSONObject> list = new ArrayList<JSONObject>();
			int dow = arrange.getIntValue("DayOfWeek");
			int lod = arrange.getIntValue("LessonOfDay");
			String key = dow + "," + lod;
			if (arrangeMap.containsKey(key)) {
				list = arrangeMap.get(key);
			} else {
				arrangeMap.put(key, list);
			}

			list.add(arrange);

		}
		
		String[] weekDates = TimetableUtil.getWeekDatesByStart(weekDate,
				maxDaysForWeek);
		
		data.put("weekDate",weekDates);
		
//		List<JSONObject> adjustedList = TimetableUtil
//				.getAdjustedListByAjustResutl(ruleConflict, week, weekOfEnd,
//						tempAjustList);

		
		//教师课表
		List<JSONObject> teacherTimetable = TimetableUtil.getTeacherTimetable(ruleConflict,curWeekTbAllNj);
		data.put("teacherTimetable",teacherTimetable);
		List<JSONObject> timetable = TimetableUtil.getAjustTimeTableByClassWithConflict(ruleConflict,
				classId,curWeekTb,arrangeMap,maxDaysForWeek,maxLessonForDay,breakRule);
		
		List<JSONObject> adjustedList = TimetableUtil.getAdjustedListByAjustRecord
				(ruleConflict, week, weekOfEnd,tempAjustRecord,timetable);
		data.put("adjustedList",adjustedList);
		
		data.put("classId", classId);
		data.put("className", classroom.getClassName());

		data.put("totalMaxDays", maxDaysForWeek);
		data.put("amLessonNum", amLessonNum);
		data.put("pmLessonNum", pmLessonNum);
		data.put("totalMaxLesson", amLessonNum + pmLessonNum);
		data.put("totalTaskLesson", maxDaysForWeek * maxLessonForDay);
		long d11 = new Date().getTime();
		
		timetable = TimetableUtil.getIsAdvancedPlusByAjstRs(tempAjustList,week,weekOfEnd,
				timetable,tempAjustRecord,groupMap,groupParamMap,ruleconflict);
		TimetableUtil.loadConflictsByAdvancedPlus(timetable,maxDaysForWeek,maxLessonForDay+1);
		this.loadSwapConflicts(timetable,0);

		long d22 = new Date().getTime();

		System.out.println("【课表临时调课】计算冲突四耗时：" + (d22 - d11));

		timetable = TimetableUtil.removeRepeatPositions(timetable);
		data.put("timetable", timetable);

		return data;

	}

	

	/**
	 * 根据课表取交换位置的冲突
	 * 
	 * @param timetableList
	 * @param breakRule 是否打破规则排课
	 */
	private void loadSwapConflicts(List<JSONObject> timetableList, int breakRule) {

		for (int i = 0; i < timetableList.size(); i++) {
			JSONObject timetable = timetableList.get(i);
			// 当前位置
			int day = timetable.getIntValue("dayOfWeek");
			int lesson = timetable.getIntValue("lessonOfDay");
			int IsAdvance = timetable.getIntValue("IsAdvance");
			List<JSONObject> conflicts = (List<JSONObject>) timetable
					.get("conflicts");

			
			for (int j = 0; j < timetableList.size(); j++) {
				JSONObject target = timetableList.get(j);
				int targetDay = target.getIntValue("dayOfWeek");
				int targetLesson = target.getIntValue("lessonOfDay");
				int tarIsAdvance = target.getIntValue("IsAdvance");
				if(breakRule==0){
					
					if (IsAdvance == 1 || tarIsAdvance == 1) {
						JSONObject conflict = new JSONObject();
						conflict.put("dayOfWeek", targetDay);
						conflict.put("lessonOfDay", targetLesson);
						conflict.put("Status", -1);
						conflict.put("swap", 1);
						conflict.put("confType", "333");
						conflicts.add(conflict);
						
						continue;
					}
				}
				List<JSONObject> targetConflicts = (List<JSONObject>) target
						.get("conflicts");
				for (int c = 0; c < targetConflicts.size(); c++) {
					JSONObject targetConflict = targetConflicts.get(c);
					int dayOfWeek = targetConflict.getIntValue("dayOfWeek");
					int lessonOfDay = targetConflict.getIntValue("lessonOfDay");
					int swap = targetConflict.getIntValue("swap");
					int tarStatus= targetConflict.getIntValue("Status");
					if (swap == 0 && day == dayOfWeek && lesson == lessonOfDay 
							&&(tarStatus==-1||tarStatus==1)) {
						// 跟当前位置有冲突存在
						JSONObject conflict = new JSONObject();
						conflict.put("dayOfWeek", targetDay);
						conflict.put("lessonOfDay", targetLesson);
						conflict.put("Status",
								targetConflict.getIntValue("Status"));
						conflict.put("swap", 1);
						conflicts.add(conflict);
					}

				}
			}

		}

	}



	/**
	 * 教师对象转换
	 * 
	 * @param teachers
	 * @param gradeDic
	 * @return
	 */
	private List<JSONObject> convertTeacherToArrayList(School school,
			String timetableId, Map<String, String> teachers,
			List<JSONObject> arrangeList, List<JSONObject> taskList,
			Map<String, Account> teacherDic, JSONObject timetableInfo,
			Map<String, Classroom> classroomsDic,
			Map<String, LessonInfo> coursesDic, Map<String, Grade> gradeDic,
			HashMap<String, HashMap<String, HashMap<String, String>>> taskMap) {
		List<JSONObject> teacherList = new ArrayList<JSONObject>();
		if (teachers == null) {
			return teacherList;
		}
		for (String teacherId : teachers.keySet()) {

			JSONObject teacher = this
					.getTimetableByTeacher(school, timetableId, teacherId,
							arrangeList, taskList, teacherDic, timetableInfo,
							classroomsDic, coursesDic, gradeDic, taskMap);
			teacher.put("teacherId", teacherId);
			teacher.put("teacherName", teachers.get(teacherId));
			Object temp = teacher.get("timetable");
			teacher.put("teacherTimetable", temp);
			teacher.remove("timetable");
			teacherList.add(teacher);
		}

		return teacherList;
	}

	/**
	 * 取科目已排课时数
	 * 
	 * @param classId
	 * @param courseId
	 * @param arrangeList
	 * @return
	 */
	private double getCourseLessonCount(String classId, String courseId,
			List<JSONObject> arrangeList) {
		double lessonCount = 0;
		for (JSONObject arrange : arrangeList) {
			if (classId.equals(arrange.getString("ClassId"))
					&& courseId.equals(arrange.getString("CourseId"))) {
				// 这里需要考虑单双周的0.5情况

				if (arrange.getIntValue("CourseType") == 0) {

					lessonCount++;
				} else {
					lessonCount += 0.5;
				}
			}
		}
		return lessonCount;
	}

	/**
	 * 取班级已排课时数
	 * 
	 * @param classId
	 * @param arrangeList
	 * @return
	 */
	private double getClassLessonCount(String classId,
			List<JSONObject> arrangeList) {
		double lessonCount = 0;
		for (JSONObject arrange : arrangeList) {
			if (classId.equals(arrange.getString("ClassId"))) {
				// 这里需要考虑单双周的0.5情况
				if (arrange.getIntValue("CourseType") == 0) {

					lessonCount++;
				} else {
					lessonCount += 0.5;
				}
			}
		}
		return lessonCount;
	}

	

	
	/**
	 * 根据老师取课表信息
	 * 
	 * @param school
	 *            学校
	 * @param timetableId
	 *            课表ID
	 * @param teacherId
	 *            老师ID
	 * @param gradeDic
	 * @return
	 */
	public JSONObject getTimetableByTeacher(School school, String timetableId,
			String teacherId, List<JSONObject> arrangeList,
			List<JSONObject> taskList, Map<String, Account> teacherDic,
			JSONObject timetableInfo, Map<String, Classroom> classroomsDic,
			Map<String, LessonInfo> coursesDic, Map<String, Grade> gradeDic,
			HashMap<String, HashMap<String, HashMap<String, String>>> taskMap) {
		String schoolId = school.getId() + "";
		List<JSONObject> timetableList = new ArrayList<JSONObject>();
		// List<JSONObject> arrangeList =
		// this.timetableService.getArrangeTimetableList(schoolId, timetableId);
		// List<JSONObject> taskList =
		// this.timetableService.getTaskByTimetable(schoolId, timetableId);
		RuleConflict ruleConflict = new RuleConflict();
		ruleConflict.setClassroomsDic(classroomsDic);
		ruleConflict.setCoursesDic(coursesDic);
		ruleConflict.setGradesDic(gradeDic);
		ruleConflict.setTeachersDic(teacherDic);
		
		this.fillTeachersToArrangeList(ruleConflict , arrangeList, taskMap);

		// JSONObject timetableInfo =
		// this.timetableService.getArrangeTimetableInfo(schoolId, timetableId);
		String schoolYear = timetableInfo.getString("SchoolYear");
		String termName = timetableInfo.getString("TermName");
		String termInfoId = schoolYear.concat(termName);

		// Map<String, Classroom> classroomsDic =
		// this.arrangeDataService.getClassRoomsDic(schoolId, termInfoId);
		// Map<String, LessonInfo> coursesDic =
		// this.arrangeDataService.getCoursesDic(school);

		JSONObject data = new JSONObject();
		data.put("teacherId", teacherId);
		String teacherName = "";
		Account userInfo = teacherDic .get(teacherId);
		if (userInfo != null) {
			teacherName = userInfo.getName();
		}
		data.put("teacherName", teacherName);

		int maxDay = 0;
		int maxLesson = 0;

		for (JSONObject arrange : arrangeList) {
			Map<String, String> teachers = (Map<String, String>) arrange
					.get("teachers");
			if (teachers==null||teacherId==null||!teachers.containsKey(teacherId)) {
				continue;
			}

			int dayOfWeek = arrange.getIntValue("DayOfWeek");
			int lessonOfDay = arrange.getIntValue("LessonOfDay");

			maxDay = maxDay < dayOfWeek ? dayOfWeek : maxDay;
			maxLesson = maxLesson < lessonOfDay ? lessonOfDay : maxLesson;

			JSONObject timetable = new JSONObject();

			String courseId = arrange.getString("CourseId");
			timetable.put("courseId", courseId);
			String courseName = "";
			String courseSimpleName = "";
			LessonInfo lessonInfo = coursesDic.get(courseId);
			if (lessonInfo != null) {
				courseName = lessonInfo.getName();
				courseSimpleName = lessonInfo.getSimpleName();
			}
			timetable.put("courseName", courseName);
			timetable.put(
					"courseSimpleName",
					courseSimpleName != null ? courseSimpleName : courseName
							.substring(0, 1));
			String classId = arrange.getString("ClassId");
			timetable.put("classId", classId);
			String className = "";
			Classroom classroom = classroomsDic.get(classId);
			if (classroom != null) {
				className = classroom.getClassName();
				timetable.put("className", className);
				timetable.put("courseType", arrange.get("CourseType"));
				timetable.put("dayOfWeek", dayOfWeek);
				timetable.put("lessonOfDay", lessonOfDay);
				long gradeId = classroomsDic.get(classId).getGradeId();
				timetable.put("usedGrade",
						TimetableUtil.getSynjByGradeIdMap(gradeId, gradeDic));
				timetableList.add(timetable);
			}
		}

		data.put("timetable", timetableList);
		data.put("totalMaxDays", maxDay + 1);
		data.put("totalMaxLesson", maxLesson + 1);

		return data;
	}

	/**
	 * 从教学任务将老师信息设置到排课结果中
	 * 
	 * @param teachersDic
	 *            教师字典数据
	 * @param arrangeList
	 *            排课信息
	 * @param taskMap
	 *            教学任务
	 */
	public void fillTeachersToArrangeList(RuleConflict ruleConflict,
			List<JSONObject> arrangeList,
			HashMap<String, HashMap<String, HashMap<String, String>>> taskMap) {
		
		ruleConflict.clearArrangedPosition();
		List<String> mcgPoss = new ArrayList<String>();
		for (JSONObject arrange : arrangeList) {
			String classId = arrange.getString("ClassId");
			String courseId = arrange.getString("CourseId");

			
			Map<String, String> teachers = new HashMap<String, String>();
			if (taskMap.containsKey(classId)
					&& taskMap.get(classId).containsKey(courseId)) {
				teachers = taskMap.get(classId).get(courseId);
			}

			arrange.put("teachers", teachers);
			arrange.put("Teachers", teachers.keySet().toString().replaceAll("\\[", "").replaceAll("\\]", "")
					.replaceAll(" ", ""));
			
			Classroom classr = ruleConflict.findClassInfo(classId);
			if(classr==null){
				continue;
			}
			Grade grade = ruleConflict.getGradeIdGradeDic().get( classr.getGradeId() );
			if(grade==null||grade.isGraduate||grade.getCurrentLevel()==null){
				continue;
			}
			String gradeLevl = grade.getCurrentLevel().getValue()+"";
			int day = arrange.getIntValue("DayOfWeek");
			int lesson = arrange.getIntValue("LessonOfDay");
			
			String mcgId = arrange.getString("McGroupId");
			int max = 0;
			if(ruleConflict.getGradeLevelMaxAmNumMap().get(gradeLevl)==null||
					ruleConflict.getGradeLevelMaxPmNumMap().get(gradeLevl)==null){
				max = 8;
			}else{
				max = ruleConflict.getGradeLevelMaxAmNumMap().get(gradeLevl)
						+ruleConflict.getGradeLevelMaxPmNumMap().get(gradeLevl);
			}
			
			int courseType = arrange.getIntValue("CourseType");
			if(mcgId!=null&&mcgId.trim().length()>0){
				String mckey = day+","+lesson+","+mcgId;
				if(!mcgPoss.contains(mckey)&&lesson<max){
					ruleConflict.addArrangedPosition(classId,courseId, day,lesson,teachers.keySet(),courseType);
					mcgPoss.add(mckey);
				}
			}else if(lesson<max){
				ruleConflict.addArrangedPosition(classId,courseId, day,lesson,teachers.keySet(),courseType);
			}

		}

	}

	/**
	 * 根据教学任务构造老师的任课数据
	 * 
	 * @param teachersDic
	 *            教师字典数据
	 * @param taskList
	 *            教学任务
	 * @return
	 */
	private Map<String, Map<String, String>> getCourseTeachers(
			Map<String, Account> teachersDic, List<JSONObject> taskList) {
		Map<String, Map<String, String>> courseTeachers = new HashMap<String, Map<String, String>>();
		for (JSONObject task : taskList) {
			String classId = task.getString("ClassId");
			String courseId = task.getString("CourseId");
			String key = classId + "_" + courseId;
			String teacherId = task.getString("TeacherId");
			Account account = teachersDic.get(teacherId);
			String teacherName = "";
			if (account != null) {
				teacherName = account.getName();
			}
			if (courseTeachers.containsKey(key)) {
				//
				courseTeachers.get(key).put(teacherId, teacherName);
			} else {
				Map<String, String> teachers = new HashMap<String, String>();
				//
				teachers.put(teacherId, teacherName);
				courseTeachers.put(key, teachers);
			}

		}

		return courseTeachers;

	}

	@Override
	public JSONObject updateTemporaryLessonPosition(School school,
				String timetableId, String classId, Integer fromDayOfWeek,
				Integer fromLessonOfDay, String fromCourseIds,String fromCourseTypes, String fromMcGroupId,
				Integer toDayOfWeek,
				Integer toLessonOfDay, String toCourseIds,String toCourseTypes, String toMcGroupId,
			  String sessionID, int week,
			int weekOfEnd,String groupId) {
		// TODO Auto-generated method stub
		long d1 = new Date().getTime();
		String schoolId = school.getId() + "";
		JSONObject timetableInfo = this.timetableService
				.getArrangeTimetableInfo(schoolId, timetableId);

		String schoolYear = timetableInfo.getString("SchoolYear");
		String termName = timetableInfo.getString("TermName");

		JSONObject hcData = loadDataByMapTask(  sessionID, schoolId,
				timetableId, schoolYear+termName, school);
		RuleConflict ruleConflict = (RuleConflict) hcData.get("ruleConflict"+schoolYear+termName);
		ruleConflict.setSchoolYear(schoolYear);
		ruleConflict.setTermName(termName);

		List<JSONObject> arrangeList = (List<JSONObject>) hcData
				.get("arrangeList");
		HashMap<String, Object> cxMap = new HashMap<String, Object>();
		cxMap.put("timetableId", timetableId);
		cxMap.put("schoolId", school.getId() + "");
		cxMap.put("week", week);
		cxMap.put("weekOfEnd", weekOfEnd);
		cxMap.put("xn", schoolYear);
		cxMap.put("groupId", groupId);
		cxMap.put("xq", termName);
		// 取 临时调课结果
		List<JSONObject> tempAjustList = timetableService
				.getTemporaryAjustList(cxMap);
		TCRCATemporaryAdjustGroupInfo groupInfo = timetableService.getSingleTempAjustGroup(cxMap);
		
		List<TCRCATemporaryAdjustGroupInfo> needInsertGroupInfo = new ArrayList<TCRCATemporaryAdjustGroupInfo>();
		List<TCRCATemporaryAdjustGroupParam> needInsertGroupParam = new ArrayList<TCRCATemporaryAdjustGroupParam>();
		
		int step = 1;
		if(groupInfo==null){
			groupInfo = new TCRCATemporaryAdjustGroupInfo();
			groupInfo.setGroupId(groupId);
			groupInfo.setGroupMinWeek(week);
			if(weekOfEnd==0){
				groupInfo.setGroupMaxWeek(week );
			}else{
				groupInfo.setGroupMaxWeek(weekOfEnd);
			}
			groupInfo.setGroupStep(1);
			groupInfo.setIsCrossClass(0);
			groupInfo.setNotStartCancel(0);
			groupInfo.setNotStartOperate(0);
			groupInfo.setXn(schoolYear);
			groupInfo.setXqm(termName);
			groupInfo.setSchoolId(schoolId);
			groupInfo.setTimetableId(timetableId);
			groupInfo.setGroupStep(1);
			int loop = weekOfEnd;
			if(loop==0){
				loop = week;
			}
			for(int i=week;i<=loop;i++){
				TCRCATemporaryAdjustGroupParam param = new TCRCATemporaryAdjustGroupParam();
				param.setXn(schoolYear);
				param.setXn(schoolYear);
				param.setXqm(termName);
				param.setSchoolId(schoolId);
				param.setTimetableId(timetableId);
				param.setGroupId(groupId);
				param.setWeek(i);
				param.setGroupMoveTimes(1);
				param.setGroupStep(1);
				needInsertGroupParam.add(param);
			}
		}else{
			List<TCRCATemporaryAdjustGroupParam> needUpdateParams = timetableService.getSingleTempAjustGroupParam(cxMap);
			HashMap<Integer,TCRCATemporaryAdjustGroupParam> paramMaps = new HashMap<Integer, TCRCATemporaryAdjustGroupParam>();
			for(  TCRCATemporaryAdjustGroupParam param:needUpdateParams){
				paramMaps.put(param.getWeek(), param);
			}
			//如果出现非起始周调课
			if(week!=groupInfo.getGroupMinWeek()){
				int x = groupInfo.getNotStartOperate();
				if(x==0||x>week){
					//保持为最小周次
					x = week;
				}
				groupInfo.setNotStartOperate(x);
			}
			if(weekOfEnd>groupInfo.getGroupMaxWeek()){
				groupInfo.setGroupMaxWeek(weekOfEnd);
			}
			step = groupInfo.getGroupStep()+1;
			
			int loop = weekOfEnd;
			if(loop==0){
				loop = week;
			}
			for(int i=week;i<=loop;i++){
				if(paramMaps.get(i)!=null){
					TCRCATemporaryAdjustGroupParam param = paramMaps.get(i);
					int groupMoveTimes = param.getGroupMoveTimes();
					param.setGroupMoveTimes(groupMoveTimes+1 );
					param.setGroupStep(step);
					needInsertGroupParam.add(param);
				}else{
					TCRCATemporaryAdjustGroupParam param = new TCRCATemporaryAdjustGroupParam();
					param.setXn(schoolYear);
					param.setXn(schoolYear);
					param.setXqm(termName);
					param.setSchoolId(schoolId);
					param.setTimetableId(timetableId);
					param.setGroupId(groupId);
					param.setWeek(i);
					param.setGroupMoveTimes(1);
					param.setGroupStep(step);
					needInsertGroupParam.add(param);
				}
			}
//			for(TCRCATemporaryAdjustGroupParam param:needUpdateParams){
//				int groupMoveTimes = param.getGroupMoveTimes();
//				param.setGroupMoveTimes(groupMoveTimes+1 );
//				needInsertGroupParam.add(param);
//			}
		}
		groupInfo.setGroupStep(step);
		needInsertGroupInfo.add(groupInfo);
		
		//调课组逻辑结束
		HashMap<String, HashMap<String, HashMap<String, String>>> taskMap = (HashMap<String, HashMap<String, HashMap<String, String>>>) hcData
				.get("taskMap");
		this.fillTeachersToArrangeList(ruleConflict , arrangeList, taskMap);
		// 已排课表-正式课表 天次，节次-课程课时
		HashMap<String, List<JSONObject>> arrangeMap = new HashMap<String, List<JSONObject>>();
		for (JSONObject arrange : arrangeList) {
			List<JSONObject> list = new ArrayList<JSONObject>();
			int dow = arrange.getIntValue("DayOfWeek");
			int lod = arrange.getIntValue("LessonOfDay");
			String _classId = arrange.getString("ClassId");
			String key = dow + "," + lod+","+_classId;
			if (arrangeMap.containsKey(key)) {
				list = arrangeMap.get(key);
			} else {
				arrangeMap.put(key, list);
			}

			list.add(arrange);
		}
		
		HashMap<Integer, HashMap<String, List<JSONObject>>> evryWeekTbAllNj = TimetableUtil
					.getEvryWeekTimeTableByParam(arrangeMap, tempAjustList, week,
							weekOfEnd,ruleConflict);
		int code = 0;
		String msg = "操作成功！";
		try{
			
			JSONObject needInsertList = TimetableUtil.getNeedRecordAdjustedList(arrangeMap, fromDayOfWeek,
					fromLessonOfDay,  fromCourseIds, fromCourseTypes,fromMcGroupId, toDayOfWeek,
					toLessonOfDay,  toCourseIds,toCourseTypes,toMcGroupId,classId,week,weekOfEnd,
					ruleConflict,schoolId,timetableId,tempAjustList,evryWeekTbAllNj,taskMap, groupId);
			
			List<TCRCATemporaryAdjustRecord> needInsertRecord = (List<TCRCATemporaryAdjustRecord>) needInsertList.get("needInsertRecord");
			List<TCRCATemporaryAdjustResult> needInsertResult =	 (List<TCRCATemporaryAdjustResult>) needInsertList.get("needInsertResult");
			List<TCRCATemporaryAdjustResult> needDeleteResult = (List<TCRCATemporaryAdjustResult>) needInsertList.get("needDeleteResult");
			
			for(TCRCATemporaryAdjustRecord record:needInsertRecord){
				record.setGroupId(groupId);
				record.setStep(step);
			}
			for(TCRCATemporaryAdjustResult result:needInsertResult){
				result.setGroupId(groupId);
			}
			timetableDao.insertMultiEntity(needInsertRecord);
			timetableDao.insertMultiEntity(needInsertResult);
			timetableDao.insertMultiEntity(needInsertGroupInfo);
			timetableDao.insertMultiEntity(needInsertGroupParam);
			timetableDao.deleteMultiTpTbRecord(needDeleteResult);
		}catch(Exception e){
			e.printStackTrace();
			code = -1;
			 msg = "操作失败！";
		}

		JSONObject rs = new JSONObject();
		rs.put("code", code);
		rs.put("msg", msg);
		
		return rs;
	}
	
	@Override
	public JSONObject updateTemporaryLessonPositionByTeacher(School school, String timetableId,
			  Integer fromDayOfWeek, Integer fromLessonOfDay,
			JSONArray  fromCourseArr, Integer toDayOfWeek, Integer toLessonOfDay,
			JSONArray  toCourseArr,  
			String sessionID, int week, int weekOfEnd,String groupId) {
		// TODO Auto-generated method stub
		long d1 = new Date().getTime();                          
		String schoolId = school.getId() + "";
		JSONObject timetableInfo = this.timetableService
				.getArrangeTimetableInfo(schoolId, timetableId);

		String schoolYear = timetableInfo.getString("SchoolYear");
		String termName = timetableInfo.getString("TermName");

		JSONObject hcData = loadDataByMapTask(  sessionID, schoolId,
				timetableId, schoolYear+termName, school);
		RuleConflict ruleConflict = (RuleConflict) hcData.get("ruleConflict"+schoolYear+termName);
		ruleConflict.setSchoolYear(schoolYear);
		ruleConflict.setTermName(termName);

		List<JSONObject> arrangeList = (List<JSONObject>) hcData
				.get("arrangeList");
		HashMap<String, Object> cxMap = new HashMap<String, Object>();
		cxMap.put("timetableId", timetableId);
		cxMap.put("schoolId", school.getId() + "");
		cxMap.put("week", week);
		cxMap.put("weekOfEnd", weekOfEnd);
		cxMap.put("xn", schoolYear);
		cxMap.put("xq", termName);
		cxMap.put("groupId", groupId);
		// 取 临时调课结果
		List<JSONObject> tempAjustList = timetableService
				.getTemporaryAjustList(cxMap);
		
		TCRCATemporaryAdjustGroupInfo groupInfo = timetableService.getSingleTempAjustGroup(cxMap);
		
		List<TCRCATemporaryAdjustGroupInfo> needInsertGroupInfo = new ArrayList<TCRCATemporaryAdjustGroupInfo>();
		List<TCRCATemporaryAdjustGroupParam> needInsertGroupParam = new ArrayList<TCRCATemporaryAdjustGroupParam>();
		
		int step = 1;
		if(groupInfo==null){
			groupInfo = new TCRCATemporaryAdjustGroupInfo();
			groupInfo.setGroupId(groupId);
			groupInfo.setGroupMinWeek(week);
			groupInfo.setGroupMaxWeek(weekOfEnd);
			groupInfo.setGroupStep(1);
			groupInfo.setIsCrossClass(0);
			groupInfo.setNotStartCancel(0);
			groupInfo.setNotStartOperate(0);
			groupInfo.setXn(schoolYear);
			groupInfo.setXqm(termName);
			groupInfo.setSchoolId(schoolId);
			groupInfo.setTimetableId(timetableId);
			int loop = weekOfEnd;
			if(loop==0){
				loop = week;
			}
			for(int i=week;i<=loop;i++){
				TCRCATemporaryAdjustGroupParam param = new TCRCATemporaryAdjustGroupParam();
				param.setXn(schoolYear);
				param.setXn(schoolYear);
				param.setXqm(termName);
				param.setSchoolId(schoolId);
				param.setTimetableId(timetableId);
				param.setGroupId(groupId);
				param.setWeek(i);
				param.setGroupMoveTimes(1);
				param.setGroupStep(1);
				needInsertGroupParam.add(param);
			}
		}else{
			List<TCRCATemporaryAdjustGroupParam> needUpdateParams = timetableService.getSingleTempAjustGroupParam(cxMap);
			HashMap<Integer,TCRCATemporaryAdjustGroupParam> paramMaps = new HashMap<Integer, TCRCATemporaryAdjustGroupParam>();
			for(  TCRCATemporaryAdjustGroupParam param:needUpdateParams){
				paramMaps.put(param.getWeek(), param);
			}
			//如果出现非起始周调课
			if(week>groupInfo.getGroupMinWeek()){
				int x = groupInfo.getNotStartOperate();
				if(x==0||x>week){
					//保持为最小周次
					x = week;
				}
				groupInfo.setNotStartOperate(x);
			}
			if(weekOfEnd>groupInfo.getGroupMaxWeek()){
				groupInfo.setGroupMaxWeek(weekOfEnd);
			}
			step = groupInfo.getGroupStep()+1;
			int loop = weekOfEnd;
			if(loop==0){
				loop = week;
			}
			for(int i=week;i<=loop;i++){
				if(paramMaps.get(i)!=null){
					TCRCATemporaryAdjustGroupParam param = paramMaps.get(i);
					int groupMoveTimes = param.getGroupMoveTimes();
					param.setGroupMoveTimes(groupMoveTimes+1 );
					param.setGroupStep(step);
					needInsertGroupParam.add(param);
				}else{
					TCRCATemporaryAdjustGroupParam param = new TCRCATemporaryAdjustGroupParam();
					param.setXn(schoolYear);
					param.setXn(schoolYear);
					param.setXqm(termName);
					param.setSchoolId(schoolId);
					param.setTimetableId(timetableId);
					param.setGroupId(groupId);
					param.setWeek(i);
					param.setGroupMoveTimes(1);
					param.setGroupStep(step);
					needInsertGroupParam.add(param);
				}
			}
		}
		//判断组是否跨班
		if(fromCourseArr!=null&&fromCourseArr.size()>0
				&&toCourseArr!=null&&toCourseArr.size()>0&&groupInfo.getIsCrossClass()==0){
			groupInfo.setIsCrossClass(1);
		}
		groupInfo.setGroupStep(step);
		needInsertGroupInfo.add(groupInfo);
		//调课组逻辑结束
		HashMap<String, HashMap<String, HashMap<String, String>>> taskMap = (HashMap<String, HashMap<String, HashMap<String, String>>>) hcData
				.get("taskMap");
		this.fillTeachersToArrangeList(ruleConflict , arrangeList, taskMap);
		// 已排课表-正式课表 天次，节次-课程课时
		HashMap<String, List<JSONObject>> arrangeMap = new HashMap<String, List<JSONObject>>();
		for (JSONObject arrange : arrangeList) {
			List<JSONObject> list = new ArrayList<JSONObject>();
			int dow = arrange.getIntValue("DayOfWeek");
			int lod = arrange.getIntValue("LessonOfDay");
			String _classId = arrange.getString("ClassId");
			String key = dow + "," + lod+","+_classId;
			if (arrangeMap.containsKey(key)) {
				list = arrangeMap.get(key);
			} else {
				arrangeMap.put(key, list);
			}
			list.add(arrange);
		}
		
		HashMap<Integer, HashMap<String, List<JSONObject>>> evryWeekTbAllNj = TimetableUtil
					.getEvryWeekTimeTableByParam(arrangeMap, tempAjustList, week,
							weekOfEnd,ruleConflict);
		int code = 0;
		String msg = "操作成功！";
		try{
			
			JSONObject needInsertList = TimetableUtil.getNeedRecordAdjustedListByAjstTeacher(arrangeMap, fromDayOfWeek,
					fromLessonOfDay, fromCourseArr, toDayOfWeek,
					toLessonOfDay,  toCourseArr,week,weekOfEnd,
					ruleConflict,schoolId,timetableId,tempAjustList,evryWeekTbAllNj,taskMap, groupId);
			
//			JSONObject needInsertList = TimetableUtil.getNeedRecordAdjustedList(arrangeMap, fromDayOfWeek,
//					fromLessonOfDay,  fromCourseIds, fromCourseTypes, toDayOfWeek,
//					toLessonOfDay,  toCourseIds,toCourseTypes,classId,week,weekOfEnd,
//					ruleConflict,schoolId,timetableId,tempAjustList,evryWeekTbAllNj);
//			
			List<TCRCATemporaryAdjustRecord> needInsertRecord = (List<TCRCATemporaryAdjustRecord>) needInsertList.get("needInsertRecord");
			List<TCRCATemporaryAdjustResult> needInsertResult =	 (List<TCRCATemporaryAdjustResult>) needInsertList.get("needInsertResult");
			List<TCRCATemporaryAdjustResult> needDeleteResult = (List<TCRCATemporaryAdjustResult>) needInsertList.get("needDeleteResult");
			
			for(TCRCATemporaryAdjustRecord record:needInsertRecord){
				record.setStep(step);
				record.setGroupId(groupId);
				record.setWeekOfEnd(weekOfEnd);
			}
			timetableDao.insertMultiEntity(needInsertRecord);
			timetableDao.insertMultiEntity(needInsertResult);
			timetableDao.insertMultiEntity(needInsertGroupInfo);
			timetableDao.insertMultiEntity(needInsertGroupParam);
			timetableDao.deleteMultiTpTbRecord(needDeleteResult);
		}catch(Exception e){
			e.printStackTrace();
			code = -1;
			 msg = "操作失败！";
		}

		JSONObject rs = new JSONObject();
		rs.put("code", code);
		rs.put("msg", msg);
		
		return rs;
	}

	@Override
	public JSONObject getCanAdjustTeacherList(School school,
			String timetableId, JSONArray classCourse,  String sessionID, int week,
			int weekOfEnd, int dayOfWeek, int lessonOfDay, int breakRule) {
		// TODO Auto-generated method stub
		long d1 = new Date().getTime();
		String schoolId = school.getId() + "";
		JSONObject timetableInfo = this.timetableService
				.getArrangeTimetableInfo(schoolId, timetableId);

		String schoolYear = timetableInfo.getString("SchoolYear");
		String termName = timetableInfo.getString("TermName");

		JSONObject hcData = loadDataByMapTask(  sessionID, schoolId,
				timetableId, schoolYear+termName, school);
		RuleConflict ruleConflict = (RuleConflict) hcData.get("ruleConflict"+schoolYear+termName);
		ruleConflict.setSchoolYear(schoolYear);
		ruleConflict.setTermName(termName);

		List<JSONObject> taskList = (List<JSONObject>) hcData.get("taskList");
		List<JSONObject> teachers = TimetableUtil.getTeachersByTask(taskList,classCourse);
		List<JSONObject> arrangeList = (List<JSONObject>) hcData
				.get("arrangeList");
		HashMap<String, Object> cxMap = new HashMap<String, Object>();
		cxMap.put("timetableId", timetableId);
		cxMap.put("schoolId", school.getId() + "");
		cxMap.put("week", week);
		cxMap.put("weekOfEnd", weekOfEnd);
		cxMap.put("xn", schoolYear);
		cxMap.put("xq", termName);
		// 取 临时调课结果
		List<JSONObject> tempAjustList = timetableService
				.getTemporaryAjustList(cxMap);

		HashMap<String, HashMap<String, HashMap<String, String>>> taskMap = (HashMap<String, HashMap<String, HashMap<String, String>>>) hcData
				.get("taskMap");
		this.fillTeachersToArrangeList(ruleConflict , arrangeList, taskMap);
		// 已排课表-正式课表 天次，节次-课程课时
		HashMap<String, List<JSONObject>> arrangeMap = new HashMap<String, List<JSONObject>>();
		for (JSONObject arrange : arrangeList) {
			List<JSONObject> list = new ArrayList<JSONObject>();
			int dow = arrange.getIntValue("DayOfWeek");
			int lod = arrange.getIntValue("LessonOfDay");
			String key = dow + "," + lod ;
//			String key = dow + "," + lod+","+_classId;
			if (arrangeMap.containsKey(key)) {
				list = arrangeMap.get(key);
			} else {
				arrangeMap.put(key, list);
			}

			list.add(arrange);

		}
		// 临时调课课表-当前周
//		HashMap<String, List<JSONObject>> curWeekTb = TimetableUtil
//				.getSingleWeekTimeTableByParam(arrangeMap, tempAjustList, week,
//						classId);
//		
		// 临时调课课表-跨周-全年级
		HashMap<String, List<JSONObject>> curAllWeekTbAllNj = TimetableUtil
				.getAllWeekTimeTableByParam(arrangeMap, tempAjustList, week,
						weekOfEnd,ruleConflict);
		List<JSONObject> tlist =  TimetableUtil.changeMapListToList(curAllWeekTbAllNj);
		// 已排课表-正式课表 天次，节次-课程课时
		arrangeMap = new HashMap<String, List<JSONObject>>();
		for (JSONObject arrange : tlist) {
			List<JSONObject> list = new ArrayList<JSONObject>();
			int dow = arrange.getIntValue("DayOfWeek");
			int lod = arrange.getIntValue("LessonOfDay");
			String key = dow + "," + lod;
			if (arrangeMap.containsKey(key)) {
				list = arrangeMap.get(key);
			} else {
				arrangeMap.put(key, list);
			}

			list.add(arrange);

		}
		List<JSONObject> tttlist = arrangeMap.get("2,2");
		tttlist = GrepUtil.grepJsonKeyBySingleVal("ClassId", "2038657", tttlist);
		int code = 0;
		String msg = "查询成功！";
		List<JSONObject> rsList  = TimetableUtil.getNoConflictTeachers
				(teachers,classCourse,dayOfWeek, lessonOfDay, arrangeMap, ruleConflict, 0, breakRule);
		
		if(rsList.size()==0){
			msg = "无符合条件数据！";
			code = -1;
		}
		JSONObject rs = new JSONObject();
		rs.put("code", code);
		rs.put("msg", msg);
		rs.put("data", rsList);
		return rs;
	}

	@Override
	public JSONObject updateAdjustBySet(School school, String timetableId,
			JSONArray classCourse,  
			String sessionID, int week, int weekOfEnd, int dayOfWeek, int lessonOfDay,
			String fromTeachers,String toTeachers,String aType,String setCourseId,String groupId) {
		// TODO Auto-generated method stub
		
		long d1 = new Date().getTime();
		String schoolId = school.getId() + "";
		JSONObject timetableInfo = this.timetableService
				.getArrangeTimetableInfo(schoolId, timetableId);

		String schoolYear = timetableInfo.getString("SchoolYear");
		String termName = timetableInfo.getString("TermName");

		JSONObject hcData = loadDataByMapTask(  sessionID, schoolId,
				timetableId, schoolYear+termName, school);
		RuleConflict ruleConflict = (RuleConflict) hcData.get("ruleConflict"+schoolYear+termName);
		ruleConflict.setSchoolYear(schoolYear);
		ruleConflict.setTermName(termName);

		HashMap<String, Object> cxMap = new HashMap<String, Object>();
		cxMap.put("timetableId", timetableId);
		cxMap.put("schoolId", school.getId() + "");
		cxMap.put("week", week);
		cxMap.put("weekOfEnd", weekOfEnd);
		cxMap.put("xn", schoolYear);
		cxMap.put("xq", termName);
		cxMap.put("groupId", groupId);
		// 取 临时调课结果
		List<JSONObject> tempAjustList = timetableService
				.getTemporaryAjustList(cxMap);
		TCRCATemporaryAdjustGroupInfo groupInfo = timetableService.getSingleTempAjustGroup(cxMap);
		
		List<TCRCATemporaryAdjustGroupInfo> needInsertGroupInfo = new ArrayList<TCRCATemporaryAdjustGroupInfo>();
		List<TCRCATemporaryAdjustGroupParam> needInsertGroupParam = new ArrayList<TCRCATemporaryAdjustGroupParam>();
		
		int step = 1;
		if(groupInfo==null){
			groupInfo = new TCRCATemporaryAdjustGroupInfo();
			groupInfo.setGroupId(groupId);
			groupInfo.setGroupMinWeek(week);
			if(weekOfEnd==0){
				groupInfo.setGroupMaxWeek(week );
			}else{
				groupInfo.setGroupMaxWeek(weekOfEnd);
			}
			groupInfo.setGroupStep(1);
			groupInfo.setIsCrossClass(0);
			groupInfo.setNotStartCancel(0);
			groupInfo.setNotStartOperate(0);
			groupInfo.setXn(schoolYear);
			groupInfo.setXqm(termName);
			groupInfo.setSchoolId(schoolId);
			groupInfo.setTimetableId(timetableId);
			int mxIx = week;
			if(weekOfEnd!=0){
				mxIx = weekOfEnd;
			}
			for(int i=week;i<=mxIx;i++){
				TCRCATemporaryAdjustGroupParam param = new TCRCATemporaryAdjustGroupParam();
				param.setXn(schoolYear);
				param.setXn(schoolYear);
				param.setXqm(termName);
				param.setSchoolId(schoolId);
				param.setTimetableId(timetableId);
				param.setGroupId(groupId);
				param.setWeek(i);
				param.setGroupMoveTimes(0);
				param.setGroupStep(1);
				needInsertGroupParam.add(param);
			}
		}else{
			List<TCRCATemporaryAdjustGroupParam> needUpdateParams = timetableService.getSingleTempAjustGroupParam(cxMap);
			HashMap<Integer,TCRCATemporaryAdjustGroupParam> paramMaps = new HashMap<Integer, TCRCATemporaryAdjustGroupParam>();
			for(  TCRCATemporaryAdjustGroupParam param:needUpdateParams){
				paramMaps.put(param.getWeek(), param);
			}
			//如果出现非起始周调课
			if(week>groupInfo.getGroupMinWeek()){
				int x = groupInfo.getNotStartOperate();
				if(x==0||x>week){
					//保持为最小周次
					x = week;
				}
				groupInfo.setNotStartOperate(x);
			}
			if(weekOfEnd>groupInfo.getGroupMaxWeek()){
				groupInfo.setGroupMaxWeek(weekOfEnd);
			}
			step = groupInfo.getGroupStep() +1;
			groupInfo.setGroupStep(step );
			
			int loop = weekOfEnd;
			if(loop==0){
				loop = week;
			}
			for(int i=week;i<=loop;i++){
				if(paramMaps.get(i)!=null){
					TCRCATemporaryAdjustGroupParam param = paramMaps.get(i);
					param.setGroupStep(step);
					needInsertGroupParam.add(param);
				}else{
					TCRCATemporaryAdjustGroupParam param = new TCRCATemporaryAdjustGroupParam();
					param.setXn(schoolYear);
					param.setXn(schoolYear);
					param.setXqm(termName);
					param.setSchoolId(schoolId);
					param.setTimetableId(timetableId);
					param.setGroupId(groupId);
					param.setWeek(i);
					param.setGroupMoveTimes(1);
					param.setGroupStep(step);
					needInsertGroupParam.add(param);
				}
			}
		}
		needInsertGroupInfo.add(groupInfo);
		//调课组逻辑结束
		
		List<JSONObject> arrangeList = (List<JSONObject>) hcData
				.get("arrangeList");
		HashMap<String, HashMap<String, HashMap<String, String>>> taskMap = (HashMap<String, HashMap<String, HashMap<String, String>>>) hcData
				.get("taskMap");
		this.fillTeachersToArrangeList(ruleConflict , arrangeList, taskMap);
		// 已排课表-正式课表 天次，节次-课程课时
		HashMap<String, List<JSONObject>> arrangeMap = new HashMap<String, List<JSONObject>>();
		for (JSONObject arrange : arrangeList) {
			List<JSONObject> list = new ArrayList<JSONObject>();
			int dow = arrange.getIntValue("DayOfWeek");
			int lod = arrange.getIntValue("LessonOfDay");
			String _classId = arrange.getString("ClassId");
			String key = dow + "," + lod+","+_classId;
			if (arrangeMap.containsKey(key)) {
				list = arrangeMap.get(key);
			} else {
				arrangeMap.put(key, list);
			}

			list.add(arrange);

		}
		
		HashMap<Integer, HashMap<String, List<JSONObject>>> evryWeekTbAllNj = TimetableUtil
					.getEvryWeekTimeTableByParam(arrangeMap, tempAjustList, week,
							weekOfEnd,ruleConflict);
		int code = 0;
		String msg = "操作成功！";
		try{
			if(!aType.equals("2")){
				HashMap<String, List<JSONObject>> toWeek = evryWeekTbAllNj.get(week);
				JSONObject json = classCourse.getJSONObject(0);
				String classId = json.getString("classId");
				String key = dayOfWeek+","+lessonOfDay+","+classId;
				if(toWeek.containsKey(key) ){
					List<JSONObject> list = toWeek.get(key);
					if(fromTeachers==null&&list.size()>0){
						fromTeachers = list.get(0).getString("Teachers");
						if(!aType.equals("2")){
							
							toTeachers = fromTeachers;
						}
					}
				}
			}
			JSONObject needInsertList = TimetableUtil.getNeedRecordAdjustedListBySet(
					arrangeMap,dayOfWeek,lessonOfDay, week,weekOfEnd,
					ruleConflict,schoolId,timetableId,tempAjustList,classCourse, fromTeachers, toTeachers, aType, setCourseId, evryWeekTbAllNj, groupId);
			
			List<TCRCATemporaryAdjustRecord> needInsertRecord = (List<TCRCATemporaryAdjustRecord>) needInsertList.get("needInsertRecord");
			List<TCRCATemporaryAdjustResult> needInsertResult =	 (List<TCRCATemporaryAdjustResult>) needInsertList.get("needInsertResult");
			List<TCRCATemporaryAdjustResult> needDeleteResult = (List<TCRCATemporaryAdjustResult>) needInsertList.get("needDeleteResult");
			
			
			timetableDao.deleteMultiTpTbRecord(needDeleteResult);
			for(TCRCATemporaryAdjustRecord record:needInsertRecord){
				record.setStep(step);
			}
			timetableDao.insertMultiEntity(needInsertRecord);
			timetableDao.insertMultiEntity(needInsertResult);
			timetableDao.insertMultiEntity(needInsertGroupInfo);
			timetableDao.insertMultiEntity(needInsertGroupParam);
		}catch(Exception e){
			e.printStackTrace();
			code = -1;
			 msg = "操作失败！";
		}

		JSONObject rs = new JSONObject();
		rs.put("code", code);
		rs.put("msg", msg);
		
		return rs;
	}

	@Override
	public JSONObject updateAdjustByDelAdjust(School school,
			String timetableId, JSONArray classCourse,  String sessionID, int week,
			int weekOfEnd, int dayOfWeek, int lessonOfDay,String selfStuCode,  String groupId) {
		// TODO Auto-generated method stub
		int curWeek = week;
		String schoolId = school.getId() + "";
		JSONObject timetableInfo = this.timetableService
				.getArrangeTimetableInfo(schoolId, timetableId);

		String schoolYear = timetableInfo.getString("SchoolYear");
		String termName = timetableInfo.getString("TermName");

		JSONObject hcData = loadDataByMapTask(  sessionID, schoolId,
				timetableId, schoolYear+termName, school);
		RuleConflict ruleConflict = (RuleConflict) hcData.get("ruleConflict"+schoolYear+termName);
		ruleConflict.setSchoolYear(schoolYear);
		ruleConflict.setTermName(termName);

		HashMap<String, Object> cxMap = new HashMap<String, Object>();
		cxMap.put("timetableId", timetableId);
		cxMap.put("schoolId", school.getId() + "");
		
		//取全部调整记录
		cxMap.put("week", 1);
			
		weekOfEnd = 21;
		cxMap.put("weekOfEnd", weekOfEnd);
		cxMap.put("xn", schoolYear);
		cxMap.put("xq", termName);
		// 取 临时调课结果
		List<JSONObject> tempAjustList = timetableService
				.getTemporaryAjustList(cxMap);
		
		// 取 临时调课记录 
		List<JSONObject> tempAjustRecord = timetableService
				.getTemporaryAjustRecord(cxMap);
		
		List<JSONObject> arrangeList = (List<JSONObject>) hcData
				.get("arrangeList");
		HashMap<String, HashMap<String, HashMap<String, String>>> taskMap = (HashMap<String, HashMap<String, HashMap<String, String>>>) hcData
				.get("taskMap");
		this.fillTeachersToArrangeList(ruleConflict , arrangeList, taskMap);
		// 已排课表-正式课表 天次，节次-课程课时
		HashMap<String, List<JSONObject>> arrangeMap = new HashMap<String, List<JSONObject>>();
		for (JSONObject arrange : arrangeList) {
			List<JSONObject> list = new ArrayList<JSONObject>();
			int dow = arrange.getIntValue("DayOfWeek");
			int lod = arrange.getIntValue("LessonOfDay");
			String _classId = arrange.getString("ClassId");
			String key = dow + "," + lod+","+_classId;
			if (arrangeMap.containsKey(key)) {
				list = arrangeMap.get(key);
			} else {
				arrangeMap.put(key, list);
			}

			list.add(arrange);

		}
		
		
		
		List<TCRCATemporaryAdjustGroupInfo> needInsertGroupInfo = new ArrayList<TCRCATemporaryAdjustGroupInfo>();
		List<TCRCATemporaryAdjustGroupParam> needInsertGroupParam = new ArrayList<TCRCATemporaryAdjustGroupParam>();
		JSONObject needRecall = null;
		List<TCRCATemporaryAdjustRecord> needInsertRecord =	 new ArrayList<TCRCATemporaryAdjustRecord>();
		TCRCATemporaryAdjustGroupInfo groupInfo = null;
		//找不到调课记录
		boolean noRecord  = false;
		if(!groupId.equals("-100")){
			List<JSONObject> records = GrepUtil.grepJsonKeyByVal(new String[]{"Week","GroupId"},
					new String[]{week+"",groupId }, tempAjustRecord);
			int step = 0;
			HashMap<Integer,JSONObject> stepMaps = new HashMap<Integer, JSONObject>();
			for(JSONObject re:records){
				//大于10 为撤销操作的记录
				if(re.getIntValue("Step")>step&&re.getIntValue("AdjustType")<10){
					step = re.getIntValue("Step");
					stepMaps.put(step, re);
				}
			}
			
			needRecall = stepMaps.get(step);
			if(needRecall==null){
				noRecord = true;
			}else{
				//获取上一步的最大结束周次
				int maxWeekOfEnd = 0 ;
				int minstart = 100;
				List<JSONObject> records2 = GrepUtil.grepJsonKeyBySingleVal("GroupId", groupId, tempAjustRecord);
				for(JSONObject cod:records2){
					if(cod.getIntValue("AdjustType")<9&&cod.getIntValue("Step")!=step){
						
						int WeekOfEnd = cod.getIntValue("WeekOfEnd");
						if(WeekOfEnd>maxWeekOfEnd&&WeekOfEnd!=0){
							maxWeekOfEnd = WeekOfEnd ;
						}
						int WeekOfStart = cod.getIntValue("WeekOfStart");
						if(WeekOfStart<minstart){
							minstart =  WeekOfStart;
						}
					}
				}
				int WeekOfStart = needRecall.getIntValue("WeekOfStart");
				int WeekOfEnd = needRecall.getIntValue("WeekOfEnd");
				curWeek = needRecall.getIntValue("Week");
				int AdjustType = needRecall.getIntValue("AdjustType");
				weekOfEnd = WeekOfEnd;
				cxMap.put("groupId", groupId);
				 groupInfo = timetableService.getSingleTempAjustGroup(cxMap);
				if(groupInfo==null){
					 
				}else{
					//如果出现非起始周撤销操作
					if( WeekOfStart!=week ){
						groupInfo.setNotStartCancel(week);
					}
					
					if(minstart>WeekOfStart&&WeekOfStart!=week){
						groupInfo.setGroupMinWeek(WeekOfStart);
					}else{
						groupInfo.setGroupMinWeek(minstart);
					}
					if(maxWeekOfEnd<groupInfo.getGroupMinWeek()){
						maxWeekOfEnd = groupInfo.getGroupMinWeek();
					}
					groupInfo.setGroupMaxWeek(maxWeekOfEnd);
				}
				//属于起始周操作
				if(WeekOfStart==week&&step==groupInfo.getGroupStep()){
					groupInfo.setGroupStep(step-1);
				}
				if(WeekOfStart!=week){
					//虽然是非起始周 但是只操作过一次
					if(groupInfo.getGroupStep()==1){
						groupInfo.setGroupStep(1);
						groupInfo.setGroupMaxWeek(week-1);
						groupInfo.setNotStartCancel(0);
						groupInfo.setNotStartOperate(0);
						cxMap.put("groupWeekOfEnd", week-1);
					}else {
						//非起始周 但是在本周本组属于最后一次操作
						int notStartCancel = 0;
						int notStartOperate = 0;
						for(JSONObject cod:records2){
							int cAjstType = cod.getIntValue("AdjustType");
							int cWeek = cod.getInteger("Week");
							int cWeekStart = cod.getInteger("WeekOfStart");
							if(cAjstType<9&&cWeek<week&&cWeekStart!=groupInfo.getGroupMinWeek()){
								if((notStartOperate==0) ||(notStartOperate>cWeek )){
									notStartOperate = cWeek;
								}
							}
						}
						
						if(needRecall.getIntValue("Step")==1&&groupInfo.getGroupStep()!=1){
							groupInfo.setGroupMaxWeek(week-1);
						}
							
						if(groupInfo.getNotStartCancel()>groupInfo.getGroupMaxWeek()){
							groupInfo.setNotStartCancel(0);
						}
						groupInfo.setNotStartOperate(notStartOperate);
						cxMap.put("groupWeekOfEnd", week-1);
						cxMap.put("groupCancleStep", step);
					}
				}
				
				needInsertGroupInfo.add(groupInfo);
				cxMap.put("week", week);
				if(WeekOfEnd==0){
					cxMap.put("weekOfEnd", week);
				}else{
					cxMap.put("weekOfEnd", WeekOfEnd);
				}
				List<TCRCATemporaryAdjustGroupParam> needUpdateParams = timetableService.getSingleTempAjustGroupParam(cxMap);
				HashMap<Integer,TCRCATemporaryAdjustGroupParam> paramMap = new HashMap<Integer, TCRCATemporaryAdjustGroupParam>();
				for(TCRCATemporaryAdjustGroupParam param:needUpdateParams){
					if(AdjustType==1){
						param.setGroupMoveTimes(param.getGroupMoveTimes()-1);
					}
					param.setGroupStep(step-1);
					needInsertGroupParam.add(param);
					paramMap.put(param.getWeek(), param);
				}
				cxMap.put("week", 1);
				cxMap.put("weekOfEnd", 21);
				List<TCRCATemporaryAdjustGroupParam> needCxs = timetableService.getSingleTempAjustGroupParam(cxMap);
				for(TCRCATemporaryAdjustGroupParam param:needCxs){
					if(!paramMap.containsKey(param.getWeek())){
						
						paramMap.put(param.getWeek(), param);
					}
				}
				if(groupInfo.getNotStartOperate()!=0){
					int op = groupInfo.getNotStartOperate();
					int nStep = -1;
					int lStep = -1;
					if(paramMap.containsKey(op)){
						nStep = paramMap.get(op).getGroupStep();
					}
					if(paramMap.containsKey(op-1)){
						lStep = paramMap.get(op-1).getGroupStep();
					}
					
					if(nStep !=-1 && lStep !=-1 &&nStep == lStep){
						groupInfo.setNotStartOperate(0);
					}
				}
				//调课组逻辑结束
				//非起始周操作时 记录该撤销
				if(WeekOfStart!=week){
					JSONObject recallObj = BeanTool.castBeanToFirstLowerKey(needRecall);
					TCRCATemporaryAdjustRecord tcrRecall = JSON.toJavaObject(recallObj, TCRCATemporaryAdjustRecord.class);
					for(  JSONObject rt: tempAjustRecord){
						JSONObject record = BeanTool.castBeanToFirstLowerKey(rt);
						TCRCATemporaryAdjustRecord tcr = JSON.toJavaObject(record, TCRCATemporaryAdjustRecord.class);
						if(tcr.getGroupId().equals(tcrRecall.getGroupId())
//									&&tcr.getClassId().equals(tcrRecall.getClassId())
//								&&tcr.getTargetDay()==tcrRecall.getTargetDay()
//								&&tcr.getTargetLesson()==tcrRecall.getTargetLesson()
//								&&tcr.getOriginalDay()==tcrRecall.getOriginalDay()
//								&&tcr.getOriginalLesson()==tcrRecall.getOriginalLesson()
//								&&tcr.getOriginalSubject().equals(tcrRecall.getOriginalSubject())
								&&tcr.getAdjustType().equals(tcrRecall.getAdjustType())
								&&tcr.getWeek()==week
								&&tcr.getStep()==tcrRecall.getStep()
								){
							for(int w = week;w<=weekOfEnd;w++){
								JSONObject recordcopy = BeanTool.castBeanToFirstLowerKey(rt);
								TCRCATemporaryAdjustRecord tcr2 = JSON.toJavaObject(recordcopy, TCRCATemporaryAdjustRecord.class);
								tcr2.setAdjustType((10+Integer.parseInt(tcr2.getAdjustType()))+"");
								tcr2.setRecordId(UUIDUtil.getUUID());
								tcr2.setWeekOfStart(week);
								tcr2.setWeek(w);
								tcr2.setStep(step);
								needInsertRecord.add(tcr2);
							}
						}
					}
				}
			}
			
		}
		
		
		HashMap<Integer, HashMap<String, List<JSONObject>>> evryWeekTbAllNj = TimetableUtil
					.getEvryWeekTimeTableByParam(arrangeMap, tempAjustList, week,
							weekOfEnd,ruleConflict);
		int code = 0;
		String msg = "操作成功！";
		try{
			if(!noRecord){
				JSONObject needInsertList = new JSONObject();
				needInsertList =TimetableUtil.getNeedRecordAdjustedListByDel(
						arrangeMap,dayOfWeek,lessonOfDay, week,weekOfEnd,
						ruleConflict,schoolId,timetableId,tempAjustList,tempAjustRecord, 
						classCourse, evryWeekTbAllNj,selfStuCode,groupId,needRecall);
				List<String> needDelRecord = (List<String>) needInsertList.get("needDelRecordIDs");
				List<TCRCATemporaryAdjustResult> needInsertResult =	 
						(List<TCRCATemporaryAdjustResult>) needInsertList.get("needInsertResult");
//				if(!groupId.equals("-100")){
					
					cxMap.put("groupId", groupId);
//				}
				//起始周撤销操作时删除步骤数以上的撤销记录
				if(needRecall!=null&&groupInfo!=null&&needRecall.getInteger("Week")==groupInfo.getGroupMinWeek()){
					int thisStep = needRecall.getIntValue("Step");
					for(  JSONObject rt: tempAjustRecord){
						if(rt.getString("GroupId").equals(groupInfo.getGroupId())
								&&rt.getInteger("Step")>=thisStep&&rt.getInteger("AdjustType")>9){
							needDelRecord.add(rt.getString("RecordId"));
						}
					}
				}
				timetableDao.delMutilTmpAjstResult(cxMap);
				timetableDao.deleteMultiTmpRecord(needDelRecord);
				
				timetableDao.insertMultiEntity(needInsertResult);
				timetableDao.insertMultiEntity(needInsertRecord);
				timetableDao.insertMultiEntity(needInsertGroupInfo);
				timetableDao.insertMultiEntity(needInsertGroupParam);
				
//				if(!groupId.equals("-100")){
					
					timetableDao.updateCheckGroupExist(cxMap);
//				}
				if(cxMap.containsKey("groupWeekOfEnd")){
					cxMap.put("curWeek", curWeek);
					timetableDao.updateWeekAgoWeekOfEnd(cxMap);
				}
			}else{
				cxMap.put("groupId", groupId);
				cxMap.put("week", week);
				timetableDao.deleteNoRecordAjst(cxMap);
				timetableDao.updateCheckGroupExist(cxMap);
			}
				
			
		}catch(Exception e){
			e.printStackTrace();
			code = -1;
			 msg = "操作失败！";
		}

		JSONObject rs = new JSONObject();
		rs.put("code", code);
		rs.put("msg", msg);
		
		return rs;
	}

	@Override
	public JSONObject getTempAjustForFineTuningDataByGrade(School school,
			String timetableId, String gradeId, String sessionID, String weekDateStr,
			int weekOfEnd) {
		// TODO Auto-generated method stub
		String weekDate = weekDateStr.split("\\|")[1];
		int week = Integer.parseInt(weekDateStr.split("\\|")[0]);
		String schoolId = school.getId() + "";
		JSONObject timetableInfo = this.timetableService
				.getArrangeTimetableInfo(schoolId, timetableId);

		String schoolYear = timetableInfo.getString("SchoolYear");
		String termName = timetableInfo.getString("TermName");
		String termInfoId = schoolYear.concat(termName);

		JSONObject gradeSet = this.timetableService.getGradeSet(schoolId,
				timetableId, gradeId);
		if (gradeSet == null) {
			throw new RuntimeException("当前选择的年级下无课表，请更换年级");
		}

		JSONObject hcData = loadDataByMapTask(  sessionID, schoolId,
				timetableId, termInfoId, school);
		RuleConflict ruleConflict = (RuleConflict) hcData.get("ruleConflict"+schoolYear+termName);
		ruleConflict.setSchoolYear(schoolYear);
		ruleConflict.setTermName(termName);
		int maxDaysForWeek = timetableInfo.getIntValue("MaxDaysForWeek");

		int amLessonNum = gradeSet.getIntValue("AMLessonNum");
		int pmLessonNum = gradeSet.getIntValue("PMLessonNum");
		int maxLessonForDay = amLessonNum + pmLessonNum;
		List<JSONObject> taskList = (List<JSONObject>) hcData.get("taskList");
		List<JSONObject> arrangeList = (List<JSONObject>) hcData
				.get("arrangeList");
		HashMap<String, Object> cxMap = new HashMap<String, Object>();
		cxMap.put("timetableId", timetableId);
		cxMap.put("schoolId", school.getId() + "");
		cxMap.put("gradeId", gradeId);
		cxMap.put("week", week);
		cxMap.put("weekOfEnd", weekOfEnd);
		cxMap.put("xn", schoolYear);
		cxMap.put("xq", termName);
		// 取 临时调课结果
		List<JSONObject> tempAjustList = timetableService
				.getTemporaryAjustList(cxMap);

		HashMap<String, HashMap<String, HashMap<String, String>>> taskMap = (HashMap<String, HashMap<String, HashMap<String, String>>>) hcData
				.get("taskMap");
		this.fillTeachersToArrangeList(ruleConflict , arrangeList, taskMap);

		JSONObject resultData = new JSONObject();
		String[] weekDates = TimetableUtil.getWeekDatesByStart(weekDate,
				maxDaysForWeek);

		// 已排课表-正式课表 天次，节次-课程课时
		HashMap<String, List<JSONObject>> arrangeMap = new HashMap<String, List<JSONObject>>();
		for (JSONObject arrange : arrangeList) {
			List<JSONObject> list = new ArrayList<JSONObject>();
			int dow = arrange.getIntValue("DayOfWeek");
			int lod = arrange.getIntValue("LessonOfDay");
			String classId = arrange.getString("ClassId" );
			String key = dow + "," + lod+","+classId;
			if (arrangeMap.containsKey(key)) {
				list = arrangeMap.get(key);
			} else {
				arrangeMap.put(key, list);
			}

			list.add(arrange);

		}
		
		resultData.put("weekDate",weekDates);
		List<JSONObject> dataList = new ArrayList<JSONObject>();
		List<String> gradeIds = new ArrayList<String>();
		gradeIds.add(gradeId);
		List<Classroom> classInfos = this.timetableService.getClassByGradeIds(
				schoolId, termInfoId, gradeIds);
		
		// 临时调课课表-当前周
		HashMap<String, List<JSONObject>> curWeekTb = TimetableUtil
				.getSingleWeekTimeTableByParam(arrangeMap, tempAjustList, week,
						null,ruleConflict);
		List<JSONObject> tlist =  TimetableUtil.changeMapListToList(curWeekTb);
		
		for (Classroom classroom : classInfos) {
			String classId = String.valueOf(classroom.getId());
			JSONObject data = new JSONObject();
			
			List<JSONObject> classlist = GrepUtil.grepJsonKeyBySingleVal("ClassId", classId, tlist);
			List<JSONObject> classlist2 = TimetableUtil.fulFillClassTbList(classlist,ruleConflict,maxDaysForWeek,maxLessonForDay);
			data.put("classId", classId);
			data.put("className", ruleConflict.getClassroomsDic().get(classId).getClassName());
			data.put("timetable", classlist2);
			dataList.add(data);

		}
		try {
			dataList = (List<JSONObject>) Sort.sort(SortEnum.ascEnding0rder, dataList, "className");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		resultData.put("totalMaxDays", maxDaysForWeek);
		resultData.put("amLessonNum", amLessonNum);
		resultData.put("pmLessonNum", pmLessonNum);
		resultData.put("totalMaxLesson", maxLessonForDay);
		resultData.put("dataList", dataList);

		return resultData;

	}

	@Override
	public JSONObject getTempAjustPositionConflicts(School school,
			String timetableId, int dayOfWeek, int lessonOfDay, String gradeId,
			String classId, String courseIds, String sessionID, String weekDateStr,
			int weekOfEnd,String McGroupIds,int breakRule) {
		// TODO Auto-generated method stub
		String weekDate = weekDateStr.split("\\|")[1];
		int week = Integer.parseInt(weekDateStr.split("\\|")[0]);
		String schoolId = school.getId() + "";
		JSONObject timetableInfo = this.timetableService
				.getArrangeTimetableInfo(schoolId, timetableId);

		String schoolYear = timetableInfo.getString("SchoolYear");
		String termName = timetableInfo.getString("TermName");
		String termInfoId = schoolYear.concat(termName);

		JSONObject gradeSet = this.timetableService.getGradeSet(schoolId,
				timetableId, gradeId);
		if (gradeSet == null) {
			throw new RuntimeException("当前选择的年级下无课表，请更换年级");
		}

		JSONObject hcData = loadDataByMapTask(  sessionID, schoolId,
				timetableId, termInfoId, school);
		RuleConflict ruleConflict = (RuleConflict) hcData.get("ruleConflict"+schoolYear+termName);
		ruleConflict.setSchoolYear(schoolYear);
		ruleConflict.setTermName(termName);
		int maxDaysForWeek = timetableInfo.getIntValue("MaxDaysForWeek");

		int amLessonNum = gradeSet.getIntValue("AMLessonNum");
		int pmLessonNum = gradeSet.getIntValue("PMLessonNum");
		int maxLessonForDay = amLessonNum + pmLessonNum;
		List<JSONObject> taskList = (List<JSONObject>) hcData.get("taskList");
		List<JSONObject> arrangeList = (List<JSONObject>) hcData
				.get("arrangeList");
		HashMap<String, Object> cxMap = new HashMap<String, Object>();
		cxMap.put("timetableId", timetableId);
		cxMap.put("schoolId", school.getId() + "");
		cxMap.put("gradeId", gradeId);
		cxMap.put("week", week);
		cxMap.put("weekOfEnd", weekOfEnd);
		cxMap.put("xn", schoolYear);
		cxMap.put("xq", termName);
		// 取 临时调课结果
		List<JSONObject> tempAjustList = timetableService
				.getTemporaryAjustList(cxMap);
		List<JSONObject> tempAjustRecord = timetableService
				.getTemporaryAjustRecord(cxMap);

		
		HashMap<String, TCRCATemporaryAdjustGroupInfo> groupMap = timetableService.getTempAjustGroupList(cxMap);
		HashMap<String, HashMap<Integer, TCRCATemporaryAdjustGroupParam>> groupParamMap = timetableService.getTempAjustGroupParamMap(cxMap);
		
		HashMap<String, HashMap<String, HashMap<String, String>>> taskMap = (HashMap<String, HashMap<String, HashMap<String, String>>>) hcData
				.get("taskMap");
		this.fillTeachersToArrangeList(ruleConflict ,
				arrangeList, taskMap);
		JSONObject resultData = new JSONObject();
		String[] weekDates = TimetableUtil.getWeekDatesByStart(weekDate,
				maxDaysForWeek);

		// 已排课表-正式课表 天次，节次-课程课时
		HashMap<String, List<JSONObject>> arrangeMap = new HashMap<String, List<JSONObject>>();
		for (JSONObject arrange : arrangeList) {
			List<JSONObject> list = new ArrayList<JSONObject>();
			int dow = arrange.getIntValue("DayOfWeek");
			int lod = arrange.getIntValue("LessonOfDay");
			String key = dow + "," + lod;
			if (arrangeMap.containsKey(key)) {
				list = arrangeMap.get(key);
			} else {
				arrangeMap.put(key, list);
			}

			list.add(arrange);

		}
		JSONObject returnData = new JSONObject();
		int code = 0;
		String msg = "查询成功！";
		int isOperate = 1;
		int isCancel = 1;
		String failMsg = "";
		// 临时调课课表-当前周-全年级
		try{
			
			HashMap<String, List<JSONObject>> curWeekTbAllNj = TimetableUtil
					.getSingleWeekTimeTableByParam(arrangeMap, tempAjustList, week,
							null,ruleConflict);
			List<JSONObject> allList = TimetableUtil.changeMapListToList(curWeekTbAllNj);
//			allList =  TimetableUtil.getIsAdvancedByAjstRs(tempAjustList,week,weekOfEnd,allList);
			allList = TimetableUtil.getIsAdvancedPlusByAjstRs(tempAjustList,week,weekOfEnd,
					allList,tempAjustRecord,groupMap,groupParamMap,ruleConflict);
			TimetableUtil.loadConflictsByAdvancedPlus(allList,maxDaysForWeek,maxLessonForDay+1);
//			TimetableUtil.loadConflictsByAdvanced(allList,maxDaysForWeek,maxLessonForDay+1);
//			// 临时调课课表-跨周-全年级
//			HashMap<String, List<JSONObject>> allWeekTbAllNj = TimetableUtil
//					.getAllWeekTimeTableByParam(arrangeMap, tempAjustList, week,
//							weekOfEnd,ruleConflict);
//			
//			List<JSONObject> arrangeNowAll = TimetableUtil.changeMapListToList(allWeekTbAllNj);
//			// 已排课表-正式课表 天次，节次-课程课时--用于计算冲突
			arrangeMap = new HashMap<String, List<JSONObject>>();
			HashMap<String, List<JSONObject>> arrangeMapNoClassId = new HashMap<String, List<JSONObject>>();
			for (JSONObject arrange : allList) {
				List<JSONObject> list = new ArrayList<JSONObject>();
				List<JSONObject> list2 = new ArrayList<JSONObject>();
				int dow = arrange.getIntValue("DayOfWeek");
				int lod = arrange.getIntValue("LessonOfDay");
				String _classId = arrange.getString("ClassId");
				String key = dow + "," + lod +"," + _classId;
				String key2 = dow + "," + lod ;
				if (arrangeMap.containsKey(key)) {
					list = arrangeMap.get(key);
				} else {
					arrangeMap.put(key, list);
				}
				if (arrangeMapNoClassId.containsKey(key2)) {
					list2 = arrangeMapNoClassId.get(key2);
				} else {
					arrangeMapNoClassId.put(key2, list2);
				}
				
				list.add(arrange);
				list2.add(arrange);
				
			}
			List<JSONObject> tarLesson  = arrangeMap.get(dayOfWeek+","+lessonOfDay+","+classId);
			
			for(JSONObject tar:tarLesson){
				if(tar.containsKey("isOperate")&&tar.getIntValue("isOperate")==-1){
					isOperate = -1;
					failMsg = tar.getString("failMsg");
				}
				if(tar.containsKey("isCancel")&&tar.getIntValue("isCancel")==-1){
					isCancel = -1;
					failMsg = tar.getString("failMsg");
				}
			}
			String[] courses = courseIds.split(",");
			String[] mcGroups = McGroupIds.split(",");
			List<JSONObject> rs = new ArrayList<JSONObject>();
			for(int i=0;i<courses.length;i++){
				Classroom classroom = ruleConflict.getClassroomsDic().get(classId);
				Grade ddd = ruleConflict.getGradeIdGradeDic().get(
						classroom .getGradeId());
				String gradeLev = ddd.getCurrentLevel().getValue() + "";
				String courseId = courses[i];
				List<JSONObject> tarLessonTmp = GrepUtil.grepJsonKeyByVal(new String[]{"ClassId","CourseId"}, new String[]{classId,courseId}, tarLesson);
				
				if(tarLessonTmp.size()>0){
					JSONObject objNow = tarLessonTmp.get(0);
					Map<String, String> teacherparam = (Map<String, String>) objNow.get("teachers");
					List<JSONObject> confs = new ArrayList<JSONObject>();
					if(breakRule==0){
						
						confs = TimetableUtil.getConflictsWithSwap(classId, courseId,
								teacherparam  , maxDaysForWeek, maxLessonForDay, arrangeMapNoClassId,
								ruleConflict, gradeLev, -100,
								objNow.getIntValue("IsAdvance"),dayOfWeek,lessonOfDay,objNow.getString("McGroupId")
								,true,objNow.getIntValue("CourseType"),objNow.getIntValue("DayOfWeek"),false);
					}else{
						confs = TimetableUtil.getOnlyTeaConflictsWithSwap(classId, courseId,
								teacherparam  , maxDaysForWeek, maxLessonForDay, arrangeMapNoClassId,
								ruleConflict, gradeLev, -100,
								objNow.getIntValue("IsAdvance"),dayOfWeek,lessonOfDay,objNow.getString("McGroupId")
								,true,objNow.getIntValue("CourseType"),objNow.getIntValue("DayOfWeek"),false);
						
					}
					
					List<JSONObject> list2 = (List<JSONObject>) objNow.get("conflicts");
					confs.addAll(list2);
					rs.addAll(confs);
				}
				
			}
			returnData.put("conflicts", rs);
		}catch(Exception e){
			e.printStackTrace();
			code = -1;
			msg = "查询失败！";
		}
		returnData.put("code", code);
		returnData.put("msg", msg);
		returnData.put("isCancel", isCancel);
		returnData.put("failMsg", failMsg);
		returnData.put("isOperate", isOperate);
		return returnData;
	}

	@Override
	public JSONObject getAdjustRecordForClassList(School school,
			String timetableId,  
			String sessionID, String weekDateStr,int astType) {
		// TODO Auto-generated method stub
		String weekDate = weekDateStr.split("\\|")[1];
		int week = Integer.parseInt(weekDateStr.split("\\|")[0]);
		String schoolId = school.getId() + "";
		JSONObject timetableInfo = this.timetableService
				.getArrangeTimetableInfo(schoolId, timetableId);

		String schoolYear = timetableInfo.getString("SchoolYear");
		String termName = timetableInfo.getString("TermName");
		String termInfoId = schoolYear.concat(termName);


		JSONObject hcData = loadDataByMapTask( sessionID, schoolId,
				timetableId, termInfoId, school);
		RuleConflict ruleConflict = (RuleConflict) hcData.get("ruleConflict"+schoolYear+termName);
		ruleConflict.setSchoolYear(schoolYear);
		ruleConflict.setTermName(termName);
		int maxDaysForWeek = timetableInfo.getIntValue("MaxDaysForWeek");

		List<JSONObject> taskList = (List<JSONObject>) hcData.get("taskList");
		List<JSONObject> arrangeList = (List<JSONObject>) hcData
				.get("arrangeList");
		HashMap<String, HashMap<String, HashMap<String, String>>> taskMap = (HashMap<String, HashMap<String, HashMap<String, String>>>) hcData
				.get("taskMap");
		this.fillTeachersToArrangeList(ruleConflict , arrangeList, taskMap);
		HashMap<String, Object> cxMap = new HashMap<String, Object>();
		cxMap.put("timetableId", timetableId);
		cxMap.put("schoolId", school.getId() + "");
		cxMap.put("week", week);
		cxMap.put("weekOfEnd", 0);
		cxMap.put("xn", schoolYear);
		cxMap.put("xq", termName);
		// 取 临时调课结果
		List<JSONObject> tempAjustList = timetableService
				.getTemporaryAjustList(cxMap);
		
		cxMap.put("cxAdjustType", "9");
		List<JSONObject> tempAjustRecord = timetableService
				.getTemporaryAjustRecord(cxMap);

		JSONObject returnData = new JSONObject();
		int code = 0;
		String msg = "查询成功！";
		// 临时调课课表-当前周-全年级
		try{
			String[] weekDates = TimetableUtil.getWeekDatesByStart(weekDate,
					maxDaysForWeek);
			
			// 已排课表-正式课表 天次，节次-课程课时
			HashMap<String, List<JSONObject>> arrangeMap = new HashMap<String, List<JSONObject>>();
			for (JSONObject arrange : arrangeList) {
				List<JSONObject> list = new ArrayList<JSONObject>();
				int dow = arrange.getIntValue("DayOfWeek");
				int lod = arrange.getIntValue("LessonOfDay");
				String classId = arrange.getString("ClassId");
				String key = dow + "," + lod +","+classId;
				if (arrangeMap.containsKey(key)) {
					list = arrangeMap.get(key);
				} else {
					arrangeMap.put(key, list);
				}

				list.add(arrange);

			}
		
			List<JSONObject> list = TimetableUtil.getAdjustRecordData(tempAjustList,week,astType,ruleConflict,weekDates,arrangeMap,tempAjustRecord);
			returnData.put("data", list);
		}catch(Exception e){
			e.printStackTrace();
			logger.info(e.getMessage());
			code = -1;
			msg = "查询失败！";
		}
		returnData.put("code", code);
		returnData.put("msg", msg);
		return returnData;
	}

	@Override
	public JSONObject updateAdjustRecordPublished(School school,
			String timetableId, String weekStr, JSONArray courses,HttpSession session) {
		// TODO Auto-generated method stub
		List<JSONObject> list = new ArrayList<JSONObject>();
		JSONObject returnData = new JSONObject();
		int week = Integer.parseInt(weekStr.split("\\|")[0]);
		int code = 0;
		String msg = "操作成功！";
		//待推送消息
		List<JSONObject> noticeDetails = new ArrayList<JSONObject>();
		List<JSONObject> needUpdateRecordList = new ArrayList<JSONObject>();
		List<JSONObject> needUpdateResultList = new ArrayList<JSONObject>();
		//identy teachers 
		HashMap<String,String > identyTeachersMap = new HashMap<String, String>();
		for(int i=0;i<courses.size();i++){
			JSONObject course = courses.getJSONObject(i);
//			courses.getJSONObject(i).put("schoolId",school.getId()+"");
//			courses.getJSONObject(i).put("week",week);
//			courses.getJSONObject(i).put("timetableId",timetableId);
//			list.add(courses.getJSONObject(i));
			
			String sendAccounts = course.getString("sendAccounts");
			String identyKey = course.getString("identyKey");
			String putTeachers = "";
			if(identyTeachersMap.containsKey(identyKey)){
				putTeachers = identyTeachersMap.get(identyKey);
			}
			putTeachers+=sendAccounts+",";
			if(putTeachers.trim().length()>0){
				putTeachers = putTeachers.substring(0,putTeachers.length()-1);
			}
			identyTeachersMap.put(identyKey, putTeachers);
			
			JSONObject sendMsgObj = course.getJSONObject("sendMsg");
			String[] sendAccountsArr = sendAccounts.split(",");
			for(int j=0;j<sendAccountsArr.length;j++){
				String teacherAccountId = sendAccountsArr[j];
				if(sendMsgObj.containsKey(teacherAccountId)){
					JSONObject noticeDetail = new JSONObject();
					noticeDetail.put("accountId", teacherAccountId);
					noticeDetail.put("schoolId", school.getId());
					noticeDetail.put("noticeDetailsContent", sendMsgObj.get(teacherAccountId));
					noticeDetail.put("userType", "3");
					noticeDetail.put("userId", "");
					noticeDetail.put("classId", "");
					noticeDetail.put("isStatus", "0");
					noticeDetail.put("family", "");
					System.out.println(sendMsgObj.get(teacherAccountId));
					
					noticeDetails.add(noticeDetail);
				}
			}
		}
		for(String identyKey:identyTeachersMap.keySet()){
			if(identyKey.startsWith("1_")){
				//需更新调课记录表的
				JSONObject obj = new JSONObject();
				String[] msgs = identyKey.split("_");
				String AdjustType = msgs[1];
				String GroupId = msgs[2];
				String ClassId = msgs[3];
				int Step = Integer.parseInt(msgs[4]);
				String WeekOfEnd = msgs[5];
				int Week  = Integer.parseInt(msgs[6]);
				
				obj.put("SchoolId",school.getId()+"" );
				obj.put("TimetableId",timetableId);
				obj.put("AdjustType", AdjustType);
				obj.put("GroupId", GroupId);
				obj.put("ClassId", ClassId);
				obj.put("Step", Step);
				obj.put("WeekOfEnd", WeekOfEnd);
				obj.put("Week", Week);
				obj.put("PublishTeachers", identyTeachersMap.get(identyKey));
				needUpdateRecordList.add(obj);
			}else{
				//需更新调课结果表的
				JSONObject obj = new JSONObject();
				String[] msgs = identyKey.split("_");
				String AdjustType = msgs[1];
				String GroupId = msgs[2];
				String ClassId = msgs[3];
				String SubjectId = msgs[4];
				int DayOfWeek  = Integer.parseInt(msgs[5]);
				int LessonOfDay  = Integer.parseInt(msgs[6]);
				int Week  = Integer.parseInt(msgs[7]);
				obj.put("SchoolId",school.getId()+"" );
				obj.put("TimetableId",timetableId);
				obj.put("AdjustType", AdjustType);
				obj.put("GroupId", GroupId);
				obj.put("ClassId", ClassId);
				obj.put("SubjectId", SubjectId);
				obj.put("DayOfWeek", DayOfWeek);
				obj.put("LessonOfDay", LessonOfDay);
				obj.put("Week", Week);
				obj.put("PublishTeachers", identyTeachersMap.get(identyKey));
				needUpdateResultList.add(obj);
			}
		}
		try{
			if(needUpdateRecordList.size()>0){
				timetableDao.updateAdjustRecordPublished(needUpdateRecordList);
			}
			if(needUpdateResultList.size()>0){
				timetableDao.updateAdjustResultPublished(needUpdateResultList);
			}
			long d1 = new Date().getTime();
			System.out.println("开始推送:");
			String result = sendMsg((long) session.getAttribute("accountId"),school.getId(),noticeDetails);
			long d2 = new Date().getTime();
			System.out.println("推送耗时："+(d2-d1));
			
			System.out.println("临时调课消息推送结果:"+result);
//			int i= timetableDao.updateAdjustRecordPublished(list);
//			msg += "共计更新"+i + "条记录";
		}catch(Exception e){
			e.printStackTrace();
			code = -1;
			msg = "操作失败!";
		}
		returnData.put("code", code);
		returnData.put("msg", msg);
		return returnData;
	}

	private String sendMsg(Long accountId, long schoolId,
			List<JSONObject> noticeDetails) {
		// TODO Auto-generated method stub
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("noticeServiceType", MessageServiceEnum.timetable.toInteger());
		jsonObject.put("noticeDate", new Date());
		jsonObject.put("schoolId", schoolId);
		jsonObject.put("noticeType", MessageNoticeTypeEnum.TIMELY.toInteger());
		jsonObject.put("needConfirm", "0");
		jsonObject.put("noticeUserType", MessageNoticeUserTypeEnum.PARTSTEACHER.toInteger());
//		jsonObject.put("noticeModel", noticeModel.toInteger());
		jsonObject.put("noticeStatus", MessageStatusEnum.SUCCESS.toInteger());
		jsonObject.put("noticeSendDate", new Date());
		jsonObject.put("serviceId", UUIDUtil.getUUID());
		//noticeOperate
		jsonObject.put("sendMsgStatus", MessageSmsEnum.DEFAULT.toInteger());
		jsonObject.put("noticeDetails", noticeDetails);
		String result="";
		try {
			logger.info("\n ========== 开始调用RPC noticeMessage()方法============");
			result = MotanService.noticeMessage(jsonObject);
			logger.info("\n ================ 返回结果："+ result+" ==============");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public JSONObject getForFineTuningDataByTeacher(School school,
			String timetableId, String teacherId, String sessionID,int breakRule) {
		// TODO Auto-generated method stub
		String schoolId = school.getId() + "";
		JSONObject timetableInfo = this.timetableService
				.getArrangeTimetableInfo(schoolId, timetableId);
		String schoolYear = timetableInfo.getString("SchoolYear");
		String termName = timetableInfo.getString("TermName");
		String termInfoId = schoolYear.concat(termName);

		List<JSONObject> gradeSets = this.timetableService.getGradeSetList(schoolId,
				timetableId, null);
		if (gradeSets == null||gradeSets.size()==0) {
			throw new RuntimeException("当前选择的年级下无课表，请更换年级");
		}

		List<JSONObject> arrangeList = this.timetableService
				.getArrangeTimetableList(schoolId, timetableId,null);

		JSONObject hcData = loadDataByMapTask( sessionID, schoolId,
				timetableId, termInfoId, school);
		RuleConflict ruleConflict = (RuleConflict) hcData.get("ruleConflict"+schoolYear+termName);
		ruleConflict.setSchoolYear(schoolYear);
		ruleConflict.setTermName(termName);

		List<JSONObject> taskList = (List<JSONObject>) hcData.get("taskList");
		for(JSONObject task:taskList){
			String _classId = task.getString("ClassId");
			String courseId = task.getString("CourseId");
			if(ruleConflict.getMcGroupId(_classId, courseId)!=null){
				task.put("McGroupId", ruleConflict.getMcGroupId(_classId, courseId));
			}
		}
		HashMap<String, HashMap<String, HashMap<String, String>>> taskMap = (HashMap<String, HashMap<String, HashMap<String, String>>>) hcData
				.get("taskMap");
		this.fillTeachersToArrangeList(ruleConflict ,
				arrangeList, taskMap);
		
		int maxDaysForWeek = timetableInfo.getIntValue("MaxDaysForWeek");

		
		HashMap<String, List<JSONObject>> curWeekTbAllNj = new HashMap<String, List<JSONObject>>();
		HashMap<String, List<JSONObject>> arrangeMap = new HashMap<String, List<JSONObject>>();
		HashMap<String, List<JSONObject>> arrangeMapNj = new HashMap<String, List<JSONObject>>();
		HashMap<String, List<JSONObject>> classArrangeMap = new HashMap<String, List<JSONObject>>();
		
		// 已排课表-正式课表 天次，节次-课程课时
		List<JSONObject> teacherRight = new ArrayList<JSONObject>();
		List<String> relatedClassIds = new ArrayList<String>();
		//相关年级-存入的是使用年级
		List<String> relatedGradeIds = new ArrayList<String>();
		for (JSONObject arrange : arrangeList) {
			List<JSONObject> list = new ArrayList<JSONObject>();
			int dow = arrange.getIntValue("DayOfWeek");
			int lod = arrange.getIntValue("LessonOfDay");
			String classId = arrange.getString("ClassId");
			String key = dow + "," + lod +","+classId;
			if (arrangeMap.containsKey(key)) {
				list = arrangeMap.get(key);
			} else {
				arrangeMap.put(key, list);
			}
			//nj
			List<JSONObject> njlist = new ArrayList<JSONObject>();
			String njkey = dow + "," + lod  ;
			if (arrangeMapNj.containsKey(njkey)) {
				njlist = arrangeMapNj.get(njkey);
			} else {
				arrangeMapNj.put(njkey, njlist);
			}
			njlist.add(arrange);
			
			Map<String,String > teaMap = (Map<String, String>) arrange.get("teachers");
			if(teaMap!=null&&teaMap.keySet().contains(teacherId)){
				teacherRight.add(arrange);
				//记录教师所教班级
				if(!relatedClassIds.contains(classId)){
					relatedClassIds.add(classId);
				}
			}
			
			list.add(arrange);
			
			List<JSONObject> cList = new ArrayList<JSONObject>();
			if(classArrangeMap.containsKey(classId)){
				cList = classArrangeMap.get(classId);
			}
			cList.add(arrange);
			classArrangeMap.put(classId, cList);
		}
		for(JSONObject arrange :taskList){
			String classId = arrange.getString("ClassId");
			String TeacherId =   arrange.getString("TeacherId");
			if(TeacherId!=null&&TeacherId.trim().equals(teacherId)){
				//记录教师所教班级
				Classroom clar = ruleConflict.findClassInfo(classId);
				if(clar==null){
					continue;
				}
				String synj = ruleConflict.getGradeIdSynjMap().get(clar.getGradeId());
				if(synj==null){
					continue;
				}
				if(!relatedGradeIds.contains(synj)){
					relatedGradeIds.add(synj);
				}
			}
			
		}
		//根据关联的班级过滤关联的年级
		int amLessonNum = 0;
		int maxLessonForDay = 0;
		for(JSONObject gradeSet:gradeSets){
			String gradeId = gradeSet.getString("GradeId");
			if(!relatedGradeIds.contains(gradeId)){
				continue;
			}
			int temp = gradeSet.getIntValue("AMLessonNum");
			if(temp>amLessonNum){
				amLessonNum = temp;
			}
			int maxtemp = temp + gradeSet.getIntValue("PMLessonNum");
			if(maxtemp>maxLessonForDay){
				maxLessonForDay = maxtemp;
			}
		}
		if(amLessonNum==0){
			amLessonNum = 4;
		}
		if(maxLessonForDay==0){
			maxLessonForDay = 8;
		}
		//教师课表
		List<JSONObject> teaTask = GrepUtil.grepJsonKeyBySingleVal("TeacherId", teacherId, taskList);
		curWeekTbAllNj.put("1", teacherRight);
		
		List<JSONObject> teacherTimetable = new ArrayList<JSONObject>();
		if(breakRule==0){
			
			teacherTimetable=	TimetableUtil.getTeacherTimetableWithConflict
					(ruleConflict,curWeekTbAllNj,arrangeMapNj,
							teacherId, maxDaysForWeek, maxLessonForDay,false);
		}else{
			teacherTimetable=	TimetableUtil.getOnlyTeacherTimetableWithConflict
					(ruleConflict,curWeekTbAllNj,arrangeMapNj,
							teacherId, maxDaysForWeek, maxLessonForDay,false);
			
		}
		this.loadSwapConflicts(teacherTimetable,breakRule);
				
		JSONObject rs = new JSONObject();
		List<JSONObject> classTimetable = TimetableUtil.getClassTimetableNoConfByClassMap(relatedClassIds,classArrangeMap,ruleConflict);
		List<JSONObject> courseList = TimetableUtil.getTeacherCourseListByTask(teaTask,classArrangeMap,ruleConflict,maxDaysForWeek, maxLessonForDay,arrangeMapNj);
		long d11 = new Date().getTime();
		List<JSONObject> teacherList = TimetableUtil.getTeacherTaskFinishSituation(taskList,arrangeList,ruleConflict);
		long d12 = new Date().getTime();
		//System.out.println("[课表微调]计算教师课程完成情况耗时："+(d12-d11));
		rs.put("timetable", teacherTimetable);
		rs.put("classTimetable", classTimetable);
		rs.put("courseList", courseList);
		rs.put("totalMaxDays", maxDaysForWeek);
		rs.put("totalMaxLesson", maxLessonForDay);
		rs.put("amLessonNum", amLessonNum);
		rs.put("teacherList", teacherList);
		rs.put("teacherName", ruleConflict.getTeachersDic().get(teacherId).getName());
		return rs;
	}

	@SuppressWarnings({   "unchecked" })
	@Override
	public JSONObject getIntelligentCourseInfo(String schoolId,
			String timetableId, List<String> gradeIds, School school,  String sessionID,String termInfoId) {
		// TODO Auto-generated method stub
		JSONObject data = this.timetableService.getTimetableSections(
				schoolId, timetableId, gradeIds, school);
		
		JSONObject hcData = loadDataByMapTask(  sessionID, schoolId,
				timetableId, termInfoId, school);
		RuleConflict ruleConflict = (RuleConflict) hcData.get("ruleConflict"+termInfoId);

		List<JSONObject> taskList = (List<JSONObject>) hcData.get("taskList");
		List<JSONObject> arrangeList = (List<JSONObject>) hcData.get("arrangeList");
		
		HashMap<String,List<String>> gradeClasses = new HashMap<String, List<String>>();
		List<String> recordedClasses = new ArrayList<String>();
		List<String> recordedGrades = new ArrayList<String>();
		//记录任务中年级下的班级
		for(JSONObject task:taskList){
			//如果已记录该班级
			String ClassId = task.getString("ClassId");
			if(recordedClasses.contains(ClassId)){
				continue;
			}
			Classroom clr = ruleConflict.getClassroomsDic().get(ClassId);
			if(clr==null){
				continue;
			}
			Grade grade = ruleConflict.getGradeIdGradeDic().get(clr.getGradeId());
			if(grade==null){
				continue;
			}
			String synj = ruleConflict.getGradeIdSynjMap().get(grade.getId());
			if(!recordedGrades.contains(synj)){
				recordedGrades.add(synj);
			}
			String lvl = grade.getCurrentLevel().getValue()+"";
			if(gradeClasses.containsKey(lvl)){
				gradeClasses.get(lvl).add(ClassId);
			}else{
				List<String> clss = new ArrayList<String>();
				clss.add(ClassId);
				gradeClasses.put(lvl, clss);
			}
			recordedClasses.add(ClassId);
		}
		//记录已有课表的年级
		List<String> existGradeIds = new ArrayList<String>();
		List<String> existGradeLvs = new ArrayList<String>();
		for(JSONObject arrange:arrangeList){
			int IsAdvance = arrange.getIntValue("IsAdvance");
			if(IsAdvance==1){
				continue;
			}
			String classId = arrange.getString("ClassId");
			Classroom clr = ruleConflict.getClassroomsDic().get(classId);
			if(clr==null){
				continue;
			}
			Grade grade = ruleConflict.getGradeIdGradeDic().get(clr.getGradeId());
			if(grade==null){
				continue;
			}
			String synj = ruleConflict.getGradeIdSynjMap().get(grade.getId());
			if(!existGradeIds.contains(synj)){
				existGradeIds.add(synj);
			}
			
			//
			String lvl = grade.getCurrentLevel().getValue()+"";
			if(!existGradeLvs.contains(lvl)){
				existGradeLvs.add(lvl);
			}
			
		}
		List<JSONObject> rows = (List<JSONObject>) data.get("rows");
		List<JSONObject> needRemoveGrades = new ArrayList<JSONObject>();
		for(JSONObject row:rows){
			String gradeId = row.getString("gradeId");
			if(existGradeIds.contains(gradeId)){
				row.put("isArranged", true);
			}else{
				row.put("isArranged", false);
			}
			if(!recordedGrades.contains(gradeId)){
				needRemoveGrades.add(row);
			}
		}
		rows.removeAll(needRemoveGrades);
		//记录已有课表年级结束
		//记录已完成年级
		List<String> finishGradeLvls = new ArrayList<String>();
		List<String> nonClasses = new ArrayList<String>();
		Map<String, Integer> amMap = ruleConflict.getGradeLevelMaxAmNumMap();
		Map<String, Integer> pmMap = ruleConflict.getGradeLevelMaxPmNumMap();
		for(Iterator<String> it = gradeClasses.keySet().iterator();it.hasNext();){
			String lvl = it.next();
			if(!existGradeLvs.contains(lvl)){
				continue;
			}
			List<String> clss = gradeClasses.get(lvl);
			int maxLessonForDay = 0;
			if(amMap.containsKey(lvl)){
				maxLessonForDay += amMap.get(lvl);
			}else{
				continue;
			}
			if(pmMap.containsKey(lvl)){
				maxLessonForDay += pmMap.get(lvl);
			}else{
				continue;
			}
			List<JSONObject> finishlist = TimetableUtil.getClassFinishSituation(clss, taskList, arrangeList, maxLessonForDay);
			// 判断年级排课是否完成
			boolean gfinish = true;
			for(JSONObject fi:finishlist){
				boolean isFinished = fi.getBooleanValue("isFinished");
				if(!isFinished){
					nonClasses.add(fi.getString("classId"));
					gfinish = false;
				}
			}
			if(!finishGradeLvls.contains(lvl)&&gfinish){
				finishGradeLvls.add(lvl);
			}
		}
		String arrs = data.getString("arrangeResult");
		if(taskList.size()==0){
			arrs = "";
		}
		Collections.sort(finishGradeLvls);
		if(finishGradeLvls.size()>0){
			arrs = "排课已完成年级：";
			for(String lvl:finishGradeLvls){
				Grade gd = ruleConflict.getGradeByLevel(Integer.parseInt(lvl));
				if(gd!=null){
					String gradeName = this.arrangeDataService.getGradeName(gd);
					arrs += gradeName+"、";
				}
			}
			if(arrs.length()>8){
				arrs = arrs.substring(0,arrs.length()-1);
				arrs+="|";	
			}
			data.put("arrangeResult",arrs);
		}
		if(nonClasses.size()>0 ){
			if(finishGradeLvls.size()==0){
				arrs = "以下年级班级课程需到微调页面进行调整:";
			}else{
				arrs += "以下年级班级课程需到微调页面进行调整:";
			}
			
			HashMap<String,JSONObject> gradeClassMap = new HashMap<String,JSONObject>();
			for(String classId :nonClasses){
				Classroom clr = ruleConflict.getClassroomsDic().get(classId);
				if(clr==null){
					continue;
				}
				Grade grade = ruleConflict.getGradeIdGradeDic().get(clr.getGradeId());
				if(grade==null){
					continue;
				}
				String gradeName = this.arrangeDataService.getGradeName(grade);
				
				JSONObject obj = new JSONObject();
				List<JSONObject> cls = new ArrayList<JSONObject>();
				if(gradeClassMap.containsKey(grade.getCurrentLevel().getValue()+"")){
					obj= gradeClassMap.get(grade.getCurrentLevel().getValue()+"");
					cls = (List<JSONObject>) obj.get("cls");
					
					JSONObject clss = new JSONObject();
					clss.put("classId", classId);
					String className = clr.getClassName();
					clss.put("className", className );
					cls.add(clss);
				}else{
					obj.put("gradeName", gradeName);
					obj.put("gradeLevel", grade.getCurrentLevel().getValue());
					JSONObject clss = new JSONObject();
					clss.put("classId", classId);
					String className = clr.getClassName();
					clss.put("className", className );
					cls.add(clss);
					obj.put("cls", cls);
					gradeClassMap.put(grade.getCurrentLevel().getValue()+"", obj);
				}
			}
			
			List<JSONObject> list = new ArrayList<JSONObject>();
			for(JSONObject obj:gradeClassMap.values()){
				list.add(obj);
			}
			ScoreUtil.sorStuScoreList(list, "gradeLevel", "asc", "", "");
			int index = 0;
			for(JSONObject nj:list){
				index ++;
				arrs += "|"+nj.getString("gradeName");
				List<JSONObject> clas = (List<JSONObject>) nj.get("cls");
				ScoreUtil.sorStuScoreList(clas, "classId", "asc", "", "");
				
				try {
					clas = (List<JSONObject>) Sort.sort(SortEnum.ascEnding0rder, clas, "className");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				for(JSONObject cl:clas){
					arrs+= cl.getString("className")+",";
				}
				if(clas.size()>0){
					arrs = arrs.substring(0,arrs.length()-1);
					if(index==list.size()){
						arrs+=".";
					}else{
						arrs+=";";
					}
				}
			}
			
			data.put("arrangeResult",arrs);
		}
		
		return data;
	}

	@Override
	public List<JSONObject> getClssCurrentTimeTable(JSONObject timetable,
			String classId, String xnxq, School school) {
		// TODO Auto-generated method stub
		List<JSONObject> weekList = new ArrayList<JSONObject>();
		List<JSONObject>  tmpweekList = new ArrayList<JSONObject>();
		
		try{
			
			JSONObject weewObj =	TimetableUtil.getWeekListByStartDate(timetable.getString("DayOfStart"),21);
			if(weewObj.containsKey("code")&&weewObj.getIntValue("code")==1){
				weekList = (List<JSONObject>) weewObj.get("rslist");
			}
			tmpweekList = GrepUtil.grepJsonKeyBySingleVal("selected", "true", weekList);
		}catch(Exception e){
			e.printStackTrace();
		}
		
		int week = 0;
		if(tmpweekList.size()>0){
			week = Integer.parseInt(tmpweekList.get(0).getString("value").split("\\|")[0]);
		}
		HashMap<String,Object> cxMap = new HashMap<String, Object>();
		cxMap.put("schoolId", school.getId()+"");
		cxMap.put("xnxq", xnxq);
		cxMap.put("classId", classId);
		cxMap.put("timetableId", timetable.get("TimetableId"));
		
		List<JSONObject> tmpAjst = new ArrayList<JSONObject>();
		if(week!=0){
			cxMap.put("week", week);
			tmpAjst =  timetableService
					.getTemporaryAjustList(cxMap);
		}


		 List<JSONObject> arrangeList = timetableDao.getArrangeTimetableList(cxMap);
		 List<JSONObject> taskList =  timetableDao.getTaskByTimetable(cxMap );
		 
		 Map<String, Account> teacherDic = new HashMap<String, Account>();
		 RuleConflict ruleConfilict = new RuleConflict();
		 List<Long> teaIds = new ArrayList<Long>();
		 HashMap<String, HashMap<String, HashMap<String, String>>> taskMap =new HashMap<String, HashMap<String,HashMap<String,String>>>();
		for (JSONObject task : taskList) {
				String ClassId = task.getString("ClassId");
				String courseId = task.getString("CourseId");
				String TeacherId = task.getString("TeacherId");
				if(TeacherId==null||TeacherId.trim().length()==0||TeacherId.equalsIgnoreCase("null")){
					continue;
				}
				Long teaId = Long.parseLong(TeacherId);
				if(teaId!=0&&!teaIds.contains(teaId)){
					teaIds.add(teaId);
				}
				HashMap<String, HashMap<String, String>> courseMap = new HashMap<String, HashMap<String, String>>();
				if (taskMap .containsKey(ClassId)) {
					courseMap = taskMap.get(ClassId);
				} else {
					taskMap.put(ClassId, courseMap);
				}
				HashMap<String, String> teaMap = new HashMap<String, String>();
				if (courseMap.containsKey(courseId)) {
					teaMap = courseMap.get(courseId);
				} else {
					courseMap.put(courseId, teaMap);
				}
				if (!teaMap.containsKey(TeacherId)&&TeacherId!=null) {
					String actName = "";
					if (teacherDic.containsKey(TeacherId)) {
						actName = teacherDic.get(TeacherId).getName();
					}
					teaMap.put(TeacherId, actName);
				}
			}
		 for(JSONObject tjs:tmpAjst){
			 String teachers = tjs.getString("Teachers");
			 List<Long> tids = StringUtil.toListFromString(teachers);
			 if(tids!=null){
				 for(Long teaId :tids){
					 if(teaId!=0&&!teaIds.contains(teaId)){
						 teaIds.add(teaId);
					 }
				 }
			 }
		 }
		 List<Account> ascts = commonService.getAccountBatch(school.getId(), teaIds,xnxq);
		 for(Account a:ascts){
			 if(a!=null&&a.getId()!=0){
				 teacherDic.put(""+a.getId(), a);
			 }
		 }
		ruleConfilict.setTeachersDic(teacherDic);
		Map<String, LessonInfo> courseDic = this.arrangeDataService.getCoursesDic(school,xnxq);
		ruleConfilict.setCoursesDic(courseDic);
		 fillTeachersToArrangeList(ruleConfilict ,
					arrangeList, taskMap);
		 
		HashMap<String, List<JSONObject>> arrangeMap = new HashMap<String, List<JSONObject>>();
		List<JSONObject> gradeSets = this.timetableService.getGradeSetList(school.getId()+"",
				cxMap.get("timetableId").toString(), null);
		
		for (JSONObject arrange : arrangeList) {
			List<JSONObject> list = new ArrayList<JSONObject>();
			int dow = arrange.getIntValue("DayOfWeek");
			int lod = arrange.getIntValue("LessonOfDay");
			String key = dow + "," + lod  ;
			if (arrangeMap.containsKey(key)) {
				list = arrangeMap.get(key);
			} else {
				arrangeMap.put(key, list);
			}
			list.add(arrange);
			
		}
		HashMap<String, List<JSONObject>> curweekTb = TimetableUtil.getSingleWeekTimeTableByParam(arrangeMap, tmpAjst, week, classId, ruleConfilict );
		List<JSONObject> curweekTbList = TimetableUtil.changeAppTbByMap(curweekTb, null, ruleConfilict, timetable);
		return curweekTbList;
	}

	@Override
	public List<JSONObject> getTeaCurrentTimeTable(JSONObject timetable,
			String teacherId, String xnxq, School school) {
		// TODO Auto-generated method stub
		List<JSONObject> weekList = new ArrayList<JSONObject>();
		List<JSONObject>  tmpweekList = new ArrayList<JSONObject>();
		try{
			JSONObject weewObj =	TimetableUtil.getWeekListByStartDate(timetable.getString("DayOfStart"),21);
			if(weewObj.containsKey("code")&&weewObj.getIntValue("code")==1){
				weekList = (List<JSONObject>) weewObj.get("rslist");
			}
//			weekList=	TimetableUtil.getWeekListByStartDate(timetable.getString("DayOfStart"),21);
			tmpweekList = GrepUtil.grepJsonKeyBySingleVal("selected", "true", weekList);
		}catch(Exception e){
			e.printStackTrace();
		}
		int week = 0;
		if(tmpweekList.size()>0){
			week = Integer.parseInt(tmpweekList.get(0).getString("value").split("\\|")[0]);
		}
		HashMap<String,Object> cxMap = new HashMap<String, Object>();
		cxMap.put("schoolId", school.getId()+"");
		cxMap.put("xnxq", xnxq);
		cxMap.put("teacherId", teacherId);
		List<Long> ct1 = StringUtil.toListFromString(timetable.getString("courseIds"));
		cxMap.put("courseIds", ct1 );
		
		cxMap.put("timetableId", timetable.get("TimetableId"));
		
		List<JSONObject> tmpAjst = new ArrayList<JSONObject>();
		List<Long> tpCourseIds = new ArrayList<Long>();
		List<Long> tpClassIds = new ArrayList<Long>();
		if(week!=0){
			cxMap.put("week", week);
			tmpAjst =  timetableService
					.getTemporaryAjustList(cxMap);
			
			for(JSONObject ajs:tmpAjst){
				String Ts = ajs.getString("Teachers");
				Long classId = Long.parseLong(ajs.getString("ClassId"));
				Long courseId = Long.parseLong(ajs.getString("SubjectId"));
				if(TimetableUtil.isStrInArray(teacherId, Ts.split(",")) ){
					if(!tpClassIds.contains(classId)){
						tpClassIds.add(classId);
					}
					if(!tpCourseIds.contains(courseId)){
						tpCourseIds.add(courseId);
					}
				}
			}
			
		}
		if(tpCourseIds.size()>0){
			for(Long cid:tpCourseIds){
				if(!ct1.contains(cid)){
					ct1.add(cid);
				}
			}
			cxMap.put("courseIds", ct1);
		}
		List<Long> ct2 = StringUtil.toListFromString(timetable.getString("classes") );
		if(tpClassIds.size()>0){
			for(Long cid:tpClassIds){
				if(!ct2.contains(cid)){
					ct2.add(cid);
				}
			}
		}
		cxMap.put("classes", ct2);
		
		
		 List<JSONObject> arrangeList = timetableDao.getArrangeTimetableList(cxMap);
		 List<JSONObject> taskList =  timetableDao.getTaskByTimetable(cxMap );
		 Map<String, Classroom> classdic = this.arrangeDataService.getClassRoomsDic(school, null,xnxq);
		 Map<String, Account> teacherDic = new HashMap<String, Account>();
		 Account act = commonService.getAccountAllById(school.getId(), Long.parseLong(teacherId),xnxq);
		 teacherDic.put(act.getId()+"", act);
		 RuleConflict ruleConfilict = new RuleConflict();
		 ruleConfilict.setTeachersDic(teacherDic);
		 ruleConfilict.setClassroomsDic(classdic);
		 Map<String, LessonInfo> courseDic = this.arrangeDataService.getCoursesDic(school,xnxq);
			ruleConfilict.setCoursesDic(courseDic);
		 HashMap<String, HashMap<String, HashMap<String, String>>> taskMap =new HashMap<String, HashMap<String,HashMap<String,String>>>();
		for (JSONObject task : taskList) {
				String classId = task.getString("ClassId");
				String courseId = task.getString("CourseId");
				String TeacherId = task.getString("TeacherId");
				HashMap<String, HashMap<String, String>> courseMap = new HashMap<String, HashMap<String, String>>();
				if (taskMap .containsKey(classId)) {
					courseMap = taskMap.get(classId);
				} else {
					taskMap.put(classId, courseMap);
				}
				HashMap<String, String> teaMap = new HashMap<String, String>();
				if (courseMap.containsKey(courseId)) {
					teaMap = courseMap.get(courseId);
				} else {
					courseMap.put(courseId, teaMap);
				}
				if (!teaMap.containsKey(TeacherId)&&TeacherId!=null) {
					String actName = "";
					if (teacherDic.containsKey(TeacherId)) {
						actName = teacherDic.get(TeacherId).getName();
					}
					teaMap.put(TeacherId, actName);
				}
			}
		 fillTeachersToArrangeList(ruleConfilict ,
					arrangeList, taskMap);
		
		HashMap<String, List<JSONObject>> arrangeMap = new HashMap<String, List<JSONObject>>();
		for (JSONObject arrange : arrangeList) {
			List<JSONObject> list = new ArrayList<JSONObject>();
			int dow = arrange.getIntValue("DayOfWeek");
			int lod = arrange.getIntValue("LessonOfDay");
			String key = dow + "," + lod  ;
			if (arrangeMap.containsKey(key)) {
				list = arrangeMap.get(key);
			} else {
				arrangeMap.put(key, list);
			}
			list.add(arrange);
			
		}
		HashMap<String, List<JSONObject>> curweekTb = TimetableUtil.getSingleWeekTimeTableByParam(arrangeMap, tmpAjst, week, null, ruleConfilict );
		List<JSONObject> curweekTbList = TimetableUtil.changeAppTbByMap(curweekTb,teacherId, ruleConfilict,timetable);
		return curweekTbList;
	}




}
