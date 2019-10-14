package com.talkweb.scoreManage.dao.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.base.dao.impl.MyBatisBaseDaoImpl;
import com.talkweb.common.splitDbAndTable.SplitDbAndTableRule;
import com.talkweb.common.splitDbAndTable.SplitDbAndTableRuleFactory;
import com.talkweb.scoreManage.dao.ClassScoreLevelReportDao;

@Repository
public class ClassScoreLevelReportDaoImpl extends MyBatisBaseDaoImpl implements ClassScoreLevelReportDao {
	private static final String MYBATIS_PREFIX = "com.talkweb.scoreReport.dao.ClassScoreLevelReportDao.";

	@Autowired
	private SplitDbAndTableRuleFactory splitDbAndTableRuleFactory;
	
	@Override
	public List<JSONObject> getAllDjInfo() {
		List<JSONObject> list = selectList(MYBATIS_PREFIX + "getAllDjInfo");
		if(list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}

	@Override
	public List<JSONObject> getDDKMJB(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<JSONObject> list = selectList(MYBATIS_PREFIX + "getDDKMJB", splitDbAndTableRule);
		if(list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}

	@Override
	public List<JSONObject> getQnjpjf(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<JSONObject> list = selectList(MYBATIS_PREFIX + "getQnjpjf", splitDbAndTableRule);
		if(list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}

	@Override
	public List<JSONObject> getNumInfo(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<JSONObject> list = selectList(MYBATIS_PREFIX + "getNumInfo", splitDbAndTableRule);
		if(list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}
	
	@Override
	public List<JSONObject> getAllDjxlInfo(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<JSONObject> list = selectList(MYBATIS_PREFIX + "getAllDjxlInfo", splitDbAndTableRule);
		if(list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}

	@Override
	public List<JSONObject> getAllPmqj(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<JSONObject> list = selectList(MYBATIS_PREFIX + "getAllPmqj", splitDbAndTableRule);
		if(list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}
	
	@Override
	public List<JSONObject> getAllDjxlInformation(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<JSONObject> list = selectList(MYBATIS_PREFIX + "getAllDjxlInformation", splitDbAndTableRule);
		if(list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}

	@Override
	public List<JSONObject> getAllPmInfo(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<JSONObject> list = selectList(MYBATIS_PREFIX + "getAllPmInfo", splitDbAndTableRule);
		if(list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}

	@Override
	public List<JSONObject> getNjPmfb(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<JSONObject> list = selectList(MYBATIS_PREFIX + "getNjPmfb", splitDbAndTableRule);
		if(list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}

	@Override
	public List<JSONObject> getNjDjxl(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<JSONObject> list = selectList(MYBATIS_PREFIX + "getNjDjxl", splitDbAndTableRule);
		if(list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}
}
