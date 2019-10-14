package com.talkweb.questionnaire.service;

import com.alibaba.fastjson.JSONObject;

public interface AppQuestionnaireService {
	JSONObject queryQuestionnaireByUser(JSONObject request) throws Exception;

	void updateForSpecificTarget(JSONObject request) throws Exception;
	
	void updateForTableTarget(JSONObject request) throws Exception;
}
