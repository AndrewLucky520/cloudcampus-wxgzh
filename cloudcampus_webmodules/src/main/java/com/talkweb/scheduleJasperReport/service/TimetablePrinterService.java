package com.talkweb.scheduleJasperReport.service;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.scheduleJasperReport.bean.Timetable;

public interface TimetablePrinterService {
	
	List<Timetable> getClassList(List<JSONObject> datas,JSONObject setInfo);
	
	List<Timetable> getTwoClassList(List<JSONObject> datas,JSONObject setInfo);
	
	List<Timetable> getTeacherList(List<JSONObject> datas,JSONObject setInfo);
	
	List<Timetable> getTwoTeacherList(List<JSONObject> datas,JSONObject setInfo);
	
	List<Timetable> getSchoolTeacherList(List<JSONObject> datas,JSONObject setInfo);
	
	List<Timetable> getSchoolClassList(List<JSONObject> datas,JSONObject setInfo);

	List<JSONObject> getAllSchoolExportList(List<JSONObject> datas,JSONObject setInfo);
	
	List<JSONObject> getClassExportList(List<JSONObject> datas,JSONObject setInfo);

	List<JSONObject> getTeacherExportList(List<JSONObject> data,JSONObject setInfo);

	List<JSONObject> getClassroomExportList(List<JSONObject> datas,
			JSONObject setInfo);

	List<JSONObject> getGradeExportList(List<JSONObject> datas,
			JSONObject setInfo);

	List<JSONObject> getStudentExportList(List<JSONObject> datas,
			JSONObject setInfo);

	List<JSONObject> getStudentPlacementExportList(List<JSONObject> datas,
			JSONObject setInfo);

}