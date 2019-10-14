package com.talkweb.scoreManage.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.common.exception.CommonRunException;
import com.talkweb.common.splitDbAndTable.TermInfoIdUtils;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.scoreManage.dao.ScoreManageAPIDao;
import com.talkweb.scoreManage.dao.ScoreManageDao;
import com.talkweb.scoreManage.po.gm.DegreeInfo;
import com.talkweb.scoreManage.service.ScoreManageAPIService;

@Service
public class ScoreManageAPIServiceImpl implements ScoreManageAPIService {
	@Autowired
	private ScoreManageAPIDao scoreManageAPIDao;
	
	@Autowired
	private ScoreManageDao scoreManageDao;
	
	@Autowired
	private AllCommonDataService allCommonDataService;

	@Override
	public List<JSONObject> getScoreIdAndNameList(String termInfoId, String schoolId, String usedGrade, List<?> classIds, List<?> subjectIds) {
		List<String> termInfoIdList = TermInfoIdUtils.getUserAllTermInfoIdsByUsedGrade(allCommonDataService, usedGrade, termInfoId);
		
		JSONObject params = new JSONObject();
		params.put("nj", usedGrade);
		params.put("xxdm", schoolId);
		if(CollectionUtils.isNotEmpty(classIds)) {
			params.put("bhList", classIds);
		}
		if(CollectionUtils.isNotEmpty(subjectIds)) {
			params.put("kmdmList", subjectIds);
		}
		
		List<JSONObject> result = new ArrayList<JSONObject>();
		for(String xnxq : termInfoIdList) {
			params.put("xnxq", xnxq);
			List<JSONObject> list = scoreManageAPIDao.getScoreIdAndNameList(xnxq, params);
			for(JSONObject json : list) {
				Integer autoIncr = json.getInteger("autoIncr");
				String examId = json.getString("examId");
				params.put("kslc", examId);
				if(scoreManageAPIDao.ifExistsScoreInfo(xnxq, autoIncr, params)) {
					json.remove("autoIncr");
					result.add(json);
				}
			}
		}
		return result;
	}

	@Override
	public List<JSONObject> queryScoreInfo(String examId, String schoolId, String termInfoId, List<?> subjectId, List<?> classIds) {
		JSONObject params = new JSONObject();
		params.put("kslc", examId);
		params.put("kslcdm", examId);
		params.put("xnxq", termInfoId);
		params.put("xxdm", schoolId);
		if(CollectionUtils.isNotEmpty(subjectId)) {
			params.put("kmdm", subjectId);
		}
		if(CollectionUtils.isNotEmpty(classIds)) {
			params.put("bhList", classIds);
		}
		
		DegreeInfo degreeInfo = scoreManageDao.getDegreeInfoById(termInfoId, params);
		if(degreeInfo == null) {
			throw new CommonRunException(-1, "无法从数据库中找到对应的考试轮次！");
		}
		Integer autoIncr = degreeInfo.getAutoIncr();
		
		return scoreManageAPIDao.queryScoreInfo(termInfoId, autoIncr, params);
	}
}
