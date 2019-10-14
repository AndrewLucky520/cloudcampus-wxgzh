package com.talkweb.timetable.action;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
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

import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.Classroom;
import com.talkweb.accountcenter.thrift.School;
import com.talkweb.accountcenter.thrift.T_Role;
import com.talkweb.accountcenter.thrift.User;
import com.talkweb.base.common.OutputMessage;
import com.talkweb.common.action.BaseAction;
import com.talkweb.common.tools.BeanTool;
import com.talkweb.common.tools.GrepUtil;
import com.talkweb.common.tools.StringUtil;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.scoreManage.action.ScoreUtil;
import com.talkweb.timetable.arrangement.service.SmartArrangeService;
import com.talkweb.timetable.arrangement.service.impl.TimetableUtil;
import com.talkweb.timetable.bo.GradeSetBo;
import com.talkweb.timetable.bo.TimetableGradeSetBO;
import com.talkweb.timetable.service.TimetableService;
/**
 * @author 
 *
 *2015年11月10日
 */
@Controller
@RequestMapping(value = "/timetableManage/")

public class TimetablAppAction extends BaseAction {
	
	
	private static final Logger logger = LoggerFactory.getLogger(TimetablAppAction.class);
	/**
	 * 获取配置文件中constant.properties当前学年学期的值
	 */
	@Value("#{settings['currentTermInfo']}")
	private String currentTermInfo;
	
	@Autowired
	private TimetableService timetableService;
	
	@Autowired
	private AllCommonDataService commonDataService;
	
	@Autowired
	private SmartArrangeService	smtService;

	private void setPromptMessage(JSONObject object, String code, String message) {
		object.put("code", code);
		object.put("msg", message);
	}
	
	/**
	 * 学生课表, 	1、正常显示  2、当前身份没有这次数据  3、当前数据已删除   4、当前数据已结束  5.当前数据未开始  
	 * @author XFQ
	 * @param req
	 * @param res
	 * @param reqData
	 * @return
	*/
	@RequestMapping(value = "lookup/getStudentTimetableByDay", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getStudentTimetableByDay(HttpServletRequest req,
			HttpServletResponse res, @RequestBody JSONObject data) {
		JSONObject response = new JSONObject();
		String userId = data.getString("userId");
		long schoolId = data.getLongValue("schoolId");
		Long uid = Long.parseLong(userId);
		long d1 = new Date().getTime();
		User user = commonDataService.getUserById(schoolId,uid);
		if(user==null||user.getUserPart()==null||user.getUserPart().getRole()==null){
			setPromptMessage(response, "2", "未查询到用户!!!");
	        return response;
		}
		School sch = commonDataService.getSchoolByUserId(schoolId,uid);
		String xnxq = commonDataService.getCurrentXnxq(sch);
		if(sch==null){
			setPromptMessage(response, "2", "未查询到归属学校!!!");
	        return response;
		}
		String classId = "";
		if(user.getUserPart().getRole().equals(T_Role.Parent)){
			if(user.getParentPart()!=null&&user.getParentPart().getClassId()!=0){
				classId = ""+user.getParentPart().getClassId();
			}
		}else if(user.getUserPart().getRole().equals(T_Role.Student)){
			if(user.getStudentPart()!=null&&user.getStudentPart().getClassId()!=0){
				classId = ""+user.getStudentPart().getClassId();
			}
		}else{
			setPromptMessage(response, "2", "用户角色错误 !!!");
	        return response;
		}
		
		if(classId.length()>0){
			JSONObject timetable = timetableService.getTimetableForAppClass(sch.getId(),xnxq,classId);
			
			if(timetable!=null && timetable.getString("TimetableId") !=null){
				// 判断改数据是否取消发布
				if(timetable.getIntValue("Published") == 1) {
					setPromptMessage(response, "4", "当前数据已结束");
			        return response;
				}
				
				String timetableId = timetable.getString("TimetableId");
				
				List<JSONObject> timetableList = smtService.getClssCurrentTimeTable(timetable,classId,xnxq,sch );
				if(timetableList!=null&&timetableList.size()>0){
					
					//加入节次起止时间
					HashMap<String,Object> reqMap = new HashMap<String, Object>();
					reqMap.put("schoolId",schoolId);
					reqMap.put("timetableId", timetableId);
					List<JSONObject> gradeList = timetableService.getTimetableGradeList(reqMap);
					
					if (null!=gradeList && gradeList.size() > 0) {
						
						List<String> gradeIdList = new ArrayList<String>();
						if (gradeList!=null) {
							for (int i = 0; i < gradeList.size(); i++) {
								gradeIdList.add(gradeList.get(i).getString("gradeId"));
							}
						}
						Map<String,ArrayList<JSONObject>> scheduleMap = getTimetableScheduleMap(gradeIdList, xnxq, timetableId);
						
				
					   String gradeId = null;
					   List<JSONObject> copyScheduleTime = new ArrayList<JSONObject>();
					   List<JSONObject> scheduleTime =null;
					   List<JSONObject> tempScheduleTime =null;
					   for (String gid : gradeIdList) {
						   
						  gradeId =  gid;
						  tempScheduleTime = scheduleMap.get(gradeId);
						  if (tempScheduleTime==null) {
							 continue;
						  }
						  if (scheduleTime==null) {
							  scheduleTime = tempScheduleTime;
						  }else{
							  JSONObject jObject = null;
							  JSONObject jtObject = null;
							  for (int i = 0; i < scheduleTime.size(); i++) {
								   jObject = scheduleTime.get(i);
								   if ("0".equals(jObject.getString("sectionId"))) {
									 break;
								   }
							  }
							  for (int i = 0; i < tempScheduleTime.size(); i++) {
								  jtObject = tempScheduleTime.get(i);
								   if ("0".equals(jtObject.getString("sectionId"))) {
									 break;
								   }
							  }
							  DateFormat df = new SimpleDateFormat("HH:mm");
							  try {
								  Date dt1 = df.parse(jObject.getString("startTime"));//将字符串转换为date类型  
						          Date dt2 = df.parse(jtObject.getString("startTime"));
						          if (dt1.getTime()> dt2.getTime()) {
						        	  scheduleTime = tempScheduleTime;
								  }
							  } catch (Exception e) {
								  logger.info(e.getMessage());
							  }
						  }
					 }
					  if (scheduleTime!=null) {
						  for(JSONObject obj : scheduleTime){
								copyScheduleTime.add(BeanTool.castBeanToFirstLowerKey(obj));
						  }
						  response.put("scheduleTime", copyScheduleTime);
					  }
					}
					
					response.put("data",timetableList);
					JSONObject timetableObj = timetableList.get(0);
					response.put("totalMaxDays", timetableObj.getString("totalMaxDays"));
					response.put("totalMaxLessons", timetableObj.getString("totalMaxLessons"));
					setPromptMessage(response,
							OutputMessage.querySuccess.getCode(),
							OutputMessage.querySuccess.getDesc());
				}else{
					setPromptMessage(response, "3", "当前数据已删除");
				}
			}else{
				setPromptMessage(response, "3", "当前数据已删除");
			}
		}else{
			setPromptMessage(response, "2", "当前身份没有这次数据");
		}
		return response;
	}
	
	/** -----查看班级列表----- **/
	@RequestMapping(value = "lookup/getAppClassList", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getAppClassList(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res) {
		JSONObject response = new JSONObject();
		long schoolId = request.getLongValue("schoolId");
		String userId = request.getString("userId");
		Long uid = Long.parseLong(userId);
		User user = commonDataService.getUserById(schoolId,uid);
		if(user==null||user.getUserPart()==null||user.getUserPart().getRole()==null){
			setPromptMessage(response, OutputMessage.queryDataError.getCode(), "未查询到用户!!!");
	        return response;
		}
		School sch = commonDataService.getSchoolByUserId(schoolId,uid);
		String xnxq = commonDataService.getCurrentXnxq(sch);
		if(sch==null){
			setPromptMessage(response, OutputMessage.queryDataError.getCode(), "未查询到归属学校!!!");
	        return response;
		}
		String teacherId = "";
		if(user.getUserPart().getRole().equals(T_Role.Teacher)
				||user.getUserPart().getRole().equals(T_Role.SchoolManager)
				||user.getUserPart().getRole().equals(T_Role.Staff)
				||user.getUserPart().getRole().equals(T_Role.SystemManager)
				){
			teacherId = user.getAccountPart().getId()+"";
		}else{
			setPromptMessage(response, OutputMessage.queryDataError.getCode(), "用户角色错误 !!!");
	        return response;
		}
		JSONObject timetable = timetableService.getTimetableForAppClassList(sch.getId(),xnxq,teacherId,true ,null);
		if (timetable!=null&&timetable.containsKey("classes"))  {
			String classes = timetable.getString("classes");
			List<Long> cids = new ArrayList<Long>();
			List<JSONObject> dataList = new ArrayList<JSONObject>();
			String[] cls = classes.split(",");
			for(int i=0;i<cls.length;i++){
				if(cls[i].trim().length()>0&&cls[i]!=null){
					long dc = Long.parseLong(cls[i]);
					if(dc!=0){
						cids.add(dc);
					}
				}
			}
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
			cxMap.put("schoolId", sch.getId()+"");
			cxMap.put("xnxq", xnxq);
			cxMap.put("teacherId", teacherId);
			cxMap.put("timetableId", timetable.get("TimetableId"));
			if(week!=0){
				cxMap .put("week", week);
				List<JSONObject> tmpAjst = timetableService
						.getTemporaryAjustList(cxMap);
				
				for(JSONObject ajs:tmpAjst){
					String Ts = ajs.getString("Teachers");
					Long classId = Long.parseLong(ajs.getString("ClassId"));
					if(TimetableUtil.isStrInArray(teacherId, Ts.split(",")) ){
						if(!cids.contains(classId)){
							cids.add(classId);
						}
					}
				}
				
			}
			if(cids.size()>0){
				List<Classroom> list = commonDataService.getSimpleClassBatch(sch.getId(), cids,xnxq);
				boolean chinese = false;
				String className = null;
				for(Classroom clr:list){
					if(clr!=null&&clr.getId()!=0){
						//Grade gd = commonDataService.getGradeById(sch.getId(), clr.getGradeId(),xnxq);
						//String gradeName = AccountStructConstants.T_GradeLevelName.get(gd.getCurrentLevel());
						JSONObject obj = new JSONObject();
						obj.put("classId", clr.getId());
						obj.put("className", clr.getClassName());// gradeName+clr.getClassName() 邓晶不加年级名
					    className =  TimetableUtil.filterUnNumber(clr.getClassName());
					    if (StringUtil.isEmpty(className)) {
					    	chinese = true;
						}
						obj.put("idx", className);
						dataList.add(obj);
					}
				}
				if (!chinese) { // 全汉字的不需要排序
					ScoreUtil.sorStuScoreList(dataList, "idx", "asc", "", "");
				}
				
			}
			
			response.put("data",dataList);
			setPromptMessage(response, OutputMessage.querySuccess.getCode(),OutputMessage.querySuccess.getDesc());
			
		} else {
			setPromptMessage(response, OutputMessage.queryDataError.getCode(),
					"未查询到已发布的课表!!!");
		}
		return response;
	}
	
	
	/**
	 * 获取时间设置Map对象
	 * @param gradeIdList
	 * @param termInfoId
	 * @param timetableId
	 * @return {String:gradeId,{String:sectionId,String:sectionTime}}
	 */
	private Map<String,ArrayList<JSONObject>> getTimetableScheduleMap(List<String> gradeIdList,String termInfoId,String timetableId){
		
		Map<String,ArrayList<JSONObject>> gradeMapScheduleTime = new HashMap<String,ArrayList<JSONObject>>();
		for(String gradeId : gradeIdList){
			ArrayList<JSONObject> scheduleTime = new ArrayList<JSONObject>();
			String schoolYear = termInfoId.substring(0, 4);
			String pycc=commonDataService.getPYCCBySYNJ(gradeId, schoolYear);
			List<HashMap<String,Object>> timetableScheduleList = timetableService.getTimetableSchedule(timetableId, pycc);	
			
			HashMap<String,String> schedule = new HashMap<String,String>();
			for(HashMap<String,Object> object : timetableScheduleList){
				JSONObject time = new JSONObject();
				time.put("sectionId", object.get("sectionId"));
				time.put("startTime", object.get("startTime"));
				time.put("endTime", object.get("endTime"));
				scheduleTime.add(time);
				
				StringBuffer sb = new StringBuffer();
				String sectionId = (String)object.get("sectionId");
				sb.append(object.get("startTime"));
				sb.append(" - " + object.get("endTime"));
				schedule.put(sectionId, sb.toString());					
			}
			gradeMapScheduleTime.put(gradeId, scheduleTime);
		}
		return gradeMapScheduleTime;//timetableScheduleMap;
	}
	
	/**
	 * 老师课表
	 * @author XFQ
	 * @param req
	 * @param res
	 * @param reqData
	 * @return
	*/
	@RequestMapping(value = "lookup/getTeacherTimetableByDay", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getTeacherTimetableByDay(HttpServletRequest req,
			HttpServletResponse res, @RequestBody JSONObject data) {


		JSONObject response = new JSONObject();
		String userId = data.getString("userId");
		long schoolId = data.getLongValue("schoolId");
		Long uid = Long.parseLong(userId);
		User user = commonDataService.getUserById(schoolId,uid);
		if(user==null||user.getUserPart()==null||user.getUserPart().getRole()==null){
			setPromptMessage(response, OutputMessage.queryDataError.getCode(), "未查询到用户!!!");
	        return response;
		}
		School sch = commonDataService.getSchoolByUserId(schoolId,uid);
		String xnxq = commonDataService.getCurrentXnxq(sch);
		if(sch==null){
			setPromptMessage(response, OutputMessage.queryDataError.getCode(), "未查询到归属学校!!!");
	        return response;
		}
		if(user.getUserPart().getRole().equals(T_Role.Teacher)
				||user.getUserPart().getRole().equals(T_Role.SchoolManager)
				||user.getUserPart().getRole().equals(T_Role.Staff)
				||user.getUserPart().getRole().equals(T_Role.SystemManager)
				){
		}else{
			setPromptMessage(response, OutputMessage.queryDataError.getCode(), "用户角色错误 !!!");
	        return response;
		}
		String teacherId = user.getAccountPart().getId()+"";
		if(userId.length()>0){
			JSONObject timetable = timetableService.getTimetableForAppClassList(sch.getId(), xnxq,
					teacherId, true , null);

			if(timetable!=null&&timetable.getString("TimetableId")!=null){
				
				String timetableId = timetable.getString("TimetableId");
				
				List<JSONObject> timetableList = smtService.getTeaCurrentTimeTable(timetable,teacherId,xnxq,sch );
				
				if(timetableList!=null&&timetableList.size()>0){
					
					//加入节次起止时间
					HashMap<String,Object> reqMap = new HashMap<String, Object>();
					reqMap.put("schoolId",schoolId);
					reqMap.put("timetableId", timetableId);
					List<JSONObject> gradeList = timetableService.getTimetableGradeList(reqMap);
					
					if (null!=gradeList && gradeList.size() > 0) {
						
						List<String> gradeIdList = new ArrayList<String>();
						if (gradeList!=null) {
							for (int i = 0; i < gradeList.size(); i++) {
								gradeIdList.add(gradeList.get(i).getString("gradeId"));
							}
						}
						Map<String,ArrayList<JSONObject>> scheduleMap = getTimetableScheduleMap(gradeIdList, xnxq, timetableId);
						
				
					   String gradeId = null;
					   List<JSONObject> copyScheduleTime = new ArrayList<JSONObject>();
					   List<JSONObject> scheduleTime =null;
					   List<JSONObject> tempScheduleTime =null;
					   for (String gid : gradeIdList) {
						   
						  gradeId =  gid;
						  tempScheduleTime = scheduleMap.get(gradeId);
						  if (tempScheduleTime==null) {
							 continue;
						  }
						  if (scheduleTime==null) {
							  scheduleTime = tempScheduleTime;
						  }else{
							  JSONObject jObject = null;
							  JSONObject jtObject = null;
							  for (int i = 0; i < scheduleTime.size(); i++) {
								   jObject = scheduleTime.get(i);
								   if ("0".equals(jObject.getString("sectionId"))) {
									 break;
								   }
							  }
							  for (int i = 0; i < tempScheduleTime.size(); i++) {
								  jtObject = tempScheduleTime.get(i);
								   if ("0".equals(jtObject.getString("sectionId"))) {
									 break;
								   }
							  }
							  DateFormat df = new SimpleDateFormat("HH:mm");
							  try {
								  Date dt1 = df.parse(jObject.getString("startTime"));//将字符串转换为date类型  
						          Date dt2 = df.parse(jtObject.getString("startTime"));
						          if (dt1.getTime()> dt2.getTime()) {
						        	  scheduleTime = tempScheduleTime;
								  }
							  } catch (Exception e) {
								  logger.info(e.getMessage());
							  }
						  }
					 }
					  if (scheduleTime!=null) {
						  for(JSONObject obj : scheduleTime){
								copyScheduleTime.add(BeanTool.castBeanToFirstLowerKey(obj));
						  }
						  response.put("scheduleTime", copyScheduleTime);
					  }
					}
					
					response.put("data",timetableList);
					
					//查询当前课表最大周次和节次
					TimetableGradeSetBO gradeSet = timetableService.getTimetableGradeSetById(timetableId);
					int totalMaxDays = 0;
					int totalMaxLessons = 0;
					if(null != gradeSet) {
						totalMaxDays = gradeSet.getMaxDaysForWeek();
						for (GradeSetBo gradeSetBo : gradeSet.getGradeSets()) {
							int num = gradeSetBo.getAmLessonNum()+gradeSetBo.getPmLessonNum();
							if(num > totalMaxLessons) totalMaxLessons = num;
						}
						response.put("totalMaxDays", String.valueOf(totalMaxDays));
						response.put("totalMaxLessons", String.valueOf(totalMaxLessons));
					}else {
						logger.warn("课表:{}年级设置数据为空", timetableId);
						JSONObject timetableObj = timetableList.get(0);
						response.put("totalMaxDays", timetableObj.getString("totalMaxDays"));
						response.put("totalMaxLessons", timetableObj.getString("totalMaxLessons"));
					}
					setPromptMessage(response,
							OutputMessage.querySuccess.getCode(),
							OutputMessage.querySuccess.getDesc());
				}else{
					setPromptMessage(response, OutputMessage.queryDataError.getCode(), "未查询到已发布课表 !!!");
				}
			}else{
				setPromptMessage(response, OutputMessage.queryDataError.getCode(), "未查询到已发布课表 !!!");
			}
		}else{
			setPromptMessage(response, OutputMessage.queryDataError.getCode(), "请求参数异常 !!!");
		}
		return response;
	}
	
	/**
	 * ZHXX-478 智慧校教师端课表小程序接口
	 * @return
	 */
	@RequestMapping(value = "lookup/getTeacherTimetableByDayToThrid", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getTeacherTimetableByDayToThrid(HttpServletRequest req,HttpServletResponse res, @RequestBody JSONObject data) {
		JSONObject response = new JSONObject();
		String extUserId = data.getString("accountId");//使用者ID	基础平台id
		String extSchoolId = data.getString("schoolId");//学校Id，基础平台id
		String schoolId = commonDataService.getSchoolIdByExtId(extSchoolId, currentTermInfo);
		if(StringUtils.isEmpty(schoolId)) {
			setPromptMessage(response, OutputMessage.queryDataError.getCode(), "未查询到学校信息 !!!");
			return response;
		} else {
			data.put("schoolId", schoolId);
		}
		
		List<JSONObject> userJsonData = commonDataService.getUserIdByExtId(extUserId, currentTermInfo);
		
		if(CollectionUtils.isEmpty(userJsonData)) {
			setPromptMessage(response, OutputMessage.queryDataError.getCode(), "未查询到用户信息 !!!");
			return response;
		} else {
			JSONObject userData = userJsonData.get(0);
			if(userData != null) {
				String userId = userData.getString("userId");
				data.put("userId", userId);
			}
		}
		
		if(userJsonData != null && userJsonData.size() > 0 && userJsonData.get(0) != null) {
			JSONObject userData = userJsonData.get(0);
			String userId = userData.getString("userId");
			data.put("userId", userId);
		} else {
			setPromptMessage(response, OutputMessage.queryDataError.getCode(), "未查询到用户信息 !!!");
			return response;
		}
		return getTeacherTimetableByDay(req,res,data);
	}
	
	/** 
	 * 班级课表
	 * @author XFQ
	 * @param req
	 * @param res
	 * @param reqData
	 * @return
	*/
	@RequestMapping(value = "lookup/getClassTimetableByDay", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getClassTimetable(HttpServletRequest req,
			HttpServletResponse res, @RequestBody JSONObject data) {

		
		JSONObject response = new JSONObject();
		String userId = data.getString("userId");
		long schoolId = data.getLongValue("schoolId");
		 String teacherid = "";
		if(!data.containsKey("classId")||data.getString("classId")==null){
			setPromptMessage(response, OutputMessage.queryDataError.getCode(), "请选择所教班级!!!");
			return response;
		}
		
		School sch = null;

		if (StringUtils.isBlank(userId)) {
			String termInfoId =  commonDataService.getCurTermInfoId(schoolId);
			System.out.println("======1> " + termInfoId);
			sch = commonDataService.getSchoolById(schoolId, termInfoId);
		}else {
			Long uid = Long.parseLong(userId);
			User user = commonDataService.getUserById(schoolId,uid);
			if(user==null||user.getUserPart()==null||user.getUserPart().getRole()==null){
				setPromptMessage(response, OutputMessage.queryDataError.getCode(), "未查询到用户!!!");
		        return response;
			}
			sch = commonDataService.getSchoolByUserId(schoolId,uid);
				if(user.getUserPart().getRole().equals(T_Role.Teacher)
						||user.getUserPart().getRole().equals(T_Role.SchoolManager)
						||user.getUserPart().getRole().equals(T_Role.Staff)
						||user.getUserPart().getRole().equals(T_Role.SystemManager)
						){
					teacherid = user.getAccountPart().getId()+"";
				}else{
					setPromptMessage(response, OutputMessage.queryDataError.getCode(), "用户角色错误 !!!");
			        return response;
				}
				
		}
 
		String xnxq = commonDataService.getCurrentXnxq(sch);
		if(sch==null){
			setPromptMessage(response, OutputMessage.queryDataError.getCode(), "未查询到归属学校!!!");
	        return response;
		}

		String classId = data.getString("classId");
		if(classId.length()>0){
			JSONObject timetable = timetableService.getTimetableForAppClassList(sch.getId(), xnxq,
					teacherid, false  , classId);
			
			if(timetable!=null&&timetable.getString("TimetableId")!=null){
				
				String timetableId  =timetable.getString("TimetableId");
				List<JSONObject> timetableList = smtService.getClssCurrentTimeTable(timetable,classId,xnxq,sch );
				if(timetableList!=null&&timetableList.size()>0){
					response.put("data",timetableList);
					
					//查询当前课表最大周次和节次
					TimetableGradeSetBO gradeSet = timetableService.getTimetableGradeSetById(timetableId);
					int totalMaxDays = 0;
					int totalMaxLessons = 0;
					if(null != gradeSet) {
						totalMaxDays = gradeSet.getMaxDaysForWeek();
						for (GradeSetBo gradeSetBo : gradeSet.getGradeSets()) {
							int num = gradeSetBo.getAmLessonNum()+gradeSetBo.getPmLessonNum();
							if(num > totalMaxLessons) totalMaxLessons = num;
						}
						response.put("totalMaxDays", String.valueOf(totalMaxDays));
						response.put("totalMaxLessons", String.valueOf(totalMaxLessons));
					}else {
						logger.warn("课表:{}年级设置数据为空", timetableId);
						JSONObject timetableObj = timetableList.get(0);
						response.put("totalMaxDays", timetableObj.getString("totalMaxDays"));
						response.put("totalMaxLessons", timetableObj.getString("totalMaxLessons"));
					}
					
					setPromptMessage(response,
							OutputMessage.querySuccess.getCode(),
							OutputMessage.querySuccess.getDesc());
				}else{
					setPromptMessage(response, OutputMessage.queryDataError.getCode(), "未查询到已发布课表 !!!");
				}
			}else{
				setPromptMessage(response, OutputMessage.queryDataError.getCode(), "未查询到已发布课表 !!!");
			}
		}else{
			setPromptMessage(response, OutputMessage.queryDataError.getCode(), "未查询到归属班级 !!!");
		}
		return response;
	}

}