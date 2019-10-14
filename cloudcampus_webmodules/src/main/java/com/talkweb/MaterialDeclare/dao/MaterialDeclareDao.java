package com.talkweb.MaterialDeclare.dao;

import java.util.List;

import com.alibaba.fastjson.JSONObject;

public interface MaterialDeclareDao {

	public  JSONObject  getAuditMaterialDeclare(JSONObject param);
 
	
	public Integer getAdminMaterialDeclareCnt(JSONObject param);
	public Integer getApplayMaterialDeclareCnt(JSONObject param);
	public Integer getAuditMaterialDeclareCnt(JSONObject param);
	public Integer getAuditedMaterialDeclareCnt(JSONObject param);	
	
	public List<JSONObject> getAdminMaterialDeclareList(JSONObject param);
	public List<JSONObject> getApplayMaterialDeclareList(JSONObject param);
	public List<JSONObject> getAuditMaterialDeclareList(JSONObject param);
	public List<JSONObject> getAuditedMaterialDeclareList(JSONObject param);
	
 
	public int insertMaterialDeclareDepartment(JSONObject param);
	public int deleteMaterialDeclareDepartment(JSONObject param);
	public int deleteMaterialDeclareDepartment2(JSONObject param);
	public Integer getDepartmentCnt(JSONObject param);
	public int updateMaterialDeclareDepartment(JSONObject param);
	public List<JSONObject> getMaterialDeclareDepartment(JSONObject param);
	
	public JSONObject  getMaterialDeclareDetail(JSONObject param);
	public int insertMaterialDeclare(JSONObject param);
	public int deleteMaterialDeclare(JSONObject param);
	public int updateMaterialDeclareStatus(JSONObject param);
	public int updateMaterialDeclareCount(JSONObject param);
	
	
	public int insertMaterialDeclareAuditMenber(List<JSONObject> list);
	public int deleteMaterialDeclareAuditMenber(JSONObject param);
	public List<JSONObject> getMaterialDeclareAuditMenber(JSONObject param);
	public JSONObject getMaterialDeclareAuditMenberByTotal(JSONObject param);
	
	public int insertMaterialDeclareProcedureMenber(List<JSONObject> list);
	public int deleteMaterialDeclareProcedureMenber(JSONObject param);
	
	
	public int insertMaterialDeclareProcedure(List<JSONObject> list);
	public int deleteMaterialDeclareProcedure(JSONObject param);
	public int updateMaterialDeclareProcedure(JSONObject param);
	
	public int insertMaterialDeclareItemdetail(List<JSONObject> list);
	public int deleteMaterialDeclareItemdetail(JSONObject param);
	
	public List<JSONObject> getMaterialDeclareStatistics(JSONObject param);
	
	public JSONObject getMaterialDeclareProcedureLevelNum(JSONObject param);
	public JSONObject getMaterialDeclareProcedureTeacherLevelNum(JSONObject param);
	public Integer getHasSetMember(JSONObject param);
	
	public List<JSONObject> getItemDetailById(String applicationId);
	public List<JSONObject> getProcedureMember(String applicationId);
	public List<JSONObject> getProcedure(String applicationId);
	public JSONObject getApplicationById(String applicationId);
	public JSONObject getSchoolById(JSONObject param);
	
}
