package com.talkweb.exammanagement.service;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.exammanagement.domain.ExecProgressRedisMsg;

public interface ExamManagementArrangeExamService {
	JSONObject getArrangeExamInfo(JSONObject request);
	
	void arrangeExam(JSONObject request);
	
	void deleteArrangeExamInfo(JSONObject request);
	
	void autoArrangeExam(JSONObject request);
	
	ExecProgressRedisMsg queryProgress(JSONObject request) throws Exception;
}
