package com.talkweb.scoreManage.dao.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.base.dao.impl.MyBatisBaseDaoImpl;
import com.talkweb.common.splitDbAndTable.SplitDbAndTableRule;
import com.talkweb.common.splitDbAndTable.SplitDbAndTableRuleFactory;
import com.talkweb.scoreManage.dao.ScoreManageAPIDao;

@Repository
public class ScoreManageAPIDaoImpl extends MyBatisBaseDaoImpl implements ScoreManageAPIDao {
	private static final String MYBATIS_PREFIX = "com.talkweb.scoreManage.dao.ScoreManageAPIDao.";
	
	@Autowired
	private SplitDbAndTableRuleFactory splitDbAndTableRuleFactory;

	@Override
	public List<JSONObject> getScoreIdAndNameList(String termInfoId, JSONObject params) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, null, params);
		List<JSONObject> list = selectList(MYBATIS_PREFIX + "getScoreIdAndNameList", splitDbAndTableRule);
		if (list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}
	
	@Override
	public boolean ifExistsScoreInfo(String termInfoId, Integer autoIncr, JSONObject params) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, params);
		Integer result = selectOne(MYBATIS_PREFIX + "ifExistsScoreInfo", splitDbAndTableRule);
		if(result == null) {
			return false;
		} else {
			return true;
		}
	}
	
	@Override
	public List<JSONObject> queryScoreInfo(String termInfoId, Integer autoIncr, JSONObject params) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, params);
		List<JSONObject> list = selectList(MYBATIS_PREFIX + "queryScoreInfo", splitDbAndTableRule);
		if (list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}
}
