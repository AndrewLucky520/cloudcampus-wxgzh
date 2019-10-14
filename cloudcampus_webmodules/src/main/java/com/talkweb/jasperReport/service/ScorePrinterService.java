package com.talkweb.jasperReport.service;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.jasperReport.bean.ClassScoreHead;
import com.talkweb.jasperReport.bean.ClassScoreMiddle;
import com.talkweb.jasperReport.bean.ClassScoreTail;
import com.talkweb.jasperReport.bean.ScoreTrendBean;

public interface ScorePrinterService {
		
	List<ClassScoreHead> getClassScoreHeadList(JSONObject data);
	
	List<ClassScoreMiddle> getClassScoreMiddleList(JSONObject data);
	
	List<ClassScoreTail> getClassScoreTailList(JSONObject data);
	
	Map<String,List<?>> getGradeReportList(JSONObject data);
	
	List<ScoreTrendBean> getResultsTrendList(List<JSONObject> datas,String head,String type);

}