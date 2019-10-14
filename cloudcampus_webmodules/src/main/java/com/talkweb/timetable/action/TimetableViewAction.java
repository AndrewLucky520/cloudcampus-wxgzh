package com.talkweb.timetable.action;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
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
import com.talkweb.accountcenter.thrift.T_GradeLevel;
import com.talkweb.accountcenter.thrift.T_Role;
import com.talkweb.accountcenter.thrift.User;
import com.talkweb.base.common.OutputMessage;
import com.talkweb.common.action.BaseAction;
import com.talkweb.common.tools.BeanTool;
import com.talkweb.common.tools.sort.Sort;
import com.talkweb.common.tools.sort.SortEnum;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.timetable.arrangement.service.SmartArrangeService;
import com.talkweb.timetable.service.TimetableService;

@Controller
@RequestMapping(value = "/timetableManage/")
public class TimetableViewAction extends BaseAction {
	
	private static final Logger logger = LoggerFactory.getLogger(TimetableViewAction.class);
	@Autowired
	private TimetableService timetableService;
	
	@Autowired
	private AllCommonDataService commonDataService;
	
	@Autowired
	private SmartArrangeService	smtService;
	

	/*
	 * 用于缓存teacher(0),class(1),teacherTimetable(2),classTimetable(3)及课表时间timetableSchedule的返回数据
	 * key:timeTableId
	 * value:result
	 */
	private JSONObject cachedData = new JSONObject();
	//private Map<String,String> timetableMap = new HashMap<String,String>();
	private boolean[] outofDate = {false,false,false,false};
	
	/*
	 * 设置数据过期,需要重新加载
	 * 0:教师课表,1:班级课表,2:教师总课表,3:班级总课表
	 * 
	 */
	public void setOutofDate(int value,boolean validate) {
		outofDate[value] = validate;
	}

	
	private void setPromptMessage(JSONObject object, String code, String message) {
		object.put("code", code);
		object.put("msg", message);
	}

	//使用缓存查询教师课表
	private JSONObject getBufferedTeacherTimetable(String timetableId,String subjectIds,String gradeIds){
		JSONObject result = new JSONObject();
		
		
		//++++++++++++++++   使用缓存查询         +++++++++++++++++++++++++		
		if(timetableId.equals(cachedData.getString("timetableId"))
				&&null!=cachedData.get("teacherTimetable")){
			List<JSONObject> allTeacherTimetableList = (List<JSONObject>)cachedData.get("teacherTimetable");
			List<JSONObject> cachedTeacherTimetableList = new ArrayList<JSONObject>();
			String[] courses = subjectIds.split(",");
			List<String> courseList = new ArrayList<String>();
			for(String courseId : courses){
				courseList.add(courseId);
			}
			String[] queryGids = gradeIds.split(",");
			List<String> queryGradeList = new ArrayList<String>();
			for(String gid : queryGids){
				queryGradeList.add(gid);
			}
			//筛选查询年级和课程
			for(JSONObject T_timetable : allTeacherTimetableList){//迭代每个老师
				//筛选年级
				JSONObject copyTimetable = BeanTool.castBeanToFirstLowerKey(T_timetable);
				switch(queryGradeList.size()) {
				case 0:break;
				case 1:
					String gradeId = T_timetable.getString("gradeId");
					String queryGrade = queryGradeList.get(0);
					if(!queryGrade.equals(gradeId)){
						continue;
					}
					default:
						cachedTeacherTimetableList.add(copyTimetable);						
				}
				//筛选课程
				List<JSONObject> timetable = (ArrayList<JSONObject>) copyTimetable.get("timetable");
				boolean hasTargetCourse = false;
				for(JSONObject course : timetable){	//迭代每门课程
					if(courseList.contains(course.getString("courseId"))){
						hasTargetCourse = true;
						break;
					}
				}
				if(!hasTargetCourse){
					cachedTeacherTimetableList.remove(copyTimetable);
				}
			}
			//筛选课程
			
			String code="0";
			String msg="查询成功！";
			if(0 == cachedTeacherTimetableList.size()){
				code = "1";
				msg = "未编排课表，无法查看！";
			}
			result.put("code", code);
			result.put("msg", msg);
			this.setPromptMessage(result, code, msg);
			result.put("data", cachedTeacherTimetableList);
			return result;
		}
		//++++++++++++++++++++++++++++       end      +++++++++++++++++
		String code = "1";
	    String msg = "未编排课表，无法查看！";
		result.put("code", code);
		result.put("msg", msg);
	    return result;
	}
	
	/**
	 * 老师课表
	 * 
	 * @author XFQ
	 * @param req
	 * @param res
	 * @param reqData
	 * @return
	 */
	@RequestMapping(value = "lookup/getTeacherTimetable.do", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getTeacherTimetable(HttpServletRequest req,
			HttpServletResponse res, @RequestBody JSONObject reqData) {
		JSONObject result = new JSONObject();
		String timetableId = reqData.getString("timetableId");
		String teacher = reqData.getString("teacher");
		String gradeIds = reqData.getString("gradeIds");	//年级
		String subjectIds = reqData.getString("subjectIds");	//科目
		//+++++++++++++++++++++++  
		//++++++++++++++++   使用缓存查询         +++++++++++++++++++++++++		
/*		if(timetableId.equals(cachedData.getString("timetableId"))
				&&null!=cachedData.get("teacherTimetable")
				&&!outofDate[0]
				&& ("".equals(teacher)||null==teacher)){
			return getBufferedTeacherTimetable(timetableId, subjectIds, gradeIds);
		}*/
		//++++++++++++++++++++++++++++       end      +++++++++++++++++
		String termInfoId = reqData.getString("selectedSemester");
		String schoolId = getXxdm(req);
		long sid = Long.valueOf(schoolId);
		School school = this.getSchool(req,termInfoId);
		String code = "0";
		String msg = "";
		HashMap<String, Classroom> cmap = new HashMap<String, Classroom>();
		HashMap<String, Grade> cgrademap = new HashMap<String, Grade>();
		HashMap<String, Integer>  gramap = new HashMap<String, Integer>();
		HashMap<String, Integer>  aLessonmap = new HashMap<String, Integer>();
		List<JSONObject> resultdate = new ArrayList<JSONObject>();
		try{
			List<String> classIdList = new ArrayList<String>();
			//+++++++++++++++++++++++++   查询课表对应的所有年级Id     +++++++++++++++++++++++++++++++++
			Map<String,String> resultMap = getAllGradeIdOfTimetable(gradeIds, termInfoId, school);
			String queryGradeId = resultMap.get("gradeIds");
			//++++++++++++++++++++++++++++++++++++   end    +++++++++++++++++++++++++++++++++++++		
			List<String> gradeIdList = Arrays.asList(gradeIds.split(","));
			if("".equals(teacher)||null==teacher){
				//gradeIdList = Arrays.asList(gradeIds.split(","));
			}else{
				//gradeIdList = Arrays.asList(queryGradeId.split(","));			
			}
			//++++++++++++++++++++++++   获取课表时间           +++++++++++++++++++++++++++++++			
			//Map<String,HashMap<String,String>> timetableScheduleMap 
							//	= getTimetableScheduleMap(gradeIdList, termInfoId, timetableId);
			Map<String,ArrayList<JSONObject>>scheduleMap = getTimetableScheduleMap(gradeIdList, termInfoId, timetableId);
			//++++++++++++++++++++++++  end   ++++++++++++++++++++++++++++++++++++++
									
			List<String> courseList = new ArrayList<String>();
			
			//++++++++++++++++++++++++++++++++++++++++++++++    查询所有科目          ++++++++++++++++++++++++++++++++++			
			/*if(subjectIds.split(",").length<2){
				if(!"".equals(teacher)||null!=teacher){
					courseList.add(subjectIds);
				}else{
					List<LessonInfo> lessonList = commonDataService.getLessonInfoList(school, termInfoId);				
					for(LessonInfo lesson : lessonList){
						//courseList.add(lesson.getId()+"");
					}
				}
			}else{
				//courseList = Arrays.asList(subjectIds.split(","));
			}*/
			//++++++++++++++++++++++++++++++++++++++++++++++++   end   +++++++++++++++++++++++++++++++++
			courseList = Arrays.asList(subjectIds.split(","));
			JSONObject param = new JSONObject();
			param.put("schoolId", sid);
			param.put("selectedSemester", termInfoId);
			for(String gradeId : gradeIdList){
				param.put("gradeId", gradeId);
				classIdList.addAll(getClassListBySYNJ(param));//耗时2s
			}
			// 年级-科目教师信息列表
			List<Account> teacherlist = new ArrayList<Account>();
			JSONObject object = new JSONObject();
			object.put("schoolId", schoolId);
			object.put("timetableId", timetableId);
			object.put("classIdList", classIdList);
			object.put("courseIdList", courseList);
			List<Long> teacherIds = timetableService.getTeacherIds(object);//耗时7s
			// 教师姓名查询
			if (StringUtils.isNotEmpty(teacher)){
				List<Account> alist = commonDataService.getAllSchoolEmployees(school,termInfoId,
						teacher);
				for(Account a : alist){
					long accountId = a.id;
					if (teacherIds.contains(accountId)){
						teacherlist.add(a);
					}
				}
			}else{
				teacherlist = commonDataService.getAccountBatch(sid, teacherIds, termInfoId);//耗时1s
			}
			// 教师ID对应的工号
			HashMap<String, String> ghmap = new HashMap<String, String>();
			List<Account> aList = commonDataService.getAllSchoolEmployees(
					school, termInfoId, null);//耗时1.5s
			for (Account account : aList) {
				String accountId = account.getId() + "";
				List<User> ulist = account.getUsers();
				if (null != ulist) {
					for (User user : ulist) {
						if (null == user || null == user.getUserPart()
								|| null == user.getUserPart().getRole()) {
							continue;
						}
						if (user.getUserPart().getRole().equals(T_Role.Teacher)) {
							if (null != user.getTeacherPart()) {
								String ghid = user.getTeacherPart().getEmpno();
								if (StringUtils.isNotEmpty(ghid)) {
									ghmap.put(accountId, ghid);
								}
							}
						}
					}
				}
			}
			if (teacherlist != null && teacherlist.size() > 0) {
				// 科目信息
				HashMap<String, LessonInfo> lmap = new HashMap<String, LessonInfo>();
				List<LessonInfo> lilist = commonDataService
						.getLessonInfoList(school, termInfoId);// 科目信息
				for (LessonInfo l : lilist) {
					 lmap.put(String.valueOf(l.getId()), l);
				}
				// 课表信息
				List<Long> tidlist = new ArrayList<Long>();
				for (Account a : teacherlist) {
					 tidlist.add(a.getId());
				}
				HashMap<String, Object> map = new HashMap<String, Object>();
				map.put("timetableId", timetableId);
				map.put("SchoolId", schoolId);
				map.put("teacherIds", tidlist);
				List<JSONObject> list = timetableService
						.getTeacherTimetable(map); //耗时15s
				// 如果存在课表信息
				if (list != null && list.size() > 0) {
					List<Long> cidlist = new ArrayList<Long>();
					HashSet<String> t = new HashSet<String>();
					//活动
					HashMap<String, Object> reasmap = new HashMap<String, Object>();
					reasmap.put("timetableId", timetableId);
					reasmap.put("schoolId", schoolId);
					List<JSONObject> researchlist = timetableService.getTeacherResearch(reasmap);	
					//年级
					List<Grade> glist=commonDataService.getGradeList(school,termInfoId);//耗时0.5s
					HashMap<String, Grade> gmap = new HashMap<String, Grade>();
					if(glist!=null&&glist.size()>0){
						for(Grade g:glist){
							gmap.put(String.valueOf(g.getId()), g);
						}
					}	
					for (JSONObject json : list) {
						 t.add(json.get("ClassId").toString());
						 cidlist.add(Long.parseLong(json.get("ClassId").toString()));
					}				
					Collections.sort(cidlist);
					List<Classroom> clist = commonDataService
							.getSimpleClassBatch(sid, cidlist, termInfoId);// 班级信息
					for (Classroom cr : clist) {				
						 cmap.put(String.valueOf(cr.getId()), cr);
						 if(gmap.get(String.valueOf(cr.getGradeId()))!=null){
							cgrademap.put(String.valueOf(cr.getId()), gmap.get(String.valueOf(cr.getGradeId())));
						}			
					}				
					HashMap<String, Object> grademap = new HashMap<String, Object>();
					grademap.put("schoolId", schoolId);
					grademap.put("timetableId", timetableId);
					grademap.put("GradeId", "");					
					List<JSONObject> gradeSetlist = timetableService
								.getGradeSet(grademap);// 年级课表安排
					if(gradeSetlist!=null&&gradeSetlist.size()>0){
						for(JSONObject o:gradeSetlist){
							int totalMax=Integer.valueOf(o.getString("AMLessonNum"))+Integer.valueOf(o.getString("PMLessonNum"));
							gramap.put(o.getString("GradeId"), totalMax);
							aLessonmap.put(o.getString("GradeId"), Integer.valueOf(o.getString("AMLessonNum")));
						}
					}
					for (Account a : teacherlist) {
						
						JSONObject data = new JSONObject();
						String accountid = a.getId() + "";
						data.put("accountId", accountid);						
						if (ghmap.containsKey(accountid)){
							data.put("teacherId", ghmap.get(accountid));
							data.put("TeacherId", ghmap.get(accountid));
						}else{
							data.put("teacherId", "");
							data.put("TeacherId", "");
						}	
						String name = a.getName();
						data.put("teacherName", name);
						if (StringUtils.isNotEmpty(name)){								
							resultdate.add(data);
						}	
					}	
					// 处理合班,单双周在前端处理
					HashMap<String, JSONObject> hsmap = new HashMap<String, JSONObject>();
					List<JSONObject> tablelist = new ArrayList<JSONObject>();				
					for (JSONObject json : list) {
						 String key = json.getString("DayOfWeek")
								+ json.getString("LessonOfDay") + "&"
								+ json.getString("TeacherId") + "&"
								+ json.getString("CourseType");	
						 String targetId = json.getString("ClassId");
						 if (hsmap.get(key) == null) {			
							 json.put("ClassId", targetId);
							 tablelist.add(json); 
							 hsmap.put(key, json);
						 }else{
							 JSONObject sJson = hsmap.get(key);
							 String sourceId = sJson.getString("ClassId");
	                         sJson.put("ClassId", sourceId + "," + targetId);						 
						 }					 
					}
					Map<String,String> classMapGrade = new HashMap<String,String>();
					// 封装timetable数据
					for (JSONObject rejson : resultdate) {
						//++++++++++++++++++++  根据gradeId查询对应的上课时间  ++++++++++++++++++++++
						//+++++++++++++++++++++  end  +++++++++++++++++++++
						HashMap<String,JSONObject> tbmap = new HashMap<String,JSONObject>();
						HashSet<String> gradeSet = new HashSet<String>();
						for (JSONObject json : tablelist) {
							if (rejson.get("accountId").toString()
									.equals(json.getString("TeacherId"))) {
								JSONObject tjson = new JSONObject();
								String courseName = COURSE_PROMPT_DELETE;
								// 单双周的标识问题
								String type = json.getString("CourseType");
								if (lmap.containsKey(json.getString("CourseId"))) {
									courseName = lmap.get(
											json.getString("CourseId"))
											.getName();
									if (StringUtils.isEmpty(courseName)){
										courseName = COURSE_PROMPT_EMPTY;
									}else{
										if (type.equals("1")){
		                             	    courseName = courseName + LABEL_SINGLE;
		                                }else if(type.equals("2")){
		                             	    courseName = courseName + LABEL_DOUBLE;
		                                }
									}	
								}
								List<String> namelist = new ArrayList<String>();
								String[] classid = json.getString("ClassId")
										.toString().split(",");
								for(String ccid : classid) {
									String cName = CLASS_PROMPT_DELETE;
									if (cmap.containsKey(ccid)) {
										cName = cmap.get(ccid)
												.getClassName();
										if (StringUtils.isEmpty(cName)){
											cName = CLASS_PROMPT_EMPTY;
										}
									}
									if(!namelist.contains(cName)){											
										namelist.add(cName);
									}
								}
								String className = namelist.get(0);
								if (namelist.size() > 1) {
									className = StringUtils.join(namelist, ",");
								}						                               
								tjson.put("courseId",
										json.getString("CourseId"));
								tjson.put("courseName", courseName);
								tjson.put("className", className);
								tjson.put("classId", json.getString("ClassId")
										.toString());
								tjson.put("courseType", type);
								tjson.put("dayOfWeek",
										json.getString("DayOfWeek"));
								tjson.put("lessonOfDay",
										json.getString("LessonOfDay"));
								tbmap.put(json.getString("DayOfWeek")+json.getString("LessonOfDay")
										+json.getString("TeacherId")+json.getString("CourseType"), tjson);
								//++++++++++++++++++++++++++++++++++++++++++++    查询教师对应的年级      ++++++++++++++++++++++++++++++++++++++++
								
								try{
									String gradeId =null;
									for (int i = 0; i < classid.length; i++) {
										gradeId = classMapGrade.get(classid[i]);
										if(null==gradeId){
											Classroom cr = commonDataService.getClassById(Long.parseLong(schoolId),Long.parseLong(classid[i]), termInfoId);										
											if(null!=cr){
												Grade grade = commonDataService.getGradeById(Long.parseLong(schoolId), cr.getGradeId(), termInfoId);
												gradeId = commonDataService.ConvertNJDM2SYNJ(grade.currentLevel.getValue()+"", json.getString("SchoolYear"));
												classMapGrade.put(classid[i], gradeId);
											}
										}
										gradeSet.add(gradeId);
										
									}
									 
									
									 
									//rejson.put("gradeId", gradeId);
									
								

									//+++++++++++++++++  加入上课时间段       +++++++++++++++																									
									//Map<String,String> schedule = timetableScheduleMap.get(gradeId);
									//tjson.put("sectionTime", schedule.get(json.getString("LessonOfDay")));
									//+++++++++++++++++++++++     end     +++++++++++++++++
								}catch(Exception e){
									e.printStackTrace();
									logger.info(e.getMessage());
								}
								
								//+++++++++++++++++++++++++++++++++++++++++++++++    end      +++++++++++++++++++++++++++++++++++
								rejson.put(
										"totalMaxDays",json.getString("MaxDaysForWeek"));
								rejson.put(
										"tableName",
										school.getName()
												+ json.getString("TimetableName"));
							}						
						}
						//+++++++++++++++++  加入上课时间段       +++++++++++++++	start	
						if (gradeSet.size() > 0) {
							  Iterator<String> it = gradeSet.iterator();
							  String gradeId = null;
							  List<JSONObject> copyScheduleTime = new ArrayList<JSONObject>();
							  List<JSONObject> scheduleTime =null;
							  List<JSONObject> tempScheduleTime =null;
							  while (it.hasNext()) {
								  gradeId =  it.next();
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
								  rejson.put("scheduleTime",copyScheduleTime);
							  }
							 
							  
						}
						//+++++++++++++++++  加入上课时间段       +++++++++++++++	end	
						
						// 教研活动
						if (researchlist != null && researchlist.size() > 0){						
							for(JSONObject researchjson : researchlist){
								if (rejson.getString("accountId").equals(researchjson.getString("TeacherId")))	{
									String key = researchjson.getString("DayOfWeek")
											+ researchjson.getString("LessonOfDay")
											+ researchjson.getString("TeacherId");
									if (tbmap.get(key + "1")==null && tbmap.get(key + "2")==null && tbmap.get(key + "0")==null){							
										JSONObject tjson = new JSONObject();
										tjson.put("dayOfWeek",
												researchjson.getString("DayOfWeek"));
										tjson.put("lessonOfDay",
												researchjson.getString("LessonOfDay"));
										
										//+++++++++++++++++  加入上课时间段       +++++++++++++++
										//String gradeId = rejson.getString("gradeId");										
										//JSONObject timetableSchedule = timetableScheduleMap.get(gradeId);
										//JSONArray array = (JSONArray) timetableSchedule.getJSONArray("data");
										//Map<String,String> schedule = timetableScheduleMap.get(gradeId);
										//tjson.put("sectionTime", schedule.get(researchjson.getString("LessonOfDay")));
										//+++++++++++++++++++++++     end     +++++++++++++++++
										
										tjson.put("className", "");
										tjson.put("courseName", researchjson.getString("TeacherGroupName"));
										tbmap.put(key + "0",tjson);												
									}
								}	
							}
						}
						Iterator<Entry<String, JSONObject>> ite = tbmap.entrySet().iterator();
						List<JSONObject> tblis = new ArrayList<JSONObject>();
						while (ite.hasNext()) {
							Map.Entry<String, JSONObject> entry =  ite.next();
							tblis.add(entry.getValue());
						}
						rejson.put("timetable", tblis);
					}
				} else {
					code = "1";
					msg = "未编排课表,无法查看";
				}
				for (Iterator<JSONObject> it = resultdate.iterator(); it.hasNext();) {
					JSONObject json = it.next();
					List<JSONObject> glist = (List<JSONObject>) json
							.get("timetable");
					if (glist.size() == 0) {
						it.remove();
					}
				}			
				for(JSONObject ro:resultdate){
					int maxDay=0,amLessonNum = 0;
					List<Integer> totallist=new ArrayList<Integer>();
					List<Integer> aLesssonlist=new ArrayList<Integer>();
					List<JSONObject> timlist=(List<JSONObject>) ro.get("timetable");
					for(JSONObject to:timlist){
						if(to.containsKey("classId")){
						String cids[] =to.getString("classId").split(",");
						for(String id:cids){
							if(cgrademap.containsKey(id)){
								Grade grade=cgrademap.get(id);
								String xn=(String) req.getSession().getAttribute("curXnxq");
								String gid=commonDataService.ConvertNJDM2SYNJ(String.valueOf(grade.getCurrentLevel().getValue()),xn.substring(0, 4));
								if(gramap.get(gid)!=null){
									totallist.add(gramap.get(gid));
									aLesssonlist.add(aLessonmap.get(gid));
								}
							}
						}
						}
					}
					if(totallist.size()>0){
					for(int max:totallist){
						maxDay = maxDay < max ? max : maxDay;
					}
					ro.put("totalMaxLessons", maxDay);
					}else{
						ro.put("totalMaxLessons", 8);
						ro.put("totalMaxDays", 5);
					}
					if(aLesssonlist.size()>0){
						for(int max:aLesssonlist){
							amLessonNum = amLessonNum < max ? max : amLessonNum;
					}
					ro.put("amLessonNum", amLessonNum);
					}
				}
				//+++++++++++++++++++++++++  加入上课时间	++++++++++++++++++++++				
				//+++++++++++++++++++++++++++++    end    ++++++++++++++++++++
				
				//+++++++++++++++++++++++  添加缓存查询数据        +++++++++++++++++++++
				/*String cachedTimetableId = cachedData.getString("timetableId");
				if(null!=cachedTimetableId && !cachedTimetableId.equals(timetableId)){
					cachedData.clear();
				}
				if(null == teacher || "".equals(teacher)){
					cachedData.put("teacherTimetable", resultdate);
					cachedData.put("timetableId", timetableId);
				}*/
				//+++++++++++++++++++++++++++   end    ++++++++++++++++++++++
				req.getSession().setAttribute("report", resultdate);
			} else {
				code = "1";
				msg = "未编排课表,无法查看";
			}
		} catch (Exception e) {
			code = "1";
			if (e.getMessage() == null) {
				msg = "发生一个内部的错误";
			} else {
				msg = e.getMessage();
			}
		}
		//++++++++++++++++++++++++++  comment by lime
        /*if(null!=resultdate && resultdate.size()>0){
			timetableMap.put(timetableId, resultdate.get(0).getString("totalMaxLessons"));
			timetableMap.put("amLessonNum", resultdate.get(0).getString("amLessonNum"));
			//setOutofDate(0,false);
		}*/
		//++++++++++++++++++++++++++
		if(null == teacher || "".equals(teacher)){		
			//return getBufferedTeacherTimetable(timetableId, subjectIds, gradeIds);		
		}else{
			
		}if(resultdate.size()<1){
				code = "1";
				msg = "未编排课表,无法查看";
			}
			result.put("code", code);
			result.put("msg", msg);
			result.put("data", resultdate);
			return result;		
	}

	@SuppressWarnings("unused")
	private JSONObject getBufferedClassTimetable(String timetableId,String classIds,String gradeIds){
		JSONObject result = new JSONObject();
		List<JSONObject> cachedGrades = (ArrayList<JSONObject>)cachedData.get("classTimetable");
		List<JSONObject> queryResult = new ArrayList<JSONObject>(); 
		List<String> queryGradeList = CollectionUtils.arrayToList(gradeIds.split(","));
		List<String> clsList = CollectionUtils.arrayToList(classIds.split(","));
		
		for(JSONObject grade : cachedGrades){ //年级筛选
			JSONObject copyGrade = BeanTool.castBeanToFirstLowerKey(grade);
			
			//指定了班级查询,只显示该班级对应的年级
			if(clsList.size()==1){	
				if(clsList.get(0).equals(copyGrade.getString("classId"))){
					queryResult.add(copyGrade);
					break;
				}else{
					continue;
				}
			}
			
			if(queryGradeList.contains(copyGrade.getString("gradeId"))){
				queryResult.add(copyGrade);
			}else{
				continue;
			}
		}
		String code="0";
		String msg="查询成功！";
		if(queryResult.size()==0){
			code = "1";
			msg = "未编排课表，无法查看！";
		}
		result.put("code", code);
		result.put("msg", msg);
		this.setPromptMessage(result, code, msg);
		result.put("data", queryResult);
		return result;
	}
	/**
	 * 
	 * 
	 * 班级课表
	 * 
	 * @author XFQ
	 * @param req
	 * @param res
	 * @param reqData
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "lookup/getClassTimetable.do", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getClassTimetable(HttpServletRequest req,
			HttpServletResponse res, @RequestBody JSONObject reqData) {
		JSONObject result = new JSONObject();
		String timetableId = reqData.getString("timetableId");
		String gradeId = reqData.getString("gradeId");
		String classId = reqData.getString("classId");
		String termInfoId = reqData.getString("selectedSemester");
		String schoolId = getXxdm(req);
		long sid = Long.valueOf(schoolId);
		School school = this.getSchool(req,termInfoId);
		String code = "0";
		String msg = "";

		//++++++++++++++++++++++++++++++   使用缓存数据查询        ++++++++++++++++++++++++++++++++++++++
		/*if(null!=timetableId && timetableId.equals(cachedData.get("timetableId"))
				&& !outofDate[1]
						&& null != cachedData.get("classTimetable")){
			return getBufferedClassTimetable(timetableId,classId,gradeId);			
		}*/
		//++++++++++++++++++++++++++++++     end     +++++++++++++++++++++++++++++++++++++++++
		
		
		List<JSONObject> resultdate = new ArrayList<JSONObject>();
		try{
			//+++++++++++++++++++++   获取学校对应的所有年级及所有班级        +++++++++++++++++++++++++++++++++
			/*Map<String,String> resultMap = getAllGradeIdOfTimetable(gradeId, termInfoId, school);
			String gradeIds = resultMap.get("gradeIds");
			String classIds = resultMap.get("classIds");
			String queryClassId = classId;
			classId = classIds;*/
			//++++++++++++++++++++++++++++   end   ++++++++++++++++++++++++++++++++++++++
			
			//++++++++++++++++++++++++   获取课表时间           +++++++++++++++++++++++++++++++			
			List<String> gradeIdList = Arrays.asList(gradeId.split(","));
			/*Map<String,HashMap<String,String>> timetableScheduleMap 
								= getTimetableScheduleMap(gradeIdList, termInfoId, timetableId);*/
			Map<String, ArrayList<JSONObject>> gradeMapSchedule = getTimetableScheduleMap(gradeIdList, termInfoId, timetableId);
			//++++++++++++++++++++++++  end   ++++++++++++++++++++++++++++++++++++++
						
			// 班级列表
			List<Classroom> classrlist = new ArrayList<Classroom>();
			List<Long> ids = new ArrayList<Long>();
			String[] gid = gradeId.split(",");//gradeId.split(",");
			for (String id : gid) {
				 ids.add(Long.valueOf(id));
			}
			if (classId.indexOf(",") == -1) {
				classrlist.add(commonDataService.getClassById(sid,
						Long.valueOf(classId),termInfoId));
			} else if (classId.indexOf(",") > -1) {
				List<Long> cids = new ArrayList<Long>();
				String[] classid = classId.split(",");
				for (String id : classid) {
					cids.add(Long.valueOf(id));
				}
				Collections.sort(cids);
				classrlist = commonDataService.getClassroomBatch(sid, cids, termInfoId);
			}
			// 班级-年级
			List<String> cid = new ArrayList<String>();
			Map<String,String> cGrade =new HashMap<String,String>();
			Map<Long,Grade> gradeCache = new HashMap<Long,Grade>();
			for (Classroom cm : classrlist) {
				 cid.add(String.valueOf(cm.getId()));
				 long grade_Id = cm.getGradeId();
				 Grade grade = gradeCache.get(grade_Id);
				 if(null==grade){
					grade = commonDataService.getGradeById(sid, grade_Id, termInfoId);				 
					gradeCache.put(grade_Id, grade);
				 }
				 String useGradeId = timetableService.convertSYNJByGrade(sid, grade, termInfoId);
				 cGrade.put(String.valueOf(cm.getId()), useGradeId);
			}
			
			String tids = StringUtils.join(cid, ",");
			HashMap<String, Object> teamap = new HashMap<String, Object>();
			teamap.put("schoolId", schoolId);
			teamap.put("termInfoId", termInfoId);
			teamap.put("classId", tids);

			HashMap<String, Object> tablemap = new HashMap<String, Object>();
			tablemap.put("schoolId", schoolId);
			tablemap.put("timetableId", timetableId);
			tablemap.put("ClassIds", cid);

			HashMap<String, Object> grademap = new HashMap<String, Object>();
			grademap.put("schoolId", schoolId);
			grademap.put("timetableId", timetableId);
			grademap.put("GradeId", ids);
			// 课表信息
			List<JSONObject> tablelist = timetableService
					.getClassTimetable(tablemap);
			if (tablelist != null && tablelist.size() > 0) {
				// 年级信息
				List<JSONObject> gradeSetlist = timetableService
						.getGradeSet(grademap);
				if(gradeSetlist!=null&&gradeSetlist.size()>0){
				// 科目信息
				HashMap<String, LessonInfo> lmap = new HashMap<String, LessonInfo>();
				List<LessonInfo> lilist = commonDataService
						.getLessonInfoList(school, termInfoId);
				for (LessonInfo l : lilist) {
					lmap.put(String.valueOf(l.getId()), l);
				}
				// 学校的老师
				HashMap<String, Account> tmap = new HashMap<String, Account>();
				List<Account> userlist = commonDataService.getAllSchoolEmployees(
						school,termInfoId, "");
				for (Account a : userlist) {
					tmap.put(String.valueOf(a.getId()), a);
				}
				// 班主任
				HashMap<Long, String> dmap = new HashMap<Long, String>();
				List<Account> bzrlist = commonDataService.getDeanList(teamap);
				for (Account a : bzrlist){
					 dmap.put(a.getId(), a.getName());
				}
				// 班级的主要信息
				for (Classroom cm : classrlist) {
					JSONObject data = new JSONObject();
					if (dmap.containsKey(cm.getDeanAccountId())){
						data.put("teacherName", dmap.get(cm.getDeanAccountId()));
					}					
					for (JSONObject json : tablelist) {
						if (String.valueOf(cm.getId()).equals(
								json.getString("ClassId").toString())) {
							data.put("totalMaxDays",
									json.getString("MaxDaysForWeek"));
							data.put("tableName",
									json.getString("TimetableName"));
							break;
						}
					}
					String name = cm.getClassName();
					data.put("className", name);
					data.put("classId", cm.getId());
					if (StringUtils.isNotEmpty(name)){	
						resultdate.add(data);
					}	
				}
				resultdate = (List<JSONObject>) Sort.sort(SortEnum.ascEnding0rder, resultdate, "className");
				// 设置totalMaxLessons的值
				Map<String,Integer> totalLesson =new HashMap<String,Integer>();
				Map<String,Integer> amLesson =new HashMap<String,Integer>();
				for (JSONObject json : gradeSetlist) {
					 totalLesson.put(
							json.getString("GradeId"),
							Integer.valueOf(json.getString("AMLessonNum")
									.toString())
									+ Integer.valueOf(json.getString(
											"PMLessonNum").toString()));
					 amLesson.put(json.getString("GradeId"),
							Integer.valueOf(json.getString("AMLessonNum")
									.toString()));
				}
				for (JSONObject rejson : resultdate) {
					 String grade_id = cGrade.get(rejson.getString("classId"));
					 if (totalLesson.containsKey(grade_id)){
						 rejson.put("totalMaxLessons", totalLesson.get(grade_id));
						 rejson.put("amLessonNum", amLesson.get(grade_id));
						 //+++++++++++++++    加入班级所属年级Id		++++++++++++
						 rejson.put("gradeId", grade_id);	
						 List<JSONObject> scheduleTime = gradeMapSchedule.get(grade_id);
							List<JSONObject> copyScheduleTime = new ArrayList<JSONObject>();
							for(JSONObject obj : scheduleTime){
								copyScheduleTime.add(BeanTool.castBeanToFirstLowerKey(obj));
							}
							rejson.put("scheduleTime",copyScheduleTime);
						 //++++++++++++++++++++　　　end     +++++++++++++++++
					 }
				}
				for (JSONObject rejson : resultdate) {
					List<JSONObject> tblist = new ArrayList<JSONObject>();
					for (JSONObject json : tablelist) {
						if (rejson.get("classId").toString()
								.equals(json.getString("ClassId"))) {
							JSONObject tjson = new JSONObject();
							String courseName = COURSE_PROMPT_DELETE;	
							String type = json.getString("CourseType");
							if (lmap.containsKey(json.getString("CourseId"))) {
								courseName = lmap.get(
										json.getString("CourseId")).getName();
								if (StringUtils.isEmpty(courseName)){
									courseName = COURSE_PROMPT_EMPTY;
								}else{													
		                            if (type.equals("1")){
		                         	    courseName = courseName + LABEL_SINGLE;
		                            }else if(type.equals("2")){
		                         	    courseName = courseName + LABEL_DOUBLE;
		                            }
								}	
							}
							String teacherName = "";
							JSONArray teacherArray = json.getJSONArray("rows");
							if (teacherArray != null 
									&& teacherArray.size()>0){
								for(int k =0;k < teacherArray.size();k++){
									String teacherId = teacherArray.getJSONObject(k).getString("TeacherId");
									String name = TEACHER_PROMPT_DELETE;
									if (tmap.containsKey(teacherId)){
										name = tmap.get(teacherId).getName();
										if (StringUtils.isEmpty(name)){
											name = TEACHER_PROMPT_EMPTY;
										}
									}
									if(k==0){
										teacherName = teacherName + name;
								    }else{
									    teacherName = teacherName+ "," + name;
								    }
								}
							}
							tjson.put("courseName", courseName);
							tjson.put("teacherName", teacherName);

							tjson.put("dayOfWeek", json.getString("DayOfWeek"));
							
							//++++++++++++++++++++++   加入上课时间字段        ++++++++++++++++++++++++
							String lessonOfDay = json.getString("LessonOfDay");
							String grade_id = rejson.getString("gradeId"); 
							
							/*tjson.put("sectionTime", 
									timetableScheduleMap.get(grade_id).get(lessonOfDay));*/							
							//++++++++++++++++++++++++++++  end  ++++++++++++++++++++++++++++
							
							tjson.put("lessonOfDay",
									lessonOfDay);
							tjson.put("courseType", type);
							tblist.add(tjson);
						}
					}
					rejson.put("timetable", tblist);
				}
				for (Iterator<JSONObject> it = resultdate.iterator(); it.hasNext();) {
					JSONObject json = it.next();
					List<JSONObject> glist = (List<JSONObject>) json
							.get("timetable");
					if (glist.size() == 0) {
						it.remove();
					}
				}
				//++++++++++++++++++++++  添加查询数据到缓存	++++++++++++++++++++
				/*String cachedTimetableId = cachedData.getString("timetableId");
				if(null!=cachedTimetableId && !cachedTimetableId.equals(timetableId)){
					cachedData.clear();
				}
				cachedData.put("timetableId", timetableId);
				cachedData.put("classTimetable", resultdate);
				setOutofDate(1, false);*/
				//++++++++++++++++++++++++++++    end     ++++++++++++++++++
				req.getSession().setAttribute("report", resultdate);
				}else{
					code = "1";
					msg = "未设置每天上课节次 ";
				}
			} else {
				code = "1";
				msg = "未编排课表,无法查看 ";
			}
			//++++		start      +++++
			//return getBufferedClassTimetable(timetableId, queryClassId, gradeId);
			//++++     end		++++++++++
		} catch (Exception e) {
			code = "1";
			if (e.getMessage() == null) {
				msg = "发生一个内部的错误";
			} else {
				msg = e.getMessage();
			}
		}
		
		this.setPromptMessage(result, code, msg);
		result.put("data", resultdate);
		return result;
	}
	
	/*
	 * 使用缓存查询
	 * 
	 */
	private List<JSONObject> getBufferedCourses(List<JSONObject> gradeList,String queryGradeId,String queryCourseId,int queryType){
		// 有对应的缓存数据
		if(null!=gradeList){	
			List<JSONObject> copyGradeList = new ArrayList<JSONObject>();
			for(JSONObject grade : gradeList){
				JSONObject copyGrade = BeanTool.castBeanToFirstLowerKey(grade);
				copyGradeList.add(copyGrade);
			}
			List<JSONObject> queryGradeList = new ArrayList<JSONObject>();
			for(JSONObject grade : copyGradeList){	//迭代 年级-教师列表				
				grade.remove("tlist");
				JSONObject queryGrade = new JSONObject();				
				if(null!=queryGradeId
						&&queryGradeId.split(",").length==1){	//年级查询条件
					//String [] ids = gradeIds.split(",");
					List<JSONObject> data = (ArrayList<JSONObject>)grade.get("data");
					String gradeId = (String)data.get(0).get("GradeId");
					if(queryGradeId.equals(gradeId)){
						Set<String>gradeKeys = grade.keySet();
						for(String key : gradeKeys){
							queryGrade.put(key, grade.get(key));
						}
					}else{
						continue;
					}
				} else { //所有年级
					queryGrade = grade;
					/*queryGradeList = gradeList;
					break;*/
				}
				
				List<JSONObject> data = (List<JSONObject>) grade
						.get("data");
				List<JSONObject> courseList = null;
				
				switch(queryType){
				case 1:	//班级总课表
					courseList = getBufferedClassCourses(data, queryCourseId);
					break;
				case 2:	//教师总课表
					courseList = getBufferedTeacherCourses(data, queryGradeId, queryCourseId);
					break;
				}

				// 添加年级的教师课程信息
				if(courseList.size()!=0){
					queryGrade.put("data", courseList);
					queryGradeList.add(queryGrade);
				}
			}			
			return queryGradeList;	
		}
		return null;
	}
	
	private List<JSONObject> getBufferedClassCourses(List<JSONObject> data,String queryCourseId){
		List<JSONObject> courseList = new ArrayList<JSONObject>();
		for(JSONObject cls : data){		//迭代 教师-课程列表
			//JSONObject copyClass = BeanTool.castBeanToFirstLowerKey(cls);			
			JSONObject copyClass = null;
			//设置了科目查询条件
			if(null!=queryCourseId && !queryCourseId.equals("") && !queryCourseId.contains(",")){
				//copyClass.remove("timetable");				
				List<String> courseIds = (ArrayList<String>)cls.get("courseIds");
				if(null!=courseIds && courseIds.contains(queryCourseId)){
					List<JSONObject> timetable = (ArrayList<JSONObject>)cls.get("timetable");//teacherCourses.add(teacher);
					List<JSONObject> queryTimetable = new ArrayList<JSONObject>();
					for(JSONObject course : timetable){
						if(queryCourseId.equals(course.get("courseId"))){
							queryTimetable.add(course);
						}
					}
					copyClass = BeanTool.castBeanToFirstLowerKey(cls);
					copyClass.remove("timetable");
					copyClass.put("timetable", queryTimetable);
				}
			}else{
				copyClass = BeanTool.castBeanToFirstLowerKey(cls);
			}
			if(null!=copyClass){
				courseList.add(copyClass);
			}
			/*else{	// 查询年级下所有的教师-课程 
				courseList.add(copyClass) ;
				break;
			}*/
		}
		return courseList;
	}
		
	private List<JSONObject> getBufferedTeacherCourses(List<JSONObject> data,String gradeId,String queryCourseId){
		List<JSONObject> teacherCourses = new ArrayList<JSONObject>();
		for(JSONObject teacher : data){		//迭代 教师-课程列表
			if(null!=queryCourseId && !queryCourseId.equals("") && !queryCourseId.contains(",")){ //设置了科目查询条件
				List<String> courseIds = (ArrayList<String>)teacher.get("courseIds");
				JSONObject copyTeacher = BeanTool.castBeanToFirstLowerKey(teacher);
				
				/*copyTeacher.remove("timetable");
				List<JSONObject> copyTimetable = new ArrayList<JSONObject>();
				List<JSONObject> timetable = (ArrayList<JSONObject>) teacher.get("timetable");
				for(JSONObject object : timetable){
					if(queryCourseId.equals(object.getString("courseId"))){
						copyTimetable.add(object);
					}
				}
				if(copyTimetable.size()==0){//查询没有对应的课程
					continue;
				}
				copyTeacher.put("timetable", copyTimetable);*/
				if(courseIds.contains(queryCourseId)){
					teacherCourses.add(copyTeacher);
				}
				/*if(null!=courseIds && courseIds.contains(queryCourseId)){
					teacherCourses.add(copyTeacher);							
				}*/
			}else{	// 年级下所有的教师-课程 
				teacherCourses = data ;
				break;
			}
		}
		return teacherCourses;
	}
	/**
	 * 
	 * 年级课表
	 * 
	 * @author XFQ
	 * @param req
	 * @param res
	 * @param reqData
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "lookup/getSchoolTimetable.do", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getSchoolTimetable(HttpServletRequest req,
			HttpServletResponse res, @RequestBody JSONObject reqData)  {
		JSONObject result = new JSONObject();
		String timetableId = reqData.getString("timetableId");//课表
		String viewType = reqData.getString("viewType");	//1:班级总课表,2:教师总课表
		String gradeId = reqData.getString("gradeId");	//年级
		
		//+++++++++++++++++++++++  使用缓存数据查询    ++++++++++++++++++++++++
		String queryCourseId = reqData.getString("subjectIds");
/*		if(null!=timetableId 
				&& timetableId.equals(cachedData.get("timetableId"))){
			List<JSONObject> cachedResult = new ArrayList<JSONObject>();
			//缓存下来的所有数据
			List<JSONObject> gradeList = null;
			boolean haveCached = true;
			switch(Integer.parseInt(viewType)){
			case 1:	// 班级总课表
				gradeList = (ArrayList<JSONObject>)cachedData.get("class");	
				if(gradeList==null||outofDate[3]){
					haveCached = false;
					break;
				}
				cachedResult = getBufferedCourses(gradeList, gradeId, queryCourseId, 1);
				break;
			case 2: // 教师总课表
				gradeList = (ArrayList<JSONObject>)cachedData.get("teacher");	
				if(gradeList==null||outofDate[2]){
					haveCached = false;
					break;
				}
				cachedResult = getBufferedCourses(gradeList, gradeId, queryCourseId, 2);
				break;
			}
			
			if(haveCached){
				String code="0", msg="返回成功！";
				if(cachedResult.size()==0){
					code="1";
					msg="未编排课表,无法查看！";
				}
				req.getSession().setAttribute("report", cachedResult);
				setPromptMessage(result, code, msg);
				req.getSession().setAttribute("report", cachedResult);
				setPromptMessage(result, code, msg);
				result.put("data", cachedResult);
				return result;
			}
		}*/
		//+++++++++++++++++++++++    end    ++++++++++++++++++++++++++++++++++
		
		String termInfoId = reqData.getString("selectedSemester");	//学期
		String schoolYear = "";
		String schoolId = getXxdm(req);	//学校代码(session)
		School school = this.getSchool(req,termInfoId);
		String code = "0";
		String msg = "";
		
			
		
		List<JSONObject> graderesult = new ArrayList<JSONObject>();
		try{
			if (termInfoId.length() > 4){
				schoolYear = termInfoId.substring(0, 4);
			}
			//+++++++++++++++++++++++++   查询课表对应的所有年级        +++++++++++++++++++++++++++++
			
/*			List<Long> gids = new ArrayList<Long>();
			List<Grade> gradeList = commonDataService.getGradeList(school, termInfoId);
			for(Grade grade : gradeList){				
				String curLevel = grade.currentLevel.getValue()+"";
				
				String gradeID = commonDataService.ConvertNJDM2SYNJ(curLevel, schoolYear);
				gids.add(Long.parseLong(gradeID));
			}
			
			
			
			StringBuffer sb = new StringBuffer();
			
			List<Long> ids = new ArrayList<Long>();
			Object[] gid = gids.toArray();//gradeId.split(","); 
			for (Object id : gid) {
				ids.add((Long)id);
				sb.append(id+",");
			}
			sb.substring(0, sb.length()-1);//[,)
			String queryGradeId = sb.toString();*/
			//++++++++++++++++++++++++++++++++++++   end    +++++++++++++++++++++++++++++++++++++
			List<String>ids = Arrays.asList(gradeId.split(","));
			
			HashMap<String, Object> cmap = new HashMap<String, Object>();
			cmap.put("schoolId", schoolId);
			cmap.put("termInfoId", termInfoId);
			cmap.put("usedGradeId", gradeId);//使用年级
			
			List<Classroom> classlist = commonDataService.getClassList(cmap);// 班级信息
			HashMap<String, Classroom> clasmap = new HashMap<String, Classroom>();
			List<Long> claslist = new ArrayList<Long>();
			for (Classroom cm : classlist) {
				clasmap.put(String.valueOf(cm.getId()), cm);
				claslist.add(cm.getId());
			}
			HashMap<String, Object> tablemap = new HashMap<String, Object>();
			tablemap.put("schoolId", schoolId);
			tablemap.put("timetableId", timetableId);
			tablemap.put("ClassIds", claslist);
			tablemap.put("GradeId", ids);

			List<JSONObject> tablelist = timetableService
					.getTimetablePie(tablemap);// 课表信息
			if (tablelist != null && tablelist.size() > 0) {
				
				List<JSONObject> gradeSetlist = timetableService
						.getGradeSet(tablemap);// 年级课表安排
				if(gradeSetlist!=null&&gradeSetlist.size()>0){
					
				HashMap<String, Account> teamap = new HashMap<String, Account>();
				List<Account> teacherlist = commonDataService
						.getAllSchoolEmployees(school,termInfoId,"");// 学校的老师			
				for (Account a : teacherlist) {
					 teamap.put(String.valueOf(a.getId()), a);
				}
				// 科目信息
				List<LessonInfo> lilist = commonDataService
						.getLessonInfoList(school, termInfoId);
				HashMap<String, LessonInfo> lmap = new HashMap<String, LessonInfo>();
				for (LessonInfo l : lilist) {
					lmap.put(String.valueOf(l.getId()), l);
				}
				HashSet<String> t = new HashSet<String>();
				List<Long> tid = new ArrayList<Long>();
				List<Long> ccid = new ArrayList<Long>();
				HashSet<String> s = new HashSet<String>();
				for (JSONObject json : tablelist) {
					if (json.getString("TeacherId") != null) {
						t.add(json.getString("TeacherId"));
					}
					s.add(json.getString("ClassId"));					
				}
				Iterator<String> iterator = s.iterator();
				while (iterator.hasNext()) {
					ccid.add(Long.valueOf(iterator.next()));
				}
				Iterator<String> iterat = t.iterator();
				while (iterat.hasNext()) {
					tid.add(Long.valueOf(iterat.next()));
				}
				if (viewType.equals("2")) {// 老师		
					//预排活动
					HashMap<String, Object> reasmap = new HashMap<String, Object>();
					reasmap.put("timetableId", timetableId);
					reasmap.put("schoolId", schoolId);
					List<JSONObject> researchlist = timetableService.getTeacherResearch(reasmap);
					// 处理合班,单双周在前端处理
					HashMap<String, JSONObject> hsmap = new HashMap<String, JSONObject>();
					List<JSONObject> list = new ArrayList<JSONObject>();				
					for (JSONObject json : tablelist) {
						 String key = json.getString("DayOfWeek")
								+ json.getString("LessonOfDay") + "&"
								+ json.getString("TeacherId") + "&"
								+ json.getString("CourseType");	
						 String targetId = json.getString("ClassId");
						 if (hsmap.get(key) == null) {			
							 json.put("ClassId", targetId);
							 list.add(json); 
							 hsmap.put(key, json);
						 }else{
							 JSONObject sJson = hsmap.get(key);
							 String sourceId = sJson.getString("ClassId");
	                         sJson.put("ClassId", sourceId + "," + targetId);						 
						 }					 
					}					
					for (JSONObject gjson : gradeSetlist) {
						JSONObject gradejson = new JSONObject();
						String gradeName = commonDataService
								.getGradeNameBySynj(String.valueOf(gjson
										.getString("GradeId")), schoolYear);
						gradejson.put("totalMaxDays",
								gjson.getString("MaxDaysForWeek"));
						gradejson.put(
								"totalMaxLessons",
								Integer.valueOf(gjson.getString("AMLessonNum")
										.toString())
										+ Integer.valueOf(gjson.getString(
												"PMLessonNum").toString()));
						gradejson.put("GradeId", gjson.getString("GradeId"));
						gradejson.put("gradeName", gradeName);
						graderesult.add(gradejson);
					}
					List<JSONObject> resultdate1 = new ArrayList<JSONObject>();
					for (JSONObject gjson : graderesult) {
						JSONObject gj = new JSONObject();
						HashMap<String, Object> clamap = new HashMap<String, Object>();
						clamap.put("schoolId", schoolId);
						clamap.put("termInfoId", termInfoId);
						clamap.put("usedGradeId", gjson.getString("GradeId"));
						List<Classroom> clas = commonDataService
								.getClassList(clamap);
						Set<JSONObject> llist = new HashSet<JSONObject>();
						for (Classroom cm : clas) {
							for (JSONObject json : list) {
								if (json.getString("ClassId").contains(
										String.valueOf(cm.getId()))) {
									llist.add(json);
								}
							}
						}
						gj.put("GradeId", gjson.getString("GradeId"));
						gj.put("data", llist);
						resultdate1.add(gj);
					}
					HashMap<String, JSONObject> rmap = new HashMap<String, JSONObject>();
					for (JSONObject rjson : resultdate1) {
						rmap.put(rjson.getString("GradeId"), rjson);
					}
					List<JSONObject> gglist = new ArrayList<JSONObject>();
					for (JSONObject gjson : graderesult) {						
						JSONObject h = new JSONObject();
						if (rmap.get(gjson.getString("GradeId")) != null) {
							Set<JSONObject> li = (HashSet<JSONObject>) rmap.get(
									gjson.getString("GradeId")).get("data");
							if (li != null && li.size() > 0) {													
								HashMap<String, Object> hmap = new HashMap<String, Object>();
								for (JSONObject json : li) {								
									if(json.getString("TeacherId")!=null){
										String key = json.getString("TeacherId");
										hmap.put(key, json.getString("TeacherId"));
									}					
								}
								Set<Entry<String, Object>> set1 = hmap
										.entrySet();
								List<Object> tids = new ArrayList<Object>();
								for (Iterator<Entry<String, Object>> it = set1
										.iterator(); it.hasNext();) {
									Entry<String, Object> entry = it.next();
									tids.add(entry.getValue());
								}
								h.put("GradeId", gjson.getString("GradeId"));
								h.put("tid", tids);
								h.put("totalMaxLessons",
										gjson.getString("totalMaxLessons"));
								h.put("gradeName", gjson.getString("gradeName"));
								h.put("tlist", li);
								gglist.add(h);
								gglist = (List<JSONObject>) Sort.sort(SortEnum.ascEnding0rder, gglist, "gradeName");
							}
						}
					}
					for (JSONObject gjson : gglist) {
						if (rmap.get(gjson.getString("GradeId")) != null) {
							Set<JSONObject> li = (HashSet<JSONObject>) rmap.get(
									gjson.getString("GradeId")).get("data");
							if (li != null && li.size() > 0) {
								List<Object> tllist = (List<Object>) gjson
										.get("tid");
								List<JSONObject> datalist = new ArrayList<JSONObject>();
								for (Object o : tllist) {
									for (JSONObject json : li) {
										if (o.toString().equals(
												json.getString("TeacherId"))) {
											if (teamap.get(o.toString()) != null) {
												JSONObject data = new JSONObject();												
												String name = teamap.get(o.toString()).getName();
												data.put("teacherName", name);
												if(StringUtils.isEmpty(name))continue;
												data.put("TeacherId", json.getString("TeacherId"));
												data.put("GradeId", gjson
														.getString("GradeId"));
												datalist.add(data);
												gjson.put(
														"totalMaxDays",
														json.getString("MaxDaysForWeek"));
												gjson.put(
														"tableName",
														json.getString("TimetableName")
																+ "("
																+ gjson.getString("gradeName")
																+ ")");
												gjson.put("data", datalist);
												break;
											}
										}
									}
								}
							}

						}
					}
					for (Iterator<JSONObject> it = gglist.iterator(); it.hasNext();) {
						JSONObject json = it.next();
						List<JSONObject> glist = (List<JSONObject>) json
								.get("data");
						if (glist == null) {
							it.remove();
						}
					}
					for (JSONObject gjson : gglist) { //年级数
						List<JSONObject> glist = (List<JSONObject>) gjson
								.get("data");
						Set<JSONObject> timelist = (HashSet<JSONObject>) gjson
								.get("tlist");
						List<JSONObject> queryList = new ArrayList<JSONObject>();
 						for (JSONObject gl : glist) 
 						{	//教师数 							
 							List<String> courseIds = new ArrayList<String>();
							HashMap<String,JSONObject> tbmap = new HashMap<String,JSONObject>();
							boolean isTargetTeacher = false;
							for (JSONObject json : timelist) {	//任课数
								if (json.getString("TeacherId")!=null&&json.getString("TeacherId").equals(
										String.valueOf(gl.get("TeacherId")))) {
									JSONObject tdata = new JSONObject();								
									/*
									 * 添加教师任教课程ID
									 */
									String courseId = json.getString("CourseId");
									if(!courseIds.contains(courseId)){
										courseIds.add(courseId);
									}
									//+++++++++++++++++++++++++++   添加按课程查询         ++++++++++++++++++++++++
									//设置了课程查询条件，忽略非查询课程
								/*	if(queryCourseId!=null&&!queryCourseId.equals(courseId)){
										continue;
									}	
									isTargetTeacher = true;*/
									//+++++++++++++++++++++++++++       end     ++++++++++++++++++++++++++
									
									String simpleName = COURSE_PROMPT_DELETE;
									String courseAllName = COURSE_PROMPT_DELETE;
									String courseName ="";
									if (lmap.containsKey(courseId)){
										courseAllName = lmap.get(json.getString("CourseId")).getName();
										if (StringUtils.isEmpty(courseAllName)){
											courseAllName = COURSE_PROMPT_EMPTY;
										}else{
											courseName = courseAllName.substring(0, 1);
										}
										simpleName = lmap.get(json.getString("CourseId")).getSimpleName();
										if (StringUtils.isEmpty(simpleName)){
											if (StringUtils.isNotEmpty(courseName)){
												simpleName = courseName;
											}else{
												simpleName = COURSE_PROMPT_EMPTY;
											}	
										}										
									}	
									String type = json.getString("CourseType");
									if (type.equals("1")){
										simpleName = simpleName + LABEL_SINGLE;
										courseAllName = courseAllName + LABEL_SINGLE;
	                                }else if(type.equals("2")){
	                                	simpleName = simpleName + LABEL_DOUBLE;
	                             	    courseAllName = courseAllName + LABEL_DOUBLE;
	                                }
									String[] nameid = json.get("ClassId")
											.toString().split(",");
									List<String> namelist = new ArrayList<String>();
									for(String id : nameid) {
										String name = CLASS_PROMPT_DELETE;
										if (clasmap.containsKey(id)) {
											name = clasmap.get(id).getClassName();
											if (StringUtils.isEmpty(name)){
												name = CLASS_PROMPT_EMPTY;
											}
										}
										if(!namelist.contains(name))namelist.add(name);
									}
									String className = namelist.get(0);
									if (namelist.size() > 1) {
										className = StringUtils.join(namelist, ",");
									}
									tdata.put("courseId",json.getString("CourseId"));
									tdata.put("courseName", simpleName);
									tdata.put("courseAllName", courseAllName);
									tdata.put("dayOfWeek",
											json.getString("DayOfWeek"));
									tdata.put("lessonOfDay",
											json.getString("LessonOfDay"));
									tdata.put("className", className);
									tdata.put("courseType", type);		
									tbmap.put(json.getString("DayOfWeek")+json.getString("LessonOfDay")
											+json.getString("TeacherId")+type, tdata);
								}
							}
							
							// 教研活动
							if (researchlist != null && researchlist.size() > 0/* && queryCourseId==null*/){						
								for(JSONObject reajson : researchlist){
									if (reajson.getString("TeacherId").equals(String.valueOf(gl.get("TeacherId")))) {
										String key = reajson.getString("DayOfWeek")+reajson.getString("LessonOfDay")+reajson.getString("TeacherId");
										if (tbmap.get(key + "1")==null && tbmap.get(key + "2")==null && tbmap.get(key + "0")==null){							
											JSONObject tjson = new JSONObject();
											tjson.put("dayOfWeek",
													reajson.getString("DayOfWeek"));
											tjson.put("lessonOfDay",
													reajson.getString("LessonOfDay"));
											tjson.put("className", "");
											String gName = reajson.getString("TeacherGroupName");
											String cName = "";
											if (gName.length() > 1) {
												cName = gName.substring(0,1);
											}
											tjson.put("courseName", cName);
											tjson.put("courseAllName", gName);
											tbmap.put(key + "0",tjson);												
										}
									}
								}
							}
							Iterator<Entry<String, JSONObject>> ite = tbmap.entrySet().iterator();
							List<JSONObject> tblis = new ArrayList<JSONObject>();
							while (ite.hasNext()) {
								Map.Entry<String, JSONObject> entry =  ite.next();
								tblis.add(entry.getValue());
							}
							gl.put("timetable", tblis);
							//+++++++++++++++++++   添加查询课表功能         ++++++++++++++++++++							
							/*if(isTargetTeacher){
								queryList.add(gl);
								continue;
							}*/								
							//+++++++++++++++++++++++++    end    —++++++++++++++++++++
							gl.put("courseIds", courseIds);
 						}
 						//++++++++++++++++++++++++   移除非查询科目         ++++++++++++++++++++++++++
 						/*glist.clear();
 						glist.addAll(queryList);*/
 						// ++++++++++++++++++++++++      end     ++++++++++++++++++++++++++
					}
					//++++++++++++++++              +++++++++++++++++++++++++
					/*String cachedTimetableId = cachedData.getString("timetableId");
					if(null!=cachedTimetableId && !cachedTimetableId.equals(timetableId)){
						cachedData.clear();
					}
					cachedData.put("teacher", gglist);
					cachedData.put("timetableId", timetableId);
					setOutofDate(3, false);*/
					//+++++++++++++++++                 =++++++++++++++++=+++
					graderesult = gglist;
					//graderesult = getBufferedCourses(graderesult, gradeId, queryCourseId, 1);
					//req.getSession().setAttribute("report", graderesult);
				} else {	//处理教师总课表
					// 处理合班,单双周在前端处理
					HashMap<String, JSONObject> hsmap = new HashMap<String, JSONObject>();
					List<JSONObject> list = new ArrayList<JSONObject>();				
					for (JSONObject json : tablelist) {
						 String key = json.getString("DayOfWeek")
								+ json.getString("LessonOfDay") + "&"
								+ json.getString("ClassId") + "&"
								+ json.getString("CourseType");	
						 String targetId = json.getString("TeacherId");
						 if (hsmap.get(key) == null) {	
							 if(StringUtils.isEmpty(targetId))targetId="";
							 json.put("TeacherId", targetId);
							 list.add(json); 
							 hsmap.put(key, json);
						 }else{
							 JSONObject sJson = hsmap.get(key);
							 String sourceId = sJson.getString("TeacherId");
							 if (StringUtils.isEmpty(sourceId)){
								 if(StringUtils.isNotEmpty(targetId)) {
									sJson.put("TeacherId", targetId); 
								 }
							 }else{
								 if(StringUtils.isNotEmpty(targetId)) {
										sJson.put("TeacherId", sourceId + "," + targetId); 
								 }else{
									 sJson.put("TeacherId", sourceId); 
								 }
							 }					 
						 }					 
					}
					for (JSONObject gjson : gradeSetlist) {
						 JSONObject gradejson = new JSONObject();
						 String gradeName = commonDataService
								.getGradeNameBySynj(String.valueOf(gjson
										.getString("GradeId")), schoolYear);
						 gradejson.put("totalMaxDays",
								gjson.getString("MaxDaysForWeek"));
						 gradejson.put(
								"totalMaxLessons",
								Integer.valueOf(gjson.getString("AMLessonNum")
										.toString())
										+ Integer.valueOf(gjson.getString(
												"PMLessonNum").toString()));
						 gradejson.put("GradeId", gjson.getString("GradeId"));
						 gradejson.put("gradeName", gradeName);
						 graderesult.add(gradejson);
					}
					graderesult = (List<JSONObject>) Sort.sort(SortEnum.ascEnding0rder, graderesult, "gradeName");
					for (JSONObject gjson : graderesult) {
						List<JSONObject> resultdate1 = new ArrayList<JSONObject>();
						HashMap<String, Object> clamap = new HashMap<String, Object>();
						clamap.put("schoolId", schoolId);
						clamap.put("termInfoId", termInfoId);
						clamap.put("usedGradeId", gjson.getString("GradeId"));
						List<Classroom> clas = commonDataService
								.getClassList(clamap);
						for (Classroom cl : clas) {
							for (JSONObject json : list) {
								if (json.getString("ClassId").equals(
										String.valueOf(cl.getId()))) {
									JSONObject data = new JSONObject();									
									String name = cl.getClassName();
									if(StringUtils.isEmpty(name))continue;
									data.put("className", name);
									data.put("ClassId", cl.getId());
									data.put("GradeId",
											gjson.getString("GradeId"));
									resultdate1.add(data);
									gjson.put("totalMaxDays",
											json.getString("MaxDaysForWeek"));
									gjson.put(
											"tableName",
											json.getString("TimetableName")
													+ "("
													+ gjson.getString("gradeName")
													+ ")");
									gjson.put("data", resultdate1);
									break;
								}
							}
						}
					}
					for (Iterator<JSONObject> it = graderesult.iterator(); it.hasNext();) {
						JSONObject json = it.next();
						List<JSONObject> glist = (List<JSONObject>) json
								.get("data");
						if (glist == null) {
							it.remove();
						}
					}
					for (JSONObject gjson : graderesult) {	//年级
						List<JSONObject> glist = (List<JSONObject>) gjson
								.get("data");
						List<JSONObject> queryList =new ArrayList<JSONObject>();
						for (JSONObject gl : glist) {	//班级
							List<JSONObject> tlist = new ArrayList<JSONObject>();
							List<String> courseIds = new ArrayList<String>();
							
							boolean isTargetTeacher = false;
							for (JSONObject json : list) {	//课程
								if (json.getString("ClassId").equals(
										String.valueOf(gl.get("ClassId")))) {
									JSONObject tdata = new JSONObject();									
									String courseId = json.getString("CourseId");

									//+++++++++++++++++++++++++++   添加按课程查询         ++++++++++++++++++++++++
									//设置了课程查询条件，忽略非查询课程
									/*if(queryCourseId!=null&&!queryCourseId.equals(courseId)){
										continue;
									}	
									isTargetTeacher = true;*/
									if(!courseIds.contains(courseId)){										
										courseIds.add(courseId);
									}
									//+++++++++++++++++++++++++++       end    ++++++++++++++++++++++++++
									
									String simpleName = COURSE_PROMPT_DELETE;
									String courseAllName = COURSE_PROMPT_DELETE;
									String courseName ="";
									if (lmap.containsKey(courseId)){
										courseAllName = lmap.get(json.getString("CourseId")).getName();
										if (StringUtils.isEmpty(courseAllName)){
											courseAllName = COURSE_PROMPT_EMPTY;
										}else{
											courseName = courseAllName.substring(0, 1);
										}
										simpleName = lmap.get(json.getString("CourseId")).getSimpleName();
										if (StringUtils.isEmpty(simpleName)){
											if (StringUtils.isNotEmpty(courseName)){
												simpleName = courseName;
											}else{
												simpleName = COURSE_PROMPT_EMPTY;
											}	
										}										
									}
									String type = json.getString("CourseType");
									if (type.equals("1")){
										simpleName = simpleName + LABEL_SINGLE;
										courseAllName = courseAllName + LABEL_SINGLE;
	                                }else if(type.equals("2")){
	                                	simpleName = simpleName + LABEL_DOUBLE;
	                             	    courseAllName = courseAllName + LABEL_DOUBLE;
	                                }
									String teachers = json.get("TeacherId").toString();
									List<String> namelist = new ArrayList<String>();
									if (StringUtils.isNotEmpty(teachers)){
										String[] nameid = teachers.split(",");
										for(String id : nameid) {
											String name = TEACHER_PROMPT_DELETE;
											if (teamap.containsKey(id)) {
												name = teamap.get(id).getName();
												if (StringUtils.isEmpty(name)){
													name = TEACHER_PROMPT_EMPTY;
												}
											}
											if(!namelist.contains(name))namelist.add(name);
										}
									}
									String teacherName = "";
									if (namelist.size() > 1) {
										teacherName = StringUtils.join(namelist, ",");
									}else if(namelist.size() == 1){
										teacherName = namelist.get(0);
									}
									tdata.put("dayOfWeek",
											json.getString("DayOfWeek"));
									tdata.put("lessonOfDay",
											json.getString("LessonOfDay"));
									tdata.put("courseAllName", courseAllName);
									tdata.put("teacherName", teacherName);
									tdata.put("courseName", simpleName);
									tdata.put("courseType", json.getString("CourseType"));
									tdata.put("courseId", courseId);
									tlist.add(tdata);
								}
								
							}
							gl.put("timetable", tlist);
							//+++++++++++++++++++++++++    end    —++++++++++++++++++++
							gl.put("courseIds", courseIds);
							//+++++++++++++++++++   添加查询课表功能         ++++++++++++++++++++							
							/*if(isTargetTeacher){
								queryList.add(gl);
								continue;
							}*/	
							
							//+++++++++++++++++++++++++    end    —++++++++++++++++++++
							
						}
						//++++++++++++++++++++++++++++     添加查询科目              +++++++++++++++++++++++++++++++
						/*glist.clear();
						glist.addAll(queryList);*/
						//++++++++++++++++++++++++++++     添加查询科目              +++++++++++++++++++++++++++++++
						gjson.put("data", Sort.sort(SortEnum.ascEnding0rder, glist, "className"));
					}
					//++++++++++++++++              +++++++++++++++++++++++++
					/*String cachedTimetableId = cachedData.getString("timetableId");
					if(null!=cachedTimetableId && !cachedTimetableId.equals(timetableId)){
						cachedData.clear();
					}
					cachedData.put("class", graderesult);
					cachedData.put("timetableId", timetableId);
					setOutofDate(2, false);*/
					//+++++++++++++++++                 =++++++++++++++++=+++
					//graderesult = getBufferedCourses(graderesult, gradeId, queryCourseId, 2);
					//req.getSession().setAttribute("report", graderesult);
				}
				}else{
					code = "1";
					msg = "未设置每天上课节次";
				}
			} else {
				code = "1";
				msg = "未编排课表,无法查看";
			}
		} catch (Exception e) {
			code = "1";
			if (e.getMessage() == null) {
				msg = "发生一个内部的错误";
			} else {
				msg = e.getMessage();
			}
		}
		//+++++++++++++++++++++++++++++++       ++++++++++++++++++++++++++++++
		//第一次就设置了查询条件
/*		if(gradeId.split(",").length==1 || queryCourseId.split(",").length == 1){
			List<JSONObject> cachedResult = new ArrayList<JSONObject>();
			//缓存下来的所有数据
			List<JSONObject> gradeList = null;
			switch(Integer.parseInt(viewType)){
			case 1:	// 班级总课表
				gradeList = (ArrayList<JSONObject>)cachedData.get("class");
				cachedResult = getBufferedCourses(gradeList, gradeId, queryCourseId, 1);
				break;
			case 2: // 教师总课表
				gradeList = (ArrayList<JSONObject>)cachedData.get("teacher");			
				cachedResult = getBufferedCourses(gradeList, gradeId, queryCourseId, 2);
				break;
			}
			
			if(null!=cachedResult){				
				req.getSession().setAttribute("report", cachedResult);
				
				if(0 == cachedResult.size()){
					code = "1";
					msg = "未编排课表,无法查看";
				}
				setPromptMessage(result, code, msg);
				result.put("data", cachedResult);
				return result;
			}
		}*/
		//过滤科目
		int queryType = Integer.parseInt(viewType);
		
		graderesult = getBufferedCourses(graderesult, gradeId, queryCourseId, queryType);
		//++++++++++++++++++++++++++++++++++        +++++++++++++++++++++++++++
		if(graderesult.size()==0){
			code = "1";
			msg = "未编排课表,无法查看";
		}
		// 有问题 后期提出
		this.setPromptMessage(result, code, msg);
		result.put("data", graderesult);
		req.getSession().setAttribute("report", graderesult);
		return result;
	}
	
	/**
	 * 根据课表ID及学段查询课表上课时间
	 */
	@RequestMapping(value = "lookup/getSchoolTimetableSchedule.do", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getSchoolTimetableSchedule(HttpServletRequest req,
			HttpServletResponse res, @RequestBody JSONObject reqData) {
		JSONObject result = new JSONObject();
		String timetableId = (String)reqData.get("timetableId");
		String pycc = (String)reqData.get("pycc");//培养层次
		int code = 1;
		String msg = "查询失败！";
		List<HashMap<String,Object>> timetableScheduleList = null;
		try {
			timetableScheduleList = timetableService.getTimetableSchedule(timetableId, pycc);			
			List<JSONObject> scheduleList = new ArrayList<JSONObject>();
			for(HashMap<String,Object> schedule : timetableScheduleList){
				JSONObject sigleSchedule = new JSONObject();
				Set<String> keys = schedule.keySet();
				for(String key : keys){
					sigleSchedule.put(key, schedule.get(key));
				}				
				scheduleList.add(sigleSchedule);
			}
			code = 0;
			msg="查询成功！";
			result.put("data", timetableScheduleList);
		} catch (Exception e) {
			result.put("data", "[]");
		}
		//获取最大节次数
		int totalMaxLessons=0,amLessonNum=0;
 		String schoolId = getXxdm(req);
		reqData.put("schoolId",schoolId);
		JSONObject sectionInfor = timetableService.getTimetableSection(reqData);
		if (sectionInfor!=null   ) {
		String selectedSemester =reqData.getString("selectedSemester");
		String xn = selectedSemester.substring(0, selectedSemester.length() - 1);
		JSONArray jsonArray = sectionInfor.getJSONArray("rows");
		if (jsonArray!=null && jsonArray.size() > 0) {
			for (int i = 0; i < jsonArray.size(); i++) {
						JSONObject object =jsonArray.getJSONObject(i) ;
						String usedGrade = object.getString("gradeId");
					T_GradeLevel gLevel = T_GradeLevel
							.findByValue(Integer.parseInt(commonDataService.ConvertSYNJ2NJDM(usedGrade, xn)));
						String pyccs = commonDataService.getPYCCByNJCode(gLevel.getValue()) +"";
						int totalMaxLessonstemp = 0;
						int amLessonNumtemp = 0;
						if (pyccs.equals(pycc)) {
							amLessonNumtemp = object.getIntValue("amLessonNum");
							totalMaxLessonstemp = amLessonNumtemp + object.getIntValue("pmLessonNum");
							amLessonNum = amLessonNum > amLessonNumtemp ?amLessonNum:amLessonNumtemp;
							totalMaxLessons = totalMaxLessons > totalMaxLessonstemp?totalMaxLessons:totalMaxLessonstemp;
						}
			}
		}
	   }
		 
 
		if(code==0
				&& result.getJSONArray("data").size()==0){ 
			List<JSONObject> scheduleList = new ArrayList<JSONObject>();
			int startHour = 8,startMinites=30;		
			for(int i=0; i<amLessonNum; i++){
				JSONObject schedule = new JSONObject();			
				schedule.put("sectionId", i);
				StringBuffer startTime = new StringBuffer();
				if(startHour<10){
					startTime.append("0");
				}
				if(startMinites<10){
					startTime.append(startHour+":0"+startMinites);
				}else {
					startTime.append(startHour+":"+startMinites);
				}
				 
				schedule.put("startTime", startTime.toString());
				//上课40分钟
				startHour += (startMinites+40)/60;
				startMinites = (startMinites+40)%60;
				StringBuffer endTime = new StringBuffer();
				if(startHour<10){
					endTime.append("0");
				}
				if(startMinites<10){
					endTime.append(startHour+":0"+startMinites);
				}else {
					endTime.append(startHour+":"+startMinites);
				}
				
				schedule.put("endTime", endTime.toString());
				scheduleList.add(schedule);
				//课间10分钟
				startHour += (startMinites+10)/60;
				startMinites = (startMinites+10)%60;
			}
			 startHour = 14;
			 startMinites=0;	
			for(int i=amLessonNum; i<totalMaxLessons; i++){

				JSONObject schedule = new JSONObject();			
				schedule.put("sectionId", i);
				StringBuffer startTime = new StringBuffer();
				if(startMinites<10){
					startTime.append(startHour+":0"+startMinites);
				}else {
					startTime.append(startHour+":"+startMinites);
				}
				
				schedule.put("startTime", startTime.toString());
				//上课40分钟
				startHour += (startMinites+40)/60;
				startMinites = (startMinites+40)%60;
				StringBuffer endTime = new StringBuffer();
				if(startMinites<10){
					endTime.append(startHour+":0"+startMinites);
				}else {
					endTime.append(startHour+":"+startMinites);
				}
				
				schedule.put("endTime", endTime.toString());
				scheduleList.add(schedule);
				//课间10分钟
				startHour += (startMinites+10)/60;
				startMinites = (startMinites+10)%60;
			}
			result.put("data", scheduleList);
			//cachedData.put(timetableId+pycc, scheduleList);
		}
		result.put("amLessonNum", amLessonNum);
		result.put("totalMaxLessons", totalMaxLessons);
		result.put("code", code);
		result.put("msg", msg);
				
		return result;
	}
	
	@RequestMapping(value = "setup/setSchoolTimetableSchedule.do", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject setSchoolTimetableSchedule(HttpServletRequest req,
			HttpServletResponse res, @RequestBody JSONObject reqData) {
		JSONArray input = new JSONArray();
		JSONObject result = new JSONObject();
		if(null!=reqData.get("data")){
			input = reqData.getJSONArray("data");			
		}else{
			result.put("msg", "修改课表时间数据为空！");
			result.put("code", -1);
			return result;
		}
		List<JSONObject> data = new ArrayList<JSONObject>();
		for(int i = 0; i<input.size(); i++){
			JSONObject object = input.getJSONObject(i);
			object.put("timetableId", reqData.get("timetableId"));
			object.put("pycc", reqData.get("pycc"));
			data.add(input.getJSONObject(i));
		}
		
		try{
			timetableService.addTimetableSchedule(data);
		}catch (Exception e) {
			result.put("msg", "修改课表时间失败！");
			result.put("code", -2);
		}
		setOutofDate(0,true);
		result.put("msg", "修改成功！");
		result.put("code", 0);
		return result;
	}
	
	/**
	 * 获取时间设置Map对象
	 * @param gradeIdList
	 * @param termInfoId
	 * @param timetableId
	 * @return {String:gradeId,{String:sectionId,String:sectionTime}}
	 */
	private Map<String,ArrayList<JSONObject>> getTimetableScheduleMap(List<String> gradeIdList,String termInfoId,String timetableId){
		Map<String,HashMap<String,String>> timetableScheduleMap = new HashMap<String,HashMap<String,String>>();			
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
		
			timetableScheduleMap.put(gradeId, schedule);
		}
		return gradeMapScheduleTime;//timetableScheduleMap;
	}
	/**
	 * 
	 * 全校课表
	 * 
	 * @author XFQ
	 * @param req
	 * @param res
	 * @param reqData
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "lookup/getAllSchoolTimetable.do", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getAllSchoolTimetable(HttpServletRequest req,
			HttpServletResponse res, @RequestBody JSONObject reqData) {
		JSONObject result = new JSONObject();
		String timetableId = reqData.getString("timetableId");
		String pycc = reqData.getString("pycc");
		String termInfoId = reqData.getString("selectedSemester");
		String schoolId = getXxdm(req);
		long schId = Long.parseLong(schoolId);
		School school = this.getSchool(req,termInfoId);
		String schoolYear = "",code = "0",msg = "";
		int	totalMaxDays = 0,totalMaxLessons = 0;
		JSONObject gradedata = new JSONObject();
		List<JSONObject> graderesult = new ArrayList<JSONObject>();
		try {
             // 学年
			 if (termInfoId.length() > 4){
			 	 schoolYear = termInfoId.substring(0, 4);
			 }
			 // 年级和培养层次关系,获取该培养层次下的年级gradeId(以,分隔)
			 Map<String,List<Long>> pyGradeIds = new HashMap<String, List<Long>>();			 
			 List<Grade> gradeList = commonDataService.getGradeList(school,termInfoId);
			 for(Grade grade : gradeList){
				 String synj = timetableService.convertSYNJByGrade(schId, grade, termInfoId);
				 String pyccdm = commonDataService.getPYCCBySYNJ(synj, schoolYear);
				 long synjdm = Long.parseLong(synj);
				 List<Long> gradeIds = null;
				 if(pyGradeIds.containsKey(pyccdm)){					 
					gradeIds = pyGradeIds.get(pyccdm);
				 }else{
					gradeIds = new ArrayList<Long>();
				 }
				 gradeIds.add(synjdm);
				 pyGradeIds.put(pyccdm, gradeIds);
			 }
			 List<Long> ids = pyGradeIds.get(pycc);
			 String gradeId = StringUtils.join(ids, ",");
			 // 查询培养层次下的班级信息
		     HashMap<String, Object> cmap = new HashMap<String, Object>();
		     cmap.put("schoolId", schoolId);
		     cmap.put("termInfoId", termInfoId);
		     cmap.put("usedGradeId", gradeId);
		     List<Classroom> classlist = commonDataService.getClassList(cmap);	 
		     // 查询培养层次下的课表信息
			 HashMap<String, Classroom> clasmap = new HashMap<String, Classroom>();
			 List<Long> claslist = new ArrayList<Long>();
			 for (Classroom cm : classlist) {
				 clasmap.put(String.valueOf(cm.getId()), cm);
				 claslist.add(cm.getId());
			 }
			 HashMap<String, Object> tablemap = new HashMap<String, Object>();
			 tablemap.put("schoolId", schoolId);
			 tablemap.put("timetableId", timetableId);
			 tablemap.put("ClassIds", claslist);
			 tablemap.put("GradeId", ids);
			 List<JSONObject> tablelist = timetableService
					.getTimetablePie(tablemap);			
			 // 遍历课表信息
			 if (tablelist != null && tablelist.size() > 0) {
				List<JSONObject> gradeSetlist = timetableService
						.getGradeSet(tablemap);
				if(gradeSetlist!=null&&gradeSetlist.size()>0){
				// 全校的老师信息
				HashMap<String, Account> teamap = new HashMap<String, Account>();
				List<Account> teacherlist = commonDataService
						.getAllSchoolEmployees(school,termInfoId,"");			
				for (Account a : teacherlist) {
					 teamap.put(String.valueOf(a.getId()), a);
				}
				// 全校的科目信息(科目Id-account对象)
				List<LessonInfo> lilist = commonDataService
						.getLessonInfoList(school, termInfoId);
				HashMap<String, LessonInfo> lmap = new HashMap<String, LessonInfo>();
				for (LessonInfo l : lilist) {
					 lmap.put(String.valueOf(l.getId()), l);
				}
				// 年级的名称
				for (JSONObject gjson : gradeSetlist) {
					 JSONObject gradejson = new JSONObject();
					 String gradeName = commonDataService
							.getGradeNameBySynj(String.valueOf(gjson
									.getString("GradeId")), schoolYear);					 
					 int maxLessons = Integer.valueOf(gjson.getString("AMLessonNum"))
							+ Integer.valueOf(gjson.getString(
									"PMLessonNum"));					
					 if(maxLessons > totalMaxLessons) totalMaxLessons = maxLessons;
					 gradejson.put("gradeName", gradeName);
					 gradejson.put("gradeId", gjson.getString("GradeId"));
					 graderesult.add(gradejson);
				}	
				// 班级，星期，节次与教师的对应关系		
				HashMap<String, JSONObject> hsmap = new HashMap<String, JSONObject>();
				List<JSONObject> list = new ArrayList<JSONObject>();
				for (JSONObject json : tablelist) {
					 String key = json.getString("DayOfWeek")
							+ json.getString("LessonOfDay") + "&"
							+ json.getString("ClassId") + "&"
							+ json.getString("CourseType");	
					 String targetId = json.getString("TeacherId");
					 if(null == targetId)targetId = "";	
					 if (hsmap.get(key) == null) {
						 int maxDays = Integer.parseInt(json.getString("MaxDaysForWeek"));
						 if (maxDays > totalMaxDays)totalMaxDays = maxDays;
						 json.put("TeacherId", targetId);
						 list.add(json); 
						 hsmap.put(key, json);
					 }else{
						 JSONObject sJson = hsmap.get(key);
						 String sourceId = sJson.getString("TeacherId");
						 if(null == sourceId)sourceId = "";
                         sJson.put("TeacherId", sourceId + "," + targetId);						 
					 }					 
				}				
				// 开始封装课表数据
				gradedata.put("totalMaxDays", totalMaxDays);				
				gradedata.put("totalMaxLesson", totalMaxLessons);
				gradedata.put("gradeList", Sort.sort(SortEnum.ascEnding0rder, graderesult, "gradeName"));
				for (JSONObject gjson : graderesult) {
					List<JSONObject> resultdate1 = new ArrayList<JSONObject>();
					HashMap<String, Object> clamap = new HashMap<String, Object>();
					clamap.put("schoolId", schoolId);
					clamap.put("termInfoId", termInfoId);
					clamap.put("usedGradeId", gjson.getString("gradeId"));
					List<Classroom> clas = commonDataService
							.getClassList(clamap);
					for (Classroom cl : clas) {
						for (JSONObject json : list) {
							if (json.getString("ClassId").equals(
									String.valueOf(cl.getId()))) {
								JSONObject data = new JSONObject();
								data.put("className", cl.getClassName());
								data.put("ClassId", cl.getId());
								resultdate1.add(data);
								gjson.put(
										"tableName",
										json.getString("TimetableName")
												+ "("
												+ gjson.getString("gradeName")
												+ ")");
								gjson.put("classTimetable", Sort.sort(SortEnum.ascEnding0rder, resultdate1, "className"));
								break;
							}
						}
					}
				}
				for (Iterator<JSONObject> it = graderesult.iterator(); it.hasNext();) {
					JSONObject json = it.next();
					List<JSONObject> glist = (List<JSONObject>) json
							.get("classTimetable");
					if (glist == null) {
						it.remove();
					}
				}
				for (JSONObject gjson : graderesult) {
					List<JSONObject> glist = (List<JSONObject>) gjson
							.get("classTimetable");
					for (JSONObject gl : glist) {
						List<JSONObject> tlist = new ArrayList<JSONObject>();
						for (JSONObject json : list) {	
							if (json.getString("ClassId").equals(
									String.valueOf(gl.get("ClassId")))) {
								JSONObject tdata = new JSONObject();
								String courseSimpleName = "",courseName="",teacherName="";
								 LessonInfo lessonInfo =  lmap.get(json.getString("CourseId"));
								 String simpleName = null;
								 if (lessonInfo == null) {
									 courseName = "";
									 logger.info("CourseId ==>" + json.getString("CourseId"));
									 logger.info("lmap ==>" + lmap);
								 }else {
									 courseName = lessonInfo.getName();
									 simpleName = lessonInfo.getSimpleName();
								 }
 

								if (StringUtils.isEmpty(simpleName)){
									if (StringUtils.isNotEmpty(courseName)&&courseName.length()>0){
										courseSimpleName = courseName.substring(0, 1);
									}
								}else{
									courseSimpleName = simpleName;
								}
								String[] nameid = json.get("TeacherId").toString().split(",");
								List<String> namelist = new ArrayList<String>();
								for (String id : nameid) {
									if (!id.trim().equals("")) {
										if (teamap.get(id) != null) {
											namelist.add(teamap.get(id)
													.getName());
										}
									}
								}
								if (namelist.size() > 1) {
									teacherName = StringUtils.join(
											namelist, ",");
								} else {
									teacherName = namelist.size() == 0 ? ""
											: namelist.get(0);
								}
								tdata.put("dayOfWeek",
										json.getString("DayOfWeek"));
								tdata.put("lessonOfDay",
										json.getString("LessonOfDay"));
								tdata.put("courseName", courseName);
								tdata.put("teacherName", teacherName);
								tdata.put("courseSimpleName", courseSimpleName);
								tdata.put("courseType", json.getString("CourseType"));
								tlist.add(tdata);
							}
						}
						gl.put("timetable", tlist);
					}
				}
				}else{
					code = "1";
					msg = "未设置每天上课节次";
				}
			} else {
				code = "1";
				msg = "未编排课表,无法查看";
			}
		} catch (Exception e) {
			code = "1";
			if (e.getMessage() == null) {
				msg = "发生一个内部的错误";
			} else {
				msg = e.getMessage();
			}
			e.printStackTrace();
		}
		this.setPromptMessage(result, code, msg);
		result.put("data", gradedata);
		return result;
	}
	
	private List<String> getClassListBySYNJ(JSONObject param) {
		long schoolId = param.getLongValue("schoolId");
		String useGrade = param.getString("gradeId");
		String xnxq = param.getString("selectedSemester");	
		List<String> classIdList = new ArrayList<String>();
		if (StringUtils.isNotEmpty(xnxq)) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("schoolId", schoolId);
			map.put("usedGradeId", useGrade);
			map.put("termInfoId", xnxq);
			List<Classroom> list = commonDataService.getClassList(map);	
			for (Classroom room : list){
				 classIdList.add(room.getId()+"");			
			}		
		}	
		return classIdList;
	}
	
	/**
	 * 获取学校对应的所有年级和班级
	 * @param gradeIds
	 * @param termInfoId
	 * @param school
	 * @return
	 */
	private Map<String,String> getAllGradeIdOfTimetable(String gradeIds,String termInfoId,School school){
		String queryGradeId="";
		List<Grade> gradeList = commonDataService.getGradeList(school, termInfoId);
		if(gradeIds.split(",").length>1){
			queryGradeId = gradeIds;			
		}else{
			String schoolYear = "";
			if (termInfoId.length() > 4){
				schoolYear = termInfoId.substring(0, 4);
			}
			List<Long> gids = new ArrayList<Long>();			
			
			for(Grade grade : gradeList){				
				String curLevel = grade.currentLevel.getValue()+"";
				
				String gradeID = commonDataService.ConvertNJDM2SYNJ(curLevel, schoolYear);
				gids.add(Long.parseLong(gradeID));
			}		
			
			StringBuffer sb = new StringBuffer();
			
			List<Long> ids = new ArrayList<Long>();
			Object[] array = gids.toArray();//gradeId.split(","); 
			for (Object id : array) {
				ids.add((Long)id);
				sb.append(id+",");
			}
			sb.substring(0, sb.length()-1);//[,)
			queryGradeId = sb.toString();
		}
		List<Long> classIdList = new ArrayList<Long>();
		for(Grade grade : gradeList){
			classIdList.addAll(grade.getClassIds());
		}
		Iterator<Long> iterator = classIdList.iterator();
		StringBuffer sb = new StringBuffer();
		sb.append(iterator.next()+"");
		while(iterator.hasNext()){
			sb.append(","+iterator.next());
		}
		
		Map<String,String> resultMap = new HashMap<String,String>();
		resultMap.put("gradeIds", queryGradeId);
		resultMap.put("classIds", sb.toString());
		return resultMap;
	}
	
	
	@RequestMapping(value = "lookup/getTeacherTimetableByTeacherId.do", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getTeacherTimetableByTeacherId(HttpServletRequest req,
			HttpServletResponse res, @RequestBody JSONObject request) {
		JSONObject response = new JSONObject();
		String schoolId = getXxdm(req);
		String teacherId = request.getString("teacherId");
		if (StringUtils.isBlank(teacherId)) {
			teacherId =  String.valueOf((Long)req.getSession().getAttribute("accountId"));
		}
		String termInfoId =request.getString("selectedSemester");
		if (StringUtils.isNotEmpty(termInfoId) && termInfoId.length() == 5) {
			String schoolYear = termInfoId.substring(0, 4);
			String termName = termInfoId.substring(4);
			Map<String, String> timetableparamMap = new HashMap<String, String>();
			timetableparamMap.put("schoolYear", schoolYear);
			timetableparamMap.put("termName", termName);
			timetableparamMap.put("schoolId", schoolId);
			timetableparamMap.put("published", "2");
			List<JSONObject> timetableList = timetableService.getTimetable(timetableparamMap);
			if (org.apache.commons.collections.CollectionUtils.isNotEmpty(timetableList)) {
				JSONObject param = new JSONObject();
				param.put("schoolId", schoolId);
				param.put("teacherId", teacherId);
				List<String> timetableIds = new ArrayList<String>();
				Map<String, String> timetableMap = new HashMap<String, String>();
			    for (int i = 0; i < timetableList.size(); i++) {
			    	timetableIds.add(timetableList.get(i).getString("timetableId"));
			    	timetableMap.put(timetableList.get(i).getString("timetableId"), timetableList.get(i).getString("timetableName"));
				} 
			    param.put("timetableList", timetableIds);
			   List<JSONObject> timetables = timetableService.getTimetableByTeacherId(param);
			   
			   List<List<JSONObject>> teacherTimetableList = new ArrayList<List<JSONObject>>();
			   if (timetables!=null && timetables.size() > 0) {
					HashMap<String, String> ghmap = new HashMap<String, String>();
					List<Account> teacherlist = new ArrayList<Account>();
					Account account = commonDataService.getAccountAllById(Long.valueOf(schoolId), Long.valueOf(teacherId), termInfoId);
					teacherlist.add(account);
					String accountId = account.getId() + "";
					List<User> ulist = account.getUsers();
						if (null != ulist) {
							for (User user : ulist) {
								if (null == user || null == user.getUserPart()
										|| null == user.getUserPart().getRole()) {
									continue;
								}
								if (user.getUserPart().getRole().equals(T_Role.Teacher)) {
									if (null != user.getTeacherPart()) {
										String ghid = user.getTeacherPart().getEmpno();
										if (StringUtils.isNotEmpty(ghid)) {
											ghmap.put(accountId, ghid);
										}
									}
								}
							}
						}
						
				    School school = this.getSchool(req,termInfoId);
					HashMap<String, LessonInfo> lmap = new HashMap<String, LessonInfo>();
					List<LessonInfo> lilist = commonDataService
							.getLessonInfoList(school, termInfoId);// 科目信息
					for (LessonInfo l : lilist) {
						 lmap.put(String.valueOf(l.getId()), l);
					}
					
					List<Long> tidlist = new ArrayList<Long>();
					for (Account a : teacherlist) {
						 tidlist.add(a.getId());
					}
					
					List<Grade> glist=commonDataService.getGradeList(school,termInfoId);//耗时0.5s
					HashMap<String, Grade> gmap = new HashMap<String, Grade>();
					if(glist!=null && glist.size()>0){
						for(Grade g : glist){
							gmap.put(String.valueOf(g.getId()), g);
						}
					}	
				   
				 for (int n = 0; n < timetables.size(); n++) {
					String timetableId = timetables.get(n).getString("TimetableId");
					long sid = Long.valueOf(schoolId);
					
					HashMap<String, Classroom> cmap = new HashMap<String, Classroom>();
					HashMap<String, Grade> cgrademap = new HashMap<String, Grade>();
					HashMap<String, Integer>  gramap = new HashMap<String, Integer>();
					HashMap<String, Integer>  aLessonmap = new HashMap<String, Integer>();
					List<JSONObject> resultdate = new ArrayList<JSONObject>();
					try{
						HashMap<String,Object> reqMap = new HashMap<String, Object>();
						reqMap.put("schoolId",schoolId);
						reqMap.put("timetableId",timetableId);
						List<JSONObject> gradeList = timetableService.getTimetableGradeList(reqMap);
						List<String> gradeIdList = new ArrayList<String>();
						if (gradeList!=null) {
							for (int i = 0; i < gradeList.size(); i++) {
								gradeIdList.add(gradeList.get(i).getString("gradeId"));
							}
						}
						Map<String,ArrayList<JSONObject>> scheduleMap = getTimetableScheduleMap(gradeIdList, termInfoId, timetableId);

							HashMap<String, Object> map = new HashMap<String, Object>();
							map.put("timetableId", timetableId);
							map.put("SchoolId", schoolId);
							map.put("teacherIds", tidlist);
							List<JSONObject> list = timetableService
									.getTeacherTimetable(map); //耗时15s
 
							if (list != null && list.size() > 0) {
								List<Long> cidlist = new ArrayList<Long>();
								HashSet<String> t = new HashSet<String>();
								 
								HashMap<String, Object> reasmap = new HashMap<String, Object>();
								reasmap.put("timetableId", timetableId);
								reasmap.put("schoolId", schoolId);
								List<JSONObject> researchlist = timetableService.getTeacherResearch(reasmap);	

								for (JSONObject json : list) {
									 t.add(json.get("ClassId").toString());
									 cidlist.add(Long.parseLong(json.get("ClassId").toString()));
								}				
								Collections.sort(cidlist);
								List<Classroom> clist = commonDataService
										.getSimpleClassBatch(sid, cidlist, termInfoId);// 班级信息
								for (Classroom cr : clist) {				
									 cmap.put(String.valueOf(cr.getId()), cr);
									 if(gmap.get(String.valueOf(cr.getGradeId()))!=null){
										cgrademap.put(String.valueOf(cr.getId()), gmap.get(String.valueOf(cr.getGradeId())));
									}			
								}				
								HashMap<String, Object> grademap = new HashMap<String, Object>();
								grademap.put("schoolId", schoolId);
								grademap.put("timetableId", timetableId);
								grademap.put("GradeId", "");					
								List<JSONObject> gradeSetlist = timetableService
											.getGradeSet(grademap);// 年级课表安排
								if(gradeSetlist!=null && gradeSetlist.size() >0 ){
									for(JSONObject o:gradeSetlist){
										int totalMax=Integer.valueOf(o.getString("AMLessonNum"))+Integer.valueOf(o.getString("PMLessonNum"));
										gramap.put(o.getString("GradeId"), totalMax);
										aLessonmap.put(o.getString("GradeId"), Integer.valueOf(o.getString("AMLessonNum")));
									}
								}
								for (Account a : teacherlist) {
									JSONObject data = new JSONObject();
									String accountid = a.getId() + "";
									data.put("accountId", accountid);						
									if (ghmap.containsKey(accountid)){
										data.put("teacherId", ghmap.get(accountid));
										data.put("TeacherId", ghmap.get(accountid));
									}else{
										data.put("teacherId", "");
										data.put("TeacherId", "");
									}	
									String name = a.getName();
									data.put("teacherName", name);
									if (StringUtils.isNotEmpty(name)){								
										resultdate.add(data);
									}	
								}	
								// 处理合班,单双周在前端处理
								HashMap<String, JSONObject> hsmap = new HashMap<String, JSONObject>();
								List<JSONObject> tablelist = new ArrayList<JSONObject>();				
								for (JSONObject json : list) {
									 String key = json.getString("DayOfWeek")
											+ json.getString("LessonOfDay") + "&"
											+ json.getString("TeacherId") + "&"
											+ json.getString("CourseType");	
									 String targetId = json.getString("ClassId");
									 if (hsmap.get(key) == null) {			
										 json.put("ClassId", targetId);
										 tablelist.add(json); 
										 hsmap.put(key, json);
									 }else{
										 JSONObject sJson = hsmap.get(key);
										 String sourceId = sJson.getString("ClassId");
				                         sJson.put("ClassId", sourceId + "," + targetId);						 
									 }					 
								}
								Map<String,String> classMapGrade = new HashMap<String,String>();
								for (JSONObject rejson : resultdate) {
									HashMap<String,JSONObject> tbmap = new HashMap<String,JSONObject>();
									HashSet<String> gradeSet = new HashSet<String>();
									for (JSONObject json : tablelist) {
										if (rejson.get("accountId").toString()
												.equals(json.getString("TeacherId"))) {
											JSONObject tjson = new JSONObject();
											String courseName = COURSE_PROMPT_DELETE;
											String type = json.getString("CourseType");
											if (lmap.containsKey(json.getString("CourseId"))) {
												courseName = lmap.get(
														json.getString("CourseId"))
														.getName();
												if (StringUtils.isEmpty(courseName)){
													courseName = COURSE_PROMPT_EMPTY;
												}else{
													if (type.equals("1")){
					                             	    courseName = courseName + LABEL_SINGLE;
					                                }else if(type.equals("2")){
					                             	    courseName = courseName + LABEL_DOUBLE;
					                                }
												}	
											}
											List<String> namelist = new ArrayList<String>();
											String[] classid = json.getString("ClassId")
													.toString().split(",");
											for(String ccid : classid) {
												String cName = CLASS_PROMPT_DELETE;
												if (cmap.containsKey(ccid)) {
													cName = cmap.get(ccid)
															.getClassName();
													if (StringUtils.isEmpty(cName)){
														cName = CLASS_PROMPT_EMPTY;
													}
												}
												if(!namelist.contains(cName)){											
													namelist.add(cName);
												}
											}
											String className = namelist.get(0);
											if (namelist.size() > 1) {
												className = StringUtils.join(namelist, ",");
											}						                               
											tjson.put("courseId",
													json.getString("CourseId"));
											tjson.put("courseName", courseName);
											tjson.put("className", className);
											tjson.put("classId", json.getString("ClassId")
													.toString());
											tjson.put("courseType", type);
											tjson.put("dayOfWeek",
													json.getString("DayOfWeek"));
											tjson.put("lessonOfDay",
													json.getString("LessonOfDay"));
											tbmap.put(json.getString("DayOfWeek")+json.getString("LessonOfDay")
													+json.getString("TeacherId")+json.getString("CourseType"), tjson);

											try{
												String gradeId =null;
												for (int i = 0; i < classid.length; i++) {
													gradeId = classMapGrade.get(classid[i]);
													if(null==gradeId){
														Classroom cr = commonDataService.getClassById(Long.parseLong(schoolId),Long.parseLong(classid[i]), termInfoId);										
														if(null!=cr){
															Grade grade = commonDataService.getGradeById(Long.parseLong(schoolId), cr.getGradeId(), termInfoId);
															gradeId = commonDataService.ConvertNJDM2SYNJ(grade.currentLevel.getValue()+"", json.getString("SchoolYear"));
															classMapGrade.put(classid[i], gradeId);
														}
													}
													gradeSet.add(gradeId);
													
												}

											}catch(Exception e){
												e.printStackTrace();
												logger.info(e.getMessage());
											}

											rejson.put(
													"totalMaxDays",json.getString("MaxDaysForWeek"));
											rejson.put(
													"tableName",  json.getString("TimetableName"));
													
										}						
									}
									if (gradeSet.size() > 0) {
										  Iterator<String> it = gradeSet.iterator();
										  String gradeId = null;
										  List<JSONObject> copyScheduleTime = new ArrayList<JSONObject>();
										  List<JSONObject> scheduleTime =null;
										  List<JSONObject> tempScheduleTime =null;
										  while (it.hasNext()) {
											  gradeId =  it.next();
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
											  rejson.put("scheduleTime",copyScheduleTime);
										  }
										 
										  
									}
 
									if (researchlist != null && researchlist.size() > 0){						
										for(JSONObject researchjson : researchlist){
											if (rejson.getString("accountId").equals(researchjson.getString("TeacherId")))	{
												String key = researchjson.getString("DayOfWeek")
														+ researchjson.getString("LessonOfDay")
														+ researchjson.getString("TeacherId");
												if (tbmap.get(key + "1")==null && tbmap.get(key + "2")==null && tbmap.get(key + "0")==null){							
													JSONObject tjson = new JSONObject();
													tjson.put("dayOfWeek",
															researchjson.getString("DayOfWeek"));
													tjson.put("lessonOfDay",
															researchjson.getString("LessonOfDay"));
 
													tjson.put("className", "");
													tjson.put("courseName", researchjson.getString("TeacherGroupName"));
													tbmap.put(key + "0",tjson);												
												}
											}	
										}
									}
									Iterator<Entry<String, JSONObject>> ite = tbmap.entrySet().iterator();
									List<JSONObject> tblis = new ArrayList<JSONObject>();
									while (ite.hasNext()) {
										Map.Entry<String, JSONObject> entry =  ite.next();
										tblis.add(entry.getValue());
									}
									rejson.put("timetable", tblis);
								}
							} else {
								continue;
							}
							for (Iterator<JSONObject> it = resultdate.iterator(); it.hasNext();) {
								JSONObject json = it.next();
								List<JSONObject> gtimetableList = (List<JSONObject>) json
										.get("timetable");
								if (gtimetableList.size() == 0) {
									it.remove();
								}
							}			
							for(JSONObject ro:resultdate){
								int maxDay=0,amLessonNum = 0;
								List<Integer> totallist=new ArrayList<Integer>();
								List<Integer> aLesssonlist=new ArrayList<Integer>();
								List<JSONObject> timlist=(List<JSONObject>) ro.get("timetable");
								for(JSONObject to:timlist){
									if(to.containsKey("classId")){
									String cids[] =to.getString("classId").split(",");
									for(String id:cids){
										if(cgrademap.containsKey(id)){
											Grade grade=cgrademap.get(id);
											String xn=(String) req.getSession().getAttribute("curXnxq");
											String gid=commonDataService.ConvertNJDM2SYNJ(String.valueOf(grade.getCurrentLevel().getValue()),xn.substring(0, 4));
											if(gramap.get(gid)!=null){
												totallist.add(gramap.get(gid));
												aLesssonlist.add(aLessonmap.get(gid));
											}
										}
									}
									}
								}
								if(totallist.size()>0){
								for(int max:totallist){
									maxDay = maxDay < max ? max : maxDay;
								}
								ro.put("totalMaxLessons", maxDay);
								}else{
									ro.put("totalMaxLessons", 8);
									ro.put("totalMaxDays", 5);
								}
								if(aLesssonlist.size()>0){
									for(int max:aLesssonlist){
										amLessonNum = amLessonNum < max ? max : amLessonNum;
								}
								ro.put("amLessonNum", amLessonNum);
								}
							}

					 
					} catch (Exception e) {
						 e.printStackTrace();
					}

				    if(resultdate.size()> 0 ){
				    	teacherTimetableList.add(resultdate);
					} 
				   }
				   response.put("data", teacherTimetableList);
				   setPromptMessage(response, OutputMessage.success.getCode(),
							"查询成功");
			    }else {
			    	setPromptMessage(response, OutputMessage.success.getCode(),
							"查询数据为空!!!");
			   }
			    
			} else {
				setPromptMessage(response, OutputMessage.success.getCode(),
						"查询数据为空!!!");
			}
		}else {
			setPromptMessage(response, OutputMessage.queryDataError.getCode(),
					"请求参数异常!!!");
		}
		return response;
	}
	
	
	
	private static final String LABEL_SINGLE = "(\u5355)";
	
	private static final String LABEL_DOUBLE = "(\u53CC)";

	private static final String CLASS_PROMPT_EMPTY = "\u73ED\u7EA7\u540D\u79F0\u4E3A\u7A7A";
	
	private static final String CLASS_PROMPT_DELETE = "\u73ED\u7EA7\u5DF2\u5220\u9664";
	
	private static final String TEACHER_PROMPT_EMPTY = "\u6559\u5E08\u540D\u79F0\u4E3A\u7A7A";
	
	private static final String TEACHER_PROMPT_DELETE = "\u6559\u5E08\u5DF2\u5220\u9664";
	
	private static final String COURSE_PROMPT_EMPTY = "\u8BFE\u7A0B\u540D\u79F0\u4E3A\u7A7A";
	
	private static final String COURSE_PROMPT_DELETE = "\u8BFE\u7A0B\u5DF2\u5220\u9664";
	
}
