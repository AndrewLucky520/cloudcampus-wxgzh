package com.talkweb.timetable.arrangement.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.Account;
import com.talkweb.accountcenter.thrift.Classroom;
import com.talkweb.accountcenter.thrift.Grade;
import com.talkweb.accountcenter.thrift.LessonInfo;
import com.talkweb.common.tools.BeanTool;
import com.talkweb.common.tools.DateUtil;
import com.talkweb.common.tools.GrepUtil;
import com.talkweb.common.tools.SortUtil;
import com.talkweb.common.tools.UUIDUtil;
import com.talkweb.common.tools.sort.Sort;
import com.talkweb.common.tools.sort.SortEnum;
import com.talkweb.scoreManage.action.ScoreUtil;
import com.talkweb.timetable.arrangement.algorithm.RuleConflict;
import com.talkweb.timetable.arrangement.domain.ArrangeCourse;
import com.talkweb.timetable.arrangement.domain.TCRCATemporaryAdjustGroupInfo;
import com.talkweb.timetable.arrangement.domain.TCRCATemporaryAdjustGroupParam;
import com.talkweb.timetable.arrangement.domain.TCRCATemporaryAdjustRecord;
import com.talkweb.timetable.arrangement.domain.TCRCATemporaryAdjustResult;

public class TimetableUtil {
	
	private static final int MaxAjst = 2;

	/**
	 * 根据起始周日期及周次设置各学周
	 * @param dayOfStart
	 * @param weekNum
	 * @return
	 */
	public static JSONObject getWeekListByStartDate(String dayOfStart, int weekNum) {
		
		JSONObject rsobj = new JSONObject();
		int code = 1;
		String msg = "获取成功！";
		// TODO Auto-generated method stub
		List<JSONObject> rs = new ArrayList<JSONObject>();
		if(dayOfStart==null||dayOfStart.trim().length()==0){
			code = -1;
			msg = ("起始日期未设置，请在【排课】-【设置课表】中设置");
			rsobj.put("code", code);
			rsobj.put("msg", msg);
			return rsobj;
		}
		
		Date d = DateUtil.parseDateDayFormat(dayOfStart);
		
		if(d==null){
			code = -1;
			msg = ("起始日期格式不正确，请在【排课】-【设置课表】中重新设置");
			rsobj.put("code", code);
			rsobj.put("msg", msg);
			return rsobj;
		}
		Date startWeekMd = DateUtil.getFirstDayOfLearnWeek(d);
		GregorianCalendar gc = (GregorianCalendar) Calendar.getInstance();
        gc.setTime(startWeekMd);
        String curMd =  DateUtil.getDateDayFormat(DateUtil.getFirstDayOfLearnWeek(new Date()));
		
        boolean find = false;
        for(int i=0;i<weekNum;i++){
			JSONObject obj = new JSONObject();
			String text = "";
			boolean selected = false;
			if(i!=0 ){
				
				gc.add(Calendar.DATE, 1 );
			}
			Date weekMd = gc.getTime();
			gc.add(Calendar.DATE,  6);
			Date weekSun = gc.getTime();
			String stt = DateUtil.getDateFormat(weekMd, "MM-dd ");
			String jd =  DateUtil.getDateDayFormat(weekMd);
			if(jd.equalsIgnoreCase(curMd)){
				selected= true;
			}
			stt = cutFirstZero(stt);
			String edd =  DateUtil.getDateFormat(weekSun, "MM-dd ");
			edd = cutFirstZero(edd);
			text = "第"+(i+1)+"周("+stt+"-"+edd+")";
			obj.put("value", (i+1)+"|"+jd);
			obj.put("text", text);
			obj.put("selected", selected);
			if(selected){
				find = true;
			}
			rs.add(obj);
		}
		if(!find&&rs.size()>0){
			rs.get(rs.size()-1).put("selected", true);
		}
		
		rsobj.put("code", code);
		rsobj.put("msg", msg);
		rsobj.put("rslist", rs);
		
		return rsobj;
	}

	/**
	 * 将日期转换为1-2位月份格式显示学周次
	 * @param stt
	 * @return
	 */
	private static String cutFirstZero(String stt) {
		// TODO Auto-generated method stub	
		if(stt.indexOf("-")!=-1){
			String[] tmp = stt.split("-");
			int t1 = Integer.parseInt(tmp[0]);
			
			stt = t1+"."+tmp[1];
			
			return stt;
		}else{
			return null;
		}
	}

	public static List<ArrangeCourse> sortCourseById(List<ArrangeCourse> courses) {
		// TODO Auto-generated method stub
		for (int i = 0; i < courses.size() - 1; i++) {
			for (int j = 0; j < courses.size() - 1 - i; j++) {
				ArrangeCourse l = courses.get(j);
				ArrangeCourse r = courses.get(j + 1);
				if (Long.parseLong(r.getCourseId()) < Long.parseLong(l
						.getCourseId())) {
					courses.remove(j);
					courses.add(j, r);
					courses.remove(j + 1);
					courses.add(j + 1, l);
				}
			}
		}
		return courses;
	}

	/**
	 * 获取调动记录
	 * 
	 * @param ruleConflict
	 * @param week
	 * @param weekOfEnd
	 * @param tempAjustList
	 * @param classId2
	 * @return
	 */
	public static List<JSONObject> getAdjustedListByAjustResutl(
			RuleConflict ruleConflict, int week, int weekOfEnd,
			List<JSONObject> tempAjustList) {
		// TODO Auto-generated method stub
		HashMap<String, JSONObject> ajustMap = new HashMap<String, JSONObject>();

		for (JSONObject tp : tempAjustList) {
			int DayOfWeek = tp.getIntValue("DayOfWeek");
			String classId = tp.getString("ClassId");

			int weekNow = tp.getIntValue("Week");
			if (weekNow != week ) {
				continue;
			}

			Classroom clas = ruleConflict.getClassroomsDic().get(classId);
			String gradeId = ruleConflict.getGradeIdSynjMap().get(
					clas.getGradeId());
			if (!ajustMap.containsKey("1_" + classId)) {
				JSONObject obj = new JSONObject();
				obj.put("adjustedId", classId);
				obj.put("adjustedName", clas.getClassName());
				obj.put("gradeId", gradeId);
				obj.put("type", 1);
				ajustMap.put("1_" + classId, obj);
			}
			String Teachers = tp.getString("Teachers");
			if (Teachers != null && Teachers.trim().length() > 0) {

				String[] teas = Teachers.split(",");
				for (int i = 0; i < teas.length; i++) {
					if (teas[i] != null && teas[i].trim().length() > 0) {
						String key = teas[i].trim();
						if (!ajustMap.containsKey("2_" + key)) {
							JSONObject obj = new JSONObject();
							obj.put("adjustedId", key);
							obj.put("adjustedName", ruleConflict
									.getTeachersDic().get(key).getName());
							obj.put("gradeId", gradeId);
							obj.put("type", 2);
							ajustMap.put("2_" + key, obj);
						}
					}
				}
			}
		}
		List<JSONObject> rsList = new ArrayList<JSONObject>();
		if(ajustMap.values().size()>0){
			for(Iterator<String> it = ajustMap.keySet().iterator();it.hasNext();){
				String key = it.next();
				rsList.add(ajustMap.get(key));
			}
		}
		ScoreUtil.sorStuScoreList(rsList, "adjustedId", "asc", "","");
		ScoreUtil.sorStuScoreList(rsList, "type", "asc", "","");
		return  rsList;
	}

	/**
	 * 根据调课结果和周次生成单周课表
	 * 
	 * @param arrangeMap
	 * @param tempAjustList
	 * @param week
	 * @param classId
	 * @return
	 */
	public static HashMap<String, List<JSONObject>> getSingleWeekTimeTableByParam(
			HashMap<String, List<JSONObject>> arrangeMap,
			List<JSONObject> tempAjustList, int week, String classId,RuleConflict ruleConfilict) {
		// TODO Auto-generated method stub
		HashMap<String, List<JSONObject>> arrangeMapClone = 
				copyMapToMap(arrangeMap,week);
		List<JSONObject> temp = new ArrayList<JSONObject>();
		if (classId != null) {
			temp = GrepUtil.grepJsonKeyByVal(
					new String[] { "ClassId", "Week" }, new String[] { classId,
							week + "" }, tempAjustList);
		} else {
			temp = GrepUtil.grepJsonKeyBySingleVal("Week", week + "",
					tempAjustList);
		}
		//标记某班级-目标位置课眼是否覆盖过
		HashMap<String,Boolean> hasRemovedPos = new HashMap<String, Boolean>();
		for (JSONObject tp : temp) {
			int DayOfWeek = tp.getIntValue("DayOfWeek");
			int LessonOfDay = tp.getIntValue("LessonOfDay");
			String AdjustType = tp.getString("AdjustType");
			String SubjectId = tp.getString("SubjectId");
			String FromSubjectId = tp.getString("FromSubjectId");
			String toTeachers = tp.getString("Teachers");
			int FromDayOfWeek = tp.getIntValue("FromDayOfWeek");
			int FromLessonOfDay = tp.getIntValue("FromLessonOfDay");
			String _classId = tp.getString("ClassId");
			String WeekOfEnd = tp.getString("WeekOfEnd");
			String groupId = tp.getString("GroupId");
			
			String tkey = DayOfWeek + "," + LessonOfDay;
			String hasKey = tkey + "," + _classId;
			
			List<JSONObject> tList = arrangeMapClone.get(tkey);
			if(tList==null){
				tkey = tkey +","+_classId;
				tList = arrangeMapClone.get(tkey);
			}
			boolean needRemoveSrc  = false;
			if(tList==null){
				tList = new ArrayList<JSONObject>();
				arrangeMapClone.put(tkey, tList);
			}
			JSONObject tSub = new JSONObject();
			if(groupId!=null&&groupId.equals("-100")){
				
				tSub.put("IsAdvancePlus", 1);
				tSub.put("IsAdvance", 1);
			}
			tSub.put("AdjustType", AdjustType);
			String[] asts = AdjustType.split(",");
			tSub.put("LessonOfDay", LessonOfDay);
			tSub.put("DayOfWeek", DayOfWeek);
			tSub.put("lessonOfDay", LessonOfDay);
			tSub.put("dayOfWeek", DayOfWeek);
			//代课
			tSub.put("teachers", changeTeaToMap(toTeachers,ruleConfilict));
			tSub.put("Teachers", toTeachers);
			tSub.put("CourseId", SubjectId);
			tSub.put("WeekOfEnd", WeekOfEnd);
			tSub.put("weekOfEnd", WeekOfEnd);
			tSub.put("courseId", SubjectId);
			tSub.put("ClassId", _classId);
			tSub.put("classId", _classId);
			tSub.put("groupId", groupId);
			tSub.put("GroupId", groupId);
			if(tp.get("CourseType")==null){
				
				tSub.put("CourseType", 0);
			}
			tSub.put("CourseType", tp.get("CourseType"));
			tSub.put("McGroupId", tp.get("McGroupId"));
			LessonInfo course = ruleConfilict.getCoursesDic().get(SubjectId);
			
			if(course==null ){
				continue;
			}
			tSub.put("courseName",course.getName());
			tSub.put("courseSimpleName", course.getSimpleName()!= null ? course
					.getSimpleName() : course.getName()
					.substring(0, 1));
			if(isStrInArray("3", asts)){
				tSub.put("teachers",new HashMap<String,String>());
			}
			List<JSONObject> tmpList = tList;
			if(_classId!=null){
				
				tmpList = GrepUtil.grepJsonKeyBySingleVal("ClassId", _classId, tList); 
				if(tmpList.size()==0){
					needRemoveSrc = true;
				}
			}
			if(hasRemovedPos.isEmpty()||!hasRemovedPos.containsKey(hasKey)){
				hasRemovedPos.put(hasKey, true);
				tList.removeAll(tmpList);
				
				if(needRemoveSrc){
					String fkey = FromDayOfWeek + "," + FromLessonOfDay;
					List<JSONObject> flist = arrangeMapClone.get(fkey);
					if(flist==null ){
						flist = arrangeMapClone.get(fkey+","+_classId);
					}
					if(flist!=null){
						
						List<JSONObject> ftmp = GrepUtil.grepJsonKeyByVal(new String[]{"ClassId","CourseId"},
								new String[]{_classId,FromSubjectId}, flist);
						flist.removeAll(ftmp);
					}
				}
			}
			tList.add(tSub);
			arrangeMapClone.put(tkey, tList);
			
		}
		return arrangeMapClone;
	}

	

	private static Map<String,String> changeTeaToMap(String toTeachers,
			RuleConflict ruleConfilict) {
		// TODO Auto-generated method stubMap<String,String> 
		
		Map<String,String> map = new HashMap<String, String>();
		if(toTeachers!=null&&toTeachers.trim().length()>0){
			
			String[] teas = toTeachers.split(",");
			for(int i=0;i<teas.length;i++){
				String teaId = teas[i];
				if(teaId!=null&&!teaId.equalsIgnoreCase("null")&&teaId.length()>0){
					String tName = "[已删除]";
					if(ruleConfilict.getTeachersDic().containsKey(teaId)){
						tName = ruleConfilict.getTeachersDic().get(teaId).getName();
					}
					map.put(teaId, tName);
				}
			}
		}
		return map;
	}

	/**
	 * 拷贝课表
	 * @param arrangeMap
	 * @return
	 */
	private static HashMap<String, List<JSONObject>> copyMapToMap(
			HashMap<String, List<JSONObject>> arrangeMap,int week) {
		// TODO Auto-generated method stub
		HashMap<String, List<JSONObject>> clone = new HashMap<String, List<JSONObject>>();
		
		for(Iterator<String> it = arrangeMap.keySet().iterator();it.hasNext();){
			String key = it.next();
			
			List<JSONObject> list = arrangeMap.get(key);
			List<JSONObject> cloneList  = new ArrayList<JSONObject>();
			
			
			for(JSONObject obj:list){
				cloneList.add(obj);
			}
			List<JSONObject> needRemoveLesson = new ArrayList<JSONObject>();
			if(week%2==0){
				//双周则去除单周课
				needRemoveLesson = GrepUtil.grepJsonKeyBySingleVal("CourseType", "1", cloneList);
			}else{
				//单周则去除双周课
				needRemoveLesson = GrepUtil.grepJsonKeyBySingleVal("CourseType", "2", cloneList);
			}
			cloneList.removeAll(needRemoveLesson);
			clone.put(key, cloneList);
		}
		return clone;
	}

	/**
	 * 根据年级id和年级字典获取使用年级
	 * 
	 * @param gradeId
	 * @param gradeDic
	 * @return
	 */
	public static String getSynjByGradeIdMap(long gradeId,
			Map<String, Grade> gradeDic) {
		// TODO Auto-generated method stub
		for (Iterator<String> it = gradeDic.keySet().iterator(); it.hasNext();) {
			String key = it.next();
			Grade gd = gradeDic.get(key);
			if (gd.getId() == gradeId) {
				return key;
			}
		}
		return null;
	}

	/**
	 * 获取周次日期字符串
	 * 
	 * @param weekDate
	 * @param maxDaysForWeek
	 * @return
	 */
	public static String[] getWeekDatesByStart(String weekDate,
			int maxDaysForWeek) {
		// TODO Auto-generated method stub
		Date d = DateUtil.parseDateDayFormat(weekDate);
		String[] rs = new String[maxDaysForWeek];
		GregorianCalendar gc = (GregorianCalendar) Calendar.getInstance();
		gc.setTime(d);
		for (int i = 0; i < maxDaysForWeek; i++) {
			if (i != 0) {

				gc.add(Calendar.DATE, 1);
			}

			rs[i] = DateUtil.getDateDayFormat(gc.getTime());
		}
		return rs;
	}

	public static void main(String[] args) {
//		String[] rs = getWeekDatesByStart("2015-10-12", 5);
//		System.out.println();
		
//		HashMap<String, List<JSONObject>> clone = new HashMap<String, List<JSONObject>>();
//		JSONObject r1 = new JSONObject();
//		r1.put("key1","key1");
//		List<JSONObject> list1 = new ArrayList<JSONObject>();
//		list1.add(r1);
//		
//		clone.put("1",list1);
//		
//		HashMap<String, List<JSONObject>> c2 = copyMapToMap(clone,1);
//		JSONObject r2 = new JSONObject();
//		r2.put("key2","key2");
//		List<JSONObject> list2 = c2.get("1");
//		list2.add(r2);
//		System.out.println("fsd");
		
		System.out.println(filterUnNumber("高二C1314"));
		
	}

	/**
	 * 获取单周教师课表
	 * 
	 * @param ruleConflict
	 * @param curWeekTb
	 * @return
	 */
	public static List<JSONObject> getTeacherTimetable(
			RuleConflict ruleConflict,
			HashMap<String, List<JSONObject>> curWeekTbAllNj) {
		// TODO Auto-generated method stub
		HashMap<String, List<JSONObject>> teaMap = new HashMap<String, List<JSONObject>>();

		for (Iterator<String> it = curWeekTbAllNj.keySet().iterator(); it
				.hasNext();) {
			String key = it.next();
			List<JSONObject> list = curWeekTbAllNj.get(key);
			for (JSONObject les : list) {

				String courseId = les.getString("CourseId");
				les.put("courseId", courseId);
				LessonInfo course = ruleConflict.getCoursesDic()
						.get(courseId);
				if(course==null){
					continue;
				}
				les.put("courseName", course.getName());
				les.put("courseSimpleName", course.getSimpleName()!= null ?
						course.getSimpleName() : course.getName().substring(0, 1));
				String classId = les.getString("ClassId");
				les.put("classId", classId);
				Classroom clas = ruleConflict.getClassroomsDic().get(classId);
				if(clas==null){
					continue;
				}
				String gradeId = ruleConflict.getGradeIdSynjMap().get(
						clas.getGradeId());
				les.put("className", clas.getClassName());
				les.put("usedGrade", gradeId);
				les.put("dayOfWeek", les.get("DayOfWeek"));
				les.put("lessonOfDay", les.get("LessonOfDay"));
				les.put("courseType", les.get("CourseType"));

				String teachers = les.getString("Teachers");
				if (teachers != null && teachers.trim().length() > 0) {

					String[] teas = teachers.split(",");

					for (int i = 0; i < teas.length; i++) {
						JSONObject cles = new JSONObject();
						try {
							cles = BeanTool.castBeanToFirstLowerKey(les);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						String teaid = teas[i];
						if (teaMap.containsKey(teaid)) {
							List<JSONObject> tList = teaMap.get(teaid);
							tList.add(cles);
						} else {
							List<JSONObject> tList = new ArrayList<JSONObject>();
							tList.add(cles);
							teaMap.put(teaid, tList);
						}
					}
				}

			}
		}
		List<JSONObject> rs = new ArrayList<JSONObject>();
		for (Iterator<String> tea = teaMap.keySet().iterator(); tea.hasNext();) {
			String teaId = tea.next();
			JSONObject obj = new JSONObject();
			obj.put("teacherId", teaId);
			if(ruleConflict.getTeachersDic().get(teaId)!=null){
				
				obj.put("teacherName", ruleConflict.getTeachersDic().get(teaId)
						.getName());
			}else{
				obj.put("teacherName","[已删除]");
			}
			obj.put("timetable", teaMap.get(teaId));

			rs.add(obj);
		}
		return rs;
	}

	/**
	 * 获取临时调课课表
	 * @param ruleConflict
	 * @param classId
	 * @param curWeekTb	当前课表
	 * @param arrangeMap	所有课表的映射 
	 * @param maxDaysForWeek
	 * @param maxLessonForDay
	 * @param breakRule 
	 * @return
	 */
	public static List<JSONObject> getAjustTimeTableByClassWithConflict(
			RuleConflict ruleConflict, String classId,
			HashMap<String, List<JSONObject>> curWeekTb,
			HashMap<String, List<JSONObject>> arrangeMap, int maxDaysForWeek,
			int maxLessonForDay, int breakRule) {
		
		List<JSONObject> timetableList = new ArrayList<JSONObject>();
		// TODO Auto-generated method stub
		// 已排课表中计算冲突
		Classroom classroom = ruleConflict.getClassroomsDic().get(classId);
		List<JSONObject> arrangeList = changeMapListToList(curWeekTb);
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

			JSONObject timetable = new JSONObject();
			timetable.put("courseId", courseId);
			timetable.put("isAdvance", arrange.get("IsAdvance"));
			timetable.put("dayOfWeek", day);
			timetable.put("lessonOfDay", lesson);
			timetable.put("classId", _classId);
			timetable.put("courseType", arrange.get("CourseType"));
			timetable.put("AdjustType",changeAjstType(arrange.getString("AdjustType")));
			
			timetable.put("McGroupId", arrange.get("McGroupId"));
			timetable.put("groupId", arrange.get("GroupId"));
			timetable.put("GroupId", arrange.get("GroupId"));
			timetable.put("WeekOfEnd", arrange.get("WeekOfEnd"));
			
			LessonInfo courseInfo = ruleConflict.findCourseInfo(courseId);
			if (courseInfo != null) {
				// 课程名称
				timetable.put("courseName", courseInfo.getName());
				timetable.put(
						"courseSimpleName",
						courseInfo.getSimpleName() != null ? courseInfo
								.getSimpleName() : courseInfo.getName()
								.substring(0, 1));
			}

			//
			Map<String, String> teachers = (Map<String, String>) arrange
					.get("teachers");
			timetable.put("teachers", convertTeacherToList(teachers));

			Grade ddd = ruleConflict.getGradeIdGradeDic().get(
					classroom.getGradeId());
			String gradeLev = ddd.getCurrentLevel().getValue() + "";
			long d1 = new Date().getTime();
			
			List<JSONObject> conflicts = new ArrayList<JSONObject>();
			if(breakRule==0){
				conflicts =  getConflicts(classId, courseId,
								teachers, maxDaysForWeek, maxLessonForDay, arrangeMap,
								ruleConflict, gradeLev, -100,
								arrange.getIntValue("IsAdvance"), timetable.getString("McGroupId"), courseType,arrange.getIntValue("DayOfWeek") );
			}else{
				conflicts =  getOnlyTeaConflicts(classId, courseId,
								teachers, maxDaysForWeek, maxLessonForDay, arrangeMap,
								ruleConflict, gradeLev, -100,
								arrange.getIntValue("IsAdvance"), timetable.getString("McGroupId"), 
								courseType,arrange.getIntValue("DayOfWeek") );
				
			}
			if(teachers==null||teachers.isEmpty()
					||(arrange.getString("McGroupId")!=null&&arrange.getString("McGroupId").trim().length()>0)){
				
				if(breakRule==0){
					
					conflicts = TimetableUtil.getConflictsWithSwap(classId,
							courseId, teachers, maxDaysForWeek, maxLessonForDay,
							arrangeMap, ruleConflict, gradeLev, -100,
							arrange.getIntValue("IsAdvance"),day, lesson, 
							arrange.getString("McGroupId"), false, courseType,arrange.getIntValue("DayOfWeek"),false );
				}else{
					
					conflicts = TimetableUtil.getOnlyTeaConflictsWithSwap(classId,  
							courseId, teachers, maxDaysForWeek, maxLessonForDay,
							arrangeMap, ruleConflict, gradeLev, -100,
							arrange.getIntValue("IsAdvance"),day, lesson, 
							arrange.getString("McGroupId"), false, courseType,arrange.getIntValue("DayOfWeek"),false );
				}
				
			}
			long d2 = new Date().getTime();

//			System.out.println("【课表临时调课】计算冲突二耗时：" + (d2 - d1));
			timetable.put("conflicts", conflicts);
			
			timetableList.add(timetable);
		}

		return timetableList;
	}
	private static String changeAjstType(String string) {
		// TODO Auto-generated method stub
		String rs = "";
		if(string!=null&&string.trim().length()>0){
			String[] arr1 = string.split(",");
			String[] arr2 = array_unique(arr1);
			for(int i=0;i<arr2.length;i++){
				if(arr2[i].trim().length()>0&&!arr2[i].trim().equals("null")){
					
					rs = rs+arr2[i]+",";
				}
			}
			if(rs.trim().length()>0){
				rs = rs.substring(0,rs.length()-1);
			}
		}
		return rs;
	}

//	/**
//	 * 获取临时调课课表
//	 * @param ruleConflict
//	 * @param classId
//	 * @param curWeekTb	当前课表
//	 * @param arrangeMap	所有课表的映射 
//	 * @param maxDaysForWeek
//	 * @param maxLessonForDay
//	 * @return
//	 */
//	@SuppressWarnings("unchecked")
//	public static List<JSONObject> getAjustTimeTableByTeacher(
//			RuleConflict ruleConflict, String teacherId,
//			HashMap<String, List<JSONObject>> curWeekTb,
//			HashMap<String, List<JSONObject>> arrangeMap, int maxDaysForWeek,
//			int maxLessonForDay) {
//		
//		List<JSONObject> timetableList = new ArrayList<JSONObject>();
//		// TODO Auto-generated method stub
//		// 已排课表中计算冲突
//		Account teacher = ruleConflict.getTeachersDic().get(teacherId);
//		List<JSONObject> arrangeList = changeMapListToList(curWeekTb);
//		for (JSONObject arrange : arrangeList) {
//			String _classId = arrange.getString("ClassId");
////			if (!classId.equals(_classId)) {
////				// 不是本班的课表，则跳过
////				continue;
////			}
//			
//			String courseId = arrange.getString("CourseId");
//			int day = arrange.getIntValue("DayOfWeek");
//			int lesson = arrange.getIntValue("LessonOfDay");
//			// 0：正课，1：单周课，2：双周课
//			int courseType = arrange.getIntValue("CourseType");
//			
//			JSONObject timetable = new JSONObject();
//			timetable.put("courseId", courseId);
//			timetable.put("IsAdvance", arrange.get("IsAdvance"));
//			LessonInfo courseInfo = ruleConflict.findCourseInfo(courseId);
//			if (courseInfo != null) {
//				// 课程名称
//				timetable.put("courseName", courseInfo.getName());
//				timetable.put(
//						"courseSimpleName",
//						courseInfo.getSimpleName() != null ? courseInfo
//								.getSimpleName() : courseInfo.getName()
//								.substring(0, 1));
//			}
//			
//			//
//			Map<String, String> teachers = (Map<String, String>) arrange
//					.get("teachers");
//			timetable.put("teachers", convertTeacherToList(teachers));
//			
//			Classroom classroom = ruleConflict.getClassroomsDic().get(_classId);
//			Grade ddd = ruleConflict.getGradeIdGradeDic().get(
//					classroom .getGradeId());
//			String gradeLev = ddd.getCurrentLevel().getValue() + "";
//			
//			long d1 = new Date().getTime();
//			List<JSONObject> conflicts = getConflicts(_classId, courseId,
//					teachers, maxDaysForWeek, maxLessonForDay, arrangeMap,
//					ruleConflict, gradeLev, -100,
//					arrange.getIntValue("IsAdvance"), timetable.getString("McGroupId"));
//			
//			long d2 = new Date().getTime();
//			
//			System.out.println("【课表临时调课-教师课表】计算冲突二耗时：" + (d2 - d1));
//			timetable.put("conflicts", conflicts);
//			
//			timetableList.add(timetable);
//		}
//		
//		return timetableList;
//	}

	/**
	 * 教师对象转换
	 * 
	 * @param teachers
	 * @return
	 */
	public static List<JSONObject> convertTeacherToList(
			Map<String, String> teachers) {
		List<JSONObject> teacherList = new ArrayList<JSONObject>();
		if (teachers == null) {
			return teacherList;
		}
		for (String teacherId : teachers.keySet()) {
			JSONObject teacher = new JSONObject();
			teacher.put("teacherId", teacherId);
			if(teachers.get(teacherId)!=null&&teachers.get(teacherId).trim().length()>0){
				
				teacher.put("teacherName", teachers.get(teacherId));
			}else{
				teacher.put("teacherName", "[已删除]");
				
			}
			teacherList.add(teacher);
		}

		return teacherList;
	}

	/**
	 * 取课程冲突
	 * 
	 * @param classId
	 * @param courseId
	 * @param teachers
	 * @param maxDays
	 * @param maxLesson
	 * @param arrangeMap
	 * @param ruleConflict
	 * @param gradeLev
	 * @param unLessonCount
	 * @param IsAdvance
	 * @param mcGroupId 
	 * @param arrangeMap2 
	 * @return
	 */
	public static List<JSONObject> getConflicts(String classId,
			String courseId, Map<String, String> teachers, int maxDays,
			int maxLesson, HashMap<String, List<JSONObject>> arrangeMap,
			RuleConflict ruleConflict, String gradeLev, double unLessonCount,
			int IsAdvance, String mcGroupId,int courseType ,int srcDay) {
		List<JSONObject> conflicts = new ArrayList<JSONObject>();

		for (int i = 0; i < maxDays; i++) {
			// 固排课与其它冲突
			if (IsAdvance == 1) {
				for (int j = 0; j < maxLesson + 1; j++) {
					JSONObject conflict = new JSONObject();
					conflict.put("dayOfWeek", i);
					conflict.put("lessonOfDay", j);
					// -1，不可调；1，教师冲突
					int status = -1;
					conflict.put("Status", status);
					conflict.put("confType", "固排");
					conflicts.add(conflict);
				}
				continue;
			}
			for (int j = 0; j < maxLesson+1; j++) {
				int canArrangeCourse = ruleConflict.canArrangeCourse(courseId,
						i, j, gradeLev,classId,arrangeMap );
				
				List<String> rClassIds = new ArrayList<String>();
				if(mcGroupId!=null){
					rClassIds = ruleConflict.getMergeClassByGroupId(mcGroupId);
				}
				if(rClassIds.size()==0){
					rClassIds.add(classId);
				}
				boolean hasCourseByAdvance = false;
				JSONObject canSd = canArrageSdWeek(arrangeMap, i, j,
						unLessonCount, rClassIds,classId,courseId);

				boolean sdWeek = canSd.getBooleanValue("b");
				double tl = 0;
				int cType = courseType;
				if(unLessonCount!=-100&&!sdWeek){
					tl = canSd.getDoubleValue("tl");
					if(tl==0.5){
						cType = 2;
					}else{
						cType =1;
					}
				}
				int canArrangeTeacher = ruleConflict.canArrangeTeacher(
						teachers.keySet(), i, j, srcDay,arrangeMap, cType);

				boolean hasCourseByTeacher = hasCourseByTeacher(arrangeMap,
						teachers.keySet(), i, j, rClassIds, courseId,cType);
				long gradeId = ruleConflict.getGradeByLevel(Integer.parseInt(gradeLev)).getId();
				String usedGradeId = ruleConflict.getGradeIdSynjMap().get(gradeId);
				boolean canArrangeGround =  ruleConflict.canArrangeGround(usedGradeId , courseId, i, j,srcDay, cType);
				if (canArrangeCourse == 0 || canArrangeTeacher == 0
						|| hasCourseByTeacher || hasCourseByAdvance || sdWeek || !canArrangeGround) {
					JSONObject conflict = new JSONObject();
					conflict.put("dayOfWeek", i);
					conflict.put("lessonOfDay", j);
					// -1，不可调；1，教师冲突
					int status = -1;
					if (hasCourseByTeacher) {
						status = 1;
					}
					conflict.put("Status", status);	
					conflict.put("confType", "2冲突2");
					conflicts.add(conflict);
				}

			}

		}

		return conflicts;
	}
	
	/**
	 * 取课程冲突
	 * 
	 * @param classId
	 * @param courseId
	 * @param teachers
	 * @param maxDays
	 * @param maxLesson
	 * @param arrangeMap
	 * @param ruleConflict
	 * @param gradeLev
	 * @param unLessonCount
	 * @param IsAdvance
	 * @param mcGroupId 
	 * @param arrangeMap2 
	 * @return
	 */
	public static List<JSONObject> getOnlyTeaConflicts(String classId,
			String courseId, Map<String, String> teachers, int maxDays,
			int maxLesson, HashMap<String, List<JSONObject>> arrangeMap,
			RuleConflict ruleConflict, String gradeLev, double unLessonCount,
			int IsAdvance, String mcGroupId,int courseType ,int srcDay) {
		List<JSONObject> conflicts = new ArrayList<JSONObject>();

		for (int i = 0; i < maxDays; i++) {
			for (int j = 0; j < maxLesson+1; j++) {

				List<String> rClassIds = new ArrayList<String>();
				if(mcGroupId!=null){
					rClassIds = ruleConflict.getMergeClassByGroupId(mcGroupId);
				}
				if(rClassIds.size()==0){
					rClassIds.add(classId);
				}
				// boolean hasCourseByAdvance = this.hasCourseByAdvance(
				// arrangeList, i, j);
				JSONObject canSd = canArrageSdWeek(arrangeMap, i, j,
						unLessonCount, rClassIds,classId,courseId);

				boolean sdWeek = canSd.getBooleanValue("b");
				double tl = 0;
				int cType = courseType;
				if(unLessonCount!=-100&&!sdWeek){
					tl = canSd.getDoubleValue("tl");
					if(tl==0.5){
						cType = 2;
					}else{
						cType =1;
					}
				}
				boolean hasCourseByTeacher = hasCourseByTeacher(arrangeMap,
						teachers.keySet(), i, j, rClassIds, courseId,cType);
				if ( hasCourseByTeacher || sdWeek ) {
					JSONObject conflict = new JSONObject();
					conflict.put("dayOfWeek", i);
					conflict.put("lessonOfDay", j);
					// -1，不可调；1，教师冲突
					int status = -1;
					if (hasCourseByTeacher) {
						status = 1;
					}
					conflict.put("Status", status);	
					conflict.put("confType", "2冲突2");
					conflicts.add(conflict);
				}

			}

		}

		return conflicts;
	}
	/**
	 * 取课程冲突
	 * 
	 * @param classId
	 * @param courseId
	 * @param teachers
	 * @param maxDays
	 * @param maxLesson
	 * @param arrangeMap
	 * @param ruleConflict
	 * @param gradeLev
	 * @param unLessonCount
	 * @param IsAdvance
	 * @param lessonOfDay 要判断课程的位置
	 * @param dayOfWeek 
	 * @param forTempAjst 
	 * @param srcTeacherIds 
	 * @return
	 */
	public static List<JSONObject> getConflictsWithSwapSub(String classId,
			String courseId, Map<String, String> teachers, int maxDays,
			int maxLesson, HashMap<String, List<JSONObject>> arrangeMap,
			RuleConflict ruleConflict, String gradeLev, double unLessonCount,
			int IsAdvance, int dayOfWeek, int lessonOfDay,String mcGroupId, 
			boolean forTempAjst,int courseType,int srcDay,boolean isForTeacher ) {
		List<JSONObject> conflicts = new ArrayList<JSONObject>();

		for (int i = 0; i < maxDays; i++) {
			// 固排课与其它冲突
			if (IsAdvance == 1) {
				for (int j = 0; j < maxLesson + 1; j++) {
					JSONObject conflict = new JSONObject();
					conflict.put("dayOfWeek", i);
					conflict.put("lessonOfDay", j);
					// -1，不可调；1，教师冲突
					int status = -1;
					conflict.put("Status", status);
					conflict.put("confType", "固排");
					conflicts.add(conflict);
				}
				continue;
			}
			for (int j = 0; j < maxLesson+1; j++) {
				
				boolean canArrangeTarget = true;
				
				List<JSONObject> targetLessons = arrangeMap.get(i+","+j);
				if(targetLessons==null){
					targetLessons = arrangeMap.get(i+","+j+","+classId);
				}
				if(targetLessons!=null&&targetLessons.size()>0){
					for(JSONObject tar:targetLessons){
						String tarClassId = tar.getString("ClassId");
						if(tarClassId.equalsIgnoreCase(classId)){
							String tarcourseId = tar.getString("CourseId");
							
							Map<String,String> tarTeachers = (Map<String, String>) tar.get("teachers");
							String tarMcGroupId = tar.getString("McGroupId");
							//放开临时按教师调课-合班课不允许调课的限制
//							if((forTempAjst&&tarMcGroupId!=null&&tarMcGroupId.length()>0)
//									||(forTempAjst&&mcGroupId!=null&&mcGroupId.length()>0)){
//								canArrangeTarget = false;
//								break;
//							}else{
								List<JSONObject> tarconfs = getConflictsByPoint(classId, tarcourseId, tarTeachers.keySet(), 
										dayOfWeek, lessonOfDay, arrangeMap, ruleConflict, gradeLev, unLessonCount,
										tar.getIntValue("IsAdvance"), tarMcGroupId, tar.getIntValue("CourseType"),tar.getIntValue("DayOfWeek"),teachers.keySet());
								if(tarconfs.size()>0){
									canArrangeTarget = false;
									break;
								}
//							}
						}
					}
				}
				int canArrangeCourse = ruleConflict.canArrangeCourse(courseId,
						i, j, gradeLev, classId, arrangeMap);
				int canArrangeTeacher = ruleConflict.canArrangeTeacher(
						teachers.keySet(), i, j,srcDay, arrangeMap, courseType);

				
				List<String> rClassIds = new ArrayList<String>();
				if(mcGroupId!=null){
					rClassIds = ruleConflict.getMergeClassByGroupId(mcGroupId);
				}
				if(rClassIds.size()==0){
					rClassIds.add(classId);
				}
				
				boolean tarCanNot = false;
				//取目标位置课程
				targetLessons = arrangeMap.get(i+","+j);
				for(String cid:rClassIds){
					if(targetLessons==null){
						targetLessons = arrangeMap.get(i+","+j+","+classId);
					}
					if(targetLessons==null){
						continue;
					}
					for(JSONObject li:targetLessons){
						if(cid.equalsIgnoreCase(li.getString("ClassId"))){
							if(li.containsKey("McGroupId")){
								if(rClassIds.size()>1&&mcGroupId!=null&&mcGroupId.trim().length()>0){
									//若要判断的课程和目标课程都是合班课 则不允许调整
									tarCanNot = true;
									break;
								}else{	
									//若要判断的课程非合班课 目标课程为合班课 则判断 目标合班课可否调到当前位置
									String _classId = li.getString("ClassId");
									String _courseId = li.getString("CourseId");
									String _mcGroupId = li.getString("McGroupId");
									Map<String,String> tarTeachers = (Map<String, String>) li.get("teachers");
									List<JSONObject> tarconfs = getConflictsByPoint(_classId, _courseId, tarTeachers.keySet(), 
											dayOfWeek, lessonOfDay, arrangeMap, ruleConflict, gradeLev,
											unLessonCount, li.getIntValue("IsAdvance"), _mcGroupId,li.getIntValue("CourseType"),
											li.getIntValue("DayOfWeek"),teachers.keySet());
									if(tarconfs.size()>0){
										tarCanNot = true;
										break;
									}
								}
							}
						}
					}
				}
			
				JSONObject canSd = canArrageSdWeek(arrangeMap, i, j,
						unLessonCount, rClassIds, classId, courseId);

				boolean sdWeek = canSd.getBooleanValue("b");
				double tl = 0;
				int cType = courseType;
				if(unLessonCount!=-100&&!sdWeek){
					tl = canSd.getDoubleValue("tl");
					if(tl==0.5){
						cType = 2;
					}else{
						cType =1;
					}
				}
				boolean hasCourseByTeacher = hasCourseByTeacher(arrangeMap,
						teachers.keySet(), i, j, rClassIds, courseId, cType);
				//教师自己的课程可对调
				if(targetLessons!=null &&isForTeacher){
					List<JSONObject> tpList = new ArrayList<JSONObject>();
					if( targetLessons.size()>1){
						tpList = GrepUtil.grepJsonKeyBySingleVal("CourseId", courseId, targetLessons);
						Set<String> set = teachers.keySet();
						String teas = "";
						for(String t:set){
							teas+= t+",";
						}
						teas = teas.substring(0,teas.length()-1);
						tpList = GrepUtil.grepJsonKeyBySingleVal("Teachers", teas, targetLessons);
					}
					if(i==0&&(j==1||j==2)){
						System.out.println("fsdfs");
					}
					if(tpList.size()==1){
						JSONObject arrange = tpList.get(0);
						int tarDayOfWeek = arrange .getIntValue("DayOfWeek");
						int tarLessonOfDay = arrange.getIntValue("LessonOfDay");
						String tarClassId = arrange.getString("ClassId");
						String tarCourseId = arrange.getString("CourseId");
						int tarCourseType = arrange.getIntValue("CourseType");
						int tarIsAdvance = arrange.getIntValue("IsAdvance");
						
						if(courseType!=0&&courseType!=tarCourseType&&tarCourseType!=0){
						}else{
							
							Map<String, String> tarTeachers = (Map<String, String>) arrange
									.get("teachers");
							if(isTeaAllSame(tarTeachers.keySet(), teachers.keySet())
									&&!(dayOfWeek==tarDayOfWeek&&lessonOfDay==tarLessonOfDay)
									&&tarIsAdvance!=1
									&&!tarClassId.equals(classId)
									&&tarCourseId.equals(courseId)
									){
								hasCourseByTeacher = false;
							}
						}
					}
					
				}
				// boolean hasCourseByAdvance = this.hasCourseByAdvance(
				// arrangeList, i, j);
				boolean hasCourseByAdvance = false;
				
				long gradeId = ruleConflict.getGradeByLevel(Integer.parseInt(gradeLev)).getId();
				String usedGradeId = ruleConflict.getGradeIdSynjMap().get(gradeId);
				boolean canArrangeGround =  ruleConflict.canArrangeGround(usedGradeId , courseId, i, j,srcDay, cType);
					
				if (canArrangeCourse == 0 || canArrangeTeacher == 0
						|| hasCourseByTeacher || hasCourseByAdvance || sdWeek 
						|| canArrangeTarget==false||tarCanNot ||!canArrangeGround) {
					JSONObject conflict = new JSONObject();
					conflict.put("dayOfWeek", i);
					conflict.put("lessonOfDay", j);
					// -1，不可调；1，教师冲突
					int status = -1;
					if (hasCourseByTeacher) {
						status = 1;
					}
					conflict.put("Status", status);
					conflict.put("confType", "2冲突2");
					conflicts.add(conflict);
				}

			}

		}

		return conflicts;
	}
	/**
	 * 取课程冲突
	 * 
	 * @param classId
	 * @param courseId
	 * @param teachers
	 * @param maxDays
	 * @param maxLesson
	 * @param arrangeMap
	 * @param ruleConflict
	 * @param gradeLev
	 * @param unLessonCount
	 * @param IsAdvance
	 * @param lessonOfDay 要判断课程的位置
	 * @param dayOfWeek 
	 * @param forTempAjst 
	 * @param srcTeacherIds 
	 * @return
	 */
	public static List<JSONObject> getOnlyTeaConflictsWithSwapSub(String classId,
			String courseId, Map<String, String> teachers, int maxDays,
			int maxLesson, HashMap<String, List<JSONObject>> arrangeMap,
			RuleConflict ruleConflict, String gradeLev, double unLessonCount,
			int IsAdvance, int dayOfWeek, int lessonOfDay,String mcGroupId, 
			boolean forTempAjst,int courseType,int srcDay ,boolean isForTeacher) {
		List<JSONObject> conflicts = new ArrayList<JSONObject>();

		for (int i = 0; i < maxDays; i++) {
			
			for (int j = 0; j < maxLesson+1; j++) {
				
				boolean canArrangeTarget = true;
				
				List<JSONObject> targetLessons = arrangeMap.get(i+","+j);
				if(targetLessons==null){
					targetLessons = arrangeMap.get(i+","+j+","+classId);
				}
				if(targetLessons!=null&&targetLessons.size()>0){
					for(JSONObject tar:targetLessons){
						String tarClassId = tar.getString("ClassId");
						if(tarClassId.equalsIgnoreCase(classId)){
							String tarcourseId = tar.getString("CourseId");
							
							Map<String,String> tarTeachers = (Map<String, String>) tar.get("teachers");
							String tarMcGroupId = tar.getString("McGroupId");
//							if((forTempAjst&&tarMcGroupId!=null&&tarMcGroupId.length()>0)
//									||(forTempAjst&&mcGroupId!=null&&mcGroupId.length()>0)){
//								canArrangeTarget = false;
//								break;
//							}else{
								List<JSONObject> tarconfs = getOnlyTeaConflictsByPoint(classId, tarcourseId, tarTeachers.keySet(), 
										dayOfWeek, lessonOfDay, arrangeMap, ruleConflict, gradeLev, unLessonCount,
										tar.getIntValue("IsAdvance"), tarMcGroupId, tar.getIntValue("CourseType"),tar.getIntValue("DayOfWeek"),teachers.keySet());
								if(tarconfs.size()>0){
									canArrangeTarget = false;
									break;
								}
//							}
						}
					}
				}
				List<String> rClassIds = new ArrayList<String>();
				if(mcGroupId!=null){
					rClassIds = ruleConflict.getMergeClassByGroupId(mcGroupId);
				}
				if(rClassIds.size()==0){
					rClassIds.add(classId);
				}
				
				boolean tarCanNot = false;
				//取目标位置课程
				targetLessons = arrangeMap.get(i+","+j);
				for(String cid:rClassIds){
					if(targetLessons==null){
						targetLessons = arrangeMap.get(i+","+j+","+classId);
					}
					if(targetLessons==null){
						continue;
					}
					for(JSONObject li:targetLessons){
						if(cid.equalsIgnoreCase(li.getString("ClassId"))){
							if(li.containsKey("McGroupId")){
								if(rClassIds.size()>1&&mcGroupId!=null&&mcGroupId.trim().length()>0){
									//若要判断的课程和目标课程都是合班课 则不允许调整
									tarCanNot = true;
									break;
								}else{	
									//若要判断的课程非合班课 目标课程为合班课 则判断 目标合班课可否调到当前位置
									String _classId = li.getString("ClassId");
									String _courseId = li.getString("CourseId");
									String _mcGroupId = li.getString("McGroupId");
									Map<String,String> tarTeachers = (Map<String, String>) li.get("teachers");
									List<JSONObject> tarconfs = getOnlyTeaConflictsByPoint(_classId, _courseId, tarTeachers.keySet(), 
											dayOfWeek, lessonOfDay, arrangeMap, ruleConflict, gradeLev,
											unLessonCount, li.getIntValue("IsAdvance"), _mcGroupId,li.getIntValue("CourseType"),
											li.getIntValue("DayOfWeek"),teachers.keySet());
									if(tarconfs.size()>0){
										tarCanNot = true;
										break;
									}
								}
							}
						}
					}
				}
			
				JSONObject canSd = canArrageSdWeek(arrangeMap, i, j,
						unLessonCount, rClassIds, classId, courseId);

				boolean sdWeek = canSd.getBooleanValue("b");
				double tl = 0;
				int cType = courseType;
				if(unLessonCount!=-100&&!sdWeek){
					tl = canSd.getDoubleValue("tl");
					if(tl==0.5){
						cType = 2;
					}else{
						cType =1;
					}
				}
				boolean hasCourseByTeacher = hasCourseByTeacher(arrangeMap,
						teachers.keySet(), i, j, rClassIds, courseId, cType);
				// boolean hasCourseByAdvance = this.hasCourseByAdvance(
				// arrangeList, i, j);
				
				//教师自己的课程可对调
				if(targetLessons!=null &&isForTeacher){
					List<JSONObject> tpList = new ArrayList<JSONObject>();
					if( targetLessons.size()>1){
						tpList = GrepUtil.grepJsonKeyBySingleVal("CourseId", courseId, targetLessons);
						Set<String> set = teachers.keySet();
						String teas = "";
						for(String t:set){
							teas+= t+",";
						}
						teas = teas.substring(0,teas.length()-1);
						tpList = GrepUtil.grepJsonKeyBySingleVal("Teachers", teas, targetLessons);
					}
					if(i==0&&(j==1||j==2)){
						System.out.println("fsdfs");
					}
					if(tpList.size()==1){
						JSONObject arrange = tpList.get(0);
						int tarDayOfWeek = arrange .getIntValue("DayOfWeek");
						int tarLessonOfDay = arrange.getIntValue("LessonOfDay");
						String tarClassId = arrange.getString("ClassId");
						String tarCourseId = arrange.getString("CourseId");
						int tarCourseType = arrange.getIntValue("CourseType");
						int tarIsAdvance = arrange.getIntValue("IsAdvance");
						
						if(courseType!=0&&courseType!=tarCourseType&&tarCourseType!=0){
						}else{
							
							Map<String, String> tarTeachers = (Map<String, String>) arrange
									.get("teachers");
							if(isTeaAllSame(tarTeachers.keySet(), teachers.keySet())
									&&!(dayOfWeek==tarDayOfWeek&&lessonOfDay==tarLessonOfDay)
									&&tarIsAdvance!=1
									&&!tarClassId.equals(classId)
									&&tarCourseId.equals(courseId)
									){
								hasCourseByTeacher = false;
							}
						}
					}
					
				}
				
				if (  hasCourseByTeacher   || sdWeek 
						|| canArrangeTarget==false||tarCanNot  ) {
					JSONObject conflict = new JSONObject();
					conflict.put("dayOfWeek", i);
					conflict.put("lessonOfDay", j);
					// -1，不可调；1，教师冲突
					int status = -1;
					if (hasCourseByTeacher) {
						status = 1;
					}
					conflict.put("Status", status);
					conflict.put("confType", "2冲突2");
					conflicts.add(conflict);
				}

			}

		}

		return conflicts;
	}
	
	
	/**
	 * 取课程冲突
	 * 
	 * @param classId
	 * @param courseId
	 * @param teachers
	 * @param maxDays
	 * @param maxLesson
	 * @param arrangeMap
	 * @param ruleConflict
	 * @param gradeLev
	 * @param unLessonCount
	 * @param IsAdvance
	 * @param lessonOfDay 要判断课程的位置
	 * @param dayOfWeek 
	 * @param forTempAjst 
	 * @param srcTeacherIds 
	 * @return
	 */
	public static List<JSONObject> getConflictsWithSwap(String classId,
			String courseId, Map<String, String> teachers, int maxDays,
			int maxLesson, HashMap<String, List<JSONObject>> arrangeMap,
			RuleConflict ruleConflict, String gradeLev, double unLessonCount,
			int IsAdvance, int dayOfWeek, int lessonOfDay,String mcGroupId, 
			boolean forTempAjst,int courseType,int srcDay ,boolean isForTeacher) {
		List<JSONObject> conflicts = new ArrayList<JSONObject>();
		if(mcGroupId!=null&&mcGroupId.trim().length()>0){
			List<String> rClassIds = new ArrayList<String>();
			if(mcGroupId!=null){
				rClassIds = ruleConflict.getMergeClassByGroupId(mcGroupId);
			}
			for(String cid:rClassIds){
				List<JSONObject> temp = getConflictsWithSwapSub(cid, courseId, teachers, maxDays, 
						maxLesson, arrangeMap, ruleConflict, gradeLev, unLessonCount, 
						IsAdvance, dayOfWeek, lessonOfDay, mcGroupId, forTempAjst, 
						courseType, srcDay,isForTeacher);
				
				conflicts.addAll(temp);
			}
		}else{
			conflicts = getConflictsWithSwapSub(classId, courseId, teachers, maxDays, 
					maxLesson, arrangeMap, ruleConflict, gradeLev, unLessonCount, 
					IsAdvance, dayOfWeek, lessonOfDay, mcGroupId, forTempAjst, 
					courseType, srcDay,isForTeacher);
		}
		
		return conflicts;
	}


	/**
	 * 取课程冲突
	 * 
	 * @param classId
	 * @param courseId
	 * @param teachers
	 * @param maxDays
	 * @param maxLesson
	 * @param arrangeMap
	 * @param ruleConflict
	 * @param gradeLev
	 * @param unLessonCount
	 * @param IsAdvance
	 * @param lessonOfDay 要判断课程的位置
	 * @param dayOfWeek 
	 * @param forTempAjst 
	 * @param srcTeacherIds 
	 * @return
	 */
	public static List<JSONObject> getOnlyTeaConflictsWithSwap(String classId,
			String courseId, Map<String, String> teachers, int maxDays,
			int maxLesson, HashMap<String, List<JSONObject>> arrangeMap,
			RuleConflict ruleConflict, String gradeLev, double unLessonCount,
			int IsAdvance, int dayOfWeek, int lessonOfDay,String mcGroupId, 
			boolean forTempAjst,int courseType,int srcDay ,boolean isForTeacher) {
		List<JSONObject> conflicts = new ArrayList<JSONObject>();
		if(mcGroupId!=null&&mcGroupId.trim().length()>0){
			List<String> rClassIds = new ArrayList<String>();
			if(mcGroupId!=null){
				rClassIds = ruleConflict.getMergeClassByGroupId(mcGroupId);
			}
			for(String cid:rClassIds){
				List<JSONObject> temp = getOnlyTeaConflictsWithSwapSub(cid, courseId, teachers, maxDays, 
						maxLesson, arrangeMap, ruleConflict, gradeLev, unLessonCount, 
						IsAdvance, dayOfWeek, lessonOfDay, mcGroupId, forTempAjst, courseType, srcDay, isForTeacher);
				
				conflicts.addAll(temp);
			}
		}else{
			conflicts = getOnlyTeaConflictsWithSwapSub(classId, courseId, teachers, maxDays, 
					maxLesson, arrangeMap, ruleConflict, gradeLev, unLessonCount, 
					IsAdvance, dayOfWeek, lessonOfDay, mcGroupId, forTempAjst, courseType, srcDay,isForTeacher);
		}
		
		return conflicts;
	}

	/**
	 * 取课程冲突
	 * 
	 * @param classId
	 * @param courseId
	 * @param teachers
	 * @param maxDays
	 * @param maxLesson
	 * @param arrangeMap
	 * @param ruleConflict
	 * @param gradeLev
	 * @param unLessonCount
	 * @param IsAdvance
	 * @param srcDay 
	 * @param srcTeacherIds 
	 * @return
	 */
	public static List<JSONObject> getConflictsByPoint(String classId,
			String courseId, Set<String> teacherIds, int dayOfWeek,
			int lessonOfDay, HashMap<String, List<JSONObject>> arrangeMap,
			RuleConflict ruleConflict, String gradeLev, double unLessonCount,
			int IsAdvance,String mcGroupId,int courseType, int srcDay, Set<String> srcTeacherIds) {
		
		List<JSONObject> conflicts = new ArrayList<JSONObject>();
		//要换的位置
		int i =dayOfWeek;
		int j= lessonOfDay;
		if (IsAdvance == 1) {
				JSONObject conflict = new JSONObject();
				conflict.put("dayOfWeek", i);
				conflict.put("lessonOfDay", j);
				// -1，不可调；1，教师冲突
				int status = -1;
				conflict.put("Status", status);
				conflict.put("confType", "固排");
				conflicts.add(conflict);
		}else{
			int canArrangeCourse = ruleConflict.canArrangeCourse(courseId,
					i, j, gradeLev, classId, arrangeMap);
			int canArrangeTeacher = ruleConflict.canArrangeTeacher(
					teacherIds, i, j,srcDay,arrangeMap, courseType);
			
			List<String> rClassIds = new ArrayList<String>();
			if(mcGroupId!=null){
				rClassIds = ruleConflict.getMergeClassByGroupId(mcGroupId);
			}
			if(rClassIds.size()==0){
				rClassIds.add(classId);
			}
			JSONObject canSd = canArrageSdWeek(arrangeMap, i, j,
					unLessonCount, rClassIds,classId,courseId);

			boolean sdWeek = canSd.getBooleanValue("b");
			double tl = 0;
			int cType = courseType;
			if(unLessonCount!=-100&&!sdWeek){
				tl = canSd.getDoubleValue("tl");
				if(tl==0.5){
					cType = 2;
				}else{
					cType =1;
				}
			}
			boolean hasCourseByTeacher = hasCourseByTeacher(arrangeMap,
					teacherIds, i, j, rClassIds, courseId, cType);
			// boolean hasCourseByAdvance = this.hasCourseByAdvance(
			// arrangeList, i, j);
			boolean hasCourseByAdvance = false;
			
			long gradeId = ruleConflict.getGradeByLevel(Integer.parseInt(gradeLev)).getId();
			String usedGradeId = ruleConflict.getGradeIdSynjMap().get(gradeId);
			boolean canArrangeGround =  ruleConflict.canArrangeGround(usedGradeId , courseId, i, j,srcDay, cType);
			
			
			if (canArrangeCourse == 0 || canArrangeTeacher == 0
					|| hasCourseByTeacher || hasCourseByAdvance || sdWeek ||!canArrangeGround) {
				JSONObject conflict = new JSONObject();
				conflict.put("dayOfWeek", i);
				conflict.put("lessonOfDay", j);
				// -1，不可调；1，教师冲突
				int status = -1;
				if (hasCourseByTeacher) {
					status = 1;
				}
				conflict.put("Status", status);
				conflict.put("confType", "2冲突2");
				conflicts.add(conflict);
			}
		}
		
		
				
		
		return conflicts;
	}
	/**
	 * 取课程冲突
	 * 
	 * @param classId
	 * @param courseId
	 * @param teachers
	 * @param maxDays
	 * @param maxLesson
	 * @param arrangeMap
	 * @param ruleConflict
	 * @param gradeLev
	 * @param unLessonCount
	 * @param IsAdvance
	 * @param srcDay 
	 * @param srcTeacherIds 
	 * @return
	 */
	public static List<JSONObject> getOnlyTeaConflictsByPoint(String classId,
			String courseId, Set<String> teacherIds, int dayOfWeek,
			int lessonOfDay, HashMap<String, List<JSONObject>> arrangeMap,
			RuleConflict ruleConflict, String gradeLev, double unLessonCount,
			int IsAdvance,String mcGroupId,int courseType, int srcDay, Set<String> srcTeacherIds) {
		
		List<JSONObject> conflicts = new ArrayList<JSONObject>();
		//要换的位置
		int i =dayOfWeek;
		int j= lessonOfDay;
		 
		
		List<String> rClassIds = new ArrayList<String>();
		if(mcGroupId!=null){
			rClassIds = ruleConflict.getMergeClassByGroupId(mcGroupId);
		}
		if(rClassIds.size()==0){
			rClassIds.add(classId);
		}
		JSONObject canSd = canArrageSdWeek(arrangeMap, i, j,
				unLessonCount, rClassIds,classId,courseId);
		
		boolean sdWeek = canSd.getBooleanValue("b");
		double tl = 0;
		int cType = courseType;
		if(unLessonCount!=-100&&!sdWeek){
			tl = canSd.getDoubleValue("tl");
			if(tl==0.5){
				cType = 2;
			}else{
				cType =1;
			}
		}
		boolean hasCourseByTeacher = hasCourseByTeacher(arrangeMap,
				teacherIds, i, j, rClassIds, courseId, cType);
		// boolean hasCourseByAdvance = this.hasCourseByAdvance(
		// arrangeList, i, j);
		
		if (   hasCourseByTeacher   || sdWeek  ) {
			JSONObject conflict = new JSONObject();
			conflict.put("dayOfWeek", i);
			conflict.put("lessonOfDay", j);
			// -1，不可调；1，教师冲突
			int status = -1;
			if (hasCourseByTeacher) {
				status = 1;
			}
			conflict.put("Status", status);
			conflict.put("confType", "2冲突2");
			conflicts.add(conflict);
		}
	
		return conflicts;
	}

	/**
	 *  计算某个排课位置是否跟老师有冲突
	 * @param arrangeMap
	 * @param teacherIds
	 * @param day
	 * @param lesson
	 * @param rClassIds
	 * @param courseId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static boolean hasCourseByTeacher(
			HashMap<String, List<JSONObject>> arrangeMapNj,
			Set<String> teacherIds, int day, int lesson, List<String> rClassIds,
			String courseId,int courseType) {

		for(String cid:rClassIds){
			boolean hasC = hasCourseByTeacherChildClass(arrangeMapNj, teacherIds, day, lesson, cid, courseId,courseType);
			if(hasC){
				return true;
			}
		}

		return false;
	}
	@SuppressWarnings("unchecked")
	public static boolean hasCourseByTeacherChildClass(
			HashMap<String, List<JSONObject>> arrangeMap,
			Set<String> teacherIds, int day, int lesson, String classId,
			String courseId,int courseType) {
		
		if(teacherIds==null||teacherIds.size()==0){
			return false;
		}
		List<JSONObject> arrangeList = arrangeMap.get(day + "," + lesson);
		if(arrangeList==null){
			arrangeList =  arrangeMap.get(day + "," + lesson+","+classId);
		}
		if (arrangeList!=null&&arrangeList.size()>0) {
			for (JSONObject arrange : arrangeList) {
				int dayOfWeek = arrange.getIntValue("DayOfWeek");
				int lessonOfDay = arrange.getIntValue("LessonOfDay");
				String tarClassId = arrange.getString("ClassId");
				String tarCourseId = arrange.getString("CourseId");
				int CourseType = arrange.getIntValue("CourseType");
				int IsAdvance = arrange.getIntValue("IsAdvance");
				
				if(courseType!=0&&courseType!=CourseType&&CourseType!=0){
					continue;
				}
				
				Map<String, String> teachers = (Map<String, String>) arrange
						.get("teachers");
				boolean isTeaAllSame = isTeaAllSame(teacherIds,
						teachers.keySet());
				if (classId.equalsIgnoreCase(tarClassId)
						&& !courseId.equalsIgnoreCase(tarCourseId)
						&& isTeaAllSame && IsAdvance != 1 && CourseType == 0
						&& dayOfWeek == day && lessonOfDay == lesson) {
					return false;
				}
//				if (!classId.equalsIgnoreCase(tarClassId)
//						&& courseId.equalsIgnoreCase(tarCourseId)
//						&& isTeaAllSame && IsAdvance != 1 && CourseType == 0
//						&& dayOfWeek == day && lessonOfDay == lesson) {
//					return false;
//				}
				if (dayOfWeek == day && lessonOfDay == lesson) {
					for (String teacherId : teacherIds) {
						if (teacherId!=null&&teachers.containsKey(teacherId)) {
							return true;
						}
					}
				}
			}
		}
		
		return false;
	}

	/**
	 * 判断两个课眼教师是否完全相同
	 * @param s1
	 * @param s2
	 * @return
	 */
	public static boolean isTeaAllSame(Set<String> s1, Set<String> s2) {
		// TODO Auto-generated method stub
		for (String s : s1) {
			if (!s2.contains(s)) {
				return false;
			}
		}
		for (String s : s2) {
			if (!s1.contains(s)) {
				return false;
			}
		}
		return true;
	}

	/**
	 *  查某个课表位置是否有预排课
	 * @param arrangeList
	 * @param day
	 * @param lesson
	 * @return
	 */
	public static boolean hasCourseByAdvance(List<JSONObject> arrangeList,
			int day, int lesson) {
		for (JSONObject arrange : arrangeList) {
			int isAdvance = arrange.getIntValue("IsAdvance");
			int dayOfWeek = arrange.getIntValue("DayOfWeek");
			int lessonOfDay = arrange.getIntValue("LessonOfDay");
			if (dayOfWeek == day && lessonOfDay == lesson && isAdvance == 1) {
				return true;
			}

		}
		return false;
	}

	/**
	 *  查某个课表位置是否可以安排单双周-----
	 * @param arrangeMap
	 * @param day
	 * @param lesson
	 * @param unLessonCount
	 * @param rClassIds
	 * @param courseId 
	 * @param classId 
	 * @return
	 */
	public static JSONObject canArrageSdWeek(
			HashMap<String, List<JSONObject>> arrangeMap, int day, int lesson,
			double unLessonCount, List<String> rClassIds, String classId, String courseId) {
		
		JSONObject obj = new JSONObject();
		if (unLessonCount == -100) {
			obj.put("b", false) ;
			return obj;
		}
		double targetLess = 0;
		List<JSONObject> arrangeList = arrangeMap.get(day + "," + lesson);
		//合班课 -只增加一次课时
		boolean addedMerge = false;
		if (arrangeList != null) {

			for (JSONObject arrange : arrangeList) {
				int dayOfWeek = arrange.getIntValue("DayOfWeek");
				int lessonOfDay = arrange.getIntValue("LessonOfDay");
				int couseType = arrange.getIntValue("CourseType");
				String ClassId = arrange.getString("ClassId");
				String CourseId = arrange.getString("CourseId");
				String mcGroupId = arrange.getString("McGroupId");
				if (dayOfWeek == day && lessonOfDay == lesson
						&& rClassIds.contains(ClassId) ) {
					if(mcGroupId!=null&&mcGroupId.length()>0){
						if(addedMerge){
							continue;
						}
						addedMerge = true;
					}
					if (couseType == 0) {
						targetLess += 1;
					} else {
						targetLess += 0.5;
					}
				}
				if(unLessonCount == 0.5&&(classId.equals(ClassId)&&courseId.equals(CourseId))){
					obj.put("b", true) ;
					return obj;
				}

			}
		}
		// 当前课程为0.5 且目标位置大于0.5课时了
		if (unLessonCount == 0.5 && targetLess > 0.5) {
			obj.put("b", true) ;
			
		}else{
			
			obj.put("b", false) ;
		}

		obj.put("tl", targetLess);
		
		return obj;
	}

	/**
	 * 将col转换为list
	 * @param curWeekTbAllNj
	 * @return
	 */
	public static List<JSONObject> changeMapListToList(
			HashMap<String, List<JSONObject>> curWeekTbAllNj) {
		// TODO Auto-generated method stub
		List<JSONObject> rs = new ArrayList<JSONObject>();
		for(Iterator<String> it = curWeekTbAllNj.keySet().iterator();it.hasNext();){
			List<JSONObject> temp = curWeekTbAllNj.get(it.next());
			rs.addAll(temp);
		}
		return rs;
	}

	/**
	 * 获取跨周课表合并 
	 * @param arrangeMap
	 * @param tempAjustList
	 * @param week
	 * @param weekOfEnd
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static HashMap<String, List<JSONObject>> getAllWeekTimeTableByParam(
			HashMap<String, List<JSONObject>> arrangeMap,
			List<JSONObject> tempAjustList, int week, int weekOfEnd,RuleConflict ruleConflict) {
		// TODO Auto-generated method stub
		HashMap<String, List<JSONObject>> arrangeMapClone = 
				copyMapToMap(arrangeMap,week);
		
		if (weekOfEnd == 0) {
			HashMap<String, List<JSONObject>> tempmap = getSingleWeekTimeTableByParam (arrangeMap, tempAjustList, week, null, ruleConflict);
			return tempmap;
		} else {
//			arrangeMapClone =  getSingleWeekTimeTableByParam (arrangeMap, tempAjustList, week, null, ruleConflict);
			for(int i=week;i<=weekOfEnd;i++){
				HashMap<String, List<JSONObject>> arrangeMapTemp  = 
				copyMapToMap(arrangeMap,week);
				HashMap<String, List<JSONObject>> tempmap = getSingleWeekTimeTableByParam (arrangeMapTemp , tempAjustList, i, null, ruleConflict);
				arrangeMapClone = MergeNjTbMap(arrangeMapClone,tempmap);
			}
			return arrangeMapClone;
		}
		
	}
	/**
	 * 获取跨周课表合并 
	 * @param arrangeMap
	 * @param tempAjustList
	 * @param week
	 * @param weekOfEnd
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static HashMap<Integer,HashMap<String, List<JSONObject>>> getEvryWeekTimeTableByParam(
			HashMap<String, List<JSONObject>> arrangeMap,
			List<JSONObject> tempAjustList, int week, int weekOfEnd,RuleConflict ruleConflict) {
		// TODO Auto-generated method stub
		HashMap<Integer,HashMap<String, List<JSONObject>>> rs = new HashMap<Integer, HashMap<String,List<JSONObject>>>();
		HashMap<String, List<JSONObject>> arrangeMapClone = 
				copyMapToMap(arrangeMap,week);
		if (weekOfEnd == 0) {
			HashMap<String, List<JSONObject>> tempmap = getSingleWeekTimeTableByParam (arrangeMap, tempAjustList, week, null, ruleConflict);
			rs.put(week, tempmap);
		} else {
			for(int i=week;i<=weekOfEnd;i++){
				HashMap<String, List<JSONObject>> tempmap = getSingleWeekTimeTableByParam (arrangeMap, tempAjustList, i, null, ruleConflict);
				arrangeMapClone = MergeNjTbMap(arrangeMapClone,tempmap);
				rs.put(i, tempmap);
			}
//			rs.put(week, arrangeMapClone);
		}
		return rs;
	}
	/**
	 * 合并各年级课表 用于冲突判断
	 * @param left
	 * @param tempmap
	 * @return
	 */
	private static HashMap<String, List<JSONObject>> MergeNjTbMap(
			HashMap<String, List<JSONObject>> left,
			HashMap<String, List<JSONObject>> tempmap) {
		// TODO Auto-generated method stub
		for(Iterator<String> it = left.keySet().iterator();it.hasNext();){
			String key = it.next();
			List<JSONObject> leftList = left.get(key);
			List<JSONObject> rightList = tempmap.get(key);
			if(key.equals("2,2")){
				System.out.println("fds");
			}
			leftList = MergeTbList(leftList,rightList);
			
			left.put(key, leftList);
			
		}
		return left;
	}

	private static List<JSONObject> MergeTbList(List<JSONObject> leftList,
			List<JSONObject> rightList) {
		// TODO Auto-generated method stub
		HashMap<String,JSONObject> leftMap = new HashMap<String, JSONObject>();
		HashMap<String,JSONObject> rightMap = new HashMap<String, JSONObject>();
		for(JSONObject t:leftList){
			int dayOfWeek = t.getIntValue("DayOfWeek");
			int lessonOfDay = t.getIntValue("LessonOfDay");
			int couseType = t.getIntValue("CourseType");
			String courseId = t.getString("CourseId");
			String classId = t.getString("ClassId");
			String Teachers = t.getString("Teachers");
			
			String key = classId+"_"+dayOfWeek+"_"+lessonOfDay+"_"+courseId+"_"+couseType+"_"+Teachers;
			leftMap.put(key, t);
		}
		for(JSONObject t:rightList){
			int dayOfWeek = t.getIntValue("DayOfWeek");
			int lessonOfDay = t.getIntValue("LessonOfDay");
			int couseType = t.getIntValue("CourseType");
			String courseId = t.getString("CourseId");
			String classId = t.getString("ClassId");
			String Teachers = t.getString("Teachers");
			
			String key = classId+"_"+dayOfWeek+"_"+lessonOfDay+"_"+courseId+"_"+couseType+"_"+Teachers;
			rightMap.put(key, t);
		}
		
		for(Iterator<String> it = leftMap.keySet().iterator();it.hasNext();){
			String key = it.next();
			if(!rightMap.containsKey(key)){
				rightList.add(leftMap.get(key));
			}
		}
		
		return rightList;
	}

	/**
	 * 获取单个教师课表 带冲突
	 * @param ruleConflict
	 * @param curWeekTbAllNj
	 * @param arrangeMap
	 * @return
	 */
	public static List<JSONObject> getTeacherTimetableWithConflict(
			RuleConflict ruleConflict,
			HashMap<String, List<JSONObject>> curWeekTbAllNj,
			HashMap<String, List<JSONObject>> arrangeMap,String teacherId,
			int maxDaysForWeek,int maxLessonForDay, boolean forTempAjst ) {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
		List<JSONObject> teaMap = new ArrayList<JSONObject>();

		for (Iterator<String> it = arrangeMap.keySet().iterator(); it
				.hasNext();) {
			String key = it.next();
			List<JSONObject> list = arrangeMap.get(key);
			for (JSONObject les : list) {
				String courseId = les.getString("CourseId");
				les.put("courseId", courseId);
				LessonInfo course = ruleConflict.getCoursesDic()
						.get(courseId);
				if(course==null){
					continue;
				}
				les.put("courseName",course. getName());
				les.put("courseSimpleName",  course.getSimpleName()!= null ?
						course.getSimpleName() : course.getName().substring(0, 1));
				String classId = les.getString("ClassId");
				les.put("classId", classId);
				Classroom classroom = ruleConflict.getClassroomsDic().get(classId);
				if(classroom==null){
					continue;
				}
				String gradeId = ruleConflict.getGradeIdSynjMap().get(
						classroom.getGradeId());
				les.put("className", classroom.getClassName());
				les.put("usedGrade", gradeId);
				les.put("dayOfWeek", les.get("DayOfWeek"));
				les.put("lessonOfDay", les.get("LessonOfDay"));
				les.put("courseType", les.get("CourseType"));
				les.put("AdjustType", changeAjstType(les.getString("AdjustType")));
				String teachers = les.getString("Teachers");
				if (teachers != null && teachers.trim().length() > 0) {

					String[] teas = teachers.split(",");

					for (int i = 0; i < teas.length; i++) {
						String teaid = teas[i].trim() ;
						if(!teaid.equalsIgnoreCase(teacherId)){
							continue;
						}
						Grade ddd = ruleConflict.getGradeIdGradeDic().get(
								classroom.getGradeId());
						String gradeLev = ddd.getCurrentLevel().getValue() + "";
						Map<String, String> teacherparam = new HashMap<String, String>();
						teacherparam.put( teaid,"tea");
						List<JSONObject> confList = new ArrayList<JSONObject>();
						if(les.containsKey("conflicts")){
							confList = (List<JSONObject>) les.get("conflicts");
						}
							
						List<JSONObject> conflicts = getConflictsWithSwap(classId, courseId,
								teacherparam , maxDaysForWeek, maxLessonForDay, arrangeMap,
								ruleConflict, gradeLev, -100,
								les.getIntValue("IsAdvance"),  les.getIntValue("DayOfWeek"), 
								les.getIntValue("LessonOfDay"),les.getString("McGroupId"), 
								forTempAjst,les.getIntValue("CourseType"),les.getIntValue("DayOfWeek"),true  );
						confList .addAll (conflicts);
						les.put("conflicts",confList);
						teaMap.add(les);
					}
				}

			}
		}
		return teaMap;
	}
	/**
	 * 获取单个教师课表 带冲突--仅判断教师冲突
	 * @param ruleConflict
	 * @param curWeekTbAllNj
	 * @param arrangeMap
	 * @return
	 */
	public static List<JSONObject> getOnlyTeacherTimetableWithConflict(
			RuleConflict ruleConflict,
			HashMap<String, List<JSONObject>> curWeekTbAllNj,
			HashMap<String, List<JSONObject>> arrangeMap,String teacherId,
			int maxDaysForWeek,int maxLessonForDay, boolean forTempAjst) {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
		List<JSONObject> teaMap = new ArrayList<JSONObject>();

		for (Iterator<String> it = curWeekTbAllNj.keySet().iterator(); it
				.hasNext();) {
			String key = it.next();
			List<JSONObject> list = curWeekTbAllNj.get(key);
			for (JSONObject les : list) {
				String courseId = les.getString("CourseId");
				les.put("courseId", courseId);
				LessonInfo course = ruleConflict.getCoursesDic()
						.get(courseId);
				if(course==null){
					continue;
				}
				les.put("courseName",course. getName());
				les.put("courseSimpleName",  course.getSimpleName()!= null ?
						course.getSimpleName() : course.getName().substring(0, 1));
				String classId = les.getString("ClassId");
				les.put("classId", classId);
				Classroom classroom = ruleConflict.getClassroomsDic().get(classId);
				if(classroom==null){
					continue;
				}
				String gradeId = ruleConflict.getGradeIdSynjMap().get(
						classroom.getGradeId());
				les.put("className", classroom.getClassName());
				les.put("usedGrade", gradeId);
				les.put("dayOfWeek", les.get("DayOfWeek"));
				les.put("lessonOfDay", les.get("LessonOfDay"));
				les.put("courseType", les.get("CourseType"));
				les.put("AdjustType", changeAjstType(les.getString("AdjustType")));
				String teachers = les.getString("Teachers");
				if (teachers != null && teachers.trim().length() > 0) {

					String[] teas = teachers.split(",");

					for (int i = 0; i < teas.length; i++) {
						String teaid = teas[i].trim() ;
						if(!teaid.equalsIgnoreCase(teacherId)){
							continue;
						}
						Grade ddd = ruleConflict.getGradeIdGradeDic().get(
								classroom.getGradeId());
						String gradeLev = ddd.getCurrentLevel().getValue() + "";
						Map<String, String> teacherparam = new HashMap<String, String>();
						teacherparam.put( teaid,"tea");
						List<JSONObject> conflicts = new ArrayList<JSONObject>();
						if(les.containsKey("conflicts")){
							conflicts = (List<JSONObject>) les.get("conflicts");
						}
						List<JSONObject> tempList = getOnlyTeaConflictsWithSwap(classId, courseId,
								teacherparam , maxDaysForWeek, maxLessonForDay, arrangeMap,
								ruleConflict, gradeLev, -100,
								les.getIntValue("IsAdvance"),  les.getIntValue("DayOfWeek"), 
								les.getIntValue("LessonOfDay"),les.getString("McGroupId"), 
								forTempAjst,les.getIntValue("CourseType"),les.getIntValue("DayOfWeek") ,true );
						conflicts.addAll(tempList);
						les.put("conflicts",conflicts);
						teaMap.add(les);
					}
				}

			}
		}
		return teaMap;
	}
	/**
	 * 根据年级课表映射 	按班级做课表 
	 * @param ruleConflict
	 * @param allWeekTbAllNj
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<JSONObject> getAjustTimeTableByClass(
			RuleConflict ruleConflict,
			HashMap<String, List<JSONObject>> allWeekTbAllNj) {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
		// 已排课表中计算冲突
		HashMap<String,JSONObject> classList = new HashMap<String, JSONObject>();
		List<JSONObject> arrangeList = changeMapListToList(allWeekTbAllNj);
		for (JSONObject arrange : arrangeList) {
			String _classId = arrange.getString("ClassId");
			JSONObject classObj = new JSONObject();
			List<JSONObject> classTb  = new ArrayList<JSONObject>();
			if (classList.containsKey(_classId)) {
				// 不是本班的课表，则跳过
				classObj = classList.get(_classId);
				classTb = (List<JSONObject>) classObj.get("timetable");
			}
			Classroom classroom = ruleConflict.getClassroomsDic().get(_classId);

			if(classroom==null){
				continue;
			}
			classObj.put("classId", _classId);
			classObj.put("className", classroom.getClassName());
			
			String courseId = arrange.getString("CourseId");
			// 0：正课，1：单周课，2：双周课

			JSONObject timetable = new JSONObject();
			timetable.put("courseId", courseId);
			timetable.put("courseType", arrange.get("CourseType"));
			timetable.put("lessonOfDay", arrange.get("LessonOfDay"));
			timetable.put("dayOfWeek", arrange.get("dayOfWeek"));
			timetable.put("usedGrade", ruleConflict.getGradeIdSynjMap().get(classroom.getGradeId()));
			LessonInfo courseInfo = ruleConflict.findCourseInfo(courseId);
			if (courseInfo != null) {
				// 课程名称
				timetable.put("courseName", courseInfo.getName());
				timetable.put(
						"courseSimpleName",
						courseInfo.getSimpleName() != null ? courseInfo
								.getSimpleName() : courseInfo.getName()
								.substring(0, 1));
			}

			//
			Map<String, String> teachers = (Map<String, String>) arrange
					.get("teachers");
			timetable.put("teachers", convertTeacherToList(teachers));

			classTb.add(timetable);
			classObj.put("timetable", classTb);
			classList.put(_classId, classObj);
		}

		List<JSONObject> rslist = new ArrayList<JSONObject>();
		for(JSONObject en:classList.values()){
			rslist.add(en);
		}
		return rslist;
	}

	/**
	 * 获取需要插入数据库的临时调课记录
	 * @param arrangeMap
	 * @param fromDayOfWeek
	 * @param fromLessonOfDay
	 * @param fromCourseIds
	 * @param fromCourseTypes 
	 * @param fromMcGroupIds 
	 * @param toDayOfWeek
	 * @param toLessonOfDay
	 * @param toCourseIds
	 * @param toCourseTypes 
	 * @param classId 
	 * @param classId 
	 * @param weekOfEnd 
	 * @param week 
	 * @param timetableId 
	 * @param schoolId 
	 * @param ruleConflict 
	 * @param tempAjustList 
	 * @param evryWeekTbAllNj 每周课表 用于判断是否含当前要调动课程
	 * @param taskMap 
	 * @return
	 */
	public static JSONObject getNeedRecordAdjustedList(
			HashMap<String, List<JSONObject>> arrangeMap,
			Integer fromDayOfWeek, Integer fromLessonOfDay,
			String fromCourseIds, String fromCourseTypes, String fromMcGroupIds, Integer toDayOfWeek, Integer toLessonOfDay,
			String toCourseIds, String toCourseTypes, String toMcGroupIds, String classId, int week, int weekOfEnd, 
			RuleConflict ruleConflict, String schoolId, String timetableId, 
			List<JSONObject> tempAjustList,
			HashMap<Integer, HashMap<String, List<JSONObject>>> evryWeekTbAllNj,
			HashMap<String, HashMap<String, HashMap<String, String>>> taskMap,String groupId) {
		// TODO Auto-generated method stub
		List<TCRCATemporaryAdjustRecord> needInsertRecord = new ArrayList<TCRCATemporaryAdjustRecord>();
		List<TCRCATemporaryAdjustResult> needInsertResult = new ArrayList<TCRCATemporaryAdjustResult>();
		List<TCRCATemporaryAdjustResult> needDeleteResult = new ArrayList<TCRCATemporaryAdjustResult>();
		
		String gradeId = ruleConflict.getGradeIdSynjMap().get(ruleConflict.getClassroomsDic().get(classId).getGradeId());
		
		String[] fromCourses = fromCourseIds.split(",");
		String[] fromCourseTps = fromCourseTypes.split(",");
				
		String[] toCourses = toCourseIds.split(",");
		//目标位置可能为空
		String[] toCourseTps = toCourseTypes.split(",");
		HashMap<String, List<JSONObject>> curtb = evryWeekTbAllNj.get(week);
		String fTeachers = getTeacherByWeekTb(curtb,classId,fromCourseIds,fromDayOfWeek,fromLessonOfDay);
		String tTeachers = getTeacherByWeekTb(curtb, classId, toCourseIds,toDayOfWeek,toLessonOfDay);
		
		String fctype = "0";
		for(int i=0;i<fromCourses.length;i++){
			//分析每个原课程位置变化情况
			String fCourseId  = fromCourses[i].trim();
			String fCourseType= fromCourseTps[i];
			if(fCourseType.equalsIgnoreCase("0")){
				fctype = fCourseType;
			}
			
			if(weekOfEnd==0){
				genNeedInsertAndDelResult(arrangeMap, fCourseId, fromDayOfWeek, fromLessonOfDay, 
						fCourseId, null, toDayOfWeek, toLessonOfDay,
						null, classId, week, week, weekOfEnd, schoolId, tempAjustList,
						needInsertResult, needDeleteResult, gradeId, timetableId, fctype, "1",evryWeekTbAllNj, groupId);
			}else{
				for(int j=week;j<=weekOfEnd;j++){		
					if(JudgeNeedContinueByCourseType(fromCourseTps,toCourseTps,j)){
						continue;
					}
					genNeedInsertAndDelResult(arrangeMap, fCourseId, fromDayOfWeek, fromLessonOfDay, 
							fCourseId, null, toDayOfWeek, toLessonOfDay,
							null, classId, j, week, weekOfEnd, schoolId, tempAjustList,
							needInsertResult, needDeleteResult, gradeId, timetableId, fctype, "1",evryWeekTbAllNj, groupId);
					
				}
			}
		}
		
		if(weekOfEnd==0){
			genNeedInsertRecord(fromDayOfWeek, fromLessonOfDay, fromCourseIds, fTeachers, 
					toDayOfWeek, toLessonOfDay, toCourseIds, tTeachers, classId, week,week, weekOfEnd, 
					schoolId, needInsertRecord, gradeId, timetableId, fctype, "1");
		}else{
			for(int j=week;j<=weekOfEnd;j++){		
				if(JudgeNeedContinueByCourseType(fromCourseTps,toCourseTps,j)){
					continue;
				}
				genNeedInsertRecord(fromDayOfWeek, fromLessonOfDay, fromCourseIds, fTeachers, 
						toDayOfWeek, toLessonOfDay, toCourseIds, tTeachers, classId, j,week, weekOfEnd, 
						schoolId, needInsertRecord, gradeId, timetableId, fctype, "1");
			}
		}
		for(int i=0;i<toCourses.length;i++){
			//分析每个原课程位置变化情况
			String tCourseId  = toCourses[i].trim();
			if(tCourseId.length()>0){
				
				if(weekOfEnd==0){
					genNeedInsertAndDelResult(arrangeMap, tCourseId, toDayOfWeek, toLessonOfDay, 
							tCourseId, null, fromDayOfWeek, fromLessonOfDay,
							null, classId,week, week, weekOfEnd, schoolId, tempAjustList,
							needInsertResult, needDeleteResult, gradeId, timetableId, fctype, "1",evryWeekTbAllNj, groupId);
						
				}else{
					
					for(int j=week;j<=weekOfEnd;j++){
						//跨周
						if(JudgeNeedContinueByCourseType(fromCourseTps,toCourseTps,j)){
							continue;
						}
						genNeedInsertAndDelResult(arrangeMap, tCourseId, toDayOfWeek, toLessonOfDay, 
								tCourseId, null, fromDayOfWeek, fromLessonOfDay,
								null, classId,j, week, weekOfEnd, schoolId, tempAjustList,
								needInsertResult, needDeleteResult, gradeId, timetableId, fctype, "1",evryWeekTbAllNj, groupId);
					}
				}
			}
			
			
			
		}
		JSONObject rs = new JSONObject();
		rs.put("needInsertRecord", needInsertRecord);
		rs.put("needInsertResult", needInsertResult);
		rs.put("needDeleteResult", needDeleteResult);
		return rs;
	}

	private static String getTeacherByWeekTb(
			HashMap<String, List<JSONObject>> curtb, String classId,
			String toCourseIds, Integer day, Integer lesson) {
		// TODO Auto-generated method stub
		String rs = null;
		String cckey = day+","+lesson+","+classId;
		if(curtb!=null&&curtb.containsKey(cckey)
				){
			JSONObject obj = curtb.get(cckey).get(0);
			rs = obj.getString("Teachers");
		}
		return rs;
	}

	

	/**
	 * 单周不调双周课，双周不调单周课
	 * @param fromCourseTps
	 * @param toCourseTps
	 * @param week
	 * @return
	 */
	private static boolean JudgeNeedContinueByCourseType(
			String[] fromCourseTps, String[] toCourseTps, int week) {
		// TODO Auto-generated method stub
		if(week%2==0){
			//双周不调单周课
			if(isStrInArray("1", fromCourseTps)||isStrInArray("1", toCourseTps)){
				return true;
			}
		}else{
			//单周不调双周课
			if(isStrInArray("2", fromCourseTps)||isStrInArray("2", toCourseTps)){
				return true;
			}
		}
		return false;
	}

	/**
	 * 判断字符串是否在数组中
	 * @param str
	 * @param arr
	 * @return
	 */
	public static boolean isStrInArray(String str,String[] arr){
		for(int i=0;i<arr.length;i++){
			if(str.equalsIgnoreCase(arr[i])){
				return true;
			}
		}
		return false;
	}
	/**
	 * 判断字符串是否在数组中
	 * @param str
	 * @param arr
	 * @return
	 */
	public static boolean isStrInSet(String str,List<String> arr){
		for(int i=0;i<arr.size();i++){
			if(str.equalsIgnoreCase(arr.get(i))){
				return true;
			}
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	public static List<JSONObject> getTeachersByTask(List<JSONObject> taskList,
			JSONArray classCourse) {
		// TODO Auto-generated method stub
		String[] teaArr = new String[classCourse.size()];
		
//		List<String> teaSet = new ArrayList<String>();
		
		for(int i=0;i<classCourse.size();i++){
			JSONObject o = classCourse.getJSONObject(i);
			String courseId = o.getString("courseId");
			teaArr[i] = courseId;
		}
		
		List<JSONObject> rs = new ArrayList<JSONObject>();
		HashMap<String,Boolean> teaMaps = new HashMap<String, Boolean>();
		for(int i=0;i<taskList.size();i++){
			
			JSONObject o = taskList.get(i);
			
			String CourseId = o.getString("CourseId");
			String TeacherId = o.getString("TeacherId");
			if(isStrInArray(CourseId, teaArr)){
				if(teaMaps.isEmpty()||!teaMaps.containsKey(TeacherId)){
					
					JSONObject r = new JSONObject();
					r.put("teacherId", TeacherId);
					r.put("courseId", CourseId);
					teaMaps.put(TeacherId, true);
					rs.add(r);
				}
			}
		}
		
		
		return rs;
	}

	public static List<JSONObject> getNoConflictTeachers(List<JSONObject> teachers, JSONArray classCourse, int dayOfWeek, int lessonOfDay, HashMap<String, 
			List<JSONObject>> arrangeMap, RuleConflict ruleConflict, int unles,int breakRule){
		
		List<JSONObject> arlist = arrangeMap.get(dayOfWeek+","+lessonOfDay);
		for(JSONObject arrange:arlist){
			Map<String,String> nowtea  = (Map<String, String>) arrange.get("teachers");
			if(nowtea==null||nowtea.size()==0){
				continue;
			}
			for(int j=0;j<teachers.size();j++){
				JSONObject tea = teachers.get(j);
				String teacherId = tea.getString("teacherId");
				if(nowtea.containsKey(teacherId) ){
					tea.put("isConf", true);
				}
			}
		}
		
		for(int j=0;j<teachers.size();j++){
			JSONObject tea = teachers.get(j);
			String courseId = tea.getString("courseId");
			String teacherId = tea.getString("teacherId");
			
			for(int i=0;i<classCourse.size(); i++){
				JSONObject o = classCourse.getJSONObject(i);
				String classId = o.getString("classId");
				int courseType = o.getIntValue("courseType");
				Classroom cls = ruleConflict.getClassroomsDic().get(classId);
				Grade gd = ruleConflict.getGradeIdGradeDic().get(cls.getGradeId());
				
				String gradeLev = gd.getCurrentLevel().getValue()+"";
				
				Map<String,String> teaMap = new HashMap<String, String>();
				teaMap.put(teacherId, "");
				Set<String> teacherIds = teaMap.keySet();
				List<JSONObject> conf = new ArrayList<JSONObject>();
				if(breakRule==0){
					conf = getConflictsByPoint(classId, courseId, teacherIds, dayOfWeek, lessonOfDay, arrangeMap, ruleConflict, gradeLev, 0.0, 0, null, courseType,-1,teacherIds);
				}else{
					conf = getOnlyTeaConflictsByPoint(classId, courseId, teacherIds, dayOfWeek, lessonOfDay, arrangeMap, ruleConflict, gradeLev, 0.0, 0, null, courseType,-1,teacherIds);
				}
				if(conf.size()>0){
					tea.put("isConf", true);
					break;
				}
			}
		}
	
		List<JSONObject> rs = new ArrayList<JSONObject>();
		for(JSONObject tea:teachers){
			String teacherId = tea.getString("teacherId");
			boolean  isConf = tea.getBooleanValue("isConf");
			if(!isConf&&ruleConflict.getTeachersDic().containsKey(teacherId)){
				JSONObject o = new JSONObject();
				o.put("teacherId", teacherId);
				o.put("teacherName", ruleConflict.getTeachersDic().get(teacherId).getName());
				
				rs.add(o);
			}
		}
		
		return rs;
	}

	/**
	 * 生成需要记录的操作结果
	 * @param arrangeMap
	 * @param setCourseId
	 * @param fromDayOfWeek
	 * @param fromLessonOfDay
	 * @param fromCourseId
	 * @param fromTeachers
	 * @param toDayOfWeek
	 * @param toLessonOfDay
	 * @param toTeachers
	 * @param classId
	 * @param week
	 * @param weekOfStart
	 * @param weekOfEnd
	 * @param schoolId
	 * @param tempAjustList
	 * @param needInsertResult
	 * @param needDeleteResult
	 * @param gradeId
	 * @param timetableId
	 * @param fCourseType
	 * @param adjustType
	 * @param evryWeekTbAllNj
	 */
	public static void genNeedInsertAndDelResult(HashMap<String, List<JSONObject>> arrangeMap,
			String setCourseId,Integer fromDayOfWeek,
			Integer fromLessonOfDay, String fromCourseId,String fromTeachers,
			Integer toDayOfWeek,
			Integer toLessonOfDay,String toTeachers,
			String classId, int week, int weekOfStart,int weekOfEnd,
			String schoolId, List<JSONObject> tempAjustList,
			List<TCRCATemporaryAdjustResult> needInsertResult,
			List<TCRCATemporaryAdjustResult> needDeleteResult, String gradeId,
			String timetableId,String fCourseType,String adjustType,
			HashMap<Integer, HashMap<String, List<JSONObject>>> evryWeekTbAllNj,String groupId) {
		
		//插入调课记录
		int cType = Integer.parseInt(fCourseType);
		//单周课 双周不调整
		if(cType==1&&week%2==0){
			return;
		}
		//双周课 单周不调整
		if(cType==2&&week%2==1){
			return;
		}
		
		HashMap<String, List<JSONObject>> weeTb = evryWeekTbAllNj.get(week);
		if(weeTb!=null){
			String tkey = fromDayOfWeek+","+fromLessonOfDay+","+classId;
			List<JSONObject> targetPositionlist = weeTb.get(tkey);
			//判断当前位置是否含被调动课程
			if(targetPositionlist!=null){
				List<JSONObject> tmpList = GrepUtil.grepJsonKeyByVal
						(new String[]{"ClassId","CourseId"}, new String[]{classId,fromCourseId}, targetPositionlist);
				if(tmpList.size()==0){
					return;
				}
				
			}else{
				return;
			}
		}
		
		// 查找当前被移动的课程 是否在结果表中有调动记录
		List<JSONObject> ajustedList =  GrepUtil.grepJsonKeyByVal(
				new String[]{"DayOfWeek","LessonOfDay","ClassId","SubjectId","Week"}, 
				new String[]{fromDayOfWeek+"",fromLessonOfDay+"",classId,fromCourseId,week+""}, 
				tempAjustList);
		if(ajustedList.size()==0){
			if(adjustType.equalsIgnoreCase("-3")){
				return;
			}
			List<JSONObject> orKyList = arrangeMap.get(fromDayOfWeek+","+fromLessonOfDay+","+classId);
			List<JSONObject> orList = GrepUtil.grepJsonKeyBySingleVal("CourseId", fromCourseId, orKyList);
			JSONObject obj = null;
			if(orList!=null&&orList.size()>0){
				obj = orList.get(0);
			}
			//没有调动记录 直接记录数据
			TCRCATemporaryAdjustResult addtr = new TCRCATemporaryAdjustResult();
			addtr.setSchoolId(schoolId);
			addtr.setTimetableId(timetableId);
			addtr.setClassId(classId);
			addtr.setWeek(week);
			addtr.setGradeId(gradeId);
			addtr.setSubjectId(setCourseId);
			addtr.setDayOfWeek(toDayOfWeek);
			addtr.setLessonOfDay(toLessonOfDay);
			addtr.setFromDayOfWeek(fromDayOfWeek);
			addtr.setFromLessonOfDay(fromLessonOfDay);
			addtr.setFromSubjectId(fromCourseId);
			addtr.setCourseType(fCourseType);
			addtr.setGroupId(groupId);
			if(obj!=null){
				addtr.setFromTeachers(obj.getString("Teachers"));
				addtr.setCourseType(obj.getString("CourseType"));
				addtr.setMcGroupId(obj.getString("McGroupId"));
			}
			if(toTeachers!=null){
				addtr.setTeachers(toTeachers);
			}else{
				if(obj!=null){
					addtr.setTeachers(obj.getString("Teachers"));
				}
				if("3".equalsIgnoreCase(adjustType)){
					addtr.setTeachers("");
				}
			}
			if(adjustType.equalsIgnoreCase("-3")){
				addtr.setFromSubjectId(setCourseId);
			}
			int ajss = Integer.parseInt(adjustType);
			
			addtr.setAdjustType(Math.abs(ajss)+"");
			addtr.setWeekOfEnd(weekOfEnd+"");
			addtr.setWeekOfStart(weekOfStart); 
			
			needInsertResult.add(addtr);
		}else{
			//有调动记录 记录需删除的结果和需新增的调动结果
			JSONObject ajst = ajustedList.get(0);
			TCRCATemporaryAdjustResult deltr = new TCRCATemporaryAdjustResult();
			deltr.setSchoolId(schoolId);
			deltr.setTimetableId(timetableId);
			deltr.setClassId(ajst.getString("ClassId"));
			deltr.setWeek(week);
			deltr.setGradeId(gradeId);
			deltr.setSubjectId(fromCourseId);
			deltr.setDayOfWeek(fromDayOfWeek);
			deltr.setLessonOfDay(fromLessonOfDay);
			needDeleteResult.add(deltr);
			//需新增的调动结果
			TCRCATemporaryAdjustResult addtr = new TCRCATemporaryAdjustResult();
			addtr.setSchoolId(schoolId);
			addtr.setTimetableId(timetableId);
			addtr.setClassId(ajst.getString("ClassId"));
			addtr.setWeek(week);
			addtr.setGradeId(gradeId);
			addtr.setSubjectId(setCourseId);
			addtr.setDayOfWeek(toDayOfWeek);
			addtr.setLessonOfDay(toLessonOfDay);
			addtr.setFromDayOfWeek(ajst.getIntValue("FromDayOfWeek"));
			addtr.setFromLessonOfDay(ajst.getIntValue("FromLessonOfDay"));
			addtr.setFromSubjectId(ajst.getString("FromSubjectId"));
			addtr.setCourseType(ajst.getString("CourseType"));
			addtr.setMcGroupId(ajst.getString("McGroupId"));
			addtr.setGroupId(groupId);
			if(toTeachers!=null){
				addtr.setTeachers(toTeachers);
			}else{
				addtr.setTeachers(ajst.getString("Teachers"));
				if("-2".equalsIgnoreCase(adjustType)){
					
					addtr.setTeachers(ajst.getString("FromTeachers"));
				}
					
			}
			if("-3".equalsIgnoreCase(adjustType)){
				addtr.setTeachers(ajst.getString("FromTeachers"));
				addtr.setFromSubjectId(setCourseId);
			}
			if("3".equalsIgnoreCase(adjustType)){
				addtr.setTeachers("");
			}
			addtr.setFromTeachers(ajst.getString("FromTeachers"));
			if(Integer.parseInt(adjustType)>0){
				
				String wed = ajst.getString("WeekOfEnd");
//			String AdjustType = getAdjustTypeStrByAjst(ajst,adjustType);
				addtr.setAdjustType(ajst.getString("AdjustType")+","+adjustType);
				addtr.setWeekOfEnd(wed+","+weekOfEnd);
			}else{
				String wed = ajst.getString("WeekOfEnd");
				String ortype = ajst.getString("AdjustType");
				if(wed.indexOf(",")!=-1){
					
					String newEnd = wed.substring(0,wed.lastIndexOf(","));
					addtr.setWeekOfEnd(newEnd);
				}else{
					addtr.setWeekOfEnd("");
				}
				if(ortype.indexOf(",")!=-1){
					String newType = ortype.substring(0,ortype.lastIndexOf(","));
					addtr.setAdjustType(newType);
				}else{
					addtr.setAdjustType("");
				}
				
			}
			addtr.setWeekOfStart(weekOfStart); 
			//调动后与原课表相同 则删除该调动
			if(jugeIfLessonEqualsOr(addtr)){
				needDeleteResult.add(addtr);
			}else{
				needInsertResult.add(addtr);
			}
		}
	}
	
	private static boolean jugeIfLessonEqualsOr(TCRCATemporaryAdjustResult addtr) {
		// TODO Auto-generated method stub
		String AdjustType = addtr.getAdjustType();
		if(AdjustType==null){
			AdjustType = "";
		}
		if(addtr.getFromDayOfWeek()==addtr.getDayOfWeek()
					&&addtr.getFromLessonOfDay()==addtr.getLessonOfDay()
					&&addtr.getFromSubjectId().equalsIgnoreCase(addtr.getSubjectId())
					&&((addtr.getTeachers()==null&&addtr.getFromTeachers()==null)
							||(addtr.getTeachers()!=null&&addtr.getFromTeachers()!=null&&addtr.getTeachers().equalsIgnoreCase(addtr.getFromTeachers())))
					&&!isStrInArray("2", AdjustType.split(","))
					&&!isStrInArray("3", AdjustType.split(","))
					&&!isStrInArray("4", AdjustType.split(","))){
			
			return true;
		}
			
		return false;
	}

	private static String getAdjustTypeStrByAjst(JSONObject ajst, String adjustType) {
		// TODO Auto-generated method stub
		String AdjustType = ajst.getString("AdjustType");
		String[] tarr = AdjustType.split(",");
		int type = Integer.parseInt(adjustType);
		if(type>0){
			
//			if(!isStrInArray(adjustType, tarr)){
				if(AdjustType.trim().length()==0){
					AdjustType = adjustType;
				}else{
					AdjustType = adjustType+","+AdjustType;
				}
//			}
		}else{
			if(Math.abs(type)==1){
				
				AdjustType = AdjustType.replaceFirst(""+Math.abs(type), "");
			}else{
				AdjustType =  AdjustType.replace(""+Math.abs(type), "");
			}
		}
		return AdjustType;
	}

	/**
	 * @param fromDayOfWeek
	 * @param fromLessonOfDay
	 * @param fromCourseIds
	 * @param toDayOfWeek
	 * @param toLessonOfDay
	 * @param classId
	 * @param week
	 * @param weekOfEnd
	 * @param schoolId
	 * @param needInsertRecord
	 * @param gradeId
	 * @param toCourseIds
	 * @param timetableId
	 * @param recordOrNot
	 * @param fCourseType
	 * @throws NumberFormatException
	 */
	public static void genNeedInsertRecord(Integer fromDayOfWeek,
			Integer fromLessonOfDay, String fromCourseIds,String fromCourseTeachers ,
			Integer toDayOfWeek,
			Integer toLessonOfDay,String toCourseIds,String toCourseTeachers,
			String classId, int week,int weekOfStart, int weekOfEnd,
			String schoolId, List<TCRCATemporaryAdjustRecord> needInsertRecord,
			String gradeId,  String timetableId, String fCourseType,String ajustType)
			throws NumberFormatException {
		TCRCATemporaryAdjustRecord tr = new TCRCATemporaryAdjustRecord();
		tr.setAdjustType(ajustType);
		tr.setClassId(classId);
		tr.setGradeId(gradeId);
		tr.setOriginalDay(fromDayOfWeek);
		tr.setOriginalLesson(fromLessonOfDay);
		tr.setOriginalSubject(fromCourseIds);
		tr.setOriginalTeachers(fromCourseTeachers);
		tr.setRecordId(UUIDUtil.getUUID());
		tr.setRecordTime(DateUtil.getDateMilliFormat().substring(0,19));
		tr.setSchoolId(schoolId);
		tr.setTargetDay(toDayOfWeek);
		tr.setTargetLesson(toLessonOfDay);
		tr.setTargetSubject(toCourseIds);
		tr.setTargetTeachers(toCourseTeachers);
		tr.setTimetableId(timetableId);
		tr.setWeekOfEnd(weekOfEnd);
		tr.setWeekOfStart(weekOfStart);
		tr.setWeek(week);
		needInsertRecord.add(tr);
	}
	/**
	 * 设置代课教师--设置自习、设置公开课等操作
	 * @param dayOfWeek
	 * @param lessonOfDay
	 * @param week
	 * @param weekOfEnd
	 * @param ruleConflict
	 * @param schoolId
	 * @param timetableId
	 * @param tempAjustList
	 * @param classCourse 要设置的班级课程 
	 * @return
	 */
	public static JSONObject getNeedRecordAdjustedListBySet(
			HashMap<String, List<JSONObject>> arrangeMap,
			int dayOfWeek,int lessonOfDay, int week, int weekOfEnd,
			RuleConflict ruleConflict, String schoolId, String timetableId,
			List<JSONObject> tempAjustList, JSONArray classCourse,
			String fromTeachers,String toTeachers,String aType,String setCourseId,
			HashMap<Integer, HashMap<String, List<JSONObject>>> evryWeekTbAllNj ,String   groupId) {
		// TODO Auto-generated method stub
		
		List<TCRCATemporaryAdjustRecord> needInsertRecord = new ArrayList<TCRCATemporaryAdjustRecord>();
		List<TCRCATemporaryAdjustResult> needInsertResult = new ArrayList<TCRCATemporaryAdjustResult>();
		List<TCRCATemporaryAdjustResult> needDeleteResult = new ArrayList<TCRCATemporaryAdjustResult>();
		
		HashMap<String,List<JSONObject>> classCcMap = new HashMap<String,List<JSONObject>>();
		
		for(int i=0;i<classCourse.size();i++){
			JSONObject cc = classCourse.getJSONObject(i);
			String classId = cc.getString("classId");
			if(classCcMap.containsKey(classId)){
				List<JSONObject> list = classCcMap.get(classId);
				list.add(cc);
			}else{
				List<JSONObject> list = new ArrayList<JSONObject>();
				list.add(cc);
				classCcMap.put(classId, list);
			}
		}
		
		for(Iterator<String> it = classCcMap.keySet().iterator();it.hasNext();){
			String classId = it.next();
			List<JSONObject> list =  classCcMap.get(classId);
			String gradeId = ruleConflict.getGradeIdSynjMap().get(ruleConflict.getClassroomsDic().get(classId).getGradeId());
			String courseIds = "";
			List<String> relatedClass = new ArrayList<String>();
			for(JSONObject obj :list){
				courseIds += obj.getString("courseId")+",";
				String setCid = obj.getString("courseId");
				String fCourseType  = obj.getString("courseType");
				String McGroupId = obj.getString("McGroupId");
				List<String> mcids =  new ArrayList<String>();
				//取消合班的联动
//				if(McGroupId!=null&&McGroupId.trim().length()>0){
//					mcids = ruleConflict.getMergeClassByGroupId(McGroupId);
//				}
				if(mcids.size()==0 ){
					mcids.add(classId);
				}
				String setCourseId2 = setCourseId;
				if(setCourseId==null){
					setCourseId2 = setCid;
				}
				for(String _classId:mcids){
					//用于记录调课
					if(!relatedClass.contains(_classId)){
						relatedClass.add(_classId);
					}
					if(weekOfEnd==0){
						
						genNeedInsertAndDelResult(arrangeMap, setCourseId2, dayOfWeek, lessonOfDay, setCid, fromTeachers, 
								dayOfWeek, lessonOfDay, toTeachers, _classId, week, week, weekOfEnd, 
								schoolId, tempAjustList, needInsertResult, needDeleteResult, gradeId, 
								timetableId, fCourseType, aType,evryWeekTbAllNj, groupId);
					}else{
						for(int j=week;j<=weekOfEnd;j++){	
							genNeedInsertAndDelResult(arrangeMap, setCourseId2, dayOfWeek, lessonOfDay, setCid, fromTeachers, 
									dayOfWeek, lessonOfDay, toTeachers, _classId, j, week, weekOfEnd, 
									schoolId, tempAjustList, needInsertResult, needDeleteResult, gradeId, 
									timetableId, fCourseType, aType,evryWeekTbAllNj, groupId);
						}
					}
				}
				
			}
			if(courseIds.length()>0){
				courseIds = courseIds.substring(0,courseIds.length()-1);
			}
			if(setCourseId==null){
				setCourseId = courseIds;
			}
			List<TCRCATemporaryAdjustRecord> needInsertRecordC = new ArrayList<TCRCATemporaryAdjustRecord>();
			HashMap<Integer,List<String>> weekRelatedRd =  new HashMap<Integer, List<String>>();
			for(String _classId:relatedClass){
				if(weekOfEnd==0){
					List<TCRCATemporaryAdjustRecord> needInsertRecordtmp = new ArrayList<TCRCATemporaryAdjustRecord>();
					genNeedInsertRecord(dayOfWeek, lessonOfDay, courseIds, fromTeachers, dayOfWeek, lessonOfDay,
							setCourseId, toTeachers, _classId, week, week, weekOfEnd, schoolId, needInsertRecordtmp, gradeId, timetableId, "", aType);
					List<String> RelatedRd = new ArrayList<String>();
					if(weekRelatedRd.containsKey(week)){
						RelatedRd = weekRelatedRd.get(week);
					}else{
						weekRelatedRd.put(week, RelatedRd);
					}
					if(!RelatedRd.contains(needInsertRecordtmp.get(0).getRecordId())){
						RelatedRd.add(needInsertRecordtmp.get(0).getRecordId());
					}
					needInsertRecordC.addAll(needInsertRecordtmp);
				}else{
					for(int j=week;j<=weekOfEnd;j++){	
						List<TCRCATemporaryAdjustRecord> needInsertRecordtmp = new ArrayList<TCRCATemporaryAdjustRecord>();
						genNeedInsertRecord(dayOfWeek, lessonOfDay, courseIds, fromTeachers, dayOfWeek, lessonOfDay,
								setCourseId, toTeachers, _classId, j, week, weekOfEnd, schoolId, needInsertRecordtmp, gradeId, timetableId, "", aType);
						List<String> RelatedRd = new ArrayList<String>();
						if(weekRelatedRd.containsKey(j)){
							RelatedRd = weekRelatedRd.get(j);
						}else{
							weekRelatedRd.put(j, RelatedRd);
						}
						if(!RelatedRd.contains(needInsertRecordtmp.get(0).getRecordId())){
							RelatedRd.add(needInsertRecordtmp.get(0).getRecordId());
						}
						needInsertRecordC.addAll(needInsertRecordtmp);
					}
				}
			}
			//有合班的情况
			if(relatedClass.size()>1){
				for(TCRCATemporaryAdjustRecord tad:needInsertRecordC){
					int w = tad.getWeek();
					String rid  = tad.getRecordId();
					List<String> RelatedRd = weekRelatedRd.get(w);
					String relateRdIds = "";
					for(String rd:RelatedRd){
						if(!rd.equalsIgnoreCase(rid)){
							relateRdIds+=rd+",";
						}
					}
					if(relateRdIds.trim().length()>0){
						relateRdIds = relateRdIds.substring(0,relateRdIds.length()-1);
					}
					tad.setRelatedRecordIds(relateRdIds);
				}
			}
			needInsertRecord.addAll(needInsertRecordC);
		}
		for(TCRCATemporaryAdjustRecord record:needInsertRecord){
			record.setGroupId(groupId);
		}
		
		JSONObject rs = new JSONObject();
		rs.put("needInsertRecord", needInsertRecord);
		rs.put("needInsertResult", needInsertResult);
		rs.put("needDeleteResult", needDeleteResult);
		return rs;
	}


	/**
	 * 还原操作记录
	 * @param arrangeMap
	 * @param dayOfWeek
	 * @param lessonOfDay
	 * @param week
	 * @param weekOfEnd
	 * @param ruleConflict
	 * @param schoolId
	 * @param timetableId
	 * @param tempAjustList
	 * @param classCourse
	 * @param evryWeekTbAllNj
	 * @param needRecall 
	 * @param groupId 
	 * @return
	 */

	public static JSONObject getNeedRecordAdjustedListByDel(
			HashMap<String, List<JSONObject>> arrangeMap, int dayOfWeek,
			int lessonOfDay, int week, int weekOfEnd,
			RuleConflict ruleConflict, String schoolId, String timetableId,
			List<JSONObject> tempAjustList,List<JSONObject> tempAjustRecord, JSONArray classCourse,
			HashMap<Integer, HashMap<String, List<JSONObject>>> evryWeekTbAllNj,String selfStuCode,
			String groupId, JSONObject needRecall) {
		// TODO Auto-generated method stub
		List<TCRCATemporaryAdjustResult> needInsertResult = new ArrayList<TCRCATemporaryAdjustResult>();
		List<TCRCATemporaryAdjustResult> needDeleteResult = new ArrayList<TCRCATemporaryAdjustResult>();
		JSONObject rs = new JSONObject();
		List<String> needDelRecordIDs = new ArrayList<String>();
		if(groupId.equals("-100")){
			HashMap<String,List<JSONObject>> classCcMap = new HashMap<String,List<JSONObject>>();
			
			HashMap<String,Boolean> courseMap = new HashMap<String, Boolean>();
			for(int i=0;i<classCourse.size();i++){
				JSONObject cc = classCourse.getJSONObject(i);
				String classId = cc.getString("classId");
				String courseId  = cc.getString("courseId");
				if(classCcMap.containsKey(classId)){
					List<JSONObject> list = classCcMap.get(classId);
					list.add(cc);
				}else{
					List<JSONObject> list = new ArrayList<JSONObject>();
					list.add(cc);
					classCcMap.put(classId, list);
				}
				courseMap.put(dayOfWeek+","+lessonOfDay+","+classId+","+courseId, true);
			}
			
			
			for(Iterator<String> it = classCcMap.keySet().iterator();it.hasNext();){
				String classId = it.next();
				List<JSONObject> list =  classCcMap.get(classId);
				String gradeId = ruleConflict.getGradeIdSynjMap().get(ruleConflict.getClassroomsDic().get(classId).getGradeId());
				String courseIds = "";
				for(JSONObject obj :list){
					courseIds += obj.getString("courseId")+",";
					String setCid = obj.getString("courseId");
					String fCourseType  = obj.getString("courseType");
					if(weekOfEnd==0){
						
						genNeedInsertAndDelResultByDel(arrangeMap, dayOfWeek, lessonOfDay, setCid, 
								classId, week, week, weekOfEnd, 
								schoolId, tempAjustList, tempAjustRecord,needInsertResult, needDeleteResult,
								needDelRecordIDs, gradeId, timetableId, fCourseType,evryWeekTbAllNj,
								selfStuCode,ruleConflict);
					}else{
						for(int j=week;j<=weekOfEnd;j++){	
							genNeedInsertAndDelResultByDel(arrangeMap, dayOfWeek, lessonOfDay, setCid, 
									classId, j, week, weekOfEnd, 
									schoolId, tempAjustList,tempAjustRecord, needInsertResult, needDeleteResult,
									needDelRecordIDs, gradeId, timetableId, fCourseType,evryWeekTbAllNj, 
									selfStuCode,ruleConflict);
							
						}
					}
					
				}
				if(courseIds.length()>0){
					courseIds = courseIds.substring(0,courseIds.length()-1);
					
				}
					
			}
			
			
			
			needInsertResult = new ArrayList<TCRCATemporaryAdjustResult>();
			for(JSONObject tmp :tempAjustList){
				tmp = BeanTool.castBeanToFirstLowerKey(tmp);
				TCRCATemporaryAdjustResult tr = JSON.toJavaObject(tmp, TCRCATemporaryAdjustResult.class);
				if(!jugeIfLessonEqualsOr(tr)){
					String key = tr.getDayOfWeek()+","+tr.getLessonOfDay()+","+tr.getClassId()+","+tr.getSubjectId();
					if( !courseMap.containsKey(key)||tr.getWeek()<week){
						
						needInsertResult.add(tr);
					}
				}
				
			}
		}else{
			weekOfEnd = needRecall.getIntValue("WeekOfEnd");
			JSONObject recordRecall = BeanTool.castBeanToFirstLowerKey(needRecall);
			TCRCATemporaryAdjustRecord tcrRecall = JSON.toJavaObject(recordRecall, TCRCATemporaryAdjustRecord.class);
			
			List<JSONObject> groupList = new ArrayList<JSONObject>();
			for(  JSONObject rt: tempAjustRecord){
				JSONObject record = BeanTool.castBeanToFirstLowerKey(rt);
				TCRCATemporaryAdjustRecord tcr = JSON.toJavaObject(record, TCRCATemporaryAdjustRecord.class);
				if(tcr.getGroupId().equals(tcrRecall.getGroupId())
//						&&tcr.getClassId().equals(tcrRecall.getClassId())
//						&&tcr.getTargetDay()==tcrRecall.getTargetDay()
//						&&tcr.getTargetLesson()==tcrRecall.getTargetLesson()
//						&&tcr.getOriginalDay()==tcrRecall.getOriginalDay()
//						&&tcr.getOriginalLesson()==tcrRecall.getOriginalLesson()
//						&&tcr.getOriginalSubject().equals(tcrRecall.getOriginalSubject())
						&&tcr.getAdjustType().equals(tcrRecall.getAdjustType())
						&&tcr.getWeek()>=week
						&&tcr.getStep()==tcrRecall.getStep()
						){
					int ajstType  = Integer.parseInt(tcr.getAdjustType());
					String[] fromCids = tcr.getOriginalSubject().split(",");
					String[] targetCids = tcr.getTargetSubject().split(",");
					String fClassId = tcr.getClassId();
					int fromDayOfWeek = tcr.getOriginalDay();
					int fromLessonOfDay = tcr.getOriginalLesson();
					int targetDayOfWeek = tcr.getTargetDay();
					int targetLessonOfDay = tcr.getTargetLesson();
					String fromTeachers = tcr.getOriginalTeachers();
					String toTeachers = tcr.getTargetTeachers();
					needDelRecordIDs.add(tcr.getRecordId());
					dealWithRecords(arrangeMap, tcr.getWeek(),  tcr.getWeek(), schoolId,
							tempAjustList, tcr.getGradeId(), timetableId, "0",
							evryWeekTbAllNj, selfStuCode, ajstType,  fromCids,
							targetCids, tcr.getClassId(), fromDayOfWeek, fromLessonOfDay,
							targetDayOfWeek, targetLessonOfDay, fromTeachers, toTeachers, groupId);
					evryWeekTbAllNj = getEvryWeekTimeTableByParam(arrangeMap, tempAjustList, week, weekOfEnd, ruleConflict);
//					if(evryWeekTbAllNj.containsKey(week)){
//						
//						List<JSONObject> arrangeNowAll =  changeMapListToList(evryWeekTbAllNj.get(week));
//						List<JSONObject> temp = GrepUtil.grepJsonKeyBySingleVal("GroupId", groupId, arrangeNowAll);
//						groupList.addAll(temp);
//					}
				}
			}
//			needInsertResult = new ArrayList<TCRCATemporaryAdjustResult>();
			for(JSONObject tmp :tempAjustList){
				tmp = BeanTool.castBeanToFirstLowerKey(tmp);
				TCRCATemporaryAdjustResult tr = JSON.toJavaObject(tmp, TCRCATemporaryAdjustResult.class);
				tr.setSchoolId(schoolId);
//				if(!jugeIfLessonEqualsOr(tr)){
				if(tr.getGroupId()!=null&&tr.getGroupId().equals(groupId)
//						&&tr.getWeek()<=tcrRecall.getWeek()
						 ){
//					if(  tr.getWeek()<week){
						
						needInsertResult.add(tr);
//					}
				}
				
			}
			
		}
		
		rs.put("needInsertResult", needInsertResult);
		rs.put("needDelRecordIDs", needDelRecordIDs);
		return rs;
	}

	/**
	 * 撤销操作--生成记录
	 * @param arrangeMap
	 * @param dayOfWeek
	 * @param lessonOfDay
	 * @param setCid
	 * @param dayOfWeek2
	 * @param lessonOfDay2
	 * @param classId
	 * @param week
	 * @param weeOfStart
	 * @param weekOfEnd
	 * @param schoolId
	 * @param tempAjustList
	 * @param tempAjustRecord
	 * @param needInsertResult
	 * @param needDeleteResult
	 * @param gradeId
	 * @param timetableId
	 * @param fCourseType
	 * @param evryWeekTbAllNj
	 */
	private static void genNeedInsertAndDelResultByDel(
			HashMap<String, List<JSONObject>> arrangeMap, int dayOfWeek,
			int lessonOfDay, String setCid, 
			String classId, int week, int weeOfStart, int weekOfEnd,
			String schoolId, List<JSONObject> tempAjustList,
			List<JSONObject> tempAjustRecord,
			List<TCRCATemporaryAdjustResult> needInsertResult,
			List<TCRCATemporaryAdjustResult> needDeleteResult,List<String> needDelRecordIDs, String gradeId,
			String timetableId, String fCourseType,
			HashMap<Integer, HashMap<String, List<JSONObject>>> evryWeekTbAllNj,String selfStuCode,RuleConflict ruleConflict) {
		// //插入调课记录
		if(fCourseType==null ){
			fCourseType = "0";
		}
		int cType = Integer.parseInt(fCourseType);
		//单周课 双周不调整
		if(cType==1&&week%2==0){
			return;
		}
		//双周课 单周不调整
		if(cType==2&&week%2==1){
			return;
		}
		
		HashMap<String, List<JSONObject>> weeTb = evryWeekTbAllNj.get(week);
		if(weeTb!=null){
			String tkey = dayOfWeek+","+lessonOfDay+","+classId;
			List<JSONObject> targetPositionlist = weeTb.get(tkey);
			//判断当前位置是否含被调动课程
			if(targetPositionlist!=null){
				List<JSONObject> tmpList = GrepUtil.grepJsonKeyByVal
						(new String[]{"ClassId","CourseId"}, new String[]{classId,setCid}, targetPositionlist);
				if(tmpList.size()==0){
					return;
				}
			}else{
				return;
			}
		}
		HashMap<String,TCRCATemporaryAdjustRecord> idRecordMap = new HashMap<String, TCRCATemporaryAdjustRecord>();
		for(JSONObject rd:tempAjustRecord ){
			JSONObject record  = BeanTool.castBeanToFirstLowerKey(rd);
			TCRCATemporaryAdjustRecord tcr = JSON.toJavaObject(record, TCRCATemporaryAdjustRecord.class);
			idRecordMap.put(tcr.getRecordId(), tcr);
		}
		
		// 查找当前被移动的课程 是否在结果表中有调动记录
		List<JSONObject> ajustedList =  GrepUtil.grepJsonKeyByVal(
				new String[]{"DayOfWeek","LessonOfDay","ClassId","SubjectId","Week"}, 
				new String[]{dayOfWeek+"",lessonOfDay+"",classId,setCid,week+""}, 
				tempAjustList);
		//原始课程id
		String fcid = setCid;
		int FromDayOfWeek = 0;
		int FromLessonOfDay = 0;
		String FromTeachers = "";
		List<String> relatedResult = new ArrayList<String>();
		if(ajustedList.size()!=0){
			JSONObject ajst = ajustedList.get(0);
			fcid = ajst.getString("FromSubjectId");
			FromDayOfWeek = ajst.getIntValue("FromDayOfWeek");
			FromLessonOfDay = ajst.getIntValue("FromLessonOfDay");
			FromTeachers = ajst.getString("FromTeachers");
			String relatedstr =  dayOfWeek+","+ lessonOfDay+","+fcid;
			relatedResult.add(relatedstr);
			fillRelatedLessonsBySwap(relatedResult,ajst,tempAjustList,classId,week+"");
		}else{
			return;
		}
		JSONObject orLesson = null;
		if(arrangeMap.containsKey(FromDayOfWeek+","+FromLessonOfDay+","+classId)){
			List<JSONObject> listRs = new ArrayList<JSONObject>();
			List<JSONObject> list = arrangeMap.get(FromDayOfWeek+","+FromLessonOfDay+","+classId);
			if(fcid.indexOf(",")!=-1){
				String[] fcids = fcid.split(",");
				for(int i=0;i<fcids.length;i++){
					
					listRs.addAll(GrepUtil.grepJsonKeyByVal
							(new String[]{"ClassId","CourseId"}, new String[]{classId,fcid}, list));
				}
			}else{
				
				listRs = GrepUtil.grepJsonKeyByVal
						(new String[]{"ClassId","CourseId"}, new String[]{classId,fcid}, list);
			}
			if(listRs.size()>0){
				orLesson = listRs.get(0);
				
			}
		}
		tempAjustRecord =  GrepUtil.grepJsonKeyByVal(
				new String[]{"ClassId","Week"}, 
				new String[]{ classId,week+""}, 
				tempAjustRecord);
		int index = tempAjustRecord.size();
		List<String> relateSubjectIds = new ArrayList<String>();
		relateSubjectIds.add(fcid);
		int relateDay =dayOfWeek ;
		int relateLesson = lessonOfDay;
		boolean first = true;
		while(index>0&&needContinueDelRecord(dayOfWeek,lessonOfDay,classId,fcid,week,tempAjustList,orLesson,needDeleteResult,first)){
			index --;
			first = false;
			JSONObject record = tempAjustRecord.get( tempAjustRecord.size()-index-1);
			record = BeanTool.castBeanToFirstLowerKey(record);
			TCRCATemporaryAdjustRecord tcr = JSON.toJavaObject(record, TCRCATemporaryAdjustRecord.class);
			
			
			int ajstType  = Integer.parseInt(tcr.getAdjustType());
			String[] fromCids = tcr.getOriginalSubject().split(",");
			String[] targetCids = tcr.getTargetSubject().split(",");
			String fromTeachers = tcr.getOriginalTeachers();
			String toTeachers = tcr.getTargetTeachers();
			String fClassId = tcr.getClassId();
			String relatedReids = tcr.getRelatedRecordIds();
			int fromDayOfWeek = tcr.getOriginalDay();
			int fromLessonOfDay = tcr.getOriginalLesson();
			int targetDayOfWeek = tcr.getTargetDay();
			int targetLessonOfDay = tcr.getTargetLesson();
			needDelRecordIDs.add(tcr.getRecordId());
			if(!jugeIdsRelated(relateSubjectIds,targetCids,relateDay,relateLesson,targetDayOfWeek,
					targetLessonOfDay,ajstType,fromDayOfWeek,fromLessonOfDay,fromCids,relatedResult)){
				continue;
			}
			relateSubjectIds = Arrays.asList(fromCids);
			relateDay = fromDayOfWeek;
			relateLesson = fromLessonOfDay;
			
			dealWithRecords(arrangeMap, week, weekOfEnd, schoolId,
					tempAjustList, gradeId, timetableId, fCourseType,
					evryWeekTbAllNj, selfStuCode, ajstType, fromCids,
					targetCids, fClassId, fromDayOfWeek, fromLessonOfDay,
					targetDayOfWeek, targetLessonOfDay,null,null,"-100");
			
			if(relatedReids!=null&&relatedReids.length()>0){
				String[] rarrs = relatedReids.split(",");
				for(int i=0;i<rarrs.length;i++){
					String rid = rarrs[i];
					if(idRecordMap.containsKey(rid)){
						TCRCATemporaryAdjustRecord rtcr = idRecordMap.get(rid);
						dealWithRecords(arrangeMap, week, weekOfEnd, schoolId,
								tempAjustList, rtcr.getGradeId(), timetableId, fCourseType,
								evryWeekTbAllNj, selfStuCode,Integer.parseInt(rtcr.getAdjustType())
								, rtcr.getOriginalSubject().split(","),
								rtcr.getTargetSubject().split(","), rtcr.getClassId(),
								rtcr.getOriginalDay(), rtcr.getOriginalLesson(),
								rtcr.getTargetDay(), rtcr.getTargetLesson(),null,null,"-100");
					}
				}
			}
			
			evryWeekTbAllNj = getEvryWeekTimeTableByParam(arrangeMap, tempAjustList, week, weekOfEnd, ruleConflict);
		}
		
		List<JSONObject> ajustedList2 =  GrepUtil.grepJsonKeyByVal(
				new String[]{"DayOfWeek","LessonOfDay","ClassId",  "Week"}, 
				new String[]{dayOfWeek+"",lessonOfDay+"",classId,  week+""}, 
				tempAjustList);
		if(ajustedList2.size()>0){
			List<TCRCATemporaryAdjustResult> needInsertResultTmp = new ArrayList<TCRCATemporaryAdjustResult>();
			List<TCRCATemporaryAdjustResult> needDeleteResultTmp = new ArrayList<TCRCATemporaryAdjustResult>();
			for(JSONObject tmp :ajustedList2){
				tmp = BeanTool.castBeanToFirstLowerKey(tmp);
				TCRCATemporaryAdjustResult tr = JSON.toJavaObject(tmp, TCRCATemporaryAdjustResult.class);
				needDeleteResultTmp.add(tr);
			}
			dealWithListByList(needInsertResultTmp,needDeleteResultTmp,tempAjustList);
		}
		
	}

	/**
	 * @param arrangeMap
	 * @param week
	 * @param weekOfEnd
	 * @param schoolId
	 * @param tempAjustList
	 * @param gradeId
	 * @param timetableId
	 * @param fCourseType
	 * @param evryWeekTbAllNj
	 * @param selfStuCode
	 * @param ajstType
	 * @param fromCids
	 * @param targetCids
	 * @param fClassId
	 * @param fromDayOfWeek
	 * @param fromLessonOfDay
	 * @param targetDayOfWeek
	 * @param targetLessonOfDay
	 */
	public static void dealWithRecords(
			HashMap<String, List<JSONObject>> arrangeMap,
			int week,
			int weekOfEnd,
			String schoolId,
			List<JSONObject> tempAjustList,
			String gradeId,
			String timetableId,
			String fCourseType,
			HashMap<Integer, HashMap<String, List<JSONObject>>> evryWeekTbAllNj,
			String selfStuCode, int ajstType, String[] fromCids,
			String[] targetCids, String fClassId, int fromDayOfWeek,
			int fromLessonOfDay, int targetDayOfWeek, int targetLessonOfDay,
			String fromTeachers,String targetTeachers,String groupId) {
		//最后处理对调记录
		switch (ajstType) {
		//对调
		case 1:
			for(int j=0;j<fromCids.length;j++){
				String fromCourseId = fromCids[j];
				if(fromCourseId.trim().length()==0){
					continue;
				}
				List<TCRCATemporaryAdjustResult> needInsertResultTmp = new ArrayList<TCRCATemporaryAdjustResult>();
				List<TCRCATemporaryAdjustResult> needDeleteResultTmp = new ArrayList<TCRCATemporaryAdjustResult>();
				genNeedInsertAndDelResult(arrangeMap, fromCourseId, targetDayOfWeek, targetLessonOfDay, fromCourseId,
						fromTeachers, fromDayOfWeek, fromLessonOfDay,fromTeachers , fClassId, week, 
						week, weekOfEnd, schoolId, tempAjustList, needInsertResultTmp, needDeleteResultTmp,
						gradeId, timetableId, fCourseType, "-1", evryWeekTbAllNj, groupId);
				dealWithListByList(needInsertResultTmp,needDeleteResultTmp,tempAjustList);
				
			}
			for(int j=0;j<targetCids.length;j++){
				String targetCourseId = targetCids[j];
				if(targetCourseId.trim().length()==0){
					continue;
				}
				List<TCRCATemporaryAdjustResult> needInsertResultTmp = new ArrayList<TCRCATemporaryAdjustResult>();
				List<TCRCATemporaryAdjustResult> needDeleteResultTmp = new ArrayList<TCRCATemporaryAdjustResult>();
				genNeedInsertAndDelResult(arrangeMap, targetCourseId, fromDayOfWeek, fromLessonOfDay, targetCourseId,
						targetTeachers, targetDayOfWeek, targetLessonOfDay,targetTeachers , fClassId, week, 
						week, weekOfEnd, schoolId, tempAjustList, needInsertResultTmp, needDeleteResultTmp,
						gradeId, timetableId, fCourseType, "-1", evryWeekTbAllNj, groupId);
				
				dealWithListByList(needInsertResultTmp,needDeleteResultTmp,tempAjustList);
			}
			
			
			break;
		//设置代课老师
		case 2:
			for(int j=0;j<targetCids.length;j++){
				String fromCourseId = targetCids[j];
				if(fromCourseId.trim().length()==0){
					continue;
				}
				List<TCRCATemporaryAdjustResult> needInsertResultTmp = new ArrayList<TCRCATemporaryAdjustResult>();
				List<TCRCATemporaryAdjustResult> needDeleteResultTmp = new ArrayList<TCRCATemporaryAdjustResult>();
				genNeedInsertAndDelResult(arrangeMap, fromCourseId, targetDayOfWeek, targetLessonOfDay, fromCourseId,
						null, targetDayOfWeek, targetLessonOfDay,null , fClassId, week, 
						week, weekOfEnd, schoolId, tempAjustList, needInsertResultTmp, needDeleteResultTmp,
						gradeId, timetableId, fCourseType, "-2", evryWeekTbAllNj, groupId);
				
				dealWithListByList(needInsertResultTmp,needDeleteResultTmp,tempAjustList);
			}
			break;
		//设置自习
		case 3:
			for(int j=0;j<fromCids.length;j++){
				String fromCourseId = fromCids[j];
				if(fromCourseId.trim().length()==0){
					continue;
				}
				List<TCRCATemporaryAdjustResult> needInsertResultTmp = new ArrayList<TCRCATemporaryAdjustResult>();
				List<TCRCATemporaryAdjustResult> needDeleteResultTmp = new ArrayList<TCRCATemporaryAdjustResult>();
				genNeedInsertAndDelResult(arrangeMap, fromCourseId, targetDayOfWeek, targetLessonOfDay, selfStuCode,
						null, targetDayOfWeek, targetLessonOfDay,null , fClassId, week, 
						week, weekOfEnd, schoolId, tempAjustList, needInsertResultTmp, needDeleteResultTmp,
						gradeId, timetableId, fCourseType, "-3", evryWeekTbAllNj, groupId);
				
				dealWithListByList(needInsertResultTmp,needDeleteResultTmp,tempAjustList);
			}
			break;
		//设置公开课
		case 4:
			for(int j=0;j<targetCids.length;j++){
				String fromCourseId = targetCids[j];
				if(fromCourseId.trim().length()==0){
					continue;
				}
				List<TCRCATemporaryAdjustResult> needInsertResultTmp = new ArrayList<TCRCATemporaryAdjustResult>();
				List<TCRCATemporaryAdjustResult> needDeleteResultTmp = new ArrayList<TCRCATemporaryAdjustResult>();
				genNeedInsertAndDelResult(arrangeMap, fromCourseId, targetDayOfWeek, targetLessonOfDay, fromCourseId,
						null, targetDayOfWeek, targetLessonOfDay,null , fClassId, week, 
						week, weekOfEnd, schoolId, tempAjustList, needInsertResultTmp, needDeleteResultTmp,
						gradeId, timetableId, fCourseType, "-4", evryWeekTbAllNj, groupId);
				
				dealWithListByList(needInsertResultTmp,needDeleteResultTmp,tempAjustList);
			}
			break;
		default:
			break;
		}
	}

	private static void fillRelatedLessonsBySwap(List<String> relatedResult,
			JSONObject ajst, List<JSONObject> tempAjustList, String classId, String week) {
		// TODO Auto-generated method stub
//		String fcid = ajst.getString("FromSubjectId");
		int FromDayOfWeek = ajst.getIntValue("FromDayOfWeek");
		int FromLessonOfDay = ajst.getIntValue("FromLessonOfDay");
		List<JSONObject> ajustedList =  GrepUtil.grepJsonKeyByVal(
				new String[]{"DayOfWeek","LessonOfDay","ClassId","Week"}, 
				new String[]{FromDayOfWeek+"",FromLessonOfDay+"",classId,week }, 
				tempAjustList);
		if(ajustedList.size()!=0){
			JSONObject ajstNow = ajustedList.get(0);
			String fcid = ajstNow.getString("FromSubjectId");
			FromDayOfWeek = ajstNow.getIntValue("DayOfWeek");
			FromLessonOfDay = ajstNow.getIntValue("LessonOfDay");
			
			String relatedstr = FromDayOfWeek+","+FromLessonOfDay+","+fcid;
			if(!relatedResult.contains(relatedstr)){
				
				relatedResult.add(relatedstr);
				fillRelatedLessonsBySwap(relatedResult,ajstNow,tempAjustList,classId,week);
			}
		}
	}

	/**
	 * 判断操作课程是否相关
	 * @param relateSubjectIds
	 * @param tarCids
	 * @param targetDayOfWeek2 
	 * @param targetDayOfWeek 
	 * @param relateLesson 
	 * @param relateDay 
	 * @param fromLessonOfDay 
	 * @param fromDayOfWeek 
	 * @param ajstType 
	 * @param relatedResult 
	 * @param fromCids2 
	 * @return
	 */
	private static boolean jugeIdsRelated(List<String> relateSubjectIds,
			String[] tarCids, int relateDay, int relateLesson, int targetDayOfWeek, int targetLesson, int ajstType, int fromDayOfWeek, int fromLessonOfDay, String[] fromCids, List<String> relatedResult) {
		// TODO Auto-generated method stub
		if(ajstType!=1){
			
			if(relateDay!=targetDayOfWeek||relateLesson!=targetLesson){
				return false;
			}
		}else{
			for(int i=0;i<tarCids.length;i++){
				String key1 = fromDayOfWeek+","+fromLessonOfDay+","+tarCids[i];
				if(relatedResult.contains(key1)){
					return true;
				}
			}
			for(int i=0;i<fromCids.length;i++){
				String key1 = targetDayOfWeek+","+targetLesson +","+fromCids[i];
				if(relatedResult.contains(key1)){
					return true;
				}
			}
			
			return false;
		}
		boolean contain = false;
		if(ajstType==1||ajstType==3){
			
			for(int i=0;i<fromCids.length;i++){
				if(relateSubjectIds.contains(fromCids[i])){
					contain = true;
					break;
				}
			}
		}
		for(int i=0;i<tarCids.length;i++){
			if(relateSubjectIds.contains(tarCids[i])){
				contain = true;
				break;
			}
		}
		return contain;
	}

	/**
	 * 处理操作记录
	 * @param needInsertResultTmp
	 * @param needDeleteResultTmp	临时操作记录
	 * @param tempAjustList	记录操作后课表
	 */
	private static void dealWithListByList(
			List<TCRCATemporaryAdjustResult> needInsertResultTmp,
			List<TCRCATemporaryAdjustResult> needDeleteResultTmp,
			List<JSONObject> tempAjustList) {
		// TODO Auto-generated method stub
		if(needDeleteResultTmp.size()>0){
			for(TCRCATemporaryAdjustResult tar :needDeleteResultTmp){
//				needDeleteResult.add(tar);
				
				List<JSONObject> needDelList =  GrepUtil.grepJsonKeyByVal(
						new String[]{"DayOfWeek","LessonOfDay","ClassId","SubjectId","Week"}, 
						new String[]{tar.getDayOfWeek()+"",tar.getLessonOfDay()+"",tar.getClassId(),tar.getSubjectId(),tar.getWeek()+""}, 
						tempAjustList);
				
				tempAjustList.removeAll(needDelList);
				
			}
		}
		if(needInsertResultTmp.size()>0){
			for(TCRCATemporaryAdjustResult tar :needInsertResultTmp){
				JSONObject needAdd = (JSONObject) JSON.toJSON(tar);
				needAdd = BeanTool.castBeanToFirstHigherKey(needAdd);
				tempAjustList.add(needAdd);
			}
		}
	}

	/**
	 * 是否需要继续删除操作记录 从而还原最初课表
	 * @param dayOfWeek
	 * @param lessonOfDay
	 * @param classId
	 * @param setCid
	 * @param week
	 * @param tempAjustList
	 * @param orLesson
	 * @param needDeleteResult 
	 * @param first 
	 * @return
	 */
	private static boolean needContinueDelRecord(int dayOfWeek,
			int lessonOfDay, String classId, String setCid, int week,
			List<JSONObject> tempAjustList, JSONObject orLesson, List<TCRCATemporaryAdjustResult> needDeleteResult, boolean first) {
		// TODO Auto-generated method stub
		if(orLesson==null){
			return true;
		}
		List<JSONObject> ajustedList =  GrepUtil.grepJsonKeyByVal(
				new String[]{"FromDayOfWeek","FromLessonOfDay","ClassId","FromSubjectId","Week"}, 
				new String[]{orLesson.getString("DayOfWeek"),orLesson.getString("LessonOfDay")+"",classId,setCid,week+""}, 
				tempAjustList);
		if(ajustedList.size()==0 ){
			return false;
		}
		

		//有调动记录 记录需删除的结果和需新增的调动结果
		JSONObject ajst = ajustedList.get(0);
		JSONObject adtr = BeanTool.castBeanToFirstLowerKey(ajst);
		TCRCATemporaryAdjustResult addtr = JSON.toJavaObject(adtr, TCRCATemporaryAdjustResult.class);
		if(jugeIfLessonEqualsOr(addtr)){
			needDeleteResult.add(addtr);
			ajustedList.remove(ajst);
			return false;
		}else{
			return true;
		}
	}

	/**
	 * 获取调课记录
	 * @param tempAjustList 调课记录表
	 * @param week	周次
	 * @param type	1按班级 2按教师
	 * @param ruleConflict 
	 * @param weekDates 
	 * @param arrangeMap 
	 * @param tempAjustRecord 
	 * @return
	 * @throws Exception 
	 */
	public static List<JSONObject> getAdjustRecordData(
			List<JSONObject> tempAjustList, int week, int astType, 
			RuleConflict ruleConflict, String[] weekDates, HashMap<String, List<JSONObject>> arrangeMap,
			List<JSONObject> tempAjustRecord) throws Exception {
		String[] daydic = new String[]{"星期一","星期二","星期三","星期四","星期五","星期六","星期日"};
		String[] lessondic = new String[]{"第一节","第二节","第三节","第四节","第五节","第六节","第七节","第八节"
				,"第九节","第十节","第十一节","第十二节","第十三节","第十四节","第十五节"};
		// TODO Auto-generated method stub
		
		List<JSONObject> needAdd = new  ArrayList<JSONObject>();
		if(tempAjustRecord.size()>0){
			for(JSONObject tmp:tempAjustRecord){
				int ast = tmp.getIntValue("AdjustType");
				
				tmp.put("SubjectId", tmp.get("TargetSubject"));
				tmp.put("DayOfWeek", tmp.get("TargetDay"));
				tmp.put("LessonOfDay", tmp.get("TargetLesson"));
				tmp.put("Teachers", tmp.get("TargetTeachers"));
				
				tmp.put("FromLessonOfDay",tmp.get("OriginalLesson"));
				tmp.put("FromDayOfWeek",tmp.get("OriginalDay"));
				tmp.put("FromSubjectId",tmp.get("TargetSubject"));
				tmp.put("FromTeachers",tmp.get("TargetTeachers"));
				
				String identyKey = 1+"_"+ast+"_" + tmp.getString("GroupId")+"_"+tmp.getString("ClassId")+"_"
						+tmp.getIntValue("Step")+"_"+tmp.getIntValue("WeekOfEnd")+"_"+week;
				
				tmp.put("identyKey", identyKey);
				if(!tmp.get("TargetSubject").equals(tmp.get("OriginalSubject"))&&
						!(tmp.getInteger("TargetDay")==tmp.getInteger("OriginalDay")
								&&tmp.getIntValue("OriginalLesson")==tmp.getIntValue("TargetLesson"))){
					JSONObject tmp2 = new JSONObject();
					
					tmp2.put("identyKey", identyKey);
					tmp2.put("SubjectId", tmp.get("OriginalSubject"));
					tmp2.put("DayOfWeek",  tmp.get("OriginalDay"));
					tmp2.put("LessonOfDay",tmp.get("OriginalLesson"));
					tmp2.put("Teachers", tmp.get("OriginalTeachers"));
					
					tmp2.put("FromLessonOfDay", tmp.get("TargetLesson"));
					tmp2.put("FromDayOfWeek",tmp.get("TargetDay"));
					tmp2.put("FromSubjectId",tmp.get("OriginalSubject") );
					tmp2.put("FromTeachers",tmp.get("OriginalTeachers"));
					
					
					tmp2.put("OriginalSubject",tmp.get("TargetSubject") );
					tmp2.put("OriginalTeachers",tmp.get("TargetTeachers"));
					tmp2.put("WeekOfEnd",tmp.get("WeekOfEnd"));
					tmp2.put("AdjustType",tmp.get("AdjustType"));
					tmp2.put("ClassId",tmp.get("ClassId"));
					tmp2.put("PublishTeachers",tmp.get("PublishTeachers"));
					tmp2.put("Published",tmp.get("Published"));
					needAdd.add(tmp2);
				}
			}
		}
		tempAjustList.addAll(tempAjustRecord);
		tempAjustList.addAll(needAdd);
		HashMap<String,JSONObject> orTbByClass = new HashMap<String, JSONObject>();
		HashMap<String,JSONObject> orTbByTeacher = new HashMap<String, JSONObject>();
		Map<String, Account> techerDic = ruleConflict.getTeachersDic();
		for(JSONObject tmp :tempAjustList){
			tmp.put("classId", tmp.get("ClassId"));
			tmp.put("className", tmp.get("ClassId"));
			tmp.put("courseId", tmp.get("SubjectId"));
			tmp.put("dayOfWeek", tmp.get("DayOfWeek"));
			tmp.put("lessonOfDay", tmp.get("LessonOfDay"));
			int dayOfWeek = tmp.getIntValue("dayOfWeek");
			int fromDayOfWeek = tmp.getIntValue("FromDayOfWeek");
			int lessonOfDay = tmp.getIntValue("lessonOfDay");
			int fromLessonOfDay = tmp.getIntValue("FromLessonOfDay");
			String classId = tmp.getString("classId");
			String courseId = tmp.getString("SubjectId");
			String fromCourseId = tmp.getString("FromSubjectId");
			if(!tmp.containsKey("identyKey")){
				String identyKey = 2+"_" +tmp.getString("AdjustType")+"_"+
						tmp.getString("GroupId")+"_"+tmp.getString("ClassId")+"_"
						+tmp.getString("SubjectId")+"_"+dayOfWeek+"_"+lessonOfDay+"_"+week;
				tmp.put("identyKey", identyKey);
			}
			
			List<JSONObject> curTbList =
					arrangeMap.get(dayOfWeek+","+lessonOfDay+","+classId);
			int AdjustType = 0;
			if(tmp.getString("AdjustType").indexOf(",")==-1){
				AdjustType = tmp.getIntValue("AdjustType");
			}
			
			Classroom clas = ruleConflict.getClassroomsDic().get(classId);
			String className = "";
			if(clas!=null){
				className =clas.getClassName();
			}
					
			LessonInfo course = ruleConflict.getCoursesDic().get(courseId);
			String courseName = "";
			if(course!=null){
				courseName = course.getName();
			}
			
			LessonInfo fCourse = ruleConflict.getCoursesDic().get(fromCourseId);
			String fromCourseName = "";
			if(fCourse!=null){
				fromCourseName = fCourse.getName();
			}
			tmp.put("className", className);
			String curCourseName = "无";
			String orTeachers  = "";
			if(curTbList!=null&&curTbList.size()>0){
				curCourseName = "";
				for(JSONObject obj:curTbList){
					int courseType = obj.getIntValue("CourseType");
					if(week!=0){
						//双周时去单周课
						if(week%2==0&&courseType==1){
							continue;
						}
						//单周时去双周课
						if(week%2==1&&courseType==2){
							continue;
						}
					}
					String curcourseId = obj.getString("CourseId");
					LessonInfo ccourse = ruleConflict.getCoursesDic().get(curcourseId);
					if(ccourse!=null){
						curCourseName += ccourse.getName() +",";
					}
					
					orTeachers += obj.getString("Teachers")+",";
				}
				
				if(curCourseName.trim().length()>0){
					curCourseName = curCourseName.substring(0,curCourseName.length()-1);
				}
			}
			//撤销操作的
			if(AdjustType>9&&AdjustType!=13){
				LessonInfo course2 = ruleConflict.getCoursesDic().get(tmp.getString("OriginalSubject"));
				if(course2!=null){
					curCourseName = course2.getName();
				}
				orTeachers = tmp.getString("OriginalTeachers");
			}
			if(AdjustType==13){
				orTeachers = "";
				courseName = curCourseName;
				fromCourseName = "自习";
				curCourseName = "自习";
			}
			tmp.put("courseName", courseName);
			tmp.put("fromCourseName", fromCourseName);
			tmp.put("orCourseName", curCourseName);
			tmp.put("orTeachers", orTeachers);
			
			String published = "否";
			int pblsd = tmp.getIntValue("Published");
			if(pblsd==1){
				published = "是";
			}
			tmp.put("published", published);
			//记录原始课表
			orTbByClass.put(classId+","+fromDayOfWeek+","+fromLessonOfDay, tmp);
			String teas = tmp.getString("FromTeachers");
			if(teas!=null&&teas.length()>0){
				String[] fteas = teas.split(",");
				for(int k=0;k<fteas.length;k++){
					String fteacherId = fteas[k];
					if(fteacherId.length()>0&&techerDic.containsKey(fteacherId)){
						orTbByTeacher.put(fteacherId+","+fromDayOfWeek+","+fromLessonOfDay, tmp);
					}
				}
			}
			String ajstType = tmp.getString("AdjustType");
			String memo = "";
			if(ajstType.trim().length()>0){
				String tajstType =  changeAjstType(ajstType);
				String[] tps = tajstType.split(",");
				for(int i=0;i<tps.length;i++){
					if(tps[i].trim().length()>0){
						int type = Integer.parseInt(tps[i]);
						switch (type) {
						case 1:
							memo += "调课、";
							break;
						case 2:
							memo += "代课、";
							break;
						case 3:
							memo += "设自习、";
							break;
						case 4:
							memo += "公开课、";
							break;
							
						case 11:
							memo += "撤销调课、";
							break;
						case 12:
							memo += "撤销代课、";
							break;
						case 13:
							memo += "撤销设自习、";
							break;
						case 14:
							memo += "撤销公开课、";
							break;
						default:
							break;
						}
					}
					
				}
				
				memo = memo.substring(0,memo.length()-1);
			}
			
			tmp.put("memo", memo);
			String weekOfEnds = tmp.getString("WeekOfEnd") ;
			String acrossWeek ="";
			if(weekOfEnds.indexOf(",")!=-1){
				String[] wds = weekOfEnds.split(",");
				String[] tps = ajstType.trim().split(",");
				for(int i=0;i<tps.length;i++){
					if(tps[i]==null||tps[i].trim().length()==0){
						continue;
					}
					int type = Integer.parseInt(tps[i] );
					String m = "调课";
					switch (type) {
					case 1:
						m= "调课";
						break;
					case 2:
						m= "代课";
						break;
					case 3:
						m= "设自习";
						break;
					case 4:
						m= "公开课";
						break;
						
					case 11:
						memo += "撤销调课、";
						break;
					case 12:
						memo += "撤销代课、";
						break;
					case 13:
						memo += "撤销设自习、";
						break;
					case 14:
						memo += "撤销公开课、";
						break;
					default:
						break;
					}
						
					
					if(wds.length>i){
						String asw = "本周";
						int weekOfEnd = Integer.parseInt(wds[i]);
						if(weekOfEnd!=0&&weekOfEnd!=week){
							asw = "当前周至第"+weekOfEnd+ "周";
						} 
						String tmpAsw = m+asw+";";
						if(acrossWeek.indexOf(tmpAsw)==-1){
							acrossWeek += m+asw+";";
						}
					}
				}
				int eqwd = getCommonWeekInEnds(wds);
				if(eqwd!=-1&&eqwd!=week){
					acrossWeek = "当前周至第"+eqwd+ "周";
				}
				if( eqwd==week||eqwd==0){
					acrossWeek =  "本周";
				}
			}else{
				if(weekOfEnds!=null&&weekOfEnds.trim().length()>0){
					
					int weekOfEnd = Integer.parseInt(weekOfEnds);
					if(weekOfEnd!=0&&weekOfEnd!=week){
						acrossWeek = "当前周至第"+weekOfEnd+ "周";
					}else{
						acrossWeek = "本周";
					}
				}else{
					acrossWeek = "本周";
				}
			}
			tmp.put("acrossWeek", acrossWeek);
			
			tmp.put("adjustTime", "("+weekDates[dayOfWeek]+")"+daydic[dayOfWeek]+lessondic[lessonOfDay]);
			tmp.put("fromAdjustTime", "("+weekDates[fromDayOfWeek]+")"+daydic[fromDayOfWeek]
					+lessondic[fromLessonOfDay]);
		}
		
		HashMap<String,Boolean> techerLessonMap = new HashMap<String, Boolean>();
		List<JSONObject> bjList = new ArrayList<JSONObject>();
		List<JSONObject> teaList = new ArrayList<JSONObject>();
		HashMap<String,List<String>> identyTeacherMap = new HashMap<String, List<String>>();
		HashMap<String,JSONObject> identyMsgMap = new HashMap<String, JSONObject>();
		
		for(JSONObject tmp :tempAjustList){
			String teachers = tmp.getString("Teachers");
			String teachersNames = getTeacherNamesById(teachers,ruleConflict);
			String fromTeachers = tmp.getString("FromTeachers");
			String orTeachers = tmp.getString("orTeachers");
			String fromTeachersNames = getTeacherNamesById(fromTeachers,ruleConflict);
			String orTeachersNames = getTeacherNamesById(orTeachers,ruleConflict);
			String  ajsType = tmp.getString("AdjustType") ;
			String identyKey = tmp.getString("identyKey");
			JSONObject sendMsg = new JSONObject();
			List<String> relatedTeachers = new ArrayList<String>();
			if(ajsType==null){
				ajsType = "";
			}
			String[] aTypeArr = ajsType.split(",");
			int dayOfWeek = tmp.getIntValue("dayOfWeek");
			int lessonOfDay = tmp.getIntValue("lessonOfDay");
			int fromDayOfWeek = tmp.getIntValue("FromDayOfWeek");
			int fromLessonOfDay = tmp.getIntValue("FromLessonOfDay");
//			if(astType==1){
				String toAdjust = tmp.getString("courseName")+"|"+teachersNames;
				if(tmp.getString("courseName").equals("自习")){
					toAdjust = tmp.getString("courseName")+"|";
				}
				String fromAdjust = tmp.getString("fromCourseName")+"|"+fromTeachersNames;
//				if(isStrInArray("1", aTypeArr)){
					fromAdjust = tmp.getString("orCourseName")+"|"+orTeachersNames;
//				}
				tmp.put("toAdjust", toAdjust);
				tmp.put("fromAdjust", fromAdjust);
				bjList.add(tmp);
//			}else{
				String[] teass = teachers.split(",");
				if(teachers!=null&&teachers.length()>0){
					String[] teas = teachers.split(",");
					
					for(int j=0;j<teas.length;j++){
						String teacherId = teas[j].trim();
						if(teacherId.length()>0&&techerDic.containsKey(teacherId) ){
							String tkey  = teacherId+","+dayOfWeek+","+lessonOfDay+ajsType;
							if(!techerLessonMap.containsKey(tkey)){
								
								JSONObject obj = new JSONObject();
								obj = BeanTool.castBeanToFirstLowerKey(tmp);
								obj.put("teacherId", teacherId);
								relatedTeachers.add(teacherId);
								obj.put("teacherName", techerDic.get(teacherId).getName());
								String PublishTeachers = tmp.getString("PublishTeachers");
								if(PublishTeachers!=null&&PublishTeachers.indexOf(teacherId)!=-1){
									obj.put("published", "是");
								}else{
									obj.put("published", "否");
								}
								String teatoAdjust = tmp.getString("courseName")+"|"+tmp.getString("adjustTime");
								String teafromAdjust = tmp.getString("fromCourseName")+"|"+tmp.getString("fromAdjustTime");
								if(isStrInArray("2", aTypeArr)){
									teafromAdjust = "无";
								}
								String teaSendMsg = obj.getString("teacherName")+"老师";
								teaSendMsg = getTeacherSendMsg(tmp, aTypeArr,
										teatoAdjust, teafromAdjust, teaSendMsg);
								
								teaSendMsg += "，有效期为"+tmp.getString("acrossWeek")+"，请到个人课表查看详情。";
								sendMsg.put(teacherId, teaSendMsg);
								
								techerLessonMap.put(tkey, true);
								obj.put("toAdjust", teatoAdjust);
								obj.put("fromAdjust", teafromAdjust);
								teaList.add(obj);
							}
						}
					}
				}
				
				//计算原老师的 为代课处理
				if(fromTeachers!=null&&fromTeachers.length()>0){
					String[] teas = fromTeachers.split(",");
					for(int j=0;j<teas.length;j++){
						String teacherId = teas[j];
						if(isStrInArray(teacherId, teass)){
							continue;
						}
						if(teacherId.length()>0&&techerDic.containsKey(teacherId) ){
							String tkey  = teacherId+","+fromDayOfWeek+","+fromLessonOfDay+ajsType;
							if(!techerLessonMap.containsKey(tkey)){
								
								JSONObject obj = new JSONObject();
								obj = (JSONObject) BeanTool.castBeanToFirstLowerKey(tmp);
								obj.put("teacherId", teacherId);
								relatedTeachers.add(teacherId);
								obj.put("teacherName", techerDic.get(teacherId).getName());
								String teatoAdjust = tmp.getString("courseName")+"|"+tmp.getString("adjustTime");
								String teafromAdjust = tmp.getString("fromCourseName")+"|"+tmp.getString("fromAdjustTime");
								String PublishTeachers = tmp.getString("PublishTeachers");
								if(PublishTeachers!=null&&PublishTeachers.indexOf(teacherId)!=-1){
									obj.put("published", "是");
								}else{
									obj.put("published", "否");
								}
								techerLessonMap.put(tkey, true);
								if(isStrInArray("2", aTypeArr)){
									teafromAdjust = "无";
								}
								if(isStrInArray("3", aTypeArr)){
									obj.put("fromAdjust", teafromAdjust);
									teatoAdjust = "无";
									obj.put("toAdjust", "无");
								}else{
									String tmpss = teatoAdjust;
									teatoAdjust = teafromAdjust;
									teafromAdjust = tmpss;
									obj.put("fromAdjust", teafromAdjust);
									obj.put("toAdjust", teatoAdjust);
								}
								
								String teaSendMsg = obj.getString("teacherName")+"老师";
								teaSendMsg = getTeacherSendMsg(tmp, aTypeArr,
										teatoAdjust, teafromAdjust, teaSendMsg);
								
								teaSendMsg += "，有效期为"+tmp.getString("acrossWeek")+"，请到个人课表查看详情。";
								sendMsg.put(teacherId, teaSendMsg);
								
								teaList.add(obj);
							}
						}
					}
				}
				if(identyTeacherMap.containsKey(identyKey)){
					List<String> tmpTeaList = identyTeacherMap.get(identyKey);
					if(tmpTeaList.size()>0){
						relatedTeachers.addAll(tmpTeaList);
					}
				}
				identyTeacherMap.put(identyKey, relatedTeachers);
				if(identyMsgMap.containsKey(identyKey)){
					JSONObject objTmp = identyMsgMap.get(identyKey);
					sendMsg.putAll(objTmp);
					
				}
				identyMsgMap.put(identyKey, sendMsg);
			}
//		}
		
		//排序
		if(astType==1){
			ScoreUtil.sorStuScoreList(bjList, "classId", "desc", "", "");
			for(JSONObject bj:bjList){
				String identyKey = bj.getString("identyKey");
				String teas = "";
				List<String> teaIdList = identyTeacherMap.get(identyKey);
				for(String tid:teaIdList){
					teas+=tid+",";
				}
				if(teas.trim().length()>0){
					teas = teas.substring(0,teas.length()-1);
				}
				bj.put("sendAccounts", teas);
				bj.put("sendMsg", identyMsgMap.get(identyKey) );
			}
			return bjList;
		}else{
			ScoreUtil.sorStuScoreList(teaList, "teacherId", "desc", "", "");
			for(JSONObject tLine:teaList){
				String identyKey = tLine.getString("identyKey");
				String teacherId = tLine.getString("teacherId");
				JSONObject sendMsg = new JSONObject();
				if(identyMsgMap.get(identyKey)!=null){
					JSONObject json = identyMsgMap.get(identyKey) ;
					String kkk = json.getString(teacherId);
					sendMsg.put(teacherId, kkk);
				}
				tLine.put("sendAccounts", teacherId);
				tLine.put("sendMsg",  sendMsg);
			}
			return teaList;
		}
	}

	private static int getCommonWeekInEnds(String[] wds) {
		// TODO Auto-generated method stub
		int rs = -1;
		for(int i=0;i<wds.length;i++){
			if(wds[i].trim().length()>0){
				
				int tp = Integer.parseInt(wds[i]);
				if(rs==-1){
					rs = tp;
				}else{
					if(rs!=tp){
						return -1;
					}
				}
			}
		}
		return rs;
	}

	private static String getTeacherSendMsg(JSONObject tmp, String[] aTypeArr,
			String teatoAdjust, String teafromAdjust, String teaSendMsg) {
		String[] teaToArr = teatoAdjust.split("\\|");
		String courseName = teaToArr[0];
		String adjustTime = "";
		if(teaToArr.length>1){
			adjustTime = teaToArr[1];
		}
		String[] teafromAdjustArr = teafromAdjust.split("\\|");
		String fromCourseName = teaToArr[0];
		String fromAdjustTime = "";
		if(teafromAdjustArr.length>1){
			fromAdjustTime = teafromAdjustArr[1];
		}
		
		
		if(aTypeArr.length==1&&Integer.parseInt(aTypeArr[0])>10){
			teaSendMsg += tmp.getString("memo") +"还原至"+ courseName+"课"
					+convertAjstTime(adjustTime);
		}else{
			if(teatoAdjust.equals("无")||isStrInArray("3", aTypeArr)){
				teaSendMsg +=  fromCourseName +"课"
							+convertAjstTime( fromAdjustTime );
				//如果有设置自习操作
				if(isStrInArray("3", aTypeArr)){
					teaSendMsg+= "已调为“自习课”";
				}else{
					teaSendMsg+= "已取消";
				}
				
			}else{
				 if(isStrInArray("2", aTypeArr)||teafromAdjust.equals("无")){
					 teaSendMsg +=  courseName +"课"
							 	+ convertAjstTime( adjustTime )
							 	+"代课";
					 
				 }else{
					 teaSendMsg +=  courseName +"课"
							 	+ convertAjstTime( fromAdjustTime )
							 	+"已调为"
							 	+convertAjstTime( adjustTime );
				 }
				 if(isStrInArray("4", aTypeArr)){
					 teaSendMsg += "安排为公开课";
				 }
			}
			
		}
		return teaSendMsg;
	}

	private static String convertAjstTime(String showTime){
		String toTime = "";
		if(showTime!=null&&showTime.length()>12){
			toTime = showTime.substring(6,showTime.length());
			String date = toTime.substring(0,5).replace("-", "月")+"日";
			toTime = date + showTime.substring(12,showTime.length());
		}
		return toTime;
	}
	private static String getTeacherNamesById(String teachers,
			RuleConflict ruleConflict) {
		// TODO Auto-generated method stub
		Map<String, Account> dic = ruleConflict.getTeachersDic();
		String rs = "";
		if(teachers!=null&&teachers.length()>0){
			String[] teas = teachers.split(",");
			for(int i=0;i<teas.length;i++){
				String teaId = teas[i];
				if(teaId.length()>0&&dic.containsKey(teaId)
						){
					rs += dic.get(teaId).getName()+",";
				}
			}
			if(rs.length()>0){
				rs = rs.substring(0,rs.length()-1);
			}
		}
		return rs;
	}
	
	//去除数组中重复的记录
	public static String[] array_unique(String[] a) {
	    // array_unique
	    List<String> list = new LinkedList<String>();
	    for(int i = 0; i < a.length; i++) {
	        if(!list.contains(a[i])) {
	            list.add(a[i]);
	        }
	    }
	    return (String[])list.toArray(new String[list.size()]);
	}

	public static List<JSONObject> fulFillClassTbList(List<JSONObject> classlist,
			RuleConflict ruleConflict, int maxDaysForWeek, int maxLessonForDay) {
		// TODO Auto-generated method stub
		List<JSONObject> rsList = new ArrayList<JSONObject>();
		for(JSONObject less:classlist){
			
//			if(les.containsKey("courseId")&&les.containsKey("courseSimpleName")){
//				continue;
//			}
			Map<String,String> teachers = (Map<String, String>) less.get("teachers");
			JSONObject les = BeanTool.castBeanToFirstLowerKey(less);
			
			String courseId = les.getString("courseId");
			les.put("teachers", convertTeacherToList(teachers));
			LessonInfo course = ruleConflict.getCoursesDic()
					.get(courseId);
			if(course==null){
				continue;
			}
			les.put("courseName", course.getName());
			les.put("courseSimpleName", course.getSimpleName()!= null ?
					course.getSimpleName() : course.getName().substring(0, 1));
			String classId = les.getString("classId");
			Classroom clas = ruleConflict.getClassroomsDic().get(classId);
			if(clas==null){
				continue;
			}
			String gradeId = ruleConflict.getGradeIdSynjMap().get(
					clas.getGradeId());
			les.put("className", clas.getClassName());
			les.put("usedGrade", gradeId);
			if(!les.containsKey("AdjustType")){
				les.put("AdjustType", "");
			}
			if(les.containsKey("adjustType")){
				les.put("AdjustType", changeAjstType(les.getString("adjustType")));
			}
			if(les.containsKey("groupId")){
				les.put("groupId", les.get("groupId"));
				les.put("GroupId", les.get("groupId"));
			}
			if(les.getInteger("dayOfWeek")<maxDaysForWeek&&les.getInteger("lessonOfDay")<maxLessonForDay){
				
				rsList.add(les);
			}
		}
		
		return rsList;
		
	}

	/**
	 * 
	 * @param tempAjustList
	 * @param week
	 * @param weekOfEnd
	 * @param timetable
	 * @param tempAjustRecord 
	 * @param groupMap 
	 * @param groupParamMap 
	 * @return
	 */
	public static List<JSONObject> getIsAdvancedPlusByAjstRs(
			List<JSONObject> tempAjustList, int week, int weekOfEnd,
			List<JSONObject> timetable, List<JSONObject> tempAjustRecord,
			HashMap<String, TCRCATemporaryAdjustGroupInfo> groupMap,
			HashMap<String, HashMap<Integer, TCRCATemporaryAdjustGroupParam>> groupParamMap,
			RuleConflict ruleConflict) {
		// TODO Auto-generated method stub
		HashMap<String,Boolean> ajstMap = new HashMap<String, Boolean>();
		//去除重复自习课
		HashMap<String,Boolean> selfStuMap = new HashMap<String, Boolean>();
		//groupId--pos
//		HashMap<String,HashMap<String,List<Integer>>> groupPosWeekMap = new HashMap<String, HashMap<String,List<Integer>>>();
		//某位置牵扯到的所有组代码
		HashMap<String,List<String>> posGroups =  new HashMap<String, List<String>>();
		//某位置_组代码--周次
		HashMap<String,List<Integer>> posgroupWeekMap = new HashMap<String, List<Integer>>();
		for(JSONObject obj:tempAjustList){
			int Week = obj.getIntValue("Week");
			//不跨周的情况
			if(weekOfEnd==0){
				if(Week!=week){
					continue;
				}
			}else{
				if(Week>weekOfEnd){
					continue;
				}
			}
			
			int DayOfWeek = obj.getIntValue("DayOfWeek");
			int LessonOfDay = obj.getIntValue("LessonOfDay");
			String ClassId = obj.getString("ClassId");
			String CourseId = obj.getString("SubjectId");
			String key = DayOfWeek+","+LessonOfDay+","+ClassId;
			String groupId = obj.getString("GroupId");
			if(groupId!=null){
				List<String> gids = new ArrayList<String>();
				if(posGroups.containsKey(key)){
					gids = posGroups.get(key);
				}
				if(!gids.contains(groupId)){
					gids.add(groupId);
				}
				posGroups.put(key, gids);
				
				String gkey = key+"_"+groupId;
				List<Integer> welist = new ArrayList<Integer>();
				if(posgroupWeekMap.containsKey(gkey)){
					welist = posgroupWeekMap.get(gkey);
				}
				welist.add(week);
				posgroupWeekMap.put(gkey, welist);
			}
			
			ajstMap.put(key, true);
			String ajstType = "";
			if(obj.getString("AdjustType")!=null){
				ajstType = obj.getString("AdjustType");
			}
			String[] ast = ajstType.split(",");
					
			if(isStrInArray("3", ast)){
				
				String key2 = key +"," +CourseId;
						
				selfStuMap.put(key2, true);
			}
		}
		
		List<JSONObject> needRemove = new ArrayList<JSONObject>();
		HashMap<String,Boolean> HasPut = new HashMap<String, Boolean>();
		for(JSONObject obj: timetable){
			JSONObject tmp = BeanTool.castBeanToFirstLowerKey(obj);
			int day = tmp.getIntValue("dayOfWeek");
			int lesson = tmp.getIntValue("lessonOfDay");
			String classId = tmp.getString("classId");
			String CourseId = tmp.getString("courseId");
			String nowGroupId = tmp.getString("groupId");
			String key = day +","+lesson +","+classId;
			String key2 = key +"," +CourseId;
			//判断是否为旧版调课逻辑产生的数据
			int IsAdvancePlus = 0;
			boolean isOld = false;
			List<String> gids = new ArrayList<String>();
			if(posGroups.containsKey(key)){
				gids = posGroups.get(key);
				if(gids.contains("-100")){
					isOld = true;
				}
			}
			if(!ajstMap.isEmpty()&&ajstMap.containsKey(key)
					//旧版的数据按照旧版逻辑走
					&&isOld){
				IsAdvancePlus = 1;
			}
			//最大三三对调逻辑部分
			if(IsAdvancePlus==0){
				for(String groupId:gids){
					List<Integer> weekList = new ArrayList<Integer>();
					String gkey = key + "_" +groupId;
					if(posgroupWeekMap.containsKey(gkey)){
						weekList = posgroupWeekMap.get(gkey);
					}
					 //每周是否超过了最大调课次数
					boolean isOverTimes = false;
					for(int weekIndex:weekList){
						if(groupParamMap.containsKey(groupId)){
							HashMap<Integer, TCRCATemporaryAdjustGroupParam> wMap = groupParamMap.get(groupId);
							if(wMap.containsKey((Integer) weekIndex)){
								TCRCATemporaryAdjustGroupParam param = wMap.get((Integer)weekIndex);
								int max = param.getGroupMoveTimes();
								if(max>=MaxAjst ){
									isOverTimes = true;
									break;
								}
							}
						}
					}
					if(isOverTimes){
						IsAdvancePlus = 1;
						break;
					}
				}
			}
			int isOperate = 1;
			int isCancel = 1;
			String failMsg = "";
			//跨周时如果有已调动记录 则不可调
			if(obj.get("GroupId")==null||obj.get("GroupId").equals("-100")&&weekOfEnd!=0){
				if(gids.size()>0){
					isOperate = -1;
					IsAdvancePlus = 1;
					failMsg = "所跨周之中有调动记录，不可在此基础上调课！";
				}
			}
			//2.	已有调课记录的课再调时不能超过上次调课的周范围
			if(weekOfEnd!=0&&obj.get("GroupId")!=null){
				String WeekOfEnd = obj.getString("WeekOfEnd");
				if(WeekOfEnd.equals("0")){
					WeekOfEnd = week+"";
				}
				if(WeekOfEnd!=null){
					
					String[] ends = WeekOfEnd.split(",");
					if(ends.length>0){
						int end = Integer.parseInt( ends[ends.length-1] );
						if(end<weekOfEnd){
							IsAdvancePlus = 1;
							//超出最大周 不可再调课
							isOperate = -1;
							failMsg = "调课跨周不可超过第"+end+"周！";
						}
					}
				}
			}
			for(String groupId:gids){
				TCRCATemporaryAdjustGroupInfo ginfo = groupMap.get(groupId);
						
				//3.	如果组内出现非起始周的调课操作则不能在此次调动操作之前的周做任何调动与撤消操作
				//也不能在之后的周做任何调动操作
				if(ginfo!=null){
					int NotStartOperate = ginfo.getNotStartOperate();
//					if(week<NotStartOperate&&NotStartOperate!=0){
					if( NotStartOperate!=0&&week!=NotStartOperate){
						//如果后续已经没有了操作记录，仍然可调
//						List<JSONObject> tpOne = GrepUtil.grepJsonKeyBySingleVal("GroupId", ginfo.getGroupId(), tempAjustList);
//						List<JSONObject> tpTwo = new ArrayList<JSONObject>();
//						for(JSONObject tt:tpOne){
//							if(tt.getInteger("Week")>week){
//								tpTwo.add(tt);
//							}
//						}
//						if(tpTwo.size()!=0){
//							
							isOperate = -1;
							isCancel = -1;
							IsAdvancePlus = 1;
							failMsg = "请到第"+NotStartOperate+"周进行撤销操作，否则无法调课！";
							break;
//						}
					}
				}
			}
			if( isOperate ==1&&isCancel==1){
				for(String groupId:gids){
					TCRCATemporaryAdjustGroupInfo ginfo = groupMap.get(groupId);
							
					//4.	如果组内出现非起始周的撤消操作则不能在此次撤消操作之前的周做任何调动操作但可以做撤消操作
					if(ginfo!=null){
						int NotStartCancel = ginfo.getNotStartCancel();
						if(week<NotStartCancel&&NotStartCancel!=0){
							isOperate = -1;
							IsAdvancePlus = 1;
							failMsg = "请先进行撤销操作，否则无法调课！";
							break;
						}
					}
				}
			}
			if(isCancel==1){
				
				for(String groupId:gids){
					TCRCATemporaryAdjustGroupInfo ginfo = groupMap.get(groupId);
					
					// 	6.	按教师调课中对调课允许产生跨班组，跨班组上又有多次其它调课操作撤
					//消时需按调课记录的倒序撤消。跨班组在按班级调课中此班级调课记录不是最后一次操作不允许撤消。
					if(ginfo!=null){
						if(ginfo.getIsCrossClass()==1 ){
							int lastStep = ginfo.getGroupStep();
							List<JSONObject> rlist = GrepUtil.grepJsonKeyBySingleVal("Step", lastStep+"", tempAjustRecord);
							List<String> lastClasses = new ArrayList<String>();
							for(JSONObject record:rlist){
								String _classId = record.getString("ClassId");
								lastClasses.add(_classId);
							}
							if(lastClasses.size()>0&&!lastClasses.contains(classId)){
								isCancel = -1;
								String _claName = "";
								String _classId =lastClasses.get(0);
								if(ruleConflict.getClassroomsDic().get(_classId )!=null){
									_claName = ruleConflict.getClassroomsDic().get(_classId).getClassName();
								}
								failMsg = "出现撤销不同步，请先到"+_claName+"班做撤销操作，否则无法撤销！";
								break;
							}
//							for(JSONObject record:rlist){
//								String _classId = record.getString("ClassId");
//								if(!_classId.equals(classId)){
//									isCancel = -1;
//									String _claName = "";
//									if(ruleConflict.getClassroomsDic().get(_classId)!=null){
//										_claName = ruleConflict.getClassroomsDic().get(_classId).getClassName();
//									}
//									failMsg = "出现撤销不同步，请先到"+_claName+"班做撤销操作，否则无法撤销！";
//									break;
//								}
//							}
						}
					}
				}
			}
			if(IsAdvancePlus!=1){
				List<JSONObject> conflicts = new ArrayList<JSONObject>();
				for(JSONObject tobj:tempAjustList){
					int Week = tobj.getIntValue("Week");
					String tClassId = tobj.getString("ClassId");
					if(Week>=week&&tClassId.equals(classId)){
						int tDayOfWeek = tobj.getIntValue("DayOfWeek");
						int tLessonOfDay = tobj.getIntValue("LessonOfDay");
						String tgroupId = tobj.getString("GroupId");
						if(tgroupId!=null&&nowGroupId!=null
								&&!tgroupId.equals(nowGroupId)){
							JSONObject conflict = new JSONObject();
							conflict.put("dayOfWeek", tDayOfWeek);
							conflict.put("lessonOfDay", tLessonOfDay);
							conflict.put("Status", 1);
							//不同组不允许交换
							conflict.put("confType", "777");
							conflicts.add(conflict);
						}
					}
				}
				if(conflicts.size()>0){
					obj.put("conflicts", conflicts);
				}
			}
			obj.put("IsAdvancePlus", IsAdvancePlus);
			obj.put("isCancel", isCancel);
			obj.put("isOperate", isOperate);
			obj.put("failMsg", failMsg);
			if(!selfStuMap.isEmpty()&&selfStuMap.containsKey(key2)&&HasPut.containsKey(key2)){
				needRemove.add(obj);
			}
			HasPut.put(key2, true);
		}
		timetable.removeAll(needRemove);
		
		return timetable;
	}

	public static void loadConflictsByAdvancedPlus(List<JSONObject> timetables,
			int maxDaysForWeek, int maxLessonForDay) {
		// TODO Auto-generated method stub
		for(JSONObject timetable:timetables){
			String classId = timetable.getString("ClassId");
			if(classId==null){
				classId = timetable.getString("classId");
			}
			
			int IsAdvance = timetable.getIntValue("IsAdvancePlus");
			String mcGroupId = timetable.getString("McGroupId");
			int orday = timetable.getIntValue("dayOfWeek");
			if(!timetable.containsKey("dayOfWeek")){
				orday = timetable.getIntValue("DayOfWeek");
			}
			int orlesson = timetable.getIntValue("lessonOfDay");
			if(!timetable.containsKey("lessonOfDay")){
				orlesson = timetable.getIntValue("LessonOfDay");
			}
			//合班牵扯到联动问题 之前禁用联动禁止合班调课 ，现在放开
//			if(mcGroupId!=null&&mcGroupId.trim().length()>0){
//				IsAdvance =1;
//			}
			List<JSONObject> conflicts = (List<JSONObject>) timetable
					.get("conflicts");
			if(conflicts==null){
				conflicts = new ArrayList<JSONObject>();
				timetable.put("conflicts", conflicts);
			}
			
			if(IsAdvance==1 ){
				for(int i=0;i<maxDaysForWeek;i++){
					for(int j=0;j<maxLessonForDay;j++){
						JSONObject conflict = new JSONObject();
						conflict.put("dayOfWeek", i);
						conflict.put("lessonOfDay", j);
						conflict.put("Status", 1);
						conflict.put("confType", "666");
						conflicts.add(conflict);
					}
				}
				for(JSONObject  tar:timetables){
					String _classId = tar.getString("ClassId");
					if(_classId==null){
						_classId = tar.getString("classId");
					}
					if(!classId.equals(_classId)){
						continue;
					}
					List<JSONObject> tarConfs=(List<JSONObject>) tar
							.get("conflicts");
					if(tarConfs==null){
						tarConfs = new ArrayList<JSONObject>();
					}
					JSONObject orconflict = new JSONObject();
					
					orconflict.put("dayOfWeek", orday);
					orconflict.put("lessonOfDay", orlesson);
					orconflict.put("Status", 1);
					orconflict.put("confType", "666");
					tarConfs.add(orconflict);
					tar.put("conflicts", tarConfs);
				}
			}else {
				String groupId = timetable.getString("GroupId");
				
				String courseId = timetable.getString("CourseId");
				if(courseId==null){
					courseId = timetable.getString("courseId");
				}
				if(groupId!=null&&!groupId.equals("-100")){
					for(JSONObject  tar:timetables){
//						if(tar.equals(timetable)){
//							continue;
//						}
						int day = tar.getIntValue("dayOfWeek");
						if(!tar.containsKey("dayOfWeek")){
							day = tar.getIntValue("DayOfWeek");
						}
						int lesson = tar.getIntValue("lessonOfDay");
						if(!tar.containsKey("lessonOfDay")){
							lesson = tar.getIntValue("LessonOfDay");
						}
						String _classId = tar.getString("ClassId");
						if(_classId==null){
							_classId = tar.getString("classId");
						}
						if(!classId.equals(_classId)){
							continue;
						}
						String _courseId = tar.getString("CourseId");
						if(_courseId==null){
							_courseId = tar.getString("courseId");
						}
								
						String tGroupId = tar.getString("GroupId");
						if((tGroupId!=null&&!tGroupId.equals("-100"))||
								(classId.equals(_classId)&&courseId.equals(_courseId))
//								&&!tGroupId.equals(groupId)
								){
							JSONObject conflict = new JSONObject();
							conflict.put("dayOfWeek", day);
							conflict.put("lessonOfDay", lesson);
							conflict.put("Status", 1);
							//不同组不允许交换 //同组也不允许调整
							conflict.put("confType", "777");
							conflicts.add(conflict);
							
							List<JSONObject> tarConfs=(List<JSONObject>) tar
									.get("conflicts");
							if(tarConfs==null){
								tarConfs = new ArrayList<JSONObject>();
							}
							JSONObject orconflict = new JSONObject();
							
							orconflict.put("dayOfWeek", orday);
							orconflict.put("lessonOfDay", orlesson);
							orconflict.put("Status", 1);
							//不同组不允许交换 //同组也不允许调整
							orconflict.put("confType", "777");
							tarConfs.add(orconflict);
							tar.put("conflicts", tarConfs);
							
						}
					}
					timetable.put("conflicts", conflicts);
				}
			}
			
		}
	}

	/**
	 * 获取按教师调课
	 * @param arrangeMap
	 * @param fromDayOfWeek
	 * @param fromLessonOfDay
	 * @param fromCourseIds
	 * @param fromCourseTypes
	 * @param toDayOfWeek
	 * @param toLessonOfDay
	 * @param toCourseIds
	 * @param toCourseTypes
	 * @param classId
	 * @param week
	 * @param weekOfEnd
	 * @param ruleConflict
	 * @param schoolId
	 * @param timetableId
	 * @param tempAjustList
	 * @param evryWeekTbAllNj
	 * @param taskMap 
	 * @return
	 */
	 
	public static JSONObject getNeedRecordAdjustedListByAjstTeacher(HashMap<String, List<JSONObject>> arrangeMap,
			Integer fromDayOfWeek, Integer fromLessonOfDay,
			JSONArray  fromCourseArr, Integer toDayOfWeek, Integer toLessonOfDay,
			JSONArray  toCourseArr, int week, int weekOfEnd, 
			RuleConflict ruleConflict, String schoolId, String timetableId, 
			List<JSONObject> tempAjustList,
			HashMap<Integer, HashMap<String, List<JSONObject>>> evryWeekTbAllNj,
			HashMap<String, HashMap<String, HashMap<String, String>>> taskMap,String groupId){
		List<TCRCATemporaryAdjustRecord> needInsertRecord = new ArrayList<TCRCATemporaryAdjustRecord>();
		List<TCRCATemporaryAdjustRecord> needInsertRecord2 = new ArrayList<TCRCATemporaryAdjustRecord>();
		List<TCRCATemporaryAdjustResult> needInsertResult = new ArrayList<TCRCATemporaryAdjustResult>();
		List<TCRCATemporaryAdjustResult> needDeleteResult = new ArrayList<TCRCATemporaryAdjustResult>();
		
		
		addToTeacherNeedResult(arrangeMap, fromDayOfWeek, fromLessonOfDay,
				fromCourseArr, toDayOfWeek, toLessonOfDay, week, weekOfEnd,
				ruleConflict, schoolId, timetableId, tempAjustList,
				evryWeekTbAllNj, needInsertRecord, needInsertResult,
				needDeleteResult,taskMap, groupId);
		
		if(toCourseArr!=null&&toCourseArr.size()>0){
			
			addToTeacherNeedResult(arrangeMap, toDayOfWeek, toLessonOfDay,
					toCourseArr, fromDayOfWeek, fromLessonOfDay, week, weekOfEnd,
					ruleConflict, schoolId, timetableId, tempAjustList,
					evryWeekTbAllNj, needInsertRecord2, needInsertResult,
					needDeleteResult,taskMap, groupId);
			
			for(int i=0;i<needInsertRecord.size();i++){
				TCRCATemporaryAdjustRecord from = needInsertRecord.get(i);
				TCRCATemporaryAdjustRecord to = needInsertRecord2.get(i);
				from.setRelatedRecordIds(to.getRecordId());
				to.setRelatedRecordIds(from.getRecordId());
			}
			
			needInsertRecord.addAll(needInsertRecord2);
		}
		JSONObject rs = new JSONObject();
		rs.put("needInsertRecord", needInsertRecord);
		rs.put("needInsertResult", needInsertResult);
		rs.put("needDeleteResult", needDeleteResult);
		return rs;
	}

	/**
	 * @param arrangeMap
	 * @param fromDayOfWeek
	 * @param fromLessonOfDay
	 * @param fromCourseArr
	 * @param toDayOfWeek
	 * @param toLessonOfDay
	 * @param week
	 * @param weekOfEnd
	 * @param ruleConflict
	 * @param schoolId
	 * @param timetableId
	 * @param tempAjustList
	 * @param evryWeekTbAllNj
	 * @param needInsertRecord
	 * @param needInsertResult
	 * @param needDeleteResult
	 * @param taskMap 
	 */
	@SuppressWarnings("unchecked")
	public static void addToTeacherNeedResult(
			HashMap<String, List<JSONObject>> arrangeMap,
			Integer fromDayOfWeek,
			Integer fromLessonOfDay,
			JSONArray fromCourseArr,
			Integer toDayOfWeek,
			Integer toLessonOfDay,
			int week,
			int weekOfEnd,
			RuleConflict ruleConflict,
			String schoolId,
			String timetableId,
			List<JSONObject> tempAjustList,
			HashMap<Integer, HashMap<String, List<JSONObject>>> evryWeekTbAllNj,
			List<TCRCATemporaryAdjustRecord> needInsertRecord,
			List<TCRCATemporaryAdjustResult> needInsertResult,
			List<TCRCATemporaryAdjustResult> needDeleteResult, 
			HashMap<String, HashMap<String, HashMap<String, String>>> taskMap,String groupId) {
		
		for(int i=0;i<fromCourseArr.size();i++){
			JSONObject tmp = fromCourseArr.getJSONObject(i);
			String classId = tmp.getString("classId");
			String courseId= tmp.getString("courseId");
			String courseType = tmp.getString("courseType");
			String fromMcGroupId = tmp.getString("fromMcGroupId");
			
			List<JSONObject> toList = arrangeMap.get(toDayOfWeek+","+toLessonOfDay+","+classId);
			
			String toCourseIds  = "";
			String toCourseTypes  = "";
			String toMcGroupId  = "";
			if(toList!=null&&toList.size()>0){
				for(JSONObject obj:toList){
					toCourseIds+= obj.getString("CourseId")+",";
					toCourseTypes+= obj.getString("CourseType")+",";
					toMcGroupId += obj.getString("toMcGroupId");
				}
			}
			if(toCourseIds.length()>0){
				toCourseIds = toCourseIds.substring(0,toCourseIds.length()-1);
			}
			if(toCourseTypes.length()>0){
				toCourseTypes  = toCourseTypes.substring(0,toCourseTypes.length()-1);
			}
			if(toMcGroupId.length()>0){
				toMcGroupId  = toMcGroupId.substring(0,toCourseTypes.length()-1);
			}
			JSONObject needInsertList = TimetableUtil.getNeedRecordAdjustedList(arrangeMap, fromDayOfWeek,
					fromLessonOfDay,  courseId, courseType, fromMcGroupId, toDayOfWeek,
					toLessonOfDay,  toCourseIds,toCourseTypes,toMcGroupId,classId, week,weekOfEnd,
					ruleConflict,schoolId,timetableId,tempAjustList,evryWeekTbAllNj, taskMap, groupId);
			
			List<TCRCATemporaryAdjustRecord> needInsertRecordtmp = (List<TCRCATemporaryAdjustRecord>) needInsertList.get("needInsertRecord");
			List<TCRCATemporaryAdjustResult> needInsertResulttmp =	 (List<TCRCATemporaryAdjustResult>) needInsertList.get("needInsertResult");
			List<TCRCATemporaryAdjustResult> needDeleteResulttmp = (List<TCRCATemporaryAdjustResult>) needInsertList.get("needDeleteResult");
			
			needInsertRecord.addAll(needInsertRecordtmp);
			needInsertResult.addAll(needInsertResulttmp);
			needDeleteResult.addAll(needDeleteResulttmp);
		}
	}

	/**
	 * 教师微调-获取教师未完成任务
	 * @param teaTask
	 * @param classArrangeMap	班课表
	 * @param ruleConflict
	 * @param maxLessonForDay 
	 * @param maxDaysForWeek 
	 * @param arrangeMap 所有课表
	 * @return
	 */
	public static List<JSONObject> getTeacherCourseListByTask(
			List<JSONObject> teaTask,
			HashMap<String, List<JSONObject>> classArrangeMap,
			RuleConflict ruleConflict, int maxDaysForWeek, int maxLessonForDay,
			HashMap<String, List<JSONObject>> arrangeMap) {
		// TODO Auto-generated method stub
		
		List<JSONObject> courseList = new ArrayList<JSONObject>();
		// 加载本班的教学计划--未排课程计算冲突
		for (JSONObject task : teaTask) {

			String classId = task.getString("ClassId");

			double weekNum = task.getDoubleValue("WeekNum");
			String courseId = task.getString("CourseId");
			String teacherId = task.getString("TeacherId");


			JSONObject taskCourse = new JSONObject();
			taskCourse.put("courseId", courseId);
			LessonInfo courseInfo = ruleConflict.findCourseInfo(courseId);
			if (courseInfo != null) {
				// 课程名称
				taskCourse.put("courseName", courseInfo.getName());
				taskCourse.put("courseSimpleName",
						courseInfo.getSimpleName());
			}else{
				continue;
			}


			double courseLessonCount =  getCourseLessonCount(classId,
					courseId, classArrangeMap);
			taskCourse.put("lessonCount", courseLessonCount);

			double unLessonCount = weekNum - courseLessonCount;
			taskCourse.put("unLessonCount", weekNum - courseLessonCount);

			
			Grade ddd = new Grade();
			Classroom classroom = ruleConflict.getClassroomsDic().get(classId);
			// 计算冲突位置
			if (classroom  == null
					|| classroom.getGradeId() == 0
					|| ruleConflict.getGradeIdGradeDic().get(
							classroom.getGradeId()) == null) {
				continue;
			} else {
				ddd = ruleConflict.getGradeIdGradeDic().get(
						classroom.getGradeId());
			}
			String gradeLev = ddd.getCurrentLevel().getValue() + "";
			
			Map<String, String> teachers = new HashMap<String, String>();
			if(ruleConflict.getTeachersDic().containsKey(teacherId)){
				
				teachers.put(teacherId, ruleConflict.getTeachersDic().get(teacherId).getName());
			}
			// 计算冲突位置
			if(unLessonCount>0){
				
				List<JSONObject> conflicts = TimetableUtil.getConflicts(classId,
						courseId, teachers , maxDaysForWeek, maxLessonForDay,
						arrangeMap, ruleConflict, gradeLev, unLessonCount, 0,task.getString("McGroupId"), 0,-1);
				
				taskCourse.put("conflicts", conflicts);
				//所有有课课程位置冲突--单双周例外
				List<JSONObject> tmp = new ArrayList<JSONObject>();
				tmp.add(taskCourse);
				loadCourseConflictsByTimetable(tmp,classArrangeMap.get(classId));
			}
			
			taskCourse.put("McGroupId", task.getString("McGroupId") );
			List<JSONObject> tmp2 = new ArrayList<JSONObject>();
			JSONObject cls  = new JSONObject();
			cls.put("classId", classId);
			cls.put("className", classroom.getClassName());
			tmp2.add(cls);
			taskCourse.put("classIds", tmp2);
			courseList.add(taskCourse);

		}
		return courseList;
	}

	private static double getCourseLessonCount(String classId, String courseId,
			HashMap<String, List<JSONObject>> classArrangeMap) {
		// TODO Auto-generated method stub
		double lessonCount  = 0;
		List<JSONObject> list = classArrangeMap.get(classId);
		if(list!=null&&list.size()>0){
			for (JSONObject arrange : list) {
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
		}
		return lessonCount;
	}

	/**
	 * 教师微调-获取班级课表【无冲突】
	 * @param relatedClassIds
	 * @param classArrangeMap
	 * @param ruleConflict 
	 * @return
	 */
	public static List<JSONObject> getClassTimetableNoConfByClassMap(
			List<String> relatedClassIds,
			HashMap<String, List<JSONObject>> classArrangeMap, RuleConflict ruleConflict) {
		// TODO Auto-generated method stub
		Map<String, Classroom> classdic = ruleConflict.getClassroomsDic();
		Map<String, LessonInfo> kmdic = ruleConflict.getCoursesDic();
		List<JSONObject> rs = new ArrayList<JSONObject>();
		for(String classId :relatedClassIds){
			if(classdic.containsKey(classId)&&classArrangeMap.containsKey(classId)){
				JSONObject clas =  new JSONObject();
				clas.put("classId", classId);
				Classroom clr = classdic.get(classId);
				clas.put("className", clr.getClassName()	);
				List<JSONObject> timetable = new ArrayList<JSONObject>();
				clas.put("timetable", timetable);
				
				String usedGrade = ruleConflict.getGradeIdSynjMap().get(classdic.get(classId).getGradeId());
				clas.put("usedGrade", usedGrade );
				
				List<JSONObject> tskList = classArrangeMap.get(classId);
				for(JSONObject task:tskList){
					
					Map<String,String> teaMap = (Map<String, String>) task.get("teachers");
					
					JSONObject now = BeanTool.castBeanToFirstLowerKey(task);
					now.put("teachers", convertTeacherToList(teaMap));
					now.put("usedGrade", usedGrade);
					
					if(!now.containsKey("courseName")){
						String cid = now.getString("courseId");
						if(!kmdic.containsKey(cid)){
							continue;
						}
						
						String cName = kmdic.get(cid).getName();
						String spName = kmdic.get(cid).getSimpleName();
						if(spName==null||spName.trim().length()==0){
							spName = cName.substring(0,1);
						}
						
						now.put("courseName", cName);
						now.put("courseSimpleName", spName);
					}
					
					timetable.add(now);
				}
				
				rs.add(clas);
			}
			
		}
		
		return rs;
	}
	
	
	/**
	 * 根据课表取课程的冲突 未排课程的
	 * 
	 * @param courseList
	 * @param timetableList
	 */
	public static void loadCourseConflictsByTimetable(List<JSONObject> courseList,
			List<JSONObject> timetableList) {

		for (int i = 0; i < courseList.size(); i++) {
			JSONObject course = courseList.get(i);

			double unLessonCount = course.getDoubleValue("unLessonCount");

			List<JSONObject> conflicts = (List<JSONObject>) course
					.get("conflicts");

			for (int j = 0; j < timetableList.size(); j++) {
				JSONObject target = timetableList.get(j);
				int targetDay = target.getIntValue("dayOfWeek");
				if(target.containsKey("DayOfWeek")){
					targetDay = target.getIntValue("DayOfWeek");
				}
				int targetLesson = target.getIntValue("lessonOfDay");
				if(target.containsKey("LessonOfDay")){
					targetLesson = target.getIntValue("LessonOfDay");
				}

				int courseType = target.getIntValue("courseType");
				if(target.containsKey("CourseType")){
					courseType = target.getIntValue("CourseType");
				}

				if (unLessonCount == 0.5 && courseType != 0) {

				} else {

					JSONObject conflict = new JSONObject();
					conflict.put("dayOfWeek", targetDay);
					conflict.put("lessonOfDay", targetLesson);
					conflict.put("Status", -1);
					conflict.put("confType", "333");

					conflicts.add(conflict);
				}
			}
			course.put("conflicts", conflicts);
			
		}

	}

	/**
	 * 将临时课表转换为需要的格式
	 * @param curweekTb
	 * @param timetable 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<JSONObject> changeAppTbByMap(
			HashMap<String, List<JSONObject>> curweekTb,String teacherId,RuleConflict rct, JSONObject timetable) {
		// TODO Auto-generated method stub
		String[] daydic = new String[]{"周一","周二","周三","周四","周五","周六","周日"};
		String[] lessondic = new String[]{"第一节","第二节","第三节","第四节","第五节","第六节","第七节","第八节"
				,"第九节","第十节","第十一节","第十二节","第十三节","第十四节","第十五节"};
		List<JSONObject> rs = new ArrayList<JSONObject>();
		int totalMaxDays = 0 ;
		int totalMaxLessons = 0 ;		
		
		
		int maxDaysForWeek = timetable.getIntValue("MaxDaysForWeek");
		
		List<Integer> allDays = new ArrayList<Integer>();
		for(int i=0;i<maxDaysForWeek;i++){
			allDays.add(i);
		}
		List<Integer> hasDays = new ArrayList<Integer>();
		HashMap<Integer,List<JSONObject>> tbMap = new HashMap<Integer, List<JSONObject>>();
		for(Iterator<String> it = curweekTb.keySet().iterator();it.hasNext();){
			String key = it.next();
			String[] keys = key.split(",");
			int day = Integer.parseInt(keys[0]);

			if(day>=maxDaysForWeek){
				continue;
			}
			int lesson = Integer.parseInt(keys[1]);
			totalMaxDays = totalMaxDays < day ? day : totalMaxDays;
			totalMaxLessons = totalMaxLessons < lesson ? lesson:totalMaxLessons;
			
			List<JSONObject> dayList = new ArrayList<JSONObject>();
			if(tbMap.containsKey(day)){
				dayList = tbMap.get(day);
			}else{
				tbMap.put(day, dayList);
			}
			
			List<JSONObject> eyeList = curweekTb.get(key);
			JSONObject curEye = mergeEyeListToOneObj(eyeList, teacherId, rct);
			if(curEye!=null&&curEye.containsKey("courseName")
					&&curEye.getString("courseName").trim().length()>0){
				curEye.put("lesson", lesson);
				curEye.put("lessonStr", lessondic[lesson]);
				dayList.add(curEye);
			}
		}
		for(Iterator<Integer> it = tbMap.keySet().iterator();it.hasNext();){
			int day = it.next();
			List<JSONObject> dayList = tbMap.get(day);
			ScoreUtil.sorStuScoreList(dayList, "lesson", "asc", "", "");
			hasDays.add(day);
			JSONObject dayObj = new JSONObject();
			dayObj.put("dayList", dayList);
			dayObj.put("day", day);
			dayObj.put("totalMaxDays", totalMaxDays + 1 );
			dayObj.put("totalMaxLessons", totalMaxLessons  + 1);
			
			for(JSONObject obj:dayList){
				String AdjustType ="";
				String ajst = obj.getString("AdjustType");
				if(ajst!=null&&!ajst.equalsIgnoreCase("null")&&ajst.trim().length()>0){
					AdjustType = ajst.replace("1", "调").replace("2", "代")
							.replace("3", "自").replace("4", "公");
				}
				obj.put("AdjustType", AdjustType);
			}
		    Calendar calendar = Calendar.getInstance();
			calendar.setTime(new Date());
			int week = calendar.get(Calendar.DAY_OF_WEEK)-2;
			if(week==-1){
				week = 6;
			}
		    if(week == day){
		    	dayObj.put("isToday", true);
		    }else{
		    	dayObj.put("isToday", false);
		    }
		    dayObj.put("dayStr", daydic[day]);
			rs.add(dayObj);
		}
		allDays.removeAll(hasDays);
		for(Integer day:allDays){

			JSONObject dayObj = new JSONObject();
			dayObj.put("day", day);
			//获取今天周次

			Calendar calendar = Calendar.getInstance();
			calendar.setTime(new Date());
			int week = calendar.get(Calendar.DAY_OF_WEEK)-2;
			if(week==-1){
				week = 6;
			}
			List<JSONObject> dayList = new ArrayList<JSONObject>();
			dayObj.put("dayList", dayList);
			if(week == day){
				dayObj.put("isToday", true);
			}else{
				dayObj.put("isToday", false);
			}
			dayObj.put("dayStr", daydic[day]);
			rs.add(dayObj);
		}
		ScoreUtil.sorStuScoreList(rs, "day", "asc", "", "");
		for(JSONObject obj:rs){
			obj.remove("pm");
			obj.remove("day");
			List<JSONObject> list = (List<JSONObject>) obj.get("dayList");
			for(JSONObject o:list){
				o.remove("pm");
				o.remove("lesson");
			}
		}
		return rs;
	}

	@SuppressWarnings("unchecked")
	private static JSONObject mergeEyeListToOneObj(List<JSONObject> eyeList,String teacherId,RuleConflict rct) {
		// TODO Auto-generated method stub
		JSONObject obj = null;
		Map<String, LessonInfo> lDic = rct.getCoursesDic();
		Map<String, Account> teaDic = rct.getTeachersDic();
		Map<String, Classroom> classDic = rct.getClassroomsDic();
		if(eyeList.size()>0){
			obj = new JSONObject();
			String courseName = "";
			String teacherName = "";
			String AdjustType = "";
			String className = "";
			for(JSONObject eye:eyeList){
				 Map<String,String> teachers = (Map<String, String>) eye.get("teachers");
				 for(String tea:teachers.keySet()){
					 if(tea!=null&&teaDic.containsKey(tea)){
						 
						 teacherName+= teaDic.get(tea).getName()+",";
					 }
				 }
				 if(teacherId!=null&&!teachers.keySet().contains(teacherId)){
					 continue;
				 }
				 if(lDic.containsKey(eye.getString("CourseId"))){
					 
					 courseName += lDic.get(eye.getString("CourseId")).getName()+",";
				 }
				 if(eye.containsKey("AdjustType")&&eye.getString("AdjustType").trim().length()>0
						 &&!eye.getString("AdjustType").equalsIgnoreCase("null")){
					 
					 AdjustType += eye.getString("AdjustType")+",";
				 }
				 if(classDic.containsKey(eye.getString("ClassId"))){
					 className += classDic.get(eye.getString("ClassId")).getClassName()+",";
					 
				 }
			}
			if(courseName.trim().length()>0){
				courseName = courseName.substring(0,courseName.length()-1);
				obj.put("courseName", courseName);
			}
			if(teacherName.trim().length()>0&&teacherName!=null){
				teacherName = teacherName.substring(0,teacherName.length()-1);
				obj.put("teacherName", teacherName);
			}else{
				obj.put("teacherName", "");
			}
			if(AdjustType.trim().length()>0){
				AdjustType = AdjustType.substring(0,AdjustType.length()-1);
				obj.put("AdjustType", changeAjstType(AdjustType));
			}
			if(className.trim().length()>0&&teacherId!=null){
				className = className.substring(0,className.length()-1);
				obj.put("className", className);
			}
		}
		return obj;
	}
	
	/**
    *
    * @param str
    *         需要过滤的字符串
    * @return
    * @Description:过滤数字以外的字符
    */
   public static String filterUnNumber(String str) {
       // 只允数字
       String regEx = "[^0-9]";
       Pattern p = Pattern.compile(regEx);
       Matcher m = p.matcher(str);
       //替换与模式匹配的所有字符（即非数字的字符将被""替换）
       return m.replaceAll("").trim();

   }

   /**
    * 计算班级任务排课完成情况
    * @param gradeClass	需要判断的班级列表
    * @param taskList	教学任务列表
    * @param arrangeList	排课列表
    * @param maxLessonForDay	每天最大节次【用于判断是否待排区】
    * @return
    */
	public static List<JSONObject> getClassFinishSituation(
			List<String> gradeClass, List<JSONObject> taskList,
			List<JSONObject> arrangeList, int maxLessonForDay) {
		// TODO Auto-generated method stub
		//任务数总计
		HashMap<String,Double> taskNumMap = new HashMap<String, Double>();
		//任务数-计数
		HashMap<String,Double> arrangeTaskNumMap = new HashMap<String, Double>();
		List<String> unfinishClasses = new ArrayList<String>();
		//生成班级课程任务映射
		for(JSONObject task:taskList){
			String classId = task.getString("ClassId");
			//只校验需要计算的班级
			if(gradeClass.contains(classId)){
				String courseId = task.getString("CourseId");
				String ckey = classId + "_"+ courseId;
				double weekNum = task.getDoubleValue("WeekNum");
				taskNumMap.put(ckey, weekNum);
			}
		}
		//校验未完成
		for(JSONObject arrange:arrangeList){
			String classId = arrange.getString("ClassId");
			String courseId = arrange.getString("CourseId");
			String ckey = classId + "_"+ courseId;
			//如果是不需要计算的班级或-已经计算得到未完成结果的班级 不需要累计计算其安排任务数
			if(!gradeClass.contains(classId)||
					unfinishClasses.contains(classId)){
				continue;
			}
			int courseType = arrange.getIntValue("CourseType");
			int LessonOfDay = arrange.getIntValue("LessonOfDay");
			// 待排区存在课程则跳过 记录为未完成
			if(LessonOfDay>=maxLessonForDay){
				unfinishClasses.add(classId);
				continue;
			}
			//累计课程数量
			double onece = 1.0;
			if(courseType!=0){
				onece = 0.5;
			}
			if(arrangeTaskNumMap.containsKey(ckey)){
				double total = arrangeTaskNumMap.get(ckey)+onece;
				arrangeTaskNumMap.put(ckey, total);
			}else{
				arrangeTaskNumMap.put(ckey, onece);
			}
			
		}
		
		for(Iterator<String> it = taskNumMap.keySet().iterator();it.hasNext();){
			String ckey = it.next();
			String classId = ckey.split("_")[0];
			String courseId = ckey.split("_")[1];
			if(unfinishClasses.contains(classId)){
				continue;
			}
			
			double arr = 0;
			if(arrangeTaskNumMap.containsKey(ckey)){
				
				arr = arrangeTaskNumMap.get(ckey);
			}
			double tnum = taskNumMap.get(ckey);
			if(arr<tnum){
				unfinishClasses.add(classId);
			}
		}
		
		List<JSONObject> rsList  = new ArrayList<JSONObject>();
		
		for(String cid:gradeClass){
			
			JSONObject obj = new JSONObject();
			obj.put("classId", cid);
			boolean finish = true;
			if(unfinishClasses.contains(cid)){
				finish = false;
			}
			obj.put("isFinished", finish);
			
			rsList.add(obj);
		}
		
		return rsList;
	}
	/**
	 * 计算教师课表完成情况
	 * @param taskList
	 * @param arrangeList
	 * @param maxLessonForDay
	 * @return
	 */
	public static List<JSONObject> getTeacherTaskFinishSituation(
			List<JSONObject> taskList,
			List<JSONObject> arrangeList, RuleConflict ruleConflict) {
		// TODO Auto-generated method stub
		List<String> techerIds = new ArrayList<String>();
		List<String> unfinishTeachers = new ArrayList<String>();
		HashMap<String,HashMap<String,Double>> teacherTaskNum = new HashMap<String, HashMap<String,Double>>();
		HashMap<String,HashMap<String,Double>> teacherArrangeNum = new HashMap<String, HashMap<String,Double>>();
		Map<String, Integer> gmaMap = ruleConflict.getGradeLevelMaxAmNumMap();
		Map<String, Integer> gmpMap = ruleConflict.getGradeLevelMaxPmNumMap();
		Map<String, Classroom> classdic = ruleConflict.getClassroomsDic();
		Map<Long, Grade> gradeDic = ruleConflict.getGradeIdGradeDic();
		//循环任务 计算教师各班任务数
		for(JSONObject task:taskList){
			String classId = task.getString("ClassId");
			String courseId = task.getString("CourseId");
			String teacherId = task.getString("TeacherId");
			
			if(teacherId==null||ruleConflict.getTeachersDic().get(teacherId)==null||
					classdic.get(classId)==null||classdic.get(classId).getGradeId()==0
					||gradeDic.get(classdic.get(classId).getGradeId())==null){
				continue;
			}
			//记录全部教师id
			if(!techerIds.contains(teacherId)){
				techerIds.add(teacherId);
			}
			
			//记录所有教师的任务数
			double weeekNum = task.getDoubleValue("WeekNum");
			String ckey = classId + "_" +courseId;
			
			if(teacherTaskNum.containsKey(teacherId)){
				HashMap<String,Double> taskNum = teacherTaskNum.get(teacherId);
				taskNum.put(ckey, weeekNum);	
			}else{
				HashMap<String,Double> taskNum = new HashMap<String, Double>();
				taskNum.put(ckey, weeekNum);
				teacherTaskNum.put(teacherId, taskNum);
			}
		}
		
		//校验未完成
		for(JSONObject arrange:arrangeList){
			String classId = arrange.getString("ClassId");
			String courseId = arrange.getString("CourseId");
			String ckey = classId + "_"+ courseId;
			String teacherIds = arrange.getString("Teachers");
			if(teacherIds==null||teacherIds.trim().length()==0){
				continue;
			}
			if(teacherIds.indexOf("101009546")!=-1){
				System.out.println("fds");
			}
			String[] teachers = teacherIds.split(",");
			int courseType = arrange.getIntValue("CourseType");
			int LessonOfDay = arrange.getIntValue("LessonOfDay");
			
			int maxLessonForDay = getMaxLessonForDay(gmaMap,gmpMap,classdic,gradeDic,classId);
			// 待排区存在课程则跳过 记录为未完成
			if(LessonOfDay>=maxLessonForDay&&maxLessonForDay!=0){
				for(int i=0;i<teachers.length;i++){
					if(teachers[i]!=null&&teachers[i].trim().length()>0){
						unfinishTeachers.add(teachers[i].trim());
					}
				}
				continue;
			}
			//累计课程数量
			double onece = 1.0;
			if(courseType!=0){
				onece = 0.5;
			}
			//计算各教师任务完成数量
			for(int i=0;i<teachers.length;i++){
				if(teachers[i]!=null&&teachers[i].trim().length()>0){
					String teacherId = teachers[i].trim();
					if(teacherArrangeNum.containsKey(teacherId )){
						HashMap<String,Double> taskNum = teacherArrangeNum.get(teacherId);
						double last = 0;
						if(taskNum.containsKey(ckey)){
							last = taskNum.get(ckey);
						}
						Double total = last+onece;
						taskNum.put(ckey, total );	
					}else{
						HashMap<String,Double> taskNum = new HashMap<String, Double>();
						taskNum.put(ckey, onece);
						teacherArrangeNum.put(teacherId, taskNum);
					}
				}
			}
			
		}
		//计算完成情况
		for(Iterator<String> it = teacherTaskNum.keySet().iterator();it.hasNext();){
			String teacherId = it.next();
			if(teacherId.equals("101009546")){
				System.out.println("fds");
			}
			if(unfinishTeachers.contains(teacherId)){
				continue;
			}
			HashMap<String, Double> taskMap = teacherTaskNum.get(teacherId);
			HashMap<String, Double> arrangeMap = teacherArrangeNum.get(teacherId);
			boolean finish = true;
			if(taskMap!=null){
				for(Iterator<String> tkey = taskMap.keySet().iterator();tkey.hasNext();){
					String tKey = tkey.next();
					if(arrangeMap!=null&&arrangeMap.containsKey(tKey)){
						if(taskMap.get(tKey)!=0&&arrangeMap.get(tKey)<taskMap.get(tKey)){
							finish = false;
							break;
						}
					}else{
						finish = false;
						break;
					}
				}
			}
			if(!finish){
				unfinishTeachers.add(teacherId);
			}
		}
		List<JSONObject> rsList = new ArrayList<JSONObject>();
		for(String tid:techerIds){
			boolean isFinished = true;
			if(unfinishTeachers.contains(tid)){
				isFinished = false;
			}
			JSONObject tea = new JSONObject();
			tea.put("teacherId", tid);
			tea.put("isFinished", isFinished);
			rsList.add(tea);
		}
		return rsList;
	}
	/**
	 * 获取班级最大节次数
	 * @param gmaMap
	 * @param gmpMap
	 * @param classdic
	 * @param gradeDic
	 * @param classId
	 * @return
	 */
	public static int getMaxLessonForDay(Map<String, Integer> gmaMap,
			Map<String, Integer> gmpMap, Map<String, Classroom> classdic,
			Map<Long, Grade> gradeDic, String classId) {
		// TODO Auto-generated method stub
		
		int rs = 0;
		if(classdic.containsKey(classId)){
			Classroom clr = classdic.get(classId);
			if(clr!=null&&clr.getGradeId()!=0){
				if(gradeDic.containsKey(clr.getGradeId())){
					Grade grade = gradeDic.get(clr.getGradeId());
					if(grade!=null){
						String glv = grade.getCurrentLevel().getValue() +"";
						if(gmaMap.containsKey(glv)){
							rs+= gmaMap.get(glv);
						}
						if(gmpMap.containsKey(glv)){
							rs+= gmpMap.get(glv);
						}
					}
				}
			}
		}
		
		
		return rs;
	}
   
	public static void getConflictsByUnfinishTask(RuleConflict ruleConflict,
			int maxDaysForWeek, int maxLessonForDay,
			HashMap<String, List<JSONObject>> arrangeMap,
			Map<String, Map<String, String>> courseTeachers, String courseId,
			List<JSONObject> conflicts, List<String> classes) {
		for (int i = 0; i < maxDaysForWeek; i++) {
			for (int j = 0; j < maxLessonForDay+1; j++) {
			   List<JSONObject> list = arrangeMap.get(i+","+j);
			   if(list==null){
				   continue;
			   }
			   for(JSONObject obj :list){
				   String cid = obj.getString("ClassId");
				   if(classes.contains(cid)){
					   JSONObject conflict = new JSONObject();
						conflict.put("dayOfWeek", i);
						conflict.put("lessonOfDay", j);
						// -1，不可调；1，教师冲突
						int status = -1;
						conflict.put("Status", status);
						conflict.put("confType", "有课");
						conflicts.add(conflict);
				   }else{
					   Map<String, String> steachers = courseTeachers.get(cid+"_"+courseId);
					   List<String> ccids = new ArrayList<String>();
					   ccids.add(cid);
					   if(steachers!=null){
						   boolean hasCourseByTeacher = hasCourseByTeacher(arrangeMap,
								   steachers.keySet(), i, j, ccids, courseId,0);
						   int canArrangeTeacher = ruleConflict.canArrangeTeacher(
								   steachers.keySet(), i, j, -1,arrangeMap,1);
						   if( canArrangeTeacher == 0||hasCourseByTeacher){
							   JSONObject conflict = new JSONObject();
								conflict.put("dayOfWeek", i);
								conflict.put("lessonOfDay", j);
								// -1，不可调；1，教师冲突
								int status = -1;
								conflict.put("Status", status);
								conflict.put("confType", "有课");
								conflicts.add(conflict);
						   }
					   }
					   
				   }
				   
			   }
				
			}
		}
	}

	/**
	 * 根据调课记录生成左侧调课记录表
	 * @param ruleConflict
	 * @param week
	 * @param weekOfEnd
	 * @param tempAjustRecord
	 * @param timetable 
	 * @return
	 */
	public static List<JSONObject> getAdjustedListByAjustRecord(
			RuleConflict ruleConflict, int week, int weekOfEnd,
			List<JSONObject> tempAjustRecord, List<JSONObject> timetable) {
		// TODO Auto-generated method stub
		
		HashMap<String,JSONObject> classList = new HashMap<String, JSONObject>();
		HashMap<String,List<JSONObject>> groupList = new HashMap<String, List<JSONObject>>();
		Map<String, LessonInfo> courseDic = ruleConflict.getCoursesDic();
		Map<String, Account> teacherDic = ruleConflict.getTeachersDic();
		
		HashMap<String,Long> groupTimeMap = new HashMap<String, Long>(); 
		
		for (JSONObject tp : tempAjustRecord) {

			int weekNow = tp.getIntValue("Week");
			if (weekNow != week ) {
				continue;
			}
			int AdjustType = tp.getIntValue("AdjustType");

			//撤销的操作记录
			if(AdjustType>9){
				continue;
			}
			
			String classId = tp.getString("ClassId");
			String groupId = tp.getString("GroupId");
			if(groupId==null||groupId.equals("null")||groupId.trim().length()==0){
				groupId = "-100";
				tp.put("groupId", groupId);
			}else{
				String RecordTime = tp.getString("RecordTime").substring(0,19);
				long t = DateUtil.parseDateFormat(RecordTime).getTime();
				if(groupTimeMap.containsKey(groupId)){
					long t2 = groupTimeMap.get(groupId);
					if(t2>t){
						groupTimeMap.put(groupId,t);
					} 
				}else{
					groupTimeMap.put(groupId,t );
				}
			}
			Classroom classroom = ruleConflict.getClassroomsDic().get(classId);
			if(classroom==null||classroom.getGradeId()==0){
				continue;
			}
			long gid = classroom.getGradeId();
			String synj = ruleConflict.getGradeIdSynjMap().get(gid);
			if(synj==null||synj.trim().length()==0){
				continue;
			}
			JSONObject classObj = new JSONObject();
			if(classList.containsKey(classId)){
				classObj = classList.get(classId);
			}else{
				classObj.put("classId", classId);
				classObj.put("gradeId", synj);
				classObj.put("className", classroom.getClassName());
			}
			
			List<String> groupIds = new ArrayList<String>();
			if(classObj.containsKey("groupIds")){
				groupIds = (List<String>) classObj.get("groupIds");
			}
			if(!groupIds.contains(groupId)){
				groupIds.add(groupId);
			}
			classObj.put("groupIds", groupIds);
			classList.put(classId, classObj);
			
			List<JSONObject> groupTpList = new ArrayList<JSONObject>();
			String ggKey = classId+"_"+groupId;
			if(groupList.containsKey(ggKey)){
				groupTpList = groupList.get(ggKey);
			}else{
				groupList.put(ggKey, groupTpList);
			}
			
			String orCourseId = tp.getString("OriginalSubject");
			String tarCourseId = tp.getString("TargetSubject");
			String OriginalTeachers = tp.getString("OriginalTeachers") ;
			String TargetTeachers = tp.getString("TargetTeachers");
					
			if(AdjustType==1||AdjustType==2){
				addRecordToStepList(courseDic, teacherDic, AdjustType,
						groupTpList, tarCourseId, TargetTeachers);
				addRecordToStepList(courseDic, teacherDic, AdjustType,
						groupTpList, orCourseId, OriginalTeachers);
			}else{
				
				addRecordToStepList(courseDic, teacherDic, AdjustType,
						groupTpList, orCourseId, OriginalTeachers);
			}
			
		}
		
		List<JSONObject> rs = new ArrayList<JSONObject>();
		
		for(Iterator<String> cla = classList.keySet().iterator();cla.hasNext();){
			String classId = cla.next();
			JSONObject classObj = classList.get(classId);
			JSONObject rsObj = new JSONObject();
			rsObj.put("classId",classId);
			rsObj.put("className",classObj.get("className"));
			rsObj.put("gradeId", classObj.get("gradeId"));
			List<String> groupIds = (List<String>) classObj.get("groupIds");
			List<JSONObject> gList = new ArrayList<JSONObject>();
			for(String groupId:groupIds){
				JSONObject group = new JSONObject();
				group.put("groupId", groupId);
				long cTime = new Date().getTime();
				if(groupTimeMap.containsKey(groupId)){
					cTime = groupTimeMap.get(groupId);
				}
				group.put("cTime", cTime);
				String ggKey = classId+"_"+groupId;
				if(groupList.containsKey(ggKey) ){
					List<JSONObject> courseList = groupList.get(ggKey);
					group.put("courseList", courseList);
					gList.add(group);
				}
			}
			gList = SortUtil.sortListByTime(gList, "cTime", "asc", "", "");
			for(JSONObject g:gList){
				g.remove("cTime");
				g.remove("cTimepm");
			}
			rsObj.put("groupList", gList);
			rs.add(rsObj);
		}
		try {
			rs = (List<JSONObject>) Sort.sort(SortEnum.ascEnding0rder, rs, "className");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return rs;
	}

	/**
	 * 将记录转换为前端需要的样式
	 * @param courseDic
	 * @param teacherDic
	 * @param AdjustType
	 * @param groupTpList
	 * @param orCourseId
	 * @param OriginalTeachers
	 */
	private static void addRecordToStepList(Map<String, LessonInfo> courseDic,
			Map<String, Account> teacherDic, int AdjustType,
			List<JSONObject> groupTpList, String orCourseId,
			String OriginalTeachers) {
		JSONObject gener = new JSONObject();
		
		gener.put("courseId", orCourseId);
		String csName = "[已删除]";
		if(courseDic.containsKey(orCourseId)){
			csName = courseDic.get(orCourseId).getName();
		}
		gener.put("courseName", csName);
		gener.put("AdjustType", AdjustType+"");
		if(OriginalTeachers!=null &&OriginalTeachers.trim().length()>0){
			String[] tids = OriginalTeachers.split(",");
			for(int i=0;i<tids.length;i++){
				String tid = tids[i];
				
				String tName = "[已删除]";
				if(teacherDic.containsKey(tid)){
					tName = teacherDic.get(tid).getName();
				}else{
					tid = "";
					
				}
				JSONObject generc = new JSONObject();
				generc.put("courseId", orCourseId);
				generc.put("courseName", csName);
				generc.put("AdjustType", AdjustType+"");
				generc.put("teacherId", tid);
				generc.put("teacherName", tName);
				groupTpList.add(generc );
			}
		}else{
			String tName = "[已删除]";
			String tid = OriginalTeachers;
			if(tid!=null&&tid.trim().length()>0
					){
				
				if(teacherDic.containsKey(tid )){
					tName = teacherDic.get(tid).getName();
				} else{
					tid = "";
				}
				gener.put("teacherName", tName);
			}else{
				gener.put("teacherName","");
				tid = "";
			}
			gener.put("teacherId", tid);
			groupTpList.add(gener);
		}
	}

	/**
	 * 移除重复位置
	 * @param timetable
	 * @return
	 */
	public static List<JSONObject> removeRepeatPositions(
			List<JSONObject> timetable) {
		// TODO Auto-generated method stub
		for(JSONObject obj:timetable){
			HashMap<String,String> existsPoss = new HashMap<String, String>();
			List<JSONObject> conflicts = (List<JSONObject>) obj.get("conflicts");
			for(JSONObject conf:conflicts){
				int day = conf.getIntValue("dayOfWeek");
				int lesson = conf.getIntValue("lessonOfDay");
				int status = conf.getIntValue("Status");
				String confType = conf.getString("confType");
				existsPoss.put(day+","+lesson,status+","+confType);
			}
			
			conflicts = new ArrayList<JSONObject>();
			for(Iterator<String> it = existsPoss.keySet().iterator();it.hasNext();){
				String posKey = it.next();
				int day = Integer.parseInt( posKey.split(",")[0] );
				int lesson = Integer.parseInt( posKey.split(",")[1] );
				
				String msg = existsPoss.get(posKey);
				int status = Integer.parseInt( msg.split(",")[0] );
				String confType =  msg.split(",")[1];
				
				JSONObject conf = new JSONObject();
				conf.put("dayOfWeek", day);
				conf.put("lessonOfDay", lesson);
				conf.put("Status", status);
				conf.put("confType", confType);
				conflicts.add(conf);
			}
			//重置冲突
			obj.put("conflicts", conflicts);
		}
		return timetable;
	}
}
