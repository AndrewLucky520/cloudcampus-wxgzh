package com.talkweb.weekWork.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.weekWork.pojo.ReportingPersonnel;
import com.talkweb.weekWork.pojo.WeeklyRecord;

public interface WeekWorkDao {

	boolean getFillManInfo(Map<String, Object> map);
	
	List<ReportingPersonnel> queryReportingPersonnel(Map<String, Object> map);
	
	List<WeeklyRecord> queryWeeklyRecordList(Map<String, Object> map);
	
	List<JSONObject> getDepartmentInfoByDataBase(JSONObject param);
	
	WeeklyRecord getWeeklyRecordListByDepartment(Map<String, Object> map);//get one WeeklyRecord by department

	List<Long> getTeachersBydepartmentId(JSONObject param);
	
	int insertWeeklyRecord(WeeklyRecord oneRecord);
	
	
	
	
	
	String getCurrentTermWeek(JSONObject param);

	List<JSONObject> getWeeklyRecordDetail(JSONObject param);

	List<JSONObject> getDistinctRecordDetail(JSONObject param);

	/*********类别和表头设置************/
	String getWeekWorkSortNames(JSONObject param);
	
	List<JSONObject> getWeekWorkTableHeadNames(JSONObject param);
	
	void insertWeekWorkSortBatch(List<JSONObject> list);
	
	void insertWeekWorkTableHeadBatch(List<JSONObject> list);
	
	void deleteWeekWorkSortById(JSONObject param);
	
	void deleteWeekWorkTableHeadById(JSONObject param);
	
	/*********部门和人员设置*****@author zhh*******/
	boolean isExsitedSameDepartmentName(JSONObject param);
	
	JSONObject getDepartmentById(JSONObject param);
	
	void insertDepartment(JSONObject param);
	
	void insertTeacherBatch(Map<String, Object> params);
	
	void deleteDepartment(JSONObject param);
	
	void deleteTeacher(JSONObject param);
	

	int deleteWeeklyContent(JSONObject del);

	int updateWeeklyContent(List<JSONObject> lj);

	int insertTerminfoAndStartWeek(JSONObject param);

	List<JSONObject> getReportingPersonList(JSONObject param);

	Date getTerminfoAndStartWeek(JSONObject param);

	int getFillRecord(JSONObject param);
	int getFillRecord2(JSONObject param);

	List<JSONObject> getBaseRecordDetail(JSONObject param);

	int deleteWeeklyRecord(JSONObject oneRecord);

	int deleteTerminfoAndStartWeek(JSONObject param);

	int deleteWeekWorkSortBySchoolId(JSONObject delParam);

	int deleteWeekWorkTableHeadBySchoolId(JSONObject delParam);

	List<JSONObject> getDepartmentList(JSONObject param);

	List<JSONObject> getMaxWeekFromRecord(JSONObject param);
	List<JSONObject> getMaxWeekFromRecord2(JSONObject param);

	int delWeeklyRecordDetail1(JSONObject param);

	int delWeeklyRecordDetail2(JSONObject param);
	
	/**周工作导出 @author zhh**/
	List<JSONObject> getDistinctRecordDetail1(JSONObject param);
	List<JSONObject> getDistinctRecordDetail2(JSONObject param);
	List<JSONObject> getFilledList(JSONObject param);//获取已填报人
	
}
