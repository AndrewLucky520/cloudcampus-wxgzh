package com.talkweb.exammanagement.dao.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.base.dao.impl.MyBatisBaseDaoImpl;
import com.talkweb.common.splitDbAndTable.SplitDbAndTableRule;
import com.talkweb.common.splitDbAndTable.SplitDbAndTableRuleFactory;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.exammanagement.dao.ExamManagementDao;
import com.talkweb.exammanagement.domain.ArrangeExamClassInfo;
import com.talkweb.exammanagement.domain.ArrangeExamPlaceInfo;
import com.talkweb.exammanagement.domain.ArrangeExamRule;
import com.talkweb.exammanagement.domain.ArrangeExamSubjectInfo;
import com.talkweb.exammanagement.domain.ExamManagement;
import com.talkweb.exammanagement.domain.ExamPlan;
import com.talkweb.exammanagement.domain.ExamSubject;
import com.talkweb.exammanagement.domain.StudNotTakingExam;
import com.talkweb.exammanagement.domain.TestNumberInfo;

@Repository
public class ExamManagementDaoImpl extends MyBatisBaseDaoImpl implements ExamManagementDao {
	Logger logger = LoggerFactory.getLogger(ExamManagementDaoImpl.class);
	
	@Autowired
	private SplitDbAndTableRuleFactory splitDbAndTableRuleFactory;
	
	@Autowired
	private AllCommonDataService commonDataService;
	
	private final static String MYBATIS_PREFIX = "com.talkweb.exammanagement.dao.examManagement.";
	
	private static int INSERT_LIMIT = 1000;
	
	private int insertBatch(List<?> list, SplitDbAndTableRule splitDbAndTableRule, String key) {
		int result = 0;
		// 分批插入，因为mysql设置了max_allowed_packet选项，超过了就丢掉部分数据，分批插入也减少数据库的压力
		int batchSize = list.size() % INSERT_LIMIT == 0 ? list.size() / INSERT_LIMIT - 1 : list.size() / INSERT_LIMIT;
		for(int batch = 0; batch <= batchSize; batch ++){
			int fromIndex = batch * INSERT_LIMIT;
			int toIndex = fromIndex + INSERT_LIMIT;
			toIndex = toIndex > list.size() ? list.size() : toIndex;
			splitDbAndTableRule.setData(list.subList(fromIndex, toIndex));
			result += insert(MYBATIS_PREFIX + key, splitDbAndTableRule);
		}
		return result;
	}

	public List<ExamManagement> getExamManagementList(Map<String, Object> map, String termInfo) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfo, null, map);
		List<ExamManagement> list = selectList(MYBATIS_PREFIX + "getExamManagementList", splitDbAndTableRule);
		if(CollectionUtils.isEmpty(list)){
			return new ArrayList<ExamManagement>();
		} else {
			return list;
		}
	}
	
	public ExamManagement getExamManagementListById(Map<String, Object> map, String termInfo) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfo, null, map);
		return selectOne(MYBATIS_PREFIX + "getExamManagementListById", splitDbAndTableRule);
	}
	
	public boolean ifExsitsSameNameInExamManagement(Object obj, String termInfo) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfo, null, obj);
		Integer result = selectOne(MYBATIS_PREFIX + "ifExsitsSameNameInExamManagement", splitDbAndTableRule);
		if(result == null) {
			return false;
		}
		return true;
	}
	
	public Integer insertExamManagement(Object obj, String termInfo) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfo, null, obj);
		return insert(MYBATIS_PREFIX + "insertExamManagement", splitDbAndTableRule);
	}
	
	public Integer updateExamManagementStatus(Object obj, String termInfo) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfo, null, obj);
		return update(MYBATIS_PREFIX + "updateExamManagementStatus", splitDbAndTableRule);
	}
	
	public Integer updateExamManagement(Object obj, String termInfo) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfo, null, obj);
		return update(MYBATIS_PREFIX + "updateExamManagement", splitDbAndTableRule);
	}
	
	public Integer deleteExamManagement(Map<String, Object> map, String termInfo) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfo, null, map);
		return delete(MYBATIS_PREFIX + "deleteExamManagement", splitDbAndTableRule);
	}
	
	public List<ExamPlan> getExamPlanList(Map<String, Object> map, String termInfo, Integer autoIncr) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfo, autoIncr, map);
		List<ExamPlan> list = selectList(MYBATIS_PREFIX + "getExamPlanList", splitDbAndTableRule);
		if(CollectionUtils.isEmpty(list)){
			return new ArrayList<ExamPlan>();
		} else {
			return list;
		}
	}
	
	public List<ExamSubject> getExamSubjectList(Map<String, Object> map, String termInfo, Integer autoIncr) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfo, autoIncr, map);
		List<ExamSubject> list = selectList(MYBATIS_PREFIX + "getExamSubjectList", splitDbAndTableRule);
		if(CollectionUtils.isEmpty(list)){
			return new ArrayList<ExamSubject>();
		} else {
			return list;
		}
	}
	
	public Integer deleteExamPlan(Map<String, Object> map, String termInfo, Integer autoIncr) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfo, autoIncr, map);
		return delete(MYBATIS_PREFIX + "deleteExamPlan", splitDbAndTableRule);
	}
	
	public Integer deleteExamSubject(Map<String, Object> map, String termInfo, Integer autoIncr) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfo, autoIncr, map);
		return delete(MYBATIS_PREFIX + "deleteExamSubject", splitDbAndTableRule);
	}
	
	public Integer insertExamPlan(Object obj, String termInfo, Integer autoIncr) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfo, autoIncr, obj);
		return insert(MYBATIS_PREFIX + "insertExamPlan", splitDbAndTableRule);
	}
	
	public Integer updateExamPlan(Object obj, String termInfo, Integer autoIncr) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfo, autoIncr, obj);
		return insert(MYBATIS_PREFIX + "updateExamPlan", splitDbAndTableRule);
	}
	
	public Integer insertExamSubjectBatch(Object obj, String termInfo, Integer autoIncr) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfo, autoIncr, obj);
		return insert(MYBATIS_PREFIX + "insertExamSubjectBatch", splitDbAndTableRule);
	}
	
	public List<JSONObject> getExamPlaceInfoInArrange(Map<String, Object> map, String termInfo, Integer autoIncr) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfo, autoIncr, map);
		List<JSONObject> result = selectList(MYBATIS_PREFIX + "getExamPlaceInfoInArrange", splitDbAndTableRule);
		if(result == null) {
			return new ArrayList<JSONObject>();
		}
		return result;
	}
	
	public List<ArrangeExamRule> getArrangeExamRule(Map<String, Object> map, String termInfo, Integer autoIncr){
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfo, autoIncr, map);
		List<ArrangeExamRule> result = selectList(MYBATIS_PREFIX + "getArrangeExamRule", splitDbAndTableRule);
		if(result == null) {
			return new ArrayList<ArrangeExamRule>();
		}
		return result;
	}
	
	public List<ArrangeExamClassInfo> getArrangeExamClassInfo(Map<String, Object> map, String termInfo, Integer autoIncr) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfo, autoIncr, map);
		List<ArrangeExamClassInfo> result = selectList(MYBATIS_PREFIX + "getArrangeExamClassInfo", splitDbAndTableRule);
		if(result == null) {
			return new ArrayList<ArrangeExamClassInfo>();
		}
		return result;
	}
	
	public List<ArrangeExamSubjectInfo> getArrangeExamSubjectInfo(Map<String, Object> map, String termInfo, Integer autoIncr) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfo, autoIncr, map);
		List<ArrangeExamSubjectInfo> result = selectList(MYBATIS_PREFIX + "getArrangeExamSubjectInfo", splitDbAndTableRule);
		if(result == null) {
			return new ArrayList<ArrangeExamSubjectInfo>();
		}
		return result;
	}
	
	public List<JSONObject> getExamSubjectGroupList(Map<String, Object> map, String termInfo, Integer autoIncr) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfo, autoIncr, map);
		List<JSONObject> result = selectList(MYBATIS_PREFIX + "getExamSubjectGroupList", splitDbAndTableRule);
		if(result == null) {
			return new ArrayList<JSONObject>();
		}
		return result;
	}

	public List<ArrangeExamPlaceInfo> getArrangeExamPlaceInfo(Map<String, Object> map, String termInfo, Integer autoIncr) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfo, autoIncr, map);
		List<ArrangeExamPlaceInfo> result = selectList(MYBATIS_PREFIX + "getArrangeExamPlaceInfo", splitDbAndTableRule);
		if(result == null) {
			return new ArrayList<ArrangeExamPlaceInfo>();
		}
		return result;
	}
	
	public int deleteArrangeExamRule(Map<String, Object> map, String termInfo, Integer autoIncr) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfo, autoIncr, map);
		return delete(MYBATIS_PREFIX + "deleteArrangeExamRule", splitDbAndTableRule);
	}
	
	public int deleteArrangeExamClassInfo(Map<String, Object> map, String termInfo, Integer autoIncr) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfo, autoIncr, map);
		return delete(MYBATIS_PREFIX + "deleteArrangeExamClassInfo", splitDbAndTableRule);
	}
	
	public int deleteArrangeExamSubjectInfo(Map<String, Object> map, String termInfo, Integer autoIncr) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfo, autoIncr, map);
		return delete(MYBATIS_PREFIX + "deleteArrangeExamSubjectInfo", splitDbAndTableRule);
	}
	
	public int deleteArrangeExamPlaceInfo(Map<String, Object> map, String termInfo, Integer autoIncr) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfo, autoIncr, map);
		return delete(MYBATIS_PREFIX + "deleteArrangeExamPlaceInfo", splitDbAndTableRule);
	}
	
	public int insertArrExamRule(Object obj, String termInfo, Integer autoIncr) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfo, autoIncr, obj);
		return insert(MYBATIS_PREFIX + "insertArrExamRule", splitDbAndTableRule);
	}
	
	public int insertArrExamClassInfoBatch(Object obj, String termInfo, Integer autoIncr) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfo, autoIncr, obj);
		return insert(MYBATIS_PREFIX + "insertArrExamClassInfoBatch", splitDbAndTableRule);
	}
	
	public int insertArrExamSubjInfoBatch(Object obj, String termInfo, Integer autoIncr) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfo, autoIncr, obj);
		return insert(MYBATIS_PREFIX + "insertArrExamSubjInfoBatch", splitDbAndTableRule);
	}
	@Override
	public List<JSONObject> getCommPlaceList(Object obj, String termInfo, Integer autoIncr){
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfo, autoIncr, obj);
		return selectList(MYBATIS_PREFIX + "getCommPlaceList", splitDbAndTableRule);
	}
	
	public int insertArrExamPlaceInfoBatch(Object obj, String termInfo, Integer autoIncr) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfo, autoIncr, obj);
		return insert(MYBATIS_PREFIX + "insertArrExamPlaceInfoBatch", splitDbAndTableRule);
	}
	
	public int updateRemainNumOfStuds(Object obj, String termInfo, Integer autoIncr) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfo, autoIncr, obj);
		return insert(MYBATIS_PREFIX + "updateRemainNumOfStuds", splitDbAndTableRule);
	}
	
	public int insertArrExamResultBatch(List<?> list, String termInfo, Integer autoIncr) {	// 数据量大，分批次插入
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfo, autoIncr, null);
		return insertBatch(list, splitDbAndTableRule, "insertArrExamResultBatch");
	}
	
	public int insertTestNumberInfoBatch(List<?> list, String termInfo, Integer autoIncr) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfo, autoIncr, null);
		return insertBatch(list, splitDbAndTableRule, "insertTestNumberInfoBatch");
	}
	
	public int insertStudsWaitingBatch(List<?> list, String termInfo, Integer autoIncr) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfo, autoIncr, null);
		return insertBatch(list, splitDbAndTableRule, "insertStudsWaitingBatch");
	}
	
	public List<TestNumberInfo> getTestNumberInfo(Map<String, Object> map, String termInfo, Integer autoIncr) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfo, autoIncr, map);
		List<TestNumberInfo> result = selectList(MYBATIS_PREFIX + "getTestNumberInfo", splitDbAndTableRule);
		if(result == null) {
			return new ArrayList<TestNumberInfo>();
		}
		return result;
	}
	
	public int deleteTestNumberInfo(Map<String, Object> map, String termInfo, Integer autoIncr) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfo, autoIncr, map);
		return delete(MYBATIS_PREFIX + "deleteTestNumberInfo", splitDbAndTableRule);
	}
	
	public int deleteArrExamResult(Map<String, Object> map, String termInfo, Integer autoIncr) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfo, autoIncr, map);
		return delete(MYBATIS_PREFIX + "deleteArrExamResult", splitDbAndTableRule);
	}
	
	public List<StudNotTakingExam> getStudsNotTakingExam(Map<String, Object> map, String termInfo, Integer autoIncr) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfo, autoIncr, map);
		List<StudNotTakingExam> result = selectList(MYBATIS_PREFIX + "getStudsNotTakingExam", splitDbAndTableRule);
		if(result == null) {
			return new ArrayList<StudNotTakingExam>();
		}
		return result;
	}
	
	public int deleteExamPlaceInfo(Map<String, Object> map, String termInfo, Integer autoIncr) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfo, autoIncr, map);
		return delete(MYBATIS_PREFIX + "deleteExamPlaceInfo", splitDbAndTableRule);
	}
	
	public int deleteStudsNotTakingExam(Map<String, Object> map, String termInfo, Integer autoIncr) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfo, autoIncr, map);
		return delete(MYBATIS_PREFIX + "deleteStudsNotTakingExam", splitDbAndTableRule);
	}
	
	public int deleteStudsWaiting(Map<String, Object> map, String termInfo, Integer autoIncr) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfo, autoIncr, map);
		return delete(MYBATIS_PREFIX + "deleteStudsWaiting", splitDbAndTableRule);
	}
	
	public String produceTestNumber(ExamPlan ep, Long serialNumber, String xn) {
		// 设置考号，设置前缀
		StringBuffer testNumber = new StringBuffer().append(ep.getTestNumPrefix());
		
		Integer gradeDigit = ep.getGradeDigit();
		Integer serialNumDigit = ep.getSerialNumDigit();
		
		String rxnd = commonDataService.ConvertSYNJ2RXND(ep.getUsedGrade(), xn);
		
		// 设置考号级
		testNumber.append(rxnd.substring(rxnd.length() > gradeDigit ? (rxnd.length() - gradeDigit) : 0,
				rxnd.length()));
		// 设置考号流水
		testNumber.append(String.format("%0" + serialNumDigit + "d", serialNumber % (int) Math.pow(10, serialNumDigit)));
		return testNumber.toString();
	}
	
	public List<JSONObject> getScoreList(Map<String, Object> map) {
		List<JSONObject> result = selectList(MYBATIS_PREFIX + "getExternalScoreList", map);
		if(result == null) {
			return new ArrayList<JSONObject>();
		}
		return result;
	}
	
	public List<JSONObject> getScoreInfo(Map<String, Object> map) {
		List<JSONObject> result = selectList(MYBATIS_PREFIX + "getScoreInfo", map);
		if(result == null) {
			return new ArrayList<JSONObject>();
		}
		return result;
	}

	@Override
	public List<JSONObject> getArrangeExamSubjectAndsubject(JSONObject request,
			String termInfo, Integer autoIncr) {
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(termInfo, autoIncr, request);
		List<JSONObject> result = selectList(MYBATIS_PREFIX + "getArrangeExamSubjectAndsubject", splitDbAndTableRule);
		return result;
	}

	@Override
	public JSONObject getDegreeinfo(Map<String, Object> map) {
		JSONObject  result = selectOne(MYBATIS_PREFIX + "getDegreeinfo"  , map);
		return result;
	}
}
