package com.talkweb.scoreManage.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.scoreManage.po.gm.CompetitionStu;
import com.talkweb.scoreManage.po.gm.Dbkslc;
import com.talkweb.scoreManage.po.gm.DegreeInfo;
import com.talkweb.scoreManage.po.gm.DegreeInfoNj;
import com.talkweb.scoreManage.po.gm.JXPGScoreSection;
import com.talkweb.scoreManage.po.gm.ScoreInfo;
import com.talkweb.scoreManage.po.gm.ScoreLevelTemplate;
import com.talkweb.scoreManage.po.gm.ScoreRankDistribute;
import com.talkweb.scoreManage.po.gm.ScoreStatTerm;
import com.talkweb.scoreManage.po.gm.ScoreStuStatisticsRankMk;
import com.talkweb.scoreManage.po.gm.SynthScore;
import com.talkweb.scoreManage.po.gm.TopGroup;
import com.talkweb.scoreManage.po.gm.TopGroupBj;
import com.talkweb.scoreManage.po.gm.Zfqjsz;
import com.talkweb.scoreManage.po.sar.SettingBJ;
import com.talkweb.scoreManage.po.sar.SettingStu;

public interface ScoreManageDao {
	List<DegreeInfo> getDegreeInfoList(String termInfo, Map<String, Object> map);

	DegreeInfo getDegreeInfoById(String termInfo, Map<String, Object> map);

	List<DegreeInfoNj> getDegreeInfoNjList(String termInfo, Map<String, Object> map);

	boolean hasSameNameInDegreeInfo(String termInfo, DegreeInfo degreeInfo);

	int insertDegreeInfo(String termInfo, DegreeInfo degreeInfo);

	List<String> getNjFromDegreeInfoNj(String termInfo, Map<String, Object> map);

	int insertDegreeInfoNjBatch(String termInfo, List<DegreeInfoNj> list);

	int updatetDegreeInfo(String termInfo, Object degreeInfo);

	int deleteDegreeInfo(String termInfo, Map<String, Object> map);

	int deleteDegreeInfoNj(String termInfo, Map<String, Object> map);

	int updateDegreeInfoNj(String termInfo, Map<String, Object> map);

	List<ScoreInfo> getScoreInfo(String termInfo, Integer autoIncr, Map<String, Object> map);

	int insertScoreInfoBatch(String termInfo, Integer autoIncr, List<ScoreInfo> list);

	int deleteScoreInfo(String termInfo, Integer autoIncr, Map<String, Object> map);

	List<String> getNjByScoreInfo(String termInfo, Integer autoIncr, Map<String, Object> map);

	int updateScoreInfo(String termInfo, Integer autoIncr, Object obj);

	List<Long> getTeacherIdFromScoreInfo(String termInfoId, Integer autoIncr, Map<String, Object> map);

	List<Long> getStudentIdFromScoreInfo(String termInfoId, Integer autoIncr, Map<String, Object> map);

	List<JSONObject> getScoreReleaseList(String termInfoId, Map<String, Object> map);

	List<String> getFzdmFromZfqjsz(String termInfoId, Integer autoIncr, Map<String, Object> map);

	List<String> getFzdmFromScoreRankDistribute(String termInfoId, Integer autoIncr, Map<String, Object> map);

	boolean JudeEqualSet(Set<String> lxs, String lxms);

	List<JSONObject> getBjfzList(String termInfo, Integer autoIncr, Map<String, Object> map);
	
	List<Dbkslc> getPrevKslcList(Date cdate, JSONObject params, Set<Long> classIds);
	
	DegreeInfo getOldKslcAndWlfz(Date cdate, JSONObject params, Set<String> dms);
	
	int insertScoreRankDistributeBatch(String termInfoId, Integer autoIncr, List<?> list);
	
	int insertDbkslcBatch(String termInfoId, Integer autoIncr, List<Dbkslc> list);

	List<String> getExamAnalysisConfig(Map<String, Object> map);
	
	boolean ifExistsJXPGScoreSection(Map<String, Object> map);
	
	int insertJXPGScoreSectionBatch(List<?> list);
	
	int initScoreStatTerm(String xxdm);
	
	boolean ifExistsSettingBj(String xxdm);
	
	int insertClassReportParamBatch(List<SettingBJ> list);
	
	boolean ifExistsSettingStu(String xxdm);
	
	int insertStudentReportParamBatch(List<SettingStu> list);

	List<JSONObject> getExamDefaultSetting(JSONObject params, Integer autoIncr, Set<String> setItemSet);
	
	boolean ifExistsAnalysisConfig01(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	boolean ifExistsAnalysisConfig02(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	boolean ifExistsAnalysisConfig03(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	boolean ifExistsAnalysisConfig04(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	boolean ifExistsAnalysisConfig05(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	boolean ifExistsAnalysisConfig06(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	boolean ifExistsAnalysisConfig07(Map<String, Object> map);
	
	boolean ifExistsAnalysisConfig08(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	boolean ifExistsAnalysisConfig09(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	boolean ifExistsAnalysisConfig10(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	boolean ifExistsAnalysisConfig11(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	boolean ifExistsAnalysisConfig12(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	boolean ifExistsAnalysisConfig13(Map<String, Object> map);
	
	boolean ifExistsAnalysisConfig14(Map<String, Object> map);
	
	boolean ifExistsSameTopGroup(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	int insertTopGroupBatch(String termInfoId, Integer autoIncr, List<?> list);
	
	int insertTopGroupBjBatch(String termInfoId, Integer autoIncr, List<?> list);
	
	List<ScoreRankDistribute> getRankListByPrevKslc(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	List<CompetitionStu> getCompetitionStu(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	int insertCompetitionStuBatch(String termInfoId, Integer autoIncr, List<?> list);
	
	List<SynthScore> getSynthScoreByPrevKslc(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	int insertSynthScoreBatch(String termInfoId, Integer autoIncr, List<?> list);
	
	List<ScoreLevelTemplate> getScoreLevelTemplate(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	List<JSONObject> getCjListForDdSet(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	int insertScoreLevelTemplateBatch(String termInfoId, Integer autoIncr, List<?> list);
	
	List<JSONObject> getTopGroupByPrevKslc(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	List<TopGroupBj> getTopGroupBj(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	List<JSONObject> getKmfzList(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	int insertScoreTjmkBatch(String termInfoId, Integer autoIncr, List<?> list);
	
	int insertScoreMfBatch(String termInfoId, Integer autoIncr, List<?> list);
	
	List<Zfqjsz> getZfqjsz(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	int insertZfqjszBatch(String termInfoId, Integer autoIncr, List<?> list);

	List<JSONObject> getwlkGroupList(String termInfo, Integer autoIncr, Map<String, Object> map);
	
	List<TopGroup> getTopGroup(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	List<String> getSsfzFromTopGroup(String termInfoId, Integer autoIncr, Map<String, Object> map);

	int deleteTopGroup(String termInfoId, Integer autoIncr, Map<String, Object> map);

	int deleteTopGroupBj(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	int updateTopGroup(String termInfoId, Integer autoIncr, Map<String, Object> map);

	List<Long> getWfzList(String termInfoId, Integer autoIncr, Map<String, Object> map);

	List<JSONObject> getYfzList(String termInfoId, Integer autoIncr, Map<String, Object> map);

	List<JSONObject> getASGListForStatic(String termInfoId, Integer autoIncr, Map<String, Object> map);

	int updateASGStatRules(String termInfoId, Integer autoIncr, JSONArray tjfz);

	List<JSONObject> getClassGroupListStatic(String termInfoId, Integer autoIncr, Map<String, Object> map);

	int updateClassGroupStatic(String termInfoId, Integer autoIncr, Map<String, Object> map);

	List<JSONObject> getSubjectStatusList(String termInfoId, Integer autoIncr, Map<String, Object> map);

	int deleteScoretjmk(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	int deleteScoreMF(String termInfoId, Integer autoIncr, Map<String, Object> map);

	List<JSONObject> getMergerSubjectList(String termInfoId, Integer autoIncr, Map<String, Object> map);

	int deleteSynthScore(String termInfoId, Integer autoIncr, Map<String, Object> map);

	int insertXscjbtjBatch(String termInfoId, Integer autoIncr, List<?> list);

	int deleteXscjbtj(String termInfoId, Integer autoIncr, Map<String, Object> map);

	void deleteAllExamAnalysisConfig(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	void delXSCJBTJ(String termInfoId, Integer autoIncr, Map<String, Object> map);

	List<JXPGScoreSection> getJxpgScoreSection(String xxdm);

	ScoreStatTerm getScoreStatTerm(String xxdm);

	int deleteStatisticalParameters(String xxdm);
	
	int insertScoreStatTerm(Map<String, Object> map);

	List<JSONObject> getIntervalList(String termInfoId, Integer autoIncr, Map<String, Object> map);

	int deleteScoreRankDistribute(String termInfoId, Integer autoIncr, Map<String, Object> map);

	JSONObject getTotalScoreSection(String termInfoId, Integer autoIncr, Map<String, Object> map);

	int insertZfqjsz(String termInfoId, Integer autoIncr, Object obj);
	
	List<Dbkslc> getDbkslcList(String termInfoId, Integer autoIncr, Map<String, Object> map);

	int deleteContrastExam(String termInfoId, Integer autoIncr, Map<String, Object> map);

	Set<Long> getBcytjXhList(String termInfoId, Integer autoIncr, Map<String, Object> map);

	List<Long> getSubjectIdFromScoreLevelTemplate(String termInfoId, Integer autoIncr, Map<String, Object> map);

	List<JSONObject> getRankingsByKMDM(String termInfoId, Integer autoIncr, Map<String, Object> map);

	int deleteScoreLevelTemplate(String termInfoId, Integer autoIncr, Map<String, Object> map);

	List<JSONObject> getAllDJList(String termInfoId, Integer autoIncr, Map<String, Object> map);

	List<SynthScore> getSynthScoreList(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	List<JSONObject> selectCjListForDdSet(String termInfoId, Integer autoIncr, Map<String, Object> map);

	List<Long> getSubjectIdsFromScoreInfo(String termInfoId, Integer autoIncr, Map<String, Object> map);

	/**
	 * 获取班级报告参数
	 * 
	 * @param map
	 * @return
	 */
	List<JSONObject> getClassReportParam(String xxdm);

	/**
	 * 获取学生报告参数
	 * 
	 * @param map
	 * @return
	 */
	List<SettingStu> getSettingStuList(String xxdm);

	int deleteCompetitionStud(String termInfoId, Integer autoIncr, Map<String, Object> map);

	List<Long> getExamSubjectIdList(String termInfo, Integer autoIncr, Map<String, Object> map);

	List<JSONObject> getExamNameList(String termInfo, Map<String, Object> map);

	List<Long> getClassIdListByGroupId(String termInfo, Integer autoIncr, Map<String, Object> map);

	List<JSONObject> getSubjectGroupNumById(String termInfoId, Integer autoIncr, Map<String, Object> map);

	boolean getAsgGroupSetFlag(String termInfoId, Integer autoIncr, Map<String, Object> map);

	boolean getClassGroupSetFlag(String termInfoId, Integer autoIncr, Map<String, Object> map);

	List<Long> getExamClassIdList(String termInfo, Integer autoIncr, Map<String, Object> map);
	
	
	List<JSONObject> getScoreStuBZF(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
	List<JSONObject> getStuScore(String termInfoId, Integer autoIncr, Map<String,Object> map);
	List<JSONObject> getStuTclassMap(Map<String,Object> obj);
	
	List<ScoreStuStatisticsRankMk> getStuSubjectScoreRank(String termInfoId, Integer autoIncr, Map<String,Object> map);
	
	List<JSONObject> get3Plus3Scores(String termInfo, Integer autoIncr, Map<String,Object> map);
	
	List<Long> getSynthscoreSubject(String termInfo, Integer autoIncr, Map<String,Object> map);
	
	JSONObject getDegreeinfoRelate(String termInfo, Integer autoIncr, Map<String,Object> map);
	int  deleteDegreeinfoRelate(String termInfo, Integer autoIncr, Map<String,Object> map);
	int  insertDegreeinfoRelate(String termInfo, Integer autoIncr, Map<String,Object> map);
	int  insertDegreeinfoError(String termInfo, Integer autoIncr, List<JSONObject> list);
	
	List<JSONObject> getExamClassList(String termInfoId, Integer autoIncr, Map<String, Object> map);
	
}
