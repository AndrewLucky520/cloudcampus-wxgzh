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
import com.talkweb.scoreManage.dao.ViewClassScoreDao;
import com.talkweb.scoreManage.po.ce.ClassExamSubjectScore;
import com.talkweb.scoreManage.po.ce.CustomScore;
import com.talkweb.scoreManage.po.ce.CustomScoreInfo;
import com.talkweb.scoreManage.po.ce.StudentTotalScoreStatistics;

@Repository
public class ViewClassScoreDaoImpl extends MyBatisBaseDaoImpl implements ViewClassScoreDao {
	private static final String packages = "com.talkweb.scoreReport.dao.ViewClassScoreDao.";
	
	@Autowired
	private SplitDbAndTableRuleFactory splitDbAndTableRuleFactory;

	@Override
	public List<Long> getClassIdsInClassExam(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<Long> result = selectList(packages + "getClassIdsInClassExam", splitDbAndTableRule);
		if(result == null) {
			return new ArrayList<Long>();
		}
		return result;
	}
	
	@Override
	public List<Long> getClassExamSubjectIdList(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<Long> list = selectList(packages + "getClassExamSubjectIdList", splitDbAndTableRule);
		if(list == null) {
			return new ArrayList<Long>();
		}
		return list;
	}

	@Override
	public List<ClassExamSubjectScore> getClassExamSubjectScoreList(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<ClassExamSubjectScore> list = selectList(packages + "getClassExamSubjectScoreList", splitDbAndTableRule);
		if(list == null) {
			return new ArrayList<ClassExamSubjectScore>();
		}
		return list;
	}

	@Override
	public List<StudentTotalScoreStatistics> getClassExamTotalScore(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<StudentTotalScoreStatistics> list = selectList(packages + "getClassExamTotalScore", splitDbAndTableRule);
		if(list == null) {
			return new ArrayList<StudentTotalScoreStatistics>();
		}
		return list;
	}

	@Override
	public List<CustomScore> getAllCustomExcel(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<CustomScore> list = selectList(packages + "getAllCustomExcel", splitDbAndTableRule);
		if(list == null) {
			return new ArrayList<CustomScore>();
		}
		return list;
	}

	@Override
	public List<CustomScoreInfo> getAllCustomDetail(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<CustomScoreInfo> list = selectList(packages + "getAllCustomDetail", splitDbAndTableRule);
		if(list == null) {
			return new ArrayList<CustomScoreInfo>();
		}
		return list;
	}
	
	@Override
	public List<JSONObject> getClassExamListByTeacher(String termInfoId, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, null, map);
		List<JSONObject> list = selectList(packages + "getClassExamListByTeacher", splitDbAndTableRule);
		if(list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}
	
	@Override
	public List<Long> getClassIdFromClassExam(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<Long> list = selectList(packages + "getClassIdFromClassExam", splitDbAndTableRule);
		if(list == null) {
			return new ArrayList<Long>();
		}
		return list;
	}
}
