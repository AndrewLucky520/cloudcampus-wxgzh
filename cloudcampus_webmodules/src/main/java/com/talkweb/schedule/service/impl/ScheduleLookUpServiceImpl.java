package com.talkweb.schedule.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.common.tools.DateUtil;
import com.talkweb.schedule.dao.ScheduleLookUpDao;
import com.talkweb.schedule.service.ScheduleLookUpService;

@Service
public class ScheduleLookUpServiceImpl implements ScheduleLookUpService {

	@Autowired
	private ScheduleLookUpDao scheduleLookUpDao;
	
	@Override
	public List<JSONObject> getSchedule(Map<String, String> map) {
		return scheduleLookUpDao.getSchedule(map);
	}

	@Override
	public void deleteSchedule(JSONObject object) {
		scheduleLookUpDao.deleteSchedule(object);		
	}

	@Override
	public int updateSchedule(JSONObject object) {
		return scheduleLookUpDao.updateSchedule(object);
	}
	
	@Override
	public int addSchedule(Map<String, Object> map) {
		return scheduleLookUpDao.addSchedule(map);
	}
	
	@Override
	public List<JSONObject> getClassTb(Map<String, Object> param) {
		// TODO Auto-generated method stub
		return scheduleLookUpDao.getClassTb(param);
	}

	@Override
	public List<JSONObject> getClassTbTeachers(Map<String, Object> param) {
		// TODO Auto-generated method stub
		return scheduleLookUpDao.getClassTbTeachers(param);
	}

	@Override
	public List<JSONObject> getClassTbZOU(Map<String, Object> param) {
		// TODO Auto-generated method stub
		return scheduleLookUpDao.getClassTbZOU(param);
	}

	@Override
	public List<JSONObject> getTeacherTb(Map<String, Object> param) {
		// TODO Auto-generated method stub
		return scheduleLookUpDao.getTeacherTb(param);
	}

	@Override
	public List<JSONObject> getStudentTb(Map<String, Object> param) {
		// TODO Auto-generated method stub
		return scheduleLookUpDao.getStudentTb(param);
	}

	@Override
	public List<JSONObject> getGroundTb(Map<String, Object> param) {
		// TODO Auto-generated method stub
		return scheduleLookUpDao.getGroundTb(param);
	}

	@Override
	public List<JSONObject> getGradeTbs(Map<String, Object> param) {
		// TODO Auto-generated method stub
		return scheduleLookUpDao.getGradeTbs(param);
	}

	@Override
	public List<JSONObject> getStudentClassTbs(Map<String, Object> param) {
		// TODO Auto-generated method stub
		return scheduleLookUpDao.getStudentClassTbs(param);
	}

	@Override
	public JSONObject getNewTimetablePrintSet(Map<String, Object> param) {
		// TODO Auto-generated method stub
		return scheduleLookUpDao.getNewTimetablePrintSet(param);
	}

	@Override
	public void updateNewTimetablePrintSet(Map<String, Object> param) {
		// TODO Auto-generated method stub
		if(scheduleLookUpDao.getNewTimetablePrintSet(param)==null){
			scheduleLookUpDao.insertNewTimetablePrintSet(param);
		}else{
			scheduleLookUpDao.updateNewTimetablePrintSet(param);
		}
	}

	@Override
	public JSONObject getScheduleTimetableById(Map<String, Object> param) {
		// TODO Auto-generated method stub
		return scheduleLookUpDao.getScheduleTimetableById(param);	
	}

	@Override
	public void insertNewTimetablePrintSet(Map<String, Object> param) {
		// TODO Auto-generated method stub
		scheduleLookUpDao.insertNewTimetablePrintSet(param);
	}

	@Override
	public List<JSONObject> getStuScheduleList(JSONObject object) {
		// TODO Auto-generated method stub
		List<JSONObject> data = new ArrayList<JSONObject>();
		try {
			data = scheduleLookUpDao.queryStuScheduleList(object);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return data;
	}

	@Override
	public List<JSONObject> getClassTbZOU1(HashMap<String, Object> param) {
		// TODO Auto-generated method stub
		return scheduleLookUpDao.getClassTbZOU1(param);
	}

	@Override
	public List<JSONObject> getStuInTclass(Map<String, Object> param) {
		// TODO Auto-generated method stub
		return scheduleLookUpDao.queryStuInTclass(param);
	}

	@Override
	public List<JSONObject> evalOfSubInSchedule(Map<String, Object> param) {
		// TODO Auto-generated method stub
		return scheduleLookUpDao.evalOfSubInSchedule(param);
	}

	@SuppressWarnings("deprecation")
	public static JSONObject getWeekStatsData(Date startDate,Date endDate,	Date dayOfStart,int maxDaysForWeek){
		JSONObject result = new JSONObject();
		List<Integer> singleWeekDays = new ArrayList<Integer>(14);
		List<Integer> doubleWeekDays = new ArrayList<Integer>(14);
		Integer singleWeekCount = 0;
		Integer doubleWeekCount = 0;
		//计算开始日期的单双周
		Date firstDayOfWeekStartDate = DateUtil.getFirstDayOfLearnWeek(startDate);
		Date firstDayOfWeekEndDate = DateUtil.getFirstDayOfLearnWeek(endDate);
		
		int startDateWeek = (int) ((firstDayOfWeekStartDate.getTime()-DateUtil.getFirstDayOfLearnWeek(dayOfStart).getTime())/(7*24*3600*1000));
		int startDateWeekFlg = startDateWeek%2+1;
		int endDateWeek =(int) ((firstDayOfWeekEndDate.getTime()-DateUtil.getFirstDayOfLearnWeek(dayOfStart).getTime())/(7*24*3600*1000));
		int endDateWeekFlg = endDateWeek%2+1;
		int weekDiff = 0;
		if(endDateWeek - startDateWeek ==0){
			weekDiff = 0;
		}else{
			weekDiff = endDateWeek - startDateWeek-1;
		}
		if(startDateWeekFlg==1){
			singleWeekCount = weekDiff/2;
			doubleWeekCount = weekDiff/2+weekDiff%2;
		}else{
			doubleWeekCount = weekDiff/2;
			singleWeekCount = weekDiff/2+weekDiff%2;
		}
		if(endDateWeek>startDateWeek){
			int startDay = startDate.getDay();
			if(startDay==0){
				startDay = 7;
			}
			int endDay = endDate.getDay();
			if(endDay==0){
				endDay = 7;
			}
			List<Integer> startWeekDays =startDateWeekFlg==1?singleWeekDays:doubleWeekDays;
			List<Integer> endWeekDays = endDateWeekFlg==1?singleWeekDays:doubleWeekDays;
			for(int i=startDay;i<=maxDaysForWeek;i++){
				startWeekDays.add(i);
			}
			for(int i=1;i<=endDay&&i<=maxDaysForWeek;i++){
				endWeekDays.add(i);
			}
			if(singleWeekDays.size() / maxDaysForWeek > 0){
				for(int i =0 ;i<singleWeekDays.size() / maxDaysForWeek;i++){
					singleWeekCount+=1;
					for(int j=1;j<=maxDaysForWeek;j++){
						singleWeekDays.remove(singleWeekDays.indexOf(new Integer(j)));
					}
				}
			}
			if(doubleWeekDays.size() / maxDaysForWeek > 0){
				for(int i =0 ;i<doubleWeekDays.size() / maxDaysForWeek;i++){
					doubleWeekCount+=1;
					for(int j=1;j<=maxDaysForWeek;j++){
						doubleWeekDays.remove(doubleWeekDays.indexOf(new Integer(j)));
					}
				}
			}
		}else{
			int startDay = startDate.getDay();
			int endDay = endDate.getDay();
			int oneWeekDayCount = endDay - startDay + 1;
			if(oneWeekDayCount>=maxDaysForWeek){
				if(startDateWeekFlg==1){
					singleWeekCount = 1;
				}else{
					doubleWeekCount = 1;
				}
			}else{
				List<Integer> startWeekDays =startDateWeekFlg==1?singleWeekDays:doubleWeekDays;
				for(int i=startDay;i<=endDay&&i<=maxDaysForWeek;i++){
					startWeekDays.add(i);
				}
			}
		}
		result.put("singleWeekCount", singleWeekCount);
		result.put("doubleWeekCount", doubleWeekCount);
		result.put("singleWeekDays", singleWeekDays);
		result.put("doubleWeekDays", doubleWeekDays);
		return result;
	}

	@Override
	public List<JSONObject> getStudentTbForApp(Map<String, Object> param) {
		// TODO Auto-generated method stub
		return scheduleLookUpDao.getStudentTbForApp(param);
	}
	
	
}
