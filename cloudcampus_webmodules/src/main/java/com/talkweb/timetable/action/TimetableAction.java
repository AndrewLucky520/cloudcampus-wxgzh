package com.talkweb.timetable.action;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.AccountStructConstants;
import com.talkweb.accountcenter.thrift.School;
import com.talkweb.accountcenter.thrift.T_GradeLevel;
import com.talkweb.base.common.CacheExpireTime;
import com.talkweb.base.common.OutputMessage;
import com.talkweb.common.action.BaseAction;
import com.talkweb.common.tools.UUIDUtil;
import com.talkweb.commondata.dao.RedisOperationDAO;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.timetable.arrangement.service.ArrangeDataService;
import com.talkweb.timetable.arrangement.service.SmartArrangeService;
import com.talkweb.timetable.domain.CourseSetInfor;
import com.talkweb.timetable.dynamicProgram.service.DynamicProgramService;
import com.talkweb.timetable.service.TimetableService;

/**
 * @ClassName: TimetableManageAction.java
 * @version:1.0
 * @Description: 课表管理控制器
 * @date 2015年5月21日
 */
@Controller
@RequestMapping(value = "/timetableManage/")
public class TimetableAction extends BaseAction {

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

	@Resource(name="redisOperationDAOSDRTempDataImpl")
 	private RedisOperationDAO redisOperationDAO;
	
	/**
	 * 获取临时文件保存目录
	 */
	@Value("#{settings['timetable.programVersion']}")
	private String programVersion;


	private void setPromptMessage(JSONObject object, String code, String message) {
		object.put("code", code);
		object.put("msg", message);
	}

	private boolean JSONObjectNotEmpty(JSONObject object) {
		return null != object && object.size() > 0;
	}

	private boolean JSONArrayNotEmpty(JSONArray object) {
		return null != object && object.size() > 0;
	}

	/** -----TAB页-课表首页----- **/
	/** -----查询课表列表----- **/
	@RequestMapping(value = "lookup/getTimetableList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getTimetableList(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String xnxq = request.getString("semesterCode");
		String label = request.getString("isManager");
		if (StringUtils.isNotEmpty(xnxq) && xnxq.length() == 5) {
			String schoolYear = xnxq.substring(0, 4);
			String termName = xnxq.substring(4);
			Map<String, String> map = new HashMap<String, String>();
			map.put("schoolYear", schoolYear);
			map.put("termName", termName);
			map.put("schoolId", getXxdm(req));
			if (StringUtils.isEmpty(label)
					||!label.equals("1")){
				map.put("published", "2");
			}
			List<JSONObject> timetableList = timetableService.getTimetable(map);
			if (CollectionUtils.isNotEmpty(timetableList)) {
				response.put("data", timetableList);
				setPromptMessage(response,
						OutputMessage.querySuccess.getCode(),
						OutputMessage.querySuccess.getDesc());
			} else {
				setPromptMessage(response, OutputMessage.success.getCode(),
						"查询数据为空!!!");
			}
		} else {
			setPromptMessage(response, OutputMessage.queryDataError.getCode(),
					"请求参数异常!!!");
		}
		return response;
	}

	/** -----新增课表----- **/
	@RequestMapping(value = "lookup/addTimetable", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject addTimetable(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String xnxq = request.getString("semesterCode");
		String tableName = request.getString("timetableName");
		if (StringUtils.isNotEmpty(xnxq) && xnxq.length() == 5
				&& StringUtils.isNotEmpty(tableName)) {
			String schoolYear = xnxq.substring(0, 4);
			String termName = xnxq.substring(4);
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("schoolYear", schoolYear);
			map.put("timetableId", UUIDUtil.getUUID());
			map.put("termName", termName);
			map.put("schoolId", getXxdm(req));
			map.put("published", "0");
			map.put("createTime", new Date());
			map.put("timetableName", tableName);
			int result = timetableService.addTimetable(map);
			if (result > 0) {
				setPromptMessage(response, OutputMessage.addSuccess.getCode(),
						OutputMessage.addSuccess.getDesc());
			} else {
				setPromptMessage(response,
						OutputMessage.addDataError.getCode(),
						OutputMessage.addDataError.getDesc());
			}
		} else {
			setPromptMessage(response, OutputMessage.addDataError.getCode(),
					"请求参数异常!!!");
		}
		return response;
	}

	/** -----更新课表----- */
	@RequestMapping(value = "lookup/updateTimetable", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject updateTimetable(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String timetableId = request.getString("timetableId");
		String tableName = request.getString("timetableName");
		if (StringUtils.isNotEmpty(timetableId)
				&& StringUtils.isNotEmpty(tableName)) {
			request.put("schoolId", getXxdm(req));
			int result = timetableService.updateTimetable(request);
			if (result > 0) {
				setPromptMessage(response,
						OutputMessage.updateSuccess.getCode(),
						OutputMessage.updateSuccess.getDesc());
			} else {
				setPromptMessage(response,
						OutputMessage.updateDataError.getCode(),
						OutputMessage.updateDataError.getDesc());
			}
		} else {
			setPromptMessage(response, OutputMessage.updateDataError.getCode(),
					"请求参数异常!!!");
		}
		return response;
	}

	/** -----更新课表发布状态----- */
	@RequestMapping(value = "lookup/updateTimetablePublished", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject updateTimetablePublished(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String timetableId = request.getString("timetableId");
		String published = request.getString("published");
		String xnxq = request.getString("selectedSemester");
		HashMap<String,Object> cxMap = new HashMap<String, Object>();
		if (StringUtils.isNotEmpty(timetableId)
				&& StringUtils.isNotEmpty(published)) {
			request.put("schoolId", getXxdm(req));
			cxMap.put("schoolId", getXxdm(req));
			cxMap.put("xnxq",xnxq);
			cxMap.put("timetableId", timetableId);
			boolean canPublish = true;
			String pbmsg = "";
			if(published.equalsIgnoreCase("2")){
				List<JSONObject> dpgrades = timetableService.getDuplicatePubGrades(cxMap);
				if(dpgrades.size()>0){
					canPublish = false;
					pbmsg="有些年级已在下列课表中发布过，请取消发布以下课表再尝试发布：";
					for(JSONObject obj:dpgrades){
						String gradeIds = obj.getString("gradeIds");
						String timetableName = obj.getString("timetableName");
						String[] grades = gradeIds.split(",");
						String gradeNames = "";
						for(int j=0;j<grades.length;j++){
							String synj = grades[j];
							int gradeLev = Integer.parseInt(commonDataService.ConvertSYNJ2NJDM(synj, xnxq.substring(0,4)));
							String gradeName = AccountStructConstants.T_GradeLevelName.get(T_GradeLevel.findByValue(gradeLev));
							gradeNames += gradeName+",";
						}
						if(gradeNames.trim().length()>0){
							gradeNames = gradeNames.substring(0,gradeNames.length()-1);
						}
						pbmsg += gradeNames+"在课表【"+timetableName+"】已发布;";
					}
					if(pbmsg.trim().length()>0){
						pbmsg = pbmsg.substring(0,pbmsg.length()-1);
					}
				}
			}
			if(canPublish){
				
				int result = timetableService.updateTimetable(request);
				if (result > 0) {
					setPromptMessage(response,
							OutputMessage.updateSuccess.getCode(),
							OutputMessage.updateSuccess.getDesc());
					
					if(published.equals("2")) {
						// 发送模板消息
						HashMap<String,Object> param = new HashMap();
						param.put("timetableId", timetableId);
						param.put("schoolId", request.getString("schoolId"));
						try {
							JSONObject timetableInfo = timetableService.getTimetableDetailById(param);
							request.put("title", timetableInfo.getString("TimetableName"));
							timetableService.sendWxTemplateMsg(request);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					
				} else {
					setPromptMessage(response,
							OutputMessage.updateDataError.getCode(),
							OutputMessage.updateDataError.getDesc());
				}
			}else{
				
				setPromptMessage(response, OutputMessage.updateDataError.getCode(),pbmsg);
			}
		} else {
			setPromptMessage(response, OutputMessage.updateDataError.getCode(),
					"请求参数异常!!!");
		}
		return response;
	}

	/** -----删除课表----- */
	@RequestMapping(value = "lookup/deleteTimetable", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject deleteTimetable(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String timetableId = request.getString("timetableId");
		if (StringUtils.isNotEmpty(timetableId)) {
			request.put("schoolId", getXxdm(req));
			timetableService.deleteTimetable(request);
			setPromptMessage(response, OutputMessage.delSuccess.getCode(),
						OutputMessage.delSuccess.getDesc());
		} else {
			setPromptMessage(response, OutputMessage.delDataError.getCode(),
					"请求参数异常!!!");
		}
		return response;
	}

	/** -----TAB页-复制----- **/
	/** 获取上学期的课表列表 **/
	@RequestMapping(value = "copy/getLastTimetableList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getLastTimetableList(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String timetableId = request.getString("timetableId");
		if (StringUtils.isNotEmpty(timetableId)) {
			request.put("schoolId", getXxdm(req));
			request.put("publish", "0");
			request.put("exclude", "Y");
			List<JSONObject> tbList = timetableService.getLastTimetableList(request);

			if (CollectionUtils.isNotEmpty(tbList)) {
				response.put("data", tbList);
				setPromptMessage(response,
						OutputMessage.querySuccess.getCode(),
						OutputMessage.querySuccess.getDesc());
			} else {
				setPromptMessage(response, OutputMessage.success.getCode(),
						"未查询到相关数据!!!");
			}
		} else {
			setPromptMessage(response, OutputMessage.queryDataError.getCode(),
					"请求参数异常!!!");
		}
		return response;
	}

	/** -----复制课表信息----- **/
	@RequestMapping(value = "copy/copyTimetable", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject copyTimetable(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String fromTimetableId = request.getString("fromTimetableId");
		String toTimetableId = request.getString("toTimetableId");
		if (StringUtils.isNotEmpty(toTimetableId)
				&& StringUtils.isNotEmpty(fromTimetableId)) {
			request.put("schoolId", getXxdm(req));		
			timetableService.copyTimetable(request);
			setPromptMessage(response, OutputMessage.addSuccess.getCode(),
						"复制课表成功!!!");
			
		} else {
			setPromptMessage(response, OutputMessage.addDataError.getCode(),
					"请求参数异常!!!");
		}
		return response;
	}

	/** -----TAB页-设置课表----- **/
	/** -----查询课表设置----- */
	@RequestMapping(value = "courseArrange/getTimetableSection", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getTimetableSection(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String timetableId = request.getString("timetableId");
		if (StringUtils.isNotEmpty(timetableId)) {
			request.put("schoolId", getXxdm(req));
			JSONObject sectionInfor = timetableService
					.getTimetableSection(request);
			if (JSONObjectNotEmpty(sectionInfor)) {
				response.put("data", sectionInfor);
				setPromptMessage(response,
						OutputMessage.querySuccess.getCode(),
						OutputMessage.querySuccess.getDesc());
			} else {
				setPromptMessage(response, OutputMessage.success.getCode(),
						"查询数据为空!!!");
			}
		} else {
			setPromptMessage(response, OutputMessage.queryDataError.getCode(),
					"请求参数异常!!!");
		}
		return response;
	}

	/** -----更新课表设置----- */
	@RequestMapping(value = "courseArrange/updateTimetableSection", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject updateTimetableSection(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String timetableId = request.getString("timetableId");
		if (StringUtils.isNotEmpty(timetableId)) {
			request.put("schoolId", getXxdm(req));
			String message = timetableService.updateTimetableSection(request);
			if (message.contains("更新失败")){
				setPromptMessage(response, OutputMessage.updateDataError.getCode(),
						message);
			}else{
				setPromptMessage(response, OutputMessage.updateSuccess.getCode(),
						message);
			}
		} else {
			setPromptMessage(response, OutputMessage.updateDataError.getCode(),
					"请求参数异常!!!");
		}
		return response;
	}

	/** -----TAB页-设置教学任务----- **/
	/** -----查询课表教学任务----- */
	@RequestMapping(value = "courseArrange/getTeachingTask", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getTeachingTask(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String timetableId = request.getString("timetableId");
		String termInfo = req.getParameter("selectedSemester");
		String gradeId = request.getString("gradeId");
		if (StringUtils.isNotEmpty(timetableId) && StringUtils.isNotEmpty(gradeId)) {
			request.put("school", this.getSchool(req,termInfo));
			request.put("schoolId", getXxdm(req));
			response = timetableService.getTeachingTask(request);
			setPromptMessage(response, OutputMessage.querySuccess.getCode(),
					OutputMessage.querySuccess.getDesc());
		} else {
			setPromptMessage(response, OutputMessage.queryDataError.getCode(),
					OutputMessage.queryDataError.getDesc());
		}
		return response;
	}

	/** -----更新课表教学任务----- */
	@RequestMapping(value = "courseArrange/updateTeachingTask", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject updateTeachingTask(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String timetableId = request.getString("timetableId");
		String gradeId = request.getString("gradeId");
		String classId = request.getString("classId");
		String courseId = request.getString("courseId");
		String weekNum = request.getString("weekNum");
		String nearNum = request.getString("nearNum");
		String termInfo = req.getParameter("selectedSemester");
		String promptMessage = OutputMessage.updateDataError.getDesc();
		boolean isRightParam = false;
		if (StringUtils.isEmpty(classId)) {
			promptMessage = "请选择班级!!!";
		} else if (StringUtils.isEmpty(courseId)) {
			promptMessage = "请选择课程!!!";
		} else if (StringUtils.isEmpty(weekNum)) {
			promptMessage = "周课时不能为空!!!";
		} else if (StringUtils.isEmpty(nearNum)) {
			promptMessage = "连排次数不能为空!!!";
		} else if (StringUtils.isEmpty(timetableId)
				|| StringUtils.isEmpty(gradeId)) {
			promptMessage = "请求参数异常!!!";
		} else {
			float a = Float.parseFloat(weekNum);
			int b = Integer.parseInt(nearNum);
			if (b <= a/2){
			   
			    //超课时优化-lime-2017-01-12-s
				  request.put("schoolId", getXxdm(req));
				  double cnt = timetableService.checkTotalCourse(request);
			      if (cnt>=0.0) {
			    	  isRightParam = true;
				  }else {
					  promptMessage = "\u60a8\u8bbe\u7f6e\u7684\u79d1\u76ee\u0020\u201c\u6bcf\u5468\u4e0a\u8bfe\u201d\u0020\u8282\u6570\u4e4b\u548c\u5df2\u8d85\u8fc7\u4e86\u6bcf\u5468\u4e0a\u8bfe\u7684\u603b\u8282\u6570\uff0c\u8bf7\u6838\u5bf9\u540e\u91cd\u65b0\u8bbe\u7f6e";
				  }
			    //超课时优化-lime-2017-01-12-e
			    
			}else{
				promptMessage = "连排次数不能大于周课时一半!!!";
			}
		}
		if (isRightParam) {
			request.put("schoolId", getXxdm(req));
			request.put("school", this.getSchool(req,termInfo));
			request.put("priority", "0");
			int label = timetableService.updateTeachingTasks(request);
			if (label > 0){				
				setPromptMessage(response,"20","需要微调校验");			
			}else{
				setPromptMessage(response,
						OutputMessage.updateSuccess.getCode(),
						OutputMessage.updateSuccess.getDesc());
			}
			clearProgDataByKey( req.getSession().getId(),timetableId,null);			
		} else {
			setPromptMessage(response, OutputMessage.queryDataError.getCode(),
					promptMessage);
		}
		return response;
	}

	/** -----更新教师及教学任务----- */
	@RequestMapping(value = "courseArrange/updateTeacherAndTeachingTask", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject updateTeacherAndTeachingTask(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String timetableId = request.getString("timetableId");
		request.put("schoolId", getXxdm(req));
		String classId = request.getString("classId");
		String courseId = request.getString("courseId");
		String weekNum = request.getString("weekNum");
		String nearNum = request.getString("nearNum");
		String promptMessage = OutputMessage.updateDataError.getDesc();
		boolean isRightParam = false;
		if (StringUtils.isEmpty(weekNum)) {
			promptMessage = "周课时不能为空!!!";
		} else if (StringUtils.isEmpty(nearNum)) {
			promptMessage = "连排次数不能为空!!!";
		} else if (StringUtils.isEmpty(timetableId)
				|| StringUtils.isEmpty(classId)
				|| StringUtils.isEmpty(courseId)) {
			promptMessage = "请求参数异常!!!";
		} else {
			float a = Float.parseFloat(weekNum);
			int b = Integer.parseInt(nearNum);
			if (b <= a/2){
			    //超课时优化-lime-2017-01-12-s
				  request.put("schoolId", getXxdm(req));
			      double cnt = timetableService.checkTotalCourse(request);
			      if (cnt>=0.0) {
			    	  isRightParam = true;
				  }else {
					  promptMessage = "\u60a8\u8bbe\u7f6e\u7684\u79d1\u76ee\u0020\u201c\u6bcf\u5468\u4e0a\u8bfe\u201d\u0020\u8282\u6570\u4e4b\u548c\u5df2\u8d85\u8fc7\u4e86\u6bcf\u5468\u4e0a\u8bfe\u7684\u603b\u8282\u6570\uff0c\u8bf7\u6838\u5bf9\u540e\u91cd\u65b0\u8bbe\u7f6e";
				  }
			    //超课时优化-lime-2017-01-12-e
			}else{
				promptMessage = "连排次数不能大于周课时一半!!!";
			}
		}
		if (isRightParam) {			
			int code = timetableService.updateTeacherAndTask(request);	
			/** ---微调校验--- **/
			if (code > 0){
				setPromptMessage(response,"20","需要微调校验");
			}else{
				setPromptMessage(response,
						OutputMessage.updateSuccess.getCode(),
						OutputMessage.updateSuccess.getDesc());
			}
			clearProgDataByKey( req.getSession().getId(),timetableId,null);
		} else {
			setPromptMessage(response, OutputMessage.updateDataError.getCode(),
					promptMessage);
		}
		return response;
	}

	/** -----删除教学任务----- **/
	@RequestMapping(value = "courseArrange/deleteTeachingTask", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject deleteTeachingTask(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String timetableId = request.getString("timetableId");
		String gradeId = request.getString("gradeId");
		String termInfo = req.getParameter("selectedSemester");
		if (StringUtils.isNotEmpty(timetableId)
				&& StringUtils.isNotEmpty(gradeId)) {
			request.put("schoolId", getXxdm(req));
			request.put("school", this.getSchool(req,termInfo));
			timetableService.deleteTeachingTask(request);			
			setPromptMessage(response,
					OutputMessage.delSuccess.getCode(),
					OutputMessage.delSuccess.getDesc());
			clearProgDataByKey( req.getSession().getId(),timetableId,null);
		} else {
			setPromptMessage(response, OutputMessage.delDataError.getCode(),
					"请求参数异常!!!");
		}
		return response;
	}

	/** -----查询教师信息----- */
	@RequestMapping(value = "courseArrange/getTaskTeacher", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getTaskTeacher(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		List<JSONObject> teacherList = null;
		request.put("schoolId", getXxdm(req));
		String termInfo = req.getParameter("selectedSemester");
		request.put("school", this.getSchool(req,termInfo));
		teacherList = timetableService.getTeacherByCondition(request);
		if (CollectionUtils.isNotEmpty(teacherList)) {
			response.put("data", teacherList);
			setPromptMessage(response, OutputMessage.querySuccess.getCode(),
					OutputMessage.updateSuccess.getDesc());
		} else {
			setPromptMessage(response, OutputMessage.success.getCode(),
					"未查询到相关数据!!!");
		}
		return response;
	}

	/** -----TAB页-设置排课规则----- **/
	/** -----更新排课规则----- */
	@RequestMapping(value = "courseArrange/updateCourseRule", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject updateCourseRule(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String timetableId = request.getString("timetableId");
		String useGradeId = request.getString("gradeId");
		String courseId = request.getString("courseId");	
		String promptMessage = OutputMessage.updateDataError.getDesc();
		boolean isRightParam = false;
		if (StringUtils.isEmpty(courseId)) {
			promptMessage = "请选择课程!!!";
		} else if (StringUtils.isEmpty(timetableId)
				|| StringUtils.isEmpty(useGradeId)) {
			promptMessage = "请求参数异常!!!";
		} else {
			isRightParam = true;
		}	
		if (isRightParam) {
			request.put("schoolId", getXxdm(req));
			int code = timetableService.updateCourseRule(request);
			/** ---微调校验--- **/
			if (code > 0){
				setPromptMessage(response,"20","需要微调校验");
			}else{
				setPromptMessage(response,
						OutputMessage.updateSuccess.getCode(),
						OutputMessage.updateSuccess.getDesc());
			}
			clearProgDataByKey( req.getSession().getId(),timetableId,null);
		} else {
			setPromptMessage(response, OutputMessage.updateDataError.getCode(),
					promptMessage);
		}
		return response;
	}

	@RequestMapping(value = "courseArrange/getTeachingTaskCourse", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getTeachingTaskCourse(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String timetableId = request.getString("timetableId");
		String gradeId = request.getString("gradeId");
		String termInfo = req.getParameter("selectedSemester");
		if (StringUtils.isNotEmpty(timetableId)
				&& StringUtils.isNotEmpty(gradeId)) {
			request.put("schoolId", getXxdm(req));
			request.put("school", this.getSchool(req,termInfo));
		    JSONArray courseArray = timetableService.getTeachingTaskCourse(request);
			if (JSONArrayNotEmpty(courseArray)) {
				response.put("data", courseArray);
				setPromptMessage(response,
						OutputMessage.querySuccess.getCode(),
						OutputMessage.querySuccess.getDesc());
			} else {
				setPromptMessage(response, OutputMessage.success.getCode(),
						"未查询到相关数据!!!");
			}
		} else {
			setPromptMessage(response, OutputMessage.queryDataError.getCode(),
					"请求参数有误!!!");
		}
		return response;
	}

	/** -----查询科目排课规则信息----- */
	@RequestMapping(value = "courseArrange/getCourseRule", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getCourseRule(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String timetableId = request.getString("timetableId");
		String useGradeId = request.getString("gradeId");
		String courseId = request.getString("courseId");
		if (StringUtils.isNotEmpty(timetableId)
				&& StringUtils.isNotEmpty(courseId)
				&& StringUtils.isNotEmpty(useGradeId)) {
			request.put("schoolId", getXxdm(req));
			JSONObject object = timetableService.getCourseRule(request);
			if (JSONObjectNotEmpty(object)) {
				response.put("data", object);
				setPromptMessage(response,
						OutputMessage.querySuccess.getCode(),
						OutputMessage.querySuccess.getDesc());
			} else {
				setPromptMessage(response, OutputMessage.success.getCode(),
						"未查询到相关数据!!!");
			}
		} else {
			setPromptMessage(response, OutputMessage.queryDataError.getCode(),
					"请求参数有误!!!");
		}
		return response;
	}

	/** -----删除科目规则----- **/
	@RequestMapping(value = "courseArrange/deleteCourseRule", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject deleteCourseRule(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String timetableId = request.getString("timetableId");
		String useGradeId = request.getString("gradeId");
		String courseId = request.getString("courseId");
		if (StringUtils.isNotEmpty(timetableId)
				&& StringUtils.isNotEmpty(courseId)
				&& StringUtils.isNotEmpty(useGradeId)) {
			request.put("schoolId", getXxdm(req));
			timetableService.deleteCourseRule(request);
			setPromptMessage(response,
					OutputMessage.delSuccess.getCode(),
					OutputMessage.delSuccess.getDesc());
			clearProgDataByKey( req.getSession().getId(),timetableId,null);
		} else {
			setPromptMessage(response, OutputMessage.delDataError.getCode(),
					"请求参数异常!!!");
		}
		return response;
	}
	
	/** -----查询教师列表----- */
	/**
	@RequestMapping(value = "courseArrange/getRuleTeacherList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getRuleTeacherList(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		request.put("schoolId", getXxdm(req));
		request.put("school", this.getSchool(req));
		response = timetableService.getRuleTeacherList(request);
		if (JSONObjectNotEmpty(response)) {
			setPromptMessage(response, OutputMessage.querySuccess.getCode(),
					OutputMessage.querySuccess.getDesc());
		} else {
			setPromptMessage(response, OutputMessage.success.getCode(),
					"查询数据为空!!!");
		}
		return response;
	} */

	/** -----编辑教师排课规则----- */
	@RequestMapping(value = "courseArrange/updateTeacherRule", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject updateTeacherRule(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String timetableId = request.getString("timetableId");
		if (StringUtils.isNotEmpty(timetableId)) {
			request.put("schoolId", getXxdm(req));
			int code = timetableService.updateTeacherRule(request);			
			/** ---微调校验--- **/
			if (code > 0){
				setPromptMessage(response,"20","需要微调校验");
			}else{
				setPromptMessage(response,
						OutputMessage.updateSuccess.getCode(),
						OutputMessage.updateSuccess.getDesc());
			}
			clearProgDataByKey( req.getSession().getId(),timetableId,null);
		} else {
			setPromptMessage(response, OutputMessage.updateDataError.getCode(),
					"请求参数异常!!!");
		}
		return response;
	}

	/** -----查询教师及教师组规则信息----- */
	@RequestMapping(value = "courseArrange/getTeacherRule", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getTeacherRule(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String timetableId = request.getString("timetableId");
		String termInfo = req.getParameter("selectedSemester");
		if (StringUtils.isNotEmpty(timetableId)) {
			request.put("school", this.getSchool(req,termInfo));
			request.put("schoolId", getXxdm(req));
			JSONObject ruleObj = timetableService.getTeacherRule(request);
			if (JSONObjectNotEmpty(ruleObj)) {
				response.put("data", ruleObj);
				setPromptMessage(response,
						OutputMessage.querySuccess.getCode(),
						OutputMessage.querySuccess.getDesc());
			} else {
				setPromptMessage(response, OutputMessage.success.getCode(),
						"未查询到相关数据!!!");
			}
		} else {
			setPromptMessage(response, OutputMessage.queryDataError.getCode(),
					"请求参数有误!!!");
		}
		return response;
	}

	/** -----删除教师规则----- **/
	@RequestMapping(value = "courseArrange/deleteTeacher", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject deleteTeacher(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String timetableId = request.getString("timetableId");
		String teacherIds = request.getString("teacherId");
		if (StringUtils.isNotEmpty(timetableId)
				&& StringUtils.isNotEmpty(teacherIds)) {
			request.put("schoolId", getXxdm(req));
			timetableService.deleteTeacherRule(request);
			setPromptMessage(response,
					OutputMessage.delSuccess.getCode(),
					OutputMessage.delSuccess.getDesc());
			clearProgDataByKey( req.getSession().getId(),timetableId,null);
		} else {
			setPromptMessage(response, OutputMessage.delDataError.getCode(),
					"请求参数异常!!!");
		}
		return response;
	}
	
	/** -----保存教师组规则----- */
	@RequestMapping(value = "courseArrange/saveTeacherGroup", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject saveTeacherGroup(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String timetableId = request.getString("timetableId");
		if (StringUtils.isNotEmpty(timetableId)) {
			request.put("schoolId", getXxdm(req));
			String groupId = request.getString("teachergroupId");
			int code = 0;
			if (StringUtils.isNotEmpty(groupId)){
				code = timetableService.updateTeacherGroup(request);
			}else{
				groupId = timetableService.addTeacherGroup(request);	
			}			
			/** ---微调校验--- **/
			response.put("teachergroupId", groupId);
			if (code > 0){
				setPromptMessage(response,"20","需要微调校验");
			}else {
				setPromptMessage(response,"2","保存数据成功!!!");				
			}
			clearProgDataByKey( req.getSession().getId(),timetableId,null);
		} else {
			setPromptMessage(response, OutputMessage.updateDataError.getCode(),
					"请求参数异常!!!");
		}
		return response;
	}
	
	/** -----删除教师组规则----- **/
	@RequestMapping(value = "courseArrange/delTeacherGroup", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject delTeacherGroup(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String timetableId = request.getString("timetableId");
		String teachergroupId = request.getString("teachergroupId");
		if (StringUtils.isNotEmpty(timetableId)
				&& StringUtils.isNotEmpty(teachergroupId)) {
			request.put("schoolId", getXxdm(req));
			timetableService.deleteTeacherGroup(request);
			setPromptMessage(response,
					OutputMessage.delSuccess.getCode(),
					OutputMessage.delSuccess.getDesc());
			clearProgDataByKey( req.getSession().getId(),timetableId,null);
		} else {
			setPromptMessage(response, OutputMessage.delDataError.getCode(),
					"请求参数异常!!!");
		}
		return response;
	}	

	/** -----新增合班上课----- **/
	@RequestMapping(value = "courseArrange/addClassGroup", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject addClassGroup(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String timetableId = request.getString("timetableId");
		String classGroupName = request.getString("classGroupName");
		String classIds = request.getString("classIds");
		String courseId = request.getString("courseId");		
		String promptMessage = OutputMessage.updateDataError.getDesc();
		boolean isRightParam = false;
		if (StringUtils.isEmpty(courseId)) {
			promptMessage = "请选择课程!!!";
		} else if(StringUtils.isEmpty(classIds)) {
			promptMessage = "请选择班级!!!";
		} else if(!classIds.contains(",")){
			promptMessage = "必须是两个以上班级!!!";
		} else if (StringUtils.isEmpty(timetableId)
				|| StringUtils.isEmpty(classGroupName)) {
			promptMessage = "请求参数异常!!!";
		} else {
			isRightParam = true;
		}		
		if (isRightParam) {
			request.put("schoolId", getXxdm(req));
			int result = timetableService.addClassGroup(request);
			if (result > 0){
				/** ---微调校验--- **/
				setPromptMessage(response,"20","需要微调校验");
			}else{
				setPromptMessage(response, OutputMessage.addSuccess.getCode(),
						OutputMessage.addSuccess.getDesc());
			}	
			clearProgDataByKey( req.getSession().getId(),timetableId,null);
		} else {
			setPromptMessage(response, OutputMessage.addDataError.getCode(),
					promptMessage);
		}
		return response;
	}

	/** -----查询合班组信息----- **/
	@RequestMapping(value = "courseArrange/getClassGroup", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getClassGroup(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String timetableId = request.getString("timetableId");
		String termInfo = req.getParameter("selectedSemester");
		if (StringUtils.isNotEmpty(timetableId)) {
			request.put("schoolId", getXxdm(req));
			request.put("school", this.getSchool(req,termInfo));
			List<JSONObject> groupList = timetableService
					.getClassGroup(request);
			if (CollectionUtils.isNotEmpty(groupList)) {
				response.put("data", groupList);
				setPromptMessage(response,
						OutputMessage.querySuccess.getCode(),
						OutputMessage.querySuccess.getDesc());
			} else {
				setPromptMessage(response, OutputMessage.success.getCode(),
						"未查询到相关数据!!!");
			}
		} else {
			setPromptMessage(response, OutputMessage.queryDataError.getCode(),
					"请求参数异常!!!");
		}
		return response;
	}

	/** -----删除班级----- **/
	@RequestMapping(value = "courseArrange/deleteClass", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject deleteClass(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String timetableId = request.getString("timetableId");
		String classGroupId = request.getString("classGroupId");
		String classId = request.getString("classId");
		if (StringUtils.isNotEmpty(timetableId)
				&& StringUtils.isNotEmpty(classGroupId)
				&& StringUtils.isNotEmpty(classId)) {
			request.put("schoolId", getXxdm(req));
			int result = timetableService.deleteClass(request);
			if (result > 0) {
				/** ---微调校验--- **/
				setPromptMessage(response,"20","需要微调校验");
			} else{
				setPromptMessage(response,
						OutputMessage.delSuccess.getCode(),
						OutputMessage.delSuccess.getDesc());
			}
			clearProgDataByKey( req.getSession().getId(),timetableId,null);
		} else {
			setPromptMessage(response, OutputMessage.delDataError.getCode(),
					"请求参数异常!!!");
		}
		return response;
	}

	/** -----删除合班组----- **/
	@RequestMapping(value = "courseArrange/deleteClassGroup", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject deleteClassGroup(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String timetableId = request.getString("timetableId");
		String classGroupId = request.getString("classGroupId");
		String termInfo = req.getParameter("selectedSemester");
		if (StringUtils.isNotEmpty(timetableId)
				&& StringUtils.isNotEmpty(classGroupId)) {
			request.put("schoolId", getXxdm(req));
			request.put("school", this.getSchool(req,termInfo));
			int result = timetableService.deleteClassGroup(request);
			if (result > 0){
				/** ---微调校验--- **/
				setPromptMessage(response,"20","需要微调校验");
			}else{
				setPromptMessage(response,
						OutputMessage.delSuccess.getCode(),
						OutputMessage.delSuccess.getDesc());
			}
			clearProgDataByKey( req.getSession().getId(),timetableId,null);
		} else {
			setPromptMessage(response, OutputMessage.delDataError.getCode(),
					"请求参数异常!!!");
		}
		return response;
	}

	/** -----新增单双周设置----- **/
	@RequestMapping(value = "courseArrange/addMonoWeek", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject addMonoWeek(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String timetableId = request.getString("timetableId");
		String gradeId = request.getString("gradeId");
		String courseIdOne = request.getString("courseIdOne");
		String courseIdTwo = request.getString("courseIdTwo");	
		String promptMessage = OutputMessage.updateDataError.getDesc();
		boolean isRightParam = false;
		if (StringUtils.isEmpty(courseIdOne)
				|| StringUtils.isEmpty(courseIdTwo)) {
			promptMessage = "请选择单双周科目!!!";
		} else if (StringUtils.isEmpty(timetableId)
				|| StringUtils.isEmpty(gradeId)) {
			promptMessage = "请求参数异常!!!";
		} else {
			isRightParam = true;
		}
		if (isRightParam) {
			request.put("schoolId", getXxdm(req));
			int result = timetableService.addMonoWeek(request);
			if (result > 0) {
				/** ---微调校验--- **/
				setPromptMessage(response,"20","需要微调校验");
			} else {
				setPromptMessage(response,
						OutputMessage.addSuccess.getCode(),
						OutputMessage.addSuccess.getDesc());
			}
		} else {
			setPromptMessage(response, 
					OutputMessage.addDataError.getCode(),
					promptMessage);
		}
		return response;
	}

	/** -----查询单双周设置----- **/
	@RequestMapping(value = "courseArrange/getMonoWeek", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getMonoWeek(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String timetableId = request.getString("timetableId");
		String gradeId = request.getString("gradeId");
		String termInfo = req.getParameter("selectedSemester");
		if (StringUtils.isNotEmpty(timetableId)
				&& StringUtils.isNotEmpty(gradeId)) {
			request.put("schoolId", getXxdm(req));
			request.put("school", this.getSchool(req,termInfo));
			JSONObject monoObj = timetableService.getMonoWeek(request);
			response.put("data", monoObj);
			setPromptMessage(response, OutputMessage.querySuccess.getCode(),
					OutputMessage.querySuccess.getDesc());
		} else {
			setPromptMessage(response, OutputMessage.queryDataError.getCode(),
					"请求参数异常!!!");
		}
		return response;
	}

	/** -----删除单双周设置----- **/
	@RequestMapping(value = "courseArrange/deleteMonoWeek", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject deleteMonoWeek(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String timetableId = request.getString("timetableId");
		String schoolId = getXxdm(req);
		String gradeId = request.getString("gradeId");
		String courseIdOne = request.getString("courseIdOne");
		String courseIdTwo = request.getString("courseIdTwo");
		if (StringUtils.isNotEmpty(timetableId)
				&& StringUtils.isNotEmpty(courseIdOne)
				&& StringUtils.isNotEmpty(gradeId)
				&& StringUtils.isNotEmpty(courseIdTwo)) {
			request.put("schoolId", schoolId);
			timetableService.deleteMonoWeek(request);
			setPromptMessage(response,
					OutputMessage.delSuccess.getCode(),
					OutputMessage.delSuccess.getDesc());	
		} else {
			setPromptMessage(response, OutputMessage.delDataError.getCode(),
					"请求参数异常!!!");
		}
		return response;
	}
	
	/** -----查询场地规则列表----- */
	@RequestMapping(value = "groundRule/getGroundRuleList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getGroundRuleList(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String timetableId = request.getString("timetableId");
		String termInfo = req.getParameter("selectedSemester");
		if (StringUtils.isNotEmpty(timetableId)) {
			request.put("schoolId", getXxdm(req));
			request.put("school", this.getSchool(req,termInfo));
			JSONArray groundArray = timetableService.getGroundRuleList(request);
			if (JSONArrayNotEmpty(groundArray)) {
				response.put("data", groundArray);
				setPromptMessage(response,
						OutputMessage.querySuccess.getCode(),
						OutputMessage.querySuccess.getDesc());
			} else {
				setPromptMessage(response, OutputMessage.success.getCode(),
						"未查询到相关数据!!!");
			}
		} else {
			setPromptMessage(response, OutputMessage.queryDataError.getCode(),
					"请求参数异常!!!");
		}
		return response;
	}
	
	/** -----更新场地规则----- */
	@RequestMapping(value = "groundRule/updateGroundRuleList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject updateGroundRuleList(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String timetableId = request.getString("timetableId");
		String schoolId = getXxdm(req);
		String ruleGroundId = request.getString("ruleGroundId");
		if (StringUtils.isNotEmpty(timetableId)) {
			request.put("schoolId", schoolId);	
			int result = 0;
			if (StringUtils.isEmpty(ruleGroundId)){
				request.put("groundRuleId", UUIDUtil.getUUID());
				result = timetableService.insertGroundRule(request);
			}else{
				request.put("groundRuleId", ruleGroundId);
				result = timetableService.updateGroundRule(request);
			}
			if (result > 0) {
				/** ---微调校验--- **/
				setPromptMessage(response,"20","需要微调校验");
			} else {
				setPromptMessage(response,
						OutputMessage.updateSuccess.getCode(),
						OutputMessage.updateSuccess.getDesc());
			}
			clearProgDataByKey( req.getSession().getId(),timetableId,null);
		} else {
			setPromptMessage(response, OutputMessage.updateDataError.getCode(),
					"请求参数异常!!!");
		}
		return response;
	}
	
	/** -----删除场地规则----- */
	@RequestMapping(value = "groundRule/deleteGroundRuleList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject deleteGroundRuleList(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String timetableId = request.getString("timetableId");
		String schoolId = getXxdm(req);
		String ruleGroundId = request.getString("ruleGroundId");
		if (StringUtils.isNotEmpty(timetableId)
				&& StringUtils.isNotEmpty(ruleGroundId)) {
			request.put("schoolId", schoolId);
			timetableService.deleteGroundRule(request);
			setPromptMessage(response,
					OutputMessage.delSuccess.getCode(),
					OutputMessage.delSuccess.getDesc());
			clearProgDataByKey( req.getSession().getId(),timetableId,null);
		} else {
			setPromptMessage(response, OutputMessage.delDataError.getCode(),
					"请求参数异常!!!");
		}
		return response;
	}
	
	/** -----获取课表下的科目与年级列表----- */
	@RequestMapping(value = "groundRule/getSubjectListAndGradeList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getSubjectListAndGradeList(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String timetableId = request.getString("timetableId");
		String termInfo = req.getParameter("selectedSemester");
		String currentXnxq = getCurXnxq(req);
		String currentXn = "";
		if (StringUtils.isNotEmpty(currentXnxq)
				&& currentXnxq.length() >= 4){
			currentXn = currentXnxq.substring(0,4);
		}
		if (StringUtils.isNotEmpty(timetableId)
				&& StringUtils.isNotEmpty(currentXn)) {
			request.put("schoolId", getXxdm(req));
			request.put("school", this.getSchool(req,termInfo));
			request.put("xn", currentXn);
			JSONObject groundObj = timetableService.getSubjectAndGradeList(request);	
			if (JSONObjectNotEmpty(groundObj)) {
				response.put("data", groundObj);
				setPromptMessage(response, OutputMessage.querySuccess.getCode(),
						OutputMessage.querySuccess.getDesc());
			} else {
				setPromptMessage(response, OutputMessage.success.getCode(),
						"未查询到相关数据!!!");
			}
		} else {
			setPromptMessage(response, OutputMessage.queryDataError.getCode(),
					"请求参数异常!!!");
		}
		return response;
	}

	/** -----TAB页-预排课----- **/
	/** -----获取教学任务科目-预排----- */
	@RequestMapping(value = "courseArrange/getTaskCourse", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getTaskCourse(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String timetableId = request.getString("timetableId");
		String termInfo = req.getParameter("selectedSemester");
		if (StringUtils.isNotEmpty(timetableId)) {
			request.put("schoolId", getXxdm(req));
			request.put("school", this.getSchool(req,termInfo));
			List<JSONObject> courseList = timetableService
					.getWalkthroughTaskCourse(request);
			if (CollectionUtils.isNotEmpty(courseList)) {
				response.put("data", courseList);
				setPromptMessage(response,
						OutputMessage.querySuccess.getCode(),
						OutputMessage.querySuccess.getDesc());
			} else {
				setPromptMessage(response, OutputMessage.success.getCode(),
						"未查询到相关数据!!!");
			}
		} else {
			setPromptMessage(response, OutputMessage.queryDataError.getCode(),
					"请求参数有误!!!");
		}
		return response;
	}

	/** -----编辑科目预排信息----- */
	@RequestMapping(value = "courseArrange/updateCourseWalkthrough", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject updateCourseWalkthrough(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String timetableId = request.getString("timetableId");
		String classIds = request.getString("classId");
		String courseId = request.getString("courseId");
		String termInfo = request.getString("selectedSemester");
	 
		String promptMessage = OutputMessage.updateDataError.getDesc();
		boolean isRightParam = false;
		if (StringUtils.isEmpty(classIds)) {
			promptMessage = "请选择班级!!!";
		}else if(StringUtils.isEmpty(courseId)){
			promptMessage = "请选择科目!!!";
		}else if (StringUtils.isEmpty(timetableId)) {
			promptMessage = "请求参数异常!!!";
		} else {
			  request.put("schoolId", getXxdm(req));
			  School school = this.getSchool(req,termInfo);
			  request.put("school", school);
			  HashMap<String, CourseSetInfor> map = timetableService.checkSingleCourse(request);
 			  if (map!=null && map.size() > 0 ) {
				  Iterator<Map.Entry<String ,  CourseSetInfor >> it = map.entrySet().iterator();
				 
				  if (StringUtils.isNotEmpty(request.getString("walkGroupId"))) {
					  promptMessage = "\u4fee\u6539\u5931\u8d25\uff01";
					  
				 }else{
					 promptMessage = "\u8bbe\u7f6e\u5931\u8d25\uff01";
				 }
				  HashMap<Double, String> hashMap = new HashMap<Double, String>();
				  String courseName = null;
				  Double userSet = null;
				  while (it.hasNext()) {
			           Map.Entry<String, CourseSetInfor> entry = it.next();
			           CourseSetInfor courseSetInfor = entry.getValue();
			           if (hashMap.containsKey(courseSetInfor.getSysSet())) {
			        	   hashMap.put(courseSetInfor.getSysSet(), hashMap.get(courseSetInfor.getSysSet()) + entry.getKey() +"\u3001");
					   }else{
						   hashMap.put(courseSetInfor.getSysSet(), entry.getKey()+"\u3001");
					   }
			           courseName = courseSetInfor.getCourseName();
			           userSet = courseSetInfor.getUserSet();
			           //promptMessage = promptMessage  +  entry.getKey()  + "\u73ed" + courseSetInfor.getCourseName() + "\u79d1\u76ee\u9884\u6392\u4e86" + courseSetInfor.getUserSet()+"\u8282\uff0c\u8d85\u8fc7\u4e86\u6b64\u79d1\u76ee\u6559\u5b66\u4efb\u52a1\u4e2d\u8bbe\u7f6e\u7684\u201c\u6bcf\u5468\u4e0a\u8bfe\u201d" + courseSetInfor.getSysSet() +"\u8282" +"<br/>" ; 
			      }
				  
				  Iterator<Map.Entry<Double, String>> it2 = hashMap.entrySet().iterator();
				  String detail = "";
				  while (it2.hasNext()) {
					  Map.Entry<Double, String> entry = it2.next();
					  String className = entry.getValue(); 
					  detail = detail  + className.substring(0, className.length() - 1 )  + "\u73ed" + courseName + "\u79d1\u76ee <br/> \u9884\u6392\u4e86" + userSet+"\u8282\uff0c\u8d85\u8fc7\u4e86\u6b64\u79d1\u76ee\u6559\u5b66\u4efb\u52a1\u4e2d\u8bbe\u7f6e\u7684\u201c\u6bcf\u5468\u4e0a\u8bfe\u201d" + entry.getKey() +"\u8282" +"<br/>" ; 
					  
				 }
				  response.put("detail", detail);
				  
				 
			  }else {
				  isRightParam = true;
			  }
			
			
		}
		if (isRightParam) {
			request.put("schoolId", getXxdm(req));
			int result = timetableService.updateCourseWalkthrough(request);
			if (result > 0) {
				setPromptMessage(response,
						OutputMessage.updateSuccess.getCode(),
						OutputMessage.updateSuccess.getDesc());
				clearProgDataByKey( req.getSession().getId(),timetableId,null);
			} else {
				setPromptMessage(response,
						OutputMessage.updateDataError.getCode(),
						OutputMessage.updateDataError.getDesc());
			}
		} else {
			setPromptMessage(response, OutputMessage.updateDataError.getCode(),
					promptMessage);
		}
		return response;
	}

	/** -----查看科目预排信息详情----- **/
	@RequestMapping(value = "courseArrange/getCourseWalkthrough", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getCourseWalkthrough(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String timetableId = request.getString("timetableId");
		String classIds = request.getString("classId");
		String courseId = request.getString("courseId");
		if (StringUtils.isNotEmpty(timetableId)
				&& StringUtils.isNotEmpty(classIds)
				&& StringUtils.isNotEmpty(courseId)) {
			request.put("schoolId", getXxdm(req));
			JSONObject walkthrough = timetableService
					.getCourseWalkthrough(request);
			if (JSONObjectNotEmpty(walkthrough)) {
				response.put("data", walkthrough);
				setPromptMessage(response,
						OutputMessage.querySuccess.getCode(),
						OutputMessage.querySuccess.getDesc());
			} else {
				setPromptMessage(response, OutputMessage.success.getCode(),
						"未查询到相关数据!!!");
			}
		} else {
			setPromptMessage(response, OutputMessage.queryDataError.getCode(),
					"请求参数异常!!!");
		}
		return response;
	}

	/** -----查看已安排的科目预排列表----- **/
	@RequestMapping(value = "courseArrange/getScheduledCourse", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getScheduledCourse(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String timetableId = request.getString("timetableId");
		String gradeId = request.getString("gradeId");
		String courseId = request.getString("courseId");
		String termInfo = req.getParameter("selectedSemester");
		if (StringUtils.isNotEmpty(timetableId)
				&& StringUtils.isNotEmpty(courseId)
				&& StringUtils.isNotEmpty(gradeId)) {
			request.put("schoolId", getXxdm(req));
			request.put("school", this.getSchool(req,termInfo));
			List<JSONObject> scheduledList = timetableService
					.getScheduledCourseList(request);
			if (CollectionUtils.isNotEmpty(scheduledList)) {
				response.put("data", scheduledList);
				setPromptMessage(response,
						OutputMessage.querySuccess.getCode(),
						OutputMessage.querySuccess.getDesc());
			} else {
				setPromptMessage(response, OutputMessage.success.getCode(),
						"未查询到相关数据!!!");
			}
		} else {
			setPromptMessage(response, OutputMessage.queryDataError.getCode(),
					"请求参数异常!!!");
		}
		return response;
	}

	/** -----移除已安排的科目预排----- **/
	@RequestMapping(value = "courseArrange/deleteCourseWalkthrough", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject deleteCourseWalkthrough(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String timetableId = request.getString("timetableId");
		String walkGroupId = request.getString("walkGroupId");
		String courseId = request.getString("courseId");
		if (StringUtils.isNotEmpty(timetableId)
				&& StringUtils.isNotEmpty(walkGroupId)
				&& StringUtils.isNotEmpty(courseId)) {
			request.put("schoolId", getXxdm(req));
			int result = timetableService.deleteCourseWalkthrough(request);
			if (result > 0) {
				setPromptMessage(response, OutputMessage.delSuccess.getCode(),
						OutputMessage.delSuccess.getDesc());
				clearProgDataByKey( req.getSession().getId(),timetableId,null);
			} else {
				setPromptMessage(response,
						OutputMessage.delDataError.getCode(),
						OutputMessage.delDataError.getDesc());
			}
		} else {
			setPromptMessage(response, OutputMessage.delDataError.getCode(),
					"请求参数异常!!!");
		}
		return response;
	}

	/** -----增加预排活动组----- **/
	@RequestMapping(value = "courseArrange/addActivityWalkthroughGroup", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject addActivityWalkthroughGroup(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String timetableId = request.getString("timetableId");
		String groupName = request.getString("researchGroupName");	
		String promptMessage = OutputMessage.updateDataError.getDesc();
		boolean isRightParam = false;
		if(StringUtils.isEmpty(groupName)){
			promptMessage = "请选择活动组名称!!!";
		}else if (StringUtils.isEmpty(timetableId)) {
			promptMessage = "请求参数异常!!!";
		} else {
			isRightParam = true;
		}
		if (isRightParam) {
			request.put("schoolId", getXxdm(req));
			request.put("teacherGroupName", groupName);
			String researchId = UUIDUtil.getUUID();
			request.put("researchId", researchId);
			int result = timetableService.addActivityWalkthroughGroup(request);
			if (result >= 0) {
				JSONObject data = new JSONObject();
				data.put("researchId", researchId);
				response.put("data", data);
				/** ---微调校验--- **/
				if (result > 0){
					setPromptMessage(response,"20","需要微调校验");
				}else{
					setPromptMessage(response,
							OutputMessage.addSuccess.getCode(),
							OutputMessage.addSuccess.getDesc());
				}
				clearProgDataByKey( req.getSession().getId(),timetableId,null);
			}
		} else {
			setPromptMessage(response, OutputMessage.addDataError.getCode(),
					promptMessage);
		}
		return response;
	}

	/** -----删除预排活动组----- **/
	@RequestMapping(value = "courseArrange/deleteActivityWalkthroughGroup", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject deleteActivityWalkthroughGroup(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String timetableId = request.getString("timetableId");
		String researchId = request.getString("researchId");
		if (StringUtils.isNotEmpty(timetableId)
				&& StringUtils.isNotEmpty(researchId)) {
			request.put("schoolId", getXxdm(req));
			timetableService
					.deleteActivityWalkthroughGroup(request);
			setPromptMessage(response,
					OutputMessage.delSuccess.getCode(),
					OutputMessage.delSuccess.getDesc());
			clearProgDataByKey( req.getSession().getId(),timetableId,null);
		} else {
			setPromptMessage(response, OutputMessage.delDataError.getCode(),
					"请求参数异常!!!");
		}
		return response;
	}

	/** -----编辑预排活动组成员----- **/
	@RequestMapping(value = "courseArrange/updateActivityWalkthroughMember", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject updateActivityWalkthroughMember(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String timetableId = request.getString("timetableId");
		String researchId = request.getString("researchId");
		if (StringUtils.isNotEmpty(timetableId)
				&& StringUtils.isNotEmpty(researchId)) {
			request.put("schoolId", getXxdm(req));
			int result = timetableService
					.updateActivityWalkthroughMember(request);
			if (result > 0) {
				/** ---微调校验--- **/
				setPromptMessage(response,"20","需要微调校验");
			} else {
				setPromptMessage(response,
						OutputMessage.updateSuccess.getCode(),
						OutputMessage.updateSuccess.getDesc());
			}
			clearProgDataByKey( req.getSession().getId(),timetableId,null);
		} else {
			setPromptMessage(response, OutputMessage.updateDataError.getCode(),
					"请求参数异常!!!");
		}
		return response;
	}

	/** -----查询已安排的活动列表----- **/
	@RequestMapping(value = "courseArrange/getActivityWalkthrough", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getActivityWalkthrough(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String timetableId = request.getString("timetableId");
		String termInfo = req.getParameter("selectedSemester");
		if (StringUtils.isNotEmpty(timetableId)) {
			request.put("schoolId", getXxdm(req));
			request.put("school", this.getSchool(req,termInfo));
			JSONObject walkthrough = timetableService
					.getActivityWalkthrough(request);
			response.put("data", walkthrough);
			setPromptMessage(response, OutputMessage.querySuccess.getCode(),
					OutputMessage.querySuccess.getDesc());
		} else {
			setPromptMessage(response, OutputMessage.queryDataError.getCode(),
					"请求参数异常!!!");
		}
		return response;
	}

	/** -----编辑活动预排信息----- **/
	@RequestMapping(value = "courseArrange/updateActivityWalkthrough", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject updateActivityWalkthrough(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String timetableId = request.getString("timetableId");
		String researchId = request.getString("researchId");
		if (StringUtils.isNotEmpty(timetableId)
				&& StringUtils.isNotEmpty(researchId)) {
			request.put("schoolId", getXxdm(req));
			int result = timetableService.updateActivityWalkthrough(request);
			if (result > 0) {
				/** ---微调校验--- **/
				setPromptMessage(response,"20","需要微调校验");
			} else {
				setPromptMessage(response,
						OutputMessage.updateSuccess.getCode(),
						OutputMessage.updateSuccess.getDesc());
			}
			clearProgDataByKey( req.getSession().getId(),timetableId,null);
		} else {
			setPromptMessage(response, OutputMessage.updateDataError.getCode(),
					"请求参数异常!!!");
		}
		return response;
	}

	/** -----查看已安排的活动详情----- **/
	@RequestMapping(value = "courseArrange/getScheduledActivity", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getScheduledActivity(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String timetableId = request.getString("timetableId");
		String researchId = request.getString("researchId");
		if (StringUtils.isNotEmpty(timetableId)
				&& StringUtils.isNotEmpty(researchId)) {
			request.put("schoolId", getXxdm(req));
			response = timetableService.getScheduledActivity(request);
			if (!response.isEmpty()) {
				setPromptMessage(response,
						OutputMessage.querySuccess.getCode(),
						OutputMessage.querySuccess.getDesc());
			} else {
				setPromptMessage(response, OutputMessage.success.getCode(),
						"未查询到相关数据!!!");
			}
		} else {
			setPromptMessage(response, OutputMessage.queryDataError.getCode(),
					"请求参数异常!!!");
		}
		return response;
	}

	/** -----移除活动预排信息----- **/
	@RequestMapping(value = "courseArrange/deleteActivityWalkthrough", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject deleteActivityWalkthrough(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String researchId = request.getString("researchId");
		String timetableId = request.getString("timetableId");
		if (StringUtils.isNotEmpty(timetableId)
				&& StringUtils.isNotEmpty(researchId)) {
			request.put("schoolId", getXxdm(req));
			timetableService.deleteActivityWalkthrough(request);
			setPromptMessage(response,
					OutputMessage.delSuccess.getCode(),
					OutputMessage.delSuccess.getDesc());
			clearProgDataByKey( req.getSession().getId(),timetableId,null);
		} else {
			setPromptMessage(response, OutputMessage.delDataError.getCode(),
					"请求参数异常!!!");
		}
		return response;
	}
	
	/** -----编辑活动预排信息----- **/
	@RequestMapping(value = "courseArrange/updateActivityWalkthroughGroup", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject updateActivityWalkthroughGroup(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String timetableId = request.getString("timetableId");
		String researchId = request.getString("researchId");
        if (StringUtils.isEmpty(researchId)){
        	researchId = UUIDUtil.getUUID();
        	request.put("researchId", researchId);
        }  	
		if (StringUtils.isNotEmpty(timetableId)) {
			request.put("schoolId", getXxdm(req));
			int result = timetableService.updateActivityWalkthroughGroup(request);
			if (result > 0) {
				/** ---微调校验--- **/
				setPromptMessage(response,"20","需要微调校验");
			} else {
				setPromptMessage(response,
						OutputMessage.updateSuccess.getCode(),
						OutputMessage.updateSuccess.getDesc());
			}
			response.put("researchId", researchId);
			clearProgDataByKey( req.getSession().getId(),timetableId,null);
		} else {
			setPromptMessage(response, OutputMessage.updateDataError.getCode(),
					"请求参数异常!!!");
		}
		return response;
	}
	
	/** -----查询课表设置----- */
	@RequestMapping(value = "lookup/getTimetablePrintSet", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getTimetablePrintSet(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String timetableId = request.getString("timetableId");
		String type = request.getString("type");
		if (StringUtils.isNotEmpty(timetableId) && 
				StringUtils.isNotEmpty(type)) {
			request.put("schoolId", getXxdm(req));
			JSONObject printInfor = timetableService
					.getTimetablePrintSet(request);
			response.put("data", printInfor);
			setPromptMessage(response,
					OutputMessage.querySuccess.getCode(),
					OutputMessage.querySuccess.getDesc());			
		} else {
			setPromptMessage(response, OutputMessage.queryDataError.getCode(),
					"请求参数异常!!!");
		}
		return response;
	}

	/** -----更新课表设置----- */
	@RequestMapping(value = "lookup/updateTimetablePrintSet", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject updateTimetablePrintSet(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		String timetableId = request.getString("timetableId");
		String type = request.getString("type");
		if (StringUtils.isNotEmpty(timetableId) && 
				StringUtils.isNotEmpty(type)) {
			request.put("schoolId", getXxdm(req));
			int result = timetableService.updateTimetablePrintSet(request);
			if (result > 0){
				setPromptMessage(response, OutputMessage.updateSuccess.getCode(),
						OutputMessage.updateSuccess.getDesc());
			}else{
				setPromptMessage(response, OutputMessage.updateDataError.getCode(),
						"更新打印设置失败!!!");
			}		
		} else {
			setPromptMessage(response, OutputMessage.updateDataError.getCode(),
					"请求参数异常!!!");
		}
		return response;
	}

	
	private void clearProgDataByKey(  String sessionID, String timetableId, String keyName) {
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
			e.printStackTrace();
		}
	}
}