package com.talkweb.jasperReport.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.jasperReport.bean.SchooltableBean;
import com.talkweb.jasperReport.bean.Timetable;
import com.talkweb.jasperReport.bean.TimetableBean;
import com.talkweb.jasperReport.service.TimetablePrinterService;
import com.talkweb.jasperReport.util.Constant;
import com.talkweb.jasperReport.util.WeekSectionTool;

/**
 * 
 * @author XFQ
 *
 */

@Service
public class TimetablePrinterServiceImpl implements TimetablePrinterService{

	static String[] less={"一","二","三","四","五","六","七","八","九","十"};
	
	@Override
	@SuppressWarnings("unchecked") 
	public List<Timetable> getClassList(List<JSONObject> datas,JSONObject setInfo) {
		List<TreeMap<String, JSONObject>> maplist = new ArrayList<TreeMap<String, JSONObject>>();
		for (JSONObject json : datas) {
			TreeMap<String, JSONObject> map = new TreeMap<String, JSONObject>();
			List<JSONObject> tlist = (List<JSONObject>) json.get("timetable");
			TreeMap<String, JSONObject> mono = new TreeMap<String, JSONObject>();
			for (JSONObject o : tlist) {
				 String key = o.getString("lessonOfDay") + o.getString("dayOfWeek");
				 map.put("data", json);
				 if (StringUtils.isNotEmpty(o.getString("courseType"))
						 &&!o.getString("courseType").equals("0")){
					 if (mono.containsKey(key)){
						 JSONObject src = mono.get(key);
						if (!o.getString("courseName").contains(
								src.getString("courseName"))) {
							o.put("courseName", src.getString("courseName")
									+ "|" + o.getString("courseName"));
						}
						if (!o.getString("teacherName").contains(
								src.getString("teacherName"))) {
							o.put("teacherName", src.getString("teacherName")
									+ "|" + o.getString("teacherName"));
						} 
					 }else{
						 mono.put(key, o);
					 }
				 }
				 map.put(key,o);	  
			}
			maplist.add(map);
		}
 
		List<Timetable> tablelist=new ArrayList<Timetable>();

		for (TreeMap<String, JSONObject> t : maplist) {
			int[] maxDayAndLessons = getMaxDayAndLessons(t.get("data").getIntValue("totalMaxDays") , t.get("data").getIntValue("totalMaxLessons"));
			Timetable table=new Timetable();
			List<TimetableBean> tlist=new ArrayList<TimetableBean>();
			for (int i = 0; i < maxDayAndLessons[1]; i++) {
			TimetableBean tb = new TimetableBean();
			tb.setLessDay("第"+less[i]+"节");
			JSONArray scheduleTime = t.get("data").getJSONArray("scheduleTime");
			if (scheduleTime!=null  && scheduleTime.size() > 0) {
				if (i >= scheduleTime.size()) {
					break;
				}
				JSONObject object = scheduleTime.getJSONObject(i);
				if (object!=null && (i +"").equals(object.getString("sectionId"))) {//速度快
					if (StringUtils.isNotBlank(object.getString("startTime")) && StringUtils.isNotBlank(object.getString("endTime"))) {
						tb.setTimeZone(object.getString("startTime") +"--" + object.getString("endTime"));
					}
				}else {
					for (int m = 0; m < scheduleTime.size(); m++) {//顺序不匹配，那就遍历
						 object = scheduleTime.getJSONObject(m);
						 if (object!=null && (i +"").equals(object.getString("sectionId"))) {
								if (StringUtils.isNotBlank(object.getString("startTime")) && StringUtils.isNotBlank(object.getString("endTime"))) {
									tb.setTimeZone(object.getString("startTime") +"--" + object.getString("endTime"));
						        }
								break;
					     }
					}
					
					
				}
				 
			}
			
				for (int k = 0; k < maxDayAndLessons[0]; k++) {
					
					if (t.get(String.valueOf(i) + String.valueOf(k)) != null) {
						if (k == 0) {
							tb.setDayOneLess(t.get(
									String.valueOf(i) + String.valueOf(k))
									.getString("courseName"));
							tb.setDayOneTeacher(t.get(
									String.valueOf(i) + String.valueOf(k))
									.getString("teacherName"));
						} else if (k == 1) {
							tb.setDayTwoLess(t.get(
									String.valueOf(i) + String.valueOf(k))
									.getString("courseName"));
							tb.setDayTwoTeacher(t.get(
									String.valueOf(i) + String.valueOf(k))
									.getString("teacherName"));
						} else if (k == 2) {
							tb.setDayThrLess(t.get(
									String.valueOf(i) + String.valueOf(k))
									.getString("courseName"));
							tb.setDayThrTeacher(t.get(
									String.valueOf(i) + String.valueOf(k))
									.getString("teacherName"));
						} else if (k == 3) {
							tb.setDayFourLess(t.get(
									String.valueOf(i) + String.valueOf(k))
									.getString("courseName"));
							tb.setDayFourTeacher(t.get(
									String.valueOf(i) + String.valueOf(k))
									.getString("teacherName"));
						} else if (k == 4) {
							tb.setDayFivLess(t.get(
									String.valueOf(i) + String.valueOf(k))
									.getString("courseName"));
							tb.setDayFivTeacher(t.get(
									String.valueOf(i) + String.valueOf(k))
									.getString("teacherName"));
						} else if (k == 5) {
							tb.setDaySixLess(t.get(
									String.valueOf(i) + String.valueOf(k))
									.getString("courseName"));
							tb.setDaySixTeacher(t.get(
									String.valueOf(i) + String.valueOf(k))
									.getString("teacherName"));
						} else if (k == 6) {
							tb.setDaySevLess(t.get(
									String.valueOf(i) + String.valueOf(k))
									.getString("courseName"));
							tb.setDaySevTeacher(t.get(
									String.valueOf(i) + String.valueOf(k))
									.getString("teacherName"));
						} else if (k == 7) {
							tb.setDayEigLess(t.get(
									String.valueOf(i) + String.valueOf(k))
									.getString("courseName"));
							tb.setDayEigTeacher(t.get(
									String.valueOf(i) + String.valueOf(k))
									.getString("teacherName"));
						} else if (k == 8) {
							tb.setDayNinLess(t.get(
									String.valueOf(i) + String.valueOf(k))
									.getString("courseName"));
							tb.setDayNinTeacher(t.get(
									String.valueOf(i) + String.valueOf(k))
									.getString("teacherName"));
						}
					} 
				}
				tlist.add(tb);
			}
			table.setClassName(t.get("data").getString("className"));
			table.setHeadTeacher(t.get("data").getString("teacherName") != null ? 
					t.get("data").getString("teacherName") : "");
			table.setTimedata(tlist);
			if (setInfo.containsKey("title")){
				table.setTimeTableName(setInfo.getString("title"));
			}else{
				table.setTimeTableName(t.get("data").getString("tableName"));
			}
			table.setBottomNote1(setInfo.getString("bottomNote1"));
			table.setBottomNote2(setInfo.getString("bottomNote2"));
			table.setBottomNote3(setInfo.getString("bottomNote3"));
			tablelist.add(table);
		}
		return tablelist;
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Timetable> getTwoClassList(List<JSONObject> datas,JSONObject setInfo) {
		List<TreeMap<String, JSONObject>> maplist = new ArrayList<TreeMap<String, JSONObject>>();
		for (JSONObject json : datas) {
			TreeMap<String, JSONObject> map = new TreeMap<String, JSONObject>();
			List<JSONObject> tlist = (List<JSONObject>) json.get("timetable");
			TreeMap<String, JSONObject> mono = new TreeMap<String, JSONObject>();
			for (JSONObject o : tlist) {
				 String key = o.getString("lessonOfDay") + o.getString("dayOfWeek");
				 map.put("data", json);
				 if (StringUtils.isNotEmpty(o.getString("courseType"))
						 &&!o.getString("courseType").equals("0")){
					 if (mono.containsKey(key)){
						 JSONObject src = mono.get(key);
						if (!o.getString("courseName").contains(
								src.getString("courseName"))) {
							o.put("courseName", src.getString("courseName")
									+ "|" + o.getString("courseName"));
						}
						if (!o.getString("teacherName").contains(
								src.getString("teacherName"))) {
							o.put("teacherName", src.getString("teacherName")
									+ "|" + o.getString("teacherName"));
						} 
					 }else{
						 mono.put(key, o);
					 }
				 }
				 map.put(key,o);	  
			}
			maplist.add(map);
		}
 
		List<Timetable> tablelist=new ArrayList<Timetable>();
		Timetable table=new Timetable();
		for (int t=0;t<maplist.size();t++) {	
			int[] maxDayAndLessons = getMaxDayAndLessons(maplist.get(t).get("data").getIntValue("totalMaxDays") , maplist.get(t).get("data").getIntValue("totalMaxLessons"));
			List<TimetableBean> tlist=new ArrayList<TimetableBean>();		
			for (int i = 0; i < maxDayAndLessons[1]; i++) {
			TimetableBean tb = new TimetableBean();
			tb.setLessDay("第"+less[i]+"节");
			JSONArray scheduleTime = maplist.get(t).get("data").getJSONArray("scheduleTime");
			if (scheduleTime!=null  && scheduleTime.size() > 0) {
				if (i >= scheduleTime.size()) {
					break;
				}
				JSONObject object = scheduleTime.getJSONObject(i);
				if (object!=null && (i +"").equals(object.getString("sectionId"))) {//速度快
					if (StringUtils.isNotBlank(object.getString("startTime")) && StringUtils.isNotBlank(object.getString("endTime"))) {
						tb.setTimeZone(object.getString("startTime") +"--" + object.getString("endTime"));
					}
				}else {
					for (int m = 0; m < scheduleTime.size(); m++) {//顺序不匹配，那就遍历
						 object = scheduleTime.getJSONObject(m);
						 if (object!=null && (i +"").equals(object.getString("sectionId"))) {
								if (StringUtils.isNotBlank(object.getString("startTime")) && StringUtils.isNotBlank(object.getString("endTime"))) {
									tb.setTimeZone(object.getString("startTime") +"--" + object.getString("endTime"));
						        }
								break;
					     }
					}
					
					
				}
				 
			}
			
				for (int k = 0; k < maxDayAndLessons[0]; k++) {
					
					if (maplist.get(t).get(
							String.valueOf(i) + String.valueOf(k)) != null) {
						if (k == 0) {
							tb.setDayOneLess(maplist.get(t)
									.get(String.valueOf(i) + String.valueOf(k))
									.getString("courseName"));
							tb.setDayOneTeacher(maplist.get(t)
									.get(String.valueOf(i) + String.valueOf(k))
									.getString("teacherName"));
						} else if (k == 1) {
							tb.setDayTwoLess(maplist.get(t)
									.get(String.valueOf(i) + String.valueOf(k))
									.getString("courseName"));
							tb.setDayTwoTeacher(maplist.get(t)
									.get(String.valueOf(i) + String.valueOf(k))
									.getString("teacherName"));
						} else if (k == 2) {
							tb.setDayThrLess(maplist.get(t)
									.get(String.valueOf(i) + String.valueOf(k))
									.getString("courseName"));
							tb.setDayThrTeacher(maplist.get(t)
									.get(String.valueOf(i) + String.valueOf(k))
									.getString("teacherName"));
						} else if (k == 3) {
							tb.setDayFourLess(maplist.get(t)
									.get(String.valueOf(i) + String.valueOf(k))
									.getString("courseName"));
							tb.setDayFourTeacher(maplist.get(t)
									.get(String.valueOf(i) + String.valueOf(k))
									.getString("teacherName"));
						} else if (k == 4) {
							tb.setDayFivLess(maplist.get(t)
									.get(String.valueOf(i) + String.valueOf(k))
									.getString("courseName"));
							tb.setDayFivTeacher(maplist.get(t)
									.get(String.valueOf(i) + String.valueOf(k))
									.getString("teacherName"));
						} else if (k == 5) {
							tb.setDaySixLess(maplist.get(t)
									.get(String.valueOf(i) + String.valueOf(k))
									.getString("courseName"));
							tb.setDaySixTeacher(maplist.get(t)
									.get(String.valueOf(i) + String.valueOf(k))
									.getString("teacherName"));
						} else if (k == 6) {
							tb.setDaySevLess(maplist.get(t)
									.get(String.valueOf(i) + String.valueOf(k))
									.getString("courseName"));
							tb.setDaySevTeacher(maplist.get(t)
									.get(String.valueOf(i) + String.valueOf(k))
									.getString("teacherName"));
						} else if (k == 7) {
							tb.setDayEigLess(maplist.get(t)
									.get(String.valueOf(i) + String.valueOf(k))
									.getString("courseName"));
							tb.setDayEigTeacher(maplist.get(t)
									.get(String.valueOf(i) + String.valueOf(k))
									.getString("teacherName"));
						} else if (k == 8) {
							tb.setDayNinLess(maplist.get(t)
									.get(String.valueOf(i) + String.valueOf(k))
									.getString("courseName"));
							tb.setDayNinTeacher(maplist.get(t)
									.get(String.valueOf(i) + String.valueOf(k))
									.getString("teacherName"));
						}
					} 
				}
				tlist.add(tb);
			}
			if ((t & 1) == 0) {// 偶数
				table.setTimedata(tlist);
				if (setInfo.containsKey("title")){
					table.setTimeTableName(setInfo.getString("title"));
				}else{
					table.setTimeTableName(maplist.get(t).get("data").getString("tableName"));
				}
				table.setClassName(maplist.get(t).get("data")
						.getString("className"));
				table.setHeadTeacher(maplist.get(t).get("data")
						.getString("teacherName") != null ? maplist.get(t)
						.get("data").getString("teacherName") : "");
				table.setBottomNote1(setInfo.getString("bottomNote1"));
				table.setBottomNote2(setInfo.getString("bottomNote2"));
				table.setBottomNote3(setInfo.getString("bottomNote3"));
				if (t == maplist.size() - 1) {
					table.setTimeTableNametwo("");
					tablelist.add(table);
				}								
			} else {
				table.setTimedatatwo(tlist);
				if (setInfo.containsKey("title")){
					table.setTimeTableNametwo(setInfo.getString("title"));
				}else{
					table.setTimeTableNametwo(maplist.get(t).get("data").getString("tableName"));
				}
				table.setClassNametwo(maplist.get(t).get("data")
						.getString("className"));
				table.setHeadTeachertwo(maplist.get(t).get("data")
						.getString("teacherName") != null ? maplist.get(t)
						.get("data").getString("teacherName") : "");
				table.setBottomNote1(setInfo.getString("bottomNote1"));
				table.setBottomNote2(setInfo.getString("bottomNote2"));
				table.setBottomNote3(setInfo.getString("bottomNote3"));
				tablelist.add(table);
				table=new Timetable();
			}
		}
		return tablelist;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<Timetable> getTeacherList(List<JSONObject> datas,JSONObject setInfo) {
		List<TreeMap<String, JSONObject>> maplist = new ArrayList<TreeMap<String, JSONObject>>();
		for (JSONObject json : datas) {
			TreeMap<String, JSONObject> map = new TreeMap<String, JSONObject>();
			List<JSONObject> tlist = (List<JSONObject>) json.get("timetable");
			TreeMap<String, JSONObject> mono = new TreeMap<String, JSONObject>();
			for (JSONObject o : tlist) {
				 String key = o.getString("lessonOfDay") + o.getString("dayOfWeek");
				 map.put("data", json);
				 if (StringUtils.isNotEmpty(o.getString("courseType"))
						 &&!o.getString("courseType").equals("0")){
					 if (mono.containsKey(key)){
						 JSONObject src = mono.get(key);
						if (!o.getString("courseName").contains(
								src.getString("courseName"))) {
							o.put("courseName", src.getString("courseName")
									+ "|" + o.getString("courseName"));
						}
						if (!o.getString("className").contains(
								src.getString("className"))) {
							o.put("className", src.getString("className")
									+ "|" + o.getString("className"));
						} 
					 }else{
						 mono.put(key, o);
					 }
				 }
				 map.put(key,o);	  
			}
			maplist.add(map);
		}
 
		List<Timetable> tablelist=new ArrayList<Timetable>();
		for (TreeMap<String, JSONObject> t : maplist) {
			int[] maxDayAndLessons = getMaxDayAndLessons(t.get("data").getIntValue("totalMaxDays") , t.get("data").getIntValue("totalMaxLessons"));
			Timetable table=new Timetable();
			List<TimetableBean> tlist=new ArrayList<TimetableBean>();
			for (int i = 0; i < maxDayAndLessons[1]; i++) {
			TimetableBean tb = new TimetableBean();
			
			tb.setLessDay("第"+less[i]+"节");
			JSONArray scheduleTime = t.get("data").getJSONArray("scheduleTime");
			if (scheduleTime!=null && scheduleTime.size() > 0) {
				if(i >= scheduleTime.size()){
					break;
				}
				JSONObject object = scheduleTime.getJSONObject(i);
				if (object!=null && (i +"").equals(object.getString("sectionId"))) {
					if (StringUtils.isNotBlank(object.getString("startTime")) && StringUtils.isNotBlank(object.getString("endTime"))) {
						tb.setTimeZone(object.getString("startTime") +"--" + object.getString("endTime"));
					}
				}else {
					for (int m = 0; m < scheduleTime.size(); m++) {
						 object = scheduleTime.getJSONObject(m);
						 if (object!=null && (i +"").equals(object.getString("sectionId"))) {
								if (StringUtils.isNotBlank(object.getString("startTime")) && StringUtils.isNotBlank(object.getString("endTime"))) {
									tb.setTimeZone(object.getString("startTime") +"--" + object.getString("endTime"));
						        }
								break;
					     }
					}
					
					
				}
				 
			}
			
			
				for (int k = 0; k < maxDayAndLessons[0]; k++) {

					if (t.get(String.valueOf(i) + String.valueOf(k)) != null) {
						if (k == 0) {
							tb.setDayOneLess(t.get(
									String.valueOf(i) + String.valueOf(k))
									.getString("courseName"));
							tb.setDayOneClass(t.get(
									String.valueOf(i) + String.valueOf(k))
									.getString("className"));
						} else if (k == 1) {
							tb.setDayTwoLess(t.get(
									String.valueOf(i) + String.valueOf(k))
									.getString("courseName"));
							tb.setDayTwoClass(t.get(
									String.valueOf(i) + String.valueOf(k))
									.getString("className"));
						} else if (k == 2) {
							tb.setDayThrLess(t.get(
									String.valueOf(i) + String.valueOf(k))
									.getString("courseName"));
							tb.setDayThrClass(t.get(
									String.valueOf(i) + String.valueOf(k))
									.getString("className"));
						} else if (k == 3) {
							tb.setDayFourLess(t.get(
									String.valueOf(i) + String.valueOf(k))
									.getString("courseName"));
							tb.setDayFourClass(t.get(
									String.valueOf(i) + String.valueOf(k))
									.getString("className"));
						} else if (k == 4) {
							tb.setDayFivLess(t.get(
									String.valueOf(i) + String.valueOf(k))
									.getString("courseName"));
							tb.setDayFivClass(t.get(
									String.valueOf(i) + String.valueOf(k))
									.getString("className"));
						}else if (k == 5) {
							tb.setDaySixLess(t.get(
									String.valueOf(i) + String.valueOf(k))
									.getString("courseName"));
							tb.setDaySixClass(t.get(
									String.valueOf(i) + String.valueOf(k))
									.getString("className"));
						}
						else if (k == 6) {
							tb.setDaySevLess(t.get(
									String.valueOf(i) + String.valueOf(k))
									.getString("courseName"));
							tb.setDaySevClass(t.get(
									String.valueOf(i) + String.valueOf(k))
									.getString("className"));
						}
						else if (k == 7) {
							tb.setDayEigLess(t.get(
									String.valueOf(i) + String.valueOf(k))
									.getString("courseName"));
							tb.setDayEigClass(t.get(
									String.valueOf(i) + String.valueOf(k))
									.getString("className"));
						}
						else if (k == 8) {
							tb.setDayNinLess(t.get(
									String.valueOf(i) + String.valueOf(k))
									.getString("courseName"));
							tb.setDayNinClass(t.get(
									String.valueOf(i) + String.valueOf(k))
									.getString("className"));
						}
					} 
				}
				tlist.add(tb);
			}	
			table.setTimedata(tlist);
			if (setInfo.containsKey("title")){
				table.setTimeTableName(setInfo.getString("title"));
			}else{
				table.setTimeTableName(t.get("data").getString("teacherName")+"老师课程表");
			}
			table.setTeacherId(t.get("data").getString("teacherId"));
			table.setTeacherName(t.get("data").getString("teacherName"));
			table.setGradeName(t.get("data").getString("gradeName"));
			table.setResearchGroup(t.get("data").getString("researchGroup"));		
			table.setBottomNote1(setInfo.getString("bottomNote1"));
			table.setBottomNote2(setInfo.getString("bottomNote2"));
			table.setBottomNote3(setInfo.getString("bottomNote3"));
			tablelist.add(table);
		}
		return tablelist;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<Timetable> getTwoTeacherList(List<JSONObject> datas,JSONObject setInfo) {
		List<TreeMap<String, JSONObject>> maplist = new ArrayList<TreeMap<String, JSONObject>>();
		for (JSONObject json : datas) {
			TreeMap<String, JSONObject> map = new TreeMap<String, JSONObject>();
			List<JSONObject> tlist = (List<JSONObject>) json.get("timetable");
			TreeMap<String, JSONObject> mono = new TreeMap<String, JSONObject>();
			for (JSONObject o : tlist) {
				 String key = o.getString("lessonOfDay") + o.getString("dayOfWeek");
				 map.put("data", json);
				 if (StringUtils.isNotEmpty(o.getString("courseType"))
						 &&!o.getString("courseType").equals("0")){
					 if (mono.containsKey(key)){
						 JSONObject src = mono.get(key);
						if (!o.getString("courseName").contains(
								src.getString("courseName"))) {
							o.put("courseName", src.getString("courseName")
									+ "|" + o.getString("courseName"));
						}
						if (!o.getString("className").contains(
								src.getString("className"))) {
							o.put("className", src.getString("className")
									+ "|" + o.getString("className"));
						} 
					 }else{
						 mono.put(key, o);
					 }
				 }
				 map.put(key,o);	  
			}
			maplist.add(map);
		}
		 
		List<Timetable> tablelist=new ArrayList<Timetable>();			
		Timetable table=new Timetable();
		for (int t=0;t<maplist.size();t++) {
			int[] maxDayAndLessons = getMaxDayAndLessons(maplist.get(t).get("data").getIntValue("totalMaxDays") , maplist.get(t).get("data").getIntValue("totalMaxLessons"));
			List<TimetableBean> tlist=new ArrayList<TimetableBean>();
			for (int i = 0; i < maxDayAndLessons[1]; i++) {
			TimetableBean tb = new TimetableBean();
			tb.setLessDay("第"+less[i]+"节");
			
			JSONArray scheduleTime = maplist.get(t).get("data").getJSONArray("scheduleTime");
			if (scheduleTime!=null &&  scheduleTime.size() > 0) {
				if (i >= scheduleTime.size()) {
					break;
				}
				JSONObject object = scheduleTime.getJSONObject(i);
				if (object!=null && (i +"").equals(object.getString("sectionId"))) {
					if (StringUtils.isNotBlank(object.getString("startTime")) && StringUtils.isNotBlank(object.getString("endTime"))) {
						tb.setTimeZone(object.getString("startTime") +"--" + object.getString("endTime"));
					}
				}else {
					for (int m = 0; m < scheduleTime.size(); m++) {
						 object = scheduleTime.getJSONObject(m);
						 if (object!=null && (i +"").equals(object.getString("sectionId"))) {
								if (StringUtils.isNotBlank(object.getString("startTime")) && StringUtils.isNotBlank(object.getString("endTime"))) {
									tb.setTimeZone(object.getString("startTime") +"--" + object.getString("endTime"));
						        }
								break;
					     }
					}
					
					
				}
				 
			}
			
				for (int k = 0; k < maxDayAndLessons[0]; k++) {
					
					if (maplist.get(t).get(String.valueOf(i) + String.valueOf(k)) != null) {
						if (k == 0) {
							tb.setDayOneLess(maplist.get(t).get(
									String.valueOf(i) + String.valueOf(k))
									.getString("courseName"));
							tb.setDayOneClass(maplist.get(t).get(
									String.valueOf(i) + String.valueOf(k))
									.getString("className"));
						} else if (k == 1) {
							tb.setDayTwoLess(maplist.get(t).get(
									String.valueOf(i) + String.valueOf(k))
									.getString("courseName"));
							tb.setDayTwoClass(maplist.get(t).get(
									String.valueOf(i) + String.valueOf(k))
									.getString("className"));
						} else if (k == 2) {
							tb.setDayThrLess(maplist.get(t).get(
									String.valueOf(i) + String.valueOf(k))
									.getString("courseName"));
							tb.setDayThrClass(maplist.get(t).get(
									String.valueOf(i) + String.valueOf(k))
									.getString("className"));
						} else if (k == 3) {
							tb.setDayFourLess(maplist.get(t).get(
									String.valueOf(i) + String.valueOf(k))
									.getString("courseName"));
							tb.setDayFourClass(maplist.get(t).get(
									String.valueOf(i) + String.valueOf(k))
									.getString("className"));
						} else if (k == 4) {
							tb.setDayFivLess(maplist.get(t).get(
									String.valueOf(i) + String.valueOf(k))
									.getString("courseName"));
							tb.setDayFivClass(maplist.get(t).get(
									String.valueOf(i) + String.valueOf(k))
									.getString("className"));
						}else if (k == 5) {
							tb.setDaySixLess(maplist.get(t).get(
									String.valueOf(i) + String.valueOf(k))
									.getString("courseName"));
							tb.setDaySixClass(maplist.get(t).get(
									String.valueOf(i) + String.valueOf(k))
									.getString("className"));
						}else if (k == 6) {
							tb.setDaySevLess(maplist.get(t).get(
									String.valueOf(i) + String.valueOf(k))
									.getString("courseName"));
							tb.setDaySevClass(maplist.get(t).get(
									String.valueOf(i) + String.valueOf(k))
									.getString("className"));
						}else if (k == 7) {
							tb.setDayEigLess(maplist.get(t).get(
									String.valueOf(i) + String.valueOf(k))
									.getString("courseName"));
							tb.setDayEigClass(maplist.get(t).get(
									String.valueOf(i) + String.valueOf(k))
									.getString("className"));
						}
						else if (k == 8) {
							tb.setDayNinLess(maplist.get(t).get(
									String.valueOf(i) + String.valueOf(k))
									.getString("courseName"));
							tb.setDayNinClass(maplist.get(t).get(
									String.valueOf(i) + String.valueOf(k))
									.getString("className"));
						}
					} 
				}
				tlist.add(tb);
			}
			if((t&1)==0){//偶数
				table.setTimedata(tlist);
				if (setInfo.containsKey("title")){
					table.setTimeTableName(setInfo.getString("title"));
				}else{
					table.setTimeTableName(maplist.get(t).get("data").getString("teacherName")+"老师课程表");
				}
				table.setTeacherId(maplist.get(t).get("data").getString("teacherId"));
				table.setTeacherName(maplist.get(t).get("data").getString("teacherName"));
				table.setGradeName(maplist.get(t).get("data").getString("gradeName"));
				table.setResearchGroup(maplist.get(t).get("data").getString("researchGroup"));		
				table.setBottomNote1(setInfo.getString("bottomNote1"));
				table.setBottomNote2(setInfo.getString("bottomNote2"));
				table.setBottomNote3(setInfo.getString("bottomNote3"));
				if(t==maplist.size()-1){
					table.setTimeTableNametwo("");
					tablelist.add(table);
				}
			}else{
				table.setTimedatatwo(tlist);
				if (setInfo.containsKey("title")){
					table.setTimeTableNametwo(setInfo.getString("title"));
				}else{
					table.setTimeTableNametwo(maplist.get(t).get("data").getString("teacherName")+"老师课程表");
				}	
				table.setTeacherIdtwo(maplist.get(t).get("data").getString("teacherId"));
				table.setTeacherNametwo(maplist.get(t).get("data").getString("teacherName"));
				table.setGradeNametwo(maplist.get(t).get("data").getString("gradeName"));
				table.setResearchGrouptwo(maplist.get(t).get("data").getString("researchGroup"));
				table.setBottomNote1(setInfo.getString("bottomNote1"));
				table.setBottomNote2(setInfo.getString("bottomNote2"));
				table.setBottomNote3(setInfo.getString("bottomNote3"));
				tablelist.add(table);
				table=new Timetable();
			}
		}
		return tablelist;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<Timetable> getSchoolClassList(List<JSONObject> datas,JSONObject setInfo) {
		HashMap<String,String> nameMap=new HashMap<String,String>();
		List<JSONObject> maplist = new ArrayList<JSONObject>();
		String nameKey = setInfo.getString("courseShowName");
		for (JSONObject json : datas) {
			List<JSONObject> dlist = (List<JSONObject>) json.get("data");
			List<TreeMap<String,JSONObject>> ff=new ArrayList<TreeMap<String,JSONObject>>();
			List<String> cidlist=new ArrayList<String>();
			JSONObject datamap=new JSONObject();
			for(JSONObject o : dlist) {
				List<JSONObject> tlist = (List<JSONObject>) o.get("timetable");
				TreeMap<String, JSONObject> map = new TreeMap<String, JSONObject>();
				TreeMap<String, JSONObject> mono = new TreeMap<String, JSONObject>();
				for (JSONObject t : tlist) {
					String classId = o.getString("ClassId");
					if(null==classId && o.get("classId")!=null){
						classId = o.getString("classId");
						o.put("ClassId", classId);
					}
					String key = classId
							+ t.getString("dayOfWeek") + t.getString("lessonOfDay");
					 if (StringUtils.isNotEmpty(t.getString("courseType"))
							 &&!t.getString("courseType").equals("0")){
						 if (mono.containsKey(key)){
							 JSONObject src = mono.get(key);
							if (!t.getString(nameKey).contains(
									src.getString(nameKey))) {
								t.put(nameKey, src.getString(nameKey)
										+ "|" + t.getString(nameKey));
							}
							if (!t.getString("teacherName").contains(
									src.getString("teacherName"))) {
								t.put("teacherName", src.getString("teacherName")
										+ "|" + t.getString("teacherName"));
							} 
						 }else{
							 mono.put(key, t);
						 }
					 }
					 map.put(key,t);	  
				}
				ff.add(map);
				cidlist.add(o.getString("ClassId"));
				nameMap.put(o.getString("ClassId"), o.getString("className"));
			}
			datamap.put("data", ff);
			datamap.put("cid", cidlist);
			datamap.put("tableName", json.getString("tableName"));
			
			maplist.add(datamap);
		}
		int lessofday = setInfo.getIntValue("totalMaxLessons");
		int dayofweek = setInfo.getIntValue("totalMaxDays");
		List<Timetable> tablelist=new ArrayList<Timetable>();
		for (JSONObject t : maplist) {
			 List<TreeMap<String,JSONObject>> ff=(List<TreeMap<String, JSONObject>>) t.get("data");
			 List<String> cidlist=(List<String>) t.get("cid");			 
			// 最大20条一页	
			int mapsize = cidlist.size();
			int times = mapsize/20 + ((mapsize%20) > 0 ? 1 : 0);
			for (int fq = 1;fq <= times;fq++){
				 int startNum = (fq-1)*20;   	    	   
			     int endNum = startNum + 20;
			     if (endNum > mapsize)endNum = mapsize;
			     List<String> tempcidlist = cidlist.subList(startNum, endNum);
			     List<TreeMap<String,JSONObject>> tempfflist = ff.subList(startNum, endNum);
					Timetable table=new Timetable(); 
					List<SchooltableBean> tlist=new ArrayList<SchooltableBean>();			
					for (int i=0;i<tempfflist.size();i++){
						 SchooltableBean tb = new SchooltableBean();
						 String cid=tempcidlist.get(i);
						 tb.setRowName(nameMap.get(cid));
						 for (int k = 0; k < dayofweek; k++) {
							for (int h = 0; h < lessofday; h++) {
								if (tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)) != null) {
									if(k==0){
										if (h == 0) {
											tb.setMondayFirName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
											tb.setMondayFirNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("teacherName"));
										} else if (h == 1) {
											tb.setMondaySecName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
											tb.setMondaySecNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("teacherName"));
										} else if (h == 2) {
											tb.setMondayThrName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
											tb.setMondayThrNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("teacherName"));
										} else if (h == 3) {
											tb.setMondayFouName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
											tb.setMondayFouNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("teacherName"));
										} else if (h == 4) {
											tb.setMondayFivName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
											tb.setMondayFivNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("teacherName"));
										}
										else if (h == 5) {
											tb.setMondaySixName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
											tb.setMondaySixNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("teacherName"));
										}
										else if (h == 6) {
											tb.setMondaySevNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("teacherName"));
											tb.setMondaySevName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
										}
										else if (h == 7) {
											tb.setMondayEigNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("teacherName"));
											tb.setMondayEigName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
										}else if (h == 8) {
											tb.setMondayNiNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("teacherName"));
											tb.setMondayNiName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
										}
									}
									else if(k==1){
										if (h == 0) {
											tb.setTuesdayFirNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("teacherName"));
											tb.setTuesdayFirName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
										} else if (h == 1) {
											tb.setTuesdaySecNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("teacherName"));
											tb.setTuesdaySecName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
										} else if (h == 2) {
											tb.setTuesdayThrNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("teacherName"));
											tb.setTuesdayThrName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
										} else if (h == 3) {
											tb.setTuesdayFouNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("teacherName"));
											tb.setTuesdayFouName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
										} else if (h == 4) {
											tb.setTuesdayFivNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("teacherName"));
											tb.setTuesdayFivName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
										}
										else if (h == 5) {
											tb.setTuesdaySixNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("teacherName"));
											tb.setTuesdaySixName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
										}
										else if (h == 6) {
											tb.setTuesdaySevNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("teacherName"));
											tb.setTuesdaySevName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
										}
										else if (h == 7) {
											tb.setTuesdayEigNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("teacherName"));
											tb.setTuesdayEigName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
										}else if (h == 8) {
											tb.setTuesdayNiNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("teacherName"));
											tb.setTuesdayNiName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
										}
									}else if(k==2){
										if (h == 0) {
											tb.setWednesdayFirNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("teacherName"));
											tb.setWednesdayFirName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
										} else if (h == 1) {
											tb.setWednesdaySecNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("teacherName"));
											tb.setWednesdaySecName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
										} else if (h == 2) {
											tb.setWednesdayThrNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("teacherName"));
											tb.setWednesdayThrName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
										} else if (h == 3) {
											tb.setWednesdayFouNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("teacherName"));
											tb.setWednesdayFouName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
										} else if (h == 4) {
											tb.setWednesdayFivNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("teacherName"));
											tb.setWednesdayFivName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
										}
										else if (h == 5) {
											tb.setWednesdaySixNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("teacherName"));
											tb.setWednesdaySixName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
										}
										else if (h == 6) {
											tb.setWednesdaySevNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("teacherName"));
											tb.setWednesdaySevName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
										}
										else if (h == 7) {
											tb.setWednesdayEigNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("teacherName"));
											tb.setWednesdayEigName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
										}else if (h == 8) {
											tb.setWednesdayNiNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("teacherName"));
											tb.setWednesdayNiName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
										}
									}else if(k==3){
										if (h == 0) {
											tb.setThursdayFirNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("teacherName"));
											tb.setThursdayFirName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
										} else if (h == 1) {
											tb.setThursdaySecNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("teacherName"));
											tb.setThursdaySecName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
										} else if (h == 2) {
											tb.setThursdayThrNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("teacherName"));
											tb.setThursdayThrName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
										} else if (h == 3) {
											tb.setThursdayFouNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("teacherName"));
											tb.setThursdayFouName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
										} else if (h == 4) {
											tb.setThursdayFivNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("teacherName"));
											tb.setThursdayFivName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
										}
										else if (h == 5) {
											tb.setThursdaySixNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("teacherName"));
											tb.setThursdaySixName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
										}
										else if (h == 6) {
											tb.setThursdaySevNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("teacherName"));
											tb.setThursdaySevName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
										}
										else if (h == 7) {
											tb.setThursdayEigNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("teacherName"));
											tb.setThursdayEigName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
										}else if (h == 8) {
											tb.setThursdayNiNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("teacherName"));
											tb.setThursdayNiName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
										}
									}else if(k==4){
										if (h == 0) {
											tb.setFridayFirNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("teacherName"));
											tb.setFridayFirName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
										} 
										else if (h == 1) {
											tb.setFridaySecNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("teacherName"));
											tb.setFridaySecName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
										} 
										else if (h == 2) {
											tb.setFridayThrNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("teacherName"));
											tb.setFridayThrName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
										} 
										else if (h == 3) {
											tb.setFridayFouNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("teacherName"));
											tb.setFridayFouName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
										} 
										else if (h == 4) {
											tb.setFridayFivNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("teacherName"));
											tb.setFridayFivName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
										}
										else if (h == 5) {
											tb.setFridaySixNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("teacherName"));
											tb.setFridaySixName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
										}
										else if (h == 6) {
											tb.setFridaySevNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("teacherName"));
											tb.setFridaySevName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
										}
										else if (h == 7) {
											tb.setFridayEigNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("teacherName"));
											tb.setFridayEigName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
										}else if (h == 8) {
											tb.setFridayNiNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("teacherName"));
											tb.setFridayNiName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
										}
									}else if(k==5){
										if (h == 0) {
											tb.setSaturdayFirNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("teacherName"));
											tb.setSaturdayFirName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
										} 
										else if (h == 1) {
											tb.setSaturdaySecNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("teacherName"));
											tb.setSaturdaySecName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
										}
										else if (h == 2) {
											tb.setSaturdayThrNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("teacherName"));
											tb.setSaturdayThrName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
										} 
										else if (h == 3) {
											tb.setSaturdayFouNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("teacherName"));
											tb.setSaturdayFouName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
										} 
										else if (h == 4) {
											tb.setSaturdayFivNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("teacherName"));
											tb.setSaturdayFivName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
										}
										else if (h == 5) {
											tb.setSaturdaySixNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("teacherName"));
											tb.setSaturdaySixName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
										}
										else if (h == 6) {
											tb.setSaturdaySevNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("teacherName"));
											tb.setSaturdaySevName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
										}
										else if (h == 7) {
											tb.setSaturdayEigNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("teacherName"));
											tb.setSaturdayEigName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
										}else if (h == 8) {
											tb.setSaturdayNiNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("teacherName"));
											tb.setSaturdayNiName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
										}
									}else if(k==6){
										if (h == 0) {
											tb.setSundayFirNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("teacherName"));
											tb.setSundayFirName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
										} 
										else if (h == 1) {
											tb.setSundaySecNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("teacherName"));
											tb.setSundaySecName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
										} 
										else if (h == 2) {
											tb.setSundayThrNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("teacherName"));
											tb.setSundayThrName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
										} 
										else if (h == 3) {
											tb.setSundayFouNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("teacherName"));
											tb.setSundayFouName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
										} 
										else if (h == 4) {
											tb.setSundayFivNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("teacherName"));
											tb.setSundayFivName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
										}
										else if (h == 5) {
											tb.setSundaySixNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("teacherName"));
											tb.setSundaySixName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
										}
										else if (h == 6) {
											tb.setSundaySevNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("teacherName"));
											tb.setSundaySevName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
										}
										else if (h == 7) {
											tb.setSundayEigNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("teacherName"));
											tb.setSundayEigName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
										}else if (h == 8) {
											tb.setSundayNiNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("teacherName"));
											tb.setSundayNiName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
										}
									}			
								}
							}					
						}				
				tlist.add(tb);
			}
			table.setStimedata(tlist);
			if (setInfo.containsKey("title")){
				table.setTimeTableName(setInfo.getString("title"));
			}else{
				table.setTimeTableName(t.getString("tableName"));
			}
			table.setBottomNote1(setInfo.getString("bottomNote1"));
			table.setBottomNote2(setInfo.getString("bottomNote2"));
			table.setBottomNote3(setInfo.getString("bottomNote3"));
			tablelist.add(table);
			}	
		}	
		return tablelist;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<Timetable> getSchoolTeacherList(List<JSONObject> datas,JSONObject setInfo) {
		HashMap<String,String> nameMap=new HashMap<String,String>();
		List<JSONObject> maplist = new ArrayList<JSONObject>();
		String nameKey = setInfo.getString("courseShowName");
		for (JSONObject json : datas) {
			List<JSONObject> dlist = (List<JSONObject>) json.get("data");
			List<TreeMap<String,JSONObject>> ff=new ArrayList<TreeMap<String,JSONObject>>();
			List<String> cidlist=new ArrayList<String>();
			JSONObject datamap=new JSONObject();
			for (JSONObject o : dlist) {
				List<JSONObject> tlist = (List<JSONObject>) o.get("timetable");
				TreeMap<String, JSONObject> map = new TreeMap<String, JSONObject>();
				TreeMap<String, JSONObject> mono = new TreeMap<String, JSONObject>();
				for (JSONObject t : tlist) {
					String teacherId = o.getString("TeacherId");
					if(null==teacherId && null!=o.get("teacherId")){
						teacherId = o.getString("teacherId");
						o.put("TeacherId", teacherId);
					}
					String key = teacherId
							+ t.getString("dayOfWeek") + t.getString("lessonOfDay");
					 if (StringUtils.isNotEmpty(t.getString("courseType"))
							 &&!t.getString("courseType").equals("0")){
						 if (mono.containsKey(key)){
							 JSONObject src = mono.get(key);
							if (!t.getString(nameKey).contains(
									src.getString(nameKey))) {
								t.put(nameKey, src.getString(nameKey)
										+ "|" + t.getString(nameKey));
							}
							if (!t.getString("className").contains(
									src.getString("className"))) {
								t.put("className", src.getString("className")
										+ "|" + t.getString("className"));
							} 
						 }else{
							 mono.put(key, t);
						 }
					 }
					 map.put(key,t);	  
				}
				ff.add(map);
				cidlist.add(o.getString("TeacherId"));
				nameMap.put(o.getString("TeacherId"), o.getString("teacherName"));
			}
			datamap.put("data", ff);
			datamap.put("TeacherId", cidlist);
			datamap.put("tableName", json.getString("tableName"));
			
			maplist.add(datamap);
		}
		int lessofday = setInfo.getIntValue("totalMaxLessons");
		int dayofweek = setInfo.getIntValue("totalMaxDays");
		List<Timetable> tablelist=new ArrayList<Timetable>();
		for (JSONObject t : maplist) {			
			List<TreeMap<String,JSONObject>> ff=(List<TreeMap<String, JSONObject>>) t.get("data");
			List<String> cidlist=(List<String>) t.get("TeacherId");			
		    // 最大20条一页	
			int mapsize = cidlist.size();
			int times = mapsize/20 + ((mapsize%20) > 0 ? 1 : 0);
			for (int fq = 1;fq <= times;fq++){
				int startNum = (fq-1)*20;   	    	   
		    	int endNum = startNum + 20;
		    	if (endNum > mapsize)endNum = mapsize;
		    	List<String> tempcidlist = cidlist.subList(startNum, endNum);
		    	List<TreeMap<String,JSONObject>> tempfflist = ff.subList(startNum, endNum);
				Timetable table=new Timetable(); 
				List<SchooltableBean> tlist=new ArrayList<SchooltableBean>();			
				for(int i=0;i<tempfflist.size();i++){
					SchooltableBean tb = new SchooltableBean();
					String cid=tempcidlist.get(i);
					tb.setRowName(nameMap.get(cid));
					for (int k = 0; k < dayofweek; k++) {
						for (int h = 0; h < lessofday; h++) {
							if (tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)) != null) {
								if(k==0){
									if (h == 0) {
										tb.setMondayFirName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
										tb.setMondayFirNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("className"));
									} else if (h == 1) {
										tb.setMondaySecName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
										tb.setMondaySecNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("className"));
									} else if (h == 2) {
										tb.setMondayThrName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
										tb.setMondayThrNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("className"));
									} else if (h == 3) {
										tb.setMondayFouName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
										tb.setMondayFouNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("className"));
									} else if (h == 4) {
										tb.setMondayFivName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
										tb.setMondayFivNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("className"));
									}
									else if (h == 5) {
										tb.setMondaySixName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
										tb.setMondaySixNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("className"));
									}
									else if (h == 6) {
										tb.setMondaySevNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("className"));
										tb.setMondaySevName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
									}
									else if (h == 7) {
										tb.setMondayEigNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("className"));
										tb.setMondayEigName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
									}else if (h == 8) {
										tb.setMondayNiNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("className"));
										tb.setMondayNiName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
									}
								}
								else if(k==1){
									if (h == 0) {
										tb.setTuesdayFirNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("className"));
										tb.setTuesdayFirName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
									} else if (h == 1) {
										tb.setTuesdaySecNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("className"));
										tb.setTuesdaySecName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
									} else if (h == 2) {
										tb.setTuesdayThrNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("className"));
										tb.setTuesdayThrName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
									} else if (h == 3) {
										tb.setTuesdayFouNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("className"));
										tb.setTuesdayFouName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
									} else if (h == 4) {
										tb.setTuesdayFivNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("className"));
										tb.setTuesdayFivName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
									}
									else if (h == 5) {
										tb.setTuesdaySixNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("className"));
										tb.setTuesdaySixName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
									}
									else if (h == 6) {
										tb.setTuesdaySevNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("className"));
										tb.setTuesdaySevName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
									}
									else if (h == 7) {
										tb.setTuesdayEigNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("className"));
										tb.setTuesdayEigName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
									}else if (h == 8) {
										tb.setTuesdayNiNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("className"));
										tb.setTuesdayNiName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
									}
								}else if(k==2){
									if (h == 0) {
										tb.setWednesdayFirNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("className"));
										tb.setWednesdayFirName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
									} else if (h == 1) {
										tb.setWednesdaySecNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("className"));
										tb.setWednesdaySecName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
									} else if (h == 2) {
										tb.setWednesdayThrNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("className"));
										tb.setWednesdayThrName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
									} else if (h == 3) {
										tb.setWednesdayFouNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("className"));
										tb.setWednesdayFouName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
									} else if (h == 4) {
										tb.setWednesdayFivNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("className"));
										tb.setWednesdayFivName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
									}
									else if (h == 5) {
										tb.setWednesdaySixNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("className"));
										tb.setWednesdaySixName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
									}
									else if (h == 6) {
										tb.setWednesdaySevNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("className"));
										tb.setWednesdaySevName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
									}
									else if (h == 7) {
										tb.setWednesdayEigNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("className"));
										tb.setWednesdayEigName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
									}else if (h == 8) {
										tb.setWednesdayNiNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("className"));
										tb.setWednesdayNiName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
									}
								}else if(k==3){
									if (h == 0) {
										tb.setThursdayFirNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("className"));
										tb.setThursdayFirName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
									} else if (h == 1) {
										tb.setThursdaySecNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("className"));
										tb.setThursdaySecName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
									} else if (h == 2) {
										tb.setThursdayThrNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("className"));
										tb.setThursdayThrName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
									} else if (h == 3) {
										tb.setThursdayFouNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("className"));
										tb.setThursdayFouName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
									} else if (h == 4) {
										tb.setThursdayFivNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("className"));
										tb.setThursdayFivName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
									}
									else if (h == 5) {
										tb.setThursdaySixNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("className"));
										tb.setThursdaySixName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
									}
									else if (h == 6) {
										tb.setThursdaySevNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("className"));
										tb.setThursdaySevName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
									}
									else if (h == 7) {
										tb.setThursdayEigNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("className"));
										tb.setThursdayEigName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
									}else if (h == 8) {
										tb.setThursdayNiNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("className"));
										tb.setThursdayNiName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
									}
								}else if(k==4){
									if (h == 0) {
										tb.setFridayFirNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("className"));
										tb.setFridayFirName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
									} else if (h == 1) {
										tb.setFridaySecNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("className"));
										tb.setFridaySecName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
									} else if (h == 2) {
										tb.setFridayThrNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("className"));
										tb.setFridayThrName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
									} else if (h == 3) {
										tb.setFridayFouNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("className"));
										tb.setFridayFouName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
									} else if (h == 4) {
										tb.setFridayFivNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("className"));
										tb.setFridayFivName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
									}else if (h == 5) {
										tb.setFridaySixNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("className"));
										tb.setFridaySixName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
									}else if (h == 6) {
										tb.setFridaySevNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("className"));
										tb.setFridaySevName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
									}else if (h == 7) {
										tb.setFridayEigNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("className"));
										tb.setFridayEigName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
									}else if (h == 8) {
										tb.setFridayNiNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("className"));
										tb.setFridayNiName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
									}
								}else if(k==5){
									if (h == 0) {
										tb.setSaturdayFirNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("className"));
										tb.setSaturdayFirName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
									} else if (h == 1) {
										tb.setSaturdaySecNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("className"));
										tb.setSaturdaySecName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
									} else if (h == 2) {
										tb.setSaturdayThrNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("className"));
										tb.setSaturdayThrName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
									} else if (h == 3) {
										tb.setSaturdayFouNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("className"));
										tb.setSaturdayFouName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
									} else if (h == 4) {
										tb.setSaturdayFivNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("className"));
										tb.setSaturdayFivName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
									}
									else if (h == 5) {
										tb.setSaturdaySixNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("className"));
										tb.setSaturdaySixName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
									}
									else if (h == 6) {
										tb.setSaturdaySevNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("className"));
										tb.setSaturdaySevName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
									}
									else if (h == 7) {
										tb.setSaturdayEigNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("className"));
										tb.setSaturdayEigName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
									}else if (h == 8) {
										tb.setSaturdayNiNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("className"));
										tb.setSaturdayNiName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
									}
								}else if(k==6){
									if (h == 0) {
										tb.setSundayFirNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("className"));
										tb.setSundayFirName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
									} else if (h == 1) {
										tb.setSundaySecNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("className"));
										tb.setSundaySecName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
									} else if (h == 2) {
										tb.setSundayThrNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("className"));
										tb.setSundayThrName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
									} else if (h == 3) {
										tb.setSundayFouNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("className"));
										tb.setSundayFouName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
									} else if (h == 4) {
										tb.setSundayFivNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("className"));
										tb.setSundayFivName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
									}
									else if (h == 5) {
										tb.setSundaySixNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("className"));
										tb.setSundaySixName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
									}
									else if (h == 6) {
										tb.setSundaySevNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("className"));
										tb.setSundaySevName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
									}
									else if (h == 7) {
										tb.setSundayEigNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("className"));
										tb.setSundayEigName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
									}else if (h == 8) {
										tb.setSundayNiNameTeacher(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString("className"));
										tb.setSundayNiName(tempfflist.get(i).get(cid+String.valueOf(k) + String.valueOf(h)).getString(nameKey));
									}
								}		
							} 
						}			
					}		
					tlist.add(tb);
				}
				table.setStimedata(tlist);
				if (setInfo.containsKey("title")){
					table.setTimeTableName(setInfo.getString("title"));
				}else{
					table.setTimeTableName(t.getString("tableName"));
				}
				table.setBottomNote1(setInfo.getString("bottomNote1"));
				table.setBottomNote2(setInfo.getString("bottomNote2"));
				table.setBottomNote3(setInfo.getString("bottomNote3"));
				tablelist.add(table);				
			}					
		}		
		return tablelist;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<JSONObject> getAllSchoolExportList(List<JSONObject> datas,
			JSONObject setInfo) {
		List<JSONObject> tablelist=new ArrayList<JSONObject>();
		String nameKey = setInfo.getString("courseShowName");
		boolean isShowExtra = setInfo.getBooleanValue("isShowExtra");
		String rowId = setInfo.getString("rowId");
		String row = setInfo.getString("row");
		String courseExtra = setInfo.getString("courseExtra");
		HashMap<String,String> nameMap = new HashMap<String,String>();
		List<JSONObject> maplist = new ArrayList<JSONObject>();
		for(JSONObject json : datas) {
			List<JSONObject> dlist = (List<JSONObject>) json.get("data");
			List<TreeMap<String,JSONObject>> ff = new ArrayList<TreeMap<String,JSONObject>>();
			List<String> cidlist = new ArrayList<String>();
			JSONObject datamap = new JSONObject();
			for(JSONObject o : dlist) {
				List<JSONObject> tlist = (List<JSONObject>) o.get("timetable");
				TreeMap<String, JSONObject> map = new TreeMap<String, JSONObject>();
				TreeMap<String, JSONObject> mono = new TreeMap<String, JSONObject>();
				for (JSONObject t : tlist) {					
					if(null==o.get(rowId)){						
						if(o.get("teacherId")!=null){
							o.put(rowId, o.get("teacherId"));
						}else if(o.get("classId")!=null){
							o.put(rowId, o.get("classId"));
						}
					}					
					
					String  key = o.getString(rowId)
							+ t.getString("dayOfWeek") + t.getString("lessonOfDay");
					
					 if (StringUtils.isNotEmpty(t.getString("courseType"))
							 &&!t.getString("courseType").equals("0")){
						 if (mono.containsKey(key)){
							 JSONObject src = mono.get(key);
							if (!t.getString(nameKey).contains(
									src.getString(nameKey))) {
								t.put(nameKey, src.getString(nameKey)
										+ "|" + t.getString(nameKey));
							}
							if (!t.getString(courseExtra).contains(
									src.getString(courseExtra))) {
								t.put(courseExtra, src.getString(courseExtra)
										+ "|" + t.getString(courseExtra));
							} 
						 }else{
							 mono.put(key, t);
						 }
					 }
					 map.put(key,t);	  
				}
				ff.add(map);
				cidlist.add(o.getString(rowId));
				nameMap.put(o.getString(rowId), o.getString(row));
			}
			datamap.put("data", ff);
			datamap.put(rowId, cidlist);
			datamap.put("tableName", json.getString("tableName"));
			maplist.add(datamap);		
		}
		int lessofday = setInfo.getIntValue("totalMaxLessons");
		int dayofweek = setInfo.getIntValue("totalMaxDays");
		for(JSONObject t : maplist) {	
			JSONObject temp = new JSONObject();
			List<TreeMap<String,JSONObject>> templist = (List<TreeMap<String, JSONObject>>) t.get("data");
			List<String> cidlist = (List<String>) t.get(rowId);			
			// 封装表头-名称
			String title = "";
			if (setInfo.containsKey("title")){
				title = setInfo.getString("title");
			}else{
				title = t.getString("tableName");
			}
			JSONObject excelTitle = new JSONObject();
			excelTitle.put("title", title);
			excelTitle.put("align", "center");
			excelTitle.put("colspan", 1+lessofday*dayofweek);
			// 封装表头-星期
			JSONArray tableHead = new JSONArray();	
			JSONArray headsOne = new JSONArray();
			JSONObject headOne = new JSONObject();
			headOne.put("title", Constant.WEEK);
			headOne.put("align", "center");
			headOne.put("width", 50);
			headsOne.add(headOne);
			for(int i = 0;i < dayofweek;i++){
				JSONObject headI = new JSONObject();
				headI.put("title", WeekSectionTool.week.get(i));
				headI.put("align", "center");
				headI.put("colspan", lessofday);
				headsOne.add(headI);
			}
			tableHead.add(headsOne);
			// 封装表头-节次
			JSONArray headsTwo = new JSONArray();
			JSONObject headTwo = new JSONObject();
			headTwo.put("title", Constant.SECTION);
			headTwo.put("field", "jc");
			headTwo.put("boxWidth", 41);
			headTwo.put("deltaWidth", 9);
			headTwo.put("align", "center");
			headTwo.put("width", 50);
			headsTwo.add(headTwo);
			for(int i = 0;i < dayofweek;i++){
				for(int j = 0;j < lessofday;j++){
					JSONObject headJ = new JSONObject();
					headJ.put("field", "row_"+ i +"_"+ j);
					headJ.put("title", WeekSectionTool.section.get(j));
					headJ.put("boxWidth", 70);
					headJ.put("deltaWidth", 9);
					headJ.put("auto", true);
					headJ.put("align", "center");
					headJ.put("width", 79);
					headsTwo.add(headJ);					
				}
			}			
			tableHead.add(headsTwo);		
			// 封装内容信息
			JSONArray tableDatas = new JSONArray();
			for(int l = 0;l < templist.size();l++){
				String cid = cidlist.get(l);
				String rowName = nameMap.get(cid);	
				JSONObject dataOne = new JSONObject();
				JSONObject dataTwo = new JSONObject();
				dataOne.put("jc", rowName);
				for (int k = 0; k < dayofweek; k++) {
					for (int h = 0; h < lessofday; h++) {	
						if (templist.get(l).get(
								cid + String.valueOf(k) + String.valueOf(h)) != null) {
							String courseName = templist
									.get(l)
									.get(cid + String.valueOf(k)
											+ String.valueOf(h))
									.getString(nameKey);
							dataOne.put("row_" + k + "_" + h, courseName);
							if (isShowExtra) {
								String extraName = templist
										.get(l)
										.get(cid + String.valueOf(k)
												+ String.valueOf(h))
										.getString(courseExtra);
								dataTwo.put("row_" + k + "_" + h, extraName);
							}
						}
					}
				}
				tableDatas.add(dataOne);
				if(isShowExtra)tableDatas.add(dataTwo);
			}						
			// 封装bottom注解信息
			JSONArray excelTails = new JSONArray();
			List<String> tails = new ArrayList<String>();
			String bottomNote1 = setInfo.getString("bottomNote1");
			if (StringUtils.isNotEmpty(bottomNote1)){
				tails.add(bottomNote1);
			}
			String bottomNote2 = setInfo.getString("bottomNote2");
			if (StringUtils.isNotEmpty(bottomNote2)){
				tails.add(bottomNote2);
			}
			String bottomNote3 = setInfo.getString("bottomNote3");
			if (StringUtils.isNotEmpty(bottomNote3)){
				tails.add(bottomNote3);
			}
			tails.add("");// 空一行
			for(int l = 0; l < tails.size();l++){
				JSONArray tailsArray = new JSONArray();
				JSONObject tailObj = new JSONObject();
				tailObj.put("title", tails.get(l));
				tailObj.put("colspan", 1+lessofday*dayofweek);
				tailsArray.add(tailObj);
				excelTails.add(tailsArray);
			}						
			// 表头-内容-表尾
			temp.put("excelTitle", excelTitle);
			temp.put("tableHead", tableHead);	
			temp.put("tableData", tableDatas);
			temp.put("excelTail", excelTails);
			tablelist.add(temp);					
		}
		return tablelist;
	}	
	
	@SuppressWarnings("unchecked")
	@Override
	public List<JSONObject> getClassExportList(List<JSONObject> datas,
			JSONObject setInfo) {
		List<JSONObject> tablelist=new ArrayList<JSONObject>();
		boolean isShowExtra = setInfo.getBooleanValue("isShowExtra");
		List<TreeMap<String, JSONObject>> maplist = new ArrayList<TreeMap<String, JSONObject>>();
		for (JSONObject json : datas) {
 
			 TreeMap<String, JSONObject> map = new TreeMap<String, JSONObject>();
			 List<JSONObject> tlist = (List<JSONObject>) json.get("timetable");
			 TreeMap<String, JSONObject> mono = new TreeMap<String, JSONObject>();
			 for(JSONObject o : tlist) {
				 String key = o.getString("dayOfWeek") + o.getString("lessonOfDay");
				 if (StringUtils.isNotEmpty(o.getString("courseType"))
						 &&!o.getString("courseType").equals("0")){
					 if (mono.containsKey(key)){
						 JSONObject src = mono.get(key);
						if (!o.getString("courseName").contains(
								src.getString("courseName"))) {
							o.put("courseName", src.getString("courseName")
									+ "|" + o.getString("courseName"));
						}
						if (!o.getString("teacherName").contains(
								src.getString("teacherName"))) {
							o.put("teacherName", src.getString("teacherName")
									+ "|" + o.getString("teacherName"));
						} 
					 }else{
						 mono.put(key, o);
					 }
				 }
				 map.put(key,o);	
				 map.put("data", json);
			}
			maplist.add(map);
		}
 		
		for(TreeMap<String, JSONObject> t : maplist) {		
			
			int[] maxDayAndLessons = getMaxDayAndLessons(t.get("data").getIntValue("totalMaxDays") , t.get("data").getIntValue("totalMaxLessons"));
			JSONObject temp = new JSONObject();			
			// 封装标题-名称
			String title = "";			
			if (setInfo.containsKey("title")){
				title = setInfo.getString("title");
			}else{
				title = t.get("data").getString("tableName");
			}			
			JSONObject excelTitle = new JSONObject();
			excelTitle.put("title", title);
			excelTitle.put("align", "center");
			excelTitle.put("colspan", 1+maxDayAndLessons[0]);
			// 封装页面标头
			JSONObject pageHead = new JSONObject();
			String pageOne = Constant.CLASSNAME + " : " + t.get("data").getString("className");
			String deanName = t.get("data").getString("teacherName");
			String pageTwo = "";
			if (StringUtils.isNotEmpty(deanName)){
				pageTwo =  Constant.CLASSDEAN + " : " + deanName;		
			}
			JSONArray scheduleTime = t.get("data").getJSONArray("scheduleTime");
			pageHead.put("title", Constant.SPACES + pageOne + Constant.SPACES + pageTwo);
			pageHead.put("align", "center");
			pageHead.put("colspan", 1+maxDayAndLessons[0]);	
			// 封装表头-星期
			JSONArray tableHead = new JSONArray();	
			JSONArray headsWeek = new JSONArray();
			JSONObject headWeek = new JSONObject();
			headWeek.put("title", Constant.SECTION);
			headWeek.put("field", "jc");
			headWeek.put("boxWidth", 71);
			headWeek.put("deltaWidth", 9);
			headWeek.put("align", "center");
			headWeek.put("width", 80);
			headsWeek.add(headWeek);
			for(int i = 0;i < maxDayAndLessons[0];i++){
				JSONObject headI = new JSONObject();
				headI.put("field", "row_0_"+ i);
				headI.put("title", WeekSectionTool.week.get(i));
				headI.put("boxWidth", 111);
				headI.put("deltaWidth", 9);
				headI.put("auto", true);
				headI.put("align", "center");
				headI.put("width", 120);			
				headsWeek.add(headI);
			}
			tableHead.add(headsWeek);
			JSONArray tableDatas = new JSONArray();	
			boolean isTimeZone = false;
			for(int k = 0; k < maxDayAndLessons[1]; k++) {	
				JSONObject dataOne = new JSONObject();
				JSONObject dataTwo = new JSONObject();
				dataOne.put("jc", "第"+less[k]+"节");
				if (scheduleTime!=null  && scheduleTime.size() > 0 ) {
					if (k >=scheduleTime.size()) {
						break;
					}
					JSONObject object = scheduleTime.getJSONObject(k);
					if (object!=null && (k +"").equals(object.getString("sectionId"))) {
						if (StringUtils.isNotBlank(object.getString("startTime")) && StringUtils.isNotBlank(object.getString("endTime"))) {
							dataTwo.put("jc", "NEEDCOMBINE" + object.getString("startTime") +"--" + object.getString("endTime"));
							isTimeZone = true;
						}
					}else {
						for (int i = 0; i < scheduleTime.size(); i++) {
							 object = scheduleTime.getJSONObject(i);
							 if (object!=null && (k +"").equals(object.getString("sectionId"))) {
									if (StringUtils.isNotBlank(object.getString("startTime")) && StringUtils.isNotBlank(object.getString("endTime"))) {
										dataOne.put("jc",  "NEEDCOMBINE" + object.getString("startTime") +"--" + object.getString("endTime"));
										isTimeZone = true;
									}
									break;
						     }
						}
						
						
					}
					 
				}
				for(int h = 0; h < maxDayAndLessons[0]; h++) {
					String courseName = "",extraName = "";
					if (t.get(String.valueOf(h) + String.valueOf(k)) != null) {	
						courseName = t.get(
								String.valueOf(h) + String.valueOf(k))
								.getString("courseName");
						dataOne.put("row_0_" + h, courseName);
						if (isShowExtra) {
							extraName = t.get(
									String.valueOf(h) + String.valueOf(k))
									.getString("teacherName");	
							dataTwo.put("row_0_" + h, extraName);
						}
					}							
				}
				tableDatas.add(dataOne);
				if(isShowExtra || StringUtils.isNotBlank(dataTwo.getString("jc"))){
					tableDatas.add(dataTwo);
				}	
			}
			// 封装bottom注解信息
			JSONArray excelTails = new JSONArray();
			List<String> tails = new ArrayList<String>();
			String bottomNote1 = setInfo.getString("bottomNote1");
			if (StringUtils.isNotEmpty(bottomNote1)){
				tails.add(bottomNote1);
			}
			String bottomNote2 = setInfo.getString("bottomNote2");
			if (StringUtils.isNotEmpty(bottomNote2)){
				tails.add(bottomNote2);
			}
			String bottomNote3 = setInfo.getString("bottomNote3");
			if (StringUtils.isNotEmpty(bottomNote3)){
				tails.add(bottomNote3);
			}
			tails.add("");// 空一行
			for(int l = 0; l < tails.size();l++){
				JSONArray tailsArray = new JSONArray();
				JSONObject tailObj = new JSONObject();
				tailObj.put("title", tails.get(l));
				tailObj.put("colspan", 1+maxDayAndLessons[0]);
				tailsArray.add(tailObj);
				excelTails.add(tailsArray);
			}			
			// 表头-内容-表尾
			temp.put("excelTitle", excelTitle);	
			temp.put("pageHead", pageHead);
			temp.put("tableHead", tableHead);	
			temp.put("tableData", tableDatas);
			temp.put("excelTail", excelTails);
			temp.put("isTimeZone", isTimeZone);
			tablelist.add(temp);
		}	
		return tablelist;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<JSONObject> getTeacherExportList(List<JSONObject> datas,
			JSONObject setInfo) {
		List<JSONObject> tablelist = new ArrayList<JSONObject>();
		List<TreeMap<String, JSONObject>> maplist = new ArrayList<TreeMap<String, JSONObject>>();
		for (JSONObject json : datas) {
			TreeMap<String, JSONObject> map = new TreeMap<String, JSONObject>();
			List<JSONObject> tlist = (List<JSONObject>) json.get("timetable");
			TreeMap<String, JSONObject> mono = new TreeMap<String, JSONObject>();
			for (JSONObject o : tlist) {
				 String key = o.getString("dayOfWeek") + o.getString("lessonOfDay");	 
				 if (StringUtils.isNotEmpty(o.getString("courseType"))
						 &&!o.getString("courseType").equals("0")){
					 if (mono.containsKey(key)){
						 JSONObject src = mono.get(key);
						if (!o.getString("courseName").contains(
								src.getString("courseName"))) {
							o.put("courseName", src.getString("courseName")
									+ "|" + o.getString("courseName"));
						}
						if (!o.getString("className").contains(
								src.getString("className"))) {
							o.put("className", src.getString("className")
									+ "|" + o.getString("className"));
						} 
					 }else{
						 mono.put(key, o);
					 }
				 }
				 map.put(key,o);
				 map.put("data", json);
			}
			maplist.add(map);
		}
 			
		for(TreeMap<String, JSONObject> t : maplist) {
			int[] maxDayAndLessons = getMaxDayAndLessons(t.get("data").getIntValue("totalMaxDays") , t.get("data").getIntValue("totalMaxLessons"));
			JSONObject temp = new JSONObject();			
			// 封装表头-名称
			String title = "";
			if (setInfo.containsKey("title")){
				title = setInfo.getString("title");
			}else{
				title = t.get("data").getString("tableName");
			}
			JSONArray scheduleTime = t.get("data").getJSONArray("scheduleTime");
			JSONObject excelTitle = new JSONObject();
			excelTitle.put("title", title);
			excelTitle.put("align", "center");
			excelTitle.put("colspan", 1+maxDayAndLessons[0]);
			// 封装页面标注
			JSONObject pageHead = new JSONObject();			
			String pageOne = Constant.NAMEHEAD + " : "
					+ t.get("data").getString("teacherName");
			String cardNo = t.get("data").getString("teacherId");
			String pageTwo = "";
			if (StringUtils.isNotEmpty(cardNo)){
				pageTwo = Constant.CARDNO + " : " + cardNo;
			}
			pageHead.put("title", Constant.SPACES + pageOne + Constant.SPACES + pageTwo);
			pageHead.put("align", "center");
			pageHead.put("colspan", 1+maxDayAndLessons[0]);
			// 封装表头-星期
			JSONArray tableHead = new JSONArray();	
			JSONArray headsWeek = new JSONArray();
			JSONObject headWeek = new JSONObject();
			headWeek.put("title", Constant.SECTION);
			headWeek.put("field", "jc");
			headWeek.put("boxWidth", 71);
			headWeek.put("deltaWidth", 9);
			headWeek.put("align", "center");
			headWeek.put("width", 80);
			headsWeek.add(headWeek);
			for(int i = 0;i < maxDayAndLessons[0];i++){
				JSONObject headI = new JSONObject();
				headI.put("field", "row_0_"+ i);
				headI.put("title", WeekSectionTool.week.get(i));
				headI.put("boxWidth", 111);
				headI.put("deltaWidth", 9);
				headI.put("auto", true);
				headI.put("align", "center");
				headI.put("width", 120);			
				headsWeek.add(headI);
			}
			tableHead.add(headsWeek);
			JSONArray tableDatas = new JSONArray();	
			for(int k = 0; k < maxDayAndLessons[1]; k++) {	
				JSONObject dataOne = new JSONObject();
				JSONObject dataTwo = new JSONObject();
				dataOne.put("jc", "第"+less[k]+"节");
				if (scheduleTime!=null && scheduleTime.size() > 0) {
					if(k >= scheduleTime.size()){
						break;
					}
					JSONObject object = scheduleTime.getJSONObject(k);
					if (object!=null && (k +"").equals(object.getString("sectionId"))) {
						if (StringUtils.isNotBlank(object.getString("startTime")) && StringUtils.isNotBlank(object.getString("endTime"))) {
							dataTwo.put("jc", "NEEDCOMBINE" + object.getString("startTime") +"--" + object.getString("endTime"));
						}
					}else {
						for (int i = 0; i < scheduleTime.size(); i++) {
							 object = scheduleTime.getJSONObject(i);
							 if (object!=null && (k +"").equals(object.getString("sectionId"))) {
									if (StringUtils.isNotBlank(object.getString("startTime")) && StringUtils.isNotBlank(object.getString("endTime"))) {
										dataOne.put("jc",  "NEEDCOMBINE" + object.getString("startTime") +"--" + object.getString("endTime"));
							        }
									break;
						     }
						}
						
						
					}
					 
				}
				
				
			    
				for(int h = 0; h < maxDayAndLessons[0]; h++) {
					String courseName = "",extraName = "";					
					if (t.get(String.valueOf(h) + String.valueOf(k)) != null) {	
						courseName = t.get(
								String.valueOf(h) + String.valueOf(k))
								.getString("courseName");
						extraName = t.get(
								String.valueOf(h) + String.valueOf(k))
								.getString("className");							
					}	
					dataOne.put("row_0_" + h, courseName);
					dataTwo.put("row_0_" + h, extraName);
					
				}
				tableDatas.add(dataOne);
				tableDatas.add(dataTwo);	
			}
			// 封装bottom注解信息
			JSONArray excelTails = new JSONArray();
			List<String> tails = new ArrayList<String>();
			String bottomNote1 = setInfo.getString("bottomNote1");
			if (StringUtils.isNotEmpty(bottomNote1)){
				tails.add(bottomNote1);
			}
			String bottomNote2 = setInfo.getString("bottomNote2");
			if (StringUtils.isNotEmpty(bottomNote2)){
				tails.add(bottomNote2);
			}
			String bottomNote3 = setInfo.getString("bottomNote3");
			if (StringUtils.isNotEmpty(bottomNote3)){
				tails.add(bottomNote3);
			}
			tails.add("");// 空一行
			for(int l = 0; l < tails.size();l++){
				JSONArray tailsArray = new JSONArray();
				JSONObject tailObj = new JSONObject();
				tailObj.put("title", tails.get(l));
				tailObj.put("colspan", 1+maxDayAndLessons[0]);
				tailsArray.add(tailObj);
				excelTails.add(tailsArray);
			}			
			// 表头-内容-表尾
			temp.put("excelTitle", excelTitle);	
			temp.put("pageHead", pageHead);
			temp.put("tableHead", tableHead);	
			temp.put("tableData", tableDatas);
			temp.put("excelTail", excelTails);
			tablelist.add(temp);
		}	
		return tablelist;
	}
	
	
	private int[] getMaxDayAndLessons(int totalMaxD , int  totalMaxL) {
		int[] maxDayAndLessons=new int[2];
		if (totalMaxD < 5){
			maxDayAndLessons[0] = 5;
		}else if(totalMaxD > 7){
			maxDayAndLessons[0] = 7;
		}else{
			maxDayAndLessons[0] = totalMaxD;
		}
		if (totalMaxL < 6){
			maxDayAndLessons[1] = 6;
		}else if(totalMaxL > 9){
			maxDayAndLessons[1] = 9;
		}else{
			maxDayAndLessons[1] = totalMaxL;
		}
		 
		return maxDayAndLessons;
		
	}
	
}