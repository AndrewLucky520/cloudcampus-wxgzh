package com.talkweb.scoreManage.service;

import com.alibaba.fastjson.JSONObject;

public interface ClassScoreLevelReportService {
	JSONObject getLevelSubjectStatisTabList(JSONObject params);
	
	JSONObject getLeveStudentNumStatisTabList(JSONObject params);
}
