package com.talkweb.csbasedata.service;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;

public interface StudentBaseManageService {

	List<JSONObject> getStudentList(JSONObject param)throws Exception;

	JSONObject getStudentInfo(JSONObject param)throws Exception;

	void resetStudentPassword(JSONObject param)throws Exception;

	void deleteStudent(JSONObject param)throws Exception;

	int updateStudent(JSONObject param)throws Exception;

	int getStudentTotal(JSONObject param)throws Exception;

	int insertImportStudentBatch(Map<String, Object> needInsert)throws Exception;
	
}