package com.talkweb.scoreManage.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.scoreManage.po.gm.CompetitionStu;
import com.talkweb.scoreManage.po.gm.Dbkslc;
import com.talkweb.scoreManage.po.gm.DegreeInfo;
import com.talkweb.scoreManage.po.gm.SynthScore;
import com.talkweb.scoreManage.po.sar.SettingBJ;

/**
 * @ClassName ScoreAnalysisDao
 * @author Homer
 * @version 1.0
 * @Description 成绩分析接口
 * @date 2015年3月26日
 */
public interface ScoreAnalysisDao {

	/**
	 * 获取总分及总分分数段规则设置
	 * 
	 * @param map
	 * @return
	 */
	List<JSONObject> getZfAndSz(String termInfoId, Integer autoIncr, Map<String, Object> map);

	List<JSONObject> getDkMf(String termInfoId, Integer autoIncr, Map<String, Object> map);

	List<CompetitionStu> getXsjsCjList(String termInfoId, Integer autoIncr, Map<String, Object> map);

	List<SynthScore> getSynthScoreList(String termInfoId, Integer autoIncr, Map<String, Object> map);

	List<JSONObject> getAllKmInLcForDD(String termInfoId, Integer autoIncr, Map<String, Object> map);

	List<JSONObject> getTjKmInWl(String termInfoId, Integer autoIncr, Map<String, Object> map);

	List<Long> getBcytjXhList(String termInfoId, Integer autoIncr, Map<String, Object> map);

	/**
	 * 获取统计条件参数 如舞弊、缺考等
	 * 
	 * @param xxdm
	 *            学校代码
	 * @return
	 */
	List<JSONObject> getCondionParam(Map<String, Object> map);

	List<JSONObject> getAllDdsz(String termInfoId, Integer autoIncr, Map<String, Object> map);

	/**
	 * 获取优秀率合格率、低分率设置参数
	 * 
	 * @param xxdm
	 *            学校代码
	 * @return
	 */
	List<JSONObject> getSectionParam(Map<String, Object> map);

	List<JSONObject> getWlfzAndBjfz(String termInfoId, Integer autoIncr, Map<String, Object> map);

	List<JSONObject> getKmInWlfz(String termInfoId, Integer autoIncr, Map<String, Object> map);

	/**
	 * 获取排名区间
	 * 
	 * @param xxdm
	 *            学校代码
	 * @param kslc
	 *            考试轮次
	 * @return
	 */
	List<JSONObject> getScoreRankDistribute(String termInfoId, Integer autoIncr, Map<String, Object> map);

	/**
	 * 获取对比考试伦次
	 * 
	 * @param xxdm
	 *            学校代码
	 * @param kslc
	 *            考试轮次
	 * @return
	 */
	List<Dbkslc> getDbkslc(String termInfoId, Integer autoIncr, Map<String, Object> map);

	/**
	 * 获取对比考试的学生总分排名
	 * 
	 * @param kslc
	 *            对比的考试轮次
	 * @return
	 */
	List<JSONObject> getAllScoreRank(String termInfoId, Integer autoIncr, Map<String, Object> map);

	/**
	 * 获取对比考试的学生单科排名
	 * 
	 * @param kslc
	 *            对比的考试轮次
	 * @return
	 */
	List<JSONObject> getSingleSubjectRank(String termInfoId, Integer autoIncr, Map<String, Object> map);

	List<JSONObject> getOldLcClassStatic(String termInfoId, Integer autoIncr, Map<String, Object> map);

	List<JSONObject> getOldLcClassKmStatic(String termInfoId, Integer autoIncr, Map<String, Object> map);

	List<SettingBJ> getSarSetBJList(Map<String, Object> map);

	List<DegreeInfo> getBjReportGzKslcList(List<String> xnxqList, Date cdate, JSONObject params);

	List<JSONObject> getBjBzfGz(String termInfoId, Integer autoIncr, Map<String, Object> map);

	List<JSONObject> getBjBzfGzKm(String termInfoId, Integer autoIncr, Map<String, Object> map);

	List<JSONObject> getSarSetStuList(Map<String, Object> map);

	List<JSONObject> getFwszList(String termInfoId, Integer autoIncr, Map<String, Object> map);

	/**
	 * 获取本次考试学生的单科成绩以及合并科目成绩
	 * 
	 * @param xxdm
	 *            学校代码
	 * @param kslc
	 *            考试轮次
	 * @param xnxq
	 * @return
	 */
	List<JSONObject> getSingleSubjectScore(String termInfoId, Integer autoIncr, Map<String, Object> map);

	/**
	 * 获取本次考试的所有班级
	 * 
	 * @param xxdm
	 *            学校代码
	 * @param kslc
	 *            考试轮次
	 * @return
	 */
	List<JSONObject> getAllExamClass(String termInfoId, Integer autoIncr, Map<String, Object> map);

	void deleteOldExamAlzRs(String termInfoId, Integer autoIncr, Map<String, Object> map);

	int insertScoreLevelSequnceBatch(String termInfoId, Integer autoIncr, List<?> list);

	int insertScoreDistributeBatch(String termInfoId, Integer autoIncr, List<?> list);

	int insertZfmfqjBatch(String termInfoId, Integer autoIncr, List<?> list);

	int insertKmmfqjBatch(String termInfoId, Integer autoIncr, List<?> list);

	int insertScoreDistributeKmBatch(String termInfoId, Integer autoIncr, List<?> list);

	int insertScoreStuStatisticsRankBatch(String termInfoId, Integer autoIncr, List<?> list);

	int insertScoreStuStatisticsRankMkBatch(String termInfoId, Integer autoIncr, List<?> list);

	int insertScoreClassStatisticsBatch(String termInfoId, Integer autoIncr, List<?> list);

	int insertScoreClassStatisticsMkBatch(String termInfoId, Integer autoIncr, List<?> list);

	int insertScoreClassDistributeBatch(String termInfoId, Integer autoIncr, List<?> list);

	int insertScoreClassDistributeMkBatch(String termInfoId, Integer autoIncr, List<?> list);

	int insertScoreRankStatisticsBatch(String termInfoId, Integer autoIncr, List<?> list);

	int insertScoreRankStatisticsMk(String termInfoId, Integer autoIncr, List<?> list);

	int insertScoreGroupStatisticsMkBatch(String termInfoId, Integer autoIncr, List<?> list);

	int insertScoreGroupStatisticsBatch(String termInfoId, Integer autoIncr, List<?> list);

	int insertScoreStuBzfBatch(String termInfoId, Integer autoIncr, List<?> list);

	int insertScoreStuBzfMkBatch(String termInfoId, Integer autoIncr, List<?> list);

	int insertScoreStuJzsqnsBatch(String termInfoId, Integer autoIncr, List<?> list);

	int insertScoreStuJzsqnsMkBatch(String termInfoId, Integer autoIncr, List<?> list);

	int insertScoreClassStatisticsRangeBatch(String termInfoId, Integer autoIncr, List<?> list);

	int insertScoreGroupStatisticsMkRange(String termInfoId, Integer autoIncr, List<?> list);

	int insertScoreGroupStatisticsRangeBatch(String termInfoId, Integer autoIncr, List<?> list);

	int insertClassScoreLevelMkBatch(String termInfoId, Integer autoIncr, List<?> list);

	int insertGroupScoreLevelMkBatch(String termInfoId, Integer autoIncr, List<?> list);

	int insertClassScoreLevelSequnceBatch(String termInfoId, Integer autoIncr, List<?> list);

	int insertScoreClassStatisticsMkRange(String termInfoId, Integer autoIncr, List<?> list);

	int insertAnalysisReportStuBatch(String termInfoId, Integer autoIncr, List<?> list);

	int insertAnalysisReportNjBatch(String termInfoId, Integer autoIncr, List<?> list);

	int insertAnalysisReportBjBatch(String termInfoId, Integer autoIncr, List<?> list);

	int insertCompetitionStuStatisticsBatch(String termInfoId, Integer autoIncr, List<?> list);

	int insertAppStudentScoreReportBatch(String termInfoId, Integer autoIncr, List<?> list);

}
