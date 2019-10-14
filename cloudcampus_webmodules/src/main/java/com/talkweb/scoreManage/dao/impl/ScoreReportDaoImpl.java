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
import com.talkweb.common.tools.StringUtil;
import com.talkweb.scoreManage.dao.ScoreReportDao;

@Repository
public class ScoreReportDaoImpl extends MyBatisBaseDaoImpl implements ScoreReportDao {

	@Autowired
	private SplitDbAndTableRuleFactory splitDbAndTableRuleFactory;
	
	private static final String MYBATIS_PREFIX = "com.talkweb.scoreReport.dao.ScoreReportDao.";

	@Override
	public boolean ifExistsDegreeInfoByStudId(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		Integer result = selectOne(MYBATIS_PREFIX + "ifExistsDegreeInfoByStudId", splitDbAndTableRule);
		if(result == null) {
			return false;
		}
		return true;
	}
	
	@Override
	public boolean ifExistsClassExamInfoByStudId(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		Integer result = selectOne(MYBATIS_PREFIX + "ifExistsClassExamInfoByStudId", splitDbAndTableRule);
		if(result == null) {
			return false;
		}
		return true;
	}
	
	@Override
	public boolean ifExistsCustomClassExamInfoByStudId(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		Integer result = selectOne(MYBATIS_PREFIX + "ifExistsCustomClassExamInfoByStudId", splitDbAndTableRule);
		if(result == null) {
			return false;
		}
		return true;
	}
	
	@Override
	public List<JSONObject> getStudentClassExamByExamId(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<JSONObject> list = selectList(MYBATIS_PREFIX + "getStudentClassExamByExamId", splitDbAndTableRule);
		if(list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}
	
	@Override
	public JSONObject getStudentAnalysisReport(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		return selectOne(MYBATIS_PREFIX + "getStudentAnalysisReport", splitDbAndTableRule);
	}
	
	@Override
	public List<JSONObject> getSchoolExamSubjectScoreList(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<JSONObject> list = selectList(MYBATIS_PREFIX + "getSchoolExamSubjectScoreList", splitDbAndTableRule);
		if(list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}
	
	@Override
	public List<JSONObject> getScoreReportTypeList(Map<String, Object> map) {
		List<JSONObject> list = selectList(MYBATIS_PREFIX + "getScoreReportTypeList", map);
		if (list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}
	
	@Override
	public Integer getNjpmMax(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		return selectOne(MYBATIS_PREFIX + "getNjpmMax", splitDbAndTableRule);
	}
	
	@Override
	public List<String> getKmdmListFromScoretjmk(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<String> list = selectList(MYBATIS_PREFIX + "getKmdmListFromScoretjmk", splitDbAndTableRule);
		if(list == null) {
			return new ArrayList<String>();
		}
		return list;
	}
	
	@Override
	public List<JSONObject> getBjcjMk(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<JSONObject> list = selectList(MYBATIS_PREFIX + "getBjcjMk", splitDbAndTableRule);
		if(list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}
	
	@Override
	public List<JSONObject> getBjcjXs(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<JSONObject> list = selectList(MYBATIS_PREFIX + "getBjcjXs", splitDbAndTableRule);
		if(list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}
	
	@Override
	public List<JSONObject> getNoExamStudentList(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<JSONObject> list = selectList(MYBATIS_PREFIX + "getNoExamStudentList", splitDbAndTableRule);
		if(list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}
	
	@Override
	public List<JSONObject> getBjcjCj(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<JSONObject> list = selectList(MYBATIS_PREFIX + "getBjcjCj", splitDbAndTableRule);
		if(list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}
	
	@Override
	public List<JSONObject> getClassScoreStudentList(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<JSONObject> list = selectList(MYBATIS_PREFIX + "getClassScoreStudentList", splitDbAndTableRule);
		if(list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}
	
	@Override
	public List<JSONObject> getClassScoreList(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<JSONObject> list = selectList(MYBATIS_PREFIX + "getClassScoreList", splitDbAndTableRule);
		if(list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}
	
	@Override
	public List<JSONObject> getClassScores(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<JSONObject> list = selectList(MYBATIS_PREFIX + "getClassScores", splitDbAndTableRule);
		if(list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}
	
	@Override
	public List<JSONObject> getAssignLevelStudentNum(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<JSONObject> list = selectList(MYBATIS_PREFIX + "getAssignLevelStudentNum", splitDbAndTableRule);
		if(list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}
	
	@Override
	public List<JSONObject> getCompetitionStuStatistics(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<JSONObject> list = selectList(MYBATIS_PREFIX + "getCompetitionStuStatistics", splitDbAndTableRule);
		if(list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}
	
	@Override
	public List<JSONObject> getCompetitionStu(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<JSONObject> list = selectList(MYBATIS_PREFIX + "getCompetitionStu", splitDbAndTableRule);
		if(list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}
	
	@Override
	public List<Long> getStudentAllExamSubjectList(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<Long> list = selectList(MYBATIS_PREFIX + "getStudentAllExamSubjectList", splitDbAndTableRule);
		if(list == null) {
			return new ArrayList<Long>();
		}
		return list;
	}
	
	@Override
	public List<JSONObject> getCompetitionStuExamScoreList(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<JSONObject> list = selectList(MYBATIS_PREFIX + "getCompetitionStuExamScoreList", splitDbAndTableRule);
		if(list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}
	
	@Override
	public List<JSONObject> getSubjectScoreClassStatistics(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<JSONObject> list = selectList(MYBATIS_PREFIX + "getSubjectScoreClassStatistics", splitDbAndTableRule);
		if(list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}
	
	@Override
	public List<JSONObject> getSubjectTotalScoreStatistics(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<JSONObject> list = selectList(MYBATIS_PREFIX + "getSubjectTotalScoreStatistics", splitDbAndTableRule);
		if(list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}
	
	@Override
	public List<JSONObject> getStudentScores(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<JSONObject> list = selectList(MYBATIS_PREFIX + "getStudentScores", splitDbAndTableRule);
		if(list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}
	
	@Override
	public List<JSONObject> getExamineNameList(String termInfoId, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, null, map);
		List<JSONObject> list = selectList(MYBATIS_PREFIX + "getExamineNameList", splitDbAndTableRule);
		if(list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}
	
	@Override
	public List<JSONObject> getClassAverageScoreList(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<JSONObject> list = selectList(MYBATIS_PREFIX + "getClassAverageScoreList", splitDbAndTableRule);
		if(list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}
	
	@Override
	public Integer getExamNormalSubjectCount(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		Integer num = selectOne(MYBATIS_PREFIX + "getExamNormalSubjectCount", splitDbAndTableRule);
		if(num != null) {
			return num;
		}
		return 0;
	}
	
	@Override
	public List<JSONObject> getLevelStudentNumList(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<JSONObject> list = selectList(MYBATIS_PREFIX + "getLevelStudentNumList", splitDbAndTableRule);
		if(list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}
	
	@Override
	public JSONObject getClassReportList(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		return selectOne(MYBATIS_PREFIX + "getClassReportList", splitDbAndTableRule);
	}
	
	@Override
	public List<JSONObject> getStudentAllExamScoreList(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<JSONObject> list = selectList(MYBATIS_PREFIX + "getStudentAllExamScoreList", splitDbAndTableRule);
		if(list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}
	
	@Override
	public JSONObject getARnj(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		return selectOne(MYBATIS_PREFIX + "getARnj", splitDbAndTableRule);
	}

	@Override
	public List<Long> getExamSubjectList(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<Long> list = selectList(MYBATIS_PREFIX + "getSubjIdListFromScoreInfo", splitDbAndTableRule);
		if(list == null) {
			list = new ArrayList<Long>();
		}
		
		String isModify = StringUtil.transformString(map.get("isModify"));
		if(!isModify.equals("1")) {
			List<Long> list2 = selectList(MYBATIS_PREFIX + "getSubjIdListFromSynthScore", splitDbAndTableRule);
			if(list2 == null) {
				list2 = new ArrayList<Long>();
			}
			for(Long subjId : list2) {
				if(!list.contains(subjId)) {
					list.add(subjId);
				}
			}
		}
		return list;
	}
	
	@Override
	public int insertStudentScoreReportTrace(String termInfoId, Integer autoIncr, Object obj) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, obj);
		return insert(MYBATIS_PREFIX + "insertStudentScoreReportTrace", splitDbAndTableRule);
	}

	@Override
	public List<Long> getSubjIdFromCompetitionStu(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<Long> list = selectList(MYBATIS_PREFIX + "getSubjIdFromCompetitionStu", splitDbAndTableRule);
		if(list == null) {
			return new ArrayList<Long>();
		}
		return list;
	}

	@Override
	public List<JSONObject> getStuSubjectScoreRank_ScoreReport(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<JSONObject> list = selectList(MYBATIS_PREFIX + "getStuSubjectScoreRank_ScoreReport", splitDbAndTableRule);
		if(list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}
	
	@Override
	public JSONObject getSchedule(Map<String, Object> param) {
		// TODO Auto-generated method stub
		return selectOne("getSchedule", param);
	}
 
	
    // 获取最近几次的考试
	@Override
	public List<JSONObject> getRecentExams(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<JSONObject> list = selectList(MYBATIS_PREFIX + "getRecentExams", splitDbAndTableRule);
		if(list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}
	
	// 获取是  班级  或者学生 是否有考试
	@Override
	public Integer getScoreinfoCnt(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		Integer cnt = selectOne(MYBATIS_PREFIX + "getScoreinfoCnt", splitDbAndTableRule);
		return cnt;
	}

	// 获取班级最近考试数据
	@Override
	public JSONObject getRecentClassExamInfo(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		return selectOne(MYBATIS_PREFIX + "getRecentClassExamInfo", splitDbAndTableRule);
	}
	
	// 获取学生最近考试数据
	@Override
	public List<JSONObject> getRecentStuExamInfo(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<JSONObject> list = selectList(MYBATIS_PREFIX + "getRecentStuExamInfo", splitDbAndTableRule);
		if(list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}
   //获取学生考试排名
	@Override
	public JSONObject getStudentStatisticsrank(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		JSONObject result = selectOne(MYBATIS_PREFIX + "getStudentStatisticsrank", splitDbAndTableRule);
		return result;
	}
     //获取班级历年科目满分 -s
	@Override
	public List<JSONObject> getExamListBynj(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		return selectList(MYBATIS_PREFIX + "getExamListBynj" , splitDbAndTableRule);
	}

	@Override
	public List<JSONObject> getBmfzByClassId(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		return selectList(MYBATIS_PREFIX + "getBmfzByClassId", splitDbAndTableRule);
	}

	@Override
	public List<JSONObject> getSubjectfullScoreByfzdm(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		return selectList(MYBATIS_PREFIX + "getSubjectfullScoreByfzdm", splitDbAndTableRule);
	}
	 //获取班级历年科目满分 -e

	//获取历年科目平均分
	@Override
	public List<JSONObject> getAllHisSubjectAverageScore(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<JSONObject> list = selectList(MYBATIS_PREFIX + "getAllHisSubjectAverageScore", splitDbAndTableRule);
		return list;
	}

	@Override
	public List<JSONObject> getExamClassReportList(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<JSONObject> list = selectList(MYBATIS_PREFIX + "getExamClassReportList", splitDbAndTableRule);
		return list;
	}

}
