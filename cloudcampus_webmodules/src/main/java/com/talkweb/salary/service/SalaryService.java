package com.talkweb.salary.service;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.salary.domain.SalDetail;
import com.talkweb.salary.domain.SalExcel;

public interface SalaryService {

	List<JSONObject> getSalaryList(JSONObject object);
	int deleteSalary(JSONObject object);

	int updateSalary(JSONObject object);

	int addSalary(JSONObject object);
	
	JSONObject getSalaries(JSONObject object);
	List<JSONObject> getPersonalSalary(JSONObject onject);
	int addImportSalary(List<SalDetail> successInfos, List<SalExcel> l_salExcel);
	int updateSalaryPublished(JSONObject param);
	List<JSONObject> getAppPersonalSalary(JSONObject param);
	
	JSONObject getCjSalaryAccount(JSONObject param);
	int updateCjSalaryAccount(JSONObject param);
	JSONObject getCjSchool(JSONObject param);
	
	List<JSONObject> getTeacherBySalaryId(JSONObject param);
}
