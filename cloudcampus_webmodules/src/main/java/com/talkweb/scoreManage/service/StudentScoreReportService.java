package com.talkweb.scoreManage.service;

import com.alibaba.fastjson.JSONObject;

public interface StudentScoreReportService {
	JSONObject getClassScoreReportList(JSONObject params);
	
	JSONObject getStudentScoreResultTrackList(JSONObject params);
	
	JSONObject getScoreSectionTotalList(JSONObject params);
	
	JSONObject getScoreSectionSubjectList(JSONObject params);
	
	JSONObject getRankSectionList(JSONObject params);
}
