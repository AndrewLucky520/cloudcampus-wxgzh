package com.talkweb.scoreManage.service;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.scoreManage.po.gm.CompetitionStu;
import com.talkweb.scoreManage.po.gm.DegreeInfo;
import com.talkweb.scoreManage.po.gm.ScoreInfo;
import com.talkweb.student.domain.page.StartImportTaskParam;

public interface ScoreManageService {
	
	DegreeInfo getDegreeInfoById(JSONObject params);
	
	/**
	 * 获取所有考试
	 * 
	 * @param map
	 * @return
	 */
	List<JSONObject> getDegreeInfoList(JSONObject params);

	/**
	 * 新增考试
	 * 
	 * @param params
	 */
	JSONObject createExam(JSONObject params);

	/**
	 * 更新考试名称
	 * 
	 * @param map
	 * @return
	 */
	int updatetExamName(JSONObject params);

	/**
	 * 删除考试
	 * 
	 * @param map
	 * @return
	 */
	void deleteExam(JSONObject map);

	/**
	 * 批量插入成绩表
	 * 
	 * @param scoreList
	 * @param taskParam
	 */
	void insertScoreInfoBatch(List<ScoreInfo> scoreList, StartImportTaskParam taskParam);

	/**
	 * 成绩列表
	 * 
	 * @param params
	 * @return
	 */
	JSONObject getScoreInfoList(JSONObject params);

	/**
	 * 更新成绩
	 * 
	 * @param params
	 * @return
	 */
	void updateScore(JSONObject params);

	/**
	 * 获取考试分析设置信息
	 * @param params
	 * @return
	 */
	JSONObject updateInitAndGetExamAnalysisSettingConfig(JSONObject params);
	
	/**
	 * 发布成绩列表
	 * 
	 * @param map
	 * @return
	 */
	List<JSONObject> getScoreReleaseList(JSONObject params);
	
	JSONObject updateScoreRelease(JSONObject params);
	

	/**
	 * 获取学生列表
	 * 
	 * @param needUpdate
	 * @return
	 */
	JSONObject getAllStuByParam(JSONObject params);

	/**
	 * 获取文理科分组
	 * 
	 * @param json
	 * @return
	 */
	List<JSONObject> getASGList(JSONObject params);
	
	void saveASG(JSONObject params);

	/**
	 * 获取班级分组
	 * 
	 * @param json
	 * @return
	 */
	List<JSONObject> getBjfzList(JSONObject params);


	JSONObject getClassGroupList(JSONObject params);

	/**
	 * 新增班级分组
	 * 
	 * @param map
	 * @return
	 */
	void addClassGroup(JSONObject params, List<String> classIds);

	/**
	 * 删除班级分组
	 * 
	 * @param map
	 * @return
	 */
	void delClassGroup(JSONObject params);

	/**
	 * 获取人数设置文理分组列表
	 * 
	 * @param map
	 * @return
	 */
	JSONObject getASGListForStatic(JSONObject params);

	/**
	 * 保存人数设置文理分组列表
	 * 
	 * @param map
	 * @return
	 */
	void updateASGStatRules(JSONObject params);

	/**
	 * 获取人数设置班级分组列表
	 * 
	 * @param map
	 * @return
	 */
	JSONObject getClassGroupListStatic(JSONObject params);
	
	JSONObject getStatStuList(JSONObject params);

	/**
	 * 保存人数设置班级分组列表
	 * 
	 * @param map
	 * @return
	 */
	void updateClassGroupStatic(JSONObject params);

	/**
	 * 保存不参与统计学生列表
	 * 
	 * @param map
	 * @return
	 */
	void addStatStuList(JSONObject params, List<String> studentIds);

	/**
	 * 删除不参与统计学生列表
	 * 
	 * @param map
	 * @return
	 */
	void delStatStuList(JSONObject params);

	/**
	 * 获取统计科目与满分列表
	 * 
	 * @param map
	 * @return
	 */
	JSONObject getStatSubjectList(JSONObject params);
	
	/**
	 * 获取混合科目
	 * @param params
	 * @return
	 */
	JSONObject getMergedSubjectList(JSONObject params);

	/**
	 * 保存统计科目与满分列表
	 * 
	 * @param list
	 * @return
	 */
	void updateStatSubjectList(JSONObject params);

	/**
	 * 新增合并科目
	 * 
	 * @param map
	 * @return
	 */
	void addMergerSubject(JSONObject params);

	/**
	 * 删除合并科目
	 * 
	 * @param map
	 * @return
	 */
	void delMergerSubject(JSONObject params);

	/**
	 * 获取统计参数-百分比
	 * 
	 * @param xxdm
	 * @return
	 */
	JSONObject getStatisticalParameters(String xxdm);

	/**
	 * 保存设置参数信息
	 * 
	 * @param map
	 * @return
	 */
	void updateStatisticalParameters(JSONObject params);

	/**
	 * 获取排名区间列表
	 * 
	 * @param map
	 * @return
	 */
	JSONObject getIntervalList(JSONObject params);

	void updateInterval(JSONObject params);

	/**
	 * 获取总分分数段设置
	 * 
	 * @param map
	 * @return
	 */
	JSONObject getTotalScoreSection(JSONObject params);

	/**
	 * 保存总分分数分段设置
	 * 
	 * @param map
	 * @return
	 */
	void updateTotalScoreSection(JSONObject params);

	/**
	 * 获取考试列表
	 * 
	 * @param map
	 * @return
	 */
	JSONObject getContrastExamList(JSONObject params);

	/**
	 * 保存排名区间列表
	 * 
	 * @param list
	 * @return
	 */
	void updateContrastExamList(JSONObject params);

	JSONObject getRankingsValueList(JSONObject params);

	/**
	 * 根据课程代码获取对应的等第设置
	 * 
	 * @param map
	 * @return
	 */
	List<JSONObject> getRankingsByKMDM(JSONObject params);

	/**
	 * 保存单科等级设置
	 * 
	 * @param list
	 * @return
	 */
	void updateBillDiviLevelSetting(JSONObject params);

	/**
	 * 删除等第设置
	 * 
	 * @param map
	 * @return
	 */
	void delBillDiviLevelSetting(JSONObject params);

	/**
	 * 生成等级
	 * 
	 * @param map
	 * @return
	 */
	void updateRankingsValue(JSONObject params);

	/**
	 * 获取单个科目的等级设置列表
	 * 
	 * @param map
	 * @return
	 */
	List<JSONObject> getSingleSubjectGrade(JSONObject params);

	/**
	 * 获取班级报告参数
	 * 
	 * @param xxdm
	 * @return
	 */
	JSONObject getClassReportParam(String xxdm);

	/**
	 * 保存班级报告参数
	 * 
	 * @param map
	 * @return
	 */
	void updateClassReportParam(JSONObject params);

	/**
	 * 获取学生报告参数
	 * 
	 * @param xxdm
	 * @return
	 */
	JSONObject getStudentReportParam(String xxdm);

	/**
	 * 保存学生报告参数
	 * 
	 * @param map
	 * @return
	 */
	void updateStudentReportParam(JSONObject params);

	List<JSONObject> getCompStdList(JSONObject params);

	void delCompStdListBatch(JSONObject params);

	void insertCompStuBatch(JSONObject params, List<CompetitionStu> list);

	List<Long> getExamSubjectIdList(JSONObject params);

	String updateHttpRemoteInterface(String url, Map<String, Object> param);

	List<Long> getClassIdListByGroupId(JSONObject params);

	int getStaticSelectedSetting(JSONObject param);
	
	/**
	 * 提供新高考志愿填报使用
	 * 参数：studentId，schoolId 返回map=key：examid value：list
	 * @param map
	 * @return
	 */
	Map<String, List<JSONObject>> getScoreStuBZF(Map<String, Object> map);
	
	JSONObject getAllScoreList(Map<String,Object> map);
	
	JSONObject getStuScoreDetail(Map<String,Object> map);
	
	List<Long> getSynthscoreSubject(Map<String,Object> map);
	
	JSONObject getDegreeinfoRelate(Map<String,Object> map);
	int  deleteDegreeinfoRelate( Map<String,Object> map);
	int  insertDegreeinfoRelate( Map<String,Object> map);
	int  insertDegreeinfoError(String termInfoId , List<JSONObject> list);
	
	/**
	 * 模板消息
	 * @param param
	 * @param result
	 */
	void sendWXMsg(DegreeInfo egreeInfo,List<Long> ids,String examTypeName,int examType,String role);
	
	public void sendWxMsgLimitCount(DegreeInfo egreeInfo,List<JSONObject> receivers,int examType,String examTypeName,String role);
	
	public void updatetDegreeInfo(String xxdm,String kslcdm,String xnxq,int counter);
}
