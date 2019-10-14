package com.talkweb.scoreManage.dao.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.base.dao.impl.MyBatisBaseDaoImpl;
import com.talkweb.common.splitDbAndTable.SplitDbAndTableRule;
import com.talkweb.common.splitDbAndTable.SplitDbAndTableRuleFactory;
import com.talkweb.scoreManage.business.ClassScoreInParam;
import com.talkweb.scoreManage.dao.ClassScoreReportDao;

@Repository
public class ClassScoreReportDaoImpl extends MyBatisBaseDaoImpl implements ClassScoreReportDao {

	private static final String MYBATIS_PREFIX = "com.talkweb.scoreManage.dao.ClassScoreReportDao.";

	@Autowired
	private SplitDbAndTableRuleFactory splitDbAndTableRuleFactory;

	@Override
	public List<JSONObject> getClassScores(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<JSONObject> list = selectList(MYBATIS_PREFIX + "getClassScores", splitDbAndTableRule);
		if (list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}

	@Override
	public List<JSONObject> getClassScoreAnalyze(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<JSONObject> list = selectList(MYBATIS_PREFIX + "getClassScoreAnalyze", splitDbAndTableRule);
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
	public Set<String> getBhFromScoreInfo(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<String> list = selectList(MYBATIS_PREFIX + "getBhFromScoreInfo", splitDbAndTableRule);
		if (list == null) {
			return new HashSet<String>();
		}
		return new HashSet<String>(list);
	}
	
	@Override
	public List<JSONObject> getGradeScoreAnalyze(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<JSONObject> list = selectList(MYBATIS_PREFIX + "getGradeScoreAnalyze", splitDbAndTableRule);
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
	public List<JSONObject> getAGradeRate(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<JSONObject> list = selectList(MYBATIS_PREFIX + "getAGradeRate", splitDbAndTableRule);
		if (list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}

	@Override
	public List<JSONObject> getAGroupGradeRate(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<JSONObject> list = selectList(MYBATIS_PREFIX + "getAGroupGradeRate", splitDbAndTableRule);
		if (list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}
	
	
	
	
	
	
	
	
	@Override
	public int getTopGourpNumber(ClassScoreInParam param) {
		return selectOne(MYBATIS_PREFIX + "getTopGourpNumber", param);
	}

	@Override
	public List<JSONObject> getClassCourses(ClassScoreInParam param) {
		return selectList(MYBATIS_PREFIX + "getClassCourses", param);
	}

	@Override
	public List<JSONObject> getAllIndexClassScoreList(ClassScoreInParam param) {
		return selectList(MYBATIS_PREFIX + "getAllIndexClassScoreList", param);
	}

	@Override
	public List<JSONObject> getAllIndexGradeScorelist(ClassScoreInParam param) {
		return selectList(MYBATIS_PREFIX + "getAllIndexClassScoreList", param);
	}

	@Override
	public List<JSONObject> getMaxMinScoreByCourseList(ClassScoreInParam param) {
		return selectList(MYBATIS_PREFIX + "getMaxMinScoreByCourseList", param);
	}

	@Override
	public List<JSONObject> getFullRangeEveryTeacherAverageList(ClassScoreInParam param) {
		return selectList(MYBATIS_PREFIX + "getFullRangeEveryTeacherAverageList", param);
	}

	
	@Override
	public JSONObject getDeanScoreAnalyzeGrade(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		JSONObject result = selectOne(MYBATIS_PREFIX + "getDeanScoreAnalyzeGrade", splitDbAndTableRule);
		return result;
	}

	@Override
	public List<JSONObject> getDeanScoreAnalyzeClass(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<JSONObject> list = selectList(MYBATIS_PREFIX + "getDeanScoreAnalyzeClass", splitDbAndTableRule);
		return list;
	}

	@Override
	public JSONObject getTeacherScoreAnalyzeGrade(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		JSONObject result = selectOne(MYBATIS_PREFIX + "getTeacherScoreAnalyzeGrade", splitDbAndTableRule);
		return result;
	}

	@Override
	public JSONObject getTeacherScoreAnalyzeClass(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		JSONObject result = selectOne(MYBATIS_PREFIX + "getTeacherScoreAnalyzeClass", splitDbAndTableRule);
		return result;
	}
}
