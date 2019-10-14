package com.talkweb.repairManage.dao.impl;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.base.dao.impl.MyBatisBaseDaoImpl;
import com.talkweb.repairManage.dao.RepairManageDao;

@Repository
public class RepairManageDaoImpl extends MyBatisBaseDaoImpl implements RepairManageDao {

	@Override
	public List<JSONObject> getRepairInfoList(JSONObject param) {
		return selectList("getRepairInfoList", param);
	}
	
	@Override
	public List<JSONObject> getRepairInfoListPlus(JSONObject param) {
		return selectList("getRepairInfoListPlus", param);
	}

	@Override
	public List<JSONObject> getOwnRepairInfoList(JSONObject param) {
		return selectList("getOwnRepairInfoList", param);
	}
	
	@Override
	public int deleteRepairInfo(JSONObject param) {
		return delete("deleteRepairInfo", param);
	}

	@Override
	public int addFeedbackInfo(JSONObject param) {
		return insert("addFeedbackInfo", param);
	}

	@Override
	public List<JSONObject> getFeedbackInfo(JSONObject param) {
		return selectList("getFeedbackInfo", param);
	}

	@Override
	public int addRepairInfo(JSONObject param) {
		return insert("addRepairInfo", param);
	}

	@Override
	public int addRepair_person(List<JSONObject> param) {
		return insert("addRepair_person", param);
	}

	@Override
	public List<JSONObject> getRepairTypeInfo(JSONObject param) {
		return selectList("getRepairTypeInfo", param);
	}

	@Override
	public List<JSONObject> getRepairPersonInfo(JSONObject param) {
		return selectList("getRepairPersonInfo",param);
	}

	@Override
	public JSONObject getRepairDetail(JSONObject param) {
		return selectOne("getRepairDetail", param);
	}

	@Override
	public List<JSONObject> getRepair_person(String repairId) {
		return selectList("getRepair_person", repairId);
	}

	@Override
	public int updateRepairInfo(JSONObject param) {
		return update("updateRepairInfo", param);
	}

	@Override
	public int updateCheckRepairInfo(JSONObject param) {
		return update("updateCheckRepairInfo", param);
	}

	@Override
	public int deleteRepair_person(String repairId) {
		return delete("deleteRepair_person",repairId);
	}

	@Override
	public List<JSONObject> getRepairTypeList(JSONObject param) {
		return selectList("getRepairTypeList",param);
	}

	@Override
	public int deleteRepairTypeInfo(JSONObject param) {
		return delete("deleteRepairTypeInfo", param);
	}

	@Override
	public int deleteRepairPersonInfo(JSONObject param) {
		return delete("deleteRepairPersonInfo", param);
	}

	@Override
	public JSONObject getEditRepairType(JSONObject param) {
		return selectOne("getEditRepairType", param);
	}

	@Override
	public int addRepairTypeInfo(JSONObject param) {
		return insert("addRepairTypeInfo", param);
	}

	@Override
	public int addRepairPersonInfo(JSONArray param1) {
		return insert("addRepairPersonInfo",param1);
	}

	@Override
	public int updateRepairTypeInfo(JSONObject param) {
		return update("updateRepairTypeInfo",param);
	}

	@Override
	public List<JSONObject> getRepairStatistics(JSONObject param) {
		return selectList("getRepairStatistics", param);
	}

	@Override
	public String getIsCheckPerson(String repairTypeId) {
		return selectOne("getIsCheckPerson", repairTypeId);
	}

	@Override
	public List<JSONObject> getAllRepairPersonByschoolId(List<String> list) {
		
		return selectList("getAllRepairPersonByschoolId", list);
	}

	@Override
	public int getRepairTypeCount(JSONObject param) {
		return selectOne("getRepairTypeCount", param);
	}

	@Override
	public List<String> getSelPersons(JSONObject param) {
		return selectList("getSelPersons", param);
	}

	@Override
	public List<JSONObject> getAllRepairInfoList(JSONObject param) {
		return selectList("getAllRepairInfoList",param);
	}

	@Override
	public List<JSONObject> getAdminRepairStatistics(JSONObject param) {
		return selectList("getAdminRepairStatistics", param);
	}

	@Override
	public String getPersonsByRepairId(JSONObject param) {
		return selectOne("getPersonsByRepairId", param);
	}

	@Override
	public int updateFeedbackState(JSONObject param) {
		return update("updateFeedbackState", param);
	}

	@Override
	public int addRepairEvalInfo(JSONObject param) {
		return update("addRepairEvalInfo",param);
	}

	@Override
	public List<JSONObject> getAllRepairFeedbackInfosById(List<String> list) {
		return selectList("getAllRepairFeedbackInfosById", list);
	}

	@Override
	public List<JSONObject> getAPPRepairInfoList(JSONObject param) {
		return selectList("getAPPRepairInfoList", param);
	}

	@Override
	public List<JSONObject> getAPPRepair_person(String repairId) {
		return selectList("getAPPRepair_person", repairId);
	}

	@Override
	public JSONObject getAPPRepairDetail(JSONObject param) {
		return selectOne("getAPPRepairDetail", param);
	}

	@Override
	public int updateRepairEvalState(JSONObject param) {
		// TODO Auto-generated method stub
		return update("updateRepairEvalState", param);
	}

	@Override
	public JSONObject getAPPRepairDetailCheck(JSONObject param) {
		return selectOne("getAPPRepairDetailCheck", param);
	}

	@Override
	public List<JSONObject> getRepairPersonByIds(JSONObject param) {
		// TODO Auto-generated method stub
		return selectList("getRepairPersonByIds", param);
	}

	@Override
	public int insertRepairpicture(List<JSONObject> list) {
		 
		return insert("insertRepairpicture", list);
	}

	@Override
	public int deleteRepairpicture(JSONObject param) {
		 
		return delete("deleteRepairpicture" , param);
	}

	@Override
	public List<JSONObject> selectRepairpictures(JSONObject param) {
		 
		return selectList("selectRepairpictures", param);
	}

}
