package com.talkweb.MaterialDeclare.service;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.utils.Result;

public interface MaterialDeclareService {

	public  JSONObject  getAuditMaterialDeclare(JSONObject param);
 
	public int insertMaterialDeclareDepartment(JSONObject param);
	public int deleteMaterialDeclareDepartment(JSONObject param);
	public List<JSONObject> getMaterialDeclareDepartment(JSONObject param);
	
	public JSONObject  getMaterialDeclareDetail(JSONObject param);
	public int insertMaterialDeclare(JSONObject param);
	public int deleteMaterialDeclare(JSONObject param);

	public int insertMaterialDeclareAuditMenber(List<JSONObject> list);
	public int deleteMaterialDeclareAuditMenber(JSONObject param);
	public List<JSONObject> getMaterialDeclareAuditMenber(JSONObject param);

	public int updateMaterialDeclareProcedure(JSONObject param);
	
	public JSONObject updateMaterialDeclareProcedureNew(JSONObject param);
	
	public List<JSONObject> getMaterialDeclareStatistics(JSONObject param);
	public List<JSONObject> getAllTeacherList(JSONObject param);
	
	public Integer getAdminMaterialDeclareCnt(JSONObject param);
	public Integer getApplayMaterialDeclareCnt(JSONObject param);
	public Integer getAuditMaterialDeclareCnt(JSONObject param);
	public Integer getAuditedMaterialDeclareCnt(JSONObject param);	
	
	public List<JSONObject> getAdminMaterialDeclareList(JSONObject param);
	public List<JSONObject> getApplayMaterialDeclareList(JSONObject param);
	public List<JSONObject> getAuditMaterialDeclareList(JSONObject param);
	public List<JSONObject> getAuditedMaterialDeclareList(JSONObject param);
	
	public Integer getHasSetMember(JSONObject param);
	public List<JSONObject> getMaterialDeclareExportList(List<JSONObject> datas);
	
	public List<JSONObject> getItemDetailById(String applicationId);
	public List<JSONObject> getProcedureMember(String applicationId);
	public List<JSONObject> getProcedure(String applicationId);
	public JSONObject getApplicationById(String applicationId);
	
	public void sendMsg(JSONObject param,Result<JSONObject> result);
	
	public JSONObject getSchoolById(JSONObject param);
	
	public int updateMaterialDeclareCount(JSONObject param);
}
