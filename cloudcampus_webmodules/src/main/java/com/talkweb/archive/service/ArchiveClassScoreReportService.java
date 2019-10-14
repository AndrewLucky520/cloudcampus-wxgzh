package com.talkweb.archive.service;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.School;

public interface ArchiveClassScoreReportService {

	 List<JSONObject> produceTeacherScoreOnInThreeReportData(School school, JSONObject params);
	
}
