package com.talkweb.onecard.action;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.Classroom;
import com.talkweb.accountcenter.thrift.Course;
import com.talkweb.accountcenter.thrift.T_Gender;
import com.talkweb.accountcenter.thrift.T_Role;
import com.talkweb.accountcenter.thrift.User;
import com.talkweb.common.action.BaseAction;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.onecard.service.OneCardService;
/**
 * @ClassName OneCardAction.java
 * @author liboqi
 * @version 1.0
 * @Description 创建的原因_Service
 * @date 2016年3月18日 上午11:06:24
 */
@Controller
@RequestMapping(value = "/oneCard/")
public class OneCardAction extends BaseAction{
	
	Logger logger = LoggerFactory.getLogger(OneCardAction.class);
	
	@Autowired
	private AllCommonDataService commonDataService;
	@Autowired
	private OneCardService oneCardService;
	
	
	/****
	 * 学生家长当前考勤情况
	 * @param req
	 * @param request
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "attendance/studentToday",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject queryBasicData(HttpServletRequest req,@RequestBody JSONObject request, HttpServletResponse res){
		JSONObject returnJson = new JSONObject();
		String userId = request.getString("userId");
		String schoolId = request.getString("schoolId");
		if(StringUtils.isEmpty(userId) || StringUtils.isEmpty(schoolId)){
			returnJson.put("code", -1);
			returnJson.put("msg", "参数为空！");
			return returnJson;
		}
		User user = commonDataService.getUserById(Long.valueOf(schoolId), Long.valueOf(userId));
		if(user == null){
			returnJson.put("code", -1);
			returnJson.put("msg", "用户为空！");
			return returnJson;
		}
		JSONObject jsonObject = new JSONObject();
		
		T_Role role = T_Role.findByValue(user.getUserPart().role.getValue());
		if(T_Role.Parent.getValue() == role.getValue()){ //如果是家长
			//获取家长的学生的信息
			userId = String.valueOf(user.getParentPart().getStudentId());
			user = commonDataService.getUserById(Long.valueOf(schoolId), Long.valueOf(userId));
		}
		if(T_Role.Teacher.getValue() != role.getValue()){  //如果是老师，不需要班级信息
			String curTermInfoId = commonDataService.getCurTermInfoId(Long.valueOf(schoolId));
			Classroom classInfo = commonDataService.getClassById(Long.valueOf(schoolId), user.getStudentPart().getClassId(), curTermInfoId);
			jsonObject.put("className", classInfo.getClassName());
			T_Gender gender = user.getAccountPart().getGender();
			if(gender != null)
				jsonObject.put("gender",gender.getValue() == 1 ? "男" : "女");
		}
		
		jsonObject.put("avatar", user.getUserPart().getAvatar());
		jsonObject.put("weekday", getStrings(new Date()));
		jsonObject.put("userName", user.getAccountPart().getName());
		List<JSONObject> attendanceList = new ArrayList<JSONObject>();
		JSONObject jsonObject2 = new JSONObject();
		jsonObject2.put("userId", userId);
		jsonObject2.put("schoolId", schoolId);
		String date = getDate();
		jsonObject2.put("tableName", date);
		jsonObject2.put("date", date);
		try {
			List<JSONObject> oneCard = oneCardService.queryOneCardByUserId(jsonObject2);
			if(CollectionUtils.isNotEmpty(oneCard)){
				for (JSONObject jsonObject3 : oneCard) {
					JSONObject jsonObject4 = new JSONObject();
					jsonObject4.put("time", getStringOfTime(jsonObject3.getDate("swingCardTime")));
					jsonObject4.put("comingSchool", getAttType(jsonObject3.getInteger("attendanceType"),jsonObject3.getString("idType")));
					jsonObject4.put("attendanceType", jsonObject3.getInteger("attendanceType"));
					jsonObject4.put("attendanceFlag", jsonObject3.getInteger("attendanceFlag"));
					jsonObject4.put("exception", getAttendanceFlag(jsonObject3.getInteger("attendanceFlag")));
					attendanceList.add(jsonObject4);
				}
			}
		} catch (Exception e) {
			logger.info(e.getMessage());
		}
		jsonObject.put("attendanceList", attendanceList);
		returnJson.put("code", 1);
		returnJson.put("msg", "");
		returnJson.put("data", jsonObject);
		return returnJson;
	}
	
	/****
	 * 学生家长本学期考勤情况
	 * @param req
	 * @param request
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "attendance/studentCurrentTerm",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject studentCurrentTerm(HttpServletRequest req,
			@RequestBody JSONObject request, HttpServletResponse res){
		JSONObject returnJson = new JSONObject();
		String userId = request.getString("userId");
		String schoolId = request.getString("schoolId");
		if(StringUtils.isEmpty(userId) || StringUtils.isEmpty(schoolId)){
			returnJson.put("code", -1);
			returnJson.put("msg", "参数为空！");
			return returnJson;
		}
		User user = commonDataService.getUserById(Long.valueOf(schoolId), Long.valueOf(userId));
		if(user == null){
			returnJson.put("code", -1);
			returnJson.put("msg", "用户为空！");
			return returnJson;
		}
		T_Role role = T_Role.findByValue(user.getUserPart().role.getValue());
		if(T_Role.Parent.getValue() == role.getValue()){
			userId = String.valueOf(user.getParentPart().getStudentId());
			user = commonDataService.getUserById(Long.valueOf(schoolId), Long.valueOf(userId));
		}
		JSONObject jsonData = new JSONObject();
		String termInfoNum = commonDataService.getCurTermInfoId(Long.valueOf(schoolId));
		if(StringUtils.isNotEmpty(termInfoNum)){
			Map<String, Object> xnxqDate = getXnxqDate(termInfoNum);
			jsonData.put("schoolId", schoolId);
			jsonData.put("userId", userId);
			jsonData.put("startDate", xnxqDate.get("startDate"));
			jsonData.put("endDate", xnxqDate.get("endDate"));
			String date = getDate();
			jsonData.put("date", date);
			jsonData.put("tableName", "t_o_onecard_"+date);
			
			try {
				List<JSONObject> oneCardByXnxq = oneCardService.queryOneCardByXnxqOfUserId(jsonData);
				Map<String, Object> initJson = initJson();
				JSONArray dataArray = new JSONArray();
				if(CollectionUtils.isNotEmpty(oneCardByXnxq)){
					for (JSONObject jsonObject : oneCardByXnxq) {
						JSONObject jsonObject2 = new JSONObject();
						String attendanceFlag = getAttendanceFlag(jsonObject.getInteger("attendanceFlag"));
						jsonObject2.put("day", getStringsByDatePar(jsonObject.getDate("swingCardTime")));
						jsonObject2.put("week", getStringsByWeek(jsonObject.getDate("swingCardTime")));
						jsonObject2.put("time", getStringOfTime(jsonObject.getDate("swingCardTime")));
						/*exception:异常状态（早退，迟到，无打卡记录）,exceptionNumber:异常次数
						  exceptionList:异常列表[{,day:日期（11月15日）,week:星期（周一）,time:时间（08:30）}]*/
						if(!"正常".equals(attendanceFlag)){
							JSONObject object = (JSONObject)initJson.get(attendanceFlag);
							object.put("exceptionNumber", (object.getInteger("exceptionNumber") + 1));
							@SuppressWarnings("unchecked")
							ArrayList<JSONObject> object2 = (ArrayList<JSONObject>)object.get("exceptionList");
							object2.add(jsonObject2);
						}
					}
				}
				Set<Entry<String, Object>> entrySet = initJson.entrySet();
				Iterator<Entry<String, Object>> iterator = entrySet.iterator();
				while (iterator.hasNext()) {
					Map.Entry<String, Object> entry = (Map.Entry<String, Object>) iterator.next();
					dataArray.add(entry.getValue());
				}
				returnJson.put("code", 1);
				returnJson.put("msg", "");
				returnJson.put("data", dataArray);
			} catch (Exception e) {
				logger.info(e.getMessage());
			}
		}
		return returnJson;
	}
	
	
	/**
	 * 老师自己今天考勤
	 * @param req
	 * @param request
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "attendance/teacherSelfCurrentTerm",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject teacherSelfCurrentTerm(HttpServletRequest req,@RequestBody JSONObject request, HttpServletResponse res){
		return studentCurrentTerm(req, request, res);
	}
	
	/**
	 * 老师自己学期考勤
	 * @param req
	 * @param request
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "attendance/teacherSelfToday",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject teacherSelfToday(HttpServletRequest req,@RequestBody JSONObject request, HttpServletResponse res){
		return queryBasicData(req, request, res);
	}
	
	/****
	 * 老师今天考勤情况
	 * @param req
	 * @param request
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "attendance/teacherToday",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject teacherToday(HttpServletRequest req,@RequestBody JSONObject request, HttpServletResponse res){
		JSONObject returnJson = new JSONObject();
		JSONObject returnJson2 = new JSONObject();
		String userId = request.getString("userId");
		String schoolId = request.getString("schoolId");
		String classId = request.getString("classId");
		if(StringUtils.isEmpty(userId) || StringUtils.isEmpty(schoolId)){
			returnJson.put("code", -1);
			returnJson.put("msg", "参数为空！");
			return returnJson;
		}
		User user = commonDataService.getUserById(Long.valueOf(schoolId), Long.valueOf(userId));
		if(user == null){
			returnJson.put("code", -1);
			returnJson.put("msg", "用户为空！");
			return returnJson;
		}
		if(StringUtils.isNotBlank(classId)){
			Classroom classInfo = commonDataService.getClassById(Long.valueOf(schoolId), Long.valueOf(classId), 
					commonDataService.getCurTermInfoId(Long.valueOf(schoolId)));
			List<Long> studentAccountIds = classInfo.getStudentAccountIds();
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("userIds", studentAccountIds);
			String date = getDate();
			jsonObject.put("date", date);
			jsonObject.put("tableName", date);
			List<JSONObject> oneCardByTeacher = oneCardService.queryOneCardByTeacher(jsonObject);
			List<JSONObject> classExceptionList = new ArrayList<JSONObject>();
			JSONObject classException = new JSONObject();
			classException.put("classId", classInfo.getId());
			classException.put("className", classInfo.getClassName());
			String attendanceRate = "0";
			if(studentAccountIds.size() != 0){
				attendanceRate = String.format("%.2f", Double.valueOf(oneCardByTeacher.size()) / studentAccountIds.size() * 100);
			}
			classException.put("attendanceRate", attendanceRate);
			List<JSONObject> exceptionTotal = new ArrayList<JSONObject>();
			Map<String, Object> initClassJson = initClassJson();
			if(CollectionUtils.isNotEmpty(oneCardByTeacher)){
			/*exception:异常状态（早退，迟到，无打卡记录）,exceptionNumberPeople:异常人数,exceptionNumber:异常次数
			exceptionList:异常列表[{,studentName:学生姓名,time:时间（08:30）}]*/
				for (JSONObject jsonObject2 : oneCardByTeacher) {
					JSONObject jsonObject3 = new JSONObject();
					jsonObject3.put("studentName", jsonObject2.getString("userName"));
					jsonObject3.put("time", "时间：（"+getStringOfTime(jsonObject.getDate("swingCardTime"))+"）");
					String attendanceFlag = getAttendanceFlag(jsonObject2.getInteger("attendanceFlag"));
					JSONObject object = (JSONObject)initClassJson.get(attendanceFlag);
					object.put("exceptionNumberPeople", (object.getInteger("exceptionNumberPeople") + 1));
					object.put("exceptionNumber", (object.getInteger("exceptionNumber") + 1));
					@SuppressWarnings("unchecked")
					ArrayList<JSONObject> object2 = (ArrayList<JSONObject>)object.get("exceptionList");
					object2.add(jsonObject3);
				}
			}
			Set<Entry<String, Object>> entrySet = initClassJson.entrySet();
			Iterator<Entry<String, Object>> iterator = entrySet.iterator();
			while (iterator.hasNext()) {
				Map.Entry<String, Object> entry = (Map.Entry<String, Object>) iterator.next();
				exceptionTotal.add((JSONObject)entry.getValue());
			}
			classException.put("exceptionTotal", exceptionTotal);
			classExceptionList.add(classException);
			returnJson2.put("weekday", getStrings(new Date()));
			returnJson2.put("classExceptionList", classExceptionList);
			returnJson.put("data", returnJson2);
		}else{
			List<Long> classIds = new ArrayList<Long>();
			List<Course> courseIds = user.getTeacherPart().getCourseIds();
			if(CollectionUtils.isNotEmpty(courseIds)){
				for (Course course : courseIds) {
					long classId2 = course.getClassId();
					if(!classIds.contains(classId2)){
						classIds.add(classId2);
					}
				}
				List<Classroom> classroomBatch = commonDataService.getClassroomBatch(Long.valueOf(schoolId), classIds, 
						commonDataService.getCurTermInfoId(Long.valueOf(schoolId)));
				List<JSONObject> classExceptionList = new ArrayList<JSONObject>();
				if(CollectionUtils.isNotEmpty(classroomBatch)){
					for (Classroom classroom : classroomBatch) {
						List<Long> studentAccountIds = classroom.getStudentAccountIds();
						if(CollectionUtils.isEmpty(studentAccountIds)){
							studentAccountIds = new ArrayList<Long>();
						}
						JSONObject jsonObject = new JSONObject();
						jsonObject.put("accountIds", studentAccountIds);
						jsonObject.put("schoolId", schoolId);
						String date = getDate();
						jsonObject.put("date", date);
						jsonObject.put("tableName", date);
						
						try {
							List<JSONObject> oneCardByTeacher = new ArrayList<JSONObject>();
							if(CollectionUtils.isNotEmpty(studentAccountIds)){
								oneCardByTeacher = oneCardService.queryOneCardByTeacher(jsonObject);
							}
							logger.debug("teacherToday ===============>" + oneCardByTeacher);
							JSONObject classException = new JSONObject();
							classException.put("classId", classroom.getId());
							classException.put("className", classroom.getClassName());
							String attendanceRate = "0";
							if(studentAccountIds.size() != 0){
								attendanceRate = String.format("%.2f", Double.valueOf(oneCardByTeacher.size()) / studentAccountIds.size() * 100);
							}
							classException.put("attendanceRate", attendanceRate);
							List<JSONObject> exceptionTotal = new ArrayList<JSONObject>();
							Map<String, Object> initClassJson = initClassJson();
							if(CollectionUtils.isNotEmpty(oneCardByTeacher)){
							/*exception:异常状态（早退，迟到，无打卡记录）,exceptionNumberPeople:异常人数,exceptionNumber:异常次数
							exceptionList:异常列表[{,studentName:学生姓名,time:时间（08:30）}]*/
								for (JSONObject jsonObject2 : oneCardByTeacher) {
									JSONObject jsonObject3 = new JSONObject();
									jsonObject3.put("studentName", jsonObject2.getString("userName"));
									jsonObject3.put("time", getStringOfTime(jsonObject2.getDate("swingCardTime")));
									String workAttr = getAttendanceFlag(jsonObject2.getInteger("attendanceFlag"));
									if(!"正常".equals(workAttr)){
										JSONObject object = (JSONObject)initClassJson.get(workAttr);
										object.put("exceptionNumberPeople", (object.getInteger("exceptionNumberPeople") + 1));
										object.put("exceptionNumber", (object.getInteger("exceptionNumber") + 1));
										@SuppressWarnings("unchecked")
										ArrayList<JSONObject> object2 = (ArrayList<JSONObject>)object.get("exceptionList");
										object2.add(jsonObject3);
									}
								}
							}
							Set<Entry<String, Object>> entrySet = initClassJson.entrySet();
							Iterator<Entry<String, Object>> iterator = entrySet.iterator();
							while (iterator.hasNext()) {
								Map.Entry<String, Object> entry = (Map.Entry<String, Object>) iterator.next();
								exceptionTotal.add((JSONObject)entry.getValue());
							}
							classException.put("exceptionTotal", exceptionTotal);
							classExceptionList.add(classException);
							returnJson2.put("weekday", getStrings(new Date()));
							returnJson2.put("classExceptionList", classExceptionList);
							returnJson.put("data", returnJson2);
						} catch (Exception e) {
							logger.info(e.getMessage());
						}
						
					}
				}
			}else{
				JSONObject json = new JSONObject();
				json.put("weekday", getStrings(new Date()));
				json.put("classExceptionList", new Object[]{});
				returnJson.put("data", json);
			}
		}
		returnJson.put("code", 1);
		returnJson.put("msg", "");
		return returnJson;
	}
	
	/****
	 * 老师本学期考勤情况
	 * @param req
	 * @param request
	 * @param res
	 * @return
	 */
	@RequestMapping(value = "attendance/teacherCurrentTerm",method = RequestMethod.POST)
	@ResponseBody
	public JSONObject teacherCurrentTerm(HttpServletRequest req,@RequestBody JSONObject request, HttpServletResponse res){
		JSONObject returnJson = new JSONObject();
		JSONObject returnJson2 = new JSONObject();
		String userId = request.getString("userId");
		String schoolId = request.getString("schoolId");
		String classId = request.getString("classId");
		if(StringUtils.isEmpty(userId) || StringUtils.isEmpty(schoolId)){
			returnJson.put("code", -1);
			returnJson.put("msg", "参数为空！");
			return returnJson;
		}
		User user = commonDataService.getUserById(Long.valueOf(schoolId), Long.valueOf(userId));
		if(user == null){
			returnJson.put("code", -1);
			returnJson.put("msg", "用户为空！");
			return returnJson;
		}
		if(StringUtils.isNotBlank(classId)){
			Classroom classInfo = commonDataService.getClassById(Long.valueOf(schoolId), Long.valueOf(classId), 
					commonDataService.getCurTermInfoId(Long.valueOf(schoolId)));
			List<Long> studentAccountIds = classInfo.getStudentAccountIds();
			Map<String, Object> map = new HashMap<String, Object>();
			getSundayOfThisWeek(map);
			getMondayOfThisWeek(map);
			JSONObject jsonData = new JSONObject();
			jsonData.put("startDate", map.get("startDate"));
			jsonData.put("endDate", map.get("endDate"));
			jsonData.put("userIds", studentAccountIds);
			String date = getDate();
			jsonData.put("date", date);
			jsonData.put("tableName", "t_o_onecard_"+date);
			jsonData.put("schoolId", schoolId);
			List<JSONObject> oneCardByWeekOfTeacher = oneCardService.queryOneCardByWeekOfTeacher(jsonData);
			List<JSONObject> classExceptionList = new ArrayList<JSONObject>();
			JSONObject classException = new JSONObject();
			classException.put("classId", classInfo.getId());
			classException.put("className", classInfo.getClassName());
			List<JSONObject> exceptionTotal = new ArrayList<JSONObject>();
			Map<String, Object> initClassJson = initClassJson();
			if(CollectionUtils.isNotEmpty(oneCardByWeekOfTeacher)){
			/*exception:异常状态（早退，迟到，无打卡记录）,exceptionNumberPeople:异常人数,exceptionNumber:异常次数
			exceptionList:异常列表[{,studentName:学生姓名,time:时间（08:30）}]*/
				for (JSONObject jsonObject2 : oneCardByWeekOfTeacher) {
					JSONObject jsonObject3 = new JSONObject();
					jsonObject3.put("studentName", jsonObject2.getString("userName"));
					jsonObject3.put("weekday",  getStrings(jsonObject2.getDate("swingCardTime")));
					jsonObject3.put("time", getStringOfTime(jsonObject2.getDate("swingCardTime")));
					String workAttr = getAttendanceFlag(jsonObject2.getInteger("attendanceFlag"));
					JSONObject object = (JSONObject)initClassJson.get(workAttr);
					object.put("exceptionNumberPeople", (object.getInteger("exceptionNumberPeople") + 1));
					object.put("exceptionNumber", (object.getInteger("exceptionNumber") + 1));
					@SuppressWarnings("unchecked")
					ArrayList<JSONObject> object2 = (ArrayList<JSONObject>)object.get("exceptionList");
					object2.add(jsonObject3);
				}
			}
			Set<Entry<String, Object>> entrySet = initClassJson.entrySet();
			Iterator<Entry<String, Object>> iterator = entrySet.iterator();
			while (iterator.hasNext()) {
				Map.Entry<String, Object> entry = (Map.Entry<String, Object>) iterator.next();
				exceptionTotal.add((JSONObject)entry.getValue());
			}
			classException.put("exceptionTotal", exceptionTotal);
			classExceptionList.add(classException);
			returnJson2.put("weekday", getStringsByWeeks((Date)map.get("startDate"),(Date)map.get("endDate")));
			returnJson2.put("classExceptionList", classExceptionList);
			returnJson.put("data", returnJson2);
		}else{
			List<Long> classIds = new ArrayList<Long>();
			List<Course> courseIds = null;
			if(user != null){
				courseIds = user.getTeacherPart().getCourseIds();
			}
			if(CollectionUtils.isNotEmpty(courseIds)){
				for (Course course : courseIds) {
					long classId2 = course.getClassId();
					if(!classIds.contains(classId2)){
						classIds.add(classId2);
					}
				}
				List<Classroom> classroomBatch = commonDataService.getClassroomBatch(Long.valueOf(schoolId), classIds, 
						commonDataService.getCurTermInfoId(Long.valueOf(schoolId)));
				if(CollectionUtils.isNotEmpty(classroomBatch)){
					List<JSONObject> classExceptionList = new ArrayList<JSONObject>();
					for (Classroom classroom : classroomBatch) {
						List<Long> studentAccountIds = classroom.getStudentAccountIds();
						Map<String, Object> map = new HashMap<String, Object>();
						getSundayOfThisWeek(map);
						getMondayOfThisWeek(map);
						JSONObject jsonData = new JSONObject();
						jsonData.put("startDate", map.get("startDate"));
						jsonData.put("endDate", map.get("endDate"));
						jsonData.put("accountIds", studentAccountIds);
						String date = getDate();
						jsonData.put("date", date);
						jsonData.put("tableName", "t_o_onecard_"+date);
						jsonData.put("classIds", classIds);
						jsonData.put("schoolId", schoolId);
						try {
							List<JSONObject> oneCardByWeekOfTeacher = new ArrayList<JSONObject>();
							if(CollectionUtils.isNotEmpty(studentAccountIds)){
								oneCardByWeekOfTeacher = oneCardService.queryOneCardByWeekOfTeacher(jsonData);
							}
							logger.debug("teacherCurrentTerm ===============>" + oneCardByWeekOfTeacher);
							JSONObject classException = new JSONObject();
							classException.put("classId", classroom.getId());
							classException.put("className", classroom.getClassName());
							List<JSONObject> exceptionTotal = new ArrayList<JSONObject>();
							Map<String, Object> initClassJson = initClassJson();
							if(CollectionUtils.isNotEmpty(oneCardByWeekOfTeacher)){
							/*exception:异常状态（早退，迟到，无打卡记录）,exceptionNumberPeople:异常人数,exceptionNumber:异常次数
							exceptionList:异常列表[{,studentName:学生姓名,time:时间（08:30）}]*/
								for (JSONObject jsonObject2 : oneCardByWeekOfTeacher) {
									JSONObject jsonObject3 = new JSONObject();
									jsonObject3.put("studentName", jsonObject2.getString("userName"));
									jsonObject3.put("weekday",  getStrings(jsonObject2.getDate("swingCardTime")));
									jsonObject3.put("time", getStringOfTime(jsonObject2.getDate("swingCardTime")));
									String workAttr = getAttendanceFlag(jsonObject2.getInteger("attendanceFlag"));
									if(!"正常".equals(workAttr)){
										JSONObject object = (JSONObject)initClassJson.get(workAttr);
										object.put("exceptionNumberPeople", (object.getInteger("exceptionNumberPeople") + 1));
										object.put("exceptionNumber", (object.getInteger("exceptionNumber") + 1));
										@SuppressWarnings("unchecked")
										ArrayList<JSONObject> object2 = (ArrayList<JSONObject>)object.get("exceptionList");
										object2.add(jsonObject3);
									}
								}
							}
							Set<Entry<String, Object>> entrySet = initClassJson.entrySet();
							Iterator<Entry<String, Object>> iterator = entrySet.iterator();
							while (iterator.hasNext()) {
								Map.Entry<String, Object> entry = (Map.Entry<String, Object>) iterator.next();
								exceptionTotal.add((JSONObject)entry.getValue());
							}
							classException.put("exceptionTotal", exceptionTotal);
							classExceptionList.add(classException);
							returnJson2.put("weekday", getStringsByWeeks((Date)map.get("startDate"),(Date)map.get("endDate")));
						} catch (Exception e) {
							logger.info(e.getMessage());
						}
					}
					returnJson2.put("classExceptionList", classExceptionList);
					returnJson.put("data", returnJson2);
				}
			}else{
				JSONObject json = new JSONObject();
				json.put("weekday", getStrings(new Date()));
				json.put("classExceptionList", new Object[]{});
				returnJson.put("data", json);
			}
		}
		returnJson.put("code", 1);
		returnJson.put("msg", "");
		return returnJson;
	}
	
	public static String getDate(){
		 return new SimpleDateFormat("yyyyMMdd",Locale.CHINA).format(new Date());
	}
	public static String getStrings(Date date){
		 return new SimpleDateFormat("E（MM月dd日）",Locale.CHINA).format(date);
	}
	public static String getStringsByWeeks(Date startDate,Date endDate){
		 return new SimpleDateFormat("MM.dd（E）",Locale.CHINA).format(startDate) + "——" + new SimpleDateFormat("MM.dd（E）",Locale.CHINA).format(endDate);
	}
	public static String getStringsByDate(Date date){
		 return new SimpleDateFormat("（MM月dd日）",Locale.CHINA).format(date);
	}
	public static String getStringsByDatePar(Date date){
		 return new SimpleDateFormat("MM月dd日",Locale.CHINA).format(date);
	}
	public static String getStringsByWeek(Date date){
		 return new SimpleDateFormat("E",Locale.CHINA).format(date);
	}
	public static String getStringOfTime(Date date){
		 return new SimpleDateFormat("HH:mm",Locale.CHINA).format(date);
	}
	//1:进校，2：出校，0：无方向标识
	public static String getAttType(Integer attendanceType,String idType){
		if("1".equals(idType)){
			if(1 == attendanceType){
				return "进校";
			}else if(2 == attendanceType){
				return "出校";
			}else{
				return "无方向标识";
			}
		}else{
			if(1 == attendanceType){
				return "上班";
			}else if(2 == attendanceType){
				return "下班";
			}else{
				return "无方向标识";
			}
		}
	}
	//1：正常，2：迟到，3:请假, 0：异常
	public static String getAttendanceFlag(Integer attendanceFlag){
		if(1 == attendanceFlag){
			return "正常";
		}else if(2 == attendanceFlag){
			return "迟到";
		}if(3 == attendanceFlag){
			return "请假";
		}else{
			return "异常";
		}
	}
	
	public static Map<String, Object> getXnxqDate(String termInfoNum){
		Map<String, Object> map = new HashMap<String, Object>();
		String year = termInfoNum.substring(0, 4);
		String term = termInfoNum.substring(4, 5);
		SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd");
		if("1".equals(term)){
			int yearNum = Integer.valueOf(year);
			Calendar startCal = Calendar.getInstance(); 
			startCal.set(yearNum, 8 - 1 , 12 - 1, 0, 0, 0);
			Date startDate = startCal.getTime();
			map.put("startDate", dateformat.format(startDate));
			Calendar endCal = Calendar.getInstance(); 
			endCal.set(Integer.valueOf(year) + 1, 1 - 1, 11, 0, 0, 0);
			Date endDate = endCal.getTime();
			map.put("endDate", dateformat.format(endDate));
		}else{
			Calendar startCal = Calendar.getInstance(); 
			startCal.set(Integer.valueOf(year) + 1, 2 - 1, 20 - 1, 0, 0, 0);
			Date startDate = startCal.getTime();
			map.put("startDate", dateformat.format(startDate));
			Calendar endCal = Calendar.getInstance(); 
			endCal.set(Integer.valueOf(year) + 1, 7 - 1, 26, 0, 0, 0);
			Date endDate = endCal.getTime();
			map.put("endDate", dateformat.format(endDate));
		}
		return map;
	}
	
	public static List<String> getSpanDate(Map<String, Object> map){
		List<String> tableNameList = new ArrayList<String>();
		Date startDate = (Date)map.get("startDate");
		Date endDate = (Date)map.get("endDate");
		Calendar startCalendar = Calendar.getInstance();
        Calendar endCalendar = Calendar.getInstance();
	    SimpleDateFormat tableName = new SimpleDateFormat("yyyyMMdd");
	    startCalendar.setTime(startDate);
	    endCalendar.setTime(endDate);
        while(true){
            startCalendar.add(Calendar.DAY_OF_MONTH, 1);
            if(startCalendar.getTimeInMillis() < endCalendar.getTimeInMillis()){
            	tableNameList.add("t_o_onecard_"+tableName.format(startCalendar.getTime()));
	        }else{
	            break;
	        }
        }
		return tableNameList;
	}
	public static Map<String, Object> initJson(){
		Map<String, Object> map = new HashMap<String, Object>();
		//exception:异常状态（早退，迟到，无打卡记录）
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("exception", "迟到");
		jsonObject.put("attendanceType", 2);
		jsonObject.put("exceptionNumber", 0);
		jsonObject.put("exceptionList", new ArrayList<JSONObject>());
		JSONObject jsonObject2 = new JSONObject();
		jsonObject2.put("exception", "请假");
		jsonObject2.put("attendanceType", 3);
		jsonObject2.put("exceptionNumber", 0);
		jsonObject2.put("exceptionList", new ArrayList<JSONObject>());
		JSONObject jsonObject3 = new JSONObject();
		jsonObject3.put("exception", "异常");
		jsonObject2.put("attendanceType", 4);
		jsonObject3.put("exceptionNumber", 0);
		jsonObject3.put("exceptionList", new ArrayList<JSONObject>());
		map.put("迟到", jsonObject);
		map.put("请假", jsonObject2);
		map.put("异常", jsonObject3);
		return map;
	}
	public static Map<String, Object> initClassJson(){
		Map<String, Object> map = new HashMap<String, Object>();
		//exception:异常状态（早退，迟到，无打卡记录）
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("exception", "迟到");
		jsonObject.put("attendanceType", 2);
		jsonObject.put("exceptionNumberPeople", 0);
		jsonObject.put("exceptionNumber", 0);
		jsonObject.put("exceptionList", new ArrayList<JSONObject>());
		
		JSONObject jsonObject2 = new JSONObject(); 
		jsonObject2.put("exception", "请假");
		jsonObject2.put("attendanceType", 3);
		jsonObject2.put("exceptionNumber", 0);
		jsonObject2.put("exceptionNumberPeople", 0);
		jsonObject2.put("exceptionList", new ArrayList<JSONObject>());
		
		JSONObject jsonObject3 = new JSONObject();
		jsonObject3.put("exception", "异常");
		jsonObject3.put("attendanceType", 4);
		jsonObject3.put("exceptionNumberPeople", 0);
		jsonObject3.put("exceptionNumber", 0);
		jsonObject3.put("exceptionList", new ArrayList<JSONObject>());
		map.put("迟到", jsonObject);
		map.put("请假", jsonObject2);
		map.put("异常", jsonObject3);
		return map;
	}

	public static void getSundayOfThisWeek(Map<String, Object> map) {
		Calendar cal=Calendar.getInstance(Locale.CHINA);
		cal.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
		cal.add(Calendar.DAY_OF_MONTH, 1);
		map.put("endDate", cal.getTime());
	}

	public static void getMondayOfThisWeek(Map<String, Object> map) {
		Calendar cal=Calendar.getInstance(Locale.CHINA);
		cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		map.put("startDate", cal.getTime());
	}
}
