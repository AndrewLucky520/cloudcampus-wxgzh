package com.talkweb.MaterialDeclare.dao.Impl;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.alibaba.fastjson.JSONObject;
import com.talkweb.MaterialDeclare.dao.MaterialDeclareDao;
import com.talkweb.base.dao.impl.MyBatisBaseDaoImpl;

@Repository
public class MaterialDeclareDaoImpl  extends MyBatisBaseDaoImpl  implements MaterialDeclareDao{

 
	@Override
	public int insertMaterialDeclareDepartment(JSONObject param) {
	 
		return insert("insertMaterialDeclareDepartment" , param);
	}

	@Override
	public int deleteMaterialDeclareDepartment(JSONObject param) {
		 
		return delete("deleteMaterialDeclareDepartment" , param);
	}

	@Override
	public List<JSONObject> getMaterialDeclareDepartment(JSONObject param) {
		 
		return selectList("getMaterialDeclareDepartment" , param);
	}

	@Override
	public JSONObject getMaterialDeclareDetail(JSONObject param) {
		 
		return selectOne("getMaterialDeclareDetail", param);
	}

	@Override
	public int insertMaterialDeclare(JSONObject param) {
		 
		return insert("insertMaterialDeclare" , param);
	}

	@Override
	public int deleteMaterialDeclare(JSONObject param) {
		 
		return delete("deleteMaterialDeclare" , param);
	}

	@Override
	public int updateMaterialDeclareStatus(JSONObject param) {
		return update("updateMaterialDeclareStatus" , param);
	}

	@Override
	public int insertMaterialDeclareAuditMenber(List<JSONObject> list) {
	 
		return insert("insertMaterialDeclareAuditMenber" , list);
	}

	@Override
	public int deleteMaterialDeclareAuditMenber(JSONObject param) {
	 
		return delete("deleteMaterialDeclareAuditMenber" , param);
	}

	@Override
	public List<JSONObject> getMaterialDeclareAuditMenber(JSONObject param) {
		 
		return selectList("getMaterialDeclareAuditMenber" , param );
	}

	@Override
	public int insertMaterialDeclareProcedureMenber(List<JSONObject> list) {
		
		return insert("insertMaterialDeclareProcedureMenber" , list);
	}

	@Override
	public int deleteMaterialDeclareProcedureMenber(JSONObject param) {
		 
		return delete("deleteMaterialDeclareProcedureMenber" , param);
	}

	@Override
	public int insertMaterialDeclareProcedure(List<JSONObject> list) {
		 
		return insert("insertMaterialDeclareProcedure" , list);
	}

	@Override
	public int deleteMaterialDeclareProcedure(JSONObject param) {
		 
		return delete("deleteMaterialDeclareProcedure" , param);
	}

	@Override
	public int updateMaterialDeclareProcedure(JSONObject param) {
		 
		return update("updateMaterialDeclareProcedure" , param);
	}

	@Override
	public JSONObject getAuditMaterialDeclare(JSONObject param) {
		List<JSONObject> list = selectList("getAuditMaterialDeclare" ,param);
		if (list!=null && list.size() > 0) {
			return list.get(0);
		}else {
			return null;
		}
	}

	@Override
	public int insertMaterialDeclareItemdetail(List<JSONObject> list) {
		 
		return insert("insertMaterialDeclareItemdetail" ,list );
	}

	@Override
	public int deleteMaterialDeclareItemdetail(JSONObject param) {
		 
		return delete("deleteMaterialDeclareItemdetail" , param );
	}

	@Override
	public JSONObject getMaterialDeclareAuditMenberByTotal(JSONObject param) {
		
		return selectOne("getMaterialDeclareAuditMenberByTotal" ,param );
	}

	@Override
	public int updateMaterialDeclareDepartment(JSONObject param) {
		 
		return update("updateMaterialDeclareDepartment" , param);
	}

	@Override
	public List<JSONObject> getMaterialDeclareStatistics(JSONObject param) {
		 
		return selectList("getMaterialDeclareStatistics" , param);
	}

	@Override
	public JSONObject getMaterialDeclareProcedureLevelNum(JSONObject param) {
		 
		return selectOne("getMaterialDeclareProcedureLevelNum" , param);
	}

	@Override
	public JSONObject getMaterialDeclareProcedureTeacherLevelNum(JSONObject param) {
		 
		return selectOne("getMaterialDeclareProcedureTeacherLevelNum" , param);
	}

	@Override
	public int deleteMaterialDeclareDepartment2(JSONObject param) {
		 
		return update("deleteMaterialDeclareDepartment2" ,param );
	}

	@Override
	public Integer getDepartmentCnt(JSONObject param) {
		 
		return selectOne("getDepartmentCnt" , param);
	}

	@Override
	public Integer getAdminMaterialDeclareCnt(JSONObject param) {
		 
		return selectOne("getAdminMaterialDeclareCnt" , param);
	}

	@Override
	public Integer getApplayMaterialDeclareCnt(JSONObject param) {
	 
		return selectOne("getApplayMaterialDeclareCnt" , param);
	}

	@Override
	public Integer getAuditMaterialDeclareCnt(JSONObject param) {
		 
		return selectOne("getAuditMaterialDeclareCnt" , param);
	}

	@Override
	public Integer getAuditedMaterialDeclareCnt(JSONObject param) {
 
		return selectOne("getAuditedMaterialDeclareCnt" , param);
	}

	@Override
	public List<JSONObject> getAdminMaterialDeclareList(JSONObject param) {
	 
		return selectList("getAdminMaterialDeclareList" , param);
	}

	@Override
	public List<JSONObject> getApplayMaterialDeclareList(JSONObject param) {
		 
		return selectList("getApplayMaterialDeclareList" , param);
	}

	@Override
	public List<JSONObject> getAuditMaterialDeclareList(JSONObject param) {
		 
		return selectList("getAuditMaterialDeclareList" , param);
	}

	@Override
	public List<JSONObject> getAuditedMaterialDeclareList(JSONObject param) {
		 
		return selectList("getAuditedMaterialDeclareList" , param);
	}

	@Override
	public Integer getHasSetMember(JSONObject param) {
		 
		return 	selectOne("getHasSetMember" , param);
	}

	@Override
	public List<JSONObject> getItemDetailById(String applicationId) {
		return selectList("getItemDetailById" , applicationId);
	}

	@Override
	public List<JSONObject> getProcedureMember(String applicationId) {
		return selectList("getProcedureMember" , applicationId);
	}

	@Override
	public List<JSONObject> getProcedure(String applicationId) {
		return selectList("getProcedure" , applicationId);
	}

	@Override
	public JSONObject getApplicationById(String applicationId) {
		return 	selectOne("getApplicationById" , applicationId);
	}

	@Override
	public JSONObject getSchoolById(JSONObject param) {
		return 	selectOne("getBaseDataSchoolById" , param);
	}

	@Override
	public int updateMaterialDeclareCount(JSONObject param) {
		return update("updateMaterialDeclareCount",param);
	}
	
}
