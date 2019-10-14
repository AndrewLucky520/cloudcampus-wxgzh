package com.talkweb.archive.dao.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.archive.dao.ArchiveClassScoreReportDao;
import com.talkweb.base.dao.impl.MyBatisBaseDaoImpl;
import com.talkweb.common.splitDbAndTable.SplitDbAndTableRule;
import com.talkweb.common.splitDbAndTable.SplitDbAndTableRuleFactory;

@Repository
public class ArchiveClassScoreReportDaoImpl  extends MyBatisBaseDaoImpl implements ArchiveClassScoreReportDao{

	private static final String MYBATIS_PREFIX = "com.talkweb.scoreManage.dao.ClassScoreReportDao.";
	
	@Autowired
	private SplitDbAndTableRuleFactory splitDbAndTableRuleFactory;
	
	@Override
	public List<JSONObject> getTeacherClassScoreAnalyze(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<JSONObject> list = selectList(MYBATIS_PREFIX + "getTeacherClassScoreAnalyze", splitDbAndTableRule);
		if (list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}

	@Override
	public List<JSONObject> selectViewScoreClassGroupClassList(String termInfoId, Integer autoIncr,
			Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<JSONObject> list = selectList(MYBATIS_PREFIX + "selectViewScoreClassGroupClassList", splitDbAndTableRule);
		if (list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}

	@Override
	public boolean ifExistsTopGroupData(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		Integer result = selectOne(MYBATIS_PREFIX + "ifExistsTopGroupData", splitDbAndTableRule);
		if(result == null) {
			return false;
		}
		return true;
	}

	@Override
	public List<JSONObject> getStudentScoreAverageValue(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<JSONObject> list = selectList(MYBATIS_PREFIX + "getStudentScoreAverageValue", splitDbAndTableRule);
		if (list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}

	@Override
	public List<JSONObject> getExcellentPassRate(String schoolId) {
		List<JSONObject> list = selectList(MYBATIS_PREFIX + "getExcellentPassRate", schoolId);
		if (list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}
	
	
	

	 
}
