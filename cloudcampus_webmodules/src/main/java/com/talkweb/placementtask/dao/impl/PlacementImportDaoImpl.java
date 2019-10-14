package com.talkweb.placementtask.dao.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Repository;

import com.talkweb.base.dao.impl.MyBatisBaseDaoImpl;
import com.talkweb.placementtask.dao.PlacementImportDao;
import com.talkweb.placementtask.domain.TPlConfIndexSubs;
import com.talkweb.placementtask.domain.TPlDezyClass;
import com.talkweb.placementtask.domain.TPlDezyClassgroup;
import com.talkweb.placementtask.domain.TPlDezySubjectcomp;
import com.talkweb.placementtask.domain.TPlDezySubjectcompStudent;
import com.talkweb.placementtask.domain.TPlDezyTclassSubcomp;
import com.talkweb.placementtask.domain.TPlDezyTclassfrom;
import com.talkweb.placementtask.vo.SubjectGroupIdInfo;

/**
 * 导入持久接口
 * @author hushowly@foxmail.com
 */
@Repository
public class PlacementImportDaoImpl extends MyBatisBaseDaoImpl  implements  PlacementImportDao{
	
	
	@Override
	public List<SubjectGroupIdInfo> selectDistinctSubjectGroupIdByPlacementId(String termInfo, String placementId, String schoolId){
		Map<String, Object> params = new HashMap<>();
		params.put("termInfo", termInfo);
		params.put("placementId", placementId);
		params.put("schoolId", schoolId);
		return selectList("com.talkweb.placementtask.dao.import.selectDistinctSubjectGroupIdByPlacementId", params);
	}
	
	
	@Override
	public List<String> selectDistinctClassGroupIdByPlacementId(String termInfo, String placementId, String schoolId){
		Map<String, Object> params = new HashMap<>();
		params.put("termInfo", termInfo);
		params.put("placementId", placementId);
		params.put("schoolId", schoolId);
		return selectList("com.talkweb.placementtask.dao.import.selectDistinctClassGroupIdByPlacementId", params);
	}
	
	@Override
	public int updateClassGroupClassIds(String termInfo, String placementId, String schoolId, List<String> classIds) {
		Map<String, Object> params = new HashMap<>();
		params.put("termInfo", termInfo);
		params.put("placementId", placementId);
		params.put("schoolId", schoolId);
		params.put("classIds", StringUtils.join(classIds, ","));
		return update("com.talkweb.placementtask.dao.import.updateClassGroupClassIds", params);
	}
	
	
	@Override
	public int deleteTPlDezyClassgroup(String termInfo, String placementId, String schoolId) {
		Map<String, Object> params = new HashMap<>();
		params.put("termInfo", termInfo);
		params.put("placementId", placementId);
		params.put("schoolId", schoolId);
		return insert("com.talkweb.placementtask.dao.import.deleteTPlDezyClassgroup", params);
	}
	
	@Override
	public int batchInsertTPlDezyClassgroupList(String termInfo, List<TPlDezyClassgroup> list) {
		Map<String, Object> params = new HashMap<>();
		params.put("termInfo", termInfo);
		params.put("list", list);
		return insert("com.talkweb.placementtask.dao.import.batchInsertTPlDezyClassgroupList", params);
	}
	
	@Override
	public int deleteTPlDezyClass(String termInfo, String placementId, String schoolId) {
		Map<String, Object> params = new HashMap<>();
		params.put("termInfo", termInfo);
		params.put("placementId", placementId);
		params.put("schoolId", schoolId);
		return insert("com.talkweb.placementtask.dao.import.deleteTPlDezyClass", params);
	}
	
	@Override
	public int batchInsertTPlDezyClassList(String termInfo, List<TPlDezyClass> list) {
		Map<String, Object> params = new HashMap<>();
		params.put("termInfo", termInfo);
		params.put("list", list);
		return insert("com.talkweb.placementtask.dao.import.batchInsertTPlDezyClassList", params);
	}
	
	@Override
	public int deleteTPlDezySubjectcomp(String termInfo, String placementId, String schoolId) {
		Map<String, Object> params = new HashMap<>();
		params.put("termInfo", termInfo);
		params.put("placementId", placementId);
		params.put("schoolId", schoolId);
		return insert("com.talkweb.placementtask.dao.import.deleteTPlDezySubjectcomp", params);
	}
	
	@Override
	public int batchInsertTPlDezySubjectcompList(String termInfo, List<TPlDezySubjectcomp> list) {
		Map<String, Object> params = new HashMap<>();
		params.put("termInfo", termInfo);
		params.put("list", list);
		return insert("com.talkweb.placementtask.dao.import.batchInsertTPlDezySubjectcompList", params);
	}
	
	
	@Override
	public int deleteTPlDezyTclassSubcomp(String termInfo, String placementId, String schoolId) {
		Map<String, Object> params = new HashMap<>();
		params.put("termInfo", termInfo);
		params.put("placementId", placementId);
		params.put("schoolId", schoolId);
		return insert("com.talkweb.placementtask.dao.import.deleteTPlDezyTclassSubcomp", params);
	}
	
	@Override
	public int batchInsertTPlDezyTclassSubcompList(String termInfo, List<TPlDezyTclassSubcomp> list) {
		Map<String, Object> params = new HashMap<>();
		params.put("termInfo", termInfo);
		params.put("list", list);
		return insert("com.talkweb.placementtask.dao.import.batchInsertTPlDezyTclassSubcompList", params);
	}
	
	@Override
	public int deleteTPlDezyTclassfrom(String termInfo, String placementId, String schoolId) {
		Map<String, Object> params = new HashMap<>();
		params.put("termInfo", termInfo);
		params.put("placementId", placementId);
		params.put("schoolId", schoolId);
		return insert("com.talkweb.placementtask.dao.import.deleteTPlDezyTclassfrom", params);
	}
	
	@Override
	public int batchInsertTPlDezyTclassfromList(String termInfo, List<TPlDezyTclassfrom> list) {
		Map<String, Object> params = new HashMap<>();
		params.put("termInfo", termInfo);
		params.put("list", list);
		return insert("com.talkweb.placementtask.dao.import.batchInsertTPlDezyTclassfromList", params);
	}
	
	@Override
	public int deleteTPlDezySubjectcompStudent(String termInfo, String placementId, String schoolId) {
		Map<String, Object> params = new HashMap<>();
		params.put("termInfo", termInfo);
		params.put("placementId", placementId);
		params.put("schoolId", schoolId);
		return insert("com.talkweb.placementtask.dao.import.deleteTPlDezySubjectcompStudent", params);
	}
	
	@Override
	public int batchInsertTPlDezySubjectcompStudentList(String termInfo, List<TPlDezySubjectcompStudent> list) {
		Map<String, Object> params = new HashMap<>();
		params.put("termInfo", termInfo);
		params.put("list", list);
		return insert("com.talkweb.placementtask.dao.import.batchInsertTPlDezySubjectcompStudentList", params);
	}
	
	@Override
	public int deleteTPlConfIndexSubs(String termInfo, String placementId, String schoolId) {
		Map<String, Object> params = new HashMap<>();
		params.put("termInfo", termInfo);
		params.put("placementId", placementId);
		params.put("schoolId", schoolId);
		return insert("com.talkweb.placementtask.dao.import.deleteTPlConfIndexSubs", params);
	}
	
	
	@Override
	public int batchInsertTPlConfIndexSubsList(String termInfo, List<TPlConfIndexSubs> list) {
		Map<String, Object> params = new HashMap<>();
		params.put("termInfo", termInfo);
		params.put("list", list);
		return insert("com.talkweb.placementtask.dao.import.batchInsertTPlConfIndexSubsList", params);
	}

}
