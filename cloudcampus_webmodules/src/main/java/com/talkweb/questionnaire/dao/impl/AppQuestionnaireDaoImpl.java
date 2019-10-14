package com.talkweb.questionnaire.dao.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.talkweb.accountcenter.thrift.AccountStructConstants;
import com.talkweb.accountcenter.thrift.Classroom;
import com.talkweb.accountcenter.thrift.Grade;
import com.talkweb.accountcenter.thrift.T_GradeLevel;
import com.talkweb.accountcenter.thrift.User;
import com.talkweb.base.dao.impl.MyBatisBaseDaoImpl;
import com.talkweb.common.tools.UUIDUtil;
import com.talkweb.commondata.service.AllCommonDataService;
import com.talkweb.questionnaire.dao.AppQuestionnaireDao;

@Repository
public class AppQuestionnaireDaoImpl extends MyBatisBaseDaoImpl implements AppQuestionnaireDao {
	private Map<T_GradeLevel, String> njName = AccountStructConstants.T_GradeLevelName;

	@Autowired
	private AllCommonDataService commonDataService;
	
	 private static final Logger logger = LoggerFactory.getLogger(AppQuestionnaireDaoImpl.class);
	
	public User getUserById(long schoolId, long userId) {
		return commonDataService.getUserById(schoolId, userId);
	}
	
	public List<JSONObject> queryAppQuestionnairesByUser(Map<String, Object> params){
		return selectList("queryAppQuestionListByUser", params);
	}
	
	public JSONObject queryRuleFortable(Map<String, Object> params){
		return selectOne("queryAppRuleFortable", params);
	}
	
	public List<JSONObject> queryOptionFortable(Map<String, Object> params){
		List<JSONObject> result = new ArrayList<JSONObject>();
		
		//获取所需要填写指标的总数
		JSONObject countJson = selectOne("queryAppOptionCountFortable", params);
		int cols = (int) params.get("cols");	// 从入参获取表格列数
		int lastIndex = 0;	// 通过总数和列数，可以计算出最后一个记录的序号
		if(countJson != null){
			lastIndex = countJson.getIntValue("COUNT") / cols - 1;	// 减一是减去表头占据的行数，表头的indexRow=0
		}
		params.put("maxVote", lastIndex);	// 对于最大票数需要做一个校验，最大票数不能大于可以总投票数，那么应是2个数中最小的
		
		//查询表格指标信息
		List<JSONObject> list = selectList("queryAppOptionFortable", params);
		if(CollectionUtils.isEmpty(list)){
			return result;
		}
		
		//循环遍历表格指标的选项id
		List<String> tableOptionIds = new ArrayList<String>();
		for(JSONObject obj : list){
			int tableOptionType = obj.getIntValue("tableOptionType");
			if(1 == tableOptionType){
				tableOptionIds.add(obj.getString("tableOptionId"));
			}
		}
		
		//通过表格id和记录的id获取记录的详细信息
		Map<String, String> tableOptionId2detailIdMap = new HashMap<String, String>();
		int vote = 0; // 查询后台详细记录数，计算已经投票数额
		if(params.get("recordId") != null && tableOptionIds.size() > 0){	// 存在记录并且指标选项id不为空
			params.put("tableOptionIds", tableOptionIds);
			//查询是否拥有详细记录
			List<JSONObject> detaillist = selectList("queryAppRecordDetailForTable", params);
			if(CollectionUtils.isNotEmpty(detaillist)){
				for(JSONObject obj : detaillist){
					String tableOptionId = obj.getString("tableOptionId");
					String evalRecordResultId = obj.getString("evalRecordResultId");
					tableOptionId2detailIdMap.put(tableOptionId, evalRecordResultId);
				}
			}
			
			JSONObject detailCount = selectOne("queryAppRecordDetailCountForTable", params);
			if(detailCount != null){
				vote = detailCount.getIntValue("COUNT");
			}
		}
		params.put("vote", vote);
		
		// indexRow = 0时表示表格头，在sql里面就通过indexRow和indexColumn排好序
		Map<Integer, String> head = new HashMap<Integer, String>();
		int preIndexRow = 0;
		for(JSONObject obj: list){
			int indexRow = (int) obj.remove("indexRow");
			int indexColumn = (int) obj.remove("indexColumn");
			String content = obj.getString("content");
			
			if(indexRow == 0){	// 添加表头数据
				head.put(indexColumn, content);
			}else{
				if(preIndexRow != indexRow) {	// 通过之前的indexRow判断，主要作用是行转列
					JSONObject json = new JSONObject();
					json.put("indexRow", indexRow);
					json.put("tableList", new JSONArray());
					if(lastIndex == indexRow){	// 判断是否最后一条数据
						json.put("IsFinish", 1);
					}else {
						json.put("IsFinish", 0);
					}
					result.add(json);
				}
				obj.put("tableHeadName", head.get(indexColumn));
				String tableOptionId = obj.getString("tableOptionId");
				int tableOptionType = obj.getIntValue("tableOptionType");
				if(tableOptionType == 1){
					if(tableOptionId2detailIdMap.containsKey(tableOptionId)){
						obj.put("recordDetailId", tableOptionId2detailIdMap.get(tableOptionId));
						obj.put("isChecked", 1);
					}else{
						obj.put("isChecked", 2);
					}
				}
				result.get(result.size() - 1).getJSONArray("tableList").add(obj);
			}
			preIndexRow = indexRow;
		}
		
		return result;
	}
	
	public List<JSONObject> queryTargetForSpecific(Map<String, Object> params) {
		JSONObject countJson = selectOne("queryAppTargetCountForSpecific", params);
		int count = 0;
		if(countJson != null) {	//计算总数据，因为序号是从1开始计算的，因此不需要count在-1
			count = countJson.getIntValue("COUNT");
		}
		
		// 查询指标
		List<JSONObject> targetList = selectList("queryAppTargetForSpecific", params);
		if(CollectionUtils.isEmpty(targetList)){
			return new ArrayList<JSONObject>();
		}
		// 遍历获取所有指标的id，即targetId
		List<String> targetIds = new ArrayList<String>();
		for(JSONObject obj : targetList){
			targetIds.add(obj.getString("targetId"));
		}
		
		// 存放详细记录的Map，主观题的key为targetId，客观题的key为targetId+"_"+levelId
		Map<String, JSONObject> recordDetail = new HashMap<String, JSONObject>();
		if(params.get("recordId") != null && targetIds.size() > 0){	// 存在记录并且有指标
			params.put("targetIds", targetIds);
			// 通过questionId、schoolId和targetIds获取，客观题记录
			List<JSONObject> objectiveDetailList = selectList("queryAppRecordDetailForSpecific", params);
			if(CollectionUtils.isNotEmpty(objectiveDetailList)){
				for(JSONObject obj : objectiveDetailList){
					String targetId = obj.getString("targetId");
					String levelId = obj.getString("levelId");
					String key = targetId + "_" + levelId;
					recordDetail.put(key, obj);
				}
			}
			
			// 通过questionId、schoolId和targetIds获取，主观题记录
			List<JSONObject> subjectiveDetailList = selectList("queryAppSubjectResultForSpecific", params);
			if(CollectionUtils.isNotEmpty(subjectiveDetailList)){
				for(JSONObject obj : subjectiveDetailList){
					String targetId = obj.getString("targetId");
					recordDetail.put(targetId, obj);
				}
			}
		}
		
		// 通过questionId、schoolId、targetIds获取客观题指标的选项
		List<JSONObject> optionList = selectList("queryAppTargetLevelForSpecific", params);	// 查询指标选项
		// 存放指标选项的Map，key为targetId
		Map<String, List<JSONObject>> targetId2OptMap = new HashMap<String, List<JSONObject>>();
		if(CollectionUtils.isNotEmpty(optionList)){
			for(JSONObject obj : optionList){
				String targetId = obj.getString("targetId");
				String levelId = obj.getString("levelId");
				String key = targetId + "_" + levelId;
				
				if(recordDetail.containsKey(key)){	// 判断此Option是否填写选择
					JSONObject json = recordDetail.get(key);
					obj.put("isChecked", 1);
					if(1 == obj.getIntValue("isEdit")) {	// 如果此选项是可编辑选项，取得填写的内容
						obj.put("content", json.getString("content"));
					}
					obj.put("evalRecordResultId", json.getString("evalRecordResultId"));
				}else{
					obj.put("isChecked", 2);
				}
				
				if(!targetId2OptMap.containsKey(targetId)){	// 存放选项
					targetId2OptMap.put(targetId, new ArrayList<JSONObject>());
				}
				targetId2OptMap.get(targetId).add(obj);
			}
		}
		
		for(JSONObject obj : targetList){
			int targetType = obj.getIntValue("targetType");
			String targetId = obj.getString("targetId");
			int targetSeq = obj.getIntValue("targetSeq");
			
			if(count == targetSeq){	// 判断是否是最后一条记录，主要用于分页
				obj.put("IsFinish", 1);
			}else {
				obj.put("IsFinish", 0);
			}
			if(1 == targetType) {	// 客观题
				obj.put("levels", targetId2OptMap.get(targetId));
			} else {	// 主观题
				if(recordDetail.containsKey(targetId)){
					JSONObject json = recordDetail.get(targetId);
					obj.put("content", json.get("content"));
					obj.put("evalRecordResultId", json.get("evalRecordResultId"));
				}
			}
		}
		
		return targetList;
	}
	
	public int deleteRecordDetailForTable(Map<String, Object> params){
		//List<JSONObject> list = selectList("queryAppTableOptionIdByRowAndQuestionId", params);
		delete("deleteAppRecordDetail", params);
		/*if(CollectionUtils.isNotEmpty(list)){
			List<String> tableOptionIds = new ArrayList<String>();
			for(JSONObject obj : list){
				tableOptionIds.add(obj.getString("tableOptionId"));
			}
			params.put("tableOptionIds", tableOptionIds);
			delete("deleteAppRecordDetail", params);
			return 1;
		}*/
		return 0;
	}
	
	public JSONObject getGradeByClassId(long classId, long schoolId, String termInfo){
		String xn = termInfo.substring(0, termInfo.length() - 1);
		JSONObject result = new JSONObject();
		Classroom cr = commonDataService.getClassById(schoolId, classId, termInfo);
		long gradeId = cr.getGradeId();
		Grade grade = commonDataService.getGradeById(schoolId, gradeId, termInfo);
		T_GradeLevel gl = grade.getCurrentLevel();
		String usedGrade = commonDataService.ConvertNJDM2SYNJ(String.valueOf(gl.getValue()), xn);
		String gradeName = njName.get(gl);
		result.put("gradeName", gradeName);
		result.put("usedGrade", usedGrade);
		result.put("gradeId", gradeId);
		result.put("grade", grade);
		return result;
	}
	
	public String getCurTermInfoId(long schoolId){
		return commonDataService.getCurTermInfoId(schoolId);
	}
	
	public String getRecordIdByParams(Map<String, Object> params){
		JSONObject result = selectOne("getAppRecordIdByParams", params);
		if(result != null){
			return result.getString("recordId");
		} else {
			return null;
		}
	}
	
	public int insertRecord(Map<String, Object> params) {
		return insert("insertAppRecord", params);
	}
	
	public int insertRecordDetailForTable(Map<String, Object> params){
		return insert("insertAppRecordDetailForTable", params);
	}
	
	public int deleteAppRecordDetailForSpecific(Map<String, Object> params){
		int result = delete("deleteAppRecordDetailForSpecific", params);
		result += delete("deleteAppSubjectResultContentForSpecific", params);
		return result;
	}
	
	public int insertAppRecordDetailForSpecific(JSONArray array, String recordId, long schoolId){
		if(CollectionUtils.isEmpty(array)){
			return 0;
		}
		List<JSONObject> objData = new ArrayList<JSONObject>();		//客观题数据
		List<JSONObject> subjData = new ArrayList<JSONObject>();	//主观题数据
		
		for(int i = 0, len = array.size(); i < len; i ++){
			JSONObject obj = array.getJSONObject(i);
			int type = obj.getIntValue("targetType");
			String targetId = obj.getString("targetId");
			if(type == 1){	// 客观题结果
				JSONArray levels = obj.getJSONArray("levels");
				for(int j = 0, length = levels.size(); j < length; j ++){
					JSONObject objLevel = levels.getJSONObject(j);
					JSONObject json = new JSONObject();
					json.put("evalRecordResultId", UUIDUtil.getUUID());
					json.put("recordId", recordId);
					json.put("schoolId", schoolId);
					json.put("targetId", targetId);
					json.put("levelId", objLevel.get("levelId"));
					json.put("content", objLevel.get("content"));
					objData.add(json);
				}
			}else if(type == 2){	// 主观题结果
				JSONObject json = new JSONObject();
				json.put("evalRecordResultId", UUIDUtil.getUUID());
				json.put("recordId", recordId);
				json.put("schoolId", schoolId);
				json.put("targetId", targetId);
				json.put("content", obj.get("content"));
				subjData.add(json);
			}
			
		}
		
		int sum = 0;
		if(CollectionUtils.isNotEmpty(objData)){
			sum += insert("insertAppRecordDetailForSpecific", objData);
		}
		
		if(CollectionUtils.isNotEmpty(subjData)){
			sum += insert("insertAppSubjectResultForSpecific", subjData);
		}
		return sum;
	}

	@Override
	public int deleteAppRecord(JSONObject param) {
		 
		return  delete("deleteAppRecord" , param);
	}

	@Override
	public List<JSONObject> getRecorddetailfortable(Map<String, Object> params) {
		 
		return selectList("getRecorddetailfortable" , params);
	}
	
	
	
}





