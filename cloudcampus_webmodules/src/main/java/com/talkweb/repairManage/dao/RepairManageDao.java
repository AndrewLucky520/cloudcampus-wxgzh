package com.talkweb.repairManage.dao;

import java.util.List;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public interface RepairManageDao {

	List<JSONObject> getRepairInfoList(JSONObject param);
	
	List<JSONObject> getRepairInfoListPlus(JSONObject param);

	int deleteRepairInfo(JSONObject param);

	int addFeedbackInfo(JSONObject param);

	List<JSONObject> getFeedbackInfo(JSONObject param);

	int addRepairInfo(JSONObject param);

	int addRepair_person(List<JSONObject> lj);

	List<JSONObject> getRepairTypeInfo(JSONObject param);

	List<JSONObject> getRepairPersonInfo(JSONObject param);

	JSONObject getRepairDetail(JSONObject param);
	
	JSONObject getAPPRepairDetailCheck(JSONObject param);
	
	List<JSONObject> getRepair_person(String repairId);

	int updateRepairInfo(JSONObject param);

	int updateCheckRepairInfo(JSONObject param);

	int deleteRepair_person(String repairId);

	List<JSONObject> getRepairTypeList(JSONObject param);

	int deleteRepairTypeInfo(JSONObject param);

	int deleteRepairPersonInfo(JSONObject param);

	JSONObject getEditRepairType(JSONObject param);

	int addRepairTypeInfo(JSONObject param);

	int addRepairPersonInfo(JSONArray param1);

	int updateRepairTypeInfo(JSONObject param);

	List<JSONObject> getRepairStatistics(JSONObject param);

	String getIsCheckPerson(String repairTypeId);

	List<JSONObject> getAllRepairPersonByschoolId(List<String> repairIds);

	int getRepairTypeCount(JSONObject param);

	List<JSONObject> getOwnRepairInfoList(JSONObject param);

	List<String> getSelPersons(JSONObject param);

	List<JSONObject> getAllRepairInfoList(JSONObject param);

	List<JSONObject> getAdminRepairStatistics(JSONObject param);

	String getPersonsByRepairId(JSONObject param);

	int updateFeedbackState(JSONObject param);

	int addRepairEvalInfo(JSONObject param);

	List<JSONObject> getAllRepairFeedbackInfosById(List<String> repairIds);

	List<JSONObject> getAPPRepairInfoList(JSONObject param);

	List<JSONObject> getAPPRepair_person(String repairId);
	
	List<JSONObject> getRepairPersonByIds(JSONObject param);

	JSONObject getAPPRepairDetail(JSONObject param);

	int updateRepairEvalState(JSONObject param);
	
	int insertRepairpicture(List<JSONObject> param);
	int deleteRepairpicture(JSONObject param);
	List<JSONObject> selectRepairpictures(JSONObject param);

}
