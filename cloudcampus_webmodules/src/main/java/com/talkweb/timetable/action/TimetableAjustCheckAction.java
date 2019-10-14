package com.talkweb.timetable.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.Account;
import com.talkweb.accountcenter.thrift.Classroom;
import com.talkweb.accountcenter.thrift.Grade;
import com.talkweb.accountcenter.thrift.LessonInfo;
import com.talkweb.accountcenter.thrift.School;
import com.talkweb.base.common.CacheExpireTime;
import com.talkweb.common.action.BaseAction;
import com.talkweb.common.tools.GrepUtil;
import com.talkweb.commondata.dao.RedisOperationDAO;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.timetable.arrangement.algorithm.RuleConflict;
import com.talkweb.timetable.arrangement.domain.RuleCourse;
import com.talkweb.timetable.arrangement.domain.RuleGround;
import com.talkweb.timetable.arrangement.domain.RulePosition;
import com.talkweb.timetable.arrangement.domain.RuleTeacher;
import com.talkweb.timetable.arrangement.service.ArrangeDataService;
import com.talkweb.timetable.arrangement.service.SmartArrangeService;
import com.talkweb.timetable.service.TimetableService;

/**
 * 
 * @author talkweb 课表微调校验控制器
 */
@Controller
@RequestMapping(value = "/timetableManage/")
public class TimetableAjustCheckAction extends BaseAction {
	@Autowired
	private TimetableService timetableService;

	@Autowired
	private AllCommonDataService commonDataService;

	@Autowired
	private SmartArrangeService smtService;

	@Autowired
	private ArrangeDataService arrangeDataService;

	@Resource(name = "redisOperationDAOSDRTempDataImpl")
	private RedisOperationDAO redisOperationDAO;

	// private static HashMap<String,JSONObject> progData = new HashMap<String,
	// JSONObject>();

	private String[] daydic = new String[] { "星期一", "星期二", "星期三", "星期四", "星期五",
			"星期六", "星期日" };
	private String[] lessondic = new String[] { "第一节", "第二节", "第三节", "第四节",
			"第五节", "第六节", "第七节", "第八节", "第九节", "第十节", "第十一节", "第十二节", "第十三节",
			"第十四节", "第十五节" };

	/**
	 * 是否需要校验
	 * 
	 * @author
	 * @param req
	 * @param res
	 * @param reqData
	 * @return
	 */
	@RequestMapping(value = "adjustment/getIsNeedAjustCheck", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getIsNeedAjustCheck(HttpServletRequest req,
			HttpServletResponse res, @RequestBody JSONObject reqData) {
		int code = 0;
		String msg = "查询成功！";

		String timetableId = "";
		if (reqData.containsKey("timetableId")) {
			timetableId = reqData.getString("timetableId");
			JSONObject obj = timetableService.getArrangeTimetableInfo(
					getXxdm(req), timetableId);
			if (obj != null && obj.containsKey("IsNeedCheck")) {
				code = obj.getIntValue("IsNeedCheck");
			}
		} else {
			code = -1;
			msg = "请求参数异常！";
		}

		JSONObject rs = new JSONObject();
		rs.put("code", code);
		rs.put("msg", msg);
		return rs;
	}

	/**
	 * 是否需要校验
	 * 
	 * @author XFQ
	 * @param req
	 * @param res
	 * @param reqData
	 * @return
	 */
	@RequestMapping(value = "adjustment/startAjustCheck", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject startAjustCheck(HttpServletRequest req,
			HttpServletResponse res, @RequestBody JSONObject reqData) {
		int code = 0;
		String msg = "查询成功！";
		String termInfoId = reqData.getString("selectedSemester");
		String timetableId = "";
		if (reqData.containsKey("timetableId")) {
			timetableId = reqData.getString("timetableId");
			msg = "成功启动任务！";
			setProgress(req.getSession().getId(), timetableId, 1, 0, "成功启动任务！",
					null);
			
			SubProcess subp = new SubProcess(req.getSession().getId(),
					timetableId, getXxdm(req), termInfoId, getSchool(req,
							termInfoId));
			code = 1;
			subp.start();
		}

		JSONObject rs = new JSONObject();
		rs.put("code", code);
		rs.put("msg", msg);
		return rs;
	}

	/**
	 * 校验进度
	 * 
	 * @author XFQ
	 * @param req
	 * @param res
	 * @param reqData
	 * @return
	 */
	@RequestMapping(value = "adjustment/getAjustCheckProgress", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getAjustCheckProgress(HttpServletRequest req,
			HttpServletResponse res, @RequestBody JSONObject reqData) {
		int code = 0;
		JSONObject data = new JSONObject();

		String sessId = req.getSession().getId();
		String timetableId = "";
		if (reqData.containsKey("timetableId")) {
			timetableId = reqData.getString("timetableId");
			HashMap<String, JSONObject> progData = new HashMap<String, JSONObject>();
			String progDatakey = "timetable." + req.getSession().getId() + "."+timetableId
					+ "ajustCheck";
			try {
				progData = (HashMap<String, JSONObject>) redisOperationDAO
						.get(progDatakey);
			} catch (Exception e) {
				// TODO Auto-generated catch block

			}
			if (progData != null && progData.containsKey(timetableId)
					&& progData.get(timetableId).containsKey(sessId)) {
				JSONObject obj = progData.get(timetableId)
						.getJSONObject(sessId);
				code = obj.getIntValue("code");
				data = obj.getJSONObject("data");
			} else {
				data.put("progress", 100);
				code = -50;
				data.put("msg", "由于长时间未操作，请重新校验");
			}
		}
		System.out.println("查询进度" + data.getIntValue("progress"));
		JSONObject rs = new JSONObject();
		rs.put("code", code);
		rs.put("data", data);
		return rs;
	}

	/**
	 * 设置进度值
	 * 
	 * @param sessId
	 * @param timetableId
	 * @param code
	 * @param progress
	 * @param msg
	 * @param rows
	 */
	public void setProgress(String sessId, String timetableId, int code,
			int progress, String msg, String[] rows) {
		HashMap<String, JSONObject> progData = new HashMap<String, JSONObject>();
		JSONObject ttJSON = new JSONObject();
		progData.put(timetableId, ttJSON);

		JSONObject ssJSON = new JSONObject();
		ttJSON.put(sessId, ssJSON);

		JSONObject data = new JSONObject();
		ssJSON.put("data", data);
		ssJSON.put("code", code);
		data.put("progress", progress);
		data.put("msg", msg);
		data.put("rows", rows);

		String progDatakey = "timetable." + sessId + "." +timetableId+ "ajustCheck";
		try {
			redisOperationDAO.set(progDatakey, progData,
					CacheExpireTime.temporaryDataMaxExpireTime.getTimeValue());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * 子线程
	 * 
	 * @author talkweb
	 *
	 */
	class SubProcess extends Thread {
		private String processId;
		private String timetableId;
		private String xxdm;
		private String xnxq;
		private School school;

		public SubProcess(String processId, String timetableId, String xxdm,
				String xnxq, School school) {
			this.processId = processId;
			this.xxdm = xxdm;
			this.timetableId = timetableId;
			this.xnxq = xnxq;
			this.school = school;
		}

		@SuppressWarnings("unchecked")
		@Override
		public void run() {
			try {
				setProgress(processId, timetableId, 1, 1, "正在读取规则设置...", null);
				RuleConflict ruleConflict = arrangeDataService.getRuleConflict(
						xxdm, timetableId);
				if (ruleConflict == null) {
					throw new RuntimeException("读取规则设置失败，请检查规则设置或联系管理员！");
				}
				Map<String, Grade> gradeDic = arrangeDataService.getGradesDic(
						xnxq.substring(0, 4), school, xnxq.substring(4, 5));
				ruleConflict.setGradesDic(gradeDic);
				List<Grade> cxGrade = new ArrayList<Grade>();
				for (Grade gd : gradeDic.values()) {
					cxGrade.add(gd);
				}
				setProgress(processId, timetableId, 1, 5, "正在读取基础数据...", null);
				Map<String, Classroom> classDic = arrangeDataService
						.getClassRoomsDic(school, cxGrade, xnxq);
				ruleConflict.setClassroomsDic(classDic);
				Map<String, LessonInfo> courseDic = arrangeDataService
						.getCoursesDic(school, xnxq);
				ruleConflict.setCoursesDic(courseDic);
				Map<String, Account> teacherDic = arrangeDataService
						.getTeachersDic(school, xnxq);
				ruleConflict.setTeachersDic(teacherDic);

				HashMap<String, Object> cxmap = new HashMap<String, Object>();
				cxmap.put("schoolId", xxdm);
				cxmap.put("timetableId", timetableId);
				List<JSONObject> gradeList = timetableService
						.getGradeSet(cxmap);
				for (JSONObject grade : gradeList) {
					String synj = grade.getString("GradeId");
					String gradeLevel = commonDataService.ConvertSYNJ2NJDM(
							synj, xnxq.substring(0, 4));
					if (gradeLevel != null && gradeLevel.trim().length() > 0) {
						// 添加年级上午最大排课数
						ruleConflict.addToGradeLevelMaxAmNumMap(gradeLevel,
								grade.getIntValue("AMLessonNum"));
						ruleConflict.addToGradeLevelMaxPmNumMap(gradeLevel,
								grade.getIntValue("PMLessonNum"));
					}
				}

				// 查询任务及结果 组建任务map
				setProgress(processId, timetableId, 1, 12, "正在读取排课结果...", null);
				List<JSONObject> arrangeList = timetableService
						.getArrangeTimetableList(xxdm, timetableId, null);

				List<JSONObject> temp = GrepUtil.grepJsonKeyBySingleVal(
						"IsDelete", "1", arrangeList);

				List<JSONObject> taskList = timetableService
						.getTaskByTimetable(xxdm, timetableId);
				HashMap<String, HashMap<String, HashMap<String, String>>> taskMap = getTaskMapByTaskList(
						ruleConflict, taskList);
				smtService.fillTeachersToArrangeList(ruleConflict, arrangeList,
						taskMap);

				HashMap<String, List<JSONObject>> arrangeMap = new HashMap<String, List<JSONObject>>();
				HashMap<String, List<JSONObject>> arrangeMapKm = new HashMap<String, List<JSONObject>>();
				HashMap<String, List<JSONObject>> arrangeMapGrd = new HashMap<String, List<JSONObject>>();
				HashMap<String, List<JSONObject>> teacherArrangeMap = new HashMap<String, List<JSONObject>>();
				List<JSONObject> McArrangeList = new ArrayList<JSONObject>();
				List<JSONObject> SdArrangeList = new ArrayList<JSONObject>();
				changeArrangeListToMaps(arrangeList, arrangeMap, arrangeMapKm,
						arrangeMapGrd, teacherArrangeMap, McArrangeList,
						SdArrangeList, ruleConflict);
				temp = GrepUtil.grepJsonKeyBySingleVal("IsDelete", "1",
						arrangeList);
				// 开始校验冲突
				setProgress(processId, timetableId, 1, 15, "开始校验冲突...", null);
				// 校验结果
				StringBuffer sber = new StringBuffer();
				// 所有冲突项目标记为 isDel=true 校验结束后删除
				// 校验合班课
				setProgress(processId, timetableId, 1, 16, "开始校验合班课规则设置...",
						null);
				tacCheckMcGroup(sber, McArrangeList, ruleConflict);

				temp = GrepUtil.grepJsonKeyBySingleVal("IsDelete", "1",
						arrangeList);
				// 校验单双周
				setProgress(processId, timetableId, 1, 30, "开始校验合班课规则设置...",
						null);
				tacCheckSdWeek(sber, SdArrangeList, ruleConflict);
				temp = GrepUtil.grepJsonKeyBySingleVal("IsDelete", "1",
						arrangeList);
				// 校验教学任务及教师上课是否冲突
				setProgress(processId, timetableId, 1, 50, "开始校验教师冲突及规则设置...",
						null);
				for (Iterator<String> teaKey = teacherArrangeMap.keySet()
						.iterator(); teaKey.hasNext();) {
					String teaKeystr = teaKey.next();
					if (teaKeystr != null
							&& teaKeystr.length() > 0
							&& !teaKeystr.toLowerCase()
									.equalsIgnoreCase("null")) {
						String teacherId = teaKeystr;
						tacCheckTeacher(sber, teacherId, teacherArrangeMap,
								ruleConflict);
					} else {
						System.out.println("【微调校验】--教师规则过滤id为空的教师课表");
					}
				}
				temp = GrepUtil.grepJsonKeyBySingleVal("IsDelete", "1",
						arrangeList);
				setProgress(processId, timetableId, 1, 70, "开始校验科目规则设置...",
						null);
				tacCheckSubject(sber, arrangeMapKm, ruleConflict);
				temp = GrepUtil.grepJsonKeyBySingleVal("IsDelete", "1",
						arrangeList);
				setProgress(processId, timetableId, 1, 85, "开始校验场地规则设置...",
						null);
				tacCheckGroundRule(sber, arrangeMapGrd, ruleConflict);
				temp = GrepUtil.grepJsonKeyBySingleVal("IsDelete", "1",
						arrangeList);

				String rs = sber.toString();
				String[] rows = null;
				if (rs.length() > 0) {
					rows = rs.split(";");
				}

				setProgress(processId, timetableId, 1, 90, "开始删除不合规课程...", null);
				List<JSONObject> needDelList =
//						GrepUtil.grepJsonKeyBySingleVal(
//						"IsDelete", "1", arrangeList);
						GrepUtil.grepJsonKeyByVal(new String[]{"IsDelete","IsAdvance"}, new String[]{"1","0"}, arrangeList);
				if (needDelList.size() > 0) {
					// 暂时不删除
					timetableService.deleteArrangeTimetableHigh(needDelList);
				}
				HashMap<String, Object> cxMap = new HashMap<String, Object>();
				cxMap.put("schoolId", xxdm);
				cxMap.put("timetableId", timetableId);
				cxMap.put("isNeedCheck", 0);
				timetableService.updateTimetableCheckStatus(cxMap);
				setProgress(processId, timetableId, 2, 100, "校验结束，已删除不合规则课程",
						rows);
			} catch (Exception e) {
				e.printStackTrace();
				setProgress(processId, timetableId, -1, 100,
						"校验失败，请检查课表数据或联系管理员", null);
			}

		}

	}

	/**
	 * 转换任务
	 * 
	 * @param ruleConflict
	 * @param taskList
	 * @return
	 */
	public HashMap<String, HashMap<String, HashMap<String, String>>> getTaskMapByTaskList(
			RuleConflict ruleConflict, List<JSONObject> taskList) {
		Map<String, Account> teaDic = ruleConflict.getTeachersDic();
		HashMap<String, HashMap<String, HashMap<String, String>>> taskMap = new HashMap<String, HashMap<String, HashMap<String, String>>>();
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
			if (!teaMap.containsKey(teacherId) && teacherId != null) {
				String actName = "";
				if (teaDic.containsKey(teacherId)) {
					actName = teaDic.get(teacherId).getName();
				}
				teaMap.put(teacherId, actName);
			}
		}

		return taskMap;
	}

	/**
	 * 将已排课表转换为各种需要的map数据
	 * 
	 * @param arrangeList
	 * @param arrangeMap
	 * @param arrangeMapNj
	 * @param classArrangeMap
	 * @param teacherArrangeMap
	 * @param mcArrangeList
	 *            合班序列
	 * @param sdArrangeList
	 *            单双周序列
	 */
	public void changeArrangeListToMaps(List<JSONObject> arrangeList,
			HashMap<String, List<JSONObject>> arrangeMap,
			HashMap<String, List<JSONObject>> arrangeMapKm,
			HashMap<String, List<JSONObject>> arrangeMapGrd,
			HashMap<String, List<JSONObject>> teacherArrangeMap,
			List<JSONObject> mcArrangeList, List<JSONObject> sdArrangeList,
			RuleConflict ruleConflict) {
		// 已排课表-正式课表 天次，节次-课程课时
		for (JSONObject arrange : arrangeList) {
			arrange.put("IsDelete", 0);
			List<JSONObject> list = new ArrayList<JSONObject>();
			int dow = arrange.getIntValue("DayOfWeek");
			int lod = arrange.getIntValue("LessonOfDay");
			String mcGroupId = arrange.getString("McGroupId");
			int courseType = arrange.getIntValue("CourseType");
			if (mcGroupId != null && mcGroupId.trim().length() > 0) {
				mcArrangeList.add(arrange);
			}
			if (courseType != 0) {
				sdArrangeList.add(arrange);
			}
			String classId = arrange.getString("ClassId");
			String key = dow + "," + lod + "," + classId;
			if (arrangeMap.containsKey(key)) {
				list = arrangeMap.get(key);
			} else {
				arrangeMap.put(key, list);
			}
			// 科目映射
			List<JSONObject> kmlist = new ArrayList<JSONObject>();
			Classroom classroom = ruleConflict.getClassroomsDic().get(classId);
			if(classroom==null){
				continue;
			}
			long gid = ruleConflict.getClassroomsDic().get(classId)
					.getGradeId();
			Grade gd = ruleConflict.getGradeIdGradeDic().get(gid);
			String synj = ruleConflict.getGradeIdSynjMap().get(gid);
			if (gd == null || gd.getCurrentLevel() == null) {
				continue;
			}
			String gradeLev = String.valueOf(gd.getCurrentLevel().getValue());

			String njkey = gradeLev + "_" + arrange.getString("CourseId");
			if (arrangeMapKm.containsKey(njkey)) {
				kmlist = arrangeMapKm.get(njkey);
			} else {
				arrangeMapKm.put(njkey, kmlist);
			}
			kmlist.add(arrange);

			// 场地映射
			List<JSONObject> grdlist = new ArrayList<JSONObject>();

			String grdKey = synj + "_" + arrange.getString("CourseId");
			if (arrangeMapGrd.containsKey(grdKey)) {
				grdlist = arrangeMapGrd.get(grdKey);
			} else {
				arrangeMapGrd.put(grdKey, grdlist);
			}
			grdlist.add(arrange);

			Map<String, String> teaMap = (Map<String, String>) arrange
					.get("teachers");
			if (teaMap != null) {
				for (String teacherId : teaMap.keySet()) {
					List<JSONObject> teacherRight = new ArrayList<JSONObject>();
					if (teacherArrangeMap.containsKey(teacherId)) {
						teacherRight = teacherArrangeMap.get(teacherId);
					} else {
						teacherArrangeMap.put(teacherId, teacherRight);
					}
					teacherRight.add(arrange);
				}
			}

			list.add(arrange);

		}
	}

	/**
	 * 循环校验教师直至符合规则
	 * 
	 * @param sber
	 * @param teacherId
	 * @param teacherArrangeMap
	 * @param ruleConflict
	 */
	private void tacCheckTeacher(StringBuffer sber, String teacherId,
			HashMap<String, List<JSONObject>> teacherArrangeMap,
			RuleConflict ruleConflict) {
		// TODO Auto-generated method stub
		int i = 0;
		while (!tacCheckTeacherChild(sber, teacherId, teacherArrangeMap,
				ruleConflict)) {
			i++;
			System.out.println("checkTeacherTimes:" + i);
		}

		return;
	}

	/**
	 * 校验教师并删除
	 * 
	 * @param sber
	 * @param teacherId
	 * @param teacherArrangeMap
	 * @param ruleConflict
	 * @return
	 */
	private boolean tacCheckTeacherChild(StringBuffer sber, String teacherId,
			HashMap<String, List<JSONObject>> teacherArrangeMap,
			RuleConflict ruleConflict) {
		List<JSONObject> teaList = teacherArrangeMap.get(teacherId);
		teaList = GrepUtil.grepJsonKeyBySingleVal("IsDelete", "0", teaList);
		if (teaList != null && teaList.size() > 0) {
			HashMap<Integer, HashMap<Integer, List<JSONObject>>> dayTeaMap = new HashMap<Integer, HashMap<Integer, List<JSONObject>>>();
			for (JSONObject teaJSON : teaList) {
				int day = teaJSON.getIntValue("DayOfWeek");
				int lesson = teaJSON.getIntValue("LessonOfDay");
				String mcGroupId = teaJSON.getString("McGroupId");
				String classId = teaJSON.getString("ClassId");
				String courseId = teaJSON.getString("CourseId");
				int IsAdvance = teaJSON.getIntValue("IsAdvance");
				int CourseType = teaJSON.getIntValue("CourseType");
				HashMap<Integer, List<JSONObject>> lessonMap = new HashMap<Integer, List<JSONObject>>();
				if (dayTeaMap.containsKey(day)) {
					lessonMap = dayTeaMap.get(day);
				} else {
					dayTeaMap.put(day, lessonMap);
				}
				List<JSONObject> lessonList = new ArrayList<JSONObject>();
				if (lessonMap.containsKey(lesson)) {
					lessonList = lessonMap.get(lesson);
				} else {
					lessonMap.put(lesson, lessonList);
				}

				lessonList.add(teaJSON);
			}
			// 校验教师规则
			RuleTeacher ruletea = ruleConflict.getRuleTeachers().get(teacherId);
			if (ruletea != null) {
				List<RulePosition> poss = ruletea.getPositions();
				// 校验教师不排位置冲突
				for (RulePosition pos : poss) {
					if (pos.getRuleType() != 0) {
						continue;
					}
					int pday = pos.getDay();
					int plesson = pos.getLesson();

					List<JSONObject> temp = new ArrayList<JSONObject>();
					if (dayTeaMap.containsKey(pday)
							&& dayTeaMap.get(pday).containsKey(plesson)) {
						temp = dayTeaMap.get(pday).get(plesson);
					}
					if (temp.size() > 0) {
						for (JSONObject tp : temp) {
							tp.put("IsDelete", 1);
							getTeacherTimeConfMsg(teacherId, sber, tp,
									ruleConflict, 2, pos);
						}
						return false;
					}
				}
				// 校验教师每天最多上课数冲突
				int maxPerDay = ruletea.getMaxPerDay();
				if (maxPerDay > 0) {
					for (Iterator<Integer> daykey = dayTeaMap.keySet()
							.iterator(); daykey.hasNext();) {
						int tday = daykey.next();
						HashMap<Integer, List<JSONObject>> dayMap = dayTeaMap
								.get(tday);
						int maxLessons = dayMap.keySet().size();
						List<JSONObject> dayList = new ArrayList<JSONObject>();
						for( List<JSONObject> loop:dayMap.values()){
							dayList.addAll(loop);
						}
						boolean finished = false;
						if (isOverByList(dayList, maxPerDay)) {
							Iterator<Integer> lessonKey = dayMap.keySet()
									.iterator();
							int fnum = dayMap.keySet().size();
							
							int tlesson = lessonKey.next();
							List<JSONObject> temp = dayMap.get(tlesson);
							
							while (isOverByList(dayList, maxPerDay) && !finished
									&& lessonKey.hasNext()) {
								List<JSONObject> temp2 = GrepUtil
										.grepJsonKeyBySingleVal("IsAdvance",
												"0", temp);
								for (JSONObject tp : temp2) {
									if (tp.getInteger("IsAdvance") != 1) {
										tp.put("IsDelete", 1);
										getTeacherTimeConfMsg(teacherId, sber,
												tp, ruleConflict, 3, maxPerDay);
									}
								}
								// 该位置全部为非预排课 删除所有 并且最大数递减
								if (temp.size() == temp2.size()) {
									maxLessons--;
								}
								fnum--;
								// 全部键循环完成
								if (fnum == 0) {
									finished = true;
								}
							}
						}
						// 教师最大班级数验证不通过 已处理完所有 不再处理
						// return false;
					}

				}

			}
			// 开始校验每节课是否有多班级上课
			for (Iterator<Integer> daykey = dayTeaMap.keySet().iterator(); daykey
					.hasNext();) {
				int tday = daykey.next();
				HashMap<Integer, List<JSONObject>> dayMap = dayTeaMap.get(tday);
				for (Iterator<Integer> lessonKey = dayMap.keySet().iterator(); lessonKey
						.hasNext();) {
					int tlesson = lessonKey.next();
					List<JSONObject> temp = dayMap.get(tlesson);
					boolean conf = false;
					int ctemp0 = GrepUtil.grepJsonKeyBySingleVal("CourseType",
							"0", temp).size();
					int ctemp1 = GrepUtil.grepJsonKeyBySingleVal("CourseType",
							"1", temp).size();
					int ctemp2 = GrepUtil.grepJsonKeyBySingleVal("CourseType",
							"2", temp).size();
					if (ctemp0 > 1 || ctemp1 > 1 || ctemp2 > 1
							|| (ctemp1 + ctemp0) > 1 || (ctemp2 + ctemp0) > 1) {
						conf = true;
					}

					List<JSONObject> temp2 = GrepUtil.grepJsonKeyBySingleVal(
							"IsAdvance", "0", temp);
					List<JSONObject> temp3 = GrepUtil.grepJsonKeyBySingleVal(
							"IsAdvance", "1", temp);
					if (conf) {
						// 删除多余课程
						if (temp2.size() != temp.size() && temp3.size() > 0) {
							// 有预排课 有则删除非预排
							for (JSONObject tp : temp2) {
								tp.put("IsDelete", 1);
								getTeacherTimeConfMsg(teacherId, sber, tp,
										ruleConflict, 1, temp3.get(0));
							}
						} else {
							// 无预排课 看是否有合班课
							List<String> mcgIds = new ArrayList<String>();
							for (JSONObject obj : temp) {
								if (obj.containsKey("McGroupId")
										&& obj.getString("McGroupId").trim()
												.length() > 0) {
									if (!mcgIds.contains(obj.getString(
											"McGroupId").trim())) {
										mcgIds.add(obj.getString("McGroupId")
												.trim());
									}
								}
							}
							if (mcgIds.size() == 0) {
								// 无合班课 删除除第一位置外的所有课程
								for (int i = 1; i < temp2.size(); i++) {
									JSONObject tp = temp.get(i);
									tp.put("IsDelete", 1);
									getTeacherTimeConfMsg(teacherId, sber, tp,
											ruleConflict, 1, temp2.get(0));
								}

							} else {
								// 有合班课 删除 除第一个合班之外的所有课程
								String mcgId = mcgIds.get(0);
								temp3 = GrepUtil.grepJsonKeyBySingleVal(
										"McGroupId", mcgId, temp);
								for (JSONObject tp : temp) {
									String McId = tp.getString("McGroupId");
									if (McId == null
											|| !McId.equalsIgnoreCase(mcgId)) {
										tp.put("IsDelete", 1);
										getTeacherTimeConfMsg(teacherId, sber,
												tp, ruleConflict, 1,
												temp3.get(0));
									}
								}
							}
						}// 无预排结束

					}// 需要去重课程结束

				}// 循环节次map结束

			}
			// 单课眼校验结束
		}
		return true;
	}

	/**
	 * 获取教师冲突信息
	 * 
	 * @param teacherId
	 *            校验教师代码
	 * @param sber
	 *            校验结果字符串
	 * @param arrange
	 *            要删除的课程
	 * @param ruleConflict
	 *            冲突规则
	 * @param type
	 *            冲突类型 1:同时上多门课 2：不排位置冲突 3：最大上课数冲突
	 * @param confWith
	 *            冲突对象
	 */
	private void getTeacherTimeConfMsg(String teacherId, StringBuffer sber,
			JSONObject arrange, RuleConflict ruleConflict, int type,
			Object confWith) {
		// TODO Auto-generated method stub
		Map<String, Account> teacherDic = ruleConflict.getTeachersDic();
		Map<String, Classroom> classroomDic = ruleConflict.getClassroomsDic();
		Map<String, LessonInfo> coursedic = ruleConflict.getCoursesDic();
		String teacherName = "";
		if (teacherDic.containsKey(teacherId)) {
			teacherName = teacherDic.get(teacherId).getName();
		}
		String courseId = arrange.getString("CourseId");
		String courseName = "";
		if (coursedic.containsKey(courseId)) {
			courseName = coursedic.get(courseId).getName();
		}
		String classId = arrange.getString("ClassId");
		String className = "";
		if (classroomDic.containsKey(classId)) {
			className = classroomDic.get(classId).getClassName();
		}
		if (type == 1) {

			JSONObject less = (JSONObject) confWith;
			String confCid = less.getString("ClassId");
			String confclassName = "";
			int day = less.getIntValue("DayOfWeek");
			int lesson = less.getIntValue("LessonOfDay");
			if (classroomDic.containsKey(confCid)) {
				confclassName = classroomDic.get(confCid).getClassName();
			}

			sber.append(teacherName).append("  ").append(courseName)
					.append("  ").append(daydic[day]).append(lessondic[lesson])
					.append("  ").append("同时上课班级：").append(className)
					.append("、").append(confclassName).append(";");

		} else if (type == 2) {
			RulePosition pos = (RulePosition) confWith;
			int day = pos.getDay();
			int lesson = pos.getLesson();

			sber.append(teacherName).append("  ").append(className)
					.append(courseName).append("  ").append(daydic[day])
					.append(lessondic[lesson]).append("  与教师规则冲突，当前位置已设置为不可排")
					.append(";");
		} else if (type == 3) {
			int max = (int) confWith;
			int day = arrange.getIntValue("DayOfWeek");
			int lesson = arrange.getIntValue("LessonOfDay");
			sber.append(teacherName).append("  ").append(className)
					.append(courseName).append("  ").append(daydic[day])
					.append(lessondic[lesson]).append("  与教师规则冲突，每天最大排课数量为:")
					.append(max).append(";");
		}
	}

	/**
	 * 校验科目规则
	 * 
	 * @param sber
	 * @param arrangeMapKm
	 * @param ruleConflict
	 */
	private void tacCheckSubject(StringBuffer sber,
			HashMap<String, List<JSONObject>> arrangeMapKm,
			RuleConflict ruleConflict) {
		// TODO Auto-generated method stub
		for (Iterator<String> sbKey = arrangeMapKm.keySet().iterator(); sbKey
				.hasNext();) {
			String kmkey = sbKey.next();
			ruleConflict.getRuleCourses().get(kmkey);
			List<JSONObject> kmlist = arrangeMapKm.get(kmkey);
			kmlist = GrepUtil.grepJsonKeyBySingleVal("IsDelete", "0", kmlist);
			String gradeLev = kmkey.split("_")[0];
			if (!ruleConflict.getGradeLevelMaxAmNumMap().containsKey(gradeLev)) {
				System.out.println("fdsd");
			}
			int amNum = ruleConflict.getGradeLevelMaxAmNumMap().get(gradeLev);
			if (ruleConflict.getRuleCourses().get(kmkey) != null) {
				RuleCourse ruleCourse = ruleConflict.getRuleCourses()
						.get(kmkey);
				HashMap<Integer, HashMap<Integer, List<JSONObject>>> dayTeaMap = new HashMap<Integer, HashMap<Integer, List<JSONObject>>>();
				HashMap<Integer, HashMap<String, List<JSONObject>>> amMap = new HashMap<Integer, HashMap<String, List<JSONObject>>>();
				HashMap<Integer, HashMap<String, List<JSONObject>>> pmMap = new HashMap<Integer, HashMap<String, List<JSONObject>>>();

				// 去除重复的合班 合班课计一次
				List<String> countedMcgStr = new ArrayList<String>();
				List<String> needDelMcgStr = new ArrayList<String>();

				for (JSONObject json : kmlist) {
					int day = json.getIntValue("DayOfWeek");
					int lesson = json.getIntValue("LessonOfDay");
					String classId = json.getString("ClassId");
					String mcGroupId = json.getString("McGroupId");
					HashMap<Integer, List<JSONObject>> lessonMap = new HashMap<Integer, List<JSONObject>>();
					if (dayTeaMap.containsKey(day)) {
						lessonMap = dayTeaMap.get(day);
					} else {
						dayTeaMap.put(day, lessonMap);
					}
					List<JSONObject> lessonList = new ArrayList<JSONObject>();
					if (lessonMap.containsKey(lesson)) {
						lessonList = lessonMap.get(lesson);
					} else {
						lessonMap.put(lesson, lessonList);
					}

					lessonList.add(json);
					if (mcGroupId != null) {
						if (countedMcgStr.contains(day + "," + lesson + ","
								+ mcGroupId)) {
							continue;
						} else {
							countedMcgStr.add(day + "," + lesson + ","
									+ mcGroupId);
						}
					}
					if (lesson < amNum) {
						HashMap<String, List<JSONObject>> amlessonMap = new HashMap<String, List<JSONObject>>();
						if (amMap.containsKey(day)) {
							amlessonMap = amMap.get(day);
						} else {
							amMap.put(day, amlessonMap);
						}
						List<JSONObject> amlessonList = new ArrayList<JSONObject>();
						amlessonList.add(json);
						if (amlessonMap.containsKey(lesson + "," + classId)) {
							amlessonList = amlessonMap.get(lesson + ","
									+ classId);
						} else {
							amlessonMap.put(lesson + "," + classId,
									amlessonList);
						}
					} else {
						HashMap<String, List<JSONObject>> pmlessonMap = new HashMap<String, List<JSONObject>>();
						if (pmMap.containsKey(day)) {
							pmlessonMap = pmMap.get(day);
						} else {
							pmMap.put(day, pmlessonMap);
						}
						List<JSONObject> pmlessonList = new ArrayList<JSONObject>();
						pmlessonList.add(json);
						if (pmlessonMap.containsKey(lesson + "," + classId)) {
							pmlessonList = pmlessonMap.get(lesson + ","
									+ classId);
						} else {
							pmlessonMap.put(lesson + "," + classId,
									pmlessonList);
						}

					}

				}
				// 构建节次map结束
				// 校验不排规则
				List<RulePosition> rps = ruleCourse.getPositions();
				if (rps != null) {
					for (RulePosition rp : rps) {
						if (rp.getRuleType() != 0) {
							continue;
						}
						int pday = rp.getDay();
						int plesson = rp.getLesson();
						if (dayTeaMap.containsKey(pday)
								&& dayTeaMap.get(pday).containsKey(plesson)) {
							List<JSONObject> temp = dayTeaMap.get(pday).get(
									plesson);
							for (JSONObject tp : temp) {
								if (tp.getInteger("IsAdvance") != 1) {
									tp.put("IsDelete", 1);
									getSubjectConfMsg(tp.getString("CourseId"),
											sber, tp, ruleConflict, 1, rp);
								}
							}
						}
					}
				}
				// 校验不排位置结束
				// 开始校验上下午最大排课数

				int maxAm = ruleCourse.getMaxAMNum();
				if (maxAm > 0) {
					for (Iterator<Integer> daykey = amMap.keySet().iterator(); daykey
							.hasNext();) {

						int tday = daykey.next();
						HashMap<String, List<JSONObject>> lessonMap = amMap
								.get(tday);
						int maxAdNum = lessonMap.keySet().size();
						int fnum = maxAdNum;
						if (maxAdNum > maxAm) {

							boolean finished = false;
							Iterator<String> lessonkey = lessonMap.keySet()
									.iterator();
							while (maxAdNum > maxAm && !finished
									&& lessonkey.hasNext()) {
								String tlesson = lessonkey.next();
								List<JSONObject> temp = lessonMap.get(tlesson);
								List<JSONObject> temp2 = GrepUtil
										.grepJsonKeyBySingleVal("IsAdvance",
												"0", temp);
								for (JSONObject tp : temp2) {
									if (tp.getInteger("IsAdvance") != 1) {
										if (tp.getString("McGroupId") != null) {
											needDelMcgStr
													.add(tday
															+ ","
															+ tlesson
																	.split(",")[0]
															+ ","
															+ tp.getString("McGroupId"));
										}
										tp.put("IsDelete", 1);
										getSubjectConfMsg(
												tp.getString("CourseId"), sber,
												tp, ruleConflict, 2, maxAm);
									}
								}
								// 该位置全部为非预排课 删除所有 并且最大数递减
								if (temp.size() == temp2.size()) {
									maxAdNum--;
								}
								fnum--;
								// 全部键循环完成
								if (fnum == 0) {
									finished = true;
								}
							}
						}
					}
				}
				// 校验下午最大数冲突
				int maxPm = ruleCourse.getMaxPMNum();
				if (maxPm > 0) {
					for (Iterator<Integer> daykey = pmMap.keySet().iterator(); daykey
							.hasNext();) {

						int tday = daykey.next();
						HashMap<String, List<JSONObject>> lessonMap = pmMap
								.get(tday);
						int maxAdNum = lessonMap.keySet().size();
						int fnum = maxAdNum;
						if (maxAdNum > maxPm) {

							boolean finished = false;
							Iterator<String> lessonkey = lessonMap.keySet()
									.iterator();
							while (maxAdNum > maxPm && !finished
									&& lessonkey.hasNext()) {
								String tlesson = lessonkey.next();
								List<JSONObject> temp = lessonMap.get(tlesson);
								List<JSONObject> temp2 = GrepUtil
										.grepJsonKeyBySingleVal("IsAdvance",
												"0", temp);
								for (JSONObject tp : temp2) {
									if (tp.getString("McGroupId") != null) {
										needDelMcgStr.add(tday + ","
												+ tlesson.split(",")[0] + ","
												+ tp.getString("McGroupId"));
									}
									if (tp.getInteger("IsAdvance") != 1) {
										tp.put("IsDelete", 1);
										getSubjectConfMsg(
												tp.getString("CourseId"), sber,
												tp, ruleConflict, 3, maxPm);
									}
								}
								// 该位置全部为非预排课 删除所有 并且最大数递减
								if (temp.size() == temp2.size()) {
									maxAdNum--;
								}
								fnum--;
								// 全部键循环完成
								if (fnum == 0) {
									finished = true;
								}
							}
						}
					}
				}

				// 去除合班课需删除的
				for (String mcId : needDelMcgStr) {
					for (JSONObject arrange : kmlist) {
						String mkey = arrange.getString("DayOfWeek") + ","
								+ arrange.getString("LessonOfDay") + ","
								+ arrange.getString("McGroupId");

						if (mkey.equalsIgnoreCase(mcId)) {
							arrange.put("IsDelete", 1);
						}
					}
				}

			}
			// 科目是否需要校验结束
		}
		// 循环科目结束

	}

	/**
	 * 校验科目规则
	 * 
	 * @param sber
	 * @param arrangeMapGrd
	 * @param ruleConflict
	 */
	private void tacCheckGroundRule(StringBuffer sber,
			HashMap<String, List<JSONObject>> arrangeMapGrd,
			RuleConflict ruleConflict) {
		// TODO Auto-generated method stub
		Map<String, RuleGround> ruleGroundDic = ruleConflict.getRuleGrounds();
		if (ruleGroundDic != null && ruleGroundDic.size() > 0) {

			for (RuleGround ruleGround : ruleGroundDic.values()) {

				String grades = ruleGround.getUsedGradeIds();
				String subjectId = ruleGround.getSubjectId();
				int maxNum = ruleGround.getMaxClassNum();

				List<JSONObject> curGradesList = new ArrayList<JSONObject>();
				if (grades != null && grades.trim().length() > 0) {
					String[] gradeArr = grades.split(",");
					for (int i = 0; i < gradeArr.length; i++) {
						String ckey = gradeArr[i] + "_" + subjectId;
						if (arrangeMapGrd.containsKey(ckey)) {
							curGradesList.addAll(arrangeMapGrd.get(ckey));
						}
					}
				}
				// 组建节次映射
				HashMap<Integer, HashMap<Integer, List<JSONObject>>> groundMap = new HashMap<Integer, HashMap<Integer, List<JSONObject>>>();
				curGradesList = GrepUtil.grepJsonKeyBySingleVal("IsDelete",
						"0", curGradesList);

				// 去除重复的合班 合班课计一次
				List<String> countedMcgStr = new ArrayList<String>();
				List<String> needDelMcgStr = new ArrayList<String>();
				for (JSONObject arrange : curGradesList) {
					int day = arrange.getIntValue("DayOfWeek");
					int lesson = arrange.getIntValue("LessonOfDay");
					String mcGroupId = arrange.getString("mcGroupId");
					if (mcGroupId != null && mcGroupId.trim().length() > 0) {

						String mcgstr = day + "," + lesson + "," + mcGroupId;
						if (countedMcgStr.contains(mcgstr)) {
							continue;
						} else {
							countedMcgStr.add(mcgstr);
						}
					}
					HashMap<Integer, List<JSONObject>> dayMap = new HashMap<Integer, List<JSONObject>>();

					if (groundMap.containsKey(day)) {
						dayMap = groundMap.get(day);
					} else {
						groundMap.put(day, dayMap);
					}

					List<JSONObject> lessonList = new ArrayList<JSONObject>();
					if (dayMap.containsKey(lesson)) {
						lessonList = dayMap.get(lesson);
					} else {
						dayMap.put(lesson, lessonList);
					}
					lessonList.add(arrange);
				}
				// 开始校验
				// 开始校验每节课是否有多班级上课
				for (Iterator<Integer> daykey = groundMap.keySet().iterator(); daykey
						.hasNext();) {
					int tday = daykey.next();
					HashMap<Integer, List<JSONObject>> dayMap = groundMap
							.get(tday);
					for (Iterator<Integer> lessonKey = dayMap.keySet()
							.iterator(); lessonKey.hasNext();) {
						int tlesson = lessonKey.next();
						List<JSONObject> temp = dayMap.get(tlesson);
						List<JSONObject> temp2 = GrepUtil
								.grepJsonKeyBySingleVal("IsAdvance", "0", temp);
						List<JSONObject> temp3 = GrepUtil
								.grepJsonKeyBySingleVal("IsAdvance", "1", temp);

						int curSize = temp.size();
						if (isOverByList(temp,maxNum)) {
							// 删除多余课程
							if (temp2.size() != temp.size() && temp3.size() > 0) {
								// 有预排课 有则删除非预排
								for (JSONObject tp : temp2) {
									tp.put("IsDelete", 1);
									if (tp.containsKey("McGroupId")
											&& tp.getString("McGroupId").trim()
													.length() > 0) {
										needDelMcgStr.add(tday
												+ ","
												+ tlesson
												+ ","
												+ tp.getString("McGroupId")
														.trim());
									}
									getGroudRuleConfMsg(sber, tp, ruleConflict,
											1, maxNum);
								}
							} else {
								// 无预排课 看是否有合班课
								for (JSONObject tp : temp2) {
									if (isOverByList(temp2,maxNum)) {
										tp.put("IsDelete", 1);
										if (tp.containsKey("McGroupId")
												&& tp.getString("McGroupId")
														.trim().length() > 0) {
											needDelMcgStr.add(tday
													+ ","
													+ tlesson
													+ ","
													+ tp.getString("McGroupId")
															.trim());
										}
										getGroudRuleConfMsg(sber, tp,
												ruleConflict, 1, maxNum);
										curSize--;
									}
								}
							}// 无预排结束

						}// 需要校验场地规则

					}// 循环节次

				}// 校验场地规则结束
				for (JSONObject tp : curGradesList) {
					if (tp.containsKey("McGroupId")
							&& needDelMcgStr
									.contains(tp.getString("McGroupId"))) {
						tp.put("IsDelete", 1);
					}
				}
			}
		}
	}

	//根据课程类型校验是否超课时
	
	private boolean isOverByList(List<JSONObject> temp, int maxNum) {
		// TODO Auto-generated method stub
		List<Integer> typelist = new ArrayList<Integer>();
		for(JSONObject t:temp){
			if(t.containsKey("IsDelete")&&t.getIntValue("IsDelete")==1){
				continue;
			}
			int ctype = t.getIntValue("CourseType");
			typelist.add(ctype);
		}
		
		double sum = 0;
		List<Integer> l1 = new ArrayList<Integer>();
		List<Integer> l2 = new ArrayList<Integer>();
		for(int ct :typelist ){
			if(ct ==0){
				sum++;
			}else if(ct==1){
				l1 .add(ct);
			}else if(ct==2){
				l2 .add(ct);
			}
		}
		if(l1.size()==l2.size()){
			sum += l1.size();
		}else if(l1.size()>l2.size()){
			sum += l2.size();
			sum += (l1.size()-l2.size());
		}else if(l1.size()<l2.size()){
			sum += l1.size();
			sum += (l2.size()-l1.size());
		}
		if(sum>maxNum){
			return true;
		}
		return false;
	}

	/**
	 * 校验单双周规则
	 * 
	 * @param sber
	 * @param sdArrangeList
	 * @param ruleConflict
	 */
	private void tacCheckSdWeek(StringBuffer sber,
			List<JSONObject> sdArrangeList, RuleConflict ruleConflict) {
		// TODO Auto-generated method stub
		// 暂时不管 待分组调课做完后处理
	}

	/**
	 * 校验单合班规则
	 * 
	 * @param sber
	 * @param mcArrangeList
	 * @param ruleConflict
	 */
	private void tacCheckMcGroup(StringBuffer sber,
			List<JSONObject> mcArrangeList, RuleConflict ruleConflict) {
		// TODO Auto-generated method stub
		Map<String, Classroom> classroomDic = ruleConflict.getClassroomsDic();
		Map<String, LessonInfo> coursedic = ruleConflict.getCoursesDic();
		for (JSONObject arrange : mcArrangeList) {
			String mcGroupId = arrange.getString("McGroupId");
			String classId = arrange.getString("ClassId");
			String courseId = arrange.getString("CourseId");
			int IsAdvance = arrange.getIntValue("IsAdvance");
			int day = arrange.getIntValue("DayOfWeek");
			int lesson = arrange.getIntValue("LessonOfDay");
			if (ruleConflict.getMcGroupId(classId, courseId)!=null&&
					ruleConflict.getMcGroupId(classId, courseId).equalsIgnoreCase(
					mcGroupId)
					|| IsAdvance == 1) {

			} else {
				arrange.put("IsDelete", 1);

				String courseName = "";
				if (coursedic.containsKey(courseId)) {
					courseName = coursedic.get(courseId).getName();
				}
				String className = "";
				if (classroomDic.containsKey(classId)) {
					className = classroomDic.get(classId).getClassName();
				}
				sber.append(className).append(courseName).append("  ")
						.append(daydic[day]).append(lessondic[lesson])
						.append("  在合班规则中未找到，不再是合班课").append(";");
			}
		}
	}

	/**
	 * 生成校验科目提示数据
	 * 
	 * @param string
	 * @param sber
	 * @param json
	 *            冲突实体
	 * @param ruleConflict
	 *            冲突规则对象
	 * @param type
	 *            冲突类型 1:不排冲突；2:上午最大数超过；3：下午最大数超过
	 * @param confWith
	 *            冲突对象
	 */
	private void getSubjectConfMsg(String string, StringBuffer sber,
			JSONObject json, RuleConflict ruleConflict, int type,
			Object confWith) {
		// TODO Auto-generated method stub
		int day = json.getIntValue("DayOfWeek");
		int lesson = json.getIntValue("LessonOfDay");
		String classId = json.getString("ClassId");
		String courseId = json.getString("CourseId");

		Map<String, Classroom> classroomDic = ruleConflict.getClassroomsDic();
		Map<String, LessonInfo> coursedic = ruleConflict.getCoursesDic();

		String courseName = "";
		if (coursedic.containsKey(courseId)) {
			courseName = coursedic.get(courseId).getName();
		}
		String className = "";
		if (classroomDic.containsKey(classId)) {
			className = classroomDic.get(classId).getClassName();
		}
		sber.append(className).append(courseName).append("  ")
				.append(daydic[day]).append(lessondic[lesson]);

		switch (type) {
		case 1:
			sber.append("  科目规则中该课程在本位置不可排").append(";");
			break;
		case 2:
			sber.append("  科目规则中该课程上午最大排课数为：" + (int) confWith).append(";");
			break;
		case 3:
			sber.append("  科目规则中该课程下午最大排课数为：" + (int) confWith).append(";");
			break;

		default:
			break;
		}
	}

	private void getGroudRuleConfMsg(StringBuffer sber, JSONObject json,
			RuleConflict ruleConflict, int type, int maxNum) {
		// TODO Auto-generated method stub
		int day = json.getIntValue("DayOfWeek");
		int lesson = json.getIntValue("LessonOfDay");
		String classId = json.getString("ClassId");
		String courseId = json.getString("CourseId");

		Map<String, Classroom> classroomDic = ruleConflict.getClassroomsDic();
		Map<String, LessonInfo> coursedic = ruleConflict.getCoursesDic();

		String courseName = "";
		if (coursedic.containsKey(courseId)) {
			courseName = coursedic.get(courseId).getName();
		}
		String className = "";
		if (classroomDic.containsKey(classId)) {
			className = classroomDic.get(classId).getClassName();
		}
		sber.append(className).append(courseName).append("  ")
				.append(daydic[day]).append(lessondic[lesson])
				.append("  场地规则中该课程最大排班数为：" + maxNum).append(";");
	}

}
