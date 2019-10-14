package com.talkweb.scoreManage.dao.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.base.dao.impl.MyBatisBaseDaoImpl;
import com.talkweb.common.splitDbAndTable.SplitDbAndTableRule;
import com.talkweb.common.splitDbAndTable.SplitDbAndTableRuleFactory;
import com.talkweb.scoreManage.dao.StudentScoreReportDao;

@Repository
public class StudentScoreReportDaoImpl extends MyBatisBaseDaoImpl implements StudentScoreReportDao {

	private static final String MYBATIS_PREFIX = "com.talkweb.scoreReport.dao.StudentScoreReportDao.";
	
	@Autowired
	private SplitDbAndTableRuleFactory splitDbAndTableRuleFactory;
	
	@Override
	public List<JSONObject> getbjcjgzxs(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<JSONObject> list = selectList(MYBATIS_PREFIX + "getbjcjgzxs", splitDbAndTableRule);
		if(list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}
	
	@Override
	public List<JSONObject> getbjcjgzsmxx(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<JSONObject> list = selectList(MYBATIS_PREFIX + "getbjcjgzsmxx", splitDbAndTableRule);
		if(list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}
	
	@Override
	public Map<String, List<String>> getDbxnxq2KslcMap(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<JSONObject> list = selectList(MYBATIS_PREFIX + "getbjcjgzdbkslc", splitDbAndTableRule);
		if(list == null) {
			list = new ArrayList<JSONObject>();
		}
		Map<String, List<String>> dbxnxq2Kslc = new HashMap<String, List<String>>();
		for(JSONObject json : list) {
			String dbxnxq = json.getString("dbxnxq");
			String dbkslc = json.getString("dbkslc");
			if(!dbxnxq2Kslc.containsKey(dbxnxq)) {
				dbxnxq2Kslc.put(dbxnxq, new ArrayList<String>());
			}
			dbxnxq2Kslc.get(dbxnxq).add(dbkslc);
		}
		
		return dbxnxq2Kslc;
	}
	
	@Override
	public List<Long> getBjcjMkGZ(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<Long> list = selectList(MYBATIS_PREFIX + "getBjcjMkGZ", splitDbAndTableRule);
		if(list == null) {
			return new ArrayList<Long>();
		}
		Collections.sort(list, new Comparator<Long>(){
			@Override
			public int compare(Long arg0, Long arg1) {
				return Long.compare(arg0, arg1);
			}
		});
		return list;
	}
	
	@Override
	public List<JSONObject> getbjcjgzcj(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<JSONObject> list = selectList(MYBATIS_PREFIX + "getbjcjgzcj", splitDbAndTableRule);
		if(list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}
}
