package com.talkweb.scoreManage.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.base.dao.impl.MyBatisBaseDaoImpl;
import com.talkweb.common.splitDbAndTable.SplitDbAndTableRule;
import com.talkweb.common.splitDbAndTable.SplitDbAndTableRuleFactory;
import com.talkweb.scoreManage.dao.ScoreAnalysisDao;
import com.talkweb.scoreManage.po.gm.CompetitionStu;
import com.talkweb.scoreManage.po.gm.Dbkslc;
import com.talkweb.scoreManage.po.gm.DegreeInfo;
import com.talkweb.scoreManage.po.gm.SynthScore;
import com.talkweb.scoreManage.po.sar.SettingBJ;

/**
 * @ClassName ScoreAnalysisDaoImpl
 * @author Homer
 * @version 1.0
 * @Description 成绩分析接口实现类
 * @date 2015年3月26日
 */
@Repository
public class ScoreAnalysisDaoImpl extends MyBatisBaseDaoImpl implements ScoreAnalysisDao {
	private static final String ANALYSIS_MYBATIS_PREFIX = "com.talkweb.scoreManage.dao.ScoreAnalysisDao.";
	private static final String MAIN_MYBATIS_PREFIX = "com.talkweb.scoreManage.dao.ScoreManageDao.";

	@Autowired
	private SplitDbAndTableRuleFactory splitDbAndTableRuleFactory;

	private static final int INSERT_LIMIT = 2000;

	private int insertBatch(List<?> list, SplitDbAndTableRule splitDbAndTableRule, String key) {
		int result = 0;
		// 分批插入，因为mysql设置了max_allowed_packet选项，超过了就丢掉部分数据，分批插入也减少数据库的压力
		int batchSize = list.size() % INSERT_LIMIT == 0 ? list.size() / INSERT_LIMIT - 1 : list.size() / INSERT_LIMIT;
		for (int batch = 0; batch <= batchSize; batch++) {
			int fromIndex = batch * INSERT_LIMIT;
			int toIndex = fromIndex + INSERT_LIMIT;
			toIndex = toIndex > list.size() ? list.size() : toIndex;
			splitDbAndTableRule.setData(list.subList(fromIndex, toIndex));
			result += insert(ANALYSIS_MYBATIS_PREFIX + key, splitDbAndTableRule);
		}
		return result;
	}

	@Override
	public List<JSONObject> getZfAndSz(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<JSONObject> list = selectList(ANALYSIS_MYBATIS_PREFIX + "getZfAndSz", splitDbAndTableRule);
		if (list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}

	@Override
	public List<JSONObject> getDkMf(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<JSONObject> list = selectList(ANALYSIS_MYBATIS_PREFIX + "getDkMf", splitDbAndTableRule);
		if (list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}

	@Override
	public List<CompetitionStu> getXsjsCjList(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<CompetitionStu> list = selectList(ANALYSIS_MYBATIS_PREFIX + "getXsjsCjList", splitDbAndTableRule);
		if (list == null) {
			return new ArrayList<CompetitionStu>();
		}
		return list;
	}

	@Override
	public List<SynthScore> getSynthScoreList(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<SynthScore> list = selectList(ANALYSIS_MYBATIS_PREFIX + "getSynthScoreListF", splitDbAndTableRule);
		if (list == null) {
			return new ArrayList<SynthScore>();
		}
		return list;
	}

	@Override
	public List<JSONObject> getAllKmInLcForDD(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<JSONObject> list = selectList(ANALYSIS_MYBATIS_PREFIX + "getAllKmInLcForDD", splitDbAndTableRule);
		if (list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}

	@Override
	public List<JSONObject> getTjKmInWl(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<JSONObject> list = selectList(ANALYSIS_MYBATIS_PREFIX + "getTjKmInWl", splitDbAndTableRule);
		if (list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}

	@Override
	public List<Long> getBcytjXhList(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<Long> list = selectList(ANALYSIS_MYBATIS_PREFIX + "getBcytjXhList", splitDbAndTableRule);
		if (list == null) {
			return new ArrayList<Long>();
		}
		return list;
	}

	@Override
	public List<JSONObject> getCondionParam(Map<String, Object> map) {
		List<JSONObject> list = selectList(ANALYSIS_MYBATIS_PREFIX + "getCondionParam", map);
		if (list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}

	@Override
	public List<JSONObject> getAllDdsz(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<JSONObject> list = selectList(ANALYSIS_MYBATIS_PREFIX + "getAllDdsz", splitDbAndTableRule);
		if (list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}

	@Override
	public List<JSONObject> getSectionParam(Map<String, Object> map) {
		List<JSONObject> list = selectList(ANALYSIS_MYBATIS_PREFIX + "getSectionParam", map);
		if (list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}

	@Override
	public List<JSONObject> getWlfzAndBjfz(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<JSONObject> list = selectList(ANALYSIS_MYBATIS_PREFIX + "getWlfzAndBjfz", splitDbAndTableRule);
		if (list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}

	@Override
	public List<JSONObject> getKmInWlfz(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<JSONObject> list = selectList(ANALYSIS_MYBATIS_PREFIX + "getKmInWlfz", splitDbAndTableRule);
		if (list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}

	@Override
	public List<JSONObject> getScoreRankDistribute(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<JSONObject> list = selectList(ANALYSIS_MYBATIS_PREFIX + "getScoreRankDistribute", splitDbAndTableRule);
		if (list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}

	@Override
	public List<Dbkslc> getDbkslc(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<Dbkslc> list = selectList(ANALYSIS_MYBATIS_PREFIX + "getDbKslc", splitDbAndTableRule);
		if (list == null) {
			return new ArrayList<Dbkslc>();
		}
		return list;
	}

	@Override
	public List<JSONObject> getAllScoreRank(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<JSONObject> list = selectList(ANALYSIS_MYBATIS_PREFIX + "getAllScoreRank", splitDbAndTableRule);
		if (list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}

	@Override
	public List<JSONObject> getSingleSubjectRank(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<JSONObject> list = selectList(ANALYSIS_MYBATIS_PREFIX + "getSingleSubjectRank", splitDbAndTableRule);
		if (list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}

	@Override
	public List<JSONObject> getOldLcClassStatic(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<JSONObject> list = selectList(ANALYSIS_MYBATIS_PREFIX + "getOldLcClassStatic", splitDbAndTableRule);
		if (list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}

	@Override
	public List<JSONObject> getOldLcClassKmStatic(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<JSONObject> list = selectList(ANALYSIS_MYBATIS_PREFIX + "getOldLcClassKmStatic", splitDbAndTableRule);
		if (list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}

	@Override
	public List<SettingBJ> getSarSetBJList(Map<String, Object> map) {
		List<SettingBJ> list = selectList(ANALYSIS_MYBATIS_PREFIX + "getSarSetBJList", map);
		if (list == null) {
			return new ArrayList<SettingBJ>();
		}
		return list;
	}

	@Override
	public List<DegreeInfo> getBjReportGzKslcList(List<String> xnxqList, Date cdate, JSONObject params) {
		List<DegreeInfo> list = new ArrayList<DegreeInfo>();

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("ltCdate", cdate);
		map.put("xxdm", params.get("xxdm"));
		map.put("relatedBj", params.get("relatedBj"));

		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule("null", null, map);

		int limit = 10; // 只需要查询最近10次的数据
		for (String xnxq : xnxqList) {
			map.put("xnxq", xnxq);

			splitDbAndTableRule.setTermInfoId(xnxq);
			splitDbAndTableRule.setAutoIncr(null);

			List<DegreeInfo> degreeInfoList = selectList(MAIN_MYBATIS_PREFIX + "getDegreeInfoList",
					splitDbAndTableRule);

			for (DegreeInfo degreeInfo : degreeInfoList) {
				splitDbAndTableRule.setAutoIncr(degreeInfo.getAutoIncr());
				map.put("kslc", degreeInfo.getKslcdm());

				Integer result = selectOne(ANALYSIS_MYBATIS_PREFIX + "ifExistsDataFromScoreClassStatistics",
						splitDbAndTableRule);
				if (result != null && result == 1) {
					list.add(degreeInfo);
					limit--;
					if (limit <= 0) {
						return list;
					}
				}
			}
		}
		return list;
	}

	@Override
	public List<JSONObject> getBjBzfGz(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<JSONObject> list = selectList(ANALYSIS_MYBATIS_PREFIX + "getBjBzfGz", splitDbAndTableRule);
		if (list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}

	@Override
	public List<JSONObject> getBjBzfGzKm(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<JSONObject> list = selectList(ANALYSIS_MYBATIS_PREFIX + "getBjBzfGzKm", splitDbAndTableRule);
		if (list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}

	@Override
	public List<JSONObject> getSarSetStuList(Map<String, Object> map) {
		List<JSONObject> list = selectList(ANALYSIS_MYBATIS_PREFIX + "getSarSetStuList", map);
		if (list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}

	@Override
	public List<JSONObject> getFwszList(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<JSONObject> list = selectList(ANALYSIS_MYBATIS_PREFIX + "getFwszList", splitDbAndTableRule);
		if (list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}

	@Override
	public List<JSONObject> getSingleSubjectScore(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<JSONObject> list = selectList(ANALYSIS_MYBATIS_PREFIX + "getSingleSubjectScore", splitDbAndTableRule);
		if (list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}

	@Override
	public List<JSONObject> getAllExamClass(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<JSONObject> list = selectList(ANALYSIS_MYBATIS_PREFIX + "getAllExamClass", splitDbAndTableRule);
		if (list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}

	@Override
	public void deleteOldExamAlzRs(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);

		delete(ANALYSIS_MYBATIS_PREFIX + "deleteScoreStuStatisticsRank", splitDbAndTableRule);
		delete(ANALYSIS_MYBATIS_PREFIX + "deleteScoreStuStatisticsRankMk", splitDbAndTableRule);
		delete(ANALYSIS_MYBATIS_PREFIX + "deleteScoreClassStatistics", splitDbAndTableRule);
		delete(ANALYSIS_MYBATIS_PREFIX + "deleteScoreClassStatisticsMk", splitDbAndTableRule);
		delete(ANALYSIS_MYBATIS_PREFIX + "deleteScoreClassDistribute", splitDbAndTableRule);
		delete(ANALYSIS_MYBATIS_PREFIX + "deleteScoreDistribute", splitDbAndTableRule);
		delete(ANALYSIS_MYBATIS_PREFIX + "deleteScoreDistributeKm", splitDbAndTableRule);
		delete(ANALYSIS_MYBATIS_PREFIX + "deleteScoreClassDistributeMk", splitDbAndTableRule);
		delete(ANALYSIS_MYBATIS_PREFIX + "deleteScoreRankStatistics", splitDbAndTableRule);
		delete(ANALYSIS_MYBATIS_PREFIX + "deleteScoreRankStatisticsMk", splitDbAndTableRule);
		delete(ANALYSIS_MYBATIS_PREFIX + "deleteScoreGroupStatisticsMk", splitDbAndTableRule);
		delete(ANALYSIS_MYBATIS_PREFIX + "deleteScoreGroupStatistics", splitDbAndTableRule);
		delete(ANALYSIS_MYBATIS_PREFIX + "deleteScoreStuBzf", splitDbAndTableRule);
		delete(ANALYSIS_MYBATIS_PREFIX + "deleteScoreStuBzfMk", splitDbAndTableRule);
		delete(ANALYSIS_MYBATIS_PREFIX + "deleteScoreStuJzsqns", splitDbAndTableRule);
		delete(ANALYSIS_MYBATIS_PREFIX + "deleteScoreStuJzsqnsMk", splitDbAndTableRule);
		delete(ANALYSIS_MYBATIS_PREFIX + "deleteScoreClassStatisticsRange", splitDbAndTableRule);
		delete(ANALYSIS_MYBATIS_PREFIX + "deleteScoreClassStatisticsMkRange", splitDbAndTableRule);
		delete(ANALYSIS_MYBATIS_PREFIX + "deleteScoreGroupStatisticsMkRange", splitDbAndTableRule);
		delete(ANALYSIS_MYBATIS_PREFIX + "deleteScoreGroupStatisticsRange", splitDbAndTableRule);
		delete(ANALYSIS_MYBATIS_PREFIX + "deleteClassScoreLevelMk", splitDbAndTableRule);
		delete(ANALYSIS_MYBATIS_PREFIX + "deleteGroupScoreLevelMk", splitDbAndTableRule);
		delete(ANALYSIS_MYBATIS_PREFIX + "deleteClassScoreLevelSequnce", splitDbAndTableRule);
		delete(ANALYSIS_MYBATIS_PREFIX + "deleteScoreLevelSequnce", splitDbAndTableRule);
		delete(ANALYSIS_MYBATIS_PREFIX + "deleteAnalysisReportNj", splitDbAndTableRule);
		delete(ANALYSIS_MYBATIS_PREFIX + "deleteAnalysisReportBj", splitDbAndTableRule);
		delete(ANALYSIS_MYBATIS_PREFIX + "deleteAnalysisReportStu", splitDbAndTableRule);
		delete(ANALYSIS_MYBATIS_PREFIX + "deleteCompetitionStuStatistics", splitDbAndTableRule);
		delete(ANALYSIS_MYBATIS_PREFIX + "deleteAppStudentScoreReport", splitDbAndTableRule);
	}

	@Override
	public int insertScoreLevelSequnceBatch(String termInfoId, Integer autoIncr, List<?> list) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, null);
		return insertBatch(list, splitDbAndTableRule, "insertScoreLevelSequnceBatch");
	}

	@Override
	public int insertScoreDistributeBatch(String termInfoId, Integer autoIncr, List<?> list) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, null);
		return insertBatch(list, splitDbAndTableRule, "insertScoreDistributeBatch");
	}

	@Override
	public int insertZfmfqjBatch(String termInfoId, Integer autoIncr, List<?> list) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, null);
		return insertBatch(list, splitDbAndTableRule, "insertZfmfqjBatch");
	}

	@Override
	public int insertKmmfqjBatch(String termInfoId, Integer autoIncr, List<?> list) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, null);
		return insertBatch(list, splitDbAndTableRule, "insertKmmfqjBatch");
	}

	@Override
	public int insertScoreDistributeKmBatch(String termInfoId, Integer autoIncr, List<?> list) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, null);
		return insertBatch(list, splitDbAndTableRule, "insertScoreDistributeKmBatch");
	}

	@Override
	public int insertScoreStuStatisticsRankBatch(String termInfoId, Integer autoIncr, List<?> list) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, null);
		return insertBatch(list, splitDbAndTableRule, "insertScoreStuStatisticsRankBatch");
	}

	@Override
	public int insertScoreStuStatisticsRankMkBatch(String termInfoId, Integer autoIncr, List<?> list) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, null);
		return insertBatch(list, splitDbAndTableRule, "insertScoreStuStatisticsRankMkBatch");
	}

	@Override
	public int insertScoreClassStatisticsBatch(String termInfoId, Integer autoIncr, List<?> list) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, null);
		return insertBatch(list, splitDbAndTableRule, "insertScoreClassStatisticsBatch");
	}

	@Override
	public int insertScoreClassStatisticsMkBatch(String termInfoId, Integer autoIncr, List<?> list) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, null);
		return insertBatch(list, splitDbAndTableRule, "insertScoreClassStatisticsMkBatch");
	}

	@Override
	public int insertScoreClassDistributeBatch(String termInfoId, Integer autoIncr, List<?> list) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, null);
		return insertBatch(list, splitDbAndTableRule, "insertScoreClassDistributeBatch");
	}

	@Override
	public int insertScoreClassDistributeMkBatch(String termInfoId, Integer autoIncr, List<?> list) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, null);
		return insertBatch(list, splitDbAndTableRule, "insertScoreClassDistributeMkBatch");
	}

	@Override
	public int insertScoreRankStatisticsBatch(String termInfoId, Integer autoIncr, List<?> list) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, null);
		return insertBatch(list, splitDbAndTableRule, "insertScoreRankStatisticsBatch");
	}

	@Override
	public int insertScoreRankStatisticsMk(String termInfoId, Integer autoIncr, List<?> list) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, null);
		return insertBatch(list, splitDbAndTableRule, "insertScoreRankStatisticsMk");
	}

	@Override
	public int insertScoreGroupStatisticsMkBatch(String termInfoId, Integer autoIncr, List<?> list) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, null);
		return insertBatch(list, splitDbAndTableRule, "insertScoreGroupStatisticsMkBatch");
	}

	@Override
	public int insertScoreGroupStatisticsBatch(String termInfoId, Integer autoIncr, List<?> list) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, null);
		return insertBatch(list, splitDbAndTableRule, "insertScoreGroupStatisticsBatch");
	}

	@Override
	public int insertScoreStuBzfBatch(String termInfoId, Integer autoIncr, List<?> list) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, null);
		return insertBatch(list, splitDbAndTableRule, "insertScoreStuBzfBatch");
	}

	@Override
	public int insertScoreStuBzfMkBatch(String termInfoId, Integer autoIncr, List<?> list) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, null);
		return insertBatch(list, splitDbAndTableRule, "insertScoreStuBzfMkBatch");
	}

	@Override
	public int insertScoreStuJzsqnsBatch(String termInfoId, Integer autoIncr, List<?> list) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, null);
		return insertBatch(list, splitDbAndTableRule, "insertScoreStuJzsqnsBatch");
	}

	@Override
	public int insertScoreStuJzsqnsMkBatch(String termInfoId, Integer autoIncr, List<?> list) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, null);
		return insertBatch(list, splitDbAndTableRule, "insertScoreStuJzsqnsMkBatch");
	}

	@Override
	public int insertScoreClassStatisticsRangeBatch(String termInfoId, Integer autoIncr, List<?> list) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, null);
		return insertBatch(list, splitDbAndTableRule, "insertScoreClassStatisticsRangeBatch");
	}

	@Override
	public int insertScoreGroupStatisticsMkRange(String termInfoId, Integer autoIncr, List<?> list) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, null);
		return insertBatch(list, splitDbAndTableRule, "insertScoreGroupStatisticsMkRange");
	}

	@Override
	public int insertScoreGroupStatisticsRangeBatch(String termInfoId, Integer autoIncr, List<?> list) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, null);
		return insertBatch(list, splitDbAndTableRule, "insertScoreGroupStatisticsRangeBatch");
	}

	@Override
	public int insertClassScoreLevelMkBatch(String termInfoId, Integer autoIncr, List<?> list) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, null);
		return insertBatch(list, splitDbAndTableRule, "insertClassScoreLevelMkBatch");
	}

	@Override
	public int insertGroupScoreLevelMkBatch(String termInfoId, Integer autoIncr, List<?> list) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, null);
		return insertBatch(list, splitDbAndTableRule, "insertGroupScoreLevelMkBatch");
	}

	@Override
	public int insertClassScoreLevelSequnceBatch(String termInfoId, Integer autoIncr, List<?> list) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, null);
		return insertBatch(list, splitDbAndTableRule, "insertClassScoreLevelSequnceBatch");
	}

	@Override
	public int insertScoreClassStatisticsMkRange(String termInfoId, Integer autoIncr, List<?> list) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, null);
		return insertBatch(list, splitDbAndTableRule, "insertScoreClassStatisticsMkRange");
	}

	@Override
	public int insertAnalysisReportStuBatch(String termInfoId, Integer autoIncr, List<?> list) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, null);
		return insertBatch(list, splitDbAndTableRule, "insertAnalysisReportStuBatch");
	}

	@Override
	public int insertAnalysisReportNjBatch(String termInfoId, Integer autoIncr, List<?> list) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, null);
		return insertBatch(list, splitDbAndTableRule, "insertAnalysisReportNjBatch");
	}

	@Override
	public int insertAnalysisReportBjBatch(String termInfoId, Integer autoIncr, List<?> list) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, null);
		return insertBatch(list, splitDbAndTableRule, "insertAnalysisReportBjBatch");
	}

	@Override
	public int insertCompetitionStuStatisticsBatch(String termInfoId, Integer autoIncr, List<?> list) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, null);
		return insertBatch(list, splitDbAndTableRule, "insertCompetitionStuStatisticsBatch");
	}

	@Override
	public int insertAppStudentScoreReportBatch(String termInfoId, Integer autoIncr, List<?> list) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, null);
		return insertBatch(list, splitDbAndTableRule, "insertAppStudentScoreReportBatch");
	}
}
