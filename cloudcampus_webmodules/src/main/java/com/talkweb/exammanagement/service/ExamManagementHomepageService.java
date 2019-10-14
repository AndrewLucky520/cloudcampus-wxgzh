package com.talkweb.exammanagement.service;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.exammanagement.domain.ExamManagement;

public interface ExamManagementHomepageService {
	List<ExamManagement> getExamManagementList(JSONObject request);
	
	void insertOrupdateExamManagement(JSONObject request);
	
	void deleteExamManagement(JSONObject request);
}
