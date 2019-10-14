package com.talkweb.scoreManage.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.AccountLesson;
import com.talkweb.accountcenter.thrift.Classroom;
import com.talkweb.base.dao.impl.MyBatisBaseDaoImpl;
import com.talkweb.common.splitDbAndTable.SplitDbAndTableRule;
import com.talkweb.common.splitDbAndTable.SplitDbAndTableRuleFactory;
import com.talkweb.common.splitDbAndTable.TermInfoIdUtils;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.scoreManage.dao.ScoreManageDao;
import com.talkweb.scoreManage.dao.ScoreReportDao;
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

@Repository
public class ScoreManageDaoImpl extends MyBatisBaseDaoImpl implements ScoreManageDao {
	private static final String MYBATIS_PREFIX = "com.talkweb.scoreManage.dao.ScoreManageDao.";

	private static final int INSERT_LIMIT = 1000;

	private int insertBatch(List<?> list, SplitDbAndTableRule splitDbAndTableRule, String key) {
		int result = 0;
		// 分批插入，因为mysql设置了max_allowed_packet选项，超过了就丢掉部分数据，分批插入也减少数据库的压力
		int batchSize = list.size() % INSERT_LIMIT == 0 ? list.size() / INSERT_LIMIT - 1 : list.size() / INSERT_LIMIT;
		for (int batch = 0; batch <= batchSize; batch++) {
			int fromIndex = batch * INSERT_LIMIT;
			int toIndex = fromIndex + INSERT_LIMIT;
			toIndex = toIndex > list.size() ? list.size() : toIndex;
			splitDbAndTableRule.setData(list.subList(fromIndex, toIndex));
			result += insert(MYBATIS_PREFIX + key, splitDbAndTableRule);
		}
		return result;
	}

	@Autowired
	private SplitDbAndTableRuleFactory splitDbAndTableRuleFactory;

	@Autowired
	private AllCommonDataService commonDataService;
	
	@Autowired
	private ScoreReportDao scoreReportDao;

	@Override
	public List<DegreeInfo> getDegreeInfoList(String termInfo, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfo, null, map);
		List<DegreeInfo> list = selectList(MYBATIS_PREFIX + "getDegreeInfoList", splitDbAndTableRule);
		if (list == null) {
			return new ArrayList<DegreeInfo>();
		}
		return list;
	}

	@Override
	public DegreeInfo getDegreeInfoById(String termInfo, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfo, null, map);
		return selectOne(MYBATIS_PREFIX + "getDegreeInfoById", splitDbAndTableRule);
	}

	@Override
	public List<DegreeInfoNj> getDegreeInfoNjList(String termInfo, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfo, null, map);
		List<DegreeInfoNj> list = selectList(MYBATIS_PREFIX + "getDegreeInfoNjList", splitDbAndTableRule);
		if (list == null) {
			return new ArrayList<DegreeInfoNj>();
		}
		return list;
	}

	@Override
	public boolean hasSameNameInDegreeInfo(String termInfo, DegreeInfo degreeInfo) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfo, null, degreeInfo);
		Integer result = selectOne(MYBATIS_PREFIX + "hasSameNameInDegreeInfo", splitDbAndTableRule);
		if (result != null) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public int insertDegreeInfo(String termInfo, DegreeInfo degreeInfo) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfo, null, degreeInfo);
		return insert(MYBATIS_PREFIX + "insertDegreeInfo", splitDbAndTableRule);
	}

	@Override
	public List<String> getNjFromDegreeInfoNj(String termInfo, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfo, null, map);
		List<String> list = selectList(MYBATIS_PREFIX + "getNjFromDegreeInfoNj", splitDbAndTableRule);
		if (list == null) {
			return new ArrayList<String>();
		}
		return list;
	}

	@Override
	public int insertDegreeInfoNjBatch(String termInfo, List<DegreeInfoNj> list) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfo, null, list);
		return insert(MYBATIS_PREFIX + "insertDegreeInfoNjBatch", splitDbAndTableRule);
	}

	@Override
	public int updatetDegreeInfo(String termInfo, Object degreeInfo) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfo, null, degreeInfo);
		return update(MYBATIS_PREFIX + "updatetDegreeInfo", splitDbAndTableRule);
	}

	@Override
	public int deleteDegreeInfo(String termInfo, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfo, null, map);
		return delete(MYBATIS_PREFIX + "deleteDegreeInfo", splitDbAndTableRule);
	}

	@Override
	public int deleteDegreeInfoNj(String termInfo, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfo, null, map);
		return delete(MYBATIS_PREFIX + "deleteDegreeInfoNj", splitDbAndTableRule);
	}

	@Override
	public int updateDegreeInfoNj(String termInfo, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfo, null, map);
		return update(MYBATIS_PREFIX + "updateDegreeInfoNj", splitDbAndTableRule);
	}

	@Override
	public List<ScoreInfo> getScoreInfo(String termInfo, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfo, autoIncr, map);
		List<ScoreInfo> list = selectList(MYBATIS_PREFIX + "getScoreInfo", splitDbAndTableRule);
		if (list == null) {
			return new ArrayList<ScoreInfo>();
		}
		return list;
	}

	@Override
	public int insertScoreInfoBatch(String termInfo, Integer autoIncr, List<ScoreInfo> list) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfo, autoIncr, list);
		return insertBatch(list, splitDbAndTableRule, "insertScoreInfoBatch");
	}

	@Override
	public int deleteScoreInfo(String termInfo, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfo, autoIncr, map);
		return delete(MYBATIS_PREFIX + "deleteScoreInfo", splitDbAndTableRule);
	}

	@Override
	public List<String> getNjByScoreInfo(String termInfo, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfo, autoIncr, map);
		List<String> list = selectList(MYBATIS_PREFIX + "getNjByScoreInfo", splitDbAndTableRule);
		if (list == null) {
			return new ArrayList<String>();
		}
		return list;
	}

	@Override
	public int updateScoreInfo(String termInfo, Integer autoIncr, Object obj) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfo, autoIncr, obj);
		return update(MYBATIS_PREFIX + "updateScoreInfo", splitDbAndTableRule);
	}

	@Override
	public List<Long> getTeacherIdFromScoreInfo(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		List<ScoreInfo> scoreInfoList = getScoreInfo(termInfoId, autoIncr, map);
		List<JSONObject> classHaveScoreList = scoreReportDao.getExamClassReportList(termInfoId, autoIncr, map);
		List<Long> haveScoreClassIds = new ArrayList();
		if(classHaveScoreList != null && classHaveScoreList.size()>0) {
			classHaveScoreList.stream().forEach(obj -> haveScoreClassIds.add(obj.getLong("bh")));
		}
		
		long schoolId = Long.parseLong(map.get("xxdm").toString());
		Set<Long> result = new HashSet<Long>();
		Map<Long, Set<Long>> classId2SubjectIds = new HashMap<Long, Set<Long>>();
		if (CollectionUtils.isNotEmpty(scoreInfoList)) {
			for (ScoreInfo scoreInfo : scoreInfoList) {
				Long classId = Long.valueOf(scoreInfo.getBh());
				Long subjectId = Long.valueOf(scoreInfo.getKmdm());
				if (classId == null || subjectId == null || !haveScoreClassIds.contains(classId)) {
					continue;
				}
				if (!classId2SubjectIds.containsKey(classId)) {
					classId2SubjectIds.put(classId, new HashSet<Long>());
				}
				classId2SubjectIds.get(classId).add(subjectId);
			}
		}
		if (classId2SubjectIds.size() == 0) {
			return new ArrayList<Long>(result);
		}
		
		List<Classroom> classrooms = commonDataService.getClassroomBatch(schoolId,
				new ArrayList<Long>(classId2SubjectIds.keySet()), termInfoId);
		if (CollectionUtils.isNotEmpty(classrooms)) {
			for (Classroom classroom : classrooms) {
				// 添加班主任id
				Long deanId = classroom.getDeanAccountId();
				if (deanId != null) {
					result.add(deanId);
				}
				// 添加任课教师id
				long classId = classroom.getId();
				List<AccountLesson> als = classroom.getAccountLessons();
				if (CollectionUtils.isEmpty(als)) {
					continue;
				}
				Set<Long> set = classId2SubjectIds.get(classId);
				for (AccountLesson al : als) {
					Long subjectId = al.getLessonId();
					if (subjectId != null && set.contains(subjectId)) {
						Long teacherId = al.getAccountId();
						if (teacherId != null) {
							result.add(teacherId);
						}
					}
				}
			}
		}

		return new ArrayList<Long>(result);
	}

	@Override
	public List<Long> getStudentIdFromScoreInfo(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		Set<Long> result = new HashSet<Long>();
		List<ScoreInfo> scoreInfoList = getScoreInfo(termInfoId, autoIncr, map);
		if (CollectionUtils.isNotEmpty(scoreInfoList)) {
			for (ScoreInfo scoreInfo : scoreInfoList) {
				Long studentId = Long.valueOf(scoreInfo.getXh());
				if (studentId != null) {
					result.add(studentId);
				}
			}
		}
		return new ArrayList<Long>(result);
	}

	@Override
	public List<JSONObject> getScoreReleaseList(String termInfoId, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, null, map);
		List<JSONObject> list = selectList(MYBATIS_PREFIX + "getScoreReleaseList", splitDbAndTableRule);
		if(list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}

	@Override
	public List<JSONObject> getBjfzList(String termInfo, Integer autoIncr, Map<String, Object> map) {
		map.put("lb", "02"); // 获取班级分组
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfo, autoIncr, map);
		List<JSONObject> list = selectList(MYBATIS_PREFIX + "getGroupDropDownList", splitDbAndTableRule);
		if (list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}

	@Override
	public List<Dbkslc> getPrevKslcList(Date cdate, JSONObject params, Set<Long> classIds) {
		List<Dbkslc> result = new ArrayList<Dbkslc>();
		
		String xxdm = params.getString("xxdm");
		String termInfoId = params.getString("xnxq");
		String nj = params.getString("nj");
		String kslc = params.getString("kslc");
		
		// 分库分表，需要遍历所有的学年学期的数据
		List<String> termInfoIdList = TermInfoIdUtils.getUserAllTermInfoIdsByUsedGrade(commonDataService, nj, termInfoId);
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("xxdm", xxdm);
		map.put("nj", nj);
		map.put("classIds", classIds);
		map.put("ltCdate", cdate);	// 小于此时间的数据
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, null, null);
		
		Set<String> hasExistedDbkslc = new HashSet<String>();	// 已经存在的对比考试轮次
		Set<Long> hasFoundClassIdSet = new HashSet<Long>();	// 已经发现的班级代码集合，作为结束操作的判断条件
		
		for(String xnxq : termInfoIdList) {	// 遍历所有的学年学期，因为使用了学年学期分库
			map.put("xnxq", xnxq);
			
			// 获取此学年学期的时间小于cdate的考试轮次，倒序排序
			List<DegreeInfo> degreeInfoList = getDegreeInfoList(xnxq, map);
			for(DegreeInfo degreeInfo : degreeInfoList) {
				if(classIds.size() == 0) {	// 如果当前考试没有班级代码，则直接取上一次考试轮次
					Dbkslc dbkslc = new Dbkslc();
					dbkslc.setKslc(kslc);
					dbkslc.setXxdm(xxdm);
					dbkslc.setDbkslc(degreeInfo.getKslcdm());
					dbkslc.setDbxnxq(degreeInfo.getXnxq());
					dbkslc.setXnxq(xnxq);
					result.add(dbkslc);
					return result;
				}
			
				map.put("kslc", degreeInfo.getKslcdm());
				Integer autoIncr = degreeInfo.getAutoIncr();
				
				splitDbAndTableRule.setAutoIncr(autoIncr);
				splitDbAndTableRule.setTermInfoId(xnxq);
				splitDbAndTableRule.setData(map);
				
				List<JSONObject> list = selectList(MYBATIS_PREFIX + "getLastKslcs", splitDbAndTableRule);
				if(list != null) {
					for(JSONObject obj : list) {
						String dbkslcdm = obj.getString("kslc");
						String dbxnxq = obj.getString("xnxq");
						Long classId = obj.getLong("bh");
						
						if (classIds.contains(classId) && !hasFoundClassIdSet.contains(classId)) {	// 已经发现的对比考试轮次
							hasFoundClassIdSet.add(classId);
							
							String key = dbkslcdm + dbxnxq;
							if(!hasExistedDbkslc.contains(key)) {
								hasExistedDbkslc.add(key);
								
								Dbkslc dbkslc = new Dbkslc();
								dbkslc.setKslc(kslc);
								dbkslc.setXxdm(xxdm);
								dbkslc.setDbkslc(degreeInfo.getKslcdm());
								dbkslc.setDbxnxq(degreeInfo.getXnxq());
								dbkslc.setXnxq(termInfoId);
								result.add(dbkslc);
							}
						}
						
						if(hasFoundClassIdSet.size() == classIds.size()) {	// 包含所有班级代码的考试轮次都已经找到了，则退出查找
							return result;
						}
					}
				}
			}
		}
		
		return result;
	}
	
	@Override
	public DegreeInfo getOldKslcAndWlfz(Date cdate, JSONObject params, Set<String> dms) {
		String schoolId = params.getString("xxdm");
		String termInfoId = params.getString("xnxq");
		String nj = params.getString("nj");
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, null, null);
		// 分库分表，需要遍历所有的学年学期的数据
		List<String> termInfoIdList = TermInfoIdUtils.getUserAllTermInfoIdsByUsedGrade(commonDataService, nj, termInfoId);
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("xxdm", String.valueOf(schoolId));
		map.put("nj", nj);
		map.put("ltCdate", cdate);	// 小于此时间的数据
		
		for(String xnxq : termInfoIdList) {
			map.put("xnxq", xnxq);
			List<DegreeInfo> degreeInfoList = getDegreeInfoList(xnxq, map);
			for(DegreeInfo degreeInfo : degreeInfoList) {
				if(degreeInfo.getCdate().before(cdate)) {	// 只查询cdate日期之前的数据
					map.put("kslc", degreeInfo.getKslcdm());
					
					Integer autoIncr = degreeInfo.getAutoIncr();
					splitDbAndTableRule.setAutoIncr(autoIncr);
					splitDbAndTableRule.setTermInfoId(xnxq);
					splitDbAndTableRule.setData(map);
					
					List<String> bjlxmsList = selectList(MYBATIS_PREFIX + "getOldKslcAndWlfz", splitDbAndTableRule);
					if(bjlxmsList != null) {
						for(String bjlxms : bjlxmsList) {
							if(JudeEqualSet(dms, bjlxms)) {
								return degreeInfo;
							}
						}
					}
				}
			}
		}
		return null;
	}
	
	public int insertScoreRankDistributeBatch(String termInfoId, Integer autoIncr, List<?> list) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, list);
		return insert(MYBATIS_PREFIX + "insertScoreRankDistributeBatch", splitDbAndTableRule);
	}
	
	@Override
	public int insertDbkslcBatch(String termInfoId, Integer autoIncr, List<Dbkslc> list) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, list);
		return insert(MYBATIS_PREFIX + "insertDbkslcBatch", splitDbAndTableRule);
	}

	@Override
	public List<String> getExamAnalysisConfig(Map<String, Object> map) {
		List<String> list = selectList(MYBATIS_PREFIX + "getExamAnalysisConfig", map);
		if (list == null) {
			return new ArrayList<String>();
		}
		return list;
	}

	@Override
	public boolean ifExistsJXPGScoreSection(Map<String, Object> map) {
		Integer result = selectOne(MYBATIS_PREFIX + "ifExistsJXPGScoreSection", map);
		if (result == null) {
			return false;
		}
		return true;
	}

	@Override
	public int insertJXPGScoreSectionBatch(List<?> list) {
		return insert(MYBATIS_PREFIX + "insertJXPGScoreSectionBatch", list);
	}

	@Override
	public int initScoreStatTerm(String xxdm) {
		return insert(MYBATIS_PREFIX + "initScoreStatTerm", xxdm);
	}

	@Override
	public boolean ifExistsSettingBj(String xxdm) {
		Integer result = selectOne(MYBATIS_PREFIX + "ifExistsSettingBj", xxdm);
		if (result == null) {
			return false;
		}
		return true;
	}

	@Override
	public int insertClassReportParamBatch(List<SettingBJ> list) {
		return insert(MYBATIS_PREFIX + "insertClassReportParamBatch", list);
	}

	@Override
	public boolean ifExistsSettingStu(String xxdm) {
		Integer result = selectOne(MYBATIS_PREFIX + "ifExistsSettingBj", xxdm);
		if (result == null) {
			return false;
		}
		return true;
	}

	@Override
	public int insertStudentReportParamBatch(List<SettingStu> list) {
		return insert(MYBATIS_PREFIX + "insertStudentReportParamBatch", list);
	}

	@Override
	public List<JSONObject> getExamDefaultSetting(JSONObject params, Integer autoIncr, Set<String> setItemSet) {
		String xnxq = params.getString("xnxq");
		
		List<JSONObject> rs = new ArrayList<JSONObject>();
		// 文理分组
		if (setItemSet.contains("01")) {
			JSONObject obj = new JSONObject();
			obj.put("setItemId", "01");
			obj.put("isSeted", ifExistsAnalysisConfig01(xnxq, autoIncr, params));
			rs.add(obj);
		}

		// 班级分组
		if (setItemSet.contains("02")) {
			JSONObject obj = new JSONObject();
			obj.put("setItemId", "02");
			obj.put("isSeted", ifExistsAnalysisConfig02(xnxq, autoIncr, params));
			rs.add(obj);
		}
		// 统计人数
		if (setItemSet.contains("03")) {
			JSONObject obj = new JSONObject();
			obj.put("setItemId", "03");
			obj.put("isSeted", ifExistsAnalysisConfig03(xnxq, autoIncr, params));
			rs.add(obj);
		}
		// 不参考统计学生
		if (setItemSet.contains("04")) {
			JSONObject obj = new JSONObject();
			obj.put("setItemId", "04");
			obj.put("isSeted", ifExistsAnalysisConfig04(xnxq, autoIncr, params));
			rs.add(obj);
		}
		// 统计科目与满分
		if (setItemSet.contains("05")) {
			JSONObject obj = new JSONObject();
			obj.put("setItemId", "05");
			obj.put("isSeted", ifExistsAnalysisConfig05(xnxq, autoIncr, params));
			rs.add(obj);
		}
		// 合并科目
		if (setItemSet.contains("06")) {
			JSONObject obj = new JSONObject();
			obj.put("setItemId", "06");
			obj.put("isSeted", ifExistsAnalysisConfig06(xnxq, autoIncr, params));
			rs.add(obj);
		}
		// 统计参数
		if (setItemSet.contains("07")) {
			JSONObject obj = new JSONObject();
			obj.put("setItemId", "07");
			obj.put("isSeted", ifExistsAnalysisConfig07(params));
			rs.add(obj);
		}
		// 排名区间
		if (setItemSet.contains("08")) {
			JSONObject obj = new JSONObject();
			obj.put("setItemId", "08");
			obj.put("isSeted", ifExistsAnalysisConfig08(xnxq, autoIncr, params));
			rs.add(obj);
		}
		// 分数段
		if (setItemSet.contains("09")) {
			JSONObject obj = new JSONObject();
			obj.put("setItemId", "09");
			obj.put("isSeted", ifExistsAnalysisConfig09(xnxq, autoIncr, params));
			rs.add(obj);
		}
		// 对比考试
		if (setItemSet.contains("10")) {
			JSONObject obj = new JSONObject();
			obj.put("setItemId", "10");
			obj.put("isSeted", ifExistsAnalysisConfig10(xnxq, autoIncr, params));
			rs.add(obj);
		}
		// 等第设置
		if (setItemSet.contains("11")) {
			JSONObject obj = new JSONObject();
			obj.put("setItemId", "11");
			obj.put("isSeted", ifExistsAnalysisConfig11(xnxq, autoIncr, params));
			rs.add(obj);
		}
		// 竞赛设置
		if (setItemSet.contains("12")) {
			JSONObject obj = new JSONObject();
			obj.put("setItemId", "12");
			obj.put("isSeted", ifExistsAnalysisConfig12(xnxq, autoIncr, params));
			rs.add(obj);
		}
		// 班级报告设置
		if (setItemSet.contains("13")) {
			JSONObject obj = new JSONObject();
			obj.put("setItemId", "13");
			obj.put("isSeted", ifExistsAnalysisConfig13(params));
			rs.add(obj);
		}
		// 学生报告设置
		if (setItemSet.contains("14")) {
			JSONObject obj = new JSONObject();
			obj.put("setItemId", "14");
			obj.put("isSeted", ifExistsAnalysisConfig14(params));
			rs.add(obj);
		}
		return rs;
	}

	@Override
	public List<JSONObject> getwlkGroupList(String termInfo, Integer autoIncr, Map<String, Object> map) {
		map.put("lb", "01"); // 文理分组下拉列表
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfo, autoIncr, map);
		List<JSONObject> list = selectList(MYBATIS_PREFIX + "getGroupDropDownList", splitDbAndTableRule);
		if (list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}

	@Override
	public List<TopGroup> getTopGroup(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<TopGroup> list = selectList(MYBATIS_PREFIX + "getTopGroup", splitDbAndTableRule);
		if (list == null) {
			return new ArrayList<TopGroup>();
		}
		return list;
	}
	
	@Override
	public List<String> getSsfzFromTopGroup(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<String> list = selectList(MYBATIS_PREFIX + "getSsfzFromTopGroup", splitDbAndTableRule);
		if (list == null) {
			return new ArrayList<String>();
		}
		return list;
	}
	
	@Override
	public int deleteTopGroup(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		return delete(MYBATIS_PREFIX + "deleteTopGroup", splitDbAndTableRule);
	}

	@Override
	public int deleteTopGroupBj(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		return delete(MYBATIS_PREFIX + "deleteTopGroupBj", splitDbAndTableRule);
	}

	@Override
	public int updateTopGroup(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		return update(MYBATIS_PREFIX + "updateTopGroup", splitDbAndTableRule);
	}
	
	@Override
	public List<JSONObject> getASGListForStatic(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<JSONObject> list = selectList(MYBATIS_PREFIX + "getASGListForStatic", splitDbAndTableRule);
		if(list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}

	@Override
	public int updateASGStatRules(String termInfoId, Integer autoIncr, JSONArray tjfz) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, null);
		int rs = 0;
		for (Object obj : tjfz) {
			JSONObject json = (JSONObject) obj;
			splitDbAndTableRule.setData(json);
			rs += update(MYBATIS_PREFIX + "updateASGStatRules", splitDbAndTableRule);
			rs += update(MYBATIS_PREFIX + "updateASGStatRules2", splitDbAndTableRule);
		}
		return rs;
	}

	@Override
	public List<JSONObject> getClassGroupListStatic(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<JSONObject> list = selectList(MYBATIS_PREFIX + "getClassGroupListStatic", splitDbAndTableRule);
		if(list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}

	@Override
	public int updateClassGroupStatic(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		int result = 0;
		result += update(MYBATIS_PREFIX + "updateClassGroupStaticClearWlk", splitDbAndTableRule);
		result += update(MYBATIS_PREFIX + "updateClassGroupStatic", splitDbAndTableRule);
		return result;
	}

	@Override
	public int insertXscjbtjBatch(String termInfoId, Integer autoIncr, List<?> list) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, list);
		return update(MYBATIS_PREFIX + "insertXscjbtjBatch", splitDbAndTableRule);
	}

	@Override
	public int deleteXscjbtj(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		return delete(MYBATIS_PREFIX + "deleteXscjbtj", splitDbAndTableRule);
	}

	@Override
	public void deleteAllExamAnalysisConfig(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		delete(MYBATIS_PREFIX + "deleteTopGroupBj", splitDbAndTableRule);
		delete(MYBATIS_PREFIX + "deleteTopGroup", splitDbAndTableRule);
		delete(MYBATIS_PREFIX + "deleteScoretjmk", splitDbAndTableRule);
		delete(MYBATIS_PREFIX + "deleteScoreRankDistribute", splitDbAndTableRule);
		delete(MYBATIS_PREFIX + "deleteZfqjsz", splitDbAndTableRule);
		delete(MYBATIS_PREFIX + "deleteScoreMF", splitDbAndTableRule);
		delete(MYBATIS_PREFIX + "deleteSynthScore", splitDbAndTableRule);
		delete(MYBATIS_PREFIX + "deleteScoreLevelTemplate", splitDbAndTableRule);
//		delete(MYBATIS_PREFIX + "deleteScoreStuStatisticsRankMK", splitDbAndTableRule);
	}

	@Override
	public void delXSCJBTJ(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		delete("delXSCJBTJ", splitDbAndTableRule);
	}

	@Override
	public boolean ifExistsAnalysisConfig01(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		Integer result = selectOne(MYBATIS_PREFIX + "ifExistsAnalysisConfig01", splitDbAndTableRule);
		if (result == null) {
			return false;
		}
		return true;
	}

	@Override
	public boolean ifExistsAnalysisConfig02(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		Integer result = selectOne(MYBATIS_PREFIX + "ifExistsAnalysisConfig02", splitDbAndTableRule);
		if (result == null) {
			return false;
		}
		return true;
	}
	
	@Override
	public boolean ifExistsAnalysisConfig03(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		Integer result = selectOne(MYBATIS_PREFIX + "ifExistsAnalysisConfig03-1", splitDbAndTableRule);
		if (result != null) {
			return true;
		}
		result = selectOne(MYBATIS_PREFIX + "ifExistsAnalysisConfig03-2", splitDbAndTableRule);
		if(result != null) {
			return true;
		}
		return false;
	}
	
	@Override
	public boolean ifExistsAnalysisConfig04(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		Integer result = selectOne(MYBATIS_PREFIX + "ifExistsAnalysisConfig04", splitDbAndTableRule);
		if (result == null) {
			return false;
		}
		return true;
	}

	@Override
	public boolean ifExistsAnalysisConfig05(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		Integer result = selectOne(MYBATIS_PREFIX + "ifExistsAnalysisConfig05", splitDbAndTableRule);
		if (result == null) {
			return false;
		}
		return true;
	}
	
	@Override
	public boolean ifExistsAnalysisConfig06(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		Integer result = selectOne(MYBATIS_PREFIX + "ifExistsAnalysisConfig06", splitDbAndTableRule);
		if (result == null) {
			return false;
		}
		return true;
	}
	
	@Override
	public boolean ifExistsAnalysisConfig07(Map<String, Object> map) {
		Integer result = selectOne(MYBATIS_PREFIX + "ifExistsAnalysisConfig07", map);
		if (result == null) {
			return false;
		}
		return true;
	}
	
	@Override
	public boolean ifExistsAnalysisConfig08(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		Integer result = selectOne(MYBATIS_PREFIX + "ifExistsAnalysisConfig08", splitDbAndTableRule);
		if (result == null) {
			return false;
		}
		return true;
	}

	@Override
	public boolean ifExistsAnalysisConfig09(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		Integer result = selectOne(MYBATIS_PREFIX + "ifExistsAnalysisConfig09", splitDbAndTableRule);
		if (result == null) {
			return false;
		}
		return true;
	}
	
	@Override
	public boolean ifExistsAnalysisConfig10(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		Integer result = selectOne(MYBATIS_PREFIX + "ifExistsAnalysisConfig10", splitDbAndTableRule);
		if (result == null) {
			return false;
		}
		return true;
	}
	
	@Override
	public boolean ifExistsAnalysisConfig11(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		Integer result = selectOne(MYBATIS_PREFIX + "ifExistsAnalysisConfig11", splitDbAndTableRule);
		if (result == null) {
			return false;
		}
		return true;
	}
	
	@Override
	public boolean ifExistsAnalysisConfig12(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		Integer result = selectOne(MYBATIS_PREFIX + "ifExistsAnalysisConfig12", splitDbAndTableRule);
		if (result == null) {
			return false;
		}
		return true;
	}
	
	@Override
	public boolean ifExistsAnalysisConfig13(Map<String, Object> map) {
		Integer result = selectOne(MYBATIS_PREFIX + "ifExistsAnalysisConfig13", map);
		if (result == null) {
			return false;
		}
		return true;
	}
	
	@Override
	public boolean ifExistsAnalysisConfig14(Map<String, Object> map) {
		Integer result = selectOne(MYBATIS_PREFIX + "ifExistsAnalysisConfig14", map);
		if (result == null) {
			return false;
		}
		return true;
	}
	
	@Override
	public boolean ifExistsSameTopGroup(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		Integer result = selectOne(MYBATIS_PREFIX + "ifExistsSameTopGroup", splitDbAndTableRule);
		if(result == null) {
			return false;
		}
		return true;
	}
	
	@Override
	public int insertTopGroupBatch(String termInfoId, Integer autoIncr, List<?> list) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, list);
		return insert(MYBATIS_PREFIX + "insertTopGroupBatch", splitDbAndTableRule);
	}
	
	@Override
	public int insertTopGroupBjBatch(String termInfoId, Integer autoIncr, List<?> list) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, list);
		return insert(MYBATIS_PREFIX + "insertTopGroupBjBatch", splitDbAndTableRule);
	}
	
	@Override
	public List<ScoreRankDistribute> getRankListByPrevKslc(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<ScoreRankDistribute> list = selectList(MYBATIS_PREFIX + "getRankListByPrevKslc", splitDbAndTableRule);
		if(list == null) {
			return new ArrayList<ScoreRankDistribute>();
		}
		return list;
	}
	
	@Override
	public List<CompetitionStu> getCompetitionStu(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<CompetitionStu> list = selectList(MYBATIS_PREFIX + "getCompetitionStu", splitDbAndTableRule);
		if(list == null) {
			return new ArrayList<CompetitionStu>();
		}
		return list;
	}
	
	@Override
	public int insertCompetitionStuBatch(String termInfoId, Integer autoIncr, List<?> list) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, list);
		return insert(MYBATIS_PREFIX + "insertCompetitionStuBatch", splitDbAndTableRule);
	}
	
	@Override
	public List<SynthScore> getSynthScoreByPrevKslc(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<SynthScore> list = selectList(MYBATIS_PREFIX + "getSynthScoreByPrevKslc", splitDbAndTableRule);
		if(list == null) {
			return new ArrayList<SynthScore>();
		}
		return list;
	}
	
	@Override
	public int insertSynthScoreBatch(String termInfoId, Integer autoIncr, List<?> list) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, list);
		return insert(MYBATIS_PREFIX + "insertSynthScoreBatch", splitDbAndTableRule);
	}
	
	@Override
	public List<ScoreLevelTemplate> getScoreLevelTemplate(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<ScoreLevelTemplate> list = selectList(MYBATIS_PREFIX + "getScoreLevelTemplate", splitDbAndTableRule);
		if(list == null) {
			return new ArrayList<ScoreLevelTemplate>();
		}
		return list;
	}
	
	@Override
	public List<JSONObject> getCjListForDdSet(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<JSONObject> list = selectList(MYBATIS_PREFIX + "getCjListForDdSet", splitDbAndTableRule);
		if(list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}
	
	@Override
	public int insertScoreLevelTemplateBatch(String termInfoId, Integer autoIncr, List<?> list) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, list);
		return insert(MYBATIS_PREFIX + "insertScoreLevelTemplateBatch", splitDbAndTableRule);
	}

	@Override
	public List<JSONObject> getTopGroupByPrevKslc(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<JSONObject> list = selectList(MYBATIS_PREFIX + "getTopGroupByPrevKslc", splitDbAndTableRule);
		if(list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}
	
	@Override
	public List<TopGroupBj> getTopGroupBj(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<TopGroupBj> list = selectList(MYBATIS_PREFIX + "getTopGroupBj", splitDbAndTableRule);
		if(list == null) {
			return new ArrayList<TopGroupBj>();
		}
		return list;
	}
	
	@Override
	public List<JSONObject> getKmfzList(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<JSONObject> list = selectList(MYBATIS_PREFIX + "getKmfzList", splitDbAndTableRule);
		if(list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}
	
	@Override
	public int insertScoreTjmkBatch(String termInfoId, Integer autoIncr, List<?> list) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, list);
		return insert(MYBATIS_PREFIX + "insertScoreTjmkBatch", splitDbAndTableRule);
	}
	
	@Override
	public int insertScoreMfBatch(String termInfoId, Integer autoIncr, List<?> list) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, list);
		return insert(MYBATIS_PREFIX + "insertScoreMfBatch", splitDbAndTableRule);
	}
	
	@Override
	public List<Zfqjsz> getZfqjsz(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<Zfqjsz> list = selectList(MYBATIS_PREFIX + "getZfqjsz", splitDbAndTableRule);
		if(list == null) {
			return new ArrayList<Zfqjsz>();
		}
		return list;
	}
	
	@Override 
	public int insertZfqjszBatch(String termInfoId, Integer autoIncr, List<?> list) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, list);
		return insert(MYBATIS_PREFIX + "insertZfqjszBatch", splitDbAndTableRule);
	}
	
	@Override
	public boolean JudeEqualSet(Set<String> lxs, String lxms) {
		Iterator<String> it = lxs.iterator();
		int i = 0;
		while (it.hasNext()) {
			String str = it.next();
			if (lxms.contains(str)) {
				i++;
			}
		}
		if (i == lxs.size()) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public List<JSONObject> getSubjectStatusList(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<JSONObject> list = selectList(MYBATIS_PREFIX + "getSubjectStatusList", splitDbAndTableRule);
		if(list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}

	@Override
	public int deleteScoretjmk(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		return delete(MYBATIS_PREFIX + "deleteScoretjmk", splitDbAndTableRule);
	}

	@Override
	public int deleteScoreMF(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		return delete(MYBATIS_PREFIX + "deleteScoreMF", splitDbAndTableRule);
	}
	
	@Override
	public List<JSONObject> getMergerSubjectList(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<JSONObject> list = selectList(MYBATIS_PREFIX + "getMergerSubjectList", splitDbAndTableRule);
		if(list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}

	@Override
	public int deleteSynthScore(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		return delete(MYBATIS_PREFIX + "deleteSynthScore", splitDbAndTableRule);
	}

	@Override
	public List<JXPGScoreSection> getJxpgScoreSection(String xxdm) {
		List<JXPGScoreSection> list = selectList(MYBATIS_PREFIX + "getJxpgScoreSection", xxdm);
		if(list == null) {
			return new ArrayList<JXPGScoreSection>();
		}
		return list;
	}

	@Override
	public ScoreStatTerm getScoreStatTerm(String xxdm) {
		return selectOne(MYBATIS_PREFIX + "getScoreStatTerm", xxdm);
	}

	@Override
	public int deleteStatisticalParameters(String xxdm) {
		// TODO Auto-generated method stub
		int num = update(MYBATIS_PREFIX + "delJXPGScoreSection", xxdm);
			num =+ update(MYBATIS_PREFIX + "delScoreStatTerm", xxdm);
		return num;
	}

	@Override
	public int insertScoreStatTerm(Map<String, Object> map) {
		return insert(MYBATIS_PREFIX + "insertScoreStatTerm", map);
	}
	
	@Override
	public List<JSONObject> getIntervalList(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<JSONObject> list = selectList(MYBATIS_PREFIX + "getIntervalList", splitDbAndTableRule);
		if(list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}

	@Override
	public int deleteScoreRankDistribute(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		return delete(MYBATIS_PREFIX + "deleteScoreRankDistribute", splitDbAndTableRule);
	}

	@Override
	public JSONObject getTotalScoreSection(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		return selectOne(MYBATIS_PREFIX + "getTotalScoreSection", splitDbAndTableRule);
	}

	@Override
	public int insertZfqjsz(String termInfoId, Integer autoIncr, Object obj) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, obj);
		return insert(MYBATIS_PREFIX + "insertZfqjsz", splitDbAndTableRule);
	}

	@Override
	public List<Dbkslc> getDbkslcList(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<Dbkslc> dbkslcList = selectList(MYBATIS_PREFIX + "getDbkslcList", splitDbAndTableRule);
		if(dbkslcList == null) {
			return new ArrayList<Dbkslc>();
		}
		return dbkslcList;
	}
	
	@Override
	public int deleteContrastExam(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		return update(MYBATIS_PREFIX + "deleteContrastExam", splitDbAndTableRule);
	}

	@Override
	public Set<Long> getBcytjXhList(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<Long> list = selectList(MYBATIS_PREFIX + "getBcytjXhList", splitDbAndTableRule);
		if(list == null) {
			return new HashSet<Long>();
		}
		return new HashSet<Long>(list);
	}

	@Override
	public List<Long> getSubjectIdFromScoreLevelTemplate(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<Long> list = selectList(MYBATIS_PREFIX + "getSubjectIdFromScoreLevelTemplate", splitDbAndTableRule);
		if(list == null) {
			return new ArrayList<Long>();
		}
		return list;
	}

	@Override
	public List<JSONObject> getRankingsByKMDM(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<JSONObject> list = selectList(MYBATIS_PREFIX + "getRankingsByKMDM", splitDbAndTableRule);
		if(list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}

	@Override
	public int deleteScoreLevelTemplate(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		return update(MYBATIS_PREFIX + "deleteScoreLevelTemplate", splitDbAndTableRule);
	}

	@Override
	public List<JSONObject> getAllDJList(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<JSONObject> list = selectList(MYBATIS_PREFIX + "getAllDJList", splitDbAndTableRule);
		if(list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}

	@Override
	public List<SynthScore> getSynthScoreList(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<SynthScore> list = selectList(MYBATIS_PREFIX + "getSynthScoreList", splitDbAndTableRule);
		if(list == null) {
			return new ArrayList<SynthScore>();
		}
		return list;
	}
	
	@Override
	public List<JSONObject> selectCjListForDdSet(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<JSONObject> list = selectList(MYBATIS_PREFIX + "selectCjListForDdSet", splitDbAndTableRule);
		if(list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}

	@Override
	public List<Long> getSubjectIdsFromScoreInfo(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<Long> list = selectList(MYBATIS_PREFIX + "getSubjectIdsFromScoreInfo", splitDbAndTableRule);
		if(list == null) {
			return new ArrayList<Long>();
		}
		return list;
	}

	@Override
	public List<JSONObject> getClassReportParam(String xxdm) {
		List<JSONObject> list = selectList(MYBATIS_PREFIX + "getClassReportParam", xxdm);
		if(list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}

	@Override
	public List<SettingStu> getSettingStuList(String xxdm) {
		List<SettingStu> list = selectList(MYBATIS_PREFIX + "getSettingStuList", xxdm);
		if(list == null) {
			return new ArrayList<SettingStu>();
		}
		return list;
	}

	@Override
	public List<Long> getWfzList(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<Long> list = selectList(MYBATIS_PREFIX + "getWfzList", splitDbAndTableRule);
		if(list == null) {
			return new ArrayList<Long>();
		}
		return list;
	}

	@Override
	public List<JSONObject> getYfzList(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<JSONObject> list = selectList(MYBATIS_PREFIX + "getYfzList", splitDbAndTableRule);
		if(list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}

	@Override
	public int deleteCompetitionStud(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		return delete(MYBATIS_PREFIX + "deleteCompetitionStud", splitDbAndTableRule);
	}

	@Override
	public List<Long> getExamSubjectIdList(String termInfo, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfo, autoIncr, map);
		List<Long> list = selectList(MYBATIS_PREFIX + "getSubjectList", splitDbAndTableRule);
		if (list == null) {
			return new ArrayList<Long>();
		}
		return list;
	}

	@Override
	public List<JSONObject> getExamNameList(String termInfo, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfo, null, map);
		List<JSONObject> list = selectList(MYBATIS_PREFIX + "getExamNameList", splitDbAndTableRule);
		if (list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}

	@Override
	public List<Long> getClassIdListByGroupId(String termInfo, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfo, autoIncr, map);
		List<Long> list = selectList(MYBATIS_PREFIX + "getClassIdListByGroupId", splitDbAndTableRule);
		if (list == null) {
			return new ArrayList<Long>();
		}
		return list;
	}

	@Override
	public List<JSONObject> getSubjectGroupNumById(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<JSONObject> list = selectList(MYBATIS_PREFIX + "getSubjectGroupNumById", splitDbAndTableRule);
		if (list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}

	@Override
	public boolean getAsgGroupSetFlag(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		Integer result = selectOne(MYBATIS_PREFIX + "getAsgGroupSetFlag", splitDbAndTableRule);
		if(result == null) {
			return false;
		}
		return true;
	}

	@Override
	public boolean getClassGroupSetFlag(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		Integer result = selectOne(MYBATIS_PREFIX + "getClassGroupSetFlag", splitDbAndTableRule);
		if(result == null) {
			return false;
		}
		return true;
	}

	public List<Long> getExamClassIdList(String termInfo, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfo, autoIncr, map);
		List<Long> list = selectList(MYBATIS_PREFIX + "getExamClassIdList", splitDbAndTableRule);
		if (list == null) {
			return new ArrayList<Long>();
		}
		return list;
	}

	@Override
	public List<String> getFzdmFromZfqjsz(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<String> list = selectList(MYBATIS_PREFIX + "getFzdmFromZfqjsz", splitDbAndTableRule);
		if (list == null) {
			return new ArrayList<String>();
		}
		return list;
	}

	@Override
	public List<String> getFzdmFromScoreRankDistribute(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<String> list = selectList(MYBATIS_PREFIX + "getFzdmFromScoreRankDistribute", splitDbAndTableRule);
		if (list == null) {
			return new ArrayList<String>();
		}
		return list;
	}
	
	@Override
	public List<JSONObject> getScoreStuBZF(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<JSONObject> list = selectList(MYBATIS_PREFIX + "getScoreStuBZF", splitDbAndTableRule);
		if(list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}

	@Override
	public List<JSONObject> getStuScore(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<JSONObject> list = selectList(MYBATIS_PREFIX + "getStuScore", splitDbAndTableRule);
		if(list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}

	@Override
	public List<JSONObject> getStuTclassMap(Map<String, Object> obj) {
		// TODO Auto-generated method stub
		return selectList(MYBATIS_PREFIX+"getStuTclassMap",obj);
	}

	@Override
	public List<ScoreStuStatisticsRankMk> getStuSubjectScoreRank(String termInfoId, Integer autoIncr,
			Map<String, Object> map) {
		// TODO Auto-generated method stub
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<ScoreStuStatisticsRankMk> list = selectList(MYBATIS_PREFIX + "getStuSubjectScoreRank", splitDbAndTableRule);
		if(list == null) {
			return new ArrayList<ScoreStuStatisticsRankMk>();
		}
		return list;		
	}
	
	@Override
	public List<JSONObject> get3Plus3Scores(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<JSONObject> list = selectList(MYBATIS_PREFIX + "get3Plus3Scores", splitDbAndTableRule);
		if(list == null) {
			return new ArrayList<JSONObject>();
		}
		return list;
	}

	@Override
	public List<Long> getSynthscoreSubject(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<Long> list = selectList(MYBATIS_PREFIX + "getSynthscoreSubject", splitDbAndTableRule);
		return list;
	}

	@Override
	public JSONObject getDegreeinfoRelate(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		JSONObject result = selectOne(MYBATIS_PREFIX + "getDegreeinfoRelate" , splitDbAndTableRule);
		return result;
	}

	@Override
	public int deleteDegreeinfoRelate(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		int result = delete(MYBATIS_PREFIX +"deleteDegreeinfoRelate", splitDbAndTableRule);
		return result;
	}

	@Override
	public int insertDegreeinfoRelate(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		int result = insert(MYBATIS_PREFIX +"insertDegreeinfoRelate", splitDbAndTableRule);
		return result;
	}

	@Override
	public int insertDegreeinfoError(String termInfo, Integer autoIncr, List<JSONObject> list) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfo, autoIncr, list);
		int result = insert(MYBATIS_PREFIX +"insertDegreeinfoError", splitDbAndTableRule);
		return result;
	}

	@Override
	public List<JSONObject> getExamClassList(String termInfoId, Integer autoIncr, Map<String, Object> map) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfoId, autoIncr, map);
		List<JSONObject> list = selectList(MYBATIS_PREFIX +"getExamClassList", splitDbAndTableRule);
		return list;
	}
}
