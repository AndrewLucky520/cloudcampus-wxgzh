package com.talkweb.scoreManage.service;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.scoreManage.proc.ProgressBar;

/**
 * @ClassName ScoreAnalysisService
 * @author Homer
 * @version 1.0
 * @Description 成绩分析业务逻辑接口
 * @date 2015年3月26日
 */
public interface ScoreAnalysisService {
	void scoreAnalysis(JSONObject params, ProgressBar progressBar) throws Exception;
}
