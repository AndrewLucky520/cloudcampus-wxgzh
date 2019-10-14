package com.talkweb.exammanagement.dao.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.base.dao.impl.MyBatisBaseDaoImpl;
import com.talkweb.common.splitDbAndTable.SplitDbAndTableRule;
import com.talkweb.common.splitDbAndTable.SplitDbAndTableRuleFactory;
import com.talkweb.exammanagement.dao.ExamManagementViewDao;
@Repository
public class ExamManagementViewDaoImpl extends MyBatisBaseDaoImpl implements ExamManagementViewDao{
	
	@Autowired
	private SplitDbAndTableRuleFactory splitDbAndTableRuleFactory;
	
	public static final String packages="com.talkweb.exammanagement.dao.ExamManagementViewDao.";
	@Override
	public List<JSONObject> getExamPlaceInfo(Map<String, Object> param) {
		// TODO Auto-generated method stub
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(param.get("termInfo").toString(), param.get("autoIncr")==null?null:(int)param.get("autoIncr"), param);
		return selectList(packages+"getExamPlaceInfo",splitDbAndTableRule);
	}

	@Override
	public List<JSONObject> getTableCornerList(Map<String, Object> param) {
		// TODO Auto-generated method stub
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(param.get("termInfo").toString(), param.get("autoIncr")==null?null:(int)param.get("autoIncr"), param);
		return selectList(packages+"getTableCornerList",splitDbAndTableRule);
	}

	@Override
	public int getTableCornerListcount(Map<String, Object> param) {
		// TODO Auto-generated method stub
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(param.get("termInfo").toString(), param.get("autoIncr")==null?null:(int)param.get("autoIncr"), param);
		Integer g=selectOne(packages+"getTableCornerListcount",splitDbAndTableRule);
		if(g==null)g=0;
		return g;
	}

	@Override
	public List<JSONObject> getTClassAndExamPlace(Map<String, Object> param) {
		// TODO Auto-generated method stub
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(param.get("termInfo").toString(), param.get("autoIncr")==null?null:(int)param.get("autoIncr"), param);
		return selectList(packages+"getTClassAndExamPlace",splitDbAndTableRule);
	}

	@Override
	public int getTClassAndExamPlaceCount(Map<String, Object> param) {
		// TODO Auto-generated method stub
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(param.get("termInfo").toString(), param.get("autoIncr")==null?null:(int)param.get("autoIncr"), param);
		Integer g=selectOne(packages+"getTClassAndExamPlaceCount",splitDbAndTableRule);
		if(g==null)g=0;
		return g;
	}

	@Override
	public List<JSONObject> getStudsAndExamPlace(Map<String, Object> param) {
		// TODO Auto-generated method stub
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(param.get("termInfo").toString(), param.get("autoIncr")==null?null:(int)param.get("autoIncr"), param);
		return selectList(packages+"getStudsAndExamPlace",splitDbAndTableRule);
	}

	@Override
	public int getStudsAndExamPlaceCount(Map<String, Object> param) {
		// TODO Auto-generated method stub
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(param.get("termInfo").toString(), param.get("autoIncr")==null?null:(int)param.get("autoIncr"), param);
		Integer g=selectOne(packages+"getStudsAndExamPlaceCount",splitDbAndTableRule);
		if(g==null)g=0;
		return g;
	}

	@Override
	public List<JSONObject> getTableCornerSeatList(Map<String, Object> param) {
		// TODO Auto-generated method stub
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(param.get("termInfo").toString(), param.get("autoIncr")==null?null:(int)param.get("autoIncr"), param);
		return selectList(packages+"getTableCornerSeatList",splitDbAndTableRule);
	}

	@Override
	public int getTableCornerSeatListCount(Map<String, Object> param) {
		// TODO Auto-generated method stub
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(param.get("termInfo").toString(), param.get("autoIncr")==null?null:(int)param.get("autoIncr"), param);
		Integer g=selectOne(packages+"getTableCornerSeatListCount",splitDbAndTableRule);
		if(g==null)g=0;
		return g;
	}

	@Override
	public List<JSONObject> getExamSubjectDist(Map<String, Object> param) {
		// TODO Auto-generated method stub
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(param.get("termInfo").toString(), param.get("autoIncr")==null?null:(int)param.get("autoIncr"), param);
		return selectList(packages+"getExamSubjectDist",splitDbAndTableRule);
	}

}
