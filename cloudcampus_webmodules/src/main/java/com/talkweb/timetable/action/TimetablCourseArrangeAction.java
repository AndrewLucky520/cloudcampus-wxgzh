package com.talkweb.timetable.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.Account;
import com.talkweb.accountcenter.thrift.Classroom;
import com.talkweb.accountcenter.thrift.Grade;
import com.talkweb.accountcenter.thrift.LessonInfo;
import com.talkweb.accountcenter.thrift.School;
import com.talkweb.base.common.CacheExpireTime;
import com.talkweb.common.action.BaseAction;
import com.talkweb.common.tools.UUIDUtil;
import com.talkweb.commondata.dao.RedisOperationDAO;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.timetable.arrangement.algorithm.RuleConflict;
import com.talkweb.timetable.arrangement.exception.ArrangeTimetableException;
import com.talkweb.timetable.arrangement.service.ArrangeDataService;
import com.talkweb.timetable.arrangement.service.SmartArrangeService;
import com.talkweb.timetable.arrangement.service.impl.TimetableUtil;
import com.talkweb.timetable.dynamicProgram.service.DynamicProgramService;
import com.talkweb.timetable.service.TimetableService;
/**
 * @author 
 *
 *2015年11月10日
 */
@Controller
@RequestMapping(value = "/timetableManage/")

public class TimetablCourseArrangeAction extends BaseAction {

	private void setPromptMessage(JSONObject object, String code, String message) {
		object.put("code", code);
		object.put("msg", message);
	}
	@Autowired
	private TimetableService timetableService;
	@Autowired
	private SmartArrangeService smartArrangeService;
	@Autowired
	private ArrangeDataService arrangeDataService;
	@Autowired
	private AllCommonDataService commonDataService;
	
	@Autowired
	private DynamicProgramService dynamicProgramService;

	private static final Logger logger = LoggerFactory
			.getLogger(TimetableAction.class);

	@Resource(name="redisOperationDAOSDRTempDataImpl")
 	private RedisOperationDAO redisOperationDAO;
	
	/**
	 * 获取临时文件保存目录
	 */
	@Value("#{settings['timetable.programVersion']}")
	private String programVersion;
	
	@RequestMapping(value = "courseArrange/getIntelligentCourseInfo", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getIntelligentCourseInfo(HttpServletRequest req,
			@RequestBody JSONObject reqData, HttpServletResponse res) {
		JSONObject result = new JSONObject();
		String timetableId = reqData.getString("timetableId");
		String gradeIdStr = reqData.getString("gradeId");
		String schoolId = getXxdm(req);
		String termInfoId = reqData.getString("selectedSemester");
		String code = "0";
		String msg = "";
		if (StringUtils.isNotBlank(timetableId)) {
			List<String> gradeIds = null;
			if (StringUtils.isNotEmpty(gradeIdStr)) {
				gradeIds = Arrays.asList(gradeIdStr.split(","));
			}
//			JSONObject data = this.timetableService.getTimetableSections(
//					schoolId, timetableId, gradeIds, getSchool(req));
			JSONObject data = this.smartArrangeService.getIntelligentCourseInfo(schoolId, timetableId, gradeIds, getSchool(req,termInfoId),
					 req.getSession().getId(),termInfoId);
			result.put("data", data);
		} else {
			code = "1";
			msg = "亲,课表是必须要选的哦";
		}

		this.setPromptMessage(result, code, msg);

		return result;
	}

	@RequestMapping(value = "courseArrange/startGenerate", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject startGenerate(HttpServletRequest req,
			@RequestBody JSONObject reqData, HttpServletResponse res) {
		JSONObject result = new JSONObject();
		final String timetableId = reqData.getString("timetableId");
		String gradeIdsStr = reqData.getString("gradeIds");
		// final String schoolId = getXxdm(req);
		String selectedSemester = reqData.getString("selectedSemester");
		logger.info("开始排课进入请求时的sessionID：" + req.getSession().getId());
		final School school = this.getSchool(req,selectedSemester);
		String code = "0";
		String msg = "";
		if (StringUtils.isBlank(timetableId)) {
			code = "-1";
			msg = "亲,课表是必须要选的哦";
		}

		if (StringUtils.isBlank(gradeIdsStr)) {
			code = "-1";
			msg = "不选年级,我是生成不了课表的";

		}

		if (!code.equals("0")) {
			this.setPromptMessage(result, code, msg);
			return result;
		}
		// -1不控制教学进度 1控制教学进度
		int isTeachingSync  = 1; 
		//1按教师天内集中 2按教师天内分散
		int lessonDistrubute  = 1;
		//尽量排完按钮是否开启
		int isTryFinish = 1;
		//教师半天内连上次数
		int teaSpNum = 3;
		try{
			if(reqData.containsKey("isTeachingSync")){
				isTeachingSync = Integer.parseInt(reqData.getString("isTeachingSync"));
			}
			if(reqData.containsKey("lessonDistrubute")){
				lessonDistrubute =  Integer.parseInt(reqData.getString("lessonDistrubute"));
			}
			if(reqData.containsKey("isTryFinish")){
				isTryFinish = Integer.parseInt(reqData.getString("isTryFinish"));
			}
			if(reqData.containsKey("teaSpNum")){
				String teaSpNumStr = reqData.getString("teaSpNum");
				if(teaSpNumStr.trim().length()>0){
					teaSpNum = Integer.parseInt(teaSpNumStr);
				}
			}
		}catch(Exception e)	{
			code= "-1";
			msg = "请设置教师进度同步方式及课时分布方式！";
			this.setPromptMessage(result, code, msg);
			return result;
		}
		final JSONObject runParams = new JSONObject();
		runParams.put("isTeachingSync", isTeachingSync);
		runParams.put("isTryFinish", isTryFinish);
		runParams.put("lessonDistrubute", lessonDistrubute);
		runParams.put("teaSpNum", teaSpNum);
		final HttpSession session = req.getSession();

		final List<String> gradeIds = Arrays.asList(gradeIdsStr.split(","));

		dynamicProgramService.updateArrangeProgress(session.getId(),  
				0, 0, "即将开始排课...");

		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					if(programVersion!=null&&programVersion.equals("v2")){
						dynamicProgramService.startTask(session, school,
								timetableId, gradeIds,  runParams);
					}else{
						smartArrangeService.autoArrangeTimeTable(session, school,
								timetableId, gradeIds, runParams);
					}
					logger.info("开始排课子线程的sessionID：" + session.getId());
				} catch (ArrangeTimetableException e) {
					smartArrangeService.updateArrangeProgress(session.getId(), -1, 0, "编排失败:" + e.getMessage());
					e.printStackTrace();

				} catch (Exception e2) {
					smartArrangeService.updateArrangeProgress(session.getId(), -1, 0, "编排失败:请稍后重试");

					e2.printStackTrace();
				}

			}
		});

		thread.start();
		//清除缓存 
		clearProgDataByKey( req.getSession().getId(),timetableId,null);
		this.setPromptMessage(result, code, msg);
		return result;
	}

	@RequestMapping(value = "courseArrange/getArrangeCourseProgress", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getArrangeCourseProgress(HttpServletRequest req,
			HttpServletResponse res) {
		JSONObject result = new JSONObject();
		// code: 0 开始 1 正在编排 2 成功结束 -1 失败结束

		int code = 0;
		double progress = 0;
		String msg = "";
		logger.info("查询进度时的sessionID：" + req.getSession().getId());
		Object arrangeKey = "timetable."  +req.getSession().getId()
				+ ".courseSmtArrange.progress";
		JSONObject obj = null;
		try {
			obj = (JSONObject) redisOperationDAO.get(arrangeKey );
			logger.info("查询进度时的进度结构：{}" , obj);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(obj==null){
			result.put("code", -100);
			result.put("data", "用户身份信息已失效，请重新登陆！");
			
			System.out.println("[课表进度]用户身份信息已失效，请重新登陆！");
		}
		code = (int) (obj.containsKey("arrangeCode") ? 0 : obj
				.getInteger("arrangeCode"));
		Object arrangeCode = obj.get("arrangeCode");
		if (arrangeCode != null) {
			code = Integer.parseInt(arrangeCode.toString());
		}

		Object arrangeProgress = obj.get("arrangeProgress");
		if (arrangeProgress != null) {
			progress = Double.parseDouble(arrangeProgress.toString());
		}

		Object arrangeMsg = obj.get("arrangeMsg");
		if (arrangeProgress != null) {
			msg = arrangeMsg.toString();
		}

		result.put("code", code);

		JSONObject data = new JSONObject();
		data.put("progress", progress);
		data.put("msg", msg);

		result.put("data", data);

		return result;
	}
	
	
	/**
	 * 班级微调 冲突
	 * 
	 * @param req
	 * @param res
	 * @param reqData
	 * @return
	 */
	@RequestMapping(value = "adjustment/forFineTuning", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject forFineTuning(HttpServletRequest req,
			HttpServletResponse res, @RequestBody JSONObject reqData) {
		JSONObject result = new JSONObject();
		String timetableId = reqData.getString("timetableId");
		String gradeId = reqData.getString("gradeId");
		String classId = reqData.getString("classId");
		String selectedSemester = reqData.getString("selectedSemester");
		int breakRule = 0;
		if(reqData.containsKey("breakRule")){
			breakRule = reqData.getIntValue("breakRule");
		}
		School school =  getSchool (req,selectedSemester);
		String code = "0";
		String msg = "";

		JSONObject data = new JSONObject();

		try {
			data = this.smartArrangeService.getForFineTuningDataByClass(school,
					timetableId, gradeId, classId,   req
							.getSession().getId(),breakRule);
		} catch (Exception e) {
			code = "-1";
			msg = e.getMessage();
			e.printStackTrace();
		}

		this.setPromptMessage(result, code, msg);
		result.put("data", data);

		return result;
	}
	
	/**
	 * 教师微调 冲突
	 * 
	 * @param req
	 * @param res
	 * @param reqData
	 * @return
	 */
	@RequestMapping(value = "adjustment/forFineTuningByTeacher", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject forFineTuningByTeacher(HttpServletRequest req,
			HttpServletResponse res, @RequestBody JSONObject reqData) {
		JSONObject result = new JSONObject();
		String timetableId = reqData.getString("timetableId");
//		String gradeId = reqData.getString("gradeId");
		String teacherId = reqData.getString("teacherId");
		// String schoolId = getXxdm(req);
		String selectedSemester = reqData.getString("selectedSemester");
		School school =  getSchool (req,selectedSemester);
		String code = "0";
		String msg = "";
		int breakRule = 0;
		if(reqData.containsKey("breakRule")){
			breakRule = reqData.getIntValue("breakRule");
		}
		JSONObject data = new JSONObject();

		try {
			data = this.smartArrangeService.getForFineTuningDataByTeacher(school,
					timetableId, teacherId,  req
							.getSession().getId(),breakRule);
		} catch (Exception e) {
			code = "-1";
			msg = e.getMessage();
			e.printStackTrace();
		}

		this.setPromptMessage(result, code, msg);
		result.put("data", data);

		return result;
	}


	/**
	 * 班级微调 冲突
	 * 
	 * @param req
	 * @param res
	 * @param reqData
	 * @return
	 */
	@RequestMapping(value = "adjustment/isSwitchCached", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject isSwitchCached(HttpServletRequest req,
			HttpServletResponse res, @RequestBody JSONObject reqData) {
		JSONObject result = new JSONObject();
		
		int code =0 ;
		String sessionId = req.getSession().getId();
		
		String timetableId = reqData.getString("timetableId");
		
		Object tbBaseKey = "timetable."+timetableId+".10.tbBaseDataCache";
		
		Object tbKey = "timetable."+timetableId+".10.tbDataCache";
		try {
			JSONObject tbData = (JSONObject) redisOperationDAO.get(tbKey);
			JSONObject tbBaseData = (JSONObject) redisOperationDAO.get(tbBaseKey);
			if(tbData!=null&&tbBaseData!=null){
				code =1;
			}
			
		}catch (Exception e){
			e.printStackTrace();
		}
		
		result.put("code", code);
		
		return result;
	}
	/**
	 * 取年级课表
	 * 
	 * @param req
	 * @param res
	 * @param reqData
	 * @return
	 */
	@RequestMapping(value = "adjustment/forFineTuningByGrade", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject forFineTuningByGrade(HttpServletRequest req,
			HttpServletResponse res, @RequestBody JSONObject reqData) {
		JSONObject result = new JSONObject();
		String timetableId = reqData.getString("timetableId");
		String gradeId = reqData.getString("gradeId");
		// String schoolId = getXxdm(req);
		String code = "0";
		String msg = "";
		String selectedSemester = reqData.getString("selectedSemester");
		School school =  getSchool (req,selectedSemester);
		JSONObject data = new JSONObject();
		int breakRule = 0;
		if(reqData.containsKey("breakRule")){
			breakRule = reqData.getIntValue("breakRule");
		}
		try {
			data = this.smartArrangeService.getForFineTuningDataByGrade(school,
					timetableId, gradeId,  req.getSession().getId(), breakRule);
		} catch (Exception e) {
			code = "-1";
			msg = e.getMessage();
		}

		this.setPromptMessage(result, code, msg);
		result.put("data", data);

		return result;
	}

	/**
	 * 单个点 年级冲突位置
	 * 
	 * @param req
	 * @param res
	 * @param reqData
	 * @return
	 */
	@RequestMapping(value = "adjustment/positionConflicts", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject positionConflicts(HttpServletRequest req,
			HttpServletResponse res, @RequestBody JSONObject reqData) {
		JSONObject result = new JSONObject();
		String timetableId = reqData.getString("timetableId");
		String gradeId = reqData.getString("gradeId");
		String classId = reqData.getString("classId");
		String courseIds = reqData.getString("courseIds");
		String[] courseTypes = reqData.getString("courseTypes").split(",");
		int[] courseTypes2 = new int[courseTypes.length];
		for(int i=0;i<courseTypes.length;i++){
			courseTypes2[i] = Integer.parseInt(courseTypes[i]);
		}
		int dayOfWeek = reqData.getIntValue("dayOfWeek");
		int lessonOfDay = reqData.getIntValue("lessonOfDay");
		String mcGroupIs = "";
		if(reqData.containsKey("McGroupIds")){
			mcGroupIs = reqData.getString("McGroupIds");
		}
		// String schoolId = getXxdm(req);
		String selectedSemester = reqData.getString("selectedSemester");
		School school =  getSchool (req,selectedSemester);
		String code = "0";
		String msg = "";
		int breakRule = 0;
		if(reqData.containsKey("breakRule")){
			breakRule = reqData.getIntValue("breakRule");
		}
		List<JSONObject> conflicts = new ArrayList<JSONObject>();

		try {
			conflicts = this.smartArrangeService.getPositionConflicts(school,
					timetableId, gradeId, classId, courseIds.split(","),courseTypes2,
					dayOfWeek, lessonOfDay,  req
					.getSession().getId(),mcGroupIs.split(","),breakRule);
		} catch (Exception e) {
			code = "-1";
			msg = e.getMessage();
			e.printStackTrace();
		}

		this.setPromptMessage(result, code, msg);
		result.put("conflicts", conflicts);

		return result;
	}

	@RequestMapping(value = "adjustment/changeLessonPosition", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject changeLessonPosition(HttpServletRequest req,
			HttpServletResponse res, @RequestBody JSONObject reqData) {
		JSONObject result = new JSONObject();
		String timetableId = reqData.getString("timetableId");
		String classId = reqData.getString("classId");
		double unLessonCount = -1;
		if(reqData.containsKey("unLessonCount")){
			unLessonCount = reqData.getDoubleValue("unLessonCount");
		}
		
		String fromCourseIdsStr = reqData.getString("fromCourseIds");
		Integer fromDayOfWeek = reqData.get("fromDayOfWeek") != null ? reqData
				.getIntValue("fromDayOfWeek") : null;
		Integer fromLessonOfDay = reqData.get("fromLessonOfDay") != null ? reqData
				.getIntValue("fromLessonOfDay") : null;
		String toCourseIdsStr = reqData.getString("toCourseIds");
		Integer toDayOfWeek = reqData.get("toDayOfWeek") != null ? reqData
				.getIntValue("toDayOfWeek") : null;
		Integer toLessonOfDay = reqData.get("toLessonOfDay") != null ? reqData
				.getIntValue("toLessonOfDay") : null;
		String schoolId = getXxdm(req);

		String[] fromCourseTypes = reqData.getString("fromCourseTypes").split(
				",");
		String[] toCourseTypes = reqData.getString("toCourseTypes").split(",");
		
		String[] fromMcgId = reqData.getString("fromMcGroupId").split(",");
		String[] toMcgId = reqData.getString("toMcGroupId").split(",");
		
		String code = "0";
		String msg = "";
		String termInfoId = reqData.getString("selectedSemester");
		String[] fromCourseIds = fromCourseIdsStr.split(",");
		String[] toCourseIds = toCourseIdsStr.split(",");
		String selectedSemester = reqData.getString("selectedSemester");
		School school =  getSchool (req,selectedSemester);
		int breakRule = 0;
		if(reqData.containsKey("breakRule")){
			breakRule = reqData.getIntValue("breakRule");
		}
//		RuleConflict ruleConflict = new RuleConflict();
		JSONObject hcData = smartArrangeService.loadDataByMapTask(  req.getSession().getId(), schoolId,
				timetableId, termInfoId, school);
		RuleConflict ruleConflict = (RuleConflict) hcData.get("ruleConflict"+termInfoId);
		List<JSONObject> arrangeList = new ArrayList<JSONObject>();
		if(hcData.containsKey("arrangeList") ){
			arrangeList = (List<JSONObject>) hcData.get("arrangeList");
		}else{
			arrangeList = this.timetableService.getArrangeTimetableList(schoolId,
					timetableId, null);
		}

		try {
			this.arrangeDataService.changeLessonPosition(schoolId, classId,
					timetableId, fromCourseIds, fromDayOfWeek, fromLessonOfDay,
					toCourseIds, toDayOfWeek, toLessonOfDay,fromCourseTypes,
					toCourseTypes,fromMcgId,toMcgId,unLessonCount,ruleConflict,arrangeList,breakRule);
			//清除缓存-排课结果
			clearProgDataByKey( req.getSession().getId(),timetableId,"arrangeList");


		} catch (Exception e) {
			code = "-1";
			msg = e.getMessage();
			e.printStackTrace();
		}
		this.setPromptMessage(result, code, msg);

		return result;
	}
	@RequestMapping(value = "adjustment/changeLessonPositionForTeacher", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject changeLessonPositionForTeacher(HttpServletRequest req,
			HttpServletResponse res, @RequestBody JSONObject reqData) {
		JSONObject result = new JSONObject();
		String timetableId = reqData.getString("timetableId");
		String teacherId = reqData.getString("teacherId");
		double unLessonCount = -1;
		if(reqData.containsKey("unLessonCount")){
			unLessonCount = reqData.getDoubleValue("unLessonCount");
		}
		
		JSONArray fromCourses = reqData.getJSONArray("fromCourseIds");
		
		Integer fromDayOfWeek = reqData.get("fromDayOfWeek") != null ? 
				reqData.getIntValue("fromDayOfWeek") : null;
		Integer fromLessonOfDay = reqData.get("fromLessonOfDay") != null ? 
				reqData.getIntValue("fromLessonOfDay") : null;
				
		JSONArray toCourses = reqData.getJSONArray("toCourseIds");
		Integer toDayOfWeek = reqData.get("toDayOfWeek") != null ?
				reqData.getIntValue("toDayOfWeek") : null;
		Integer toLessonOfDay = reqData.get("toLessonOfDay") != null ? 
				reqData.getIntValue("toLessonOfDay") : null;
				
		int breakRule = 0;
		if(reqData.containsKey("breakRule")){
			breakRule = reqData.getIntValue("breakRule");
		}
		String schoolId = getXxdm(req);
		String code = "0";
		String msg = "";
		String termInfoId = reqData.getString("selectedSemester");
//		RuleConflict ruleConflict = new RuleConflict();
		JSONObject hcData = smartArrangeService.loadDataByMapTask(  req.getSession().getId(), schoolId,
				timetableId, termInfoId, getSchool(req,termInfoId));
		RuleConflict ruleConflict = (RuleConflict) hcData.get("ruleConflict"+termInfoId);
		List<JSONObject> arrangeList = new ArrayList<JSONObject>();
		if(hcData.containsKey("arrangeList") ){
			arrangeList = (List<JSONObject>) hcData.get("arrangeList");
		}else{
			arrangeList = this.timetableService.getArrangeTimetableList(schoolId,
					timetableId,null);
		}
		
		try {
			this.arrangeDataService.changeLessonPositionForTeacher(schoolId, 
					timetableId, fromCourses, fromDayOfWeek, fromLessonOfDay,
					toCourses, toDayOfWeek, toLessonOfDay,unLessonCount,ruleConflict,arrangeList,breakRule);
			//清除缓存-排课结果
			clearProgDataByKey( req.getSession().getId(),timetableId,"arrangeList");
			
			
		} catch (Exception e) {
			code = "-1";
			msg = e.getMessage();
			e.printStackTrace();
		}
		this.setPromptMessage(result, code, msg);
		
		return result;
	}

	@RequestMapping(value = "adjustment/deleteLessonPosition", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject deleteLessonPosition(HttpServletRequest req,
			HttpServletResponse res, @RequestBody JSONObject reqData) {
		JSONObject result = new JSONObject();
		String timetableId = reqData.getString("timetableId");
		String classId = reqData.getString("classId");
		String courseIdsStr = reqData.getString("courseIds");
		int dayOfWeek = reqData.getIntValue("dayOfWeek");
		int lessonOfDay = reqData.getIntValue("lessonOfDay");
		String mcGroupId = "";
		if(reqData.containsKey("McGroupIds")){
			mcGroupId = reqData.getString("McGroupIds");
		}
		String schoolId = getXxdm(req);
		String code = "0";
		String msg = "";
		String termInfoId = reqData.getString("selectedSemester");
		String[] courseIds = courseIdsStr.split(",");
		String[] mcgIds = mcGroupId.split(",");
//		RuleConflict ruleConflict = new RuleConflict();
		int breakRule = 0;
		if(reqData.containsKey("breakRule")){
			breakRule = reqData.getIntValue("breakRule");
		}
		JSONObject hcData = smartArrangeService.loadDataByMapTask(  req.getSession().getId(), schoolId,
				timetableId, termInfoId, getSchool(req,termInfoId));
		RuleConflict ruleConflict = (RuleConflict) hcData.get("ruleConflict"+termInfoId);
		
		int resultCode = this.arrangeDataService.deleteLessonPosition(schoolId,
				classId, timetableId, courseIds, dayOfWeek, lessonOfDay,mcgIds,ruleConflict,breakRule);
		if (resultCode > 0) {
			//清除缓存-排课结果
			clearProgDataByKey( req.getSession().getId(),timetableId,"arrangeList");

			code = "0";
			msg = "删除成功";
		} else {
			code = "-1";
			msg = "预排课程无法删除，请在科目预排中处理!";
		}

		this.setPromptMessage(result, code, msg);
		return result;
	}

	@RequestMapping(value = "adjustment/deleteLessonPositionForTeacher", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject deleteLessonPositionForTeacher(HttpServletRequest req,
			HttpServletResponse res, @RequestBody JSONObject reqData) {
		JSONObject result = new JSONObject();
		String timetableId = reqData.getString("timetableId");
		JSONArray classCourse = reqData.getJSONArray("classCourse");
		
		int dayOfWeek = reqData.getIntValue("dayOfWeek");
		int lessonOfDay = reqData.getIntValue("lessonOfDay");
		
		String schoolId = getXxdm(req);
		String code = "0";
		String msg = "";
		String termInfoId = reqData.getString("selectedSemester");
//		RuleConflict ruleConflict = new RuleConflict();
		JSONObject hcData = smartArrangeService.loadDataByMapTask(  req.getSession().getId(), schoolId,
				timetableId, termInfoId, getSchool(req,termInfoId));
		RuleConflict ruleConflict = (RuleConflict) hcData.get("ruleConflict"+termInfoId);
		int breakRule = 0;
		if(reqData.containsKey("breakRule")){
			breakRule = reqData.getIntValue("breakRule");
		}
		int resultCode = this.arrangeDataService.deleteLessonPositionForTeacher(schoolId,
				  timetableId, classCourse, dayOfWeek, lessonOfDay,ruleConflict,breakRule);
		if (resultCode>0) {
			//清除缓存-排课结果
			clearProgDataByKey( req.getSession().getId(),timetableId,"arrangeList");

			code = "0";
			msg = "删除成功";
		} else {
			code = "-1";
			msg = "预排课程无法删除，请在科目预排中处理!";
		}

		this.setPromptMessage(result, code, msg);
		return result;
	}


	@RequestMapping(value = "adjustment/lessonOfTeacher", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject lessonOfTeacher(HttpServletRequest req,
			HttpServletResponse res, @RequestBody JSONObject reqData) {
		JSONObject result = new JSONObject();
		String timetableId = reqData.getString("timetableId");
		String teacherIds = reqData.getString("teacherIds");
		String schoolId = getXxdm(req);
		String selectedSemester = reqData.getString("selectedSemester");
		School school =  getSchool (req,selectedSemester);
		String code = "0";
		String msg = "";
		String termInfoId = reqData.getString("selectedSemester");
		List<JSONObject> dataList = new ArrayList<JSONObject>();

		for (String teacherId : teacherIds.split(",")) {

			try {
				List<JSONObject> arrangeList = this.timetableService
						.getArrangeTimetableList(schoolId, timetableId,null);
				List<JSONObject> taskList = this.timetableService
						.getTaskByTimetable(schoolId, timetableId);
				Map<String, Account> teacherDic = this.arrangeDataService
						.getTeachersDic(school,termInfoId);
				JSONObject timetableInfo = this.timetableService
						.getArrangeTimetableInfo(schoolId, timetableId);
				Map<String, Classroom> classroomsDic = this.arrangeDataService
						.getClassRoomsDic(school, null, termInfoId);
				Map<String, LessonInfo> coursesDic = this.arrangeDataService
						.getCoursesDic(school,termInfoId);
				Map<String, Grade> gradeDic = this.arrangeDataService
						.getGradesDic(termInfoId.substring(0,4), school,termInfoId.substring(4,5));

				HashMap<String, HashMap<String, HashMap<String, String>>> taskMap = getMapByList(taskList,teacherDic);
				JSONObject data = this.smartArrangeService
						.getTimetableByTeacher(school, timetableId, teacherId,
								arrangeList, taskList, teacherDic,
								timetableInfo, classroomsDic, coursesDic,
								gradeDic, taskMap );
				dataList.add(data);

			} catch (Exception e) {
				code = "-1";
				msg = e.getMessage();
			}

		}

		this.setPromptMessage(result, code, msg);
		result.put("data", dataList);

		return result;
	}

	private HashMap<String, HashMap<String, HashMap<String, String>>> getMapByList(
			List<JSONObject> taskList, Map<String, Account> teacherDic) {
		// TODO Auto-generated method stub'
		HashMap<String, HashMap<String, HashMap<String, String>>> taskMap = new HashMap<String, HashMap<String,HashMap<String,String>>>();
		Map<String, Account> teaDic = teacherDic;
		for(JSONObject task:taskList){
			String classId = task.getString("ClassId");
			String courseId = task.getString("CourseId");
			String teacherId = task.getString("TeacherId");
			 HashMap<String, HashMap<String, String>> courseMap = new HashMap<String, HashMap<String,String>>();
			 if(taskMap.containsKey(classId)){
				 courseMap = taskMap.get(classId);
			 }else{
				 taskMap.put(classId, courseMap); 
			 }
			 HashMap<String, String> teaMap = new HashMap<String, String>();
			if(courseMap.containsKey(courseId)){
				teaMap = courseMap.get(courseId);
			}else{
				courseMap.put(courseId, teaMap);
			}
			if(!teaMap.containsKey(teacherId)){
				String actName = teaDic.get(teacherId).getName();
				teaMap.put(teacherId, actName);
			}
		}
		return taskMap;
	}

	
	/**
	 * 获取课表周次
	 * @param req
	 * @param res
	 * @param reqData
	 * @return
	 */
	@RequestMapping(value = "temporaryAdjust/getTimetableWeekList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getTimetableWeekList(HttpServletRequest req,
			HttpServletResponse res, @RequestBody JSONObject reqData) {
		String timetableId = reqData.getString("timetableId");
		String schoolId = getXxdm(req);
		JSONObject rs = new JSONObject();
		HashMap<String,Object> cxMap = new HashMap<String, Object>();
		cxMap.put("schoolId", schoolId);
		cxMap.put("timetableId", timetableId);
		JSONObject timetable = timetableService.getTimetableForWeekList(cxMap);
		String termInfoId = reqData.getString("selectedSemester");
		String DayOfStart = timetable.getString("DayOfStart");
		
		List<JSONObject> data = new ArrayList<JSONObject>();
		int code =1;
		try{
//			data = TimetableUtil.getWeekListByStartDate(DayOfStart,21);
			JSONObject weewObj =	TimetableUtil.getWeekListByStartDate(timetable.getString("DayOfStart"),21);
			if(weewObj.containsKey("code")&&weewObj.getIntValue("code")==1){
				data = (List<JSONObject>) weewObj.get("rslist");
			}else{
				code = -1;
				rs.put("msg", weewObj.getString("msg"));
			}		
		}catch(Exception e){
			code = -1;
			rs.put("msg", e.getMessage());
		}
		
		rs.put("code", code);
		rs.put("data", data);
		
		return rs;
	}
	
	/**
	 * 获取课表周次
	 * @param req
	 * @param res
	 * @param reqData
	 * @return
	 */
	@RequestMapping(value = "temporaryAdjust/forFineTuning", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject temporaryAjustForFineTuning(HttpServletRequest req,
			HttpServletResponse res, @RequestBody JSONObject reqData) {
		JSONObject rs = new JSONObject();
		
		String timetableId = reqData.getString("timetableId");
		String week = reqData.getString("week");
		//0的时候代表不跨周
		int weekOfEnd = 0;
		if(reqData.containsKey("weekOfEnd")&&reqData.getString("weekOfEnd").trim().length()>0){
			weekOfEnd = reqData.getInteger("weekOfEnd");
		}
		int breakRule = 0;
		if(reqData.containsKey("breakRule")&&reqData.getString("breakRule").trim().length()>0){
			breakRule = reqData.getInteger("breakRule");
		}
		
		String gradeId = reqData.getString("gradeId");
		String classId = reqData.getString("classId");
		String selectedSemester = reqData.getString("selectedSemester");
		School school =  getSchool (req,selectedSemester);
		
		String schoolId = getXxdm(req);
		
		HashMap<String,Object> cxMap = new HashMap<String, Object>();
		cxMap.put("schoolId", schoolId);
		cxMap.put("timetableId", timetableId);
		
		JSONObject data = new JSONObject();

		String code = "1";
		String msg = "";
		
		try {
			data = this.smartArrangeService.getTempAjustForFineTuningDataByClass(school,
					timetableId, gradeId, classId,   req
							.getSession().getId(),week,weekOfEnd,breakRule);
		} catch (Exception e) {
			code = "-1";
			msg = e.getMessage();
			e.printStackTrace();
		}

		this.setPromptMessage(rs , code, msg);
		rs.put("data", data);

		return rs;
	}
	
	/**
	 * 获取课表周次
	 * @param req
	 * @param res
	 * @param reqData
	 * @return
	 */
	@RequestMapping(value = "temporaryAdjust/forFineTuningByTeacher", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject temporaryAjustForFineTuningByTeacher(HttpServletRequest req,
			HttpServletResponse res, @RequestBody JSONObject reqData) {
		JSONObject rs = new JSONObject();
		
		String timetableId = reqData.getString("timetableId");
		String week = reqData.getString("week");
		//0的时候代表不跨周
		int weekOfEnd = 0;
		if(reqData.containsKey("weekOfEnd")&&reqData.getString("weekOfEnd").trim().length()>0){
			weekOfEnd = reqData.getInteger("weekOfEnd");
		}
		int breakRule = 0;
		if(reqData.containsKey("breakRule")&&reqData.getString("breakRule").trim().length()>0){
			breakRule = reqData.getInteger("breakRule");
		}
		String teacherId = reqData.getString("teacherId");
		String selectedSemester = reqData.getString("selectedSemester");
		School school =  getSchool (req,selectedSemester);
		
		
		String schoolId = getXxdm(req);
		
		HashMap<String,Object> cxMap = new HashMap<String, Object>();
		cxMap.put("schoolId", schoolId);
		cxMap.put("timetableId", timetableId);
		
		JSONObject data = new JSONObject();
		
		String code = "1";
		String msg = "";
		
		try {
			data = this.smartArrangeService.getTempAjustForFineTuningDataByTeacher(school,
					timetableId, teacherId,   req
					.getSession().getId(),week,weekOfEnd, breakRule);
		} catch (Exception e) {
			code = "-1";
			msg = e.getMessage();
			e.printStackTrace();
		}
		
		this.setPromptMessage(rs , code, msg);
		rs.put("data", data);
		
		return rs;
	}
	/**
	 * 对调临时课表
	 * @param req
	 * @param res
	 * @param reqData
	 * @return
	 */
	@RequestMapping(value = "temporaryAdjust/changeLessonPosition", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject temporaryAdjustChangeLessonPosition(HttpServletRequest req,
			HttpServletResponse res, @RequestBody JSONObject reqData) {
		JSONObject rs = new JSONObject();
		
		String timetableId = reqData.getString("timetableId");
		int week =Integer.parseInt( reqData.getString("weekOfStart").split("\\|")[0]);
		//0的时候代表不跨周
		int weekOfEnd = 0;
		if(reqData.containsKey("weekOfEnd")&&reqData.getString("weekOfEnd").trim().length()>0){
			weekOfEnd = reqData.getInteger("weekOfEnd");
		}
		String classId = reqData.getString("classId");
		String fromCourseIds = reqData.getString("fromCourseIds");
		String fromCourseTypes = reqData.getString("fromCourseTypes");
		String fromMcGroupId = reqData.getString("fromMcGroupId");
		Integer fromDayOfWeek = reqData.getInteger("fromDayOfWeek");
		Integer fromLessonOfDay = reqData.getInteger("fromLessonOfDay");
		
		String toCourseIds = reqData.getString("toCourseIds");
		String toCourseTypes = reqData.getString("toCourseTypes");
		String toMcGroupId = reqData.getString("toMcGroupId");
		Integer toDayOfWeek = reqData.getInteger("toDayOfWeek");
		Integer toLessonOfDay = reqData.getInteger("toLessonOfDay");
		String selectedSemester = reqData.getString("selectedSemester");
		School school =  getSchool (req,selectedSemester);
		String schoolId = getXxdm(req);
		
		HashMap<String,Object> cxMap = new HashMap<String, Object>();
		cxMap.put("schoolId", schoolId);
		cxMap.put("timetableId", timetableId);
		
		JSONObject data = new JSONObject();
		
		String code = "1";
		String msg = "";
		String groupId = "";
		String toGroupId = reqData.getString("toGroupId");
		String fromGroupId = reqData.getString("fromGroupId");
		if(fromGroupId!=null&&fromGroupId.trim().length()>0&&!fromGroupId.equals("-100")
				&&toGroupId!=null&&toGroupId.trim().length()>0&&!toGroupId.equals("-100")){
			if(!fromGroupId.equals(toGroupId)){
				code = "-1";
				msg = "不允许跨组调课";
				this.setPromptMessage(rs , code, msg);
				rs.put("data", data);
				
				return rs;
			}
		}
		if(fromGroupId!=null&&fromGroupId.trim().length()>0){
			groupId = fromGroupId;
		}
		if(toGroupId!=null&&toGroupId.trim().length()>0){
			groupId = toGroupId;
		}
		if(groupId.trim().length()==0){
			groupId = UUIDUtil.getUUID();
		}
		
		try {
			data = this.smartArrangeService.updateTemporaryLessonPosition(school,timetableId, classId,
					fromDayOfWeek,fromLessonOfDay,fromCourseIds,fromCourseTypes,fromMcGroupId,
					toDayOfWeek,toLessonOfDay,toCourseIds,toCourseTypes,toMcGroupId,
					  req.getSession().getId(),week,weekOfEnd,groupId);
		} catch (Exception e) {
			code = "-1";
			msg = e.getMessage();
			e.printStackTrace();
		}
		
		this.setPromptMessage(rs , code, msg);
		rs.put("data", data);
		
		return rs;
	}
	
	/**
	 * 按需清除缓存
	 * @param progDataMap2
	 * @param id
	 * @param timetableId
	 * @param keyName
	 */
	private void clearProgDataByKey(  String sessionID, String timetableId, String keyName) {
		// TODO Auto-generated method stub
		Object tbBaseKey = "timetable."+timetableId+".10.tbBaseDataCache";
		
		Object tbKey = "timetable."+timetableId+".10.tbDataCache";
		JSONObject tbData = null;
		try {
			tbData = (JSONObject) redisOperationDAO.get(tbKey);
			if(tbData!=null){
				if(keyName!=null){
					tbData.remove(keyName);
				}else{
					tbData = null;
				}
			}
			redisOperationDAO.set(tbKey, tbData,CacheExpireTime.sessionMinExpireTime.getTimeValue());
			if(keyName==null){
				redisOperationDAO.del(tbBaseKey);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	/**
	 * 按教师对调临时课表 
	 * @param req
	 * @param res
	 * @param reqData
	 * @return
	 */
	@RequestMapping(value = "temporaryAdjust/changeLessonPositionForTeacher", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject temporaryAdjustChangeLessonPositionForTeacher(HttpServletRequest req,
			HttpServletResponse res, @RequestBody JSONObject reqData) {
		JSONObject rs = new JSONObject();
		
		String timetableId = reqData.getString("timetableId");
		int week =Integer.parseInt( reqData.getString("weekOfStart").split("\\|")[0]);
		//0的时候代表不跨周
		int weekOfEnd = 0;
		if(reqData.containsKey("weekOfEnd")&&reqData.getString("weekOfEnd").trim().length()>0){
			weekOfEnd = reqData.getInteger("weekOfEnd");
		}

		JSONArray fromCourseArr = reqData.getJSONArray("fromCourseIds");
		JSONArray toCourseArr = reqData.getJSONArray("toCourseIds");
		
		Integer fromDayOfWeek = reqData.getInteger("fromDayOfWeek");
		Integer fromLessonOfDay = reqData.getInteger("fromLessonOfDay");
		
		Integer toDayOfWeek = reqData.getInteger("toDayOfWeek");
		Integer toLessonOfDay = reqData.getInteger("toLessonOfDay");
		
		String selectedSemester = reqData.getString("selectedSemester");
		School school =  getSchool (req,selectedSemester);
		
		
		String schoolId = getXxdm(req);
		
		HashMap<String,Object> cxMap = new HashMap<String, Object>();
		cxMap.put("schoolId", schoolId);
		cxMap.put("timetableId", timetableId);
		
		JSONObject data = new JSONObject();
		
		String code = "1";
		String msg = "";
		
		String groupId = "";
		String toGroupId = reqData.getString("toGroupId");
		String fromGroupId = reqData.getString("fromGroupId");
		if(fromGroupId!=null&&fromGroupId.trim().length()>0&&!fromGroupId.equals("-100")
				&&toGroupId!=null&&toGroupId.trim().length()>0&&!toGroupId.equals("-100")){
			if(!fromGroupId.equals(toGroupId)){
				code = "-1";
				msg = "不允许跨组调课";
				this.setPromptMessage(rs , code, msg);
				rs.put("data", data);
				
				return rs;
			}
		}
		if(fromGroupId!=null&&fromGroupId.trim().length()>0){
			groupId = fromGroupId;
		}
		if(toGroupId!=null&&toGroupId.trim().length()>0){
			groupId = toGroupId;
		}
		if(groupId.trim().length()==0){
			groupId = UUIDUtil.getUUID();
		}
		
		
		try {
			data = this.smartArrangeService.updateTemporaryLessonPositionByTeacher(school,timetableId,
					fromDayOfWeek,fromLessonOfDay,fromCourseArr,
					toDayOfWeek,toLessonOfDay,toCourseArr, req.getSession().getId()
					,week,weekOfEnd,groupId);
		} catch (Exception e) {
			code = "-1";
			msg = e.getMessage();
			e.printStackTrace();
		}
		
		this.setPromptMessage(rs , code, msg);
		rs.put("data", data);
		
		return rs;
	}
	
	
	/**
	 * 获取可代课老师
	 * @param req
	 * @param res
	 * @param reqData
	 * @return
	 */
	@RequestMapping(value = "temporaryAdjust/getCanAdjustTeacherList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject temporaryAdjustGetCanAdjustTeacherList(HttpServletRequest req,
			HttpServletResponse res, @RequestBody JSONObject reqData) {
		JSONObject rs = new JSONObject();
		//从教学任务中获取教师 且不冲突
		String timetableId = reqData.getString("timetableId");
		int week =Integer.parseInt( reqData.getString("weekOfStart").split("\\|")[0]);
		//0的时候代表不跨周
		int weekOfEnd = 0;
		if(reqData.containsKey("weekOfEnd")&&reqData.getString("weekOfEnd").trim().length()>0){
			weekOfEnd = reqData.getInteger("weekOfEnd");
		}
		int dayOfWeek = reqData.getIntValue("dayOfWeek");
		int lessonOfDay = reqData.getIntValue("lessonOfDay");
		
		JSONArray classCourse = reqData.getJSONArray("classCourse");
				
		String selectedSemester = reqData.getString("selectedSemester");
		School school =  getSchool (req,selectedSemester);
		
		String schoolId = getXxdm(req);
		
		HashMap<String,Object> cxMap = new HashMap<String, Object>();
		cxMap.put("schoolId", schoolId);
		cxMap.put("timetableId", timetableId);
		int breakRule = 0;
		if(reqData.containsKey("breakRule")&&reqData.getString("breakRule").trim().length()>0){
			breakRule = reqData.getInteger("breakRule");
		}
		
		try {
			rs = this.smartArrangeService.getCanAdjustTeacherList(school,timetableId, classCourse,
					  req.getSession().getId(),week,weekOfEnd,dayOfWeek,lessonOfDay,breakRule);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		return rs;
	}
	
	/**
	 * 设置可代课老师
	 * @param req
	 * @param res
	 * @param reqData
	 * @return
	 */
	@RequestMapping(value = "temporaryAdjust/updateAdjustTeacher", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject temporaryAdjustUpdateAdjustTeacher(HttpServletRequest req,
			HttpServletResponse res, @RequestBody JSONObject reqData) {
		JSONObject rs = new JSONObject();
		//从教学任务中获取教师 且不冲突
		String timetableId = reqData.getString("timetableId");
		int week =Integer.parseInt( reqData.getString("weekOfStart").split("\\|")[0]);
		//0的时候代表不跨周
		int weekOfEnd = 0;
		if(reqData.containsKey("weekOfEnd")&&reqData.getString("weekOfEnd").trim().length()>0){
			weekOfEnd = reqData.getInteger("weekOfEnd");
		}
		int dayOfWeek = reqData.getIntValue("dayOfWeek");
		int lessonOfDay = reqData.getIntValue("lessonOfDay");
		
		JSONArray classCourse = reqData.getJSONArray("classCourse");
		
		String fromTeachers = reqData.getString("fromTeachers");
		String toTeachers = reqData.getString("toTeachers");
		
		String selectedSemester = reqData.getString("selectedSemester");
		School school =  getSchool (req,selectedSemester);
		
		String schoolId = getXxdm(req);
		
		HashMap<String,Object> cxMap = new HashMap<String, Object>();
		cxMap.put("schoolId", schoolId);
		cxMap.put("timetableId", timetableId);
		
		JSONObject data = new JSONObject();
		
		String code = "1";
		String msg = "";
		
		String groupId =  reqData.getString("groupId");
		if(groupId==null||groupId.trim().length()==0){
			groupId = UUIDUtil.getUUID();
		}
		
		try {
			data = this.smartArrangeService.updateAdjustBySet(school,timetableId, classCourse,
					  req.getSession().getId(),week,weekOfEnd,dayOfWeek,lessonOfDay,
					fromTeachers,toTeachers,"2",null,groupId);
		} catch (Exception e) {
			code = "-1";
			msg = e.getMessage();
			e.printStackTrace();
		}
		
		this.setPromptMessage(rs , code, msg);
		rs.put("data", data);
		
		return rs;
	}
	
	/**
	 * 获取自习
	 * @param req
	 * @param res
	 * @param reqData
	 * @return
	 */
	@RequestMapping(value = "temporaryAdjust/updateAdjustSelfStudy", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject temporaryAdjustUpdateAdjustSelfStudy(HttpServletRequest req,
			HttpServletResponse res, @RequestBody JSONObject reqData) {
		JSONObject rs = new JSONObject();
		//从教学任务中获取教师 且不冲突
		String timetableId = reqData.getString("timetableId");
		int week =Integer.parseInt( reqData.getString("weekOfStart").split("\\|")[0]);
		//0的时候代表不跨周
		int weekOfEnd = 0;
		if(reqData.containsKey("weekOfEnd")&&reqData.getString("weekOfEnd").trim().length()>0){
			weekOfEnd = reqData.getInteger("weekOfEnd");
		}
		int dayOfWeek = reqData.getIntValue("dayOfWeek");
		int lessonOfDay = reqData.getIntValue("lessonOfDay");
		String termInfoId = reqData.getString("selectedSemester");
		JSONArray classCourse = reqData.getJSONArray("classCourse");

		String selectedSemester = reqData.getString("selectedSemester");
		School school =  getSchool (req,selectedSemester);
		
		String schoolId = getXxdm(req);
		
		HashMap<String,Object> cxMap = new HashMap<String, Object>();
		cxMap.put("schoolId", schoolId);
		cxMap.put("timetableId", timetableId);
		
		JSONObject data = new JSONObject();
		
		String code = "1";
		String msg = "";
		
		String groupId =  reqData.getString("groupId");
		if(groupId==null||groupId.trim().length()==0){
			groupId = UUIDUtil.getUUID();
		}
		try {
			String selfStuRs = getSelfStudyCodeBySchool(school,termInfoId);
			String selfStuCode = selfStuRs.split("\\|")[0];
			String selfStuSimpleName = selfStuRs.split("\\|")[1];
			data = this.smartArrangeService.updateAdjustBySet(school,timetableId, classCourse,
					 req.getSession().getId(),week,weekOfEnd,dayOfWeek,lessonOfDay,
					null,null,"3",selfStuCode,groupId);
			rs.put("courseId", selfStuCode);
			rs.put("courseSimpleName", selfStuSimpleName);
		} catch (Exception e) {
			code = "-1";
			msg = e.getMessage();
			e.printStackTrace();
		}
		
//		this.setPromptMessage(rs , code, msg);
		rs.put("data", data);
		rs.put("code", code);
		rs.put("msg", msg);
		return rs;
	}
	private String getSelfStudyCodeBySchool(School school,String termInfoId) {
		// TODO Auto-generated method stub
		List<LessonInfo> less = commonDataService.getLessonInfoList(school,termInfoId);
		String code = "";
		for(LessonInfo les:less){
			String lesName = les.getName();
			if(lesName.equalsIgnoreCase("自习") ){
				code = les.getId()+"|"+(les.getSimpleName()!=null?les.getSimpleName():les.getName().substring(0,1));
			}
		}
		if(code.length()==0){
			throw new RuntimeException("学校未开设【自习】科目，请在基础数据中添加【自习】科目！");
		}
		return code;
	}

	/**
	 * 获取自习
	 * @param req
	 * @param res
	 * @param reqData
	 * @return
	 */
	@RequestMapping(value = "temporaryAdjust/updateAdjustOpenCourses", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject temporaryAdjustUpdateAdjustOpenCourses(HttpServletRequest req,
			HttpServletResponse res, @RequestBody JSONObject reqData) {
		JSONObject rs = new JSONObject();
		//从教学任务中获取教师 且不冲突
		String timetableId = reqData.getString("timetableId");
		int week =Integer.parseInt( reqData.getString("weekOfStart").split("\\|")[0]);
		//0的时候代表不跨周
		int weekOfEnd = 0;
		if(reqData.containsKey("weekOfEnd")&&reqData.getString("weekOfEnd").trim().length()>0){
			weekOfEnd = reqData.getInteger("weekOfEnd");
		}
		int dayOfWeek = reqData.getIntValue("dayOfWeek");
		int lessonOfDay = reqData.getIntValue("lessonOfDay");
		
		JSONArray classCourse = reqData.getJSONArray("classCourse");
		String selectedSemester = reqData.getString("selectedSemester");
		School school =  getSchool (req,selectedSemester);
		
		String schoolId = getXxdm(req);
		
		HashMap<String,Object> cxMap = new HashMap<String, Object>();
		cxMap.put("schoolId", schoolId);
		cxMap.put("timetableId", timetableId);

		String code = "1";
		String msg = "";
		
		String groupId =  reqData.getString("groupId");
		if(groupId==null||groupId.trim().length()==0){
			groupId = UUIDUtil.getUUID();
		}
		try {
			rs = this.smartArrangeService.updateAdjustBySet(school,timetableId, classCourse,
					  req.getSession().getId(),week,weekOfEnd,dayOfWeek,lessonOfDay,
					null,null,"4",null,groupId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		return rs;
	}
	
	/**
	 * 撤销操作
	 * @param req
	 * @param res
	 * @param reqData
	 * @return
	 */
	@RequestMapping(value = "temporaryAdjust/delAdjust", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject temporaryAdjustUpdateAdjustDelAdjust(HttpServletRequest req,
			HttpServletResponse res, @RequestBody JSONObject reqData) {
		JSONObject rs = new JSONObject();
		String timetableId = reqData.getString("timetableId");
		int week =Integer.parseInt( reqData.getString("weekOfStart").split("\\|")[0]);
		//0的时候代表不跨周	-- 
		int weekOfEnd = 0;
		if(reqData.containsKey("weekOfEnd")&&reqData.getString("weekOfEnd").trim().length()>0){
			weekOfEnd = reqData.getInteger("weekOfEnd");
		}
		int dayOfWeek = reqData.getIntValue("dayOfWeek");
		int lessonOfDay = reqData.getIntValue("lessonOfDay");
		String termInfoId = reqData.getString("selectedSemester");
		JSONArray classCourse = reqData.getJSONArray("classCourse");
		String selectedSemester = reqData.getString("selectedSemester");
		School school =  getSchool (req,selectedSemester);
		
		String schoolId = getXxdm(req);
		
		HashMap<String,Object> cxMap = new HashMap<String, Object>();
		cxMap.put("schoolId", schoolId);
		cxMap.put("timetableId", timetableId);
		
		String groupId =  reqData.getString("groupId");
		if(groupId==null||groupId.trim().length()==0){
			groupId = UUIDUtil.getUUID();
		}
		
		try {
			String selfStuRs = getSelfStudyCodeBySchool(school, termInfoId);
			String selfStuCode = selfStuRs.split("\\|")[0];
			rs = this.smartArrangeService.updateAdjustByDelAdjust(school,timetableId, classCourse,
					  req.getSession().getId(),week,weekOfEnd,dayOfWeek,lessonOfDay, selfStuCode,groupId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		return rs;
	}
	
	/**
	 * 获取周次课表 年级-不计冲突
	 * @param req
	 * @param res
	 * @param reqData
	 * @return
	 */
	@RequestMapping(value = "temporaryAdjust/forFineTuningByGrade", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject temporaryAjustForFineTuningByGrade(HttpServletRequest req,
			HttpServletResponse res, @RequestBody JSONObject reqData) {
		JSONObject rs = new JSONObject();
		
		String timetableId = reqData.getString("timetableId");
		String week = reqData.getString("week");
		//0的时候代表不跨周
		int weekOfEnd = 0;
		if(reqData.containsKey("weekOfEnd")&&reqData.getString("weekOfEnd").trim().length()>0){
			weekOfEnd = reqData.getInteger("weekOfEnd");
		}
		String gradeId = reqData.getString("gradeId");
		String selectedSemester = reqData.getString("selectedSemester");
		School school =  getSchool (req,selectedSemester);
		String code = "0";
		String msg = "";
		JSONObject data = new JSONObject();
		
		String schoolId = getXxdm(req);
		
		HashMap<String,Object> cxMap = new HashMap<String, Object>();
		cxMap.put("schoolId", schoolId);
		cxMap.put("timetableId", timetableId);
		
		
		try {
			data = this.smartArrangeService.getTempAjustForFineTuningDataByGrade(school,
					timetableId, gradeId,   req
							.getSession().getId(),week,weekOfEnd);
		} catch (Exception e) {
			code = "-1";
			msg = "查询失败";
			e.printStackTrace();
		}


		rs.put("data", data);
		rs.put("code", code);
		rs.put("msg", msg);
		return rs;
	}

	/**
	 * 获取单课眼冲突-年级临时调课
	 * @param req
	 * @param res
	 * @param reqData
	 * @return
	 */
	@RequestMapping(value = "temporaryAdjust/positionConflicts", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject temporaryAjustPositionConflicts(HttpServletRequest req,
			HttpServletResponse res, @RequestBody JSONObject reqData) {
		JSONObject rs = new JSONObject();
		
		String timetableId = reqData.getString("timetableId");
		String week = reqData.getString("week");
		//0的时候代表不跨周
		int weekOfEnd = 0;
		if(reqData.containsKey("weekOfEnd")&&reqData.getString("weekOfEnd").trim().length()>0){
			weekOfEnd = reqData.getInteger("weekOfEnd");
		}
		int breakRule = 0;
		if(reqData.containsKey("breakRule")&&reqData.getString("breakRule").trim().length()>0){
			breakRule = reqData.getInteger("breakRule");
		}
		int dayOfWeek = reqData.getIntValue("dayOfWeek");
		int lessonOfDay = reqData.getIntValue("lessonOfDay");
		String gradeId = reqData.getString("gradeId");
		String classId = reqData.getString("classId");
		String courseIds = reqData.getString("courseIds");
		String selectedSemester = reqData.getString("selectedSemester");
		School school =  getSchool (req,selectedSemester);
		
		
		String schoolId = getXxdm(req);
		
		HashMap<String,Object> cxMap = new HashMap<String, Object>();
		cxMap.put("schoolId", schoolId);
		cxMap.put("timetableId", timetableId);
		
		String mcGroupIs = "";
		if(reqData.containsKey("McGroupIds")){
			mcGroupIs = reqData.getString("McGroupIds");
		}
		
		try {
			rs = this.smartArrangeService.getTempAjustPositionConflicts(school,
					timetableId,dayOfWeek,lessonOfDay, gradeId, classId,courseIds , req
							.getSession().getId(),week,weekOfEnd, mcGroupIs,breakRule);
		} catch (Exception e) {
			e.printStackTrace();
		}


		return rs;
	}
	/**
	 * 获取班级调课记录
	 * @param req
	 * @param res
	 * @param reqData
	 * @return
	 */
	@RequestMapping(value = "temporaryAdjust/getAdjustRecordForClassList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getAdjustRecordForClassList(HttpServletRequest req,
			HttpServletResponse res, @RequestBody JSONObject reqData) {
		JSONObject rs = new JSONObject();
		
		String timetableId = reqData.getString("timetableId");
		String week = reqData.getString("week");
		//0的时候代表不跨周
		String selectedSemester = reqData.getString("selectedSemester");
		School school =  getSchool (req,selectedSemester);
		
		
		String schoolId = getXxdm(req);
		
		HashMap<String,Object> cxMap = new HashMap<String, Object>();
		cxMap.put("schoolId", schoolId);
		cxMap.put("timetableId", timetableId);
		
		try {
			rs = this.smartArrangeService.getAdjustRecordForClassList(school,
					timetableId,  req
					.getSession().getId(),week,1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		return rs;
	}
	
	/**
	 * 获取教师发布记录
	 * @param req
	 * @param res
	 * @param reqData
	 * @return
	 */
	@RequestMapping(value = "temporaryAdjust/getAdjustRecordForTeacherList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getAdjustRecordForTeacherList(HttpServletRequest req,
			HttpServletResponse res, @RequestBody JSONObject reqData) {
		JSONObject rs = new JSONObject();
		
		String timetableId = reqData.getString("timetableId");
		String week = reqData.getString("week");
		//0的时候代表不跨周
		String selectedSemester = reqData.getString("selectedSemester");
		School school =  getSchool (req,selectedSemester);
		
		
		String schoolId = getXxdm(req);
		
		HashMap<String,Object> cxMap = new HashMap<String, Object>();
		cxMap.put("schoolId", schoolId);
		cxMap.put("timetableId", timetableId);
		
		
		try {
			rs = this.smartArrangeService.getAdjustRecordForClassList(school,
					timetableId,  req
					.getSession().getId(),week,2);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return rs;
	}
	/**
	 * 发布调课记录
	 * @param req
	 * @param res
	 * @param reqData
	 * @return
	 */
	@RequestMapping(value = "temporaryAdjust/updateAdjustRecordPublished", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject updateAdjustRecordPublished(HttpServletRequest req,
			HttpServletResponse res, @RequestBody JSONObject reqData) {
		JSONObject rs = new JSONObject();
		
		String timetableId = reqData.getString("timetableId");
		String week = reqData.getString("week");
		JSONArray courses = reqData.getJSONArray("data");
		//0的时候代表不跨周
		String selectedSemester = reqData.getString("selectedSemester");
		School school =  getSchool (req,selectedSemester);
		
		
		String schoolId = getXxdm(req);
		
		HashMap<String,Object> cxMap = new HashMap<String, Object>();
		cxMap.put("schoolId", schoolId);
		cxMap.put("timetableId", timetableId);
		
		
		try {
			rs = this.smartArrangeService.updateAdjustRecordPublished(school,
					timetableId,week,courses,req.getSession());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		return rs;
	}
	

	/** -----清除缓存----- */
	@RequestMapping(value = "perm/clearTimeTableCache", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject clearTimeTableCache(HttpServletRequest req, @RequestBody JSONObject request) {
		//清除缓存 
		JSONObject obj = new JSONObject();
		String timetableId = request.getString("timetableId");
		if(timetableId!=null){
			clearProgDataByKey( req.getSession().getId(),timetableId,null);
		}
		
		obj.put("code", 1);
		
		return obj;
	}
}