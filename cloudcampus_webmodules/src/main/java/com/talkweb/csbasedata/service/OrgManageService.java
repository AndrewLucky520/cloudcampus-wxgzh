package com.talkweb.csbasedata.service;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;

public interface OrgManageService {
		//科室
		List<JSONObject> getDepartmentList(JSONObject obj)throws Exception;
		int updateDepartment(JSONObject obj)throws Exception;
		int updateDepartmentMember(JSONObject obj)throws Exception;
		JSONObject getDepartmentInfo(JSONObject obj)throws Exception;
		int deleteDepartment(JSONObject obj)throws Exception;
		int deleteDepartmentMember(JSONObject param)throws Exception;
		//导入
		int addImportDepartmentBatch(Map<String, Object> needInsert)throws Exception;
				
		//年级组
		List<JSONObject> getGradegroupList(JSONObject obj)throws Exception;
		JSONObject updateGradegroup(JSONObject obj)throws Exception;
		JSONObject getGradegroupInfo(JSONObject obj)throws Exception;
		int deleteGradegroup(JSONObject obj)throws Exception;
		//教研组
		JSONObject getResearchgroupInfo(JSONObject param)throws Exception;
		JSONObject updateResearchgroup(JSONObject param)throws Exception;
		int deleteResearchgroup(JSONObject param)throws Exception;
		List<JSONObject> getResearchgroupList(JSONObject param)throws Exception;
		//备课组
		List<JSONObject> getPreparationList(JSONObject param)throws Exception;
		int deletePreparation(JSONObject param)throws Exception;
		JSONObject getPreparationInfo(JSONObject param)throws Exception;
		JSONObject updatePreparation(JSONObject param)throws Exception;
		
}
