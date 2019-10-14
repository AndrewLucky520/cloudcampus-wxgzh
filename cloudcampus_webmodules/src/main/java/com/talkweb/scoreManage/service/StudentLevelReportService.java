package com.talkweb.scoreManage.service;

import com.alibaba.fastjson.JSONObject;

public interface StudentLevelReportService {
	JSONObject getStudentLevelScoreResultDetailTab(JSONObject params);
	
	JSONObject getStudentLevelWeakSubjectList(JSONObject params);
	
	JSONObject getLevelSubjectAOneThirdStatisList(JSONObject params);
	
	JSONObject getLevelTotalScoreStaticList(JSONObject params);
	
	JSONObject getLevelEveryAStatisTabList(JSONObject params);
}
