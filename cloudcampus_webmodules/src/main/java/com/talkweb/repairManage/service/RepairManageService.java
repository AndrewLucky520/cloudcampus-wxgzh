package com.talkweb.repairManage.service;

import java.util.List;

import com.alibaba.fastjson.JSONObject;

public interface RepairManageService {

	List<JSONObject> getRepairInfoList(JSONObject param);
	
	List<JSONObject> getRepairInfoListPlus(JSONObject param);

	int deleteRepairInfo(JSONObject param);

	int addFeedbackInfo(JSONObject param);

	List<JSONObject> getFeedbackInfo(JSONObject param);

	int addRepairInfo(JSONObject param);

	List<JSONObject> getRepairTypeInfo(JSONObject param);

	List<JSONObject> getRepairPersonInfo(JSONObject param);

	JSONObject getRepairDetail(JSONObject param);

	int updateCheckRepairInfo(JSONObject param);

	int updateRepairInfo(JSONObject param);

	List<JSONObject> getRepairTypeList(JSONObject param);

	int deleteRepairTypeInfo(JSONObject param);

	JSONObject getEditRepairType(JSONObject param);

	int addRepairTypeInfo(JSONObject param);

	int updateRepairTypeInfo(JSONObject param);

	List<JSONObject> getRepairStatistics(JSONObject param);

	String getIsCheckPerson(String repairTypeId);

	int getRepairTypeCount(JSONObject param);

	List<JSONObject> getOwnRepairInfoList(JSONObject param);

	List<String> getSelPersons(JSONObject param);

	List<JSONObject> getAllRepairInfoList(JSONObject param);

	String getPersonsByRepairId(JSONObject param);

	int addRepairEvalInfo(JSONObject param);

	List<JSONObject> getAPPRepairInfoList(JSONObject param);
	
	List<JSONObject> getRepairPersonByIds(JSONObject param);

	JSONObject getAPPRepairDetail(JSONObject param);

	int updateRepairEvalState(JSONObject param);
	
	JSONObject getAPPRepairCheckDetail(JSONObject param)throws Exception;
	
	int deleteRepairpicture(JSONObject param);
	List<JSONObject> selectRepairpictures(JSONObject param);
	int insertRepairpicture(JSONObject param);
	
	JSONObject getRepairInfo(JSONObject param);

}
