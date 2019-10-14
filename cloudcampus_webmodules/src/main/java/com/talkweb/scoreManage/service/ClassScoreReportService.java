package com.talkweb.scoreManage.service;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.School;
import com.talkweb.scoreManage.business.ClassScoreInParam;

public interface ClassScoreReportService {
	/***
	 * 组织成绩全指标分析表数据
	 * 
	 * @param param
	 * @return
	 */
	JSONObject produceScoreAllIndexDataModel(ClassScoreInParam param);

	/****
	 * 生成组织一分三率表的数据，包括动态表头数据。
	 * 
	 * @param param
	 * @return
	 */
	JSONObject produceScoreOnInThreeReportData(School school, JSONObject params);

	/***
	 * 生成班级对比表数据
	 * 
	 * @param param
	 * @return
	 */
	JSONObject produceClassScoreData(School school, JSONObject params);

	/****
	 * 生成等第成绩一分三率表(等第成绩)
	 * 
	 * @param param
	 * @return
	 */
	JSONObject produceLevelAllStatisReportData(School school, JSONObject params);
 

}
