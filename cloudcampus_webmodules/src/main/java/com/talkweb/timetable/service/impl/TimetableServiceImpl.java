package com.talkweb.timetable.service.impl;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.Account;
import com.talkweb.accountcenter.thrift.AccountStructConstants;
import com.talkweb.accountcenter.thrift.Classroom;
import com.talkweb.accountcenter.thrift.Grade;
import com.talkweb.accountcenter.thrift.LessonInfo;
import com.talkweb.accountcenter.thrift.OrgInfo;
import com.talkweb.accountcenter.thrift.School;
import com.talkweb.accountcenter.thrift.T_GradeLevel;
import com.talkweb.accountcenter.thrift.T_Role;
import com.talkweb.accountcenter.thrift.TeacherLesson;
import com.talkweb.accountcenter.thrift.User;
import com.talkweb.base.common.OutputMessage;
import com.talkweb.common.tools.DateUtil;
import com.talkweb.common.tools.UUIDUtil;
import com.talkweb.common.tools.sort.ChineseSort;
import com.talkweb.common.tools.sort.Sort;
import com.talkweb.common.tools.sort.SortEnum;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.commondata.service.CsCurCommonDataService;
import com.talkweb.scoreManage.action.ScoreUtil;
import com.talkweb.timetable.arrangement.domain.TCRCATemporaryAdjustGroupInfo;
import com.talkweb.timetable.arrangement.domain.TCRCATemporaryAdjustGroupParam;
import com.talkweb.timetable.arrangement.service.ArrangeDataService;
import com.talkweb.timetable.bo.TimetableGradeSetBO;
import com.talkweb.timetable.dao.TimetableDao;
import com.talkweb.timetable.domain.CourseSetInfor;
import com.talkweb.timetable.domain.ImportTaskParameter;
import com.talkweb.timetable.mapper.ITimetableMapper;
import com.talkweb.timetable.service.TimetableService;
import com.talkweb.utils.KafkaUtils;

@Service
public class TimetableServiceImpl implements TimetableService {

	@Autowired
	ITimetableMapper timetableMapper;
	
	@Autowired
	private AllCommonDataService commonService;
	
	@Autowired
	private CsCurCommonDataService csCurCommonDataService;
	
	@Autowired
	private TimetableDao timetableDao;
	
	@Autowired
	private ArrangeDataService arrangeDataService;
	
	private static final Logger logger = LoggerFactory.getLogger(TimetableServiceImpl.class);
	
	private static NumberFormat NFormat = NumberFormat.getInstance();
	
	private static final String TIMETABLE_LASTTERM = "1";
	private static final String TIMETABLE_NEXTTERM = "2";
	private static final String TIMETABLE_RESEARCH = "1";
	private static final String TIMETABLE_REGULARCOURSE = "0";
	
	@Value("#{settings['clientId']}")
	private String clientId;

	@Value("#{settings['clientSecret']}")
	private String clientSecret;

	@Value("#{settings['kafkaUrl']}")
	private String kafkaUrl;
 
	@Value("#{settings['pk.msgUrlPc']}")
	private String msgUrlPc="https://pre.yunxiaoyuan.com/talkCloud/homePage.html#cloudManager/syllabus";
	
	@Value("#{settings['pk.msgUrlApp']}")
	private String msgUrlApp="https://pre.yunxiaoyuan.com/apph5/openH5/timetable/index.html?cur=1";
	
	private static final String MSG_TYPE_CODE = "PK";
	
	private static HashMap<String,String> xnxqMap = new HashMap<String,String>();
	
	private boolean isNotEmpty(JSONObject object, String key) {
		boolean label = true;
		if (null == object){
			label = false;
		}else if(!object.containsKey(key)){
			label = false;
		}else if(null == object.get(key)){
			label = false;
		}else if("".equals(object.get(key).toString())){
			label = false;
		}
		return label;
	}		
	
	/** ---查询所有教师信息--- */
	private Map<String,String> getTeachersMap(School school,String termInfo){
		Map<String,String> teacherMap = new HashMap<String,String>();
		List<Account> tList = commonService.getAllSchoolEmployees(school,termInfo, null);
		for (Account account : tList){
			 String teacherId = account.getId()+"";
			 String teacherName = account.getName();
			 teacherMap.put(teacherId, teacherName);
		}
		return teacherMap;
	}
	
	private List<Account> getAccountAllByName(School school,String name,String termInfo){
		return commonService.getAllSchoolEmployees(school,termInfo,name);
	}
	
	private long getTeacherId(long schId, long accountId, String termInfo) {
		Account account = commonService.getAccountAllById(schId, accountId, termInfo);
		List<User> ulist= account.getUsers();
		long teacherId = 0;
		if (null != ulist) {
			for(User user : ulist) {
				if (null == user || null == user.getUserPart() || null == user.getUserPart().getRole()) {
					continue;
				}
				if (user.getUserPart().getRole().equals(T_Role.Teacher)) {
					teacherId = user.getTeacherPart().getId();
				}												 	
		    }
	    }
		return teacherId;
	}
	
	private Map<String, String> getLessonsMap(School school,String termInfo) {
		Map<String, String> lessonMap = new HashMap<String, String>();
		List<LessonInfo> lList = commonService.getLessonInfoList(school,termInfo);
		for (LessonInfo lesson : lList) {
			 String lessonId = lesson.getId()+"";
			 String lessonName = lesson.getName();
			 lessonMap.put(lessonId, lessonName);
		}
		return lessonMap;
	}
	
	/** ---根据班级信息--- */
	private Map<String, String> getClassesMap(School school,String termInfo) {
		Map<String, String> classMap = new HashMap<String, String>();
		List<Classroom> cList = commonService.getAllClass(school,termInfo);
		for (Classroom room : cList) {
			 String classId = room.getId() + "";
			 String className = room.getClassName();
			 classMap.put(classId, className);
		}
		return classMap;
	}

	/** 根据使用年级查询年级名称 **/
	private String getGradeNameBySYNJ(String synj, String xn){
		String gradeName = "";
		String njdm = commonService.ConvertSYNJ2NJDM(synj, xn);	
		T_GradeLevel tgl = T_GradeLevel.findByValue(Integer.parseInt(njdm));
		if (AccountStructConstants.T_GradeLevelName.containsKey(tgl)){
			gradeName = AccountStructConstants.T_GradeLevelName.get(tgl);
		}
		return gradeName;
	}
	
	/** 根据年级ID查询年级下的班级ID列表 **/
	/**
	private List<String> getClassIdsByGradeId(long schoolId,long gradeId,String termInfo){
		List<String> classIdList = new ArrayList<String>();
		Grade gradeInfo = commonService.getGradeById(schoolId,gradeId,termInfo);
		if (null != gradeInfo){
			List<Long> classIds = gradeInfo.getClassIds();
			for(long classId : classIds){
				classIdList.add(String.valueOf(classId));
			}
		}
		return classIdList;
	}*/
	
	private JSONObject getClassListBySYNJ(JSONObject parameter) {
		long schoolId = Long.parseLong(parameter.getString("schoolId"));
		String useGrade = parameter.getString("gradeId");
		String xnxq = parameter.getString("selectedSemester");
		HashMap<String,String> classMap = new HashMap<String,String>();
		String gradeName = "";
		List<String> classIdList = new ArrayList<String>();
		JSONObject result = new JSONObject();
		if (StringUtils.isNotEmpty(xnxq)) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("schoolId", schoolId);
			map.put("usedGradeId", useGrade);
			map.put("termInfoId", xnxq);
			List<Classroom> list = commonService.getClassList(map);	
			for (Classroom room : list){
				 classIdList.add(room.getId()+"");
				 classMap.put(room.getId()+"",room.getClassName());
			}		
			gradeName = getGradeNameBySYNJ(useGrade,xnxq.substring(0, 4));				
		}	
		result.put("gradeName", gradeName);
		result.put("classIdList", classIdList);
		result.put("classMap", classMap);
		return result;
	}
	
	/** 年级ID转使用年级**/ 
	public String convertSYNJByGrade(long schoolId,Grade grade,String termInfo) {
		String synj = "";
		if (null != grade){
		    String xn = termInfo.substring(0, 4);
			String gradeLevel = grade.getCurrentLevel().getValue()+"";
			synj = commonService.ConvertNJDM2SYNJ(gradeLevel, xn);
		}
		return synj;	
	}

	/** 使用年级转年级代码 **/
	private String convertSYNJToNJDM(JSONObject object) {
		String njdm = "";
		String synj = object.getString("gradeId");
		String termInfo = object.getString("selectedSemester");
		if (StringUtils.isNotEmpty(termInfo)&&termInfo.length()>4){
			String xn = termInfo.substring(0,4);
			njdm = commonService.ConvertSYNJ2NJDM(synj, xn);
		}	
		return njdm;
	}
	
	private void setTotalLessonDay(JSONObject object, JSONObject result,
			boolean flag) {
		JSONObject totalNum = timetableDao.getTotalNum(object);
		if (isNotEmpty(totalNum, "totalMaxDays")) {
			String totalMaxDays = totalNum.get("totalMaxDays").toString();
			result.put("totalMaxDays", totalMaxDays);
		}
		if (isNotEmpty(totalNum, "totalMaxLesson")) {
			String totalLessons = totalNum.get("totalMaxLesson").toString();
			result.put("totalMaxLesson", totalLessons);
		}
		if (flag && isNotEmpty(totalNum, "amLessonNum")) {
			String amLessonNum = totalNum.get("amLessonNum").toString();
			result.put("amLessonNum", amLessonNum);
		}
	}

	/** 通过课表获取学年学期 **/
	public String getTimetableXnxq(String timetableId,String schoolId){
		String xnxq = "";
		String key = timetableId + schoolId;
		if (xnxqMap.containsKey(key)){
			xnxq = xnxqMap.get(key).toString();
		}else{
			Map<String,String> map = new HashMap<String,String>();
			map.put("schoolId", schoolId);
			map.put("timetableId", timetableId);
			String semester = timetableDao.getTimetableXnxq(map);
			if (StringUtils.isNotEmpty(semester)) {
				xnxq = semester;
				xnxqMap.put(key, semester);
			}
		}
		return xnxq;
	}

	/**-----TAB页:课表视图-----**/	
	
	@Override
	public List<JSONObject> getTimetable(Map<String, String> map) {
		return timetableDao.getTimetable(map);
	}

	@Override
	public void deleteTimetable(JSONObject object) {
		timetableDao.deleteTimetable(object);		
	}

	@Override
	public int updateTimetable(JSONObject object) {
		return timetableDao.updateTimetable(object);
	}
	
	private int doTimetableCheck(String timetableId,String schoolId) {
		int code = 0;
		JSONObject json = this.getArrangeTimetableInfo(schoolId, timetableId);
		if(isNotEmpty(json,"Published")){
			code = json.getIntValue("Published");
			if (code > 0){
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("schoolId", schoolId);
				map.put("timetableId", timetableId);
				timetableDao.updateTimetableCheck(map);	
			}
		}
		return code;
	}

	@Override
	public int addTimetable(Map<String, Object> map) {
		return timetableDao.addTimetable(map);
	}
	
	/**-----TAB页:复制-----**/
	
	@Override
	public List<JSONObject> getLastTimetableList(JSONObject object) {
		String timetableId = object.getString("timetableId");
		String schoolId = object.getString("schoolId");
		String termInfo = object.getString("selectedSemester"); 
		String publish = object.getString("publish"); 
		String exclude = object.getString("exclude"); 
		List<JSONObject> resultList = new ArrayList<JSONObject>();
		if (termInfo.length() > 4){
			String xn = termInfo.substring(0,4);
			String xq = termInfo.substring(4,5);
			if (StringUtils.isNotEmpty(xn) && StringUtils.isNotEmpty(xq)){
				Map<String,String> map = new HashMap<String,String>();
				map.put("notTimetableId", timetableId);
				map.put("schoolYear", xn);
				map.put("termName", xq);
				map.put("schoolId", schoolId);
				if (xq.equals(TIMETABLE_LASTTERM)){
					map.put("lastTerm", TIMETABLE_NEXTTERM);
					String newxn = (Integer.parseInt(xn)-1) + "";
					map.put("lastYear", newxn);					
				}
				if (xq.equals(TIMETABLE_NEXTTERM)){
					map.put("lastTerm", TIMETABLE_LASTTERM);
					map.put("lastYear", xn);
				}
				
				if (StringUtils.isNotEmpty(exclude)) {
					map.put("exclude", exclude);
				}
				
				if (StringUtils.isNotEmpty(publish)) {
					map.put("published", publish);
				}
				resultList = timetableDao.getTimetable(map);
			}		
		}
		return resultList;
	}

	@Override
	public void copyTimetable(JSONObject object) {
		long schoolId = Long.parseLong(object.getString("schoolId"));
		String tTermInfo = object.getString("selectedSemester");
		String sTermInfo = object.getString("fromTermInfoId");
		School school = commonService.getSchoolById(schoolId, sTermInfo);
		if (tTermInfo.length() >= 4 && sTermInfo.length() >= 4){
			String firstXn = sTermInfo.substring(0, 4);
			String secondXn = tTermInfo.substring(0, 4);
			if(!firstXn.equals(secondXn)){
				List<Grade> gradeList = commonService.getGradeList(school, sTermInfo);
				Map<String,Grade> gradeMap = new HashMap<String,Grade>();
				for(Grade grade : gradeList){
					String gradeName = grade.currentLevel.name();
					gradeMap.put(gradeName, grade);
				}
				if (gradeMap.containsKey("T_PrimarySix")){
					gradeMap.remove("T_PrimarySix");
				}
				if (gradeMap.containsKey("T_HighThree")){
					gradeMap.remove("T_HighThree");
				}
				if (gradeMap.containsKey("T_JuniorFour")){
					gradeMap.remove("T_JuniorFour");
				}else if(gradeMap.containsKey("T_JuniorThree")){
					gradeMap.remove("T_JuniorThree");
				}
				Iterator<Entry<String, Grade>> iter = gradeMap.entrySet().iterator();
				List<String> gradeIds = new ArrayList<String>();
				List<String> classIds = new ArrayList<String>();
		        while (iter.hasNext()) {
		        	Map.Entry<String,Grade> entry = (Map.Entry<String,Grade>) iter.next();
		        	Grade grade = entry.getValue();
		        	gradeIds.add(convertSYNJByGrade(schoolId,grade,sTermInfo));
		        	classIds.add(grade.classIds.toString());
		        }				
				object.put("gradeIds", StringUtils.join(gradeIds, ","));
				object.put("classIds", StringUtils.join(classIds, ","));
				timetableDao.copyTimetableTwo(object);
			}else{
				timetableDao.copyTimetable(object);
			}		
		}
	}	
	
	@Override
	public int addTimetableList(List<JSONObject> timetableList) {	
		return timetableDao.insertTimetableList(timetableList);
	}

	/**-----TAB页:设置课表-----**/	
	
	@Override
	public JSONObject getTimetableSection(JSONObject object) {
		JSONObject sectionObject = timetableDao
				.getTimetableSection(object);
		if (isNotEmpty(sectionObject,"rows")) {
			JSONArray arrayList = sectionObject.getJSONArray("rows");		
			String xn = sectionObject.getString("schoolYear");			
			for (int k = 0; k < arrayList.size(); k++) {
				 JSONObject grade = arrayList.getJSONObject(k);
				 String useGrade = grade.getString("gradeId");				 
				 String gradeName =  getGradeNameBySYNJ(useGrade,xn);
				 if (StringUtils.isNotEmpty(gradeName)){
					 grade.put("gradeName", gradeName);
				 }
			}
		}
		return sectionObject;
	}
	
	@Override
	public JSONObject getTimetableSections(String schoolId, String timetableId, List<String> gradeIds,School sch) {
		List<JSONObject> gradeList = this.getGradeSetList(schoolId, timetableId, gradeIds);
		//取年级字典
		JSONObject timetableInfo = this.getArrangeTimetableInfo(schoolId, timetableId);
		String schoolYear = timetableInfo.getString("SchoolYear");
		String TermName = timetableInfo.getString("TermName");
		Map<String, Grade> gradesDic = this.arrangeDataService.getGradesDic( schoolYear,sch, TermName);
		List<JSONObject> kbArray = new ArrayList<JSONObject>();
		for (JSONObject obj : gradeList) {
			String gradeId = obj.getString("GradeId");
			Grade grade = gradesDic.get(gradeId);
			if(grade==null){
				continue;
			}
			String gradeName = this.arrangeDataService.getGradeName(grade);
			obj.put("gradeId", gradeId);
			obj.put("gradeName", gradeName);
			obj.put("amLessonNum", obj.getString("AMLessonNum"));
			obj.put("pmLessonNum", obj.getString("PMLessonNum"));
			kbArray.add(obj);
		}
		JSONObject timetableBase = this.getArrangeTimetableInfo(schoolId, timetableId);
		ScoreUtil.sorStuScoreList(kbArray, "gradeId", "desc","","");
		timetableBase.put("rows", kbArray);
		return timetableBase;
	}

	@Override
	public String updateTimetableSection(JSONObject object) {
		String tableId = object.getString("timetableId");
		String schoolId = object.getString("schoolId");
		String message = OutputMessage.updateSuccess.getDesc();
		JSONObject sectionObject = timetableDao
				.getTimetableSection(object);
		if (null != sectionObject){
			int days = sectionObject.getIntValue("maxDaysForWeek");
			String maxDay = object.getString("maxDaysForWeek");
			int pageDay = Integer.parseInt(maxDay);
			String result = sectionObject.getString("isArrange");
			if (!"0".equals(result) && (pageDay < days)){
				// 删除排课结果
				object.put("pageMaxDay", pageDay);
				timetableDao.deleteTimetableList(object);
				message = "更新成功,缩减上课天数时排课信息也会删除!!!";
			}
			timetableDao.updateTimetable(object);
			/** ---更新设置节数---*/
			Map<String,Integer> map = new HashMap<String,Integer>();
			String rows = object.getString("rows");
			JSONArray arrayList = JSONArray.parseArray(rows);
			List<JSONObject> gList = new ArrayList<JSONObject>();
			Map<String,JSONObject> number = new HashMap<String,JSONObject>();
			for (int k = 0; k < arrayList.size(); k++) {
				 JSONObject jb = arrayList.getJSONObject(k);
				 jb.put("schoolId", schoolId);
				 jb.put("timetableId", tableId);
				 String pageGradeId = jb.getString("gradeId");
				 int pageAmNum = Integer.parseInt(jb.getString("amLessonNum"));
				 int pagePmNum = Integer.parseInt(jb.getString("pmLessonNum"));
				 map.put(pageGradeId, pageAmNum + pagePmNum);
				 gList.add(jb);
				 number.put(pageGradeId, jb); 
			}
			/** ---获取班级已设置节数和---*/
			String gradeRows = sectionObject.getString("rows");
			JSONArray gradeList = JSONArray.parseArray(gradeRows);
			List<String> gradeIdList = new ArrayList<String>();
			for (int k = 0; k < gradeList.size(); k++) {
				 JSONObject grade = gradeList.getJSONObject(k);
				 String gradeId = grade.getString("gradeId");
				 int amNum = Integer.parseInt(grade.getString("amLessonNum"));
				 int pmNum = Integer.parseInt(grade.getString("pmLessonNum"));
				 // 查询年级下的班级
				 JSONObject param = new JSONObject();
				 param.put("timetableId", tableId);
				 param.put("gradeId", gradeId);
				 param.put("schoolId", schoolId); 
				 param.put("selectedSemester", object.getString("selectedSemester"));
				 JSONObject classObj = getClassListBySYNJ(param);
				 @SuppressWarnings("unchecked")
				 List<String> idList = (List<String>)classObj.get("classIdList");
				 if (map.containsKey(gradeId)){   // 修改节数
					 if(!"0".equals(result) && 
							 (amNum + pmNum) > map.get(gradeId)){
						 // 删除排课结果	 
						 if (CollectionUtils.isNotEmpty(idList)){
							 object.put("classIdList", idList);
							 object.put("pageMaxLesson", map.get(gradeId));
							 timetableDao.deleteTimetableList(object);
						 } 
						 message = "更新成功,缩减上课节数时排课信息也会删除!!!";
					 }
				 }else{        // 删除节数设置				 
					 if (CollectionUtils.isNotEmpty(idList))
						 param.put("classIdList", idList);
					 List<JSONObject> tasks = timetableDao.getTeachingTaskCourse(param);
					 if (tasks.size() > 0){ 
						 if (number.containsKey(gradeId))
						     gList.remove(number.get(gradeId));
						 // 不允许删除有任务的年级
						 gradeIdList.add(gradeId);
						 message = "更新失败,包含教学任务的年级不能删除!!!";
					 }
				 }
			}	
			if (CollectionUtils.isNotEmpty(gradeIdList)){
				object.put("gradeIds", gradeIdList);			
			}
			timetableDao.deleteGradeList(object);
			if (CollectionUtils.isNotEmpty(gList)){
				timetableDao.addGradeList(gList);
			}
		}		
		return message;
	}

	/**-----TAB页:设置教学任务-----**/		
	@SuppressWarnings("unchecked")
	@Override
	public JSONObject getTeachingTask(JSONObject parameter) {
		School school = (School)parameter.get("school");
		String termInfo = parameter.getString("selectedSemester");
		JSONObject classObj = getClassListBySYNJ(parameter);
		List<String> classIdList = (List<String>)classObj.get("classIdList");
		if (CollectionUtils.isNotEmpty(classIdList)){
			parameter.put("classIdList", classIdList);
		}
		Map<String, String> setMap = new HashMap<String, String>();
		/** ---获取班级已设置节数---*/
		List<JSONObject> setNumList = timetableDao.getSetNumList(parameter);		
		for (JSONObject setNum : setNumList) {
			 float setNumber = setNum.getFloat("setNum");
			 setMap.put(setNum.getString("classId"),NFormat.format(setNumber));
		}	
		/** ---获取年级总节数---*/
		JSONObject totalNum = timetableDao.getTotalNum(parameter);
		/** ---获取基础数据---*/
		Map<String,String> courseMap = getLessonsMap(school,termInfo);
		Map<String,String> teacherMap = getTeachersMap(school,termInfo);
		Map<String,String> classMap = getClassesMap(school,termInfo);
		/** ---设置column信息--- */
		Set<JSONObject> allCourseSet = new LinkedHashSet<JSONObject>();		
		parameter.put("sort", "-a.CourseId DESC");
		List<JSONObject> taskLList = timetableDao.getTeachingTasks(parameter);				
		for (JSONObject  task: taskLList) {
			 JSONArray courseArray = task.getJSONArray("courseInfo");
			 for (int i = 0; i < courseArray.size(); i++) {
				  JSONObject course = courseArray.getJSONObject(i);
				  String courseId = course.getString("courseId");
				  JSONObject column = new JSONObject();
				  column.put("field", courseId);
				  if (courseMap.containsKey(courseId)){
					  column.put("title", courseMap.get(courseId).toString());
				  }
				  allCourseSet.add(column);
			 }     
		}
		/** ---设置data信息--- */ 
		parameter.put("sort", "a.ClassId ASC");
		List<JSONObject> taskCList = timetableDao.getTeachingTasks(parameter);
		for (JSONObject sourseTask : taskCList) {
			 String classId = sourseTask.getString("classId");
			 if (classMap.containsKey(classId)){
				 sourseTask.put("className", classMap.get(classId).toString());
			 }			 		
			 JSONArray cArray = sourseTask.getJSONArray("courseInfo");
			 sourseTask.remove("courseInfo");
			 Set<JSONObject> partCourseSet = new HashSet<JSONObject>();
			 for (int j = 0; j < cArray.size(); j++) {
				  JSONObject course = cArray.getJSONObject(j);
				  String courseId = course.getString("courseId");
				  String lessonName = courseMap.get(courseId);
				  JSONObject column = new JSONObject();
				  column.put("field", courseId);
				  column.put("title", lessonName);
				  partCourseSet.add(column);
				  sourseTask.put("courseName_" + courseId,lessonName);
				  float weekNum = course.getFloat("weekNum");			 
				  sourseTask.put("weekNum_" + courseId, NFormat.format(weekNum));
				  sourseTask.put("nearNum_" + courseId, course.getString("nearNum"));				
				  JSONArray tArray = course.getJSONArray("teacherIds");						  
				  for (int k = 0; k < tArray.size(); k++){
					   JSONObject teacherInfo = tArray.getJSONObject(k);
					   if (isNotEmpty(teacherInfo, "teacherId")) {
					   String tId = teacherInfo.getString("teacherId");
					   if (teacherMap.containsKey(tId)){
						   teacherInfo.put("teacherName", teacherMap
									.get(tId).toString());
					   }else{
						   tArray.remove(teacherInfo);
					   }	   
					}			   
				  }				  
				  sourseTask.put("teachers_"+ courseId, tArray);			
			}				
			List<JSONObject> resultSet = (List<JSONObject>) CollectionUtils
					.subtract(allCourseSet, partCourseSet);
			for (JSONObject result : resultSet) {
				 String field = result.getString("field");
				 sourseTask.put("courseName_" + field, "");
				 sourseTask.put("weekNum_" + field, "");
				 sourseTask.put("nearNum_" + field, "");
				 sourseTask.put("teachers_" + field, "");
			}												
			if (setMap.containsKey(classId)) {
				sourseTask.put("setNum", setMap.get(classId).toString());
			} else {
				sourseTask.put("setNum", "0");
			}
			if (isNotEmpty(totalNum,"totalNum")) {
				float sections = totalNum.getFloat("totalNum");
				sourseTask.put("totalNum", NFormat.format(sections));
			} else {
				sourseTask.put("totalNum", "0");
			}		
		}
		JSONObject timetable = new JSONObject();
		try {
			taskCList = (List<JSONObject>)Sort.sort(SortEnum.ascEnding0rder, taskCList, "className");
		} catch (Exception e) {
			 logger.info("Sort Error!!!");
		}
		timetable.put("data", taskCList);
		if (isNotEmpty(totalNum,"totalMaxDays")) {
			timetable.put("maxDaysForWeek", totalNum.getString("totalMaxDays"));
		}
		timetable.put("column", JSON.parseArray(allCourseSet.toString()));
		return timetable;
	}
	
	@Override
	public List<JSONObject> getTeacherByCondition(JSONObject object) {
		List<JSONObject> resultList = new ArrayList<JSONObject>();
		School school = (School) object.get("school");
		String teacherName = object.getString("teacherId");
		String termInfo = object.getString("selectedSemester");
		Map<String,String> teacherMap = getTeachersMap(school,termInfo);
		if (StringUtils.isNotEmpty(teacherName)) {
			List<Account> accountList = getAccountAllByName(school, teacherName, termInfo);
			for (Account account : accountList) {
				 JSONObject result = new JSONObject();
				 result.put("teacherId", account.getId() + "");
				 result.put("teacherName", account.getName());
				 resultList.add(result);
			}
		} else {
			String courseId = object.getString("courseId");
			if (StringUtils.isNotEmpty(courseId)) {
				long lessonId = Long.parseLong(courseId);
				List<OrgInfo> orgList = commonService.getSchoolOrgList(school, termInfo);
				List<String> repeat = new ArrayList<String>();
				for (OrgInfo org : orgList) {
					 List<Long> cList = org.getLessonIds();
					 if (CollectionUtils.isNotEmpty(cList)
							&& cList.contains(lessonId)) {
						List<Long> idList = org.getMemberAccountIds();
						if (CollectionUtils.isNotEmpty(idList)) {
							for (long accountId : idList) {
								 String idString = accountId + "";
								 if(repeat.contains(idString))continue;
								 if (teacherMap.containsKey(idString)) {
									JSONObject result = new JSONObject();
									result.put("teacherId", idString);
									repeat.add(idString);
									result.put("teacherName",
											teacherMap.get(idString));
									resultList.add(result);									
								 }
							}
						}
					}
				}
			}
		}
		return resultList;
	}
	
	@Override
	public int updateTeachingTasks(JSONObject object) {		
		String timetableId = object.getString("timetableId");
		String schoolId = object.getString("schoolId");
		String courseIds = object.getString("courseId");
		String classIds = object.getString("classId");
		List<String> courseIdList = Arrays.asList(courseIds.split(","));
		List<String> classIdList = Arrays.asList(classIds.split(","));
		for (String classId : classIdList) {
			for (String courseId : courseIdList) {
				 object.put("courseId", courseId);
				 object.put("classId", classId);
				 List<JSONObject> task = timetableDao.getTeachingTaskImbody(object);
				 if (CollectionUtils.isEmpty(task)) {
					 object.put("taskId", UUIDUtil.getUUID());
					 timetableDao.addTeachingTask(object);
				 } else {
					 object.put("taskId", task.get(0).getString("taskId"));
					 timetableDao.updateTeachingTask(object);
				 } 
			}
		}
		return doTimetableCheck(timetableId,schoolId);	
	}

	@Override
	public int updateTeacherAndTask(JSONObject object) {
		/** ---更新教学任务--- **/
		String teacherIds = object.getString("teacherId");
		long lessonId = Long.parseLong(object.getString("courseId"));
		long classId = Long.parseLong(object.getString("classId"));
		String schoolId = object.getString("schoolId");
		String termInfo = object.getString("selectedSemester");
		long schId = Long.parseLong(schoolId);
		List<JSONObject> task = timetableDao.getTeachingTaskImbody(object);
		/** ---教学任务重复--- **/		
		List<String> tasksIdList = new ArrayList<String>();
		for(int i = 1;i < task.size();i++){
			tasksIdList.add(task.get(i).getString("taskId"));
		}
		if (CollectionUtils.isNotEmpty(tasksIdList)){
			JSONObject tasks = new JSONObject();
			tasks.put("schoolId", schoolId);
			tasks.put("timetableId", object.getString("timetableId"));
			tasks.put("taskIdList", tasksIdList);
			timetableDao.deleteTeachingTaskById(tasks);
			timetableDao.deleteTaskTeachers(tasks);
		}
		object.put("taskId", task.get(0).getString("taskId"));	
		/** ---更新教学任务,删除教师信息--- **/
		timetableDao.updateTeachingTask(object);
		timetableDao.deleteTaskTeachers(object);
		/** ---更新教师信息--- **/
		List<JSONObject> teacherList = new ArrayList<JSONObject>();
		Classroom room = new Classroom();
		List<TeacherLesson> lessons = new ArrayList<TeacherLesson>();
		if (StringUtils.isNotEmpty(teacherIds)) {
			List<String> teacherIdList = Arrays.asList(teacherIds.split(","));		
			for (String teacherId : teacherIdList) {
				 JSONObject parameter = new JSONObject();
				 parameter.put("schoolId", schoolId);
				 parameter.put("taskId", object.getString("taskId"));
				 parameter.put("teacherId", teacherId);
				 teacherList.add(parameter);
				 /** ---教师回写数据--- **/
				 TeacherLesson lesson = new TeacherLesson();
				 long userId = getTeacherId(schId,Long.parseLong(teacherId),termInfo);
				 if (userId != 0){
					 lesson.setTeacherId(userId);
				 }
				 lesson.setLessonId(lessonId); 				
				 lessons.add(lesson);
			}
			timetableDao.updateTaskTeachers(teacherList);	
		}else{
			TeacherLesson lesson = new TeacherLesson();
			lesson.setLessonId(lessonId);
			lessons.add(lesson);
		}					
		room.setId(classId);
		room.setTeacherLessons(lessons);
		Classroom roomsrc = commonService.getClassById(schId, classId, termInfo);
		logger.info("classroom merger param roomsrc:"+roomsrc.toString()+"  room:"+room.toString());
		Classroom roomtrg = mergeClassroom(roomsrc,room);
		logger.info("classroom:"+roomsrc.toString()+" after merger :"+roomtrg);
		commonService.updateClassroom(schId, roomtrg, termInfo);	
		String timetableId = object.getString("timetableId");
		return doTimetableCheck(timetableId,schoolId);
	}

	@Override
	public void setImportTeachingTasks(List<JSONObject> taskList,School school,
			String termInfo) {
		/** ---教学任务信息--- **/
		for (JSONObject object : taskList) {
			 String schoolId = object.getString("schoolId");
			 List<JSONObject> task = timetableDao.getTeachingTaskImbody(object);
			 if (CollectionUtils.isNotEmpty(task)) {
				 object.put("taskId", task.get(0).getString("taskId"));
				 timetableDao.updateTeachingTask(object);
			 }else{
				 object.put("taskId", UUIDUtil.getUUID());
				 timetableDao.addTeachingTask(object);
			 } 
			 timetableDao.deleteTaskTeachers(object);
			 String teacher = object.getString("teacherIds");
			 if (StringUtils.isNotEmpty(teacher)) {
				 List<String> teacherIdList = Arrays.asList(teacher.split(","));
				 List<JSONObject> teacherlist = new ArrayList<JSONObject>();
				 for (String teacherId : teacherIdList) {
					  JSONObject parameter = new JSONObject();
					  parameter.put("schoolId", schoolId);
					  parameter.put("taskId", object.getString("taskId"));
					  parameter.put("teacherId", teacherId);
					  teacherlist.add(parameter);		  
				}			
				timetableDao.updateTaskTeachers(teacherlist);						 
			 }		 
		}
		//writebackTeacherList(taskList,school,termInfo);
	}
	
	@Override
	public void setImportMonoWeeks(List<JSONObject> impList,
			String schoolId,String timetableId) {
		JSONObject param = new JSONObject();
		param.put("schoolId", schoolId);
		param.put("timetableId", timetableId);
		// ---数据库中已经存在的单双周设置
		List<JSONObject> dbList = timetableDao.getMonoWeekList(param);
		for(JSONObject source : impList){
			boolean isMerge = false;
			for(JSONObject target : dbList) {
				String s_course1 = source.getString("courseIdOne");
				String t_course1 = target.getString("courseIdOne");
				String s_course2 = source.getString("courseIdTwo");
				String t_course2 = target.getString("courseIdTwo");
				String s_gradeId = source.getString("gradeId");
				String t_gradeId = target.getString("gradeId");
				if (s_course1.equals(t_course1) && s_course2.equals(t_course2)
						&& s_gradeId.equals(t_gradeId)) {
					String t_classIds = target.getString("classIds");
					String s_classIds = source.getString("classIds");
					if (!t_classIds.contains(s_classIds)){						 
						 target.put("classIds",t_classIds+","+s_classIds);
					}
					isMerge = true;break;
				}
			}	
			if(!isMerge)dbList.add(source);
		}
        if (CollectionUtils.isNotEmpty(dbList))
        	timetableDao.updateMonoWeek(dbList);
	}

	@Override
	public void writebackTeacherList(List<JSONObject> infos,School school,
			String termInfo){		
		/** --- 班级room{教师--课程信息(1:1)}(1:n) */
		/** --- 回写班级信息,先删除classId下所有的任教关系(教师-课程信息1:1),再重写 */
		logger.info("infos====>" + infos);
		Map<String,Classroom> roomMap = new HashMap<String,Classroom>();
        for(JSONObject json : infos){
        	String classId = json.getString("classId");
        	String courseId = json.getString("courseId");
        	String teacherIds = "";
        	if (json.containsKey("teacherId")){
        		teacherIds = json.getString("teacherId");
        	}else if(json.containsKey("teacherIds")){
        		teacherIds = json.getString("teacherIds");
        	}
        	// 设置教学任务—导入任课教师增加班主任字段2017-01-10-lime-s
        	String bzr = "";
        	if (json.containsKey("bzr")){
        		bzr = json.getString("bzr");
        	}
        	// 设置教学任务—导入任课教师增加班主任字段2017-01-10-lime-e
        	if (StringUtils.isNotEmpty(teacherIds)){
        		String[] teachers = teacherIds.split(",");
         		for(String teacherId : teachers){
         			TeacherLesson lesson = new TeacherLesson();
    				long userId = getTeacherId(school.getId(),Long.parseLong(teacherId),termInfo);
   				    if (userId != 0){
   					    lesson.setTeacherId(userId);
   				    }
    				lesson.setLessonId(Long.parseLong(courseId));
    				if (roomMap.containsKey(classId)){
        				Classroom room = roomMap.get(classId);
        				List<TeacherLesson> src = room.getTeacherLessons();	
        				mergeTLessonList(src,lesson);
        			}else{
        				long classid = Long.parseLong(classId);
        				Classroom room = new Classroom();
        				room.setId(classid);
    					List<TeacherLesson> lessons = new ArrayList<TeacherLesson>();
        				lessons.add(lesson);
        				room.setTeacherLessons(lessons); 
        				if (StringUtils.isNotEmpty(bzr)) {
        					Long bzrUserId = getTeacherId(school.getId(),Long.parseLong(bzr),termInfo);
        					room.setTeacherId(bzrUserId);// 保存班级只看这个 不看dean
						}
        				roomMap.put(classId, room);
        				
        			}	
         		}
        	}else{//只有班主任
        		long classid = Long.parseLong(classId);
        		Classroom room = commonService.getClassById(school.getId(), classid, termInfo);// 其他的不动
        		Long bzrUserId = getTeacherId(school.getId(),Long.parseLong(bzr),termInfo);
        		logger.info(" bzrUserId==> " + bzrUserId);
				room.setTeacherId(bzrUserId);// 保存班级只看这个 不看dean
				logger.info(" room===> " );
				logger.info(" room===> " + room );
        		roomMap.put(classId, room);
        	}	
        }
        Iterator<Entry<String, Classroom>> iter = roomMap.entrySet().iterator();
        while (iter.hasNext()) { 
        	Map.Entry<String,Classroom> entry = (Map.Entry<String,Classroom>) iter.next();
        	long class_id = Long.parseLong(entry.getKey());
        	Classroom roomsrc = commonService.getClassById(school.getId(), class_id, termInfo);
        	Classroom roomtrg = entry.getValue();
        	Classroom room = mergeClassroom(roomsrc,roomtrg);	
        	logger.info(" room===> " + room );
        	commonService.updateClassroom(school.getId(), room, termInfo);
        }
	}

	private void mergeTLessonList(List<TeacherLesson> src, TeacherLesson lesson) {
		boolean label = true;
		for(TeacherLesson less : src){
			if(less.equals(lesson))label = false;
		}
		if(label)src.add(lesson);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void deleteTeachingTask(JSONObject object) {
        List<String> classIdList = new ArrayList<String>();
        List<String> lessonIdList = new ArrayList<String>();
        String courseIds = object.getString("courseId");
        String classIds = object.getString("classId");
        String timetableId = object.getString("timetableId");
        String termInfo = object.getString("selectedSemester");
        String schoolId = object.getString("schoolId");
        String useGradeId = object.getString("gradeId");
        long schId = Long.parseLong(schoolId); 
        if (StringUtils.isNotEmpty(classIds)){
        	String[] classids = classIds.split(",");
        	for(String classId : classids){
        		classIdList.add(classId);
        	}	
        }else{
        	object.put("gradeId", useGradeId);
        	JSONObject classObj = getClassListBySYNJ(object);
    		classIdList = (List<String>)classObj.get("classIdList");	
        }
        object.put("classIdList", classIdList);
        if (StringUtils.isNotEmpty(courseIds)){   
        	String[] lessonIds = courseIds.split(",");
        	for(String lessonId : lessonIds){
        		lessonIdList.add(lessonId);
        	}
        	object.put("courseIdList", lessonIdList);
        }  	
        /** ---教师信息--- */
        object.remove("classId");object.remove("courseId");
        /** ---回写: 教师、班级、课程信息 */
        List<JSONObject> task = timetableDao.getTeachingTaskImbody(object);
        Map<String,Classroom> roomMap = new HashMap<String,Classroom>();
        for (JSONObject info : task){
        	 String lessonId = info.getString("courseId");
			 String classId = info.getString("classId");
			 if(roomMap.containsKey(classId)){
 				Classroom room = roomMap.get(classId);
 				List<TeacherLesson> lessons = room.getTeacherLessons();
 				TeacherLesson lesson = new TeacherLesson();
 				lesson.setLessonId(Long.parseLong(lessonId));
 				mergeTeacherLessonById(lessons,lesson);        				
 			}else{
 				Classroom room = new Classroom();
 				room.setId(Long.parseLong(classId));
 				List<TeacherLesson> lessons = new ArrayList<TeacherLesson>();
 				JSONArray teacherIds = info.getJSONArray("teacherIds");
 				if (teacherIds==null || teacherIds.size() == 0) {
 					TeacherLesson lesson = new TeacherLesson();
 	 				lesson.setLessonId(Long.parseLong(lessonId));
 	 				lessons.add(lesson);
				}else {
					for (int i = 0; i < teacherIds.size(); i++) {
						TeacherLesson lesson = new TeacherLesson();
						lesson.setLessonId(Long.parseLong(lessonId));
						lesson.setTeacherId(teacherIds.getJSONObject(i).getLongValue("teacherId"));
						lessons.add(lesson);
					}
				}
 				room.setTeacherLessons(lessons);
 				roomMap.put(classId, room);
 			}      	
        }
        Iterator<Entry<String, Classroom>> iter = roomMap.entrySet().iterator();
        while (iter.hasNext()) {
        	Map.Entry<String,Classroom> entry = (Map.Entry<String,Classroom>) iter.next();
        	long class_id = Long.parseLong(entry.getKey());
        	Classroom roomsrc = commonService.getClassById(schId, class_id, termInfo);
        	Classroom roomtrg = entry.getValue();
        	Classroom room = mergeClassroom(roomsrc,roomtrg);
        	commonService.updateClassroom(schId, room, termInfo);
        }
        /** 删除教学任务 */        
    	timetableDao.deleteTeachingTask(object);
    	timetableDao.deleteTaskTeachers(object); 
    	/** 删除合班组 */  
    	List<JSONObject> fixedList = timetableDao.getFixedClassGroup(object);
    	for (JSONObject fixed : fixedList) {
    		 String lessonId = fixed.getString("courseId"); 
    		 if (lessonIdList.contains(lessonId)
    				 ||CollectionUtils.isEmpty(lessonIdList)){
    			 JSONArray array = fixed.getJSONArray("classList");
    			 String classGroupId = fixed.getString("classGroupId");
    			 boolean label = determineDelAll(classIdList,array);
    			 if (label){
    				 JSONObject group = new JSONObject();
    				 group.put("schoolId", schoolId);
    				 group.put("timetableId", timetableId);
    				 group.put("classGroupId", classGroupId);
    				 timetableDao.deleteClassGroup(group);
    			 }else{
    				 JSONObject fix = new JSONObject();
    				 fix.put("schoolId", schoolId);
    				 fix.put("timetableId", timetableId);
    				 fix.put("classIdList", classIdList);
    				 fix.put("classGroupId", classGroupId);
    				 timetableDao.deleteClassFixed(fix);
    			 }
    		 }	 
		}
    	/** 删除单双周 */
    	List<JSONObject> monoList = timetableDao.getMonoWeekList(object);
    	if (classIdList.size() > 1){       	
            for(JSONObject mono : monoList){
            	String course1 = mono.getString("courseIdOne");
            	String course2 = mono.getString("courseIdTwo");
            	if (lessonIdList.contains(course1) 
            			|| lessonIdList.contains(course2)){
            		timetableDao.deleteMonoWeek(mono);
            	}
            }
    	}else if(classIdList.size() == 1){
    		for(JSONObject mono : monoList){
            	String classes = mono.getString("classIds");
            	classes = classes.replace("," + classIds, "");
            	mono.put("classIds", classes);
            	timetableDao.updateMonoWeekClass(mono);
            }
    	}
    	/** 删除预排和排课结果 */  
    	timetableDao.deleteTimetableList(object);
	}
	
	/**-----处理相关课程任教信息-----**/
	private Classroom mergeClassroom(Classroom roomsrc, Classroom roomtrg) {
		if (null == roomsrc)return roomtrg;
		List<TeacherLesson> lessonsrc = roomsrc.getTeacherLessons();
		List<TeacherLesson> lessontrg = roomtrg.getTeacherLessons();
		if (CollectionUtils.isNotEmpty(lessonsrc) && CollectionUtils.isNotEmpty(lessontrg)){
			List<TeacherLesson> target = new ArrayList<TeacherLesson>();
			for(TeacherLesson src: lessonsrc){
				long srcid = src.getLessonId();
				boolean label = true;
				for(TeacherLesson trg:lessontrg){	
					long trgid = trg.getLessonId();
					if (srcid == trgid){
						label = false; break;
					}
				}
				if(label) target.add(src);
			}
			if(target.size() > 0)lessontrg.addAll(target);
		}
		return roomtrg;
	}
	
	/**-----处理相关课程任教信息-----**/
	private void mergeTeacherLessonById(List<TeacherLesson> lessons,
			TeacherLesson lesson) {
		long dst_courseId = lesson.getLessonId();
		boolean label = true;
		for(TeacherLesson tl : lessons) {
			long src_courseId = tl.getLessonId();
			if (dst_courseId == src_courseId) {
				label = false;break;
			}
		}					
		if (label)lessons.add(lesson);
	}
	
	private boolean determineDelAll(List<String> idList, JSONArray array) {
		boolean label = true;
		for(int i = 0;i < array.size();i++){
			JSONObject object = array.getJSONObject(i);
			String classId = object.getString("classId");
			if(!idList.contains(classId))label = false;
		}
		return label;
	}
	
	@Override
	public int addImportTeachers(School sch,List<JSONObject> teacherList,
			ImportTaskParameter stt) {
		Set<JSONObject> teachers = new HashSet<JSONObject>();		
		JSONObject taskParam = new JSONObject();
		Set<String> dset = new HashSet<String>();		
		HashMap<String,String> idsMap = new HashMap<String,String>();
		JSONObject parameter = new JSONObject();
		parameter.put("timetableId", stt.getTimetableId());
		parameter.put("schoolId", stt.getXxdm());
		taskParam.put("schoolId", stt.getXxdm());
		List<JSONObject> list = timetableDao.getTeachingTaskImbody(parameter);
		for(JSONObject object : list){
			 idsMap.put(object.getString("classCourse"), object.getString("taskId"));
		}
		int result = 0;
		if (CollectionUtils.isNotEmpty(teacherList)) {
			for(JSONObject teacher : teacherList) {
				String classId = teacher.getString("classId");
				String courseId = teacher.getString("courseId");												
				if (idsMap.containsKey(classId+courseId)) {
					String taskId = idsMap.get(classId+courseId).toString();
					dset.add(taskId);
					JSONObject param = new JSONObject();
					param.put("schoolId", teacher.getString("schoolId"));
					param.put("taskId", taskId);
					param.put("teacherId", teacher.getString("teacherId"));
					teachers.add(param);
				}				
			}
			if (dset.size() > 0) {
				List<String> tasklist = new ArrayList<String>(dset);
				taskParam.put("taskIdList", tasklist);
				timetableDao.deleteTaskTeachers(taskParam);
			}			
			if (teachers.size() > 0) {
				List<JSONObject> teacherlist = new ArrayList<JSONObject>(teachers);
				result = timetableDao.updateTaskTeachers(teacherlist); 
			}	
		}	
		return result;
	}
	
	@Override
	public List<Long> getTeacherIds(JSONObject object) {
		List<JSONObject> taskList = timetableDao.getTeachingTaskImbody(object);
		List<Long> teacherIds = new ArrayList<Long>();
		for(JSONObject task : taskList){
			String teacher = task.getString("teacherIds");
			JSONArray teachers = JSON.parseArray(teacher);			
			for(int k = 0; k < teachers.size(); k++) {
				JSONObject tInfo = teachers.getJSONObject(k);
				String teacherId = tInfo.getString("teacherId");
				long schId = Long.parseLong(teacherId);
				if(!teacherIds.contains(schId))teacherIds.add(schId);									
			} 
		}
		return teacherIds;
	}

	/**-----TAB页:设置排课规则-----**/	
	@Override
	public JSONArray getTeachingTaskCourse(JSONObject object) {	
		School school = (School)object.get("school");
		String termInfo = object.getString("selectedSemester");
		Map<String,String> courseMap = getLessonsMap(school, termInfo);
	    JSONObject classObj = getClassListBySYNJ(object);
		@SuppressWarnings("unchecked")
		List<String> classIdList = (List<String>)classObj.get("classIdList");
	    JSONArray result = new JSONArray();	  
		if (CollectionUtils.isNotEmpty(classIdList)) {
			object.put("classIdList", classIdList);
			List<JSONObject> taskList = timetableDao.getTeachingTaskCourse(object);
			object.put("gradeDM", convertSYNJToNJDM(object));
			List<String> courseList = timetableDao.getRuleCourseIds(object);
			for (JSONObject taskCourse : taskList) {
			String rows = taskCourse.getString("rows");
			JSONArray rowResult = JSON.parseArray(rows);			
			for (int k = 0; k < rowResult.size(); k++) {
				 JSONObject courseInfo = rowResult.getJSONObject(k);
				 String courseId = courseInfo.getString("courseId");
				 if (courseMap.containsKey(courseId)) {
					 courseInfo.put("courseName", courseMap.get(courseId).toString());
				 }
				 if (CollectionUtils.isNotEmpty(courseList)) {
					 if (courseList.contains(courseId)) {
						 courseInfo.put("isLabel", "1");
					 } else {
						 courseInfo.put("isLabel", "0");
					 }
				 }
				 if(!result.contains(courseInfo))result.add(courseInfo);
			 }	
			}
		}	
	    return result;
	}
	
	@Override
	public int updateCourseRule(JSONObject object) {
        object.put("gradeDM", convertSYNJToNJDM(object));
        List<String> ruleIdList = timetableDao.getCourseRuleId(object);
		int resultOne = 0,resultTwo = 0,result = 0;
		for(String courseRuleId : ruleIdList){
			object.put("courseRuleId", courseRuleId);
			timetableDao.deleteCourseRule(object);
			timetableDao.deleteCourseFixed(object);
		}
		int i = 0;
		String amMaxNum = object.getString("amMaxNum");
		if (StringUtils.isEmpty(amMaxNum)){
			i++;
			object.remove("amMaxNum");
		}		
		String pmMaxNum = object.getString("pmMaxNum");
        if (StringUtils.isEmpty(pmMaxNum)){
        	i++;
        	object.remove("pmMaxNum");
		}
		JSONArray rows = object.getJSONArray("rows");
		if (rows.size() == 0){
			i++;
		}
		String maxNum = object.getString("maxNum");
        if (StringUtils.isEmpty(maxNum)){
        	i++;
        	object.remove("maxNum");
		}
        String courseLevel = object.getString("courseLevel");
		if (StringUtils.isEmpty(courseLevel)||"1".equals(courseLevel)){
			i++;
		}
		if (i < 5) {
			object.put("courseRuleId", UUIDUtil.getUUID());
			resultOne = timetableDao.addCourseRule(object);
			if (resultOne > 0) {
				List<JSONObject> list = new ArrayList<JSONObject>();
				for (int k = 0; k < rows.size(); k++) {
					JSONObject fixed = rows.getJSONObject(k);
					fixed.put("courseRuleId", object.getString("courseRuleId"));
					fixed.put("schoolId", object.getString("schoolId"));
					list.add(fixed);
				}
				if (CollectionUtils.isNotEmpty(list)) {
					resultTwo = timetableDao.addCourseFixed(list);
				}
			}
		}
		String timetableId = object.getString("timetableId");
		String schoolId = object.getString("schoolId");
		if ((resultOne + resultTwo) > 0){
    		 result = doTimetableCheck(timetableId,schoolId);
    	}
		return result;
	}

	@Override
	public void deleteCourseRule(JSONObject object) {
		object.put("gradeDM", convertSYNJToNJDM(object));
		List<String> ruleIdList = timetableDao.getCourseRuleId(object);
		for(String courseRuleId : ruleIdList){
			object.put("courseRuleId", courseRuleId);
			timetableDao.deleteCourseRule(object);
			timetableDao.deleteCourseFixed(object);
		}
	}

	@Override
	public JSONObject getCourseRule(JSONObject object) {		
		JSONObject result = new JSONObject();			
		object.put("gradeDM", convertSYNJToNJDM(object));
		JSONObject temp = timetableDao.getCourseRule(object);
		if (null != temp){
			result = temp;
		}			
		setTotalLessonDay(object,result,true);
		return result;	
	}

	@Override
	public JSONObject getRuleTeacherList(JSONObject object) {
		JSONObject result = new JSONObject();
		try {
			 JSONArray array = new JSONArray();
			 List<Account> accountList = null;
			 String teacherName = "";
			 String orgIds = "";
			 long schoolId = Long.parseLong(object.getString("schoolId"));	
			 School school = (School)object.get("school");
			 String termInfo = object.getString("selectedSemester");
			 List<String> setList = timetableDao.getRuleTeacher(object);		 
			 if (isNotEmpty(object,"gradeGroupId")) {
				 orgIds = object.getString("gradeGroupId");
			 }
			 if (isNotEmpty(object,"researchGroupId")) {
				 orgIds = object.getString("researchGroupId");
			 }
			 if (isNotEmpty(object,"teacherName")) {
				 teacherName = object.getString("teacherName");
			 }
			 if (StringUtils.isEmpty(orgIds)){
				 accountList = commonService.getAllSchoolEmployees(school, termInfo, null);
			 }else{
				 accountList = commonService.getOrgTeacherList(termInfo, schoolId, orgIds, null);
			 }
			 if (CollectionUtils.isNotEmpty(accountList)) {
				 for (Account info : accountList) {
					  JSONObject temp = new JSONObject();
					  String id = info.getId() + "";
					  temp.put("teacherId", id);
					  String name = info.getName();
					  temp.put("teacherName", name);
					  if (CollectionUtils.isNotEmpty(setList)
							&& setList.contains(id)) {
						  temp.put("isLabel", "1");
					  }else{
						  temp.put("isLabel", "0");
					  }
					  if (StringUtils.isNotEmpty(teacherName)){
						  if(name.contains(teacherName))array.add(temp);
					  }else{
						  array.add(temp);
					  }	  
				 }	 				 
			 }
			 if (array.size() > 0) {
				 result.put("teacherList", array);
				 result.put("code", OutputMessage.querySuccess.getCode());
				 result.put("msg", OutputMessage.querySuccess.getDesc());
			 } else {
				 result.put("msg", "未查询到教师信息！！");
				 result.put("code", OutputMessage.querySuccess.getCode());
			 }
		 } catch (Exception e) {
			 e.printStackTrace();
			 result.put("msg", "查询到教师信息出错！！");
			 result.put("code", OutputMessage.queryDataError.getCode());
		 }
		 return result;
	}
	
	@Override
	public JSONObject getTeacherRule(JSONObject object) {	
		School school = (School)object.get("school");
		String termInfo = object.getString("selectedSemester");	
		JSONObject result = new JSONObject();
		Map<String,String> teacherMap = getTeachersMap(school,termInfo);
		List<JSONObject> ruleList = timetableDao.getTeacherRule(object);
		Map<String,JSONObject> map = new LinkedHashMap<String,JSONObject>();
		for(JSONObject obj : ruleList){	
			String groupType = obj.getString("type");
			String ruleId = obj.getString("ruleId");			
			if ("00".equals(groupType)){
				JSONArray tList = null;
				JSONArray rows = null;
				if (map.containsKey(ruleId)){
					JSONObject model = map.get(ruleId);
					tList = model.getJSONArray("teacherList");
					rows = model.getJSONArray("rows");					
				}else{
					JSONObject model = new JSONObject();
					model.put("id", ruleId);
					model.put("name", obj.getString("name"));
					model.put("type", "1");
					tList = new JSONArray();
					rows = new JSONArray();					
					model.put("teacherList", tList);
					model.put("rows", rows);
					map.put(ruleId, model);
				}				
				String teacherId = obj.getString("memberId");
				String dayOfWeek = obj.getString("dayOfWeek");
				String lessonOfDay = obj.getString("lessonOfDay");
				if (StringUtils.isNotEmpty(teacherId)) {
					JSONObject teacher = new JSONObject();
					teacher.put("teacherId", teacherId);
					if (teacherMap.containsKey(teacherId)) {
						String name = teacherMap.get(teacherId);
						if (StringUtils.isNotEmpty(name)){
							teacher.put("teacherName",name);
							tList.add(teacher);
						}						
					}
				}
				if (StringUtils.isNotEmpty(dayOfWeek) &&
						StringUtils.isNotEmpty(lessonOfDay)){						
					JSONObject row = new JSONObject();
					row.put("dayOfWeek", dayOfWeek);
					row.put("lessonOfDay", lessonOfDay);
					rows.add(row);
				}								
			} else if (groupType.contains("1")) {
				JSONObject model = null;
				JSONArray rows = null;	
				JSONArray grouprows = null;
				String teacherName = null;
				if (teacherMap.containsKey(ruleId)){
					teacherName = teacherMap.get(ruleId);
				}
				if (StringUtils.isEmpty(teacherName))continue;
				if (map.containsKey(ruleId)){
					model = map.get(ruleId);
					if ("01".equals(groupType)){
						rows = model.getJSONArray("rows");
					}else{
						grouprows = model.getJSONArray("grouprows");	
					}
				}else{
					model = new JSONObject();
					model.put("id", ruleId);
					model.put("type", "2");
					model.put("maxPerDay", obj.getString("maxPerDay"));
					rows = new JSONArray();
					grouprows = new JSONArray();
					model.put("rows", rows);
					model.put("grouprows", grouprows);	
					map.put(ruleId, model);
				}
				model.put("name", teacherName);
				String dayOfWeek = obj.getString("dayOfWeek");
				String lessonOfDay = obj.getString("lessonOfDay");				
				if (StringUtils.isNotEmpty(dayOfWeek) &&
						StringUtils.isNotEmpty(lessonOfDay)){						
					JSONObject row = new JSONObject();
					row.put("dayOfWeek", dayOfWeek);
					row.put("lessonOfDay", lessonOfDay);
					if ("01".equals(groupType)){
						rows.add(row);
					}else{
						grouprows.add(row);
					}	
				}
			}		
		}
		Iterator<Entry<String, JSONObject>> iter = map.entrySet().iterator();
		List<JSONObject> groupList = new ArrayList<JSONObject>();
        while (iter.hasNext()) {
        	Map.Entry<String,JSONObject> entry = (Map.Entry<String,JSONObject>) iter.next();
        	groupList.add(entry.getValue());
        }
		result.put("ruleList", groupList);
		setTotalLessonDay(object, result, true);
		return result;
	}

	@Override
	public void deleteTeacherRule(JSONObject object) {
		String[] teacherIds = object.getString("teacherId").split(",");
		List<String> teacherIdList = Arrays.asList(teacherIds);
		if (CollectionUtils.isNotEmpty(teacherIdList)){
			object.put("teacherIdList", teacherIdList);
			object.remove("teacherId");
			timetableDao.deleteTeacherRule(object);
		}
		timetableDao.deleteTeacherRuleFixed(object);
	}
	
	@Override
	public int updateTeacherRule(JSONObject object) {
		int result = 0;
		if (isNotEmpty(object,"teacherId")) {
			String[] teacherIds = object.getString("teacherId").split(",");
			List<String> array = Arrays.asList(teacherIds);
			for (String teacherId : array) {
				 object.put("teacherId", teacherId);
				 int number = updateTeachersRule(object);
				 if (number > 0) result++;
			}
		}
		String timetableId = object.getString("timetableId");
		String schoolId = object.getString("schoolId");
		if (result > 0){
    		 result = doTimetableCheck(timetableId,schoolId);
    	}
		return result;
	}

	private int updateTeachersRule(JSONObject object) {
		String teacherRuleId = timetableDao.getTeacherRuleId(object);
		int resultOne = 0,resultTwo = 0;
		if (StringUtils.isNotEmpty(teacherRuleId)) {
			object.put("teacherRuleId", teacherRuleId);
			timetableDao.deleteTeacherRule(object);
			timetableDao.deleteTeacherRuleFixed(object);
		}
		boolean rowEmpty = isNotEmpty(object, "rows")
				&& (object.getJSONArray("rows").size() > 0);
		boolean perEmpty = isNotEmpty(object,"maxPerDay");
		
		if (rowEmpty || perEmpty){
			if(!perEmpty)object.put("maxPerDay", 0);
			object.put("teacherRuleId", UUIDUtil.getUUID());
			object.put("createTime", DateUtil.getDateFormat(new Date(),"yyyyMMddHHmmssSSS"));
			resultOne = timetableDao.updateTeacherRule(object);
			if (rowEmpty){						
				String rows = object.getString("rows");
				JSONArray array = JSON.parseArray(rows);
				List<JSONObject> list = new ArrayList<JSONObject>();
				for (int k = 0; k < array.size(); k++) {
					 JSONObject param = array.getJSONObject(k);
					 param.put("teacherRuleId", object.getString("teacherRuleId"));
					 param.put("schoolId", object.getString("schoolId"));
					 list.add(param);
				}
				if (CollectionUtils.isNotEmpty(list)){
					resultTwo = timetableDao.addTeacherRuleFixed(list);
				}
			}	
		}
		return resultOne + resultTwo;
	}

	@Override
	public String addTeacherGroup(JSONObject object) {
		String teacherGroupId = UUIDUtil.getUUID();
		object.put("teachergroupId", teacherGroupId);
		object.put("createTime", DateUtil.getDateFormat(new Date(),"yyyyMMddHHmmssSSS"));
		int groupNum = timetableDao.updateTeacherGroup(object);
		if (groupNum > 0){
			if (isNotEmpty(object, "teacherId")){
				String[] teacherIds = object.getString("teacherId").split(",");
				List<JSONObject> teaherList = new ArrayList<JSONObject>();
				for (String teacherId : teacherIds) {
					 JSONObject param = new JSONObject();
					 param.put("teachergroupId", object.getString("teachergroupId"));
					 param.put("schoolId", object.getString("schoolId"));
					 param.put("timetableId", object.getString("timetableId"));
					 param.put("teacherId", teacherId);
					 teaherList.add(param);
				}
				if (CollectionUtils.isNotEmpty(teaherList)){
				    timetableDao.addTeacherGroupMembers(teaherList);
				}
			}		
			if (isNotEmpty(object, "rows")
					&& (object.getJSONArray("rows").size() > 0)){
				JSONArray ruleGroups = object.getJSONArray("rows");
				List<JSONObject> list = new ArrayList<JSONObject>();
				for (int k = 0; k < ruleGroups.size(); k++) {
					 JSONObject param = ruleGroups.getJSONObject(k);
					 param.put("teachergroupId", object.getString("teachergroupId"));
					 param.put("schoolId", object.getString("schoolId"));
					 param.put("timetableId", object.getString("timetableId"));
					 list.add(param);
				}
				if (CollectionUtils.isNotEmpty(list)){
					timetableDao.addTeacherGroupFixed(list);
				}
			}	
			return teacherGroupId;
		}else{
			return "";
		}
	}

	@Override
	public int updateTeacherGroup(JSONObject object) {
		int result = 0,retm = 0,retf = 0;
		int groupNum = timetableDao.updateTeacherGroup(object);
		if (groupNum > 0){
			if (isNotEmpty(object, "teacherId")){
				String[] teacherIds = object.getString("teacherId").split(",");				
				List<JSONObject> teaherList = new ArrayList<JSONObject>();
				for (String teacherId : teacherIds) {
					 JSONObject param = new JSONObject();
					 param.put("teachergroupId", object.getString("teachergroupId"));
					 param.put("schoolId", object.getString("schoolId"));
					 param.put("timetableId", object.getString("timetableId"));
					 param.put("teacherId", teacherId);
					 teaherList.add(param);
				}
				timetableDao.delTeacherGroupMembers(object);
				if (CollectionUtils.isNotEmpty(teaherList)){
				    retm = timetableDao.addTeacherGroupMembers(teaherList);
				}
			}		
			if (isNotEmpty(object, "rows")
					&& (object.getJSONArray("rows").size() > 0)){
				JSONArray ruleGroups = object.getJSONArray("rows");
				List<JSONObject> fixList = new ArrayList<JSONObject>();
				for (int k = 0; k < ruleGroups.size(); k++) {
					 JSONObject param = ruleGroups.getJSONObject(k);
					 param.put("teachergroupId", object.getString("teachergroupId"));
					 param.put("schoolId", object.getString("schoolId"));
					 param.put("timetableId", object.getString("timetableId"));
					 fixList.add(param);
				}
				timetableDao.delTeacherGroupFixed(object);
				if (CollectionUtils.isNotEmpty(fixList)){
					retf = timetableDao.addTeacherGroupFixed(fixList);
				}
			}
		}		
		String timetableId = object.getString("timetableId");
		String schoolId = object.getString("schoolId");
		if (retm  > 0 || retf > 0){
    		 result = doTimetableCheck(timetableId,schoolId);
    	}
		return result;
	}

	@Override
	public void deleteTeacherGroup(JSONObject object) {
		int label = timetableDao.deleteTeacherGroup(object);
		if (label > 0){
			timetableDao.delTeacherGroupMembers(object);
			timetableDao.delTeacherGroupFixed(object);
		}
	}

	@Override
	public int addClassGroup(JSONObject object) {
		String classRuleId = UUIDUtil.getUUID();
		object.put("classRuleId", classRuleId);
		object.put("classGroupId", classRuleId);
		object.put("createTime", new Date());
		int result = 0;
		int resultOne = timetableDao.addClassGroup(object);
		if (resultOne > 0) {
			if (isNotEmpty(object, "classIds")){
				String[] classIds = object.getString("classIds").split(",");
				List<String> array = Arrays.asList(classIds);
				List<JSONObject> list = new ArrayList<JSONObject>();
				for (String classId : array) {
					 JSONObject param = new JSONObject();
					 param.put("classId", classId);
					 param.put("classRuleId", classRuleId);					
					 param.put("schoolId", object.getString("schoolId"));
					 list.add(param);
				}
				if (CollectionUtils.isNotEmpty(list)){
					timetableDao.addFixedClassGroup(list);			
				}
			}
			String timetableId = object.getString("timetableId");
			String schoolId = object.getString("schoolId");
			result = doTimetableCheck(timetableId,schoolId);
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<JSONObject> getClassGroup(JSONObject parameter) {
		School school = (School)parameter.get("school");
		String termInfo = parameter.getString("selectedSemester");
		Map<String,String> classMap = getClassesMap(school,termInfo);
		List<JSONObject> list = timetableDao.getFixedClassGroup(parameter);
		for (JSONObject object : list) {
			 JSONArray classArray = object.getJSONArray("classList");
			 List<JSONObject> classList = new ArrayList<JSONObject>();
			 for(int i =0; i < classArray.size(); i++){
				 JSONObject classInfo = classArray.getJSONObject(i);
				 String classId = classInfo.getString("classId");
				 JSONObject info = new JSONObject();
				 info.put("classId", classId);
				 String classSrcName = "\u5DF2\u5220\u9664";
				 if (classMap.containsKey(classId)){
					 String className = classMap.get(classId);			 
					 if(StringUtils.isNotEmpty(className)){
						classSrcName =  className;
					 }					 
				 }
				 info.put("className", classSrcName);
				 classList.add(info);
			 }
			 try {
				 object.put("classList", (List<JSONObject>) Sort.sort(SortEnum.ascEnding0rder, classList, "className"));
			} catch (Exception e) {
				logger.info("Sort Error!!!");
			}
		}
		return list;
	}

	@Override
	public int deleteClass(JSONObject object) {
		int number = timetableDao.deleteClassFixed(object);
		String timetableId = object.getString("timetableId");
		String schoolId = object.getString("schoolId");
		int result = 0;
		if (number > 0){
    		result = doTimetableCheck(timetableId,schoolId);
    	}
		return result; 
	}

	@Override
	public int deleteClassGroup(JSONObject object) {
		int resultOne = 0,resultTwo = 0,result = 0;
		resultOne = timetableDao.deleteClassFixed(object);
		resultTwo = timetableDao.deleteClassGroup(object);
		String timetableId = object.getString("timetableId");
		String schoolId = object.getString("schoolId");
		String termInfo = object.getString("selectedSemester");
		/** ---更新合班组名称--- */
		School school = (School)object.get("school");
		if (isNotEmpty(object,"courseId")){
			String courseId = object.getString("courseId");
			Map<String,String> courseMap = getLessonsMap(school, termInfo);
			if (courseMap.containsKey(courseId)){
				String courseName = courseMap.get(courseId);
			    List<String> CList = timetableDao.getClassGroup(object);
			    for(int i = 1;i<= CList.size();i++){
			    	JSONObject temp = new JSONObject();
			    	String classRuleId = CList.get(i-1);
			    	temp.put("timetableId", timetableId);
			    	temp.put("schoolId", schoolId);
			    	temp.put("classRuleId", classRuleId);
			    	temp.put("courseId", courseId);
			    	temp.put("classGroupName", courseName+i+"\u7EC4");
			    	timetableDao.updateClassGroupName(temp);
			    }		    
			}
		}
		if ((resultOne + resultTwo) > 0){
    		 result = doTimetableCheck(timetableId,schoolId);
    	}
		return result;
	}
	
	@Override
	public int addMonoWeek(JSONObject object) {
		List<String> lessonList = new ArrayList<String>();
		lessonList.add(object.getString("courseIdOne"));
		lessonList.add(object.getString("courseIdTwo"));
		JSONObject classObj = getClassListBySYNJ(object);
		@SuppressWarnings("unchecked")
		List<String> classIdList = (List<String>)classObj.get("classIdList");
		Set<String> classIds = new HashSet<String>();
		if (CollectionUtils.isNotEmpty(classIdList)) {
	        object.put("classIdList", classIdList);
	        List<JSONObject> courseList = timetableDao.getMonoTaskCourse(object);
			Map<String,JSONArray> ctcMap = new HashMap<String,JSONArray>();
	        for (JSONObject ltc : courseList){
	        	  String courseId = ltc.getString("courseId");
	        	  ctcMap.put(courseId, ltc.getJSONArray("monoLessonIdList"));
	        }  	
	        for (String course : lessonList){
	        	 if (ctcMap.containsKey(course)){
	    		     JSONArray array = ctcMap.get(course);
	        	     for (int i = 0; i<array.size();i++){
	        		 JSONObject classobj = array.getJSONObject(i);
	        		 classIds.add(classobj.getString("classId"));       		     
	        	    }
	    	    }	        	
	        }   	     			   	       	       
	    }   
		object.put("classIds", StringUtils.join(classIds, ","));
		int number = timetableDao.addMonoWeek(object);
		String timetableId = object.getString("timetableId");
		String schoolId = object.getString("schoolId");
		int result = 0;
		if (number > 0){
    		 result = doTimetableCheck(timetableId,schoolId);
    	}
		return result; 
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public JSONObject getMonoWeek(JSONObject object){
		JSONObject result = new JSONObject();
		List<JSONObject> courseIdList = new ArrayList<JSONObject>();
		List<JSONObject> rows = new ArrayList<JSONObject>();	
		School school = (School)object.get("school");
		String termInfo = object.getString("selectedSemester");
		Map<String,String> lessonMap = getLessonsMap(school, termInfo);
		JSONObject classObj = getClassListBySYNJ(object);
		List<String> classIdList = (List<String>)classObj.get("classIdList");
		HashMap<String,String> roomMap = (HashMap<String,String>)classObj.get("classMap");	
		String gradeName = classObj.getString("gradeName");	
		if (CollectionUtils.isNotEmpty(classIdList)) {
	        object.put("classIdList", classIdList);
	        List<JSONObject> monoList = timetableDao.getMonoWeekList(object);
	        List<JSONObject> courseList = timetableDao.getMonoTaskCourse(object);
			HashMap<String,String> lesson = new LinkedHashMap<String,String>();			
	        for (JSONObject ltc : courseList){
	        	 String courseId = ltc.getString("courseId");
	        	 if (lessonMap.containsKey(courseId)){
	        		 lesson.put(courseId, lessonMap.get(courseId).toString());
	        	 }else{
	        		 lesson.put(courseId, "");
	        	 }
	        }
	        for (JSONObject mono : monoList){
	        	 JSONObject row = new JSONObject();
	        	 row.put("gradeName", gradeName);
	        	 String course1 = mono.getString("courseIdOne");
	        	 String courseName1 = "";
	        	 String courseName2 = "";
	        	 List<String> classNames = new ArrayList<String>();
	        	 if (lessonMap.containsKey(course1)){
	        		 courseName1 = lessonMap.get(course1);
	        	 }
	        	 String course2 = mono.getString("courseIdTwo");
	        	 if (lessonMap.containsKey(course2)){
	        		 courseName2 = lessonMap.get(course2);
	        	 }
	        	 row.put("courseNames", courseName1 + "|" + courseName2);
	        	 String classIds = mono.getString("classIds");
	        	   
	        	 List<String> cList = Arrays.asList(classIds.split(","));
	        	 for(int k =0; k < cList.size(); k++){
	        		 String className = roomMap.get(cList.get(k));
	        		 if (StringUtils.isNotEmpty(className)){
	        			 classNames.add(roomMap.get(cList.get(k)));  
	        		 }  		 
	        	 }
				 try {
					  classNames = (List<String>) Sort.sort(SortEnum.ascEnding0rder, classNames, null);
				 } catch (Exception e) {
					 logger.info("Sort Error!!!");
				 }
	        	 row.put("courseIdOne", course1);
	        	 row.put("courseIdTwo", course2);
	        	 row.put("classNames", StringUtils.join(classNames,"、"));
	        	 rows.add(row);		        	   
	        	 if(lesson.containsKey(course1))lesson.remove(course1);
	        	 if(lesson.containsKey(course2))lesson.remove(course2);		        	   
	        }
	        Iterator<Entry<String, String>> iter = lesson.entrySet().iterator();
	        while (iter.hasNext()) {
	        	    JSONObject courseObj = new JSONObject();
	                Map.Entry<String,String> entry = (Map.Entry<String,String>) iter.next();
	                courseObj.put("courseId", entry.getKey());
	                courseObj.put("courseName", entry.getValue());
	                courseIdList.add(courseObj);
	        }		       		        
		}
		result.put("rows", rows);
		result.put("courseIdList", courseIdList);						
		return result;
	}

	@Override
	public void deleteMonoWeek(JSONObject object) {
		timetableDao.deleteMonoWeek(object);
	}

	@Override
	public JSONArray getGroundRuleList(JSONObject object) {
		String timetableId = object.getString("timetableId");
		String schoolId = object.getString("schoolId");	
		School school = (School)object.get("school");
		String termInfo = object.getString("selectedSemester");
		Map<String,String> courseMap = getLessonsMap(school, termInfo);
		List<JSONObject> gList = getRuleGroundList(schoolId,timetableId);
		Map<String,String> reqMap = new HashMap<String, String>();
		reqMap.put("schoolId",schoolId);
		reqMap.put("timetableId",timetableId);
		List<JSONObject> totalList = timetableDao.getGroundRuleSum(reqMap);
		Map<String,String> totalMap = new HashMap<String,String>();
		for (JSONObject totalNum : totalList) {
			totalMap.put(totalNum.getString("subjectId"),
					totalNum.getInteger("totalNum").toString());
		}
		JSONArray groundArray = new JSONArray();
		for(JSONObject ground : gList){
			JSONObject temp = new JSONObject();
			if (ground.containsKey("GroundRuleId")){
				temp.put("ruleGroundId", ground.getString("GroundRuleId"));
			}
			if (ground.containsKey("SubjectId")){
				String lessId = ground.getString("SubjectId");
				if (StringUtils.isNotEmpty(lessId)){
					temp.put("subjectId", lessId);
					if (courseMap.containsKey(lessId)){
						temp.put("subjectName", courseMap.get(lessId));
					}
					if (totalMap.containsKey(lessId)){
						temp.put("totalNum", totalMap.get(lessId));
					}
				}		
			}
			if (ground.containsKey("MaxClassNum")){
				temp.put("maxClassNum", ground.getString("MaxClassNum"));
			}
			if (ground.containsKey("UsedGradeIds")){
				String usedGradeIds = ground.getString("UsedGradeIds");
				if (StringUtils.isNotEmpty(usedGradeIds)){
					temp.put("usedGradeIds", usedGradeIds);
					String usedGradeNames = "";
					if (termInfo.length() >= 4){
						String xn = termInfo.substring(0, 4);
						String[] gradeIds = usedGradeIds.split(",");
						List<String> gradeNames = new ArrayList<String>();
						for(String useGrade : gradeIds){
							gradeNames.add(getGradeNameBySYNJ(useGrade,xn));
						}
						if (CollectionUtils.isNotEmpty(gradeNames)){
							ChineseSort.sortByJx(gradeNames);
					        usedGradeNames = StringUtils.join(gradeNames, ",");
						}
					}
					if (StringUtils.isNotEmpty(usedGradeNames)){
						temp.put("usedGradeNames", usedGradeNames);
					}
				}	
			}
			groundArray.add(temp);
		}
		return groundArray;
	}

	@Override
	public int updateGroundRule(JSONObject object) {
		int number = timetableDao.updateGroundRule(object);
		String timetableId = object.getString("timetableId");
		String schoolId = object.getString("schoolId");
		int result = 0;
		if (number > 0){
    		result = doTimetableCheck(timetableId,schoolId);
    	}
		return result;
	}
	
	@Override
	public int insertGroundRule(JSONObject object) {
		int number = timetableDao.insertGroundRule(object);
		String timetableId = object.getString("timetableId");
		String schoolId = object.getString("schoolId");
		int result = 0;
		if (number > 0){
    		result = doTimetableCheck(timetableId,schoolId);
    	}
		return result;
	}

	@Override
	public void deleteGroundRule(JSONObject object) {
		timetableDao.deleteGroundRule(object);
	}
	
	/**
	public JSONObject getSubjectAndGradeList2(JSONObject object) {
		JSONObject result = new JSONObject();
		School school = (School)object.get("school");
		String termInfo = object.getString("selectedSemester");
		Map<String,String> courseMap = getLessonsMap(school, termInfo);
		HashMap<String,Object> reqMap = new HashMap<String, Object>();
		reqMap.put("schoolId",object.getString("schoolId"));
		reqMap.put("timetableId",object.getString("timetableId"));	
		// ----查询课表下年级列表
		List<JSONObject> gradelist = this.getTimetableGradeList(reqMap);		
		Map<String, Grade> gradesDic = this.arrangeDataService.getGradesDic( termInfo.substring(0,4),school,termInfo.substring(4,5));
		List<JSONObject> gradeList = new ArrayList<JSONObject>();
		for(JSONObject obj:gradelist){
			String gradeId = obj.getString("gradeId");			
			Grade grade = gradesDic.get(gradeId);
			if(grade==null){
				continue;
			}
			String gradeName = this.arrangeDataService.getGradeName(grade);			
			JSONObject v = new JSONObject();
			v.put("gradeId", gradeId);
			v.put("gradeName", gradeName);			
			gradeList.add(v);
		}
		ScoreUtil.sorStuScoreList(gradeList, "gradeId", "desc", "", "");
		if (CollectionUtils.isNotEmpty(gradeList)){
			result.put("gradeList", gradeList);
		}
		// ----查询课表下科目列表
		List<JSONObject> subjectList = new ArrayList<JSONObject>();
		List<JSONObject> lesslist = this.getTimetableSubjectList(reqMap);
		for(JSONObject obj:lesslist){
			JSONObject v = new JSONObject();
			String lessId = obj.getString("courseId");					
			if (courseMap.containsKey(lessId)){
				String subjectName = courseMap.get(lessId);
				if (StringUtils.isNotEmpty(subjectName)){
					v.put("subjectId", lessId);
					v.put("subjectName", subjectName);
					subjectList.add(v);
				}	
			}		
		}
		ScoreUtil.sorStuScoreList(subjectList, "subjectId", "asc", "", "");
		if (CollectionUtils.isNotEmpty(subjectList)){
			result.put("subjectList", subjectList);
		}	
		return result;
	}  */
	
	@Override
	public JSONObject getSubjectAndGradeList(JSONObject object) {
		JSONObject result = new JSONObject();
		List<JSONObject> subjectList = new ArrayList<JSONObject>();
		School school = (School)object.get("school");
		long schoolId = Long.parseLong(object.getString("schoolId"));
		String termInfo = object.getString("selectedSemester");
		Map<String,String> courseMap = getLessonsMap(school, termInfo);
		/** ----查询全校所有班级-年级对应关系---- **/ 
		List<Grade> gradeList = commonService.getGradeList(school, termInfo);
		Map<String,String> classMap = new HashMap<String,String>();
		for(Grade grade : gradeList){
			String usedGradeId = convertSYNJByGrade(schoolId, grade, termInfo);					
			List<Long> ids = grade.getClassIds();
			for(Long id : ids){
				classMap.put(id + "", usedGradeId);
			}
		}
		/** ----查询教学任务下的科目列表---- **/ 
		JSONObject reqJSON = new JSONObject();
		reqJSON.put("schoolId",object.getString("schoolId"));
		reqJSON.put("timetableId",object.getString("timetableId"));	
		List<JSONObject> courseList = timetableDao.getRoundruleCourse(reqJSON);
		for(JSONObject course : courseList){			
			String lessId = course.getString("courseId");			
			if (courseMap.containsKey(lessId)) {
				String subjectName = courseMap.get(lessId);
				if (StringUtils.isNotEmpty(subjectName)) {
					JSONObject v = new JSONObject();
					v.put("subjectId", lessId);
					v.put("subjectName", subjectName);
					String classIds = course.getString("rows");					
					String grades = course.getString("gradeIds");
					JSONArray rowArray = JSON.parseArray(classIds);
					List<String> collection = new ArrayList<String>();
					JSONArray gradeArray = new JSONArray();
					for (int i = 0; i < rowArray.size(); i++) {
						String classId = rowArray.getJSONObject(i).getString(
								"classId");
						if (classMap.containsKey(classId)) {
							String useGrade = classMap.get(classId);
							if (!collection.contains(useGrade)
									&& grades.contains(useGrade)) {
								String gradeName = getGradeNameBySYNJ(useGrade,
										termInfo.substring(0, 4));
								JSONObject g = new JSONObject();
								g.put("gradeId", useGrade);
								g.put("gradeName", gradeName);
								gradeArray.add(g);
								collection.add(useGrade);
							}
						}
					}
					if (gradeArray.size() >0 )
						v.put("gradeList", gradeArray);
					subjectList.add(v);
				}
			}				
		}
		if (CollectionUtils.isNotEmpty(subjectList)){
			result.put("subjectList", subjectList);
		}	
		return result;
	}
	
	/**-----TAB页:预排课-----**/
	@SuppressWarnings("unchecked")
	@Override
	public List<JSONObject> getWalkthroughTaskCourse(JSONObject object) {
		List<JSONObject> courseList = timetableDao.getWalkthroughCourse(object);		
		School school = (School)object.get("school");
		long schoolId = Long.parseLong(object.getString("schoolId"));
		String termInfo = object.getString("selectedSemester");
		// 查询所有科目信息
		Map<String,String> courseMap = getLessonsMap(school, termInfo);
		// 查询所有年级信息
		List<Grade> gradeList = commonService.getGradeList(school, termInfo);
		Map<String,List<String>> gradeMap = new HashMap<String,List<String>>();
		Map<String,String> classMap = new HashMap<String,String>();
		for(Grade grade : gradeList){
			List<String> classIds = new ArrayList<String>();
			String usedGradeId = convertSYNJByGrade(schoolId, grade, termInfo);					
			List<Long> ids = grade.getClassIds();
			for(Long id : ids){
				classIds.add(id + "");
				classMap.put(id + "", usedGradeId);
			}
			gradeMap.put(usedGradeId, classIds);
		}
		Map<String,JSONArray> rowsMap = new HashMap<String,JSONArray>();
		for(JSONObject taskCourse : courseList) {
			String classId = taskCourse.getString("classId");
			String gradeIds = taskCourse.getString("gradeIds");
			/*** 判断classId在OMS中是否仍然存在 ***/
			if (classMap.containsKey(classId)){
				String usedGrade = classMap.get(classId);
				/*** 判断教学任务中的记录与年级设置同步 ***/
				if (gradeIds.contains(usedGrade)){
					JSONArray currentRows = taskCourse.getJSONArray("rows");
					JSONArray rows = null; 
					if (rowsMap.containsKey(usedGrade)){
						rows = rowsMap.get(usedGrade);
					}else{
						rows = new JSONArray();
						rowsMap.put(usedGrade,rows);
					}
					for(int i = 0; i < currentRows.size(); i++) {
						JSONObject course = currentRows.getJSONObject(i);
						if (!rows.contains(course))rows.add(course);
					}
				}
			}
		}
		List<JSONObject> resultList = new ArrayList<JSONObject>();
		Iterator<String> it = rowsMap.keySet().iterator();
		while (it.hasNext()) {
			   JSONObject data = new JSONObject();
		       String usedGrade = it.next();		       		       
		       String xn = termInfo.substring(0, 4);
			   String gradeName = getGradeNameBySYNJ(usedGrade,xn);
		       data.put("gradeId", usedGrade);
		       data.put("gradeName", gradeName);
		       JSONArray rows = rowsMap.get(usedGrade);		       
		       List<String> classIdList = gradeMap.get(usedGrade);
		       List<String> cList = null;
			   if (CollectionUtils.isNotEmpty(classIdList)){
				   object.put("classIdList", classIdList);
				   object.put("isAdvance", TIMETABLE_RESEARCH);
				   cList = timetableDao.getAdvanceCourses(object);
			   } 
			   for(int j = 0; j < rows.size(); j++) {
				   JSONObject row = rows.getJSONObject(j);				   
				   String courseId = row.getString("courseId");
				   if (courseMap.containsKey(courseId)) {
					   row.put("courseName", courseMap.get(courseId).toString());
				   }
				   if (CollectionUtils.isNotEmpty(cList)) {
					   if (cList.contains(courseId)) {
						   row.put("isLabel", "1");
					   } else {
						   row.put("isLabel", "0");
					   }
				   }
			   }  
			   data.put("rows", rows);
			   object.put("gradeId", usedGrade);
			   setTotalLessonDay(object,data,true);
			   resultList.add(data);
			   try {
				   resultList = (List<JSONObject>)Sort.sort(SortEnum.ascEnding0rder, resultList, "gradeName");
			   } catch (Exception e) {
					e.printStackTrace();
			   }
		}
		return resultList;
	}

	@Override
	public int updateCourseWalkthrough(JSONObject object) {
		String classIds = object.getString("classId");
		object.remove("classId");
		String timetableId = object.getString("timetableId");
		String schoolId = object.getString("schoolId");
		String courseId = object.getString("courseId");
		List<String> classIdList = Arrays.asList(classIds.split(","));		
		int tempResult = 0;
		String walkGroupId = UUIDUtil.getUUID();
		if(isNotEmpty(object,"walkGroupId")){
		   walkGroupId = object.getString("walkGroupId");
		}
		if(CollectionUtils.isNotEmpty(classIdList)){
		   object.put("classIdList", classIdList);
		}
		object.put("isAdvance", TIMETABLE_RESEARCH);
		timetableDao.deleteTimetableList(object);
		for (String classId : classIdList){
			 List<JSONObject> list = new ArrayList<JSONObject>();
			 String rows = object.getString("rows");
			 JSONArray array = JSON.parseArray(rows);
			 for(int k = 0; k < array.size(); k++){
				 JSONObject obj = array.getJSONObject(k);
				 obj.put("timetableId", timetableId);
				 obj.put("classId", classId);
				 obj.put("courseId", courseId);
				 obj.put("isAdvance", TIMETABLE_RESEARCH);
				 obj.put("courseType", TIMETABLE_REGULARCOURSE);
				 obj.put("walkGroupId", walkGroupId);
				 obj.put("schoolId", schoolId);				 
				 list.add(obj);
			  }
			 if (CollectionUtils.isNotEmpty(list)){
				 tempResult = timetableDao.insertTimetableList(list);
			 }  
		}	
		return tempResult;
	}

	@Override
	public JSONObject getCourseWalkthrough(JSONObject object) {
		JSONObject result = new JSONObject();
		String classIds = object.getString("classId");
		List<String> classIdList = Arrays.asList(classIds.split(","));	
		object.put("isAdvance", TIMETABLE_RESEARCH);
		if (CollectionUtils.isNotEmpty(classIdList)){
			object.put("classIdList", classIdList);
		}
		String courseId = object.getString("courseId");
		setTotalLessonDay(object,result,false);	
		List<JSONObject> wList = timetableDao.getTimetablePartList(object);
		JSONArray rows = new JSONArray();
		JSONArray conflicts = new JSONArray();
		for (JSONObject lesson : wList){
			 JSONObject info = new JSONObject();
			 String lessonId = lesson.getString("courseId");
			 info.put("lessonOfDay", lesson.getString("lessonOfDay"));
			 info.put("dayOfWeek", lesson.getString("dayOfWeek")); 
			 if (lessonId.equals(courseId)){
				 rows.add(info); 
			 }else{
				 conflicts.add(info);
			 }			
		}
		if (rows.size() > 0)result.put("rows", rows);
		if (conflicts.size() > 0)result.put("conflicts", conflicts);
		return result;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<JSONObject> getScheduledCourseList(JSONObject object) {		
		School school = (School)object.get("school");
		String termInfo = object.getString("selectedSemester");
		Map<String,String> lessonMap = getLessonsMap(school, termInfo);
		Map<String,String> classMap = getClassesMap(school,termInfo);
		JSONObject classObj = getClassListBySYNJ(object);
		List<String> classIdList = (List<String>)classObj.get("classIdList");
		if (CollectionUtils.isNotEmpty(classIdList)){
			object.put("classIdList", classIdList);
		}
		object.put("isAdvance", TIMETABLE_RESEARCH);
		List<JSONObject> scheduledList = timetableDao.getTimetableList(object);	
		for (JSONObject scheduled : scheduledList){
			 String setNum = scheduled.getJSONArray("rows").size() + "";
			 scheduled.put("setNum", setNum);
			 JSONArray classArray = scheduled.getJSONArray("classList"); 
			 
			 List<JSONObject> classList = new ArrayList<JSONObject>();
			 for(int i =0; i < classArray.size(); i++){
				 JSONObject classInfo = classArray.getJSONObject(i);
				 String classId = classInfo.getString("classId");	
				 if (classMap.containsKey(classId)){
					 String name = classMap.get(classId).toString();
					 JSONObject info = new JSONObject();
					 info.put("classId", classId);
					 if (StringUtils.isNotEmpty(name)){
						 info.put("className", name);
					 }else{
						 info.put("className", "\u5DF2\u5220\u9664");
					 }
					 classList.add(info);
				 }
			  }
			  try {
				 scheduled.put("classList", (List<JSONObject>) Sort.sort(SortEnum.ascEnding0rder, classList, "className"));
			 } catch (Exception e) {
				logger.info("Sort Error!!!");
			 }
			 String courseId = scheduled.getString("courseId");
			 if (lessonMap.containsKey(courseId)){
				 scheduled.put("courseName", lessonMap.get(courseId).toString());
			 }		 
		} 
		return scheduledList;
	}

	@Override
	public int deleteCourseWalkthrough(JSONObject object) {
		return timetableDao.deleteTimetableList(object);
	}

	@Override
	public int addActivityWalkthroughGroup(JSONObject object) {		
		int number = timetableDao.updateResearchWork(object);
		String timetableId = object.getString("timetableId");
		String schoolId = object.getString("schoolId");
		int result = -1;
		if (number > 0){
    		result = doTimetableCheck(timetableId,schoolId);
    	}
		return result;
		
	}

	@Override
	public JSONObject getActivityWalkthrough(JSONObject object) {
		JSONObject result = new JSONObject();
		School school = (School)object.get("school");
		String termInfo = object.getString("selectedSemester");
		Map<String,String> teacherMap = getTeachersMap(school, termInfo);
		setTotalLessonDay(object,result,true);
		List<JSONObject> activityList = timetableDao.getResearchWorksList(object);
		List<JSONObject> resultList = new ArrayList<JSONObject>();
		for (JSONObject temp : activityList){			
			 JSONObject tempObj = new JSONObject();
			 tempObj.put("researchId", temp.getString("researchId"));
			 tempObj.put("researchGroupName", temp.getString("teacherGroupName"));
			 JSONArray tArray = temp.getJSONArray("teacherList"); 
			 for (int i = 0; i < tArray.size(); i++){
				   JSONObject teacherInfo = tArray.getJSONObject(i);
				   if (isNotEmpty(teacherInfo, "teacherId")) {
				   String tId = teacherInfo.getString("teacherId");
				   if (teacherMap.containsKey(tId)){
					   teacherInfo.put("teacherName", teacherMap.get(tId).toString());
				       }else{
					   tArray.remove(teacherInfo);
				       }
				   }			 			 
		     }
			 tempObj.put("teacherList", tArray);
			 tempObj.put("rows", temp.getJSONArray("rows"));
			 resultList.add(tempObj);
		}	 
		result.put("researchList", resultList);
		return result;
	}	

	@Override
	public int updateActivityWalkthroughMember(JSONObject object) {
		String schoolId = object.getString("schoolId");
		String researchId = object.getString("researchId");
		timetableDao.deleteResearchTeach(object);
		int number = 0,result = 0; 
		if (isNotEmpty(object, "teacherIds")){
			String[] teachers = object.getString("teacherIds").split(",");
			List<String> teacherIdList = Arrays.asList(teachers);
			List<JSONObject> teacherlist = new ArrayList<JSONObject>();
			for (String teacherId : teacherIdList){
				 JSONObject parameter = new JSONObject();
				 parameter.put("researchId", researchId);
				 parameter.put("schoolId", schoolId);
				 parameter.put("teacherId", teacherId);
				 teacherlist.add(parameter);
			}
			if (CollectionUtils.isNotEmpty(teacherlist)){
				number = timetableDao.addResearchTeacher(teacherlist);
			}
		}
		String timetableId = object.getString("timetableId");
		if (number > 0){
    		result = doTimetableCheck(timetableId,schoolId);
    	}
		return result;
	}

	@Override
	public int updateActivityWalkthrough(JSONObject object) {
		String schoolId = object.getString("schoolId");
		String researchId = object.getString("researchId");
		int number = 0,result = 0;
		if (isNotEmpty(object, "rows")){
			String rows = object.getString("rows");
			JSONArray rowArray = JSON.parseArray(rows);
			List<JSONObject> fixedlist = new ArrayList<JSONObject>();
            for (int k = 0; k < rowArray.size(); k++){
           	     JSONObject parameter = rowArray.getJSONObject(k);
			     parameter.put("researchId", researchId);
			     parameter.put("schoolId", schoolId);
			     fixedlist.add(parameter);
            }
            timetableDao.deleteResearchFixed(object);
            if (CollectionUtils.isNotEmpty(fixedlist)){
            	number = timetableDao.addResearchFixed(fixedlist);
            }
		}
		String timetableId = object.getString("timetableId");
		if (number > 0){
    		result = doTimetableCheck(timetableId,schoolId);
    	}
		return result;
	}

	@Override
	public JSONObject getScheduledActivity(JSONObject object) {
		List<JSONObject> activityList = timetableDao.getResearchWorksList(object);
		JSONObject result = new JSONObject();
		if (!activityList.isEmpty()){			 
			JSONObject temp = activityList.get(0);
			if(null == temp)temp = new JSONObject();
		       setTotalLessonDay(object,temp,false);
			result.put("data", temp);
		}
		return result;		
	}
	
	@Override
	public void deleteActivityWalkthrough(JSONObject object) {			
		timetableDao.deleteResearchFixed(object);
	}

	@Override
	public void deleteActivityWalkthroughGroup(JSONObject object) {	
		int workResult = timetableDao.deleteResearchWork(object);
		if (workResult > 0){
			timetableDao.deleteResearchFixed(object);
			timetableDao.deleteResearchTeach(object);
		}
	}

	@Override
	public int updateActivityWalkthroughGroup(JSONObject object) {
		String schoolId = object.getString("schoolId");
		String researchId = object.getString("researchId");
        if (StringUtils.isEmpty(researchId))
        	object.put("researchId", UUIDUtil.getUUID()); 
        object.put("teacherGroupName", object.getString("researchGroupName"));
        int record = 0,number1 = 0,number2 = 0,result = 0;
        record = timetableDao.updateResearchWork(object);
		if (record > 0 && isNotEmpty(object, "rows")){
			String rows = object.getString("rows");
			JSONArray rowArray = JSON.parseArray(rows);
			List<JSONObject> fixedlist = new ArrayList<JSONObject>();
            for (int k = 0; k < rowArray.size(); k++){
           	     JSONObject parameter = rowArray.getJSONObject(k);
			     parameter.put("researchId", researchId);
			     parameter.put("schoolId", schoolId);
			     fixedlist.add(parameter);
            }
            timetableDao.deleteResearchFixed(object);
            if (CollectionUtils.isNotEmpty(fixedlist)){
            	number1 = timetableDao.addResearchFixed(fixedlist);
            }
		}
		if (record > 0 && isNotEmpty(object, "teacherIds")){
			String[] teachers = object.getString("teacherIds").split(",");
			List<String> teacherIdList = Arrays.asList(teachers);
			List<JSONObject> teacherlist = new ArrayList<JSONObject>();
			for (String teacherId : teacherIdList){
				 JSONObject parameter = new JSONObject();
				 parameter.put("researchId", researchId);
				 parameter.put("schoolId", schoolId);
				 parameter.put("teacherId", teacherId);
				 teacherlist.add(parameter);
			}
			timetableDao.deleteResearchTeach(object);
			if (CollectionUtils.isNotEmpty(teacherlist)){
				number2 = timetableDao.addResearchTeacher(teacherlist);
			}
		}
		String timetableId = object.getString("timetableId");
		if ((number1 + number2) >0){
    		 result = doTimetableCheck(timetableId,schoolId);
    	}
		return result;
	}

	/**-----TAB页:课表APP查询-----**/
	@Override
	public JSONObject getClassTimetableByDay(JSONObject object) {
		School school = (School)object.get("school");
		String termInfo = object.getString("selectedSemester");
		Map<String,String> teacherMap = getTeachersMap(school,termInfo);
		Map<String,String> lessonMap = getLessonsMap(school,termInfo);
		JSONObject result = new JSONObject();
		String timetableId = object.getString("timetableId");
		String schoolId = object.getString("schoolId");
		long sid = Long.parseLong(schoolId);
		long classId = Long.parseLong(object.getString("classId"));
		Classroom room = commonService.getClassById(sid,classId,termInfo);
		if (null != room){
			long gradeId = room.getGradeId();
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("gradeId", gradeId + "");
			map.put("schoolId", schoolId);
			map.put("timetableId", timetableId);
			List<JSONObject> list = timetableDao.getGradeSet(map);
		    if (CollectionUtils.isNotEmpty(list)) {
				result.put("totalMaxLesson",
						list.get(0).getIntValue("AMLessonNum")
					+ list.get(0).getIntValue("PMLessonNum"));
			} 
		 }	
		 List<JSONObject> tList = timetableDao.getTimetableByDay(object);
		 if (CollectionUtils.isNotEmpty(tList)){
			 JSONArray tArray = new JSONArray();
			 result.put("timetable", tArray);
			 for(JSONObject temp : tList){
				 String lessonId = temp.getString("courseId");
				 if (lessonMap.containsKey(lessonId)){
					 temp.put("courseName", lessonMap.get(lessonId));
				 }
				 String teacherId = temp.getString("teacherId");
				 if (teacherMap.containsKey(teacherId)){
					 temp.put("teacherName", temp.get("teacherId"));
				 }
				 tArray.add(temp);
			 } 
		}
		return result;
	}

	@Override
	public JSONObject getTeacherTimetableByDay(JSONObject object) {
		School school = (School)object.get("school");
		String termInfo = object.getString("selectedSemester");
		Map<String,String> classMap = getClassesMap(school,termInfo);
		Map<String,String> lessonMap = getLessonsMap(school,termInfo); 
		JSONObject result = new JSONObject();
		List<JSONObject> tList = timetableDao.getTimetableByDay(object);
		if (CollectionUtils.isNotEmpty(tList)){
			 JSONArray tArray = new JSONArray();
			 result.put("timetable", tArray);
			 for(JSONObject temp : tList){
				 String lessonId = temp.getString("courseId");
				 if (lessonMap.containsKey(lessonId)){
					 temp.put("courseName", lessonMap.get(lessonId));
				 }
				 String classId = temp.getString("classId");
				 if (classMap.containsKey(classId)){
					 temp.put("className", temp.get("classId"));
				 }
				 tArray.add(temp);
			 } 
		}
		return result;
	}

	@Override
	public List<JSONObject> getAppClassList(HashMap<String,Object> map) {
		List<JSONObject> rList = new ArrayList<JSONObject>();
		List<Classroom> roomList = commonService.getClassList(map);
		for (Classroom room : roomList) {
			 JSONObject classInfor = new JSONObject();
			 classInfor.put("classId", room.getId() + "") ;
			 classInfor.put("className",room.getClassName());
			 rList.add(classInfor);
		}
		return rList;
	}

		@Override
	public JSONObject getArrangeTimetableInfo(String schoolId, String timetableId) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("schoolId", schoolId);
		map.put("timetableId", timetableId);
		return this.timetableDao.getArrangeTimetableInfo(map);
	}

	@Override
	public List<Classroom> getClassByGradeIds(String schoolId, String termInfoId, List<String> gradeIds) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("schoolId", Long.valueOf(schoolId));
		if(termInfoId!=null && termInfoId!="") {
			map.put("termInfoId", termInfoId);
		}
		map.put("usedGradeId", StringUtils.join(gradeIds, ","));

		return this.commonService.getClassList(map);
	}

	@Override
	public List<JSONObject> getTaskByTimetable(String schoolId, String timetableId, List<String> classIds) {
		//
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("schoolId", schoolId);
		map.put("timetableId", timetableId);
		map.put("classIds", classIds);
		
		return this.timetableDao.getTaskByTimetable(map);
	}
	

	@Override
	public List<JSONObject> getTaskByTimetableClassId(String schoolId, String timetableId, String classId) {
		List<String> classIds = new ArrayList<String>();
		classIds.add(classId);
		
		return this.getTaskByTimetable(schoolId, timetableId, classIds);
	}
	

	@Override
	public List<JSONObject> getTaskByTimetable(String schoolId, String timetableId) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("schoolId", schoolId);
		map.put("timetableId", timetableId);
		
		return this.getTaskByTimetable(schoolId, timetableId, null);
	}
	
	@Override
	public JSONObject getGradeSet(String schoolId, String timetableId, String gradeId) {
		List<String> gradeIds = null;
		if(gradeId!=null){
			gradeIds = new ArrayList<String>();
			gradeIds.add(gradeId);
		}
		List<JSONObject> list = this.getGradeSetList(schoolId, timetableId, gradeIds);
		if(list!=null && list.size()>0){
			return list.get(0);
		}else{
			return null;
		}
	}

	@Override
	public List<JSONObject> getGradeSetList(String schoolId,	String timetableId, List<String> gradeIds) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("schoolId", schoolId);
		map.put("timetableId", timetableId);
		if(gradeIds!=null&&gradeIds.size()>0){
			
			map.put("gradeIds", gradeIds);
		}
		
		return this.timetableDao.getArrangeGradeSetList(map);
	}

	@Override
	public List<JSONObject> getRuleCourseList(String schoolId,	String timetableId) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("schoolId", schoolId);
		map.put("timetableId", timetableId);
		
		return this.timetableDao.getRuleCourseList(map);
	}

	@Override
	public List<JSONObject> getRuleCourseFixedByRuleId(String schoolId, String courseRuleId) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("schoolId", schoolId);
		map.put("courseRuleId", courseRuleId);
		
		return this.timetableDao.getRuleCourseFixedByRuleId(map);
	}

	@Override
	public List<JSONObject> getRuleTeacherList(String schoolId, String timetableId) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("schoolId", schoolId);
		map.put("timetableId", timetableId);
		
		return this.timetableDao.getRuleTeacherList(map);
	}

	@Override
	public List<JSONObject> getRuleTeacherFixedByRuleId(String schoolId, String teacherRuleId) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("schoolId", schoolId);
		map.put("teacherRuleId", teacherRuleId);
		
		return this.timetableDao.getRuleTeacherFixedByRuleId(map);
	}

	@Override
	public List<JSONObject> getRuleClassGroupList(String schoolId, String timetableId) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("schoolId", schoolId);
		map.put("timetableId", timetableId);
		
		return this.timetableDao.getRuleClassGroupList(map);
	}

	@Override
	public List<JSONObject> getRuleClassFixedByRuleId(String schoolId, String classRuleId) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("schoolId", schoolId);
		map.put("classRuleId", classRuleId);
		
		return this.timetableDao.getRuleClassFixedByRuleId(map);
	}

	@Override
	public List<JSONObject> getResearchWorkList(String schoolId,	String timetableId) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("schoolId", schoolId);
		map.put("timetableId", timetableId);
		
		return this.timetableDao.getResearchWorkList(map);
	}

	@Override
	public List<JSONObject> getAdvanceArrangeList(String schoolId, String timetableId) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("schoolId", schoolId);
		map.put("timetableId", timetableId);
		
		return this.timetableDao.getAdvanceArrangeList(map);
	}

	@Override
	public boolean saveTimetableBatch(String schoolId, String timetableId,	List<Map<String, Object>> timeTableList, Set<String> gradeIds) {
		
		return this.timetableDao.saveTimetableBatch(schoolId, timetableId, timeTableList, gradeIds);
	}

	@Override
	public List<JSONObject> getArrangeTimetableList(String schoolId, String timetableId,String classId) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("schoolId", schoolId);
		map.put("timetableId", timetableId);
		map.put("classId", classId);
		
		return this.timetableDao.getArrangeTimetableList(map);
	}

	@Override
	public int addArrangeTimetable(String schoolId, String timetableId,
			String classId, String courseId, int day, int lesson,int courseType, String mcGroupId) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("schoolId", schoolId);
		map.put("timetableId", timetableId);
		map.put("classId", classId);
		map.put("courseId", courseId);
		map.put("day", day);
		map.put("lesson", lesson);
		map.put("courseType", courseType);
		map.put("isAdvance", "0");
		map.put("mcGroupId", mcGroupId);
		
		return this.timetableDao.addArrangeTimetable(map);
	}

	@Override
	public int deleteArrangeTimetable(String schoolId, String timetableId,
			String classId, String[] courseIds, int day, int lesson, String mcGroupId) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("schoolId", schoolId);
		map.put("timetableId", timetableId);
		map.put("classId", classId);
		map.put("courseIds", courseIds);
		map.put("day", day);
		map.put("lesson", lesson);
		if(mcGroupId!=null){
			map.put("mcGroupId", mcGroupId);
		}
		return this.timetableDao.deleteArrangeTimetable(map);
	}

	@Override
	public int clearTimetable(String schoolId, String timetableId,String subsql,String xn,String xq) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("schoolId", schoolId);
		map.put("timetableId", timetableId);
		map.put("subsql", subsql);
		map.put("xn", xn);
		map.put("xq", xq);
		return this.timetableDao.clearArrangeTimetable(map);
	}
	

	@Override
	public List<JSONObject> getTeacherTimetable(HashMap<String, Object> map) {
		return timetableDao.getTeacherTimetable(map);
	}

	@Override
	public List<JSONObject> getMaxLessonNum(HashMap<String, Object> map) {
		return timetableDao.getMaxLessonNum(map);
	}

	@Override
	public List<JSONObject> getClassTimetable(HashMap<String, Object> map) {
		return timetableDao.getClassTimetable(map);
	}

	@Override
	public List<JSONObject> getGradeSet(HashMap<String, Object> map) {
		return timetableDao.getGradeSet(map);
	}

	@Override
	public List<JSONObject> getTimetablePie(HashMap<String, Object> map) {
		return timetableDao.getTimetablePie(map);
	}

	@Override
	public JSONObject getEvenOddWeekGroupList(String schoolId,
			String timetableId) {
		List<JSONObject>  list = timetableDao.getEvenOddWeekGroupList(schoolId,timetableId);
		HashMap<String,String> map1 = new HashMap<String, String>();
		HashMap<String,String> map2 = new HashMap<String, String>();
		JSONObject rs = new JSONObject();
		
		
		for(JSONObject obj:list){
			String classIds = obj.getString("ClassIds");
			String[] classes = classIds.split(",");
			
			int inx = classes.length/2;
			
			
			for(int i=0;i<classes.length;i++){
				
				if(i<=inx){
					String courseId1 = obj.getString("CourseId1");
					String courseId2 = obj.getString("CourseId2");
					String classId = classes[i];
					//单周课
					map1.put(classId+"_"+courseId1, courseId2);
					//双周课
					map2.put(classId+"_"+courseId2, courseId1);
				}else{
					String courseId1 = obj.getString("CourseId1");
					String courseId2 = obj.getString("CourseId2");
					String classId = classes[i];
					//单周课
					map1.put(classId+"_"+courseId2, courseId1);
					//双周课
					map2.put(classId+"_"+courseId1, courseId2);
					
				}
			}
					
			
		}	
		rs.put("map1", map1);
		rs.put("map2", map2);
		return rs;
	}

	@Override
	public List<JSONObject> getTimeByTeacher(HashMap<String, Object> map) {
		return timetableDao.getTimeByTeacher(map);
	}

	@Override
	public List<JSONObject> getByTeacherTime(HashMap<String, Object> map) {
		return timetableDao.getByTeacherTime(map);
	}

	@Override
	public List<JSONObject> getTeacherResearch(HashMap<String, Object> map) {
		return timetableDao.getTeacherResearch(map);
	}

	@Override
	public List<JSONObject> getRuleTeachersInHasArrage(String schoolId,
			String timetableId) {
		return timetableDao.getRuleTeachersInHasArrage(schoolId,timetableId);
	}
	
	@Override
	public JSONObject getTimetablePrintSet(JSONObject object) {
		JSONObject printset = timetableDao.getTimetablePrintSet(object);		
		if (null == printset || printset.size() == 0){		
			printset = new JSONObject();
			String xnxq = object.getString("selectedSemester");
			if (StringUtils.isNotEmpty(xnxq) && xnxq.length() == 5){	
				String schoolYear = xnxq.substring(0, 4);
				String termName = xnxq.substring(4);
				Map<String, String> map = new HashMap<String, String>();
				map.put("schoolYear", schoolYear);
				map.put("termName", termName);
				map.put("schoolId", object.getString("schoolId"));
				map.put("timetableId", object.getString("timetableId"));
				List<JSONObject> tList = timetableDao.getTimetable(map);
				if (tList.size() > 0){
					printset.put("title", tList.get(0).getString("timetableName"));
				}
			}
			printset.put("bottomNote1", "");
			printset.put("bottomNote1", "");
			printset.put("bottomNote2", "");
			printset.put("bottomNote3", "");
			printset.put("printStyle", "01");
		}
		return printset;
	}

	@Override
	public int updateTimetablePrintSet(JSONObject object) {
		return timetableDao.updateTimetablePrintSet(object);
	}

	@Override
	public List<JSONObject> getTimetableGradeList(HashMap<String, Object> reqMap) {
		return timetableDao.getTimetableGradeList(reqMap);
	}

	@Override
	public List<JSONObject> getTimetableSubjectList(
			HashMap<String, Object> reqMap) {
		return timetableDao.getTimetableSubjectList(reqMap);
	}

	@Override
	public List<JSONObject> getTimetableTeacherList(
			HashMap<String, Object> reqMap) {
		return timetableDao.getTimetableTeacherList(reqMap);
	}

	@Override
	public List<String> getTaskClassList(JSONObject object) {
		return timetableDao.getTaskClassList(object);
	}

	@Override
	public JSONObject getTimetableForWeekList(HashMap<String, Object> cxMap) {
		return timetableDao.getTimetableForWeekList(cxMap);
	}

	@Override
	public List<JSONObject> getTemporaryAjustList(HashMap<String, Object> cxMap) {
		return timetableDao.getTemporaryAjustList(cxMap);
	}

	@Override
	public <T> void insertMultiEntity(List<T> dataList) {
		timetableDao.insertMultiEntity(dataList);
	}

	@Override
	public List<JSONObject> getTemporaryAjustRecord(
			HashMap<String, Object> cxMap) {
		return timetableDao.getTemporaryAjustRecord(cxMap);
	}

	@Override
	public void updateAutoArrangeResult(String timetableId, String schoolId,
			String rsMsg) {
		HashMap<String,Object> map = new HashMap<String, Object>();
		map.put("timetableId", timetableId);
		map.put("schoolId", schoolId);
		map.put("rsMsg", rsMsg);		
		timetableDao.updateAutoArrangeResult(map);		
	}

	@SuppressWarnings("unchecked")
	@Override
	public int updateArrangeTimetable(JSONObject needUpdate) {
		int rs = 0;
		int breakRule  = 0;
		if(needUpdate.containsKey("breakRule")){
			breakRule = needUpdate.getIntValue("breakRule");
		}
		if(needUpdate.containsKey("needDelete")){
			List<JSONObject> ni = (List<JSONObject>) needUpdate.get("needDelete");
			for(JSONObject o:ni){
				//
				o.put("breakRule", breakRule);
			}
			rs += this.deleteArrangeTimetable(ni);
		}
		if(needUpdate.containsKey("needInsert")){
			List<JSONObject> ni = (List<JSONObject>) needUpdate.get("needInsert");
			if(breakRule==1){
				for(JSONObject o:ni){
					//
					o.put("isAdvance", 2);
				}
			}
			rs  += this.insertArrangeTimetableBatch(ni);
		}
		return rs;
	}

	@Override
	public int insertArrangeTimetableBatch(List<JSONObject> needInsert) {
		int rs= 0;
		if(needInsert.size()>0){
			rs  += this.timetableDao.addArrangeTimetableBatch(needInsert);
		}
		return rs;
	}

	@Override
	public int deleteArrangeTimetable(List<JSONObject> needDel) {
		int rs = 0;
		if(needDel.size()>0){
			for(JSONObject obj:needDel){
				rs  += 	this.timetableDao.deleteArrangeTimetable(obj);
			}
		}
		return rs;
	}

	@Override
	public List<JSONObject> getDuplicatePubGrades(HashMap<String, Object> cxMap) {
		return timetableDao.getDuplicatePubGrades(cxMap);
	}

	@Override
	public JSONObject getTimetableForAppClass(long schoolId, String xnxq,
			String classId) {
		HashMap<String,String> cxMap = new HashMap<String, String>();
		cxMap.put("schoolId", schoolId+"");
		cxMap.put("xnxq", xnxq);
		cxMap.put("classId", classId);
		return timetableDao.getTimetableForAppClass(cxMap);
	}

	@Override
	public JSONObject getTimetableForAppClassList(long id, String xnxq,
			String teacherId, boolean needClass , String classId) {
		HashMap<String,String> cxMap = new HashMap<String, String>();
		cxMap.put("schoolId", id+"");
		cxMap.put("xnxq", xnxq);
		cxMap.put("teacherId", teacherId);
		cxMap.put("needClass", needClass?"1":"0");
		if (StringUtils.isBlank(teacherId)) {
			cxMap.put("onlyClass", "Y");
			cxMap.put("classId", classId);
		}
		return timetableDao.getTimetableForAppClassList(cxMap);
	}

	@Override
	public List<JSONObject> getRuleGroundList(String schoolId,
			String timetableId) {
		HashMap<String,Object> cxMap = new HashMap<String, Object>();
		cxMap.put("schoolId", schoolId);
		cxMap.put("timetableId", timetableId);
		return timetableDao.getRuleGroundList(cxMap);
	}

	@Override
	public int deleteArrangeTimetableHigh(List<JSONObject> needDelete) {
		return timetableDao.deleteArrangeTimetableHigh(needDelete);
	}

	@Override
	public int updateTimetableCheckStatus(HashMap<String, Object> cxMap) {
		return timetableDao.updateTimetableCheckStatus(cxMap);
	}

	@Override
	public Map<String, List<JSONObject>> getRuleTeacherGroups(String schoolId,
			String timetableId) throws Exception{
		List<JSONObject> teacherGroups = timetableDao.getTeacherGroupRules(schoolId,timetableId);
		List<String> groups = new ArrayList<String>();
		Map<String, List<JSONObject>> tgPos = new HashMap<String, List<JSONObject>>();
		for(JSONObject tg:teacherGroups){
			String TeacherGroupId = tg.getString("TeacherGroupId");
			if(TeacherGroupId==null){
				continue;
			}
			TeacherGroupId = TeacherGroupId.trim();
			if(!groups.contains(TeacherGroupId)){
				groups.add(TeacherGroupId);
			}
			if(tgPos.containsKey(TeacherGroupId)){
				tgPos.get(TeacherGroupId).add(tg);
			}else{
				List<JSONObject> list = new ArrayList<JSONObject>();
				list.add(tg);
				tgPos.put(TeacherGroupId, list);
			}
			
		}
		HashMap<String,Object> cxMap = new HashMap<String, Object>();
		cxMap.put("schoolId", schoolId);
		cxMap.put("timetableId", timetableId);
		if(groups.size()>0){
			cxMap.put("groups", groups);
		}
		List<JSONObject> teacherInGroups = timetableDao.getTeacherRelatedGroups(cxMap);
		Map<String, List<JSONObject>> rs = new HashMap<String, List<JSONObject>>();
		if(teacherGroups.size()>0&&teacherInGroups.size()>0){
			for(JSONObject tig :teacherInGroups){
				String TeacherId = tig.getString("TeacherId");
				String TeacherGroupId = tig.getString("TeacherGroupId");
				if(tgPos.containsKey(TeacherGroupId)){
					if(rs.containsKey(TeacherId)){
						List<JSONObject> list = rs.get(TeacherId);
						for( JSONObject objT:tgPos.get(TeacherGroupId)){
							int tDayOfWeek = objT.getIntValue("DayOfWeek");
							int tLessonOfDay = objT.getIntValue("LessonOfDay");
							int ruleType = objT.getIntValue("RuleType");
							boolean has = false;
							for(JSONObject objN:list){
								int sDayOfWeek = objN.getIntValue("DayOfWeek");
								int sLessonOfDay = objN.getIntValue("LessonOfDay");
								int suleType = objN.getIntValue("RuleType");
								if(tDayOfWeek==sDayOfWeek&&tLessonOfDay==sLessonOfDay&&ruleType==suleType){
									has = true;
									break;
								}
							}
							if(!has){
								list.add(objT);
							}
						}
//						list.addAll(tgPos.get(TeacherGroupId));
					}else{
						List<JSONObject> list = new ArrayList<JSONObject>();
						for(JSONObject obj: tgPos.get(TeacherGroupId)){
							list.add(obj);
						}
						rs.put(TeacherId,list );
					}
				}
			}
		}
		return rs ;
	}

	@Override
	public HashMap<String,TCRCATemporaryAdjustGroupInfo> getTempAjustGroupList(
			HashMap<String, Object> cxMap) {
		// TODO Auto-generated method stub
		HashMap<String,TCRCATemporaryAdjustGroupInfo> rs = new HashMap<String, TCRCATemporaryAdjustGroupInfo>();
		List<TCRCATemporaryAdjustGroupInfo> list = timetableDao.getTempAjustGroupList(cxMap);
		for(TCRCATemporaryAdjustGroupInfo tg:list){
			rs.put(tg.getGroupId(), tg);
		}
		return rs;
	}

	@Override
	public HashMap<String, HashMap<Integer, TCRCATemporaryAdjustGroupParam>> getTempAjustGroupParamMap(
			HashMap<String, Object> cxMap) {
		// TODO Auto-generated method stub
		List<TCRCATemporaryAdjustGroupParam> list = timetableDao.getTempAjustGroupParam(cxMap);
		HashMap<String, HashMap<Integer, TCRCATemporaryAdjustGroupParam>> rs= new HashMap<String, HashMap<Integer,TCRCATemporaryAdjustGroupParam>>();
		for(TCRCATemporaryAdjustGroupParam param:list){
			String gid = param.getGroupId();
			int week = param.getWeek();
			
			HashMap<Integer, TCRCATemporaryAdjustGroupParam> weekMap = new HashMap<Integer, TCRCATemporaryAdjustGroupParam>();
			
			if(rs.containsKey(gid)){
				weekMap = rs.get(gid);
			}
			
			weekMap.put(week, param);
			rs.put(gid, weekMap);
			
		}
		
		return rs;
	}

	@Override
	public TCRCATemporaryAdjustGroupInfo getSingleTempAjustGroup(
			HashMap<String, Object> cxMap) {
		// TODO Auto-generated method stub
		return timetableDao.getSingleTempAjustGroup(cxMap);
	}

	@Override
	public List<TCRCATemporaryAdjustGroupParam> getSingleTempAjustGroupParam(
			HashMap<String, Object> cxMap) {
		// TODO Auto-generated method stub
		return timetableDao.getSingleTempAjustGroupParam(cxMap);
	}

	@Override
	public int addTimetableSchedule(List<JSONObject> object) {
		// TODO Auto-generated method stub
		
		return timetableDao.insertTimetableSchedule(object);
	}

	@Override
	public int updataTimetableSchedule(List<JSONObject> object) {
		// TODO Auto-generated method stub
		return timetableDao.updateTimetableSchedule(object);
	}

	@Override
	public List<HashMap<String,Object>> getTimetableSchedule(String timetableId,String pycc) {
		// TODO Auto-generated method stub
		return timetableDao.queryTimetableSchedule(timetableId,pycc);
	}

	@Override
	public double checkTotalCourse(JSONObject parameter) {
		double sections = 0;
		String courseId = parameter.getString("courseId");
		String weekNum = parameter.getString("weekNum");
		String classId = parameter.getString("classId");
		List<String> list = new ArrayList<String>();
		Collections.addAll(list, classId.split(","));
		parameter.put("classIdList", list);
		JSONObject totalNum = timetableDao.getTotalNum(parameter);
        String course = null;
		if (isNotEmpty(totalNum,"totalNum")) {
			  sections =totalNum.getDouble("totalNum");
		}
		parameter.put("sort", "-a.CourseId DESC");
		List<JSONObject> taskLList = timetableDao.getTeachingTasks(parameter);	
		if (CollectionUtils.isNotEmpty(taskLList)) {
			for (int i = 0; i <taskLList.size(); i++) {
				JSONObject jsonObject = taskLList.get(i);
				if (jsonObject.containsKey("courseInfo")) {
					JSONArray jsArray =jsonObject.getJSONArray("courseInfo"); 
					double sum  = 0;
					boolean exist = false;
					for (int j = 0; j < jsArray.size(); j++) {
						JSONObject obj = jsArray.getJSONObject(j);
						course = obj.getString("courseId");
						if (courseId.contains(course)) {
							 
							sum = sum +Double.parseDouble(weekNum);//set new week day , replace old value
							exist = true;
						}else{
							sum = sum +obj.getDoubleValue("weekNum");
						}
					}
					if (!exist) {
						sum = sum +Double.parseDouble(weekNum);//add new course.
					}
					if ( (sections -sum) < 0 )   {
						return sections -sum;
					}
					
				}
				
			}
		}

		return 0.0;
	}

 
	private HashMap<String, HashMap<String, Double>> getCourseWeekNum(JSONObject parameter) {
		String courseId = parameter.getString("courseId");
		String classId = parameter.getString("classId");
		String course = null;
		List<String> list = new ArrayList<String>();
		Collections.addAll(list, classId.split(","));
		parameter.put("classIdList", list);
		parameter.put("sort", "-a.CourseId DESC");
		List<JSONObject> taskLList = timetableDao.getTeachingTasks(parameter);	
		HashMap<String, HashMap<String, Double>> map = new HashMap<String, HashMap<String, Double>>();
		if (CollectionUtils.isNotEmpty(taskLList)) {
			
			for (int i = 0; i <taskLList.size(); i++) {
				HashMap<String, Double> cntMap = new HashMap<String, Double>();
				JSONObject jsonObject = taskLList.get(i);
				String temClassId = jsonObject.getString("classId");
				if (jsonObject.containsKey("courseInfo")) {
					JSONArray jsArray =jsonObject.getJSONArray("courseInfo"); 
					for (int j = 0; j < jsArray.size(); j++) {
						JSONObject obj = jsArray.getJSONObject(j);
						course = obj.getString("courseId");
						if (courseId.equals(course)) {
							cntMap.put(course,obj.getDoubleValue("weekNum"));
						}
					}
				}
				map.put(temClassId, cntMap);
			}
		}

		return map;
	}

	@Override
	public HashMap<String, CourseSetInfor> checkSingleCourse(JSONObject object) {
		  String classIds = object.getString("classId");
		  String courseId = object.getString("courseId");
		  String schoolId = object.getString("schoolId");
		  String gradeId = object.getString("gradeId");
		  String termInfo = object.getString("selectedSemester");
		  School school = (School)object.get("school");
 
		  HashMap<String,HashMap<String, Double>> map = getCourseWeekNum(object);
		  String classId[] = classIds.split(",");
		  JSONArray jsonArray = object.getJSONArray("rows");
		  int setCnt = jsonArray.size();
		  HashMap<String,CourseSetInfor> map2 = new HashMap<String,CourseSetInfor>();
		 
			 
		  HashMap<String, Object> map3 = new HashMap<String, Object>();
		  map3.put("schoolId", schoolId);
		  map3.put("usedGradeId", gradeId);
		  map3.put("termInfoId", termInfo);
		  List<Classroom> list = commonService.getClassList(map3);
		  HashMap<String,String> classMap = new HashMap<String,String>();
		  for (Classroom room : list){
				 classMap.put(room.getId()+"",room.getClassName());
			}
		 
		  Map<String,String> courseMap = getLessonsMap(school, termInfo);
		  
		  for (int i = 0; i < classId.length; i++) {
			  HashMap<String, Double> cntMap = map.get(classId[i]);
			  double cnt = cntMap.get(courseId);
			  if (setCnt > cnt) {
				  CourseSetInfor infor = new CourseSetInfor();
				  infor.setCourseId(courseId);
				  infor.setCourseName(courseMap.get(courseId));
				  infor.setSysSet(cnt);
				  infor.setUserSet(setCnt); 
				  map2.put(classMap.get(classId[i]), infor);
			  }
		  }
		
		return map2;
	}

	@Override
	public List<JSONObject> getTimetableByTeacherId(JSONObject param) {
		return timetableDao.getTimetableByTeacherId(param);
	}

	@Override
	public List<JSONObject> getLatestPublishedTimetable(JSONObject object) {
		return timetableDao.getLatestTimetable(object);
	}
	
	@Override
	public TimetableGradeSetBO getTimetableGradeSetById(String timetableId) {
		return timetableMapper.getTimetableGradeSetById(timetableId);
	}

	@Override
	public void sendWxTemplateMsg(JSONObject params) {
//		logger.info("sendVenueWxTemplateMsg:"+params.toJSONString());
		try {
			Long schoolId = params.getLong("schoolId");
			String timetableId = params.getString("timetableId");
			String TermInfoId = commonService.getCurTermInfoId(schoolId);
			
			Map<String,Object> param = new HashMap<>();
			param.put("timetableId", timetableId);
			param.put("schoolId", schoolId);
			param.put("termInfoId", TermInfoId);
			
			List<JSONObject> list = timetableDao.getClassTimetableList(param);
			if(list == null || list.isEmpty()) {
				logger.info("getClassTimetableList为空");
				return;
			}
			
			// 获取设置课表
			JSONObject sectionParam = new JSONObject();
			sectionParam.put("timetableId", timetableId);
			sectionParam.put("schoolId", schoolId);
			List<JSONObject> sections = timetableDao.getTimetableSections(sectionParam);
			Map<String,Integer> sectionMap = new HashMap<>();
			if(CollectionUtils.isNotEmpty(sections)) {
				sections.stream().forEach(obj -> sectionMap.put(obj.getString("GradeId"), (obj.getInteger("AMLessonNum")+obj.getInteger("PMLessonNum"))*obj.getIntValue("MaxDaysForWeek")));
			}
			
			// 排除给老师发送重复消息，一个老师多个班级，只发一个班级
			Map<Long, String> classMap = new HashMap<>();
			Map<Long, String> teacherMap = new HashMap<>();
			// 班级一周上课次数
			Map<String,Integer> classCount = new HashMap<>();
			List<Long> gradeIds = new ArrayList<>();
			
			for(JSONObject obj : list) {
				Long teacherId = obj.getLong("TeacherId");
				Long classId = obj.getLong("ClassId");
				Long gradeId = obj.getLong("gradeId");
				
				classMap.put(classId, obj.getString("className"));
				if (!teacherMap.containsKey(teacherId)) {
					teacherMap.put(teacherId, obj.getString("className"));
				}
				
				if(!gradeIds.contains(gradeId))
					gradeIds.add(gradeId);
				
			}
			
			// 获取学校extId
			String termInfo = commonService.getCurTermInfoId(schoolId);
			School schoolObj = commonService.getSchoolById(schoolId,termInfo);
			
			// 将年级ID转成开始学年，将学年对应的课表数保存 
			List<Grade> grades = commonService.getGradeBatch(schoolId, gradeIds, termInfo);
			Map<Long,Integer> gradeSections = new HashMap<>();
			if(CollectionUtils.isNotEmpty(grades)) {
				for(Grade grade : grades) {
					String gradeYear = commonService.ConvertNJDM2SYNJ(grade.getCurrentLevel().getValue()+"", termInfo.substring(0, 4));
					int cnt = sectionMap.get(gradeYear);
					gradeSections.put(grade.getId(), cnt);
				}
			}
			
			// 通过gradeId，和className进行关联
			Map<String,Integer> classSections = new HashMap<>();
			for(JSONObject obj : list) {
				Long gradeId = obj.getLong("gradeId");
				String className = obj.getString("className");
				
				if(!classSections.containsKey(className))
					classSections.put(className, gradeSections.get(gradeId));
			}
			

			List<JSONObject> receivers = new ArrayList<>();
			List<Account> teachers = commonService.getAccountBatch(schoolId, new ArrayList<>(teacherMap.keySet()), termInfo);
			for(Account acc : teachers) {
				JSONObject receiver = new JSONObject();
				receiver.put("userId", acc.getExtId());
				receiver.put("userName", acc.getName());
				receiver.put("className", teacherMap.get(acc.getId()));
				receivers.add(receiver);
			}
			
			
			List<Classroom> classroomList = commonService.getClassroomBatch(schoolId, new ArrayList<>(classMap.keySet()), termInfo);
			List<Long> allAccIds = new ArrayList<Long>();
			Map<Long, String> accId2ClassName = new HashMap<Long, String>();
			for(Classroom classroom : classroomList) {
				if (CollectionUtils.isEmpty(classroom.getStudentAccountIds())){
					continue;
				}
				for (Long stuId : classroom.getStudentAccountIds()){
					allAccIds.add(stuId);
					accId2ClassName.put(stuId, classroom.getClassName());
				}
			}
			
			List<Account> accountList = commonService.getAccountBatch(schoolId, allAccIds, termInfo);
			for(Account acc : accountList){
				JSONObject receiver = new JSONObject();
				receiver.put("userId", acc.getExtId());
				receiver.put("userName", acc.getName());
				receiver.put("className", accId2ClassName.get(acc.getId()));
				receivers.add(receiver);
			}
			
			List<JSONObject> parents= commonService.getSimpleParentByStuMsg(allAccIds, termInfo, schoolId);
			if (parents!=null && parents.size() > 0) {
				for (int i = 0; i < parents.size(); i++) {
					JSONObject parent = parents.get(i);
					Long stdId = parent.getLong("studentAccountId");
					JSONObject msgCenterReceiver = new JSONObject();
					msgCenterReceiver.put("userId", parent.getString("extUserId"));
					msgCenterReceiver.put("userName", parent.getString("name"));
					msgCenterReceiver.put("className", accId2ClassName.get(stdId));
					receivers.add(msgCenterReceiver);
				}
			}
			
			for(JSONObject receiver : receivers) {
				String name = receiver.getString("className");
				receiver.remove("className");
				JSONObject values = new JSONObject();
				values.put("keyword3", name);
				values.put("keyword5", classSections.get(name)+"节/周");
				receiver.put("keyValues", values);
			}
			
			String title = "学校为你推送了一份课表!";
			int year = Integer.parseInt(termInfo.substring(0, 4));
			int term = Integer.parseInt(termInfo.substring(4));
			String termInfoStr = year + "-" + (year + 1) + "学年第" + (term == 1 ? "一" : "二") + "学期";
			
			// 4. 发送消息
			JSONObject msg = new JSONObject();
		    String msgId = UUIDUtil.getUUID().replace("-", "");
			msg.put("msgId", msgId);
			msg.put("msgTitle",title);
			msg.put("msgContent", title);
			msg.put("msgUrlPc", msgUrlPc);
			msg.put("msgUrlApp", msgUrlApp);
			msg.put("msgOrigin", "课表排课提醒");
			msg.put("msgTypeCode", MSG_TYPE_CODE);
			msg.put("msgTemplateType", MSG_TYPE_CODE);
			msg.put("schoolId",schoolObj.getExtId());
			msg.put("creatorName", schoolObj.getName());
		    
			// 标题
			JSONObject first = new JSONObject();
			first.put("value",title);

			// 学校名称
			JSONObject keyword1 = new JSONObject();
			keyword1.put("value", schoolObj.getName());
			
			// 学期
			JSONObject keyword2 = new JSONObject();
			keyword2.put("value", termInfoStr);

			// 班级
			JSONObject keyword3 = new JSONObject();
			keyword3.put("value", "");
			
			// 科目
			JSONObject keyword4 = new JSONObject();
			keyword4.put("value", "语文、数学、英语等");
			
			// 节次
			JSONObject keyword5 = new JSONObject();
			keyword5.put("value", "");

			JSONObject remark = new JSONObject();
			remark.put("value", "请点击查看详细！");

			JSONObject wxData = new JSONObject();
			wxData.put("first", first);
			wxData.put("keyword1", keyword1);
			wxData.put("keyword2", keyword2);
			wxData.put("keyword3", keyword3);
			wxData.put("keyword4", keyword4);
			wxData.put("keyword5", keyword5);
			wxData.put("remark", remark);
			wxData.put("url", msgUrlApp);
			msg.put("msgWxJson", wxData);

			JSONObject msgBody = new JSONObject();
			msgBody.put("msg", msg);
			msgBody.put("receivers", receivers); 
			logger.info("所有接受对象：" + msgBody);
			KafkaUtils.sendAppMsg(kafkaUrl, msgId, msgBody, MSG_TYPE_CODE , clientId, clientSecret);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public JSONObject getTimetableDetailById(HashMap<String, Object> param) {
		return timetableDao.getTimetableDetailById(param);
	}
	
}