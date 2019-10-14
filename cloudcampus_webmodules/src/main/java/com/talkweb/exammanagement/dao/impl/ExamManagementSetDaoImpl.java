package com.talkweb.exammanagement.dao.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.base.dao.impl.MyBatisBaseDaoImpl;
import com.talkweb.common.splitDbAndTable.SplitDbAndTableRule;
import com.talkweb.common.splitDbAndTable.SplitDbAndTableRuleFactory;
import com.talkweb.exammanagement.dao.ExamManagementSetDao;
@Repository
public class ExamManagementSetDaoImpl extends MyBatisBaseDaoImpl implements ExamManagementSetDao{
	
	@Autowired
	private SplitDbAndTableRuleFactory splitDbAndTableRuleFactory;
	
	public static final String packages="com.talkweb.exammanagement.dao.ExamManagementSetDao.";

	@Override
	public List<JSONObject> getExamPlaceList(Map<String, Object> param) {
		// TODO Auto-generated method stub
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(param.get("termInfo").toString(),  param.get("autoIncr")==null?null:(int)param.get("autoIncr"), param);
		return selectList(packages+"getExamPlaceList",splitDbAndTableRule);
	}

	@Override
	public List<JSONObject> getHasOldExamPlaceList(Map<String, Object> param) {
		// TODO Auto-generated method stub
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(param.get("termInfo").toString(), param.get("autoIncr")==null?null: param.get("autoIncr")==null?null:(int)param.get("autoIncr"), param);
		return selectList(packages+"getHasOldExamPlaceList",splitDbAndTableRule);
	}

	@Override
	public void deleteExamPlace(Map<String, Object> param) {
		// TODO Auto-generated method stub
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(param.get("termInfo").toString(),   param.get("autoIncr")==null?null:(int)param.get("autoIncr"), param);
		delete(packages+"deleteExamPlace",splitDbAndTableRule);
	}

	@Override
	public List<JSONObject> getExamPlace(Map<String, Object> param) {
		// TODO Auto-generated method stub
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(param.get("termInfo").toString(),   param.get("autoIncr")==null?null:(int)param.get("autoIncr"), param);
		return selectList(packages+"getExamPlace",splitDbAndTableRule);
	}

	@Override
	public void saveExamPlace(Map<String, Object> param) {
		// TODO Auto-generated method stub
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(param.get("termInfo").toString(),   param.get("autoIncr")==null?null:(int)param.get("autoIncr"), param);
		update(packages+"saveExamPlace",splitDbAndTableRule);
	}
	
	@Override
	public void updateExamPlace(Map<String, Object> param) {
		// TODO Auto-generated method stub
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(param.get("termInfo").toString(),   param.get("autoIncr")==null?null:(int)param.get("autoIncr"), param);
		update(packages+"updateExamPlace",splitDbAndTableRule);
	}
	
	@Override
	public List<JSONObject> getOldExamPlace(Map<String, Object> param) {
		// TODO Auto-generated method stub
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(param.get("copyTermInfo").toString(),   param.get("autoIncr")==null?null:(int)param.get("autoIncr"), param);
		return selectList(packages+"getOldExamPlace",splitDbAndTableRule);
	}

	@Override
	public List<JSONObject> getNonparticipationExamList(
			Map<String, Object> param) {
		// TODO Auto-generated method stub
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(param.get("termInfo").toString(),   param.get("autoIncr")==null?null:(int)param.get("autoIncr"), param);
		return selectList(packages+"getNonparticipationExamList",splitDbAndTableRule);
	}
	@Override
	public List<JSONObject> getExplantSubject(
			Map<String, Object> param) {
		// TODO Auto-generated method stub
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(param.get("termInfo").toString(),   param.get("autoIncr")==null?null:(int)param.get("autoIncr"), param);
		return selectList(packages+"getExplantSubject",splitDbAndTableRule);
	}
	
	
	@Override
	public void saveNonparticipationExamList(Map<String, Object> param) {
		// TODO Auto-generated method stub
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(param.get("termInfo").toString(),   param.get("autoIncr")==null?null:(int)param.get("autoIncr"), param);
		update(packages+"saveNonparticipationExamList",splitDbAndTableRule);
	}

	@Override
	public void deleteNonparticipationExamList(Map<String, Object> param) {
		// TODO Auto-generated method stub
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(param.get("termInfo").toString(),   param.get("autoIncr")==null?null:(int)param.get("autoIncr"), param);
		update(packages+"deleteNonparticipationExamList",splitDbAndTableRule);
	}

	@Override
	public List<JSONObject> getUserExamPlace(Map<String, Object> param) {
		// TODO Auto-generated method stub
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(param.get("termInfo").toString(),   param.get("autoIncr")==null?null:(int)param.get("autoIncr"), param);
		return selectList(packages+"getUserExamPlace",splitDbAndTableRule);
	}

	@Override
	public List<JSONObject> studsWaiting(Map<String, Object> param) {
		// TODO Auto-generated method stub
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(param.get("termInfo").toString(),   param.get("autoIncr")==null?null:(int)param.get("autoIncr"), param);
		return selectList(packages+"studsWaiting",splitDbAndTableRule);
	}

	@Override
	public List<JSONObject> getStudsInExamPlace(Map<String, Object> param) {
		// TODO Auto-generated method stub
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(param.get("termInfo").toString(),   param.get("autoIncr")==null?null:(int)param.get("autoIncr"), param);
		return selectList(packages+"getStudsInExamPlace",splitDbAndTableRule);
	}

	@Override
	public void saveArrangeExamResult(Map<String, Object> param) {
		// TODO Auto-generated method stub
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(param.get("termInfo").toString(),   param.get("autoIncr")==null?null:(int)param.get("autoIncr"), param);
		update(packages+"saveArrangeExamResult",splitDbAndTableRule);
	}

	@Override
	public void deleteArrangeExamResult(Map<String, Object> param) {
		// TODO Auto-generated method stub
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(param.get("termInfo").toString(),   param.get("autoIncr")==null?null:(int)param.get("autoIncr"), param);
		update(packages+"deleteArrangeExamResult",splitDbAndTableRule);
	}

	@Override
	public void saveStudsWaiting(Map<String, Object> param) {
		// TODO Auto-generated method stub
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(param.get("termInfo").toString(),   param.get("autoIncr")==null?null:(int)param.get("autoIncr"), param);
		update(packages+"saveStudsWaiting",splitDbAndTableRule);
	}

	@Override
	public void deleteStudsWaiting(Map<String, Object> param) {
		// TODO Auto-generated method stub
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(param.get("termInfo").toString(),   param.get("autoIncr")==null?null:(int)param.get("autoIncr"), param);
		update(packages+"deleteStudsWaiting",splitDbAndTableRule);
	}

	@Override
	public void deleteExamResult(Map<String, Object> param) {
		// TODO Auto-generated method stub
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(param.get("termInfo").toString(),   param.get("autoIncr")==null?null:(int)param.get("autoIncr"), param);
		update(packages+"deleteExamResult",splitDbAndTableRule);
	}

	@Override
	public List<JSONObject> getMaxtestNumber(Map<String, Object> param) {
		// TODO Auto-generated method stub
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(param.get("termInfo").toString(),   param.get("autoIncr")==null?null:(int)param.get("autoIncr"), param);
		return selectList(packages+"getMaxtestNumber",splitDbAndTableRule);
	}

	@Override
	public List<JSONObject> getSubjectGbyResult(Map<String, Object> param) {
		// TODO Auto-generated method stub
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(param.get("termInfo").toString(),   param.get("autoIncr")==null?null:(int)param.get("autoIncr"), param);
		return selectList(packages+"getSubjectGbyResult",splitDbAndTableRule);
	}

	@Override
	public List<JSONObject> getstudsWaitingSubject(JSONObject param) {
		// TODO Auto-generated method stub
		SplitDbAndTableRule splitDbAndTableRule = splitDbAndTableRuleFactory.getRule(param.get("termInfo").toString(),   param.get("autoIncr")==null?null:(int)param.get("autoIncr"), param);
		return selectList(packages+"getstudsWaitingSubject",splitDbAndTableRule);
	}

}
