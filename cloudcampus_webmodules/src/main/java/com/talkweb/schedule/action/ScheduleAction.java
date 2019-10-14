package com.talkweb.schedule.action;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.School;
import com.talkweb.accountcenter.thrift.T_Role;
import com.talkweb.accountcenter.thrift.User;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.schedule.utils.ExcelTool;
import com.talkweb.utils.HttpClientUtil;
import com.talkweb.utils.JSONUtil;
import com.talkweb.utils.SplitUtil;

@RequestMapping("/schedule")
@RestController
public class ScheduleAction {
	@Autowired
	private AllCommonDataService allCommonDataService;
    @Autowired  
    private HttpServletRequest request;

    public static String rootPath = SplitUtil.getRootPath("schedule.url");

	@RequestMapping("/lookup/getScheduleList")
	@ResponseBody
	public JSONObject getScheduleList(@RequestBody JSONObject param){
		return postAction(param, "lookup/getScheduleList");
	}

	@RequestMapping("/lookup/addSchedule")
	@ResponseBody
	public JSONObject addSchedule(@RequestBody JSONObject param){
		return postAction(param, "lookup/addSchedule");
	}

	@RequestMapping("/lookup/deleteSchedule")
	@ResponseBody
	public JSONObject deleteSchedule(@RequestBody JSONObject param){
		return postAction(param, "lookup/deleteSchedule");
	}

	@RequestMapping("/lookup/updateSchedule")
	@ResponseBody
	public JSONObject updateSchedule(@RequestBody JSONObject param){
		return postAction(param, "lookup/updateSchedule");
	}

	@RequestMapping("/lookup/updateSchedulePublished")
	@ResponseBody
	public JSONObject updateSchedulePublished(@RequestBody JSONObject param){
		return postAction(param, "lookup/updateSchedulePublished");
	}

	@RequestMapping("/lookup/getTimetablePrintSet")
	@ResponseBody
	public JSONObject getTimetablePrintSet(@RequestBody JSONObject param){
		return postAction(param, "lookup/getTimetablePrintSet");
	}

	@RequestMapping("/lookup/updateTimetablePrintSet")
	@ResponseBody
	public JSONObject updateTimetablePrintSet(@RequestBody JSONObject param){
		return postAction(param, "lookup/updateTimetablePrintSet");
	}

	@RequestMapping("/lookup/getStuScheduleList")
	@ResponseBody
	public JSONObject getStuScheduleList(HttpServletRequest req, @RequestBody JSONObject param){
		try {
			JSONObject data = checkStudent(param);
			if (data != null) {
				return data;
			}
			return getScheduleList(param);
		} catch(Exception ex) {
			return JSONUtil.getResponse(ex, -1, "获取学生课表异常。");
		}
	}

	@RequestMapping("/lookup/getStudentSelfTimetable")
	@ResponseBody
	public JSONObject getStudentSelfTimetable(@RequestBody JSONObject param){
		try {
			JSONObject data = checkStudent(param);
			if (data != null) {
				return data;
			}
			JSONObject ret = postAction(param, "lookup/getTimetable");
			setReportData(ret);
			return ret;
		} catch(Exception ex) {
			return JSONUtil.getResponse(ex, -1, "获取学生课表异常。");
		}
	}

	@RequestMapping("/lookup/getGradeTimetable")
	@ResponseBody
	public JSONObject getGradeTimetable(@RequestBody JSONObject param){
		this.request.getSession().removeAttribute("schedulereport");
		JSONObject ret = postAction(param, "lookup/getGradeTimetable");
		setReportData(ret);
		return ret;
	}

	@RequestMapping("/lookup/getClassroomTimetable")
	@ResponseBody
	public JSONObject getClassroomTimetable(@RequestBody JSONObject param){
		this.request.getSession().removeAttribute("schedulereport");
		JSONObject ret = postAction(param, "lookup/getClassroomTimetable");
		setReportData(ret);
		return ret;
	}

	@RequestMapping("/lookup/getTeacherTimetable")
	@ResponseBody
	public JSONObject getTeacherTimetable(@RequestBody JSONObject param){
		this.request.getSession().removeAttribute("schedulereport");
		JSONObject ret = postAction(param, "lookup/getTeacherTimetable");
		setReportData(ret);
		return ret;
	}

	@RequestMapping("/lookup/getStudentTimetable")
	@ResponseBody
	public JSONObject getStudentTimetable(@RequestBody JSONObject param){
		this.request.getSession().removeAttribute("schedulereport");
		JSONObject ret = postAction(param, "lookup/getStudentTimetable");
		setReportData(ret);
		return ret;
	}

	@RequestMapping("/lookup/getStudentPlacementInfo")
	@ResponseBody
	public JSONObject getStudentPlacementInfo(@RequestBody JSONObject param){
		this.request.getSession().removeAttribute("schedulereport");
		JSONObject ret = postAction(param, "lookup/getStudentPlacementInfo");
		setReportData(ret);
		return ret;
	}

	@RequestMapping("/lookup/teacherLessonsStatistics")
	@ResponseBody
	public JSONObject teacherLessonsStatistics(@RequestBody JSONObject param){
		this.request.getSession().removeAttribute("teacherLessonsStatsData");
		JSONObject ret = postAction(param, "lookup/teacherLessonsStatistics");
		Object obj = ret.get("data");
		this.request.getSession().setAttribute("teacherLessonsStatsData", obj);
		return ret;
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "teacherLessonsStatsReport", method = RequestMethod.GET)
	@ResponseBody
	public void teacherLessonsStatsReport(HttpServletRequest req,HttpServletResponse res) throws Exception{
		String termInfoId = req.getParameter("termInfoId");
		List<Object> data  = null;
		if(req.getSession().getAttribute("teacherLessonsStatsData")!=null){
			data = (List<Object>) req.getSession().getAttribute("teacherLessonsStatsData");
		}else{
			throw new Exception("无可查数据");
		};

		JSONArray excelHead = JSON.parseArray("[[{field:'teacherName',title:'教师姓名',width:80, align:'center',sortable:true},"
	                + "{field:'totalLessons',title:'总课时',width:80, align:'center',sortable:true},"
	                + "{field:'gradeName',title:'年级',width:80, align:'center',sortable:true},"
	                + "{field:'subjectName',title:'科目',width:80,align:'center',sortable:true},"
	                + "{field:'className',title:'班级名称',width:80,align:'center',sortable:true},"
	                + "{field:'lessons',title:'任教课时',width:80,align:'center',sortable:true}]"
	                + "]");
		String[] needMerg = {"teacherName","totalLessons","gradeName","subjectName"};
	    ExcelTool.exportExcelWithData(new JSONArray(data), excelHead,
	    		getTerminfoName(termInfoId)+"课时统计", needMerg, req, res);
		return ;	
	}

	@RequestMapping("/courseArrange/getTimetableSection")
	@ResponseBody
	public JSONObject getTimetableSection(@RequestBody JSONObject param){
		return postAction(param, "courseArrange/getTimetableSection");
	}

	@RequestMapping("/courseArrange/getGradePlacementId")
	@ResponseBody
	public JSONObject getGradePlacementId(@RequestBody JSONObject param){
		return postAction(param, "courseArrange/getGradePlacementId");
	}

	@RequestMapping("/courseArrange/updateTimetableSection")
	@ResponseBody
	public JSONObject updateTimetableSection(@RequestBody JSONObject param){
		return postAction(param, "courseArrange/updateTimetableSection");
	}

	@RequestMapping("/courseArrange/generateTclass")
	@ResponseBody
	public JSONObject generateTclass(@RequestBody JSONObject param){
		return postAction(param, "courseArrange/generateTclass");
	}

	@RequestMapping("/courseArrange/getTClassInfo")
	@ResponseBody
	public JSONObject getTClassInfo(@RequestBody JSONObject param){
		return postAction(param, "courseArrange/getTClassInfo");
	}

	@RequestMapping("/courseArrange/delTclass")
	@ResponseBody
	public JSONObject delTclass(@RequestBody JSONObject param){
		return postAction(param, "courseArrange/delTclass");
	}
	
	@RequestMapping("/courseArrange/getTeachingTask")
	@ResponseBody
	public JSONObject getTeachingTask(@RequestBody JSONObject param){
		return postAction(param, "courseArrange/getTeachingTask");
	}
	
	@RequestMapping("/courseArrange/getTaskTeacher")
	@ResponseBody
	public JSONObject getTaskTeacher(@RequestBody JSONObject param){
		return postAction(param, "courseArrange/getTaskTeacher");
	}

	@RequestMapping("/courseArrange/updateTeachingTask")
	@ResponseBody
	public JSONObject updateTeachingTask(@RequestBody JSONObject param){
		return postAction(param, "courseArrange/updateTeachingTask");
	}

	@RequestMapping("/courseArrange/deleteTeachingTask")
	@ResponseBody
	public JSONObject deleteTeachingTask(@RequestBody JSONObject param){
		return postAction(param, "courseArrange/deleteTeachingTask");
	}
	
	@RequestMapping("/courseArrange/getCourseRule")
	@ResponseBody
	public JSONObject getCourseRule(@RequestBody JSONObject param){
		return postAction(param, "courseArrange/getCourseRule");
	}
	
	@RequestMapping("/courseArrange/updateCourseRule")
	@ResponseBody
	public JSONObject updateCourseRule(@RequestBody JSONObject param){
		return postAction(param, "courseArrange/updateCourseRule");
	}
	
	@RequestMapping("/courseArrange/deleteCourseRule")
	@ResponseBody
	public JSONObject deleteCourseRule(@RequestBody JSONObject param){
		return postAction(param, "courseArrange/deleteCourseRule");
	}

	@RequestMapping("/courseArrange/getTeachingTaskCourse")
	@ResponseBody
	public JSONObject getTeachingTaskCourse(@RequestBody JSONObject param){
		return postAction(param, "courseArrange/getTeachingTaskCourse");
	}
	
	@RequestMapping("/courseArrange/getTaskCourse")
	@ResponseBody
	public JSONObject getTaskCourse(@RequestBody JSONObject param){
		return postAction(param, "courseArrange/getTaskCourse");
	}

	@RequestMapping("/courseArrange/getCourseWalkthrough")
	@ResponseBody
	public JSONObject getCourseWalkthrough(@RequestBody JSONObject param){
		return postAction(param, "courseArrange/getCourseWalkthrough");
	}

	@RequestMapping("/courseArrange/getCourseWalkthroughConflicts")
	@ResponseBody
	public JSONObject getCourseWalkthroughConflicts(@RequestBody JSONObject param){
		return postAction(param, "courseArrange/getCourseWalkthroughConflicts");
	}
	
	@RequestMapping("/courseArrange/updateCourseWalkthrough")
	@ResponseBody
	public JSONObject updateCourseWalkthrough(@RequestBody JSONObject param){
		return postAction(param, "courseArrange/updateCourseWalkthrough");
	}

	@RequestMapping("/courseArrange/deleteCourseWalkthrough")
	@ResponseBody
	public JSONObject deleteCourseWalkthrough(@RequestBody JSONObject param){
		return postAction(param, "courseArrange/deleteCourseWalkthrough");
	}

	@RequestMapping("/courseArrange/getTeacherRule")
	@ResponseBody
	public JSONObject getTeacherRule(@RequestBody JSONObject param){
		return postAction(param, "courseArrange/getTeacherRule");
	}

	@RequestMapping("/courseArrange/saveTeacherGroup")
	@ResponseBody
	public JSONObject saveTeacherGroup(@RequestBody JSONObject param){
		return postAction(param, "courseArrange/saveTeacherGroup");
	}

	@RequestMapping("/courseArrange/delTeacherGroup")
	@ResponseBody
	public JSONObject delTeacherGroup(@RequestBody JSONObject param){
		return postAction(param, "courseArrange/delTeacherGroup");
	}

	@RequestMapping("/courseArrange/updateTeacherRule")
	@ResponseBody
	public JSONObject updateTeacherRule(@RequestBody JSONObject param){
		return postAction(param, "courseArrange/updateTeacherRule");
	}

	@RequestMapping("/courseArrange/deleteTeacher")
	@ResponseBody
	public JSONObject deleteTeacher(@RequestBody JSONObject param){
		return postAction(param, "courseArrange/deleteTeacher");
	}

	@RequestMapping("/courseArrange/getClassGroup")
	@ResponseBody
	public JSONObject getClassGroup(@RequestBody JSONObject param){
		return postAction(param, "courseArrange/getClassGroup");
	}

	@RequestMapping("/courseArrange/addClassGroup")
	@ResponseBody
	public JSONObject addClassGroup(@RequestBody JSONObject param){
		return postAction(param, "courseArrange/addClassGroup");
	}

	@RequestMapping("/courseArrange/deleteClass")
	@ResponseBody
	public JSONObject deleteClass(@RequestBody JSONObject param){
		return postAction(param, "courseArrange/deleteClass");
	}

	@RequestMapping("/courseArrange/deleteClassGroup")
	@ResponseBody
	public JSONObject deleteClassGroup(@RequestBody JSONObject param){
		return postAction(param, "courseArrange/deleteClassGroup");
	}

	@RequestMapping("/courseArrange/getMonoWeek")
	@ResponseBody
	public JSONObject getMonoWeek(@RequestBody JSONObject param){
		return postAction(param, "courseArrange/getMonoWeek");
	}

	@RequestMapping("/courseArrange/addMonoWeek")
	@ResponseBody
	public JSONObject addMonoWeek(@RequestBody JSONObject param){
		return postAction(param, "courseArrange/addMonoWeek");
	}

	@RequestMapping("/courseArrange/deleteMonoWeek")
	@ResponseBody
	public JSONObject deleteMonoWeek(@RequestBody JSONObject param){
		return postAction(param, "courseArrange/deleteMonoWeek");
	}

	@RequestMapping("/courseArrange/getGradeCrossPosSet")
	@ResponseBody
	public JSONObject getGradeCrossPosSet(@RequestBody JSONObject param){
		return postAction(param, "courseArrange/getGradeCrossPosSet");
	}

	@RequestMapping("/courseArrange/saveGradeCrossPosSet")
	@ResponseBody
	public JSONObject saveGradeCrossPosSet(@RequestBody JSONObject param){
		return postAction(param, "courseArrange/saveGradeCrossPosSet");
	}
	
	@RequestMapping("/courseArrange/getIntelligentCourseInfo")
	@ResponseBody
	public JSONObject getIntelligentCourseInfo(@RequestBody JSONObject param){
		return postAction(param, "courseArrange/getIntelligentCourseInfo");
	}
	
	@RequestMapping("/courseArrange/startGenerate")
	@ResponseBody
	public JSONObject startGenerate(@RequestBody JSONObject param){
		param.put("sessionId", request.getSession().getId());
		return postAction(param, "courseArrange/startGenerate");
	}
	
	@RequestMapping("/courseArrange/getArrangeCourseProgress")
	@ResponseBody
	public JSONObject getArrangeCourseProgress(@RequestBody JSONObject param) {
		param.put("sessionId", request.getSession().getId());
		return postAction(param, "courseArrange/getArrangeCourseProgress");
	}

	@RequestMapping("/importTeachingTask/importTeachingTask")
	@ResponseBody
	public JSONObject importTeachingTask(HttpServletRequest req, 
			@RequestParam("importFile") MultipartFile file) {
		req.getSession().setAttribute("scheduleId", req.getParameter("scheduleId"));//缓存scheduleid
		req.getSession().setAttribute("gradeId", req.getParameter("gradeId"));
		JSONObject param = new JSONObject();
		param.put("scheduleId", req.getParameter("scheduleId"));
		param.put("termInfoId", req.getParameter("termInfoId"));
		param.put("gradeId", req.getParameter("gradeId"));
		param.put("sessionId", req.getSession().getId());
		param.put("schoolId", request.getSession().getAttribute("xxdm"));
		return HttpClientUtil.postFile(rootPath + "importTeachingTask/importTeachingTask", file, "importFile", param);
	}
	
	@RequestMapping("/importTeachingTask/getExcelMatch")
	@ResponseBody
	public JSONObject getExcelMatch(HttpServletRequest req) {
		JSONObject param = new JSONObject();
		initScheduleParameter(req, param);
		return postAction(param, "importTeachingTask/getExcelMatch");
	}

	@RequestMapping("/importTeachingTask/startImportTask")
	@ResponseBody
	public JSONObject startImportTask(HttpServletRequest req, @RequestBody JSONObject param) {
		initScheduleParameter(req, param);
		return postAction(param, "importTeachingTask/startImportTask");
	}

	@RequestMapping("/importTeachingTask/singleDataCheck")
	@ResponseBody
	public JSONObject singleDataCheck(HttpServletRequest req, @RequestBody JSONObject param) {
		initScheduleParameter(req, param);
		return postAction(param, "importTeachingTask/singleDataCheck");
	}

	@RequestMapping("/importTeachingTask/continueImport")
	@ResponseBody
	public JSONObject continueImport(HttpServletRequest req) {
		JSONObject param = new JSONObject();
		initScheduleParameter(req, param);
		return postAction(param, "importTeachingTask/continueImport");
	}

	@RequestMapping("/importTeachingTask/importProgress")
	@ResponseBody
	public JSONObject importProgress(HttpServletRequest req) {
		JSONObject param = new JSONObject();
		initScheduleParameter(req, param);
		return postAction(param, "importTeachingTask/importProgress");
	}

	@RequestMapping("/app/getTeacherScheduleList")
	@ResponseBody
	public JSONObject getTeacherScheduleList(@RequestBody JSONObject param) {
		JSONObject data = checkTeacher(param);
		if (data != null) {
			return data;
		}
		return getScheduleList(param);
	}

	@RequestMapping("/app/getTeacherTimetableByDay")
	@ResponseBody
	public JSONObject getTeacherTimetableByDay(@RequestBody JSONObject param) {
		try {
			JSONObject data = checkTeacher(param);
			if (data != null) {
				return data;
			}
			return postAction(param, "app/getTimetableForApp");
		} catch(Exception ex) {
			return JSONUtil.getResponse(ex, -1, "获取教师课表异常。");
		}
	}

	@RequestMapping("/app/getStudentTimetableByDay")
	@ResponseBody
	public JSONObject getStudentTimetableByDay(@RequestBody JSONObject param) {
		try {
			JSONObject data = checkStudent(param);
			if (data != null) {
				return data;
			}
			return postAction(param, "app/getTimetableForApp");
		} catch(Exception ex) {
			return JSONUtil.getResponse(ex, -1, "获取学生课表异常。");
		}
	}

	@RequestMapping("/adjustment/forFineTuning")
	@ResponseBody
	public JSONObject forFineTuning(@RequestBody JSONObject param) {
		return postAction(param, "adjustment/forFineTuning");
	}

	@RequestMapping("/adjustment/groundPositionConflicts")
	@ResponseBody
	public JSONObject groundPositionConflicts(@RequestBody JSONObject param) {
		return postAction(param, "adjustment/groundPositionConflicts");
	}

	@RequestMapping("/adjustment/changeLessonPosition")
	@ResponseBody
	public JSONObject changeLessonPosition(@RequestBody JSONObject param) {
		return postAction(param, "adjustment/changeLessonPosition");
	}

	@RequestMapping("/adjustment/deleteLessonPosition")
	@ResponseBody
	public JSONObject deleteLessonPosition(@RequestBody JSONObject param) {
		return postAction(param, "adjustment/deleteLessonPosition");
	}

	@RequestMapping("/adjustment/forFineTuningByTimepoint")
	@ResponseBody
	public JSONObject forFineTuningByTimepoint(@RequestBody JSONObject param) {
		return postAction(param, "adjustment/forFineTuningByTimepoint");
	}

	@RequestMapping("/adjustment/timepointPositionConflicts")
	@ResponseBody
	public JSONObject timepointPositionConflicts(@RequestBody JSONObject param) {
		return postAction(param, "adjustment/timepointPositionConflicts");
	}

	@RequestMapping("/adjustment/changeLessonPositionForTimepoint")
	@ResponseBody
	public JSONObject changeLessonPositionForTimepoint(@RequestBody JSONObject param) {
		return postAction(param, "adjustment/changeLessonPositionForTimepoint");
	}

	@RequestMapping("/adjustment/isSwitchCached")
	@ResponseBody
	public JSONObject isSwitchCached(@RequestBody JSONObject param) {
		return JSONUtil.getResponse(1);
	}
	
	@RequestMapping("/adjustment/isStudentCached")
	@ResponseBody
	public JSONObject isStudentCached(@RequestBody JSONObject param) {
		return JSONUtil.getResponse(1);
	}
	
	@RequestMapping("/common/getScheduleSubjectTclassList")
	@ResponseBody
	public JSONObject getScheduleSubjectTclassList(@RequestBody JSONObject param){
		return postAction(param, "common/getScheduleSubjectTclassList");
	}

	@RequestMapping("/common/getScheduleIdGradeList")
	@ResponseBody
	public JSONObject getScheduleIdGradeList(@RequestBody JSONObject param){
		return postAction(param, "common/getScheduleIdGradeList");
	}
	
	@RequestMapping("/common/getTeachingTaskGradeList")
	@ResponseBody
	public JSONObject getTeachingTaskGradeList(@RequestBody JSONObject param){
		return getScheduleIdGradeList(param);
	}

	@RequestMapping("/common/getScheduleSubjectList")
	@ResponseBody
	public JSONObject getScheduleSubjectList(@RequestBody JSONObject param){
		return postAction(param, "common/getScheduleSubjectList");
	}

	@RequestMapping("/common/getScheduleGroundList")
	@ResponseBody
	public JSONObject getScheduleGroundList(@RequestBody JSONObject param){
		return postAction(param, "common/getScheduleGroundList");
	}

	@RequestMapping("/common/getScheduleSubjectGroupList")
	@ResponseBody
	public JSONObject getScheduleSubjectGroupList(@RequestBody JSONObject param){
		return postAction(param, "common/getScheduleSubjectGroupList");
	}

	@RequestMapping("/common/getScheduleSubjectGroupTclassList")
	@ResponseBody
	public JSONObject getScheduleSubjectGroupTclassList(@RequestBody JSONObject param){
		return postAction(param, "common/getScheduleSubjectGroupTclassList");
	}
	@RequestMapping("/common/clearTimeTableCache")
	@ResponseBody
	public JSONObject clearTimeTableCache(@RequestBody JSONObject param){
		return JSONUtil.getResponse(1);
	}
	
	private void initScheduleParameter(HttpServletRequest req, JSONObject param) {
		param.put("sessionId", request.getSession().getId());
		param.put("scheduleId", req.getSession().getAttribute("scheduleId"));
		param.put("gradeId", req.getSession().getAttribute("gradeId"));
	}
	
	private void setReportData(JSONObject result) {
		Object obj = result.get("data");
		this.request.getSession().setAttribute("schedulereport", obj);
	}
	
	private JSONObject postAction(JSONObject param, String action) {
		if (param != null) {
			if (StringUtils.isEmpty(param.get("schoolId"))) {
				param.put("schoolId", request.getSession().getAttribute("xxdm"));
			}
			School school = (School) request.getSession().getAttribute("school");
			if (school != null) {
				param.put("schoolExtId", school.getExtId());
				param.put("schoolName", school.getName());
			}
		}
		if (!rootPath.endsWith("/schedule/")) {
			rootPath += "/schedule/";
		}
		return HttpClientUtil.postAction(rootPath + action, param);		
	}

	private JSONObject checkTeacher(JSONObject param) {
		String userId = param.getString("userId");
		Long schoolId = param.getLong("schoolId");
		Long uid = Long.parseLong(userId);
		String xnxq = allCommonDataService.getCurTermInfoId(schoolId);
		User user = allCommonDataService.getUserById(schoolId, uid, xnxq);
		
		if (user == null || user.getUserPart() == null
				|| user.getUserPart().getRole() == null) {
			return JSONUtil.getResponse(-1002, "未查询到用户!!!");
		}
		if (schoolId == 0) {
			return JSONUtil.getResponse(-1002, "未查询到归属学校!!!");
		}
		if (user.getUserPart().getRole().equals(T_Role.Teacher)
				|| user.getUserPart().getRole().equals(T_Role.SchoolManager)
				|| user.getUserPart().getRole().equals(T_Role.Staff)
				|| user.getUserPart().getRole().equals(T_Role.SystemManager)) {
		} else {
			return JSONUtil.getResponse(-1002, "用户角色错误 !!!");
		}

		param.put("termInfoId", xnxq);
		param.put("teacherId", user.getAccountPart().getId());
		if (StringUtils.isEmpty(param.get("teacherId"))) {
			return JSONUtil.getResponse(-1, "老师账号id为空，查询失败。");
		}
		return null;
	}

	private JSONObject checkStudent(JSONObject param) {
		String xnxq = param.getString("termInfoId");
		String userId = param.getString("userId");
		Long schoolId = param.getLong("schoolId");
		if (StringUtils.isEmpty(xnxq)) {
			xnxq = allCommonDataService.getCurTermInfoId(schoolId);
			param.put("termInfoId", xnxq);
		}
		User user = null;
		if (schoolId == null) {
			Object s = request.getSession().getAttribute("xxdm");
			schoolId = Long.parseLong(s.toString());
			user = (User) request.getSession().getAttribute("user");
		} else {
			Long uid = Long.parseLong(userId);
			user = allCommonDataService.getUserById(schoolId, uid, xnxq);
		}
		
		if (user == null || user.getUserPart() == null
				|| user.getUserPart().getRole() == null) {
			return JSONUtil.getResponse(-1002, "未查询到用户!!!");
		}
		if (schoolId == 0) {
			return JSONUtil.getResponse(-1002, "未查询到归属学校!!!");
		}
		if (user.getUserPart().getRole().equals(T_Role.Parent)) {
			if (user.getParentPart() != null
					&& user.getParentPart().getClassId() != 0) {
				Long stdId =  user.getParentPart().getStudentId();
				user =  allCommonDataService.getUserById(schoolId, stdId, xnxq);
			}
		} else if (user.getUserPart().getRole().equals(T_Role.Student)) {
		} else {
			return JSONUtil.getResponse(-1002, "用户角色错误 !!!");
		}

		param.put("studentId", user.getAccountPart().getId());
		param.put("studentName", user.getAccountPart().getName());
		param.put("classId", user.getStudentPart().getClassId());
		if (StringUtils.isEmpty(param.get("studentId"))) {
			return JSONUtil.getResponse(-1, "学生账号id为空，查询失败。");
		}
		return null;
	}

	private static String getTerminfoName(String termInfoId){
		int year = Integer.parseInt( termInfoId.substring(0, 4));
		int termNo =  Integer.parseInt(termInfoId.substring(4, 5));
		String CHN_NUMBER[] = {"", "一", "二"};
		return String.format("%d-%d学年第%s学期", year,year+1,CHN_NUMBER[termNo]);
	}

}
