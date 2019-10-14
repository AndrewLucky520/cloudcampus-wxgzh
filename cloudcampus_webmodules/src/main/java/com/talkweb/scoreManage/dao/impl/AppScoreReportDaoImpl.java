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
import com.talkweb.scoreManage.dao.AppScoreReportDao;

@Repository
public class AppScoreReportDaoImpl extends MyBatisBaseDaoImpl implements AppScoreReportDao {

	@Autowired
	private SplitDbAndTableRuleFactory splitDbAndTableRuleFactory;
	
	private static final String MYBATIS_PREFIX = "com.talkweb.scoreReport.dao.AppScoreReportDao.";

	@Override
	public List<JSONObject> getExamList(String termInfoId, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, null, map);
		List<JSONObject> list = selectList(MYBATIS_PREFIX + "getAppExamList", splitDbAndTableRule);
		if(list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}
	
	@Override
	public boolean ifExistsStuInExamScore(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		Integer result = selectOne(MYBATIS_PREFIX + "ifExistsStuInExamScore", splitDbAndTableRule);
		if(result == null) {
			return false;
		}
		return true;
	}

	@Override
	public List<JSONObject> getClassExamTotalScoreInfoList(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<JSONObject> list = selectList(MYBATIS_PREFIX + "getClassExamTotalScoreInfoList", splitDbAndTableRule);
		if(list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}

	@Override
	public List<JSONObject> getOfficialExamTotalScoreInfoList(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<JSONObject> list = selectList(MYBATIS_PREFIX + "getOfficialExamTotalScoreInfoList", splitDbAndTableRule);
		if(list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}

	@Override
	public List<JSONObject> getOfficialExamAchievementList(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<JSONObject> list = selectList(MYBATIS_PREFIX + "getOfficialExamAchievementList", splitDbAndTableRule);
		if(list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}

	@Override
	public String getOfficailAppStudentReport(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		return selectOne(MYBATIS_PREFIX + "getOfficailAppStudentReport", splitDbAndTableRule);
	}

	@Override
	public List<JSONObject> selectSchoolExamSubjectScoreList(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<JSONObject> list = selectList(MYBATIS_PREFIX + "selectSchoolExamSubjectScoreList", splitDbAndTableRule);
		if(list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}
	
	@Override
	public List<JSONObject> getFrontFiveSchoolExamList(String termInfoId, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, null, map);
		List<JSONObject> list = selectList(MYBATIS_PREFIX + "getFrontFiveSchoolExamList", splitDbAndTableRule);
		if(list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}
	
	@Override
	public int insertExamView(String termInfoId, Integer autoIncr, Object obj) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, obj);
		return insert(MYBATIS_PREFIX + "insertExamView", splitDbAndTableRule);
	}
	
	@Override
	public List<JSONObject> selectClassExamEverSubjectScore(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<JSONObject> list = selectList(MYBATIS_PREFIX + "selectClassExamEverSubjectScore", splitDbAndTableRule);
		if(list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}
	
	@Override
	public JSONObject selectClassExamTotalScore(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		return selectOne(MYBATIS_PREFIX + "selectClassExamTotalScore", splitDbAndTableRule);
	}
	
	@Override
	public JSONObject selectClassExamClassTotalScore(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		return selectOne(MYBATIS_PREFIX + "selectClassExamClassTotalScore", splitDbAndTableRule);
	}
	
	@Override
	public List<Long> selectClassExamSubjectList(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<Long> list = selectList(MYBATIS_PREFIX + "selectClassExamSubjectList", splitDbAndTableRule);
		if(list == null) {
			return new ArrayList<Long>();
		}
		return list;
	}
	
	@Override
	public List<JSONObject> selectCustomScoreByStudentId(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<JSONObject> list = selectList(MYBATIS_PREFIX + "selectCustomScoreByStudentId", splitDbAndTableRule);
		if(list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}
	
	@Override
	public List<JSONObject> selectTeacherExamSoreList(String termInfoId, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, null, map);
		List<JSONObject> list = selectList(MYBATIS_PREFIX + "selectTeacherExamSoreList", splitDbAndTableRule);
		if(list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}
	
	@Override
	public List<Long> getBhListFromStatistics(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<Long> list = selectList(MYBATIS_PREFIX + "getBhListFromStatistics", splitDbAndTableRule);
		if(list == null) {
			return new ArrayList<Long>();
		}
		return list;
	}
	
	@Override
	public List<Long> getClassIdFromClassExamRelative(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<Long> list = selectList(MYBATIS_PREFIX + "getClassIdFromClassExamRelative", splitDbAndTableRule);
		if(list == null) {
			return new ArrayList<Long>();
		}
		return list;
	}
	
	@Override
	public List<Long> getClassIdFromCustomScoreInfo(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<Long> list = selectList(MYBATIS_PREFIX + "getClassIdFromCustomScoreInfo", splitDbAndTableRule);
		if(list == null) {
			return new ArrayList<Long>();
		}
		return list;
	}
	
	@Override
	public List<JSONObject> selectParentViewList(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<JSONObject> list = selectList(MYBATIS_PREFIX + "selectParentViewList", splitDbAndTableRule);
		if(list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}
	
	@Override
	public List<JSONObject> selectSchoolExamCount(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<JSONObject> list = selectList(MYBATIS_PREFIX + "selectSchoolExamCount", splitDbAndTableRule);
		if(list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}
	
	@Override
	public List<JSONObject> selectClassExamCount(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<JSONObject> list = selectList(MYBATIS_PREFIX + "selectClassExamCount", splitDbAndTableRule);
		if(list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}
	
	@Override
	public List<JSONObject> selectCustomExamCount(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<JSONObject> list = selectList(MYBATIS_PREFIX + "selectCustomExamCount", splitDbAndTableRule);
		if(list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}
	
	@Override
	public String selectClassReportViewList(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		return selectOne(MYBATIS_PREFIX + "selectClassReportViewList", splitDbAndTableRule);
	}
	
	@Override
	public List<Long> selectSchoolExamUser(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<Long> list = selectList(MYBATIS_PREFIX + "selectSchoolExamUser", splitDbAndTableRule);
		if(list == null) {
			return new ArrayList<Long>();
		}
		return list;
	}

	@Override
	public List<Long> selectClassExamUser(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<Long> list = selectList(MYBATIS_PREFIX + "selectClassExamUser", splitDbAndTableRule);
		if(list == null) {
			return new ArrayList<Long>();
		}
		return list;
	}
	
	@Override
	public List<Long> selectCustomExamUser(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<Long> list = selectList(MYBATIS_PREFIX + "selectCustomExamUser", splitDbAndTableRule);
		if(list == null) {
			return new ArrayList<Long>();
		}
		return list;
	}
	
	@Override
	public Set<Long> selectParentView(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<Long> list = selectList(MYBATIS_PREFIX + "selectParentView", splitDbAndTableRule);
		if(list == null) {
			return new HashSet<Long>();
		}
		return new HashSet<Long>(list);
	}
	
	@Override
	public List<JSONObject> selectClassExamSubjectScoreList(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<JSONObject> list = selectList(MYBATIS_PREFIX + "selectClassExamSubjectScoreList", splitDbAndTableRule);
		if(list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}

	@Override
	public JSONObject getGradeExamScore(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		JSONObject result = selectOne(MYBATIS_PREFIX + "getGradeExamScore", splitDbAndTableRule);
		return result;
	}

	@Override
	public List<JSONObject> getStudentList(String termInfoId, Integer autoIncr, JSONObject param) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, param);
		Integer examType =param.getInteger("examType");
		List<JSONObject> list = null;
		if (examType == 0) {
			list = selectList(MYBATIS_PREFIX + "getStudentList", splitDbAndTableRule);
		}else if (examType == 1) {
			list = selectList(MYBATIS_PREFIX + "getStudentList1", splitDbAndTableRule);
		}else  if (examType == 2){
			list = selectList(MYBATIS_PREFIX + "getStudentList2", splitDbAndTableRule);
		}
		if(list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}

	@Override
	public List<JSONObject> getExamStudentScoreReport(String termInfoId, Integer autoIncr, JSONObject param) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, param);
		Integer examType =param.getInteger("examType");
		List<JSONObject> list = null;
		if (examType == 0) {
			 list = selectList(MYBATIS_PREFIX + "getExamStudentScoreReport", splitDbAndTableRule);
		}else if (examType == 1) {
			 list = selectList(MYBATIS_PREFIX + "getExamStudentScoreReport1", splitDbAndTableRule);
		}else if (examType == 2) {
			 list = selectList(MYBATIS_PREFIX + "getExamStudentScoreReport2", splitDbAndTableRule);
		}
		
		return list;
	}

	@Override
	public boolean ifExistsClassOrStuInExamScore(String termInfoId, Integer autoIncr, JSONObject param) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, param);
		Integer examType =param.getInteger("examType");
		if (examType == 0) {
			Integer result = selectOne(MYBATIS_PREFIX + "ifExistsClassOrStuInExamScore", splitDbAndTableRule);
			if (result == null) {
				return false;
			}
		}else if (examType == 1 || examType == 2) {
			Integer result = selectOne(MYBATIS_PREFIX + "ifExistsClassExamScore", splitDbAndTableRule);
			if (result == null) {
				return false;
			}
		} 
		return true;
	}

	
	@Override
	public List<JSONObject> getSchoolExamList(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<JSONObject> list = selectList(MYBATIS_PREFIX + "getSchoolExamList", splitDbAndTableRule);
		return list;
	}

	@Override
	public List<JSONObject> getFullScore(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<JSONObject> list = selectList(MYBATIS_PREFIX + "getFullScore", splitDbAndTableRule);
		return list;
	}

	@Override
	public JSONObject getClassAvergaeScore(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		JSONObject result = selectOne(MYBATIS_PREFIX + "getClassAvergaeScore", splitDbAndTableRule);
		return result;
	}

	@Override
	public JSONObject getGradeAvergaeScore(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		JSONObject result = selectOne(MYBATIS_PREFIX + "getGradeAvergaeScore", splitDbAndTableRule);
		return result;
	}
 
}
