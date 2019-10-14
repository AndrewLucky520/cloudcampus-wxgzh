package com.talkweb.salary.dao;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.salary.domain.SalExcel;

public interface SalaryDao {

	List<JSONObject> getSalaryList(JSONObject object);

	int addSalary(JSONObject object);

	int updateSalary(JSONObject object);

	int insertSalExcel(List<SalExcel> l_salExcel);

	int insertSalDetail(List<JSONObject> l_params);

	int updateSalaryPublished(JSONObject param);

	List<JSONObject> getSalaryId(JSONObject object);

	List<JSONObject> getSalaryDetail(JSONObject object);

	List<JSONObject> getSalaryExcel(String salaryId);

	List<JSONObject> getAllSalaryExcel(JSONObject object);

	List<JSONObject> getAllSalaryDetail(JSONObject object);

	int updateSalaryImported(String salaryId);

	int deleteSalary(JSONObject object);

	String getSalaryNameById(String salaryId);

	int deleteSalaryDetail(JSONObject object);

	int deleteSalaryExcel(JSONObject object);

	List<JSONObject> getSalaryIdMax(JSONObject object);

	JSONObject getMaxYearMonth(JSONObject param);
	
	JSONObject getCjSalaryAccount(JSONObject param);
	int updateCjSalaryAccount(JSONObject param);
	int insertCjSalaryAccount(JSONObject param);
	JSONObject getCjSchool(JSONObject param);
	
	List<JSONObject> getTeacherBySalaryId(JSONObject param);

}
